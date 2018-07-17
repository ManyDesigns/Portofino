import {BrowserModule} from '@angular/platform-browser';
import {BrowserAnimationsModule} from '@angular/platform-browser/animations';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {NgModule} from '@angular/core';
import {PortofinoAppComponent} from './portofino-app.component';
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
import {ContentDirective} from './content.directive';
import {PageComponent} from './page.component';
import {BulkEditComponent} from "./crud/bulk/bulk-edit.component";
import {BlobFieldComponent} from "./crud/blob-field.component";
import {FileInputAccessorModule} from "file-input-accessor";
import {ManyToManyComponent} from './many-to-many/many-to-many.component';

@NgModule({
  declarations: [
    PortofinoAppComponent, CrudComponent, LoginComponent, SearchFieldComponent, SearchComponent,
    FieldComponent, BlobFieldComponent, DetailComponent, CreateComponent, BulkEditComponent,
    ContentDirective, PageComponent, ManyToManyComponent
  ],
  imports: [
    BrowserModule, BrowserAnimationsModule, FlexLayoutModule, FormsModule, HttpClientModule, ReactiveFormsModule,
    MatAutocompleteModule, MatButtonModule, MatCheckboxModule, MatDatepickerModule, MatDialogModule, MatFormFieldModule,
    MatIconModule, MatInputModule, MatMenuModule, MatPaginatorModule, MatRadioModule, MatSelectModule, MatSidenavModule,
    MatSnackBarModule, MatSortModule, MatTableModule, MatToolbarModule, MatMomentDateModule,
    FileInputAccessorModule,
    RouterModule.forChild([])
  ],
  providers: [
    PortofinoService, AuthenticationService,
    { provide: LOGIN_COMPONENT, useFactory: () => LoginComponent },
    { provide: HTTP_INTERCEPTORS, useClass: AuthenticationInterceptor, multi: true },
    { provide: TokenStorageService, useClass: LocalTokenStorageService }],
  bootstrap: [PortofinoAppComponent],
  entryComponents: [LoginComponent, CrudComponent, ManyToManyComponent]
})
export class PortofinoModule { }
