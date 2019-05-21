import {Component, NgModule} from '@angular/core';
import { PortofinoModule, PortofinoUpstairsModule, Page, PortofinoComponent} from "portofino";
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
import {QuillModule} from "ngx-quill";
import {HttpClientModule} from "@angular/common/http";
import {FlexLayoutModule} from "@angular/flex-layout";
import {MatMomentDateModule} from "@angular/material-moment-adapter";
import {FileInputAccessorModule} from "file-input-accessor";
import {TranslateModule} from "@ngx-translate/core";
import {registerLocaleData} from "@angular/common";
import localeIt from "@angular/common/locales/it";

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
        <p>Welcome to Portofino 5. This is your new empty application.</p>
        <p>
          Use the navigation button
          <button title="{{ 'Navigation' | translate }}" type="button" mat-icon-button
                  (click)="portofino.toggleSidenav()">
            <mat-icon aria-label="Side nav toggle icon">menu</mat-icon>
          </button>
          to explore the pages.
        </p>
        <p>Initially, the application has the user admin/admin built in. You can use that to run the wizard, connect to your database, and build a complete application from it.</p>
        <p>The wizard can be found "upstairs", where all the configuration tools lie. The "upstairs" section is optional and can be removed or disabled in production.</p>
      </ng-template>
    </portofino-page-layout>`
})
@PortofinoComponent({ name: 'welcome' })
export class WelcomeComponent extends Page {}

@NgModule({
  declarations: [AppComponent, WelcomeComponent],
  providers: [],
  imports: [
    PortofinoModule.withRoutes([]), PortofinoUpstairsModule,
    BrowserModule, BrowserAnimationsModule, FlexLayoutModule, FormsModule, HttpClientModule, ReactiveFormsModule,
    MatAutocompleteModule, MatButtonModule, MatCheckboxModule, MatDatepickerModule, MatDialogModule, MatFormFieldModule,
    MatIconModule, MatInputModule, MatMenuModule, MatPaginatorModule, MatRadioModule, MatSelectModule, MatSidenavModule,
    MatSnackBarModule, MatSortModule, MatTableModule, MatToolbarModule, MatMomentDateModule,
    FileInputAccessorModule, QuillModule, TranslateModule.forRoot()],
  entryComponents: [WelcomeComponent],
  bootstrap: [AppComponent]
})
export class AppModule {}
