import {Component, Input, OnInit} from '@angular/core';
import {HttpClient, HttpHeaders, HttpParams} from '@angular/common/http';
import {PortofinoService} from "../portofino.service";
import {ClassAccessor, isInSummary, isSearchable, Property} from "../class-accessor";
import {MatTableDataSource, PageEvent} from "@angular/material";

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

  searchFields: Property[] = [];
  searchValues = {};
  searchResults: SearchResults;
  searchResultsDataSource = new MatTableDataSource();
  searchResultFields: Property[] = [];
  columnsToDisplay: string[] = [];
  @Input()
  pageSize = 1;

  constructor(private http: HttpClient, public portofino: PortofinoService) { }

  ngOnInit() {
    this.http.get<ClassAccessor>(this.portofino.apiPath + this.config.path + this.classAccessorPath).subscribe(
      classAccessor => this.init(classAccessor)
    );
  }

  protected init(classAccessor: ClassAccessor) {
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
    let searchString = new HttpParams();
    for (let p in this.searchValues) {
      searchString = searchString.set(`search_${p}`, this.searchValues[p]);
    }
    params = params.set("searchString", searchString.toString());
    params = params.set("firstResult", (page * this.pageSize).toString());
    params = params.set("maxResults", this.pageSize.toString());
    this.http.get<SearchResults>(this.portofino.apiPath + this.config.path, {params: params}).subscribe(
      results => {
        results.records = results['Result'];
        this.searchResults = results;
        this.searchResultsDataSource.data = this.searchResults.records;
      }
    );
  }

  loadPage(event: PageEvent) {
    this.loadSearchResultsPage(event.pageIndex);
  }

  clearSearch() {
    this.searchValues = {};
    this.searchResults = null;
    this.searchResultsDataSource.data = [];
  }

}

class SearchResults {
  recordsReturned: number;
  totalRecords: number;
  startIndex: number;
  records: object[];
}
