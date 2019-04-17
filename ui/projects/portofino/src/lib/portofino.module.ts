import {BrowserModule} from '@angular/platform-browser';
import {BrowserAnimationsModule} from '@angular/platform-browser/animations';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {ErrorHandler, ModuleWithProviders, NgModule, Type} from '@angular/core';
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
import {LOCALE_STORAGE_SERVICE, PortofinoService, ProgressInterceptor} from './portofino.service';
import {HTTP_INTERCEPTORS, HttpClientModule} from '@angular/common/http';
import {
  AuthenticationInterceptor,
  AuthenticationService,
  LOGIN_COMPONENT,
  TOKEN_STORAGE_SERVICE
} from "./security/authentication.service";
import {LoginComponent} from './security/login/login.component';
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
  MatTreeModule,
  MatCardModule,
  MatExpansionModule,
  MatDividerModule,
  MatListModule,
  MatProgressSpinnerModule,
  MatTabsModule, MatStepperModule, MatGridListModule
} from '@angular/material';
import {MatMomentDateModule} from "@angular/material-moment-adapter";
import {FlexLayoutModule} from "@angular/flex-layout";
import {ExtraOptions, RouterModule, Routes} from '@angular/router';
import {SearchComponent} from './crud/search/search.component';
import {BreadcrumbsComponent} from "./breadcrumbs/breadcrumbs.component";
import {DetailComponent} from './crud/detail/detail.component';
import {CreateComponent} from './crud/detail/create.component';
import {MainPageDirective, NavigationDirective} from './content.directive';
import {
  DefaultNavigationComponent,
  DefaultPageLayout,
  NAVIGATION_COMPONENT,
  PageHeader, PageService
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
  MatSnackBarNotificationService, NotificationErrorHandler, NotificationInterceptor,
  NotificationService
} from "./notifications/notification.service";
import {ScrollingModule} from "@angular/cdk/scrolling";
import { NgxdModule } from '@ngxd/core';
import {FieldFactory, FieldFactoryComponent} from "./fields/field.factory";
import {DateTimeValueAccessor, DateTimeFieldComponent} from "./fields/date-time-field.component";
import {BooleanFieldComponent} from "./fields/boolean-field.component";
import {NumberFieldComponent} from "./fields/number-field.component";
import {TextFieldComponent} from "./fields/text-field.component";
import {SelectFieldComponent} from "./fields/select-field.component";
import {PageFactoryComponent} from "./page.factory";
import {LanguageSelectorComponent} from "./i18n/language.selector.component";
import {LanguageInterceptor} from "./i18n/language.interceptor";
import {MatDatetimepickerModule} from "@mat-datetimepicker/core";
import {MatMomentDatetimeModule} from "@mat-datetimepicker/moment";
import {LocalStorageService} from "ngx-store";
import {CreatePageComponent, DeletePageComponent, MovePageComponent, PageCrudService} from "./administration/page-crud.service";
import {PermissionsComponent, SettingsComponent, UpstairsComponent} from "./administration/upstairs.component";
import {ConnectionsComponent} from "./administration/connections.component";
import {WizardComponent} from "./administration/wizard.component";
import {TablesComponent} from "./administration/tables.component";
import {ActionsComponent, CreateActionComponent, GenericPage} from "./administration/actions.component";

