import {BrowserModule} from '@angular/platform-browser';
import {BrowserAnimationsModule} from '@angular/platform-browser/animations';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {ModuleWithProviders, NgModule, Type} from '@angular/core';
import {
  DefaultToolbarComponent,
  PortofinoAppComponent,
  TOOLBAR_COMPONENT,
  ToolbarDirective
} from './portofino-app.component';
import {
  BulkEditComponentHolder,
  CreateComponentHolder,
  CrudComponent,
  DetailComponentHolder,
  SearchComponentHolder
} from './crud/crud.component';
import {PortofinoService} from './portofino.service';
import {HTTP_INTERCEPTORS, HttpClientModule} from '@angular/common/http';
import {AuthenticationInterceptor, AuthenticationService, LOGIN_COMPONENT} from "./security/authentication.service";
import {LoginComponent} from './security/login/login.component';
import {LocalTokenStorageService, TokenStorageService} from "./security/token-storage.service";
import {SearchFieldComponent} from './crud/search/search-field.component';
import {
  MatAutocompleteModule,
  MatButtonModule,
  MatCheckboxModule,
  MatDatepickerModule,
  MatDialogModule,
  MatFormFieldModule,
  MatIconModule,
  MatInputModule,
  MatMenuModule,
  MatPaginatorModule,
  MatProgressBarModule,
  MatRadioModule,
  MatSelectModule,
  MatSidenavModule,
  MatSnackBarModule,
  MatSortModule,
  MatTableModule,
  MatToolbarModule,
  MatTreeModule, MatCardModule, MatExpansionModule, MatDividerModule, MatListModule, MatProgressSpinnerModule
} from '@angular/material';
import {MatMomentDateModule} from "@angular/material-moment-adapter";
import {FlexLayoutModule} from "@angular/flex-layout";
import {RouterModule, Routes} from '@angular/router';
import {SearchComponent} from './crud/search/search.component';
import {BreadcrumbsComponent} from "./breadcrumbs/breadcrumbs.component";
import {DetailComponent} from './crud/detail/detail.component';
import {CreateComponent} from './crud/detail/create.component';
import {MainPageDirective, NavigationDirective} from './content.directive';
import {
  DefaultNavigationComponent,
  DefaultPageLayout,
  NAVIGATION_COMPONENT,
  PageHeader, PageService, SourceSelector, SourceSelectorTree
} from './page';
import {BulkEditComponent} from "./crud/bulk/bulk-edit.component";
import {BlobFieldComponent} from "./fields/blob-field.component";
import {FileInputAccessorModule} from "file-input-accessor";
import {ManyToManyComponent} from './many-to-many/many-to-many.component';
import {ButtonComponent, ButtonsComponent} from "./button.component";
import {QuillModule} from "ngx-quill";
import {DynamicFormComponentDirective, FormComponent} from "./form";
import {TranslateModule} from "@ngx-translate/core";
import {ContentComponent} from "./content.component";
import {
  MatSnackBarNotificationService,
  NotificationService} from "./notifications/notification.service";
import {ScrollingModule} from "@angular/cdk/scrolling";
import { NgxdModule } from '@ngxd/core';
import {FieldFactory, FieldFactoryComponent} from "./fields/field.factory";
import {DateTimeValueAccessor, DateTimeFieldComponent} from "./fields/date-time-field.component";
import {BooleanFieldComponent} from "./fields/boolean-field.component";
import {NumberFieldComponent} from "./fields/number-field.component";
import {TextFieldComponent} from "./fields/text-field.component";
import {SelectFieldComponent} from "./fields/select-field.component";
import {PageFactoryComponent} from "./page.factory";

