import {
  AfterViewInit,
  Component,
  ComponentFactoryResolver,
  Injectable,
  Injector,
  OnInit,
  ViewChild
} from "@angular/core";
import {PortofinoService} from "../portofino.service";
import {HttpClient} from "@angular/common/http";
import {Page, PageConfiguration, PageService, Permissions} from "../page";
import {map} from "rxjs/operators";
import {ActivatedRoute, Router} from "@angular/router";
import {AuthenticationService} from "../security/authentication.service";
import {Field, Form, FormComponent} from "../form";
import {Property} from "../class-accessor";
import {Button} from "../buttons";

@Component({
  selector: 'portofino-upstairs',
  templateUrl: './upstairs.component.html'
})
export class UpstairsComponent extends Page implements OnInit, AfterViewInit {

  readonly settingsForm = new Form([
    Field.fromProperty(Property.create({name: "appName", label: "Application Name"}).required()),
    Field.fromProperty(Property.create({name: "loginPath", label: "Login Path"}).required())
  ]);
  @ViewChild("settingsFormComponent")
  settingsFormComponent: FormComponent;

  connectionProviders: ConnectionProviderSummary[];

  constructor(portofino: PortofinoService, http: HttpClient, router: Router, route: ActivatedRoute,
              authenticationService: AuthenticationService, protected pageService: PageService) {
    super(portofino, http, router, route, authenticationService);
    this.configuration = {
      title: "Upstairs"
    };
    route.url.pipe(map(segments => segments.join(''))).subscribe(url => {
      this.url = url;
    });
  }

  ngOnInit(): void {
    this.pageService.notifyPageLoaded(this);
    this.settingsPanel.loadPermissions();
    this.loadConnectionProviders();
    this.loadDatabasePlatforms();
  }

  ngAfterViewInit(): void {
    this.resetSettings();
  }

  loadConnectionProviders() {
    const url = `${this.portofino.apiRoot}portofino-upstairs/database/connections`;
    this.page.http.get<ConnectionProviderSummary[]>(url).subscribe(s => { this.connectionProviders = s; });
  }

  loadDatabasePlatforms() {
    const url = `${this.portofino.apiRoot}portofino-upstairs/database/platforms`;
    this.page.http.get<any>(url).subscribe(s => { console.log(s) });
  }

  @Button({ list: "settings", text: "Save", color: "primary" })
  saveSettings() {
    this.settingsFormComponent.controls.updateValueAndValidity(); //TODO why is this needed?
    this.http.put(this.portofino.apiRoot + "portofino-upstairs/settings", this.settingsFormComponent.controls.value).subscribe();
  }

  @Button({ list: "settings", text: "Cancel" })
  resetSettings() {
    this.http.get<any>(this.portofino.apiRoot + "portofino-upstairs/settings").subscribe(settings => {
      this.settingsFormComponent.controls.get('appName').setValue(settings.appName.value);
      this.settingsFormComponent.controls.get('loginPath').setValue(settings.loginPath.value);
    });
  }

}

class ConnectionProviderSummary {
  name: string;
  status: string;
  description: string;
}
