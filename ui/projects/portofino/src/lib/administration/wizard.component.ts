import {Page, PageConfiguration} from "../page";
import {Component, OnInit} from "@angular/core";
import {ConnectionProviderDetails, ConnectionProviderSummary, DatabasePlatform} from "./support";
import {from} from "rxjs";
import {mergeMap} from "rxjs/operators";

@Component({
  templateUrl: 'wizard.component.html'
})
export class WizardComponent extends Page implements OnInit {

  connectionProviders: ConnectionProviderSummary[];
  databasePlatforms: DatabasePlatform[];
  wizard: { connectionProvider: ConnectionProviderSummary } | any =
    { newConnectionType: 'jdbc', strategy: "automatic" };

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

  wizardStep(event) {
    if(event.selectedIndex == 1) {
      if(this.wizard.connectionProvider) {
        const url = `${this.portofino.apiRoot}portofino-upstairs/database/connections/${this.wizard.connectionProvider.name}`;
        this.http.get<ConnectionProviderDetails>(url).subscribe(c => {
          this.wizard.schemas = c.schemas;
        });
      } else {
        const url = `${this.portofino.apiRoot}portofino-upstairs/database/connections`;
        const conn = new ConnectionProviderDetails();
        conn.databaseName = { value: this.wizard.databaseName };
        conn.jndiResource = { value: this.wizard.jndiResource };
        conn.driver = { value: this.wizard.driver.standardDriverClassName };
        conn.url = { value: this.wizard.connectionUrl };
        conn.username = { value: this.wizard.username };
        conn.password = { value: this.wizard.password };
        this.http.post<ConnectionProviderDetails>(url, conn).subscribe(c => {
          const summary = new ConnectionProviderSummary();
          summary.name = c.databaseName.value;
          summary.status = c.status.value;
          this.connectionProviders.push(summary);
          this.wizard.connectionProvider = summary;
          this.wizard.schemas = c.schemas;
          this.notificationService.info(this.translate.instant("Database created."));
        });
      }
    } else if(event.selectedIndex == 2) {
      const url = `${this.portofino.apiRoot}portofino-upstairs/database/connections/${this.wizard.connectionProvider.name}/schemas`;
      this.http.put<any[]>(url, this.wizard.schemas).subscribe(tables => {
        tables.forEach(t => { t.selected = t.root; });
        this.wizard.tables = tables;
      });
    }
  }

  generateApplication(wizard) {
    const url = `${this.portofino.apiRoot}portofino-upstairs/application`;
    this.http.post(url, wizard).subscribe((actions: { path: string, type: string, title: string, detail: boolean }[]) => {
      if(this.portofino.localApiPath) {
        this.createPages(actions).subscribe(
          () => {},
          error => this.notificationService.error("Error " + error), //TODO describe, I18n
          () => {
            this.notificationService.info(this.translate.instant("Pages created. Setting up authc/authz."));
            setTimeout(() => this.configSecurity(url, wizard), 1000);
          });
      } else {
        this.notificationService.info(this.translate.instant("Local API not available. Only the application backend has been created."));
        setTimeout(() => { this.router.navigateByUrl("/"); }, 5000);
      }
    });
  }

  protected configSecurity(url: string, wizard) {
    return this.http.post(`${url}/security`, wizard).subscribe(() => {
        this.notificationService.info(this.translate.instant("Application created. You'll be logged out shortly."));
        setTimeout(() => {
          this.authenticationService.logout();
          this.router.navigateByUrl("/");
        }, 5000);
      },
      () => this.notificationService.error(this.translate.get("Error creating Security.groovy")));
  }

  protected createPages(actions: { path: string; type: string; title: string; detail: boolean }[]) {
    return from(actions).pipe(mergeMap(a => {
      const segments = a.path.split("/").filter(s => s && (s != "_detail"));
      let confPath = "/" + segments.join("/");
      confPath = this.getConfigurationLocation(confPath);
      const page = new PageConfiguration();
      page.source = segments[segments.length - 1];
      page.type = a.type;
      page.title = a.title;
      return this.http.post(`${this.portofino.localApiPath}/${confPath}`, page, {
        params: {
          childrenProperty: a.detail ? "detailChildren" : "children",
          loginPath: this.portofino.loginPath
        }
      });
    }, 1)); //Note concurrent: 1. It is necessary for calls to be executed sequentially.
  }

  trackByColumnName(index, column) {
    return column.columnName;
  }

  trackByTableName(index, table) {
    return table.table.tableName;
  }

  tableName(table) {
    let prefix = "";
    //TODO multiple configured databases. Portofino 4 did not display this information.
    if(this.wizard.schemas && this.wizard.schemas.filter(s => s.selected).length > 1) {
      prefix += `${table.schema}.`;
    }
    return prefix + table.table.tableName;
  }
}
