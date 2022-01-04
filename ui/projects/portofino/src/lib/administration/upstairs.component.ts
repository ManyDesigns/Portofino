import {AfterViewInit, Component, Inject, OnInit, ViewChild} from "@angular/core";
import {Page, PageChild, PageConfiguration, PortofinoComponent} from "../page";
import {Field, Form, FormComponent} from "../form";
import {Property} from "../class-accessor";
import {Button} from "../buttons";
import {Observable, of} from "rxjs";
import {ConnectionsComponent} from "./connections.component";
import {WizardComponent} from "./wizard.component";
import {TablesComponent} from "./tables.component";
import {ActionsComponent} from "./actions.component";
import {LOCALE_STORAGE_SERVICE, PortofinoService} from "../portofino.service";
import {ActivatedRoute, Router} from "@angular/router";
import {AuthenticationService} from "../security/authentication.service";
import {HttpClient} from "@angular/common/http";
import {TranslateService} from "@ngx-translate/core";
import {NotificationService} from "../notifications/notification.services";
import {WebStorageService} from "../storage/storage.services";
import {Location} from "@angular/common";
import {CrudComponent} from "../pages/crud/crud.component";

export const API_ROOT_KEY = "portofino.upstairs.apiRoot";

@Component({
  selector: 'portofino-upstairs',
  templateUrl: '../../../assets/administration/upstairs.component.html'
})
@PortofinoComponent({ name: "portofino-upstairs", hideFromCreateNewPage: true })
export class UpstairsComponent extends Page implements OnInit {

  constructor(portofino: PortofinoService, http: HttpClient, router: Router, route: ActivatedRoute,
              authenticationService: AuthenticationService, notificationService: NotificationService,
              translate: TranslateService, location: Location,
              @Inject(LOCALE_STORAGE_SERVICE) protected storage: WebStorageService) {
    super(portofino, http, router, route, authenticationService, notificationService, translate, location);
  }

  get children() {
    return [
      { path: 'settings', title: 'Settings', icon: 'settings', accessible: true, showInNavigation: true },
      { path: 'permissions', title: 'Permissions', icon: 'lock', accessible: true, showInNavigation: true },
      { path: 'connections', title: 'Connections', icon: 'network_wifi', accessible: true, showInNavigation: true },
      { path: 'wizard', title: 'Wizard', icon: 'web', accessible: true, showInNavigation: true },
      { path: 'tables', title: 'Tables', icon: 'storage', accessible: true, showInNavigation: true },
      { path: 'actions', title: 'Actions', icon: 'build', accessible: true, showInNavigation: true },
      { path: 'mail', title: 'Mail', icon: 'email', accessible: true, showInNavigation: true },
      { path: 'crud', title: 'Quick CRUD', icon: 'table_chart', accessible: true, showInNavigation: true }
    ];
  }

  loadChildConfiguration(child: PageChild): Observable<PageConfiguration> {
    const types = {
      actions: ActionsComponent, connections: ConnectionsComponent, crud: GenericCrudComponent,
      mail: MailSettingsComponent, permissions: PermissionsComponent, settings: SettingsComponent,
      tables: TablesComponent, wizard: WizardComponent
    };
    return of({ title: child.title, source: null, children: [], actualType: types[child.path] });
  }

  changeApiRoot() {
    if(this.portofino.localApiAvailable) {
      return;
    }
    if(!this.portofino.apiRoot.endsWith("/")) {
      this.portofino.apiRoot += "/";
    }
    this.http.get<any>(this.portofino.apiRoot + ':description').subscribe(() => {
      this.checkAccess(true);
      this.storage.set(API_ROOT_KEY, this.portofino.apiRoot);
    }, error => {
      console.error(error);
      this.notificationService.error(this.translate.get("Invalid API root (see console for details)"));
    });
  }

  ngOnInit(): void {
    this.changeApiRoot();
  }

}

@Component({
  selector: 'portofino-upstairs-settings',
  template: `
    <div style="padding: 20px;">
      <form (submit)="saveSettings()">
        <mat-card>
          <mat-card-content><portofino-form #settingsFormComponent [form]="settingsForm"></portofino-form></mat-card-content>
          <mat-card-actions>
            <button type="submit" style="display:none">{{ 'Save' | translate }}</button>
            <portofino-buttons [component]="this"></portofino-buttons>
          </mat-card-actions>
        </mat-card>
      </form>
    </div>`
})
export class SettingsComponent extends Page implements AfterViewInit {
  readonly settingsForm = new Form([
    Field.fromProperty(Property.create({name: "appName", label: "Application Name"}).required()),
    Field.fromProperty(Property.create({name: "loginPath", label: "Login Path"}).required())
  ]);
  @ViewChild("settingsFormComponent", { static: true })
  settingsFormComponent: FormComponent;

  @Button({ text: "Save", color: "primary" })
  saveSettings() {
    this.settingsFormComponent.controls.updateValueAndValidity(); //TODO why is this needed?
    this.http.put(this.portofino.apiRoot + "portofino-upstairs/settings", this.settingsFormComponent.controls.value).subscribe(
      () => this.notificationService.info(this.translate.get("Settings saved"))
    );
  }

  @Button({ text: "Cancel" })
  resetSettings() {
    this.http.get<any>(this.portofino.apiRoot + "portofino-upstairs/settings").subscribe(settings => {
      this.settingsFormComponent.controls.get('appName').setValue(settings.appName.value);
      this.settingsFormComponent.controls.get('loginPath').setValue(settings.loginPath.value);
    });
  }

