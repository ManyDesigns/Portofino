import {BrowserModule} from '@angular/platform-browser';
import {BrowserAnimationsModule} from '@angular/platform-browser/animations';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {ErrorHandler, ModuleWithProviders, NgModule, Type} from '@angular/core';
import {
  DefaultFooterComponent,
  DefaultToolbarComponent, FOOTER_COMPONENT, FooterDirective,
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
import {LOCALE_STORAGE_SERVICE, LOCALES, PortofinoService, ProgressInterceptor} from './portofino.service';
import {HTTP_INTERCEPTORS, HttpClientModule} from '@angular/common/http';
import {
  AuthenticationInterceptor,
  AuthenticationService, CHANGE_PASSWORD_COMPONENT,
  LOGIN_COMPONENT,
  TOKEN_STORAGE_SERVICE
} from "./security/authentication.service";
import {LoginComponent} from './security/login/login.component';
import {SearchFieldComponent} from './crud/search/search-field.component';
import { MatAutocompleteModule } from '@angular/material/autocomplete';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatDialogModule } from '@angular/material/dialog';
import { MatDividerModule } from '@angular/material/divider';
import { MatExpansionModule } from '@angular/material/expansion';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatGridListModule } from '@angular/material/grid-list';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatListModule } from '@angular/material/list';
import { MatMenuModule } from '@angular/material/menu';
import { MatPaginatorModule } from '@angular/material/paginator';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatRadioModule } from '@angular/material/radio';
import { MatSelectModule } from '@angular/material/select';
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatSnackBarModule } from '@angular/material/snack-bar';
import { MatSortModule } from '@angular/material/sort';
import { MatStepperModule } from '@angular/material/stepper';
import { MatTableModule } from '@angular/material/table';
import { MatTabsModule } from '@angular/material/tabs';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatTreeModule } from '@angular/material/tree';
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
  PageLayout,
  NAVIGATION_COMPONENT,
  PageHeader, PageService, TemplatesComponent
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
import {
  MailSettingsComponent,
  PermissionsComponent,
  SettingsComponent,
  UpstairsComponent
} from "./administration/upstairs.component";
import {ConnectionsComponent} from "./administration/connections.component";
import {WizardComponent} from "./administration/wizard.component";
import {TablesComponent} from "./administration/tables.component";
import {ActionsComponent, CreateActionComponent, GenericPage} from "./administration/actions.component";
import {TRANSLATIONS_EN} from "./i18n/en";
import {TRANSLATIONS_IT} from "./i18n/it";
import {ChangePasswordComponent} from "./security/login/change-password.component";

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
    FormComponent,
    MatAutocompleteModule, MatCheckboxModule, MatDatepickerModule, MatFormFieldModule, MatIconModule, MatInputModule,
    MatMomentDateModule, MatMomentDatetimeModule, MatRadioModule, MatSelectModule, MatDatetimepickerModule]
})
export class PortofinoFormsModule {}

