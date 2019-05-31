import {
  AfterViewInit, ChangeDetectorRef,
  Component,
  ComponentFactoryResolver, ContentChild, Directive,
  Inject,
  InjectionToken,
  Input,
  OnInit, ViewChild,
  ViewContainerRef
} from '@angular/core';
import {PortofinoService, SideNavPosition} from "./portofino.service";
import {AuthenticationService} from "./security/authentication.service";
import {NAVIGATION_COMPONENT, TemplatesComponent} from "./page";
import {NavigationDirective} from "./content.directive";
import {PageCrudService} from "./administration/page-crud.service";

export const TOOLBAR_COMPONENT = new InjectionToken('Toolbar Component');
export const FOOTER_COMPONENT = new InjectionToken('Footer Component');

@Directive({
  selector: '[portofino-toolbar]'
})
export class ToolbarDirective {
  constructor(public viewContainerRef: ViewContainerRef) { }
}

@Directive({
  selector: '[portofino-footer]'
})
export class FooterDirective {
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
  selector: 'portofino-default-footer',
  template: `<mat-toolbar fxLayoutAlign="center center"><footer style="font-size: 10px">{{'Powered by Portofino 5'|translate}}</footer></mat-toolbar>`
})
export class DefaultFooterComponent {}

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
  @ViewChild(ToolbarDirective, { static: false })
  toolbarHost: ToolbarDirective;
  @ViewChild(FooterDirective, { static: false })
  footerHost: FooterDirective;

  @ViewChild(NavigationDirective, { static: false })
  navigationHost: NavigationDirective;

  @ViewChild(TemplatesComponent, { static: true })
  builtinTemplates: TemplatesComponent;
  @ContentChild(TemplatesComponent, { static: false })
  extraTemplates: TemplatesComponent;

  constructor(public portofino: PortofinoService, public authenticationService: AuthenticationService,
              protected componentFactoryResolver: ComponentFactoryResolver,
              protected changeDetector: ChangeDetectorRef,
              @Inject(TOOLBAR_COMPONENT) protected toolbarComponent,
              @Inject(FOOTER_COMPONENT) protected footerComponent,
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
    for (let key in this.builtinTemplates.templates) {
      this.portofino.templates[key] = this.builtinTemplates.templates[key];
    }
    if(this.extraTemplates) {
      for (let key in this.extraTemplates.templates) {
        this.portofino.templates[key] = this.extraTemplates.templates[key];
      }
    }
    //Dynamically create the toolbar, footer and navigation components
    let toolbarFactory = this.componentFactoryResolver.resolveComponentFactory(this.toolbarComponent);
    let toolbar = this.toolbarHost.viewContainerRef.createComponent(toolbarFactory).instance as ToolbarComponent;
    toolbar.title = this.title;
    let navigationFactory = this.componentFactoryResolver.resolveComponentFactory(this.navigationComponent);
    this.navigationHost.viewContainerRef.createComponent(navigationFactory);
    let footerFactory = this.componentFactoryResolver.resolveComponentFactory(this.footerComponent);
    this.footerHost.viewContainerRef.createComponent(footerFactory);
    this.changeDetector.detectChanges();
  }
}
