import {BrowserModule} from '@angular/platform-browser';
import {BrowserAnimationsModule} from '@angular/platform-browser/animations';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {NgModule} from '@angular/core';
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
import {RouterModule} from '@angular/router';
import {SearchComponent} from './crud/search/search.component';
import {FieldComponent} from './crud/field.component';
import {DetailComponent} from './crud/detail/detail.component';
import {CreateComponent} from './crud/detail/create.component';
import {EmbeddedContentDirective, MainContentDirective, NavigationDirective} from './content.directive';
import {DefaultNavigationComponent, NAVIGATION_COMPONENT, PageComponent} from './page.component';
import {BulkEditComponent} from "./crud/bulk/bulk-edit.component";
import {BlobFieldComponent} from "./crud/blob-field.component";
import {FileInputAccessorModule} from "file-input-accessor";
import {ManyToManyComponent} from './many-to-many/many-to-many.component';
import {ButtonComponent} from "./button.component";
import {QuillModule} from "ngx-quill";

@NgModule({
  declarations: [
    PortofinoAppComponent, ButtonComponent, LoginComponent,
    CrudComponent, SearchFieldComponent, SearchComponent, FieldComponent, BlobFieldComponent, DetailComponent,
    CreateComponent, BulkEditComponent, ManyToManyComponent,
    MainContentDirective, EmbeddedContentDirective, NavigationDirective, PageComponent, DefaultNavigationComponent,
    ToolbarDirective, DefaultToolbarComponent
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
    { provide: LOGIN_COMPONENT, useFactory: () => LoginComponent },
    { provide: NAVIGATION_COMPONENT, useFactory: () => DefaultNavigationComponent },
    { provide: TOOLBAR_COMPONENT, useFactory: () => DefaultToolbarComponent },
    { provide: HTTP_INTERCEPTORS, useClass: AuthenticationInterceptor, multi: true },
    { provide: TokenStorageService, useClass: LocalTokenStorageService }],
  bootstrap: [PortofinoAppComponent],
  entryComponents: [
    LoginComponent, DefaultNavigationComponent, DefaultToolbarComponent,
    CrudComponent, ManyToManyComponent]
})
export class PortofinoModule { }