@NgModule({
  declarations: [
    PageLayout, ButtonComponent, ButtonsComponent,
    ContentComponent, PageFactoryComponent, PageHeader, MainPageDirective, TemplatesComponent,
    LanguageSelectorComponent,
    NavigationDirective, DefaultNavigationComponent,
    ToolbarDirective, DefaultToolbarComponent, FooterDirective, DefaultFooterComponent, BreadcrumbsComponent,
    CreatePageComponent, DeletePageComponent, MovePageComponent
  ],
  imports: [
    BrowserModule, BrowserAnimationsModule, ReactiveFormsModule, FormsModule, FlexLayoutModule,
    HttpClientModule, PortofinoFormsModule,
    MatButtonModule, MatCardModule, MatDialogModule, MatDividerModule, MatExpansionModule, MatMenuModule,
    MatProgressBarModule, MatSidenavModule, MatSnackBarModule, MatProgressSpinnerModule, MatStepperModule,
    MatTabsModule, MatTableModule, MatTreeModule, MatListModule, MatToolbarModule,
    NgxdModule, RouterModule.forChild([]), ScrollingModule, TranslateModule.forChild()
  ],
  providers: [PortofinoService, AuthenticationService, PageService, PageCrudService],
  entryComponents: [
    DefaultNavigationComponent, DefaultToolbarComponent, DefaultFooterComponent,
    CreatePageComponent, DeletePageComponent, MovePageComponent],
  exports: [
    PageLayout, ButtonComponent, ButtonsComponent, BreadcrumbsComponent,
    ContentComponent, PageFactoryComponent, PageHeader, TemplatesComponent,
    DefaultNavigationComponent, NavigationDirective, DefaultToolbarComponent, ToolbarDirective,
    DefaultFooterComponent, FooterDirective]
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
    MatAutocompleteModule, MatButtonModule, MatCardModule, MatCheckboxModule, MatDatepickerModule, MatDialogModule,
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
    ConnectionsComponent, MailSettingsComponent, PermissionsComponent, SettingsComponent, TablesComponent, WizardComponent],
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
    ConnectionsComponent, MailSettingsComponent, PermissionsComponent, SettingsComponent, TablesComponent, WizardComponent],
  exports: [UpstairsComponent]
})
export class PortofinoUpstairsModule {}

@NgModule({
  declarations: [PortofinoAppComponent, LoginComponent, ChangePasswordComponent],
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
    { provide: CHANGE_PASSWORD_COMPONENT, useFactory: PortofinoModule.changePasswordComponent },
    { provide: NAVIGATION_COMPONENT, useFactory: PortofinoModule.navigationComponent },
    { provide: TOOLBAR_COMPONENT, useFactory: PortofinoModule.toolbarComponent },
    { provide: FOOTER_COMPONENT, useFactory: PortofinoModule.footerComponent },
    { provide: HTTP_INTERCEPTORS, useClass: AuthenticationInterceptor, multi: true },
    { provide: HTTP_INTERCEPTORS, useClass: LanguageInterceptor, multi: true },
    { provide: HTTP_INTERCEPTORS, useClass: NotificationInterceptor, multi: true },
    { provide: HTTP_INTERCEPTORS, useClass: ProgressInterceptor, multi: true },
    { provide: TOKEN_STORAGE_SERVICE, useClass: LocalStorageService },
    { provide: LOCALE_STORAGE_SERVICE, useClass: LocalStorageService },
    { provide: LOCALES, useValue: [
      { key: 'en', name: 'English', translations: TRANSLATIONS_EN },
      { key: 'it', name: 'Italiano', translations: TRANSLATIONS_IT }]},
    { provide: NotificationService, useClass: MatSnackBarNotificationService },
    { provide: ErrorHandler, useClass: NotificationErrorHandler }],
  entryComponents: [LoginComponent, ChangePasswordComponent],
  exports: [
    PortofinoAppComponent, PageLayout, ButtonComponent, ButtonsComponent, LoginComponent, TemplatesComponent,
    ContentComponent, PageFactoryComponent, PageHeader,
    DefaultNavigationComponent, DefaultToolbarComponent, DefaultFooterComponent,
    CrudComponent, SearchFieldComponent, SearchComponent, DetailComponent, CreateComponent, BulkEditComponent,
    SearchComponentHolder, DetailComponentHolder, CreateComponentHolder, BulkEditComponentHolder,
    ManyToManyComponent]
})
export class PortofinoModule {
  static changePasswordComponent() {
    return ChangePasswordComponent;
  }

  static loginComponent() {
    return LoginComponent;
  }

  static navigationComponent() {
    return DefaultNavigationComponent;
  }

  static toolbarComponent() {
    return DefaultToolbarComponent;
  }

  static footerComponent() {
    return DefaultFooterComponent;
  }

  public static withRoutes(routes: Routes, config: ExtraOptions = {}): (ModuleWithProviders|Type<PortofinoModule>)[] {
    return [RouterModule.forRoot(
      [...routes,
              { path: "**", component: ContentComponent}],
      { onSameUrlNavigation: "reload", ...config }),
      PortofinoModule];
  }
}
