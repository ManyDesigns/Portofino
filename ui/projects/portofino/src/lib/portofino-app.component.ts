import {
  AfterViewInit, ChangeDetectorRef,
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
import {PortofinoService, SideNavPosition} from "./portofino.service";
import {AuthenticationService} from "./security/authentication.service";
import {NAVIGATION_COMPONENT} from "./page";
import {NavigationDirective} from "./content.directive";
import {PageCrudService} from "./administration/page-crud.service";

export const TOOLBAR_COMPONENT = new InjectionToken('Toolbar Component');

@Directive({
  selector: '[portofino-toolbar]'
})
export class ToolbarDirective {
  constructor(public viewContainerRef: ViewContainerRef) { }
}

export interface ToolbarComponent {
  authenticationService: AuthenticationService;
  title: string;
}

@Component({
  selector: 'portofino-default-toolbar',
  templateUrl: './toolbar.component.html',
  styleUrls: ['./toolbar.component.css']
})
export class DefaultToolbarComponent implements ToolbarComponent {
  title: string;
  constructor(
    public authenticationService: AuthenticationService, public portofino: PortofinoService,
    public pageCrudService: PageCrudService) {}
}

@Component({
  selector: 'portofino-app',
  templateUrl: './portofino-app.component.html',
  styleUrls: ['./portofino-app.component.css']
})
export class PortofinoAppComponent implements OnInit, AfterViewInit {

  @Input('appTitle')
  title = 'Portofino';
  @Input()
  apiRoot: string;
  @Input()
  sideNavPosition: SideNavPosition;
  @Input()
  upstairsLink = this.portofino.upstairsLink;
  @ViewChild(ToolbarDirective)
  toolbarHost: ToolbarDirective;

  @ViewChild(NavigationDirective)
  navigationHost: NavigationDirective;

  constructor(public portofino: PortofinoService, public authenticationService: AuthenticationService,
              protected componentFactoryResolver: ComponentFactoryResolver,
              protected changeDetector: ChangeDetectorRef,
              @Inject(TOOLBAR_COMPONENT) protected toolbarComponent,
              @Inject(NAVIGATION_COMPONENT) protected navigationComponent) {}

  ngOnInit(): void {
    if(this.sideNavPosition) {
      this.portofino.sideNavPosition = this.sideNavPosition;
    }
    if(this.apiRoot) {
      this.portofino.defaultApiRoot = this.apiRoot;
    }
    this.portofino.upstairsLink = this.upstairsLink;
    this.portofino.init();
  }

  ngAfterViewInit(): void {
    //Dynamically create the toolbar and navigation components
    let componentFactory = this.componentFactoryResolver.resolveComponentFactory(this.toolbarComponent);
    let toolbar = this.toolbarHost.viewContainerRef.createComponent(componentFactory).instance as ToolbarComponent;
    let navigationFactory = this.componentFactoryResolver.resolveComponentFactory(this.navigationComponent);
    this.navigationHost.viewContainerRef.createComponent(navigationFactory);
    toolbar.title = this.title;
    this.changeDetector.detectChanges();
  }
}
