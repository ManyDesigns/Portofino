import {Component, NgModule} from '@angular/core';
import {
  PortofinoModule, Page, NAVIGATION_COMPONENT, DefaultNavigationComponent,
  PortofinoComponent, PortofinoService, AuthenticationService} from "portofino";
import {
  MatAutocompleteModule,
  MatButtonModule,
  MatCardModule,
  MatCheckboxModule,
  MatDatepickerModule,
  MatDialogModule,
  MatDividerModule,
  MatExpansionModule,
  MatFormFieldModule,
  MatIconModule,
  MatInputModule, MatListModule,
  MatMenuModule,
  MatPaginatorModule, MatProgressBarModule,
  MatRadioModule,
  MatSelectModule,
  MatSidenavModule,
  MatSnackBarModule,
  MatSortModule,
  MatTableModule,
  MatToolbarModule, MatTreeModule
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
import {ScrollingModule} from "@angular/cdk/scrolling";
import {NgxdModule} from "@ngxd/core";

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
    http: HttpClient, portofino: PortofinoService, router: Router,  authenticationService: AuthenticationService) {
    super(portofino, http, router, authenticationService);
  }
}

@Component({
  selector: 'app-root',
  template: `<portofino-app appTitle="Demo-TT"></portofino-app>`
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
    MatAutocompleteModule, MatButtonModule, MatCardModule, MatCheckboxModule, MatDatepickerModule, MatDialogModule,
    MatDividerModule, MatExpansionModule, MatFormFieldModule, MatIconModule, MatInputModule, MatListModule, MatMenuModule,
    MatPaginatorModule, MatProgressBarModule, MatRadioModule, MatSelectModule, MatSidenavModule, MatSnackBarModule,
    MatSortModule, MatTableModule, MatTreeModule, MatToolbarModule, MatMomentDateModule, ScrollingModule,
    FileInputAccessorModule, NgxdModule, QuillModule,
    TranslateModule.forRoot()],
  entryComponents: [ CustomNavigation, WelcomeComponent ],
  bootstrap: [AppComponent]
})
export class AppModule {
  static navigation() {
    return DefaultNavigationComponent
    //return CustomNavigation
  }
}
