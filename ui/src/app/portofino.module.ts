import { BrowserModule } from '@angular/platform-browser';
import { FormsModule } from '@angular/forms';
import { NgModule } from '@angular/core';
import { NgbModule } from '@ng-bootstrap/ng-bootstrap';

import { PortofinoComponent } from './portofino.component';
import { CrudComponent } from './crud/crud.component';
import { PortofinoService } from './portofino.service';
import {HTTP_INTERCEPTORS, HttpClientModule} from '@angular/common/http';
import {AuthenticationService} from "./security/authentication.service";
import { LoginComponent } from './security/login/login.component';


@NgModule({
  declarations: [
    PortofinoComponent,
    CrudComponent,
    LoginComponent
  ],
  imports: [
    BrowserModule, FormsModule, HttpClientModule, NgbModule.forRoot()
  ],
  providers: [PortofinoService, {
    provide: HTTP_INTERCEPTORS,
    useClass: AuthenticationService,
    multi: true
  }],
  bootstrap: [PortofinoComponent],
  entryComponents: [LoginComponent]
})
export class PortofinoModule { }
