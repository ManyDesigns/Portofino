import {Component, NgModule} from '@angular/core';
import {RouterModule} from '@angular/router';
import {PortofinoModule, PortofinoAppComponent, PageComponent} from 'portofino';

@Component({
  selector: 'portofino-hello',
  template: `<p>Welcome to Portofino 5!</p>`
})
export class HelloPortofino {}

@Component({
  selector: 'my-app',
  template: `<portofino-app title="Demo-TT" apiRoot="http://localhost:8080/demo-tt/api/"></portofino-app>`
})
export class AppComponent {}

@NgModule({
  declarations: [AppComponent, HelloPortofino],
  imports: [
    RouterModule.forRoot([
      { path: "start", component: HelloPortofino }, //TODO custom routes file
      { path: "**", component: PageComponent}], { onSameUrlNavigation: "reload", enableTracing: false }),
    PortofinoModule
  ],
  bootstrap: [AppComponent]
})
export class AppModule { }
