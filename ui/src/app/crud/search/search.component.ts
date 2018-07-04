import {Component, ContentChild, Input, OnInit, TemplateRef} from '@angular/core';
import {Configuration, SelectionOption, SelectionProvider} from "../crud.component";
import {ClassAccessor, isEnabled, isInSummary, isSearchable, Property} from "../../class-accessor";
import {MatTableDataSource, PageEvent, Sort} from "@angular/material";
import {HttpClient, HttpParams} from "@angular/common/http";
import {PortofinoService} from "../../portofino.service";
import {FormArray, FormControl, FormGroup} from "@angular/forms";
import {debounceTime} from "rxjs/operators";
import {SelectionModel} from "@angular/cdk/collections";

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
  @Input()
  sourceUrl: string;

  properties: Property[] = [];
  form: FormGroup;
  results: SearchResults;
  resultsDataSource = new MatTableDataSource();
  resultFields: Property[] = [];
  columnsToDisplay: string[] = [];
  @Input()
  pageSize: number;
  sortInfo: Sort;
  page: number;

  @Input()
  selectionEnabled: boolean;
  selection = new SelectionModel<any>(true, []);
  readonly selectColumnName = "__select";

  @ContentChild("buttons")
  buttons: TemplateRef<any>;

  constructor(private http: HttpClient, private portofino: PortofinoService) {}

  ngOnInit() {
    const formControls = {};
    if(this.selectionEnabled) {
      this.columnsToDisplay.push(this.selectColumnName);
    }
    this.classAccessor.properties.forEach(property => {
      if(!isEnabled(property)) {
        return;
      }
      property = {...property};
      if(isSearchable(property)) {
        this.properties.push(property);
        formControls[property.name] = new FormControl();
      }
      if(isInSummary(property)) {
        this.resultFields.push(property);
        this.columnsToDisplay.push(property.name);
      }
    });

    this.form = new FormGroup(formControls);

    this.selectionProviders.forEach(sp => {
      sp.fieldNames.forEach((name, index) => {
        const property = this.properties.find(p => p.name == name);
        if(!property) {
          return;
        }
        const spUrl = `${this.sourceUrl}/:selectionProvider/${sp.name}/${index}`;
        property.selectionProvider = {
          name: sp.name,
          index: index,
          displayMode: sp.searchDisplayMode,
          url: spUrl,
          nextProperty: null,
          updateDependentOptions: () => {
            const nextProperty = property.selectionProvider.nextProperty;
            if(nextProperty) {
              this.loadSelectionOptions(this.properties.find(p => p.name == nextProperty));
            }
          },
          options: []
        };
        if(property.selectionProvider.displayMode == 'AUTOCOMPLETE') {
          const autocomplete = this.form.get(property.name);
          autocomplete.valueChanges.pipe(debounceTime(500)).subscribe(value => {
            if(autocomplete.dirty && value != null && value.hasOwnProperty("length")) {
              this.loadSelectionOptions(property, value);
            }
          });
        } else if(index == 0) {
          this.loadSelectionOptions(property);
        }
        if(index < sp.fieldNames.length - 1) {
          property.selectionProvider.nextProperty = sp.fieldNames[index + 1];
        }
      });
    });

    if(!this.pageSize) {
      this.pageSize = this.configuration.rowsPerPage;
    }

    this.search();
  }

  protected loadSelectionOptions(property: Property, autocomplete: string = null) {
    const url = property.selectionProvider.url;
    let params = new HttpParams();
    if(property.selectionProvider.displayMode == 'AUTOCOMPLETE') {
      if(autocomplete) {
        params = params.set(`labelSearch`, autocomplete);
      } else {
        this.setSelectOptions(property, []);
        return;
      }
    }
    this.http.get<SelectionOption[]>(url, { params: params }).subscribe(
      options => {
        this.setSelectOptions(property, options);
        if(property.selectionProvider.displayMode == 'CHECKBOX') {
          const controls = [];
          for(let i = 0; i < options.length; i++) {
            controls.push(new FormControl());
          }
          this.form.setControl(property.name, new FormArray(controls));
        }
      });
  }

  private setSelectOptions(property: Property, options) {
    property.selectionProvider.options = options;
    this.clearDependentSelectionValues(property);
    const selected = options.find(o => o.s);
    if (selected) {
      this.form.get(property.name).setValue(selected);
    }
  }

  protected clearDependentSelectionValues(property: Property) {
    const nextProperty = property.selectionProvider.nextProperty;
    if (nextProperty) {
      this.clearSelectionValues(this.properties.find(p => p.name == nextProperty));
    }
  }

  protected clearSelectionValues(property: Property) {
    this.form.get(property.name).setValue(null);
    property.selectionProvider.options = [];
    const nextProperty = property.selectionProvider.nextProperty;
    if(nextProperty) {
      this.clearSelectionValues(this.properties.find(p => p.name == nextProperty));
    }
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
    this.http.get<SearchResults>(this.sourceUrl, {params: params}).subscribe(
      results => {
        results.records = results['Result'];
        this.results = results;
        this.resultsDataSource.data = this.results.records;
        this.page = page;
        this.selection.clear();
      }
    );
  }

  protected composeSearch(params: HttpParams) {
    this.properties.forEach(property => {
      if(property.selectionProvider) {
        params = this.addSelectionProviderSearchParameter(property, params);
      } else {
        params = this.addSimpleSearchParameter(property, params);
      }
    });
    return params;
  }

  protected addSimpleSearchParameter(property, params: HttpParams) {
    const name = property.name;
    const value = this.form.get(name).value;
    if(value == null) {
      return params;
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
    return params;
  }

  protected addSelectionProviderSearchParameter(property, params: HttpParams) {
    const name = property.name;
    let value = this.form.get(name).value;
    if (property.selectionProvider.displayMode == 'CHECKBOX') {
      property.selectionProvider.options.forEach((option, index) => {
        if(value[index]) {
          params = params.append(`search_${name}`, option.v);
        }
      });

    } else {
      if (value != null) {
        value = value.v;
        if (value instanceof Array) {
          value.forEach(v => {
            params = params.append(`search_${name}`, v.toString());
          });
        } else {
          params = params.set(`search_${name}`, value.toString());
        }
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
    this.form.reset();
    this.properties.forEach(property => {
      const sp = property.selectionProvider;
      if(sp) {
        if(sp.displayMode == 'AUTOCOMPLETE') {
          sp.options = [];
        }
        this.clearDependentSelectionValues(property);
      }
    });
  }

  refreshSearch() {
    this.loadSearchResultsPage(this.page);
  }

  //Selection
  isAllSelected() {
    const numSelected = this.selection.selected.length;
    const numRows = this.resultsDataSource.data.length;
    return numSelected == numRows;
  }

  /** Selects all rows if they are not all selected; otherwise clear selection. */
  toggleSelectAll() {
    this.isAllSelected() ?
      this.selection.clear() :
      this.resultsDataSource.data.forEach(row => this.selection.select(row));
  }

  getSelectedIds(): string[] {
    return this.selection.selected.map(row => row.__rowKey);
  }

}

class SearchResults {
  recordsReturned: number;
  totalRecords: number;
  startIndex: number;
  records: object[];
}
