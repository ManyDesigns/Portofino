import {AfterViewInit, Component, Inject, OnInit, ViewChild} from "@angular/core";
import {Page, PageChild, PageConfiguration} from "../page";
import {Field, Form, FormComponent} from "../form";
import {Property} from "../class-accessor";
import {Button} from "../buttons";
import {Observable, of, throwError} from "rxjs";
import {PortofinoComponent} from "../page.factory";
import {ConnectionsComponent} from "./connections.component";
import {WizardComponent} from "./wizard.component";
import {TablesComponent} from "./tables.component";
import {ActionsComponent} from "./actions.component";
import {LOCALE_STORAGE_SERVICE, PortofinoService} from "../portofino.service";
import {ActivatedRoute, Router} from "@angular/router";
import {AuthenticationService} from "../security/authentication.service";
import {HttpClient} from "@angular/common/http";
import {TranslateService} from "@ngx-translate/core";
import {NotificationService} from "../notifications/notification.service";
import {LocalStorageService} from "ngx-store";
import {map} from "rxjs/operators";

@Component({
  selector: 'portofino-upstairs',
  templateUrl: './upstairs.component.html'
})
@PortofinoComponent({ name: "portofino-upstairs" })
export class UpstairsComponent extends Page implements OnInit {

  constructor(portofino: PortofinoService, http: HttpClient, router: Router, route: ActivatedRoute,
              authenticationService: AuthenticationService, notificationService: NotificationService,
              translate: TranslateService, @Inject(LOCALE_STORAGE_SERVICE) protected storage: LocalStorageService) {
    super(portofino, http, router, route, authenticationService, notificationService, translate);
  }

  loadChildConfiguration(child: PageChild): Observable<PageConfiguration> {
    if(child.path == 'actions') {
      return of({ actualType: ActionsComponent, title: "Actions", source: null, securityCheckPath: null, children: [] });
    } else if(child.path == 'connections') {
      return of({ actualType: ConnectionsComponent, title: "Connections", source: null, securityCheckPath: null, children: [] });
    } else if(child.path == 'permissions') {
      return of({ actualType: PermissionsComponent, title: "Permissions", source: null, securityCheckPath: null, children: [] });
    } else if(child.path == 'settings') {
      return of({ actualType: SettingsComponent, title: "Settings", source: null, securityCheckPath: null, children: [] });
    } else if(child.path == 'tables') {
      return of({ actualType: TablesComponent, title: "Tables", source: null, securityCheckPath: null, children: [] });
    } else if(child.path == 'wizard') {
      return of({ actualType: WizardComponent, title: "wizard", source: null, securityCheckPath: null, children: [] });
    } else {
      return throwError(404);
    }
  }

  changeApiRoot() {
    if(!this.portofino.defaultApiRoot.endsWith("/")) {
      this.portofino.defaultApiRoot += "/";
    }
    this.http.get<any>(this.portofino.defaultApiRoot + ':description').subscribe(response => {
      if(response.loginPath) {
        let loginPath = response.loginPath;
        if(loginPath.startsWith('/')) {
          loginPath = loginPath.substring(1);
        }
        this.portofino.loginPath = loginPath;
        this.doChangeApiRoot();
      }
    }, error => {
      console.error(error);
      this.notificationService.error(this.translate.get("Invalid API root (see console for details)"));
    });
  }

  protected doChangeApiRoot() {
    this.storage.set("portofino.upstairs.apiRoot", this.portofino.defaultApiRoot);
    this.portofino.init();
    this.children.forEach(c => {
      this.checkAccessibility(c);
    });
  }

  prepare() {
    return super.prepare().pipe(map(() => {
      const apiRoot = this.storage.get("portofino.upstairs.apiRoot");
      if(apiRoot) {
        this.portofino.defaultApiRoot = apiRoot;
        this.doChangeApiRoot();
      }
      return this;
    }));
  }

  ngOnInit(): void {
    this.changeApiRoot();
  }

}

@Component({
  selector: 'portofino-upstairs-settings',
  template: `
    <form (submit)="saveSettings()">
      <mat-card>
        <mat-card-content><portofino-form #settingsFormComponent [form]="settingsForm"></portofino-form></mat-card-content>
        <mat-card-actions>
          <button type="submit" style="display:none">{{ 'Save' | translate }}</button>
          <portofino-buttons [component]="this"></portofino-buttons>
        </mat-card-actions>
      </mat-card>
  </form>`
})
export class SettingsComponent extends Page implements AfterViewInit {
  readonly settingsForm = new Form([
    Field.fromProperty(Property.create({name: "appName", label: "Application Name"}).required()),
    Field.fromProperty(Property.create({name: "loginPath", label: "Login Path"}).required())
  ]);
  @ViewChild("settingsFormComponent")
  settingsFormComponent: FormComponent;

  @Button({ text: "Save", color: "primary" })
  saveSettings() {
    this.settingsFormComponent.controls.updateValueAndValidity(); //TODO why is this needed?
    this.http.put(this.portofino.apiRoot + "portofino-upstairs/settings", this.settingsFormComponent.controls.value).subscribe();
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
    </mat-card>`
})
export class PermissionsComponent extends Page implements OnInit {
  ngOnInit(): void {
    this.parent.settingsPanel.loadPermissions();
  }
}
