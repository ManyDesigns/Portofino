import {Component, Input, OnInit} from '@angular/core';
import {HttpClient, HttpHeaders, HttpParams} from '@angular/common/http';
import {PortofinoService} from "../portofino.service";
import {ClassAccessor, isInSummary, isSearchable, Property} from "../class-accessor";
import {MatTableDataSource, MatSort, PageEvent, Sort} from "@angular/material";

@Component({
  selector: 'portofino-crud',
  templateUrl: './crud.component.html',
  styleUrls: ['./crud.component.css']
})
export class CrudComponent implements OnInit {

  @Input()
  config: any;

  classAccessor: ClassAccessor;
  classAccessorPath = '/:classAccessor';
  configuationPath = '/:configuration';

  searchFields: Property[] = [];
  searchValues = {};
  searchResults: SearchResults;
  searchResultsDataSource = new MatTableDataSource();
  searchResultFields: Property[] = [];
  columnsToDisplay: string[] = [];
  @Input()
  pageSize: number;
  sortInfo: Sort;

  constructor(private http: HttpClient, public portofino: PortofinoService) { }

  ngOnInit() {
    this.http.get<ClassAccessor>(this.portofino.apiPath + this.config.path + this.classAccessorPath).subscribe(
      classAccessor => this.http.get<Configuration>(this.portofino.apiPath + this.config.path + this.configuationPath).subscribe(
        configuration => this.init(classAccessor, configuration)
      )
    );
  }

  protected init(classAccessor: ClassAccessor, configuration: Configuration) {
    this.classAccessor = classAccessor;
    this.classAccessor.properties.forEach(property => {
      if(isSearchable(property)) {
        this.searchFields.push(property);
      }
      if(isInSummary(property)) {
        this.searchResultFields.push(property);
        this.columnsToDisplay.push(property.name);
      }
    });
    if(!this.pageSize) {
      this.pageSize = configuration.rowsPerPage;
    }
    this.search();
  }

  getFieldId(prefix: string, field: Property) {
    return `crud-${prefix}-${this.classAccessor.name}-${field.name}`
  }

  search() {
    this.loadSearchResultsPage(0);
  }

  protected loadSearchResultsPage(page: number) {
    let params = new HttpParams();
    params = this.composeSearch(params);
    params = params.set("firstResult", (page * this.pageSize).toString());
    params = params.set("maxResults", this.pageSize.toString());
    if(this.sortInfo) {
      params = params.set("sortProperty", this.sortInfo.active);
      params = params.set("sortDirection", this.sortInfo.direction);
    }
    this.http.get<SearchResults>(this.portofino.apiPath + this.config.path, {params: params}).subscribe(
      results => {
        results.records = results['Result'];
        this.searchResults = results;
        this.searchResultsDataSource.data = this.searchResults.records;
      }
    );
  }

  protected composeSearch(params: HttpParams) {
    for (let p in this.searchValues) {
      if (this.searchValues[p] === null) {
        continue;
      }
      const property = this.searchFields.find(value => value.name == p);
      if (!property) {
        continue;
      }
      if (this.portofino.isDate(property)) {
        params = params.set(`search_${p}_min`, this.searchValues[p].valueOf().toString());
        params = params.set(`search_${p}_max`, this.searchValues[p].valueOf().toString());
      } else if (this.portofino.isNumber(property)) {
        params = params.set(`search_${p}_min`, this.searchValues[p].toString());
        params = params.set(`search_${p}_max`, this.searchValues[p].toString());
      } else {
        params = params.set(`search_${p}`, this.searchValues[p].toString());
      }
    }
    return params;
  }

  loadPage(event: PageEvent) {
    this.loadSearchResultsPage(event.pageIndex);
  }

  sort(sort: Sort) {
    this.sortInfo = sort;
    this.loadSearchResultsPage(0);
  }

  clearSearch() {
    this.searchValues = {};
  }

}

class SearchResults {
  recordsReturned: number;
  totalRecords: number;
  startIndex: number;
  records: object[];
}

class Configuration {
  rowsPerPage: number;
}
