import {AfterViewInit, Component, OnInit, ViewChild} from "@angular/core";
import {Page, PageChild, PageConfiguration} from "../page";
import {Field, Form, FormComponent} from "../form";
import {Property} from "../class-accessor";
import {Button} from "../buttons";
import {Observable, of, throwError} from "rxjs";
import {PortofinoComponent} from "../page.factory";
import {ConnectionsComponent} from "./connections.component";
import {WizardComponent} from "./wizard.component";
import {TablesComponent} from "./tables.component";

@Component({
  selector: 'portofino-upstairs',
  templateUrl: './upstairs.component.html'
})
@PortofinoComponent({ name: "portofino-upstairs" })
export class UpstairsComponent extends Page {

  static defaultConfiguration(): PageConfiguration {
    return {
      title: "Upstairs", type: "portofino-upstairs", source: "", securityCheckPath: "",
      children: [
        { path: "settings", title: "Settings", icon: "settings", showInNavigation: true, accessible: true, embedded: false },
        { path: "permissions", title: "Permissions", icon: "lock", showInNavigation: true, accessible: true, embedded: false },
        { path: "connections", title: "Connections", icon: "lock", showInNavigation: true, accessible: true, embedded: false },
        { path: "wizard", title: "Wizard", icon: "web", showInNavigation: true, accessible: true, embedded: false },
        { path: "tables", title: "Tables", icon: "storage", showInNavigation: true, accessible: true, embedded: false }]
    };
  }

  loadChildConfiguration(child: PageChild): Observable<PageConfiguration> {
    if(child.path == 'connections') {
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
