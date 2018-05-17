import { BrowserModule } from '@angular/platform-browser';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { FormsModule } from '@angular/forms';
import { NgModule } from '@angular/core';

import { PortofinoComponent } from './portofino.component';
import { CrudComponent } from './crud/crud.component';
import { PortofinoService } from './portofino.service';
import {HTTP_INTERCEPTORS, HttpClientModule} from '@angular/common/http';
import {AuthenticationService} from "./security/authentication.service";
import { LoginComponent } from './security/login/login.component';
import {LocalTokenStorageService, TokenStorageService} from "./security/token-storage.service";
import { SearchFieldComponent } from './crud/search-field/search-field.component';
import { MatButtonModule, MatDialogModule, MatFormFieldModule, MatInputModule } from '@angular/material';


@NgModule({
  declarations: [
    PortofinoComponent,
    CrudComponent,
    LoginComponent,
    SearchFieldComponent
  ],
  imports: [
    BrowserModule, BrowserAnimationsModule, FormsModule, HttpClientModule,
    MatButtonModule, MatDialogModule, MatFormFieldModule, MatInputModule
  ],
  providers: [
    PortofinoService,
    { provide: HTTP_INTERCEPTORS, useClass: AuthenticationService, multi: true },
    { provide: TokenStorageService, useClass: LocalTokenStorageService }],
  bootstrap: [PortofinoComponent],
  entryComponents: [LoginComponent]
})
export class PortofinoModule { }
