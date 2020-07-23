import {Component, Input, NgModule, OnInit} from '@angular/core';
import { MatAutocompleteModule } from "@angular/material/autocomplete";
import { MatButtonModule } from "@angular/material/button";
import { MatCardModule } from "@angular/material/card";
import { MatCheckboxModule } from "@angular/material/checkbox";
import { MatDatepickerModule } from "@angular/material/datepicker";
import { MatDialogModule } from "@angular/material/dialog";
import { MatDividerModule } from "@angular/material/divider";
import { MatExpansionModule } from "@angular/material/expansion";
import { MatFormFieldModule } from "@angular/material/form-field";
import { MatIconModule } from "@angular/material/icon";
import { MatInputModule } from "@angular/material/input";
import { MatListModule } from "@angular/material/list";
import { MatMenuModule } from "@angular/material/menu";
import { MatPaginatorModule } from "@angular/material/paginator";
import { MatProgressBarModule } from "@angular/material/progress-bar";
import { MatRadioModule } from "@angular/material/radio";
import { MatSelectModule } from "@angular/material/select";
import { MatSidenavModule } from "@angular/material/sidenav";
import { MatSnackBarModule } from "@angular/material/snack-bar";
import { MatSortModule } from "@angular/material/sort";
import { MatTableModule } from "@angular/material/table";
import { MatToolbarModule } from "@angular/material/toolbar";
import { MatTreeModule } from "@angular/material/tree";
import {
  PortofinoModule,
  Page,
  NAVIGATION_COMPONENT,
  DefaultNavigationComponent,
  PortofinoComponent,
  PortofinoService,
  SearchResults,
  NOTIFICATION_HANDLERS,
  MatSnackBarNotificationService,
  CrudComponent,
  TextPageComponent,
  CustomPageComponent,
  DetailComponent, PortofinoFormsModule
} from "portofino";
import {BrowserModule} from "@angular/platform-browser";
import {BrowserAnimationsModule} from "@angular/platform-browser/animations";
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {QuillModule} from "ngx-quill";
import {HttpClient, HttpClientModule} from "@angular/common/http";
import {FlexLayoutModule} from "@angular/flex-layout";
import {MatMomentDateModule} from "@angular/material-moment-adapter";
import {FileInputAccessorModule} from "file-input-accessor";
import {TranslateModule} from "@ngx-translate/core";
import {ScrollingModule} from "@angular/cdk/scrolling";
import {NgxdModule} from "@ngxd/core";
import {RouterModule} from "@angular/router";

import {registerLocaleData} from "@angular/common";
import localeEs from "@angular/common/locales/es";
import localeIt from "@angular/common/locales/it";
import { ProfileComponent } from './profile.component';
import {MatChipsModule} from "@angular/material/chips";
import {MatProgressSpinnerModule} from "@angular/material/progress-spinner";

registerLocaleData(localeEs);
registerLocaleData(localeIt);

@Component({
  selector: 'portofino-hello',
  template: `<p>Welcome to Portofino 5!</p>`
})
export class HelloPortofino implements OnInit {

  constructor(protected http: HttpClient, protected portofino: PortofinoService) {}

  ngOnInit(): void {
    this.http.get(this.portofino.apiRoot).subscribe(x => console.log("API Call", x));
  }

}

@Component({
  selector: 'custom-navigation',
  template: `<h3>Custom navigation</h3><p><a routerLink="/hello">Start here</a> </p>`
})
export class CustomNavigation {}

@Component({
  selector: 'portofino-welcome',
  templateUrl: 'home.html',
  styleUrls: ['home.scss']
})
@PortofinoComponent({ name: 'welcome' })
export class WelcomeComponent extends Page implements OnInit {
  projects = [];
  ngOnInit(): void {
    this.http.get<SearchResults>(`${this.portofino.apiRoot}projects?maxResults=20`).subscribe(
      res => this.projects = res.records
    );
  }
}

@PortofinoComponent({ name: 'projects-crud' })
@Component({
  selector: 'demo-tt-projects-crud',
  templateUrl: '../../node_modules/portofino/assets/pages/crud/crud.component.html'
})
export class ProjectsCrud extends CrudComponent {

  initialize(): void {
    super.initialize();
    this.detailComponent = ProjectsSummary;
    this.detailComponentContext = { customInput: "works!" };
  }

  showSearch() {
    this.router.navigateByUrl("/");
  }
}

