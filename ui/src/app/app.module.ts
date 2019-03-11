import {Component, Input, NgModule, OnInit} from '@angular/core';
import {
  Button, PortofinoModule, PortofinoService, CrudComponent, SearchComponent,
  NAVIGATION_COMPONENT, DefaultNavigationComponent, PortofinoComponent} from "portofino";
import {
  MatAutocompleteModule,
  MatButtonModule,
  MatCardModule,
  MatCheckboxModule,
  MatDatepickerModule,
  MatDialogModule,
  MatDividerModule,
  MatExpansionModule,
  MatFormFieldModule,
  MatIconModule,
  MatInputModule, MatListModule,
  MatMenuModule,
  MatPaginatorModule, MatProgressBarModule,
  MatRadioModule,
  MatSelectModule,
  MatSidenavModule,
  MatSnackBarModule,
  MatSortModule,
  MatTableModule,
  MatToolbarModule, MatTreeModule
} from "@angular/material";
import {BrowserModule} from "@angular/platform-browser";
import {BrowserAnimationsModule} from "@angular/platform-browser/animations";
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {QuillModule} from "ngx-quill";
import {HttpClient, HttpClientModule} from "@angular/common/http";
import {FlexLayoutModule} from "@angular/flex-layout";
import {MatMomentDateModule} from "@angular/material-moment-adapter";
import {FileInputAccessorModule} from "file-input-accessor";
import {TranslateModule} from "@ngx-translate/core";
import {ScrollingModule} from "@angular/cdk/scrolling";
import {NgxdModule} from "@ngxd/core";
import {registerLocaleData} from "@angular/common";
import localeIt from "@angular/common/locales/it";

registerLocaleData(localeIt);

@Component({
  selector: 'portofino-hello',
  template: `<p>Welcome to Portofino 5!</p>`
})
export class HelloPortofino implements OnInit {

  constructor(protected http: HttpClient, protected portofino: PortofinoService) {}

  ngOnInit(): void {
    this.http.get(this.portofino.apiRoot).subscribe(x => console.log("API Call", x));
  }

}

@Component({
  selector: 'custom-navigation',
  template: `<h3>Custom navigation</h3><p><a routerLink="/start">Start here</a> </p>`
})
export class CustomNavigation {}

@Component({
  selector: 'app-root',
  template: `<portofino-app appTitle="Demo-TT" apiRoot="http://localhost:8080/demo-tt/api/"></portofino-app>`
})
export class AppComponent {}

@PortofinoComponent({ name: 'customcrud' })
export class CustomCrud extends CrudComponent {

  initialize(): void {
    console.log("Custom crud");
    super.initialize();
    this.configuration.title = 'Custom CRUD';
    this.searchComponent = CustomSearch;
    this.searchComponentContext = { customInput: "works!" };
  }

  @Button({
    list: 'search-results', text: 'Custom button', icon: 'save', color: "warn", enabledIf: CustomCrud.buttonEnabled
  })
  hello() {
    console.log("Custom button", this.configuration);
  }

  static buttonEnabled() {
    return true;
  }
}

export class CustomSearch extends SearchComponent {
  @Input()
  customInput;
  ngOnInit(): void {
    console.log("Custom search with input", this.customInput);
    super.ngOnInit();
  }
}

@NgModule({
  declarations: [AppComponent, HelloPortofino, CustomNavigation, CustomCrud, CustomSearch],
  providers: [
    { provide: NAVIGATION_COMPONENT, useFactory: AppModule.navigation },
  ],
  imports: [
    PortofinoModule.withRoutes([{ path: "start", component: HelloPortofino }], { enableTracing: true }),
    BrowserModule, BrowserAnimationsModule, FlexLayoutModule, FormsModule, HttpClientModule, ReactiveFormsModule,
    MatAutocompleteModule, MatButtonModule, MatCardModule, MatCheckboxModule, MatDatepickerModule, MatDialogModule,
    MatDividerModule, MatExpansionModule, MatFormFieldModule, MatIconModule, MatInputModule, MatListModule, MatMenuModule,
    MatPaginatorModule, MatProgressBarModule, MatRadioModule, MatSelectModule, MatSidenavModule, MatSnackBarModule,
    MatSortModule, MatTableModule, MatTreeModule, MatToolbarModule, MatMomentDateModule, ScrollingModule,
    FileInputAccessorModule, NgxdModule, QuillModule,
    TranslateModule.forRoot()],
  entryComponents: [ CustomNavigation, CustomCrud, CustomSearch ],
  bootstrap: [AppComponent]
})
export class AppModule {
  static navigation() {
    return DefaultNavigationComponent
    //return CustomNavigation
  }
}
