import {Component, NgModule, Optional} from '@angular/core';
import {
  PortofinoModule, PortofinoUpstairsModule, AuthenticationService,
  NotificationService, MatSnackBarNotificationService, NOTIFICATION_HANDLERS,
  Page, PortofinoComponent, PortofinoService, SidenavService,
  CrudComponent, CustomPageComponent, TextPageComponent,
  UpstairsComponent,
  PageLayout
} from "portofino";
import { MatAutocompleteModule } from "@angular/material/autocomplete";
import { MatButtonModule } from "@angular/material/button";
import { MatCheckboxModule } from "@angular/material/checkbox";
import { MatDatepickerModule } from "@angular/material/datepicker";
import { MatDialogModule } from "@angular/material/dialog";
import { MatFormFieldModule } from "@angular/material/form-field";
import { MatIconModule } from "@angular/material/icon";
import { MatInputModule } from "@angular/material/input";
import { MatMenuModule } from "@angular/material/menu";
import { MatPaginatorModule } from "@angular/material/paginator";
import { MatRadioModule } from "@angular/material/radio";
import { MatSelectModule } from "@angular/material/select";
import { MatSidenavModule } from "@angular/material/sidenav";
import { MatSnackBarModule } from "@angular/material/snack-bar";
import { MatSortModule } from "@angular/material/sort";
import { MatTableModule } from "@angular/material/table";
import { MatToolbarModule } from "@angular/material/toolbar";
import {BrowserModule} from "@angular/platform-browser";
import {BrowserAnimationsModule} from "@angular/platform-browser/animations";
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {ActivatedRoute, Router, RouterModule} from '@angular/router';
import {QuillModule} from "ngx-quill";
import {HttpClientModule, HttpClient} from "@angular/common/http";
import {FlexLayoutModule} from "@angular/flex-layout";
import { MatLuxonDateModule } from "@angular/material-luxon-adapter";
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

@NgModule({
  declarations: [AppComponent],
  providers: [
    { provide: NOTIFICATION_HANDLERS, useClass: MatSnackBarNotificationService, multi: true }
  ],
  imports: [
    RouterModule.forRoot([...PortofinoModule.defaultRoutes()], PortofinoModule.defaultRouterConfig()),
    PortofinoModule, PortofinoUpstairsModule,
    BrowserModule, BrowserAnimationsModule, FlexLayoutModule, FormsModule, HttpClientModule, ReactiveFormsModule,
    MatAutocompleteModule, MatButtonModule, MatCheckboxModule, MatDatepickerModule, MatDialogModule, MatFormFieldModule,
    MatIconModule, MatInputModule, MatMenuModule, MatPaginatorModule, MatRadioModule, MatSelectModule, MatSidenavModule,
    MatSnackBarModule, MatSortModule, MatTableModule, MatToolbarModule, MatLuxonDateModule,
    FileInputAccessorModule, QuillModule.forRoot(), TranslateModule.forRoot()],
  bootstrap: [AppComponent]
})
export class AppModule {
  // It's necessary to spell the components used in the application here, otherwise Angular (Ivy) tree-shakes them.
  // See https://github.com/angular/angular/issues/33715#issuecomment-617606494 and
  // https://github.com/angular/angular/issues/35314#issuecomment-584821399
  static entryComponents = [
    CrudComponent, CustomPageComponent, TextPageComponent, UpstairsComponent ];
}
