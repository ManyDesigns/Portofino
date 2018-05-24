import {
  Component, ContentChild, ElementRef, EventEmitter, Input, OnInit, Output, TemplateRef, ViewChild,
  ViewContainerRef
} from '@angular/core';
import {Configuration, CrudComponent} from "../crud.component";
import {ClassAccessor, isEnabled, isInSummary, isSearchable, Property} from "../../class-accessor";
import {MatTableDataSource, PageEvent, Sort} from "@angular/material";
import {HttpClient, HttpParams} from "@angular/common/http";
import {PortofinoService} from "../../portofino.service";

@Component({
  selector: 'portofino-crud-search',
  templateUrl: './search.component.html',
  styleUrls: ['./search.component.css']
})
export class SearchComponent implements OnInit {

  @Input()
  classAccessor: ClassAccessor;
  @Input()
  configuration: Configuration;
  @Output()
  detail = new EventEmitter<string>();

  searchFields: Property[] = [];
  searchQuery = {};
  results: SearchResults;
  resultsDataSource = new MatTableDataSource();
  resultFields: Property[] = [];
  columnsToDisplay: string[] = [];
  @Input()
  pageSize: number;
  sortInfo: Sort;

  @ContentChild("buttons") buttons: TemplateRef<any>;

  constructor(private http: HttpClient, private portofino: PortofinoService) {}

  ngOnInit() {
    this.classAccessor.properties.forEach(property => {
      if(!isEnabled(property)) {
        return;
      }
      if(isSearchable(property)) {
        this.searchFields.push(property);
      }
      if(isInSummary(property)) {
        this.resultFields.push(property);
        this.columnsToDisplay.push(property.name);
      }
    });
    if(!this.pageSize) {
      this.pageSize = this.configuration.rowsPerPage;
    }
    this.search();
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
    const searchUrl = this.portofino.apiPath + this.configuration.path;
    this.http.get<SearchResults>(searchUrl, {params: params}).subscribe(
      results => {
        results.records = results['Result'];
        this.results = results;
        this.resultsDataSource.data = this.results.records;
      }
    );
  }

  protected composeSearch(params: HttpParams) {
    for (let p in this.searchQuery) {
      if (this.searchQuery[p] === null) {
        continue;
      }
      const property = this.searchFields.find(value => value.name == p);
      if (!property) {
        continue;
      }
      if (this.portofino.isDate(property)) {
        params = params.set(`search_${p}_min`, this.searchQuery[p].valueOf().toString());
        params = params.set(`search_${p}_max`, this.searchQuery[p].valueOf().toString());
      } else if (this.portofino.isNumber(property)) {
        params = params.set(`search_${p}_min`, this.searchQuery[p].toString());
        params = params.set(`search_${p}_max`, this.searchQuery[p].toString());
      } else {
        params = params.set(`search_${p}`, this.searchQuery[p].toString());
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
    this.searchQuery = {};
  }

  openDetail(id: string) {
    this.detail.emit(id);
  }

}

class SearchResults {
  recordsReturned: number;
  totalRecords: number;
  startIndex: number;
  records: object[];
}
