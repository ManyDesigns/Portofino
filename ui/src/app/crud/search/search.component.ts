import {Component, ContentChild, Input, OnInit, TemplateRef} from '@angular/core';
import {Configuration, SelectionProvider} from "../crud.component";
import {ClassAccessor, isEnabled, isInSummary, isSearchable, Property} from "../../class-accessor";
import {MatTableDataSource, PageEvent, Sort} from "@angular/material";
import {HttpClient, HttpParams} from "@angular/common/http";
import {PortofinoService} from "../../portofino.service";
import {FormControl, FormGroup} from "@angular/forms";

@Component({
  selector: 'portofino-crud-search',
  templateUrl: './search.component.html',
  styleUrls: ['./search.component.css']
})
export class SearchComponent implements OnInit {

  @Input()
  classAccessor: ClassAccessor;
  @Input()
  selectionProviders: SelectionProvider[];
  @Input()
  configuration: Configuration;

  searchFields: Property[] = [];
  form: FormGroup;
  results: SearchResults;
  resultsDataSource = new MatTableDataSource();
  resultFields: Property[] = [];
  columnsToDisplay: string[] = [];
  @Input()
  pageSize: number;
  sortInfo: Sort;

  @ContentChild("buttons")
  buttons: TemplateRef<any>;

  constructor(private http: HttpClient, private portofino: PortofinoService) {}

  ngOnInit() {
    const formControls = {};
    this.classAccessor.properties.forEach(property => {
      if(!isEnabled(property)) {
        return;
      }
      property = {...property};
      if(isSearchable(property)) {
        this.searchFields.push(property);
        formControls[property.name] = new FormControl();
      }
      if(isInSummary(property)) {
        this.resultFields.push(property);
        this.columnsToDisplay.push(property.name);
      }
    });
    if(!this.pageSize) {
      this.pageSize = this.configuration.rowsPerPage;
    }
    this.form = new FormGroup(formControls);
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
    const searchUrl = this.portofino.apiPath + this.configuration.source;
    this.http.get<SearchResults>(searchUrl, {params: params}).subscribe(
      results => {
        results.records = results['Result'];
        this.results = results;
        this.resultsDataSource.data = this.results.records;
      }
    );
  }

  protected composeSearch(params: HttpParams) {
    this.searchFields.forEach(property => {
      const name = property.name;
      const value = this.form.get(name).value;
      if(value == null) {
        return;
      }
      if (this.portofino.isDate(property)) {
        params = params.set(`search_${name}_min`, value.valueOf().toString());
        params = params.set(`search_${name}_max`, value.valueOf().toString());
      } else if (this.portofino.isNumber(property)) {
        params = params.set(`search_${name}_min`, value.toString());
        params = params.set(`search_${name}_max`, value.toString());
      } else {
        params = params.set(`search_${name}`, value.toString());
      }
    });
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
    this.form.reset();
  }

}

class SearchResults {
  recordsReturned: number;
  totalRecords: number;
  startIndex: number;
  records: object[];
}
