import {Component, NgModule} from '@angular/core';
import {
  PortofinoModule, Page, CrudComponent, NAVIGATION_COMPONENT, DefaultNavigationComponent,
  PortofinoComponent, PortofinoService, AuthenticationService} from "portofino";
import {
  MatAutocompleteModule,
  MatButtonModule, MatCheckboxModule, MatDatepickerModule, MatDialogModule,
  MatFormFieldModule,
  MatIconModule, MatInputModule, MatMenuModule, MatPaginatorModule, MatRadioModule, MatSelectModule,
  MatSidenavModule,
  MatSnackBarModule,
  MatSortModule,
  MatTableModule, MatToolbarModule
} from "@angular/material";
import {BrowserModule} from "@angular/platform-browser";
import {BrowserAnimationsModule} from "@angular/platform-browser/animations";
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {Router} from "@angular/router";
import {QuillModule} from "ngx-quill";
import {HttpClientModule, HttpClient} from "@angular/common/http";
import {FlexLayoutModule} from "@angular/flex-layout";
import {MatMomentDateModule} from "@angular/material-moment-adapter";
import {FileInputAccessorModule} from "file-input-accessor";
import {TranslateModule} from "@ngx-translate/core";

@Component({
  selector: 'portofino-start',
  template: `<p>Start here</p>`
})
export class StartHere {}

@Component({
  selector: 'custom-navigation',
  template: `<h3>Custom navigation</h3><p><a routerLink="/start">Start here</a> </p>`
})
export class CustomNavigation {}

@Component({
  selector: 'portofino-welcome',
  template: `
    <portofino-default-page-layout [page]="this">
      <ng-template #content><p>Welcome to Portofino 5!</p></ng-template>
    </portofino-default-page-layout>`
})
@PortofinoComponent({ name: 'welcome' })
export class WelcomeComponent extends Page {
  constructor(
    protected http: HttpClient, public portofino: PortofinoService, protected router: Router,
    public authenticationService: AuthenticationService) {
    super(portofino, http, router, authenticationService);
  }
}

@Component({
  selector: 'app-root',
  template: `<portofino-app appTitle="Portofino Application"></portofino-app>`
})
export class AppComponent {}

@NgModule({
  declarations: [AppComponent, StartHere, CustomNavigation, WelcomeComponent],
  providers: [
    { provide: NAVIGATION_COMPONENT, useFactory: AppModule.navigation },
  ],
  imports: [
    PortofinoModule.withRoutes([{ path: "start", component: StartHere }]),
    BrowserModule, BrowserAnimationsModule, FlexLayoutModule, FormsModule, HttpClientModule, ReactiveFormsModule,
    MatAutocompleteModule, MatButtonModule, MatCheckboxModule, MatDatepickerModule, MatDialogModule, MatFormFieldModule,
    MatIconModule, MatInputModule, MatMenuModule, MatPaginatorModule, MatRadioModule, MatSelectModule, MatSidenavModule,
    MatSnackBarModule, MatSortModule, MatTableModule, MatToolbarModule, MatMomentDateModule,
    FileInputAccessorModule, QuillModule, TranslateModule.forRoot()],
  entryComponents: [ CustomNavigation, WelcomeComponent ],
  bootstrap: [AppComponent]
})
export class AppModule {
  static navigation() {
    return DefaultNavigationComponent
    //return CustomNavigation
  }
}
