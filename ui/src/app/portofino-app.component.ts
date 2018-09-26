import {
  Component,
  ComponentFactoryResolver,
  Directive,
  Inject,
  InjectionToken,
  Input,
  OnInit,
  ViewChild,
  ViewContainerRef
} from '@angular/core';
import {PortofinoService} from "./portofino.service";
import {AuthenticationService} from "./security/authentication.service";
import {NAVIGATION_COMPONENT, NavigationComponent, Page} from "./page.component";
import {NavigationDirective} from "./content.directive";

export const TOOLBAR_COMPONENT = new InjectionToken('Toolbar Component');

@Directive({
  selector: '[portofino-toolbar]'
})
export class ToolbarDirective {
  constructor(public viewContainerRef: ViewContainerRef) { }
}

export interface ToolbarComponent {
  authenticationService: AuthenticationService;
}

@Component({
  selector: 'portofino-default-toolbar',
  templateUrl: './toolbar.component.html',
  styleUrls: ['./toolbar.component.css']
})
export class DefaultToolbarComponent implements ToolbarComponent {
  constructor(public authenticationService: AuthenticationService) {}
}

@Component({
  selector: 'portofino-app',
  templateUrl: './portofino-app.component.html',
  styleUrls: ['./portofino-app.component.css']
})
export class PortofinoAppComponent implements OnInit {

  static components: any = {};

  title = 'Portofino UI';
  @Input()
  apiRoot: string = 'http://localhost:8080/demo-tt/api/';
  @ViewChild(ToolbarDirective)
  toolbarHost: ToolbarDirective;

  constructor(public portofino: PortofinoService, public authenticationService: AuthenticationService,
              protected componentFactoryResolver: ComponentFactoryResolver,
              @Inject(TOOLBAR_COMPONENT) protected toolbarComponent) {}

  ngOnInit(): void {
    if(this.apiRoot) {
      this.portofino.defaultApiRoot = this.apiRoot;
      this.portofino.localApiPath = null;
    }
    this.portofino.init();
    //Dynamically create the toolbar component
    let componentFactory = this.componentFactoryResolver.resolveComponentFactory(this.toolbarComponent);
    this.toolbarHost.viewContainerRef.createComponent(componentFactory);
  }
}

export function PortofinoComponent(info: { name: string }) {
  return function(target) {
    PortofinoAppComponent.components[info.name] = target;
  };
}
