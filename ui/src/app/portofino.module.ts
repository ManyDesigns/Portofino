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
  MatAutocompleteModule,
  MatButtonModule, MatCheckboxModule,
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
  MatSortModule,
  MatTableModule,
  MatToolbarModule
} from '@angular/material';
import {MatMomentDateModule} from "@angular/material-moment-adapter";
import {FlexLayoutModule} from "@angular/flex-layout";
import { RouterModule} from '@angular/router';
import { SearchComponent } from './crud/search/search.component';
import { FieldComponent } from './crud/field.component';
import { DetailComponent } from './crud/detail/detail.component';
import { CreateComponent } from './crud/detail/create.component';
import { ContentDirective } from './content.directive';
import { PageComponent } from './page.component';
import {environment} from "../environments/environment";
import {BulkEditComponent} from "./crud/bulk/bulk-edit.component";

if(environment.production) {
  enableProdMode();
}

@NgModule({
  declarations: [
    PortofinoComponent, CrudComponent, LoginComponent, SearchFieldComponent, SearchComponent, FieldComponent,
    DetailComponent, CreateComponent, BulkEditComponent, ContentDirective, PageComponent
  ],
  imports: [
    BrowserModule, BrowserAnimationsModule, FlexLayoutModule, FormsModule, HttpClientModule, ReactiveFormsModule,
    MatAutocompleteModule, MatButtonModule, MatCheckboxModule, MatDatepickerModule, MatDialogModule, MatFormFieldModule,
    MatIconModule, MatInputModule, MatMenuModule, MatPaginatorModule, MatRadioModule, MatSelectModule, MatSidenavModule,
    MatSortModule, MatTableModule, MatToolbarModule, MatMomentDateModule,
    RouterModule.forRoot([{
      path: "**", component: PageComponent
    }], { onSameUrlNavigation: "reload" })
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
