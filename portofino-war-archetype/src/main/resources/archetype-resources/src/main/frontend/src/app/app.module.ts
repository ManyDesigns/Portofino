import {Component, NgModule} from '@angular/core';
import {PortofinoModule, PageComponent} from "portofino";
import {RouterModule} from "@angular/router";

@Component({
  selector: 'portofino-hello',
  template: `<p>Welcome to Portofino 5!</p>`
})
export class HelloPortofino {}

@Component({
  selector: 'app-root',
  template: `<portofino-app title="Demo-TT" apiRoot="http://localhost:8080/demo-tt/api/"></portofino-app>`
})
export class AppComponent {}

@NgModule({
  declarations: [AppComponent, HelloPortofino],
  imports: [
    RouterModule.forRoot(
      [{ path: "start", component: HelloPortofino },
        { path: "**", component: PageComponent}],
      { onSameUrlNavigation: "reload", enableTracing: false }),
    PortofinoModule
  ],
  bootstrap: [AppComponent]
})
export class AppModule { }
