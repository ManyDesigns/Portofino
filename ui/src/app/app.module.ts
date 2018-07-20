import {Component, NgModule} from '@angular/core';
import {RouterModule} from '@angular/router';
import {PortofinoModule, PortofinoAppComponent, PageComponent} from 'portofino';

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
    PortofinoModule
  ],
  bootstrap: [PortofinoAppComponent]
})
export class AppModule { }
