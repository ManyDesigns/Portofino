import {BrowserModule} from '@angular/platform-browser';
import {BrowserAnimationsModule} from '@angular/platform-browser/animations';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {ModuleWithProviders, NgModule} from '@angular/core';
import {
  DefaultToolbarComponent,
  PortofinoAppComponent,
  TOOLBAR_COMPONENT,
  ToolbarDirective
} from './portofino-app.component';
import {CrudComponent} from './crud/crud.component';
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
  MatRadioModule,
  MatSelectModule,
  MatSidenavModule,
  MatSnackBarModule,
  MatSortModule,
  MatTableModule,
  MatToolbarModule
} from '@angular/material';
import {MatMomentDateModule} from "@angular/material-moment-adapter";
import {FlexLayoutModule} from "@angular/flex-layout";
import {RouterModule, Routes} from '@angular/router';
import {SearchComponent} from './crud/search/search.component';
import {FieldComponent} from './fields/field.component';
import {DetailComponent} from './crud/detail/detail.component';
import {CreateComponent} from './crud/detail/create.component';
import {EmbeddedContentDirective, MainContentDirective, NavigationDirective} from './content.directive';
import {
  DefaultNavigationComponent,
  DefaultPageLayout,
  NAVIGATION_COMPONENT,
  Page,
  PageComponent,
  PageHeader
} from './page.component';
import {BulkEditComponent} from "./crud/bulk/bulk-edit.component";
import {BlobFieldComponent} from "./fields/blob-field.component";
import {FileInputAccessorModule} from "file-input-accessor";
import {ManyToManyComponent} from './many-to-many/many-to-many.component';
import {ButtonComponent} from "./button.component";
import {QuillModule} from "ngx-quill";

@NgModule({
  declarations: [
    PortofinoAppComponent, DefaultPageLayout, ButtonComponent, LoginComponent,
    CrudComponent, SearchFieldComponent, SearchComponent, FieldComponent, BlobFieldComponent, DetailComponent,
    CreateComponent, BulkEditComponent, ManyToManyComponent,
    PageComponent, PageHeader, MainContentDirective, EmbeddedContentDirective,
    NavigationDirective, DefaultNavigationComponent, ToolbarDirective, DefaultToolbarComponent
  ],
  imports: [
    BrowserModule, BrowserAnimationsModule, FlexLayoutModule, FormsModule, HttpClientModule, ReactiveFormsModule,
    MatAutocompleteModule, MatButtonModule, MatCheckboxModule, MatDatepickerModule, MatDialogModule, MatFormFieldModule,
    MatIconModule, MatInputModule, MatMenuModule, MatPaginatorModule, MatRadioModule, MatSelectModule, MatSidenavModule,
    MatSnackBarModule, MatSortModule, MatTableModule, MatToolbarModule, MatMomentDateModule,
    FileInputAccessorModule, QuillModule,
    RouterModule.forChild([])
  ],
  providers: [
    PortofinoService, AuthenticationService,
    { provide: LOGIN_COMPONENT, useFactory: PortofinoModule.loginComponent },
    { provide: NAVIGATION_COMPONENT, useFactory: PortofinoModule.navigationComponent },
    { provide: TOOLBAR_COMPONENT, useFactory: PortofinoModule.toolbarComponent },
    { provide: LOGIN_COMPONENT, useFactory: PortofinoModule.loginComponent },
    { provide: HTTP_INTERCEPTORS, useClass: AuthenticationInterceptor, multi: true },
    { provide: TokenStorageService, useClass: LocalTokenStorageService }],
  entryComponents: [
    LoginComponent, DefaultNavigationComponent, DefaultToolbarComponent,
    CrudComponent, ManyToManyComponent],
  exports: [
    PortofinoAppComponent, DefaultPageLayout, ButtonComponent, LoginComponent,
    PageComponent, PageHeader, CrudComponent, SearchFieldComponent, SearchComponent, FieldComponent,
    BlobFieldComponent, DetailComponent, CreateComponent, BulkEditComponent, ManyToManyComponent,
    DefaultNavigationComponent, DefaultToolbarComponent]
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

  public static withRoutes(routes: Routes): (ModuleWithProviders)[] {
    return [RouterModule.forRoot(
      [...routes, { path: "**", component: PageComponent}],
      { onSameUrlNavigation: "reload", enableTracing: false }),
      {
        ngModule: PortofinoModule,
        providers: []
      }];
  }
}
