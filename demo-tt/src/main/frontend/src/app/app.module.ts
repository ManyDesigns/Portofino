import {Component, Input, NgModule, OnInit} from '@angular/core';
import {
  PortofinoModule,
  PortofinoUpstairsModule,
  Page,
  NAVIGATION_COMPONENT,
  DefaultNavigationComponent,
  PortofinoComponent,
  PortofinoService,
  CrudComponent,
  SearchComponent,
  Button,
  SearchResults,
  NOTIFICATION_HANDLERS,
  MatSnackBarNotificationService, PortofinoFormsModule
} from "portofino";
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
import { ProfileComponent } from './profile.component';

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
  template: `<h3>Custom navigation</h3><p><a routerLink="/hello">Start here</a> </p>`
})
export class CustomNavigation {}

@Component({
  selector: 'portofino-welcome',
  templateUrl: 'home.html',
  styleUrls: ['home.scss']
})
@PortofinoComponent({ name: 'welcome' })
export class WelcomeComponent extends Page implements OnInit {
  projects = [];
  ngOnInit(): void {
    this.http.get<SearchResults>(`${this.portofino.apiRoot}projects?maxResults=20`).subscribe(
      res => this.projects = res.records
    );
  }
}

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


@Component({
  selector: 'app-root',
  template: `<portofino-app appTitle="Demo-TT" apiRoot="http://localhost:8080/demo-tt/api/">
    <portofino-templates></portofino-templates>
  </portofino-app>`
})
export class DemoTTAppComponent {}

@NgModule({
  declarations: [
    DemoTTAppComponent, HelloPortofino, CustomNavigation, WelcomeComponent, CustomCrud, CustomSearch,
    ProfileComponent],
  providers: [
    { provide: NAVIGATION_COMPONENT, useFactory: DemoTTAppModule.navigation },
    { provide: NOTIFICATION_HANDLERS, useClass: MatSnackBarNotificationService, multi: true },
  ],
  imports: [
    PortofinoModule.withRoutes([{path: "hello", component: HelloPortofino}]), PortofinoUpstairsModule,
    PortofinoFormsModule,
    BrowserModule, BrowserAnimationsModule, FlexLayoutModule, FormsModule, HttpClientModule, ReactiveFormsModule,
    MatAutocompleteModule, MatButtonModule, MatCardModule, MatCheckboxModule, MatDatepickerModule, MatDialogModule,
    MatDividerModule, MatExpansionModule, MatFormFieldModule, MatIconModule, MatInputModule, MatListModule, MatMenuModule,
    MatPaginatorModule, MatProgressBarModule, MatRadioModule, MatSelectModule, MatSidenavModule, MatSnackBarModule,
    MatSortModule, MatTableModule, MatTreeModule, MatToolbarModule, MatMomentDateModule, ScrollingModule,
    FileInputAccessorModule, NgxdModule, QuillModule.forRoot(),
    TranslateModule.forRoot()],
  entryComponents: [ CustomNavigation, WelcomeComponent, CustomCrud, CustomSearch, ProfileComponent ],
  bootstrap: [DemoTTAppComponent]
})
export class DemoTTAppModule {
  static navigation() {
    return DefaultNavigationComponent
    //return CustomNavigation
  }
}
