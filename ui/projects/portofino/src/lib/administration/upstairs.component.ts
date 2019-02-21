import {
  AfterViewInit,
  Component,
  OnInit,
  ViewChild
} from "@angular/core";
import {PortofinoService} from "../portofino.service";
import {HttpClient} from "@angular/common/http";
import {Page, PageService} from "../page";
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
  connectionProvider: ConnectionProviderDetails;
  isEditConnectionProvider = false;
  databasePlatforms: DatabasePlatform[];

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

  openConnectionProvider(conn: ConnectionProviderSummary) {
    const url = `${this.portofino.apiRoot}portofino-upstairs/database/connections/${conn.name}`;
    this.page.http.get<ConnectionProviderDetails>(url).subscribe(c => { this.connectionProvider = c; });
  }

  @Button({ list: "connection", text: "Edit", color: "primary", presentIf: UpstairsComponent.isViewConnectionProvider })
  editConnectionProvider() {
    this.isEditConnectionProvider = true;
  }

  static isEditConnectionProvider(self: UpstairsComponent) {
    return self.isEditConnectionProvider;
  }

  static isViewConnectionProvider(self: UpstairsComponent) {
    return !self.isEditConnectionProvider;
  }

  @Button({ list: "connection", text: "Save", color: "primary", icon: "save", presentIf: UpstairsComponent.isEditConnectionProvider })
  saveConnectionProvider() {
    const url = `${this.portofino.apiRoot}portofino-upstairs/database/connections/${this.connectionProvider.databaseName.value}`;
    this.page.http.put(url, this.connectionProvider).subscribe(() => {
      this.isEditConnectionProvider = false;
    });
  }

  @Button({ list: "connection", text: "Delete", color: "warn", icon: "delete", presentIf: UpstairsComponent.isViewConnectionProvider })
  deleteConnectionProvider() {
    const url = `${this.portofino.apiRoot}portofino-upstairs/database/connections/${this.connectionProvider.databaseName.value}`;
    this.page.http.delete(url).subscribe(() => {
      this.isEditConnectionProvider = false;
      this.connectionProviders = this.connectionProviders.filter(
        c => c.name != this.connectionProvider.databaseName.value);
      this.connectionProvider = null;
    });
  }

  @Button({ list: "connection", text: "Synchronize", icon: "refresh", presentIf: UpstairsComponent.isViewConnectionProvider })
  synchronizeConnectionProvider() {
    const url = `${this.portofino.apiRoot}portofino-upstairs/database/connections/${this.connectionProvider.databaseName.value}/:synchronize`;
    this.page.http.post(url, {}).subscribe(() => {});
  }

  @Button({ list: "connection", text: "Cancel" })
  closeConnectionProvider() {
    this.isEditConnectionProvider = false;
    this.connectionProvider = null;
  }

  loadDatabasePlatforms() {
    const url = `${this.portofino.apiRoot}portofino-upstairs/database/platforms`;
    this.page.http.get<{ [name: string]: DatabasePlatform }>(url).subscribe(d => {
      this.databasePlatforms = [];
      for (let k in d) {
        this.databasePlatforms.push(d[k]);
      }
    });
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

class ConnectionProviderDetails {
  databaseName: { value: string };
  driver: { value: string };
  errorMessage: { value: string };
  falseString: { value: string };
  hibernateDialect: { value: string };
  jndiResource: { value: string };
  lastTested: { value: number; displayValue: string };
  password: { value: string };
  schemas: { catalog: string; name: string; schema: string; selected: boolean }[];
  status: { value: string };
  trueString: { value: string };
  url: { value: string };
  user: { value: string };
  username: { value: string };
}

class DatabasePlatform {
  description: string;
  standardDriverClassName: string;
  status: string;
}