@NgModule({
  declarations: [
    FieldFactoryComponent, BlobFieldComponent, BooleanFieldComponent, DateTimeValueAccessor, DateTimeFieldComponent,
    NumberFieldComponent, SelectFieldComponent, TextFieldComponent,
    FormComponent, DynamicFormComponentDirective],
  imports: [
    BrowserModule, BrowserAnimationsModule, ReactiveFormsModule, FormsModule, FlexLayoutModule,
    MatAutocompleteModule, MatCheckboxModule, MatDatepickerModule, MatFormFieldModule, MatIconModule, MatInputModule,
    MatMomentDateModule, MatRadioModule, MatSelectModule,
    FileInputAccessorModule, NgxdModule, QuillModule, TranslateModule.forChild()],
  providers: [ FieldFactory ],
  entryComponents: [
    BlobFieldComponent, BooleanFieldComponent, DateTimeFieldComponent,
    NumberFieldComponent, SelectFieldComponent, TextFieldComponent],
  exports: [
    FieldFactoryComponent, BlobFieldComponent, BooleanFieldComponent, DateTimeValueAccessor, DateTimeFieldComponent,
    NumberFieldComponent, SelectFieldComponent, TextFieldComponent,
    FormComponent]
})
export class PortofinoFormsModule {}

@NgModule({
  declarations: [
    PortofinoAppComponent, DefaultPageLayout, ButtonComponent, ButtonsComponent, LoginComponent,
    ContentComponent, PageFactoryComponent, PageHeader, MainPageDirective,
    NavigationDirective, DefaultNavigationComponent, ToolbarDirective, DefaultToolbarComponent,
    SourceSelector, SourceSelectorTree,
    CrudComponent, SearchFieldComponent, SearchComponent, DetailComponent, CreateComponent, BulkEditComponent,
    SearchComponentHolder, DetailComponentHolder, CreateComponentHolder, BulkEditComponentHolder,
    ManyToManyComponent,BreadcrumbsComponent
  ],
  imports: [
    BrowserModule, BrowserAnimationsModule, ReactiveFormsModule, FormsModule, FlexLayoutModule,
    HttpClientModule, PortofinoFormsModule,
    MatAutocompleteModule, MatButtonModule, MatCardModule, MatCheckboxModule, MatDatepickerModule, MatDialogModule,
    MatDividerModule, MatExpansionModule, MatFormFieldModule, MatIconModule, MatInputModule, MatMenuModule,
    MatMomentDateModule, MatPaginatorModule, MatProgressBarModule, MatRadioModule, MatSelectModule,
    MatSidenavModule, MatSnackBarModule, MatSortModule, MatProgressSpinnerModule, MatTableModule, MatTreeModule,
    MatListModule, MatToolbarModule,
    NgxdModule, RouterModule.forChild([]), ScrollingModule, TranslateModule
  ],
  providers: [
    PortofinoService, AuthenticationService, PageService,
    //These are factories to avoid circular dependencies
    { provide: LOGIN_COMPONENT, useFactory: PortofinoModule.loginComponent },
    { provide: NAVIGATION_COMPONENT, useFactory: PortofinoModule.navigationComponent },
    { provide: TOOLBAR_COMPONENT, useFactory: PortofinoModule.toolbarComponent },
    { provide: HTTP_INTERCEPTORS, useClass: AuthenticationInterceptor, multi: true },
    { provide: TokenStorageService, useClass: LocalTokenStorageService },
    { provide: NotificationService, useClass: MatSnackBarNotificationService }],
  entryComponents: [
    LoginComponent, DefaultNavigationComponent, DefaultToolbarComponent, SourceSelector, SourceSelectorTree,
    CrudComponent, SearchComponent, DetailComponent, CreateComponent, BulkEditComponent, ManyToManyComponent],
  exports: [
    PortofinoAppComponent, DefaultPageLayout, ButtonComponent, ButtonsComponent, LoginComponent,
    ContentComponent, PageFactoryComponent, PageHeader, DefaultNavigationComponent, DefaultToolbarComponent,
    CrudComponent, SearchFieldComponent, SearchComponent, DetailComponent, CreateComponent, BulkEditComponent,
    SearchComponentHolder, DetailComponentHolder, CreateComponentHolder, BulkEditComponentHolder,
    ManyToManyComponent]
})
export class PortofinoModule {
  static loginComponent() {
    return LoginComponent;
  }

  static navigationComponent() {
    return DefaultNavigationComponent;
  }

  static toolbarComponent() {
    return DefaultToolbarComponent;
  }

  public static withRoutes(routes: Routes): (ModuleWithProviders|Type<PortofinoModule>)[] {
    return [RouterModule.forRoot(
      [...routes, { path: "**", component: ContentComponent}],
      { onSameUrlNavigation: "reload", enableTracing: false }),
      PortofinoModule];
  }
}