  ngAfterViewInit(): void {
    this.resetSettings();
  }

}

@Component({
  template: `
    <div style="padding: 20px;">
      <mat-card>
        <mat-card-content>
          <table *ngIf="parent.settingsPanel.permissions" class="mat-table">
            <tr class="mat-header-row">
              <th class="mat-header-cell">{{'Group'|translate}}</th>
              <th class="mat-header-cell">{{'Access level'|translate}}</th>
            </tr>
            <tr *ngFor="let group of parent.settingsPanel.groups" class="mat-row">
              <td class="mat-cell">{{group.name}}</td>
              <td class="mat-cell">
                <mat-select [(ngModel)]="group.level">
                  <mat-option *ngFor="let level of parent.settingsPanel.accessLevels" [value]="level">
                    {{level}}
                  </mat-option>
                </mat-select>
              </td>
            </tr>
          </table>
        </mat-card-content>
        <mat-card-actions><portofino-buttons [component]="parent" list="permissions"></portofino-buttons></mat-card-actions>
      </mat-card>
    </div>`
})
export class PermissionsComponent extends Page implements OnInit {
  ngOnInit(): void {
    this.parent.settingsPanel.loadPermissions();
  }
}

@Component({
  selector: 'portofino-upstairs-mail-settings',
  template: `
    <div style="padding: 20px;">
      <form (submit)="saveSettings()">
        <mat-card>
          <mat-card-content>
            <portofino-form #settingsFormComponent [form]="settingsForm"
                            fxLayout="row wrap" fxLayoutGap="20px" fxLayoutAlign="default center">
            </portofino-form>
          </mat-card-content>
          <mat-card-actions>
            <button type="submit" style="display:none">{{ 'Save' | translate }}</button>
            <portofino-buttons [component]="this"></portofino-buttons>
          </mat-card-actions>
        </mat-card>
      </form>
    </div>`
})
export class MailSettingsComponent extends Page implements AfterViewInit {
  readonly settingsForm = new Form([
    Field.fromProperty(Property.create({name: "mailEnabled", label: "Mail enabled", type: "boolean"}).required()),
    Field.fromProperty(Property.create({name: "keepSent", label: "Keep sent messages", type: "boolean"}).required()),
    Field.fromProperty(Property.create({name: "queueLocation", label: "Queue location"}).required()),
    Field.fromProperty(Property.create({name: "smtpHost", label: "Host"})),
    Field.fromProperty(Property.create({name: "smtpPort", label: "Port"})),
    Field.fromProperty(Property.create({name: "smtpSSL", label: "SSL enabled", type: "boolean"})),
    Field.fromProperty(Property.create({name: "smtpTLS", label: "TLS enabled", type: "boolean"})),
    Field.fromProperty(Property.create({name: "smtpLogin", label: "Login"})),
    Field.fromProperty(Property.create({name: "smtpPassword", label: "Password"}).withAnnotation("com.manydesigns.elements.annotations.Password")),
  ]);
  @ViewChild("settingsFormComponent", { static: true })
  settingsFormComponent: FormComponent;

  @Button({ text: "Save", color: "primary" })
  saveSettings() {
    this.settingsFormComponent.controls.updateValueAndValidity(); //TODO why is this needed?
    this.http.put(this.portofino.apiRoot + "portofino-upstairs/mail", this.settingsFormComponent.controls.value).subscribe(
      () => this.notificationService.info(this.translate.get("Settings saved"))
    );
  }

  @Button({ text: "Cancel" })
  resetSettings() {
    this.http.get<any>(this.portofino.apiRoot + "portofino-upstairs/mail").subscribe(settings => {
      this.settingsFormComponent.controls.get('mailEnabled').setValue(settings.mailEnabled.value);
      this.settingsFormComponent.controls.get('keepSent').setValue(settings.keepSent.value);
      this.settingsFormComponent.controls.get('queueLocation').setValue(settings.queueLocation.value);
      this.settingsFormComponent.controls.get('smtpHost').setValue(settings.smtpHost.value);
      this.settingsFormComponent.controls.get('smtpPort').setValue(settings.smtpPort.value);
      this.settingsFormComponent.controls.get('smtpSSL').setValue(settings.smtpSSL.value);
      this.settingsFormComponent.controls.get('smtpTLS').setValue(settings.smtpTLS.value);
      this.settingsFormComponent.controls.get('smtpLogin').setValue(settings.smtpLogin.value);
      this.settingsFormComponent.controls.get('smtpPassword').setValue(settings.smtpPassword.value);
    });
  }

  ngAfterViewInit(): void {
    this.resetSettings();
  }

}

@Component({
  selector: 'portofino-upstairs-generic-crud',
  template: `
    <mat-card>
      <mat-card-content>
        <mat-form-field>
          <mat-label>{{'Source'|translate}}</mat-label>
          <input matInput [(ngModel)]="crudConfiguration.source"/>
        </mat-form-field>
        <button mat-button (click)="setSource()">{{"Connect"|translate}}</button>
        <portofino-crud #crud></portofino-crud>
      </mat-card-content>
    </mat-card>`
})
export class GenericCrudComponent extends Page {

  @ViewChild("crud")
  crud: CrudComponent;

  crudConfiguration = {
    title: "Quick CRUD",
    source: "/",
    children: [],
    openDetailInSamePageWhenEmbedded: true
  };

  setSource() {
    this.crud.configuration = {...this.crudConfiguration}; //Force creation of a new object for change detection
    this.crud.reset();
    setTimeout(() => {
      this.crud.initialize();
    });
  }

}
