import {Component, Input, NgModule, OnInit} from '@angular/core';
import {
  PortofinoModule, PageConfiguration,
  NAVIGATION_COMPONENT, ROOT_PAGE_CONFIGURATION_LOADER,
  DefaultNavigationComponent, PortofinoComponent, PortofinoUpstairsModule, UpstairsComponent} from "portofino";
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
import {Observable, of} from "rxjs";

registerLocaleData(localeIt);

@Component({
  selector: 'app-root',
  template: `<portofino-app appTitle="Portofino Upstairs" apiRoot="http://localhost:8080/demo-tt/api/" [upstairsLink]="null"></portofino-app>`
})
export class AppComponent {}

@NgModule({
  declarations: [AppComponent],
  providers: [
    { provide: NAVIGATION_COMPONENT, useFactory: AppModule.navigation },
    { provide: ROOT_PAGE_CONFIGURATION_LOADER, useFactory: AppModule.rootPageConfiguration }
  ],
  imports: [
    PortofinoModule.withRoutes([]), PortofinoUpstairsModule,
    BrowserModule, BrowserAnimationsModule, FlexLayoutModule, FormsModule, HttpClientModule, ReactiveFormsModule,
    MatAutocompleteModule, MatButtonModule, MatCardModule, MatCheckboxModule, MatDatepickerModule, MatDialogModule,
    MatDividerModule, MatExpansionModule, MatFormFieldModule, MatIconModule, MatInputModule, MatListModule, MatMenuModule,
    MatPaginatorModule, MatProgressBarModule, MatRadioModule, MatSelectModule, MatSidenavModule, MatSnackBarModule,
    MatSortModule, MatTableModule, MatTreeModule, MatToolbarModule, MatMomentDateModule, ScrollingModule,
    FileInputAccessorModule, NgxdModule, QuillModule,
    TranslateModule.forRoot()],
  entryComponents: [],
  bootstrap: [AppComponent]
})
export class AppModule {
  static navigation() {
    return DefaultNavigationComponent
    //return CustomNavigation
  }

  static rootPageConfiguration(): () => Observable<PageConfiguration> {
    return () => of(UpstairsComponent.defaultConfiguration());
  }
}
