import {Component, NgModule} from '@angular/core';
import {PortofinoModule} from 'portofino';

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
    PortofinoModule.withRoutes([{ path: "start", component: HelloPortofino }])
  ],
  bootstrap: [AppComponent]
})
export class AppModule { }
