import {Component, NgModule, Optional} from '@angular/core';
import {
  PortofinoModule, PortofinoUpstairsModule, AuthenticationService,
  NotificationService, MatSnackBarNotificationService, NOTIFICATION_HANDLERS,
  Page, PortofinoComponent, PortofinoService, SidenavService
} from "portofino";
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
import {ActivatedRoute, Router} from '@angular/router';
import {QuillModule} from "ngx-quill";
import {HttpClientModule, HttpClient} from "@angular/common/http";
import {FlexLayoutModule} from "@angular/flex-layout";
import {MatMomentDateModule} from "@angular/material-moment-adapter";
import {FileInputAccessorModule} from "file-input-accessor";
import {TranslateModule, TranslateService} from "@ngx-translate/core";
import {registerLocaleData} from "@angular/common";

//Customize for your app's locales, if any. As of version 5.1.3, Portofino is translated to Spanish and Italian.
//Additional translations are welcome!
import localeEs from "@angular/common/locales/es";
import localeIt from "@angular/common/locales/it";

registerLocaleData(localeEs);
registerLocaleData(localeIt);

@Component({
  selector: 'app-root',
  template: `<portofino-app appTitle="Portofino Application" apiRoot="http://localhost:8080/api/"></portofino-app>`
})
export class AppComponent {}

@Component({
  selector: 'portofino-welcome',
  template: `
    <portofino-page-layout [page]="this">
      <ng-template #content>
        <p>Welcome to Portofino 5. This is your new, empty application.</p>
        <p>
          Use the navigation button
          <button title="{{ 'Navigation' | translate }}" type="button" mat-icon-button
                  (click)="sidenav.toggle()">
            <mat-icon aria-label="Side nav toggle icon">menu</mat-icon>
          </button>
          to explore the pages.
        </p>
        <p>
          Your application has a REST API which depends on the resource-actions that it's made of.
          You can download <a [href]="portofino.apiRoot">the documentation of the REST API</a> in OpenAPI (aka Swagger) format.
        </p>
        <p>
          Initially, the application has the user admin/admin built in.
          You can use that to <a [routerLink]="portofino.upstairsLink + '/wizard'">run the wizard</a>,
          connect to your database, and build a complete application from it in a few clicks. Please refer to the
          <a href="https://github.com/ManyDesigns/Portofino/wiki/Getting-started-with-Portofino-5">getting started page</a>
          if you feel lost.
        </p>
        <p>
          The wizard is one of the tools that can be found in the administration section
          <a [routerLink]="portofino.upstairsLink">"upstairs"</a> (link in the toolbar).
          The "upstairs" section is optional and can be disabled in production, leaving only the "downstairs" floor, i.e., the application.
          "Upstairs" and "downstairs" are historical references to Portofino 3, which used the same model-driven interface
          both for the application and for the application's model (the metamodel).
        </p>
        <p>
          You can find additional documentation in the <a href="https://github.com/ManyDesigns/Portofino/wiki">wiki</a>
          and in the <a href="https://portofino.manydesigns.com/en/docs">documentation center</a>, which was written for
          Portofino 4.x, but is in many cases still applicable.
        </p>
      </ng-template>
    </portofino-page-layout>`
})
@PortofinoComponent({ name: 'welcome' })
export class WelcomeComponent extends Page {
  constructor(
    portofino: PortofinoService, http: HttpClient, router: Router,
    @Optional() route: ActivatedRoute, authenticationService: AuthenticationService,
    notificationService: NotificationService, translate: TranslateService,
    public sidenav: SidenavService) {
    super(portofino, http, router, route, authenticationService, notificationService, translate);
  }
}

@NgModule({
  declarations: [AppComponent, WelcomeComponent],
  providers: [
    { provide: NOTIFICATION_HANDLERS, useClass: MatSnackBarNotificationService, multi: true }
  ],
  imports: [
    PortofinoModule.withRoutes([]), PortofinoUpstairsModule,
    BrowserModule, BrowserAnimationsModule, FlexLayoutModule, FormsModule, HttpClientModule, ReactiveFormsModule,
    MatAutocompleteModule, MatButtonModule, MatCheckboxModule, MatDatepickerModule, MatDialogModule, MatFormFieldModule,
    MatIconModule, MatInputModule, MatMenuModule, MatPaginatorModule, MatRadioModule, MatSelectModule, MatSidenavModule,
    MatSnackBarModule, MatSortModule, MatTableModule, MatToolbarModule, MatMomentDateModule,
    FileInputAccessorModule, QuillModule.forRoot(), TranslateModule.forRoot()],
  entryComponents: [WelcomeComponent],
  bootstrap: [AppComponent]
})
export class AppModule {}
