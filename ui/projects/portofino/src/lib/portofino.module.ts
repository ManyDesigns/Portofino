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
import {FieldComponent} from './fields/field.component';
import {BreadcrumbsComponent} from "./breadcrumbs/breadcrumbs.component";
import {DetailComponent} from './crud/detail/detail.component';
import {CreateComponent} from './crud/detail/create.component';
import {EmbeddedContentDirective, MainContentDirective, NavigationDirective} from './content.directive';
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
import {ButtonComponent} from "./button.component";
import {QuillModule} from "ngx-quill";
import {DynamicFormComponentDirective, FormComponent} from "./form";
import {TranslateModule} from "@ngx-translate/core";
import {PageComponent} from "./page.component";
import {
  MatSnackBarNotificationService,
  NotificationService} from "./notifications/notification.service";
import {ScrollingModule} from "@angular/cdk/scrolling";
import { NgxdModule } from '@ngxd/core';

@NgModule({
  declarations: [
    PortofinoAppComponent, DefaultPageLayout, ButtonComponent, LoginComponent,
    FieldComponent, BlobFieldComponent, FormComponent,
    PageComponent, PageHeader, MainContentDirective, EmbeddedContentDirective, DynamicFormComponentDirective,
    NavigationDirective, DefaultNavigationComponent, ToolbarDirective, DefaultToolbarComponent,
    SourceSelector, SourceSelectorTree,
    CrudComponent, SearchFieldComponent, SearchComponent, DetailComponent, CreateComponent, BulkEditComponent,
    SearchComponentHolder, DetailComponentHolder, CreateComponentHolder, BulkEditComponentHolder,
    ManyToManyComponent,BreadcrumbsComponent
  ],
  imports: [
    BrowserModule, BrowserAnimationsModule, FlexLayoutModule, FormsModule, HttpClientModule, ReactiveFormsModule,
    MatAutocompleteModule, MatButtonModule, MatCardModule, MatCheckboxModule, MatDatepickerModule, MatDialogModule,
    MatDividerModule, MatExpansionModule, MatFormFieldModule, MatIconModule, MatInputModule, MatMenuModule,
    MatPaginatorModule, MatProgressBarModule, MatRadioModule, MatSelectModule, MatSidenavModule, MatSnackBarModule,
    MatSortModule, MatProgressSpinnerModule, MatTableModule, MatTreeModule,MatListModule, MatToolbarModule,
    MatMomentDateModule, ScrollingModule,
    FileInputAccessorModule, NgxdModule, QuillModule,
    RouterModule.forChild([]),
    TranslateModule
  ],
  providers: [
    PortofinoService, AuthenticationService, PageService,
    { provide: LOGIN_COMPONENT, useFactory: PortofinoModule.loginComponent },
    { provide: NAVIGATION_COMPONENT, useFactory: PortofinoModule.navigationComponent },
    { provide: TOOLBAR_COMPONENT, useFactory: PortofinoModule.toolbarComponent },
    { provide: LOGIN_COMPONENT, useFactory: PortofinoModule.loginComponent },
    { provide: HTTP_INTERCEPTORS, useClass: AuthenticationInterceptor, multi: true },
    { provide: TokenStorageService, useClass: LocalTokenStorageService },
    { provide: NotificationService, useClass: MatSnackBarNotificationService }],
  entryComponents: [
    LoginComponent, DefaultNavigationComponent, DefaultToolbarComponent, SourceSelector, SourceSelectorTree,
    CrudComponent, SearchComponent, DetailComponent, CreateComponent, BulkEditComponent, ManyToManyComponent],
  exports: [
    PortofinoAppComponent, DefaultPageLayout, ButtonComponent, LoginComponent,
    FieldComponent, BlobFieldComponent, FormComponent,
    PageComponent, PageHeader, DefaultNavigationComponent, DefaultToolbarComponent,
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
      [...routes, { path: "**", component: PageComponent}],
      { onSameUrlNavigation: "reload", enableTracing: false }),
      PortofinoModule];
  }
}