@NgModule({
  declarations: [
    FieldFactoryComponent, BlobFieldComponent, BooleanFieldComponent, DateTimeValueAccessor, DateTimeFieldComponent,
    NumberFieldComponent, SelectFieldComponent, TextFieldComponent,
    FormComponent, DynamicFormComponentDirective],
  imports: [
    BrowserModule, BrowserAnimationsModule, ReactiveFormsModule, FormsModule, FlexLayoutModule,
    MatAutocompleteModule, MatCheckboxModule, MatDatepickerModule, MatFormFieldModule, MatIconModule, MatInputModule,
    MatMomentDateModule, MatMomentDatetimeModule, MatRadioModule, MatSelectModule, MatDatetimepickerModule,
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
    DefaultPageLayout, ButtonComponent, ButtonsComponent,
    ContentComponent, PageFactoryComponent, PageHeader, MainPageDirective,
    LanguageSelectorComponent,
    NavigationDirective, DefaultNavigationComponent,
    ToolbarDirective, DefaultToolbarComponent, BreadcrumbsComponent,
    CreatePageComponent, DeletePageComponent, MovePageComponent
  ],
  imports: [
    BrowserModule, BrowserAnimationsModule, ReactiveFormsModule, FormsModule, FlexLayoutModule,
    HttpClientModule, PortofinoFormsModule,
    MatButtonModule, MatCardModule, MatCheckboxModule, MatDatepickerModule, MatDialogModule,
    MatDividerModule, MatExpansionModule, MatFormFieldModule, MatIconModule, MatInputModule, MatMenuModule,
    MatMomentDateModule, MatMomentDatetimeModule, MatProgressBarModule, MatRadioModule,
    MatSelectModule, MatSidenavModule, MatSnackBarModule, MatProgressSpinnerModule, MatStepperModule,
    MatTabsModule, MatTableModule, MatTreeModule, MatListModule, MatToolbarModule, MatDatetimepickerModule,
    NgxdModule, RouterModule.forChild([]), ScrollingModule, TranslateModule.forChild()
  ],
  providers: [PortofinoService, AuthenticationService, PageService, PageCrudService],
  entryComponents: [
    DefaultNavigationComponent, DefaultToolbarComponent,
    CreatePageComponent, DeletePageComponent, MovePageComponent],
  exports: [
    DefaultPageLayout, ButtonComponent, ButtonsComponent, BreadcrumbsComponent,
    ContentComponent, PageFactoryComponent, PageHeader,
    DefaultNavigationComponent, NavigationDirective, DefaultToolbarComponent, ToolbarDirective]
})
export class PortofinoPagesModule {}

@NgModule({
  declarations: [
    CrudComponent, SearchFieldComponent, SearchComponent, DetailComponent, CreateComponent, BulkEditComponent,
    SearchComponentHolder, DetailComponentHolder, CreateComponentHolder, BulkEditComponentHolder,
    ManyToManyComponent
  ],
  imports: [
    BrowserModule, BrowserAnimationsModule, ReactiveFormsModule, FormsModule, FlexLayoutModule,
    HttpClientModule, PortofinoFormsModule, PortofinoPagesModule,
    MatAutocompleteModule, MatCardModule, MatCheckboxModule, MatDatepickerModule, MatDialogModule,
    MatDividerModule, MatExpansionModule, MatFormFieldModule, MatIconModule, MatInputModule, MatMenuModule,
    MatMomentDateModule, MatMomentDatetimeModule, MatPaginatorModule, MatProgressBarModule, MatRadioModule,
    MatSelectModule, MatSidenavModule, MatSnackBarModule, MatSortModule, MatProgressSpinnerModule, MatStepperModule,
    MatTabsModule, MatTableModule, MatTreeModule, MatListModule, MatToolbarModule, MatDatetimepickerModule,
    NgxdModule, RouterModule.forChild([]), ScrollingModule, TranslateModule
  ],
  providers: [],
  entryComponents: [
    CrudComponent, SearchComponent, DetailComponent, CreateComponent, BulkEditComponent,
    ManyToManyComponent],
  exports: [
    CrudComponent, SearchFieldComponent, SearchComponent, DetailComponent, CreateComponent, BulkEditComponent,
    SearchComponentHolder, DetailComponentHolder, CreateComponentHolder, BulkEditComponentHolder,
    ManyToManyComponent]
})
export class PortofinoCrudModule {}

