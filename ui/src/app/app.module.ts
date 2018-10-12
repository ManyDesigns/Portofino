import {Component, NgModule} from '@angular/core';
import {RouterModule} from '@angular/router';
import {PortofinoModule} from "./portofino.module";
import {PortofinoAppComponent} from "./portofino-app.component";
import {PageComponent} from "./page.component";
import {TranslateModule} from "@ngx-translate/core";

@Component({
  selector: 'portofino-hello',
  template: `<p>Welcome to Portofino 5!</p>`
})
export class HelloPortofino {}

@NgModule({
  declarations: [HelloPortofino],
  imports: [
    RouterModule.forRoot([
      { path: "start", component: HelloPortofino }, //TODO custom routes file
      { path: "**", component: PageComponent}], { onSameUrlNavigation: "reload", enableTracing: false }),
    PortofinoModule,
    TranslateModule.forRoot()
  ],
  bootstrap: [PortofinoAppComponent]
})
export class AppModule { }
