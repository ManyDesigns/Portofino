import {Component, NgModule} from '@angular/core';
import {
  PortofinoModule, Page, NAVIGATION_COMPONENT, DefaultNavigationComponent,
  PortofinoComponent} from "portofino";
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
import {QuillModule} from "ngx-quill";
import {HttpClientModule} from "@angular/common/http";
import {FlexLayoutModule} from "@angular/flex-layout";
import {MatMomentDateModule} from "@angular/material-moment-adapter";
import {FileInputAccessorModule} from "file-input-accessor";
import {TranslateModule} from "@ngx-translate/core";
import {ScrollingModule} from "@angular/cdk/scrolling";
import {NgxdModule} from "@ngxd/core";
import {registerLocaleData} from "@angular/common";
import localeIt from "@angular/common/locales/it";

registerLocaleData(localeIt);

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
      <ng-template #content>
        <p>Welcome to the Portofino 5 demo application "demo-tt"!</p>
        <p>
          Use the navigation button
          <button title="{{ 'Navigation' | translate }}" type="button" mat-icon-button
                  (click)="portofino.toggleSidenav()">
            <mat-icon aria-label="Side nav toggle icon">menu</mat-icon>
          </button>
          to explore the app.
        </p>
      </ng-template>
    </portofino-default-page-layout>`
})
@PortofinoComponent({ name: 'welcome' })
export class WelcomeComponent extends Page {}

@Component({
  selector: 'app-root',
  template: `<portofino-app appTitle="Demo-TT" apiRoot="http://localhost:8080/demo-tt/api/"></portofino-app>`
})
export class DemoTTAppComponent {}

@NgModule({
  declarations: [DemoTTAppComponent, StartHere, CustomNavigation, WelcomeComponent],
  providers: [
    { provide: NAVIGATION_COMPONENT, useFactory: DemoTTAppModule.navigation },
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
  bootstrap: [DemoTTAppComponent]
})
export class DemoTTAppModule {
  static navigation() {
    return DefaultNavigationComponent
    //return CustomNavigation
  }
}
