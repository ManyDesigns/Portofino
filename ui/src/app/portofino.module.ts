import { BrowserModule } from '@angular/platform-browser';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {enableProdMode, NgModule} from '@angular/core';

import { PortofinoComponent } from './portofino.component';
import { CrudComponent } from './crud/crud.component';
import { PortofinoService } from './portofino.service';
import {HTTP_INTERCEPTORS, HttpClientModule} from '@angular/common/http';
import {AuthenticationInterceptor, AuthenticationService, LOGIN_COMPONENT} from "./security/authentication.service";
import { LoginComponent } from './security/login/login.component';
import {LocalTokenStorageService, TokenStorageService} from "./security/token-storage.service";
import { SearchFieldComponent } from './crud/search/search-field.component';
import {
  MatButtonModule, MatDatepickerModule, MatDialogModule, MatFormFieldModule, MatIconModule, MatInputModule,
  MatMenuModule, MatPaginatorModule, MatSidenavModule, MatSortModule, MatTableModule, MatToolbarModule
} from '@angular/material';
import {MatMomentDateModule} from "@angular/material-moment-adapter";
import {FlexLayoutModule} from "@angular/flex-layout";
import { RouterModule, Routes } from '@angular/router';
import { SearchComponent } from './crud/search/search.component';
import { FieldComponent } from './crud/detail/field.component';
import { EditComponent } from './crud/detail/edit.component';
import { CreateComponent } from './crud/detail/create.component';
import { ContentDirective } from './content.directive';
import { PageComponent } from './page.component';
import {environment} from "../environments/environment";

if(environment.production) {
  enableProdMode();
}

@NgModule({
  declarations: [
    PortofinoComponent, CrudComponent, LoginComponent, SearchFieldComponent, SearchComponent, FieldComponent,
    EditComponent, CreateComponent, ContentDirective, PageComponent
  ],
  imports: [
    BrowserModule, BrowserAnimationsModule, FlexLayoutModule, FormsModule, HttpClientModule, ReactiveFormsModule,
    MatButtonModule, MatDatepickerModule, MatDialogModule, MatFormFieldModule, MatIconModule, MatInputModule,
    MatMenuModule, MatPaginatorModule, MatSidenavModule, MatSortModule, MatTableModule, MatToolbarModule,
    MatMomentDateModule,
    RouterModule.forRoot([{
      path: "**", component: PageComponent
    }])
  ],
  providers: [
    PortofinoService, AuthenticationService,
    { provide: LOGIN_COMPONENT, useFactory: () => LoginComponent },
    { provide: HTTP_INTERCEPTORS, useClass: AuthenticationInterceptor, multi: true },
    { provide: TokenStorageService, useClass: LocalTokenStorageService }],
  bootstrap: [PortofinoComponent],
  entryComponents: [LoginComponent, CrudComponent]
})
export class PortofinoModule { }
