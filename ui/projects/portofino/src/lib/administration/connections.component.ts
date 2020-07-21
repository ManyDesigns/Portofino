import {Page} from "../page";
import {Component, OnInit} from "@angular/core";
import {ConnectionProviderDetails, ConnectionProviderSummary, DatabasePlatform} from "./support";
import {Button} from "../buttons";

@Component({
  templateUrl: '../../../assets/administration/connections.component.html',
  styleUrls: ['../../../assets/administration/connections.component.scss']
})
export class ConnectionsComponent extends Page implements OnInit {

  connectionProviders: ConnectionProviderSummary[];
  connectionProvider: ConnectionProviderDetails;
  isEditConnectionProvider = false;
  databasePlatforms: DatabasePlatform[];

  ngOnInit(): void {
    this.loadConnectionProviders();
    this.loadDatabasePlatforms();
  }

  loadConnectionProviders() {
    const url = `${this.portofino.apiRoot}portofino-upstairs/database/connections`;
    this.page.http.get<ConnectionProviderSummary[]>(url).subscribe(s => {
      this.connectionProviders = s;
    });
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

  openConnectionProvider(conn: ConnectionProviderSummary) {
    const url = `${this.portofino.apiRoot}portofino-upstairs/database/connections/${conn.name}`;
    this.page.http.get<ConnectionProviderDetails>(url).subscribe(c => { this.connectionProvider = c; });
  }

  @Button({ list: "connection", text: "Edit", color: "primary", presentIf: ConnectionsComponent.isViewConnectionProvider })
  editConnectionProvider() {
    this.isEditConnectionProvider = true;
  }

  static isEditConnectionProvider(self: ConnectionsComponent) {
    return self.isEditConnectionProvider;
  }

  static isViewConnectionProvider(self: ConnectionsComponent) {
    return !self.isEditConnectionProvider;
  }

  @Button({ list: "connection", text: "Save", color: "primary", icon: "save", presentIf: ConnectionsComponent.isEditConnectionProvider })
  saveConnectionProvider() {
    const url = `${this.portofino.apiRoot}portofino-upstairs/database/connections/${this.connectionProvider.databaseName.value}`;
    this.page.http.put(url, this.connectionProvider).subscribe(() => {
      this.isEditConnectionProvider = false;
    });
  }

  @Button({ list: "connection", text: "Delete", color: "warn", icon: "delete", presentIf: ConnectionsComponent.isViewConnectionProvider })
  deleteConnectionProvider() {
    const url = `${this.portofino.apiRoot}portofino-upstairs/database/connections/${this.connectionProvider.databaseName.value}`;
    this.page.http.delete(url).subscribe(() => {
      this.isEditConnectionProvider = false;
      this.connectionProviders = this.connectionProviders.filter(
        c => c.name != this.connectionProvider.databaseName.value);
      this.connectionProvider = null;
    });
  }

  @Button({ list: "connection", text: "Test", icon: "flash_on", presentIf: ConnectionsComponent.isViewConnectionProvider })
  testConnectionProvider() {
    const url = `${this.portofino.apiRoot}portofino-upstairs/database/connections/${this.connectionProvider.databaseName.value}/:test`;
    this.page.http.post<string[]>(url, {}).subscribe(status => {
      if(status[0] == 'connected') {
        this.notificationService.info(this.translate.get("Connection tested successfully"));
      }
    });
  }

  @Button({ list: "connection", text: "Synchronize", icon: "refresh", presentIf: ConnectionsComponent.isViewConnectionProvider })
  synchronizeConnectionProvider() {
    const url = `${this.portofino.apiRoot}portofino-upstairs/database/connections/${this.connectionProvider.databaseName.value}/:synchronize`;
    this.page.http.post(url, {}).subscribe(() => {});
  }

  @Button({ list: "connection", text: "Cancel" })
  closeConnectionProvider() {
    this.isEditConnectionProvider = false;
    this.connectionProvider = null;
  }
}