@NgModule({
  declarations: [
    UpstairsComponent,
    ActionsComponent, GenericPage, CreateActionComponent,
    ConnectionsComponent, PermissionsComponent, SettingsComponent, TablesComponent, WizardComponent],
  imports: [
    BrowserModule, BrowserAnimationsModule, ReactiveFormsModule, FormsModule, FlexLayoutModule,
    HttpClientModule, PortofinoFormsModule, PortofinoPagesModule,
    MatButtonModule, MatCardModule, MatCheckboxModule, MatDatepickerModule, MatDialogModule,
    MatDividerModule, MatExpansionModule, MatFormFieldModule, MatGridListModule, MatIconModule, MatInputModule,
    MatMenuModule, MatMomentDateModule, MatMomentDatetimeModule, MatProgressBarModule, MatRadioModule,
    MatSelectModule, MatSidenavModule, MatSnackBarModule, MatProgressSpinnerModule, MatStepperModule,
    MatTabsModule, MatTableModule, MatTreeModule, MatListModule, MatToolbarModule, MatDatetimepickerModule,
    NgxdModule, RouterModule.forChild([]), TranslateModule.forChild()
  ],
  providers: [],
  entryComponents: [
    UpstairsComponent,
    ActionsComponent, GenericPage, CreateActionComponent,
    ConnectionsComponent, PermissionsComponent, SettingsComponent, TablesComponent, WizardComponent],
  exports: [UpstairsComponent]
})
export class PortofinoUpstairsModule {}

@NgModule({
  declarations: [PortofinoAppComponent, LoginComponent],
  imports: [
    BrowserModule, BrowserAnimationsModule, ReactiveFormsModule, FormsModule, FlexLayoutModule,
    HttpClientModule, PortofinoFormsModule, PortofinoPagesModule, PortofinoCrudModule,
    MatAutocompleteModule, MatButtonModule, MatCardModule, MatCheckboxModule, MatDatepickerModule, MatDialogModule,
    MatDividerModule, MatExpansionModule, MatFormFieldModule, MatIconModule, MatInputModule, MatMenuModule,
    MatMomentDateModule, MatMomentDatetimeModule, MatPaginatorModule, MatProgressBarModule, MatRadioModule,
    MatSelectModule, MatSidenavModule, MatSnackBarModule, MatSortModule, MatProgressSpinnerModule, MatStepperModule,
    MatTabsModule, MatTableModule, MatTreeModule, MatListModule, MatToolbarModule, MatDatetimepickerModule,
    NgxdModule, RouterModule.forChild([]), ScrollingModule, TranslateModule
  ],
  providers: [
    //These are factories to avoid circular dependencies
    { provide: LOGIN_COMPONENT, useFactory: PortofinoModule.loginComponent },
    { provide: NAVIGATION_COMPONENT, useFactory: PortofinoModule.navigationComponent },
    { provide: TOOLBAR_COMPONENT, useFactory: PortofinoModule.toolbarComponent },
    { provide: HTTP_INTERCEPTORS, useClass: AuthenticationInterceptor, multi: true },
    { provide: HTTP_INTERCEPTORS, useClass: LanguageInterceptor, multi: true },
    { provide: HTTP_INTERCEPTORS, useClass: NotificationInterceptor, multi: true },
    { provide: HTTP_INTERCEPTORS, useClass: ProgressInterceptor, multi: true },
    { provide: TOKEN_STORAGE_SERVICE, useClass: LocalStorageService },
    { provide: LOCALE_STORAGE_SERVICE, useClass: LocalStorageService },
    { provide: NotificationService, useClass: MatSnackBarNotificationService },
    { provide: ErrorHandler, useClass: NotificationErrorHandler }],
  entryComponents: [LoginComponent],
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

  public static withRoutes(routes: Routes, config: ExtraOptions = {}): (ModuleWithProviders|Type<PortofinoModule>)[] {
    return [RouterModule.forRoot(
      [...routes,
              { path: "**", component: ContentComponent}],
      { onSameUrlNavigation: "reload", ...config }),
      PortofinoModule];
  }
}