@Component({
  selector: 'demo-tt-projects-summary',
  template: `<div>
    <mat-card>
      <mat-card-title *ngIf="object">
        {{object.id.value}} – {{object.title.value}}
        <mat-chip-list style="display: inline-block">
          <mat-chip color="primary" selected *ngIf="object.public_.value">Public project</mat-chip>
          <mat-chip color="warn" selected *ngIf="!object.public_.value">Private project</mat-chip>
        </mat-chip-list>
      </mat-card-title>
      <mat-card-title *ngIf="!object">{{'Loading...' | translate}}</mat-card-title>
      <mat-card-content>
        <ng-container *ngIf="object && !editMode">
          <div [innerHTML]="object.description.value"></div>
          <a *ngIf="object.url" [href]="object.url.value">{{object.url.value}}</a>
        </ng-container>
        <form (submit)="save()" *ngIf="object && editMode">
          <portofino-form [controls]="form" [form]="formDefinition" (formReset)="onFormReset()"
                          fxLayout="row wrap" fxLayoutGap="20px" fxLayoutAlign="default center"></portofino-form>
          <button type="submit" style="display:none">{{ 'Save' | translate }}</button>
        </form>
        <mat-spinner *ngIf="loading"></mat-spinner>
        <mat-error *ngIf="!object && !loading">
          {{ '_ not found' | translate:{ what: id } }}
        </mat-error>
      </mat-card-content>
      <mat-card-actions>
        <button type="submit" style="display:none">{{ 'Save' | translate }}</button>
        <portofino-button *ngFor="let button of parentButtons"
                          [button]="button" [component]="parent"></portofino-button>
        <portofino-buttons [component]="this"></portofino-buttons>
      </mat-card-actions>
    </mat-card>
  </div>`,
  styleUrls: ['../../node_modules/portofino/assets/pages/crud/search/search.component.scss']
})
export class ProjectsSummary extends DetailComponent {
  @Input()
  customInput;
  ngOnInit(): void {
    console.log("Custom detail with input", this.customInput);
    super.ngOnInit();
  }

  afterSaved() {
    this.editMode = false;
  }
}

@Component({
  selector: 'app-root',
  template: `<portofino-app appTitle="Demo-TT" apiRoot="http://localhost:18080/demo-tt/api/">
    <portofino-templates></portofino-templates>
  </portofino-app>`
})
export class DemoTTAppComponent {}

@NgModule({
  declarations: [
    DemoTTAppComponent, HelloPortofino, CustomNavigation, WelcomeComponent, ProfileComponent,
    ProjectsCrud, ProjectsSummary],
  providers: [
    { provide: NAVIGATION_COMPONENT, useFactory: DemoTTAppModule.navigation },
    { provide: NOTIFICATION_HANDLERS, useClass: MatSnackBarNotificationService, multi: true },
  ],
  imports: [
    RouterModule.forRoot([
        {path: "hello", component: HelloPortofino}, ...PortofinoModule.defaultRoutes()],
      PortofinoModule.defaultRouterConfig()),
    PortofinoModule,
    BrowserModule, BrowserAnimationsModule, FlexLayoutModule, FormsModule, HttpClientModule, ReactiveFormsModule,
    MatAutocompleteModule, MatButtonModule, MatCardModule, MatCheckboxModule, MatDatepickerModule, MatDialogModule,
    MatDividerModule, MatExpansionModule, MatFormFieldModule, MatIconModule, MatInputModule, MatListModule, MatMenuModule,
    MatPaginatorModule, MatProgressBarModule, MatRadioModule, MatSelectModule, MatSidenavModule, MatSnackBarModule,
    MatSortModule, MatTableModule, MatTreeModule, MatToolbarModule, MatMomentDateModule, ScrollingModule,
    FileInputAccessorModule, NgxdModule, QuillModule.forRoot(),
    TranslateModule.forRoot(), MatChipsModule, MatProgressSpinnerModule, PortofinoFormsModule],
  bootstrap: [DemoTTAppComponent]
})
export class DemoTTAppModule {
  static navigation() {
    return DefaultNavigationComponent
    //return CustomNavigation
  }

  // It's necessary to spell used components here, otherwise Angular (Ivy) tree-shakes them.
  // See https://github.com/angular/angular/issues/33715#issuecomment-617606494 and
  // https://github.com/angular/angular/issues/35314#issuecomment-584821399
  static entryComponents = [ CrudComponent, CustomPageComponent, TextPageComponent ];
}
