import {
  AfterViewInit, ChangeDetectorRef,
  Component,
  ComponentFactoryResolver, ContentChild, Directive,
  Inject,
  InjectionToken, Injector, Input, OnInit, Type, ViewChild,
  ViewContainerRef
} from '@angular/core';
import {PortofinoService} from "./portofino.service";
import {AuthenticationService} from "./security/authentication.service";
import {NAVIGATION_COMPONENT, TemplatesComponent} from "./page";
import {NavigationDirective} from "./content.directive";
import {PageCrudService} from "./administration/page-crud.service";
import {SidenavService} from "./sidenav.service";
import {NotificationsHolder} from "./notifications/notification.services";
import {BlobFieldComponent} from "./fields/blob-field.component";
import {BooleanFieldComponent} from "./fields/boolean-field.component";
import {DateTimeFieldComponent} from "./fields/date-time-field.component";
import {NumberFieldComponent} from "./fields/number-field.component";
import {SelectFieldComponent} from "./fields/select-field.component";
import {TextFieldComponent} from "./fields/text-field.component";
import {createCustomElement} from "@angular/elements";
import {FormComponent} from "./form";

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
  templateUrl: '../../assets/toolbar.component.html',
  styleUrls: ['../../assets/toolbar.component.scss']
})
export class DefaultToolbarComponent implements ToolbarComponent {
  title: string;
  constructor(
    public authenticationService: AuthenticationService, public portofino: PortofinoService,
    public pageCrudService: PageCrudService, public sidenav: SidenavService,
    public notifications: NotificationsHolder) {}
}

@Component({
  selector: 'portofino-default-footer',
  template: `<mat-toolbar fxLayoutAlign="center center">
    <footer style="font-size: 10px">
      {{'Powered by'|translate}} <a href="https://portofino.manydesigns.com" target="_blank">Portofino 5</a>
    </footer>
  </mat-toolbar>`
})
export class DefaultFooterComponent {}

@Component({
  selector: 'portofino-app',
  templateUrl: '../../assets/portofino-app.component.html',
  styleUrls: ['../../assets/portofino-app.component.scss']
})
export class PortofinoAppComponent implements OnInit, AfterViewInit {

  @Input('appTitle')
  title = 'Portofino';
  @Input()
  apiRoot: string;
  @Input()
  upstairsLink = this.portofino.upstairsLink;
  @Input()
  preInit: (self: PortofinoAppComponent) => void;
  @Input()
  postInit: (self: PortofinoAppComponent) => void;
  @ViewChild(ToolbarDirective)
  toolbarHost: ToolbarDirective;
  @ViewChild(FooterDirective)
  footerHost: FooterDirective;

  @ViewChild(NavigationDirective)
  navigationHost: NavigationDirective;

  @ViewChild(TemplatesComponent, { static: true })
  builtinTemplates: TemplatesComponent;
  @ContentChild(TemplatesComponent)
  extraTemplates: TemplatesComponent;

  constructor(public portofino: PortofinoService, public authenticationService: AuthenticationService,
              protected componentFactoryResolver: ComponentFactoryResolver,
              protected changeDetector: ChangeDetectorRef, public sidenav: SidenavService,
              protected injector: Injector,
              @Inject(TOOLBAR_COMPONENT) protected toolbarComponent,
              @Inject(FOOTER_COMPONENT) protected footerComponent,
              @Inject(NAVIGATION_COMPONENT) protected navigationComponent) {}

  ngOnInit(): void {
    if(this.preInit) {
      this.preInit(this);
    }
    if(this.apiRoot) {
      this.portofino.defaultApiRoot = this.apiRoot;
    }
    this.portofino.upstairsLink = this.upstairsLink;
    this.portofino.applicationName = this.title;
    this.portofino.init();
    if(this.postInit) {
      this.postInit(this);
    }
    this.defineCustomComponents();
  }

  protected defineCustomComponents() {
    this.defineCustomElement('portofino-form-component', FormComponent);
    //Fields
    this.defineCustomElement('portofino-blob-field-component', BlobFieldComponent);
    this.defineCustomElement('portofino-boolean-field-component', BooleanFieldComponent);
    this.defineCustomElement('portofino-date-time-field-component', DateTimeFieldComponent);
    this.defineCustomElement('portofino-number-field-component', NumberFieldComponent);
    this.defineCustomElement('portofino-select-field-component', SelectFieldComponent);
    this.defineCustomElement('portofino-text-field-component', TextFieldComponent);
  }

  protected defineCustomElement(name, type: Type<any>) {
    const element = createCustomElement(type, { injector: this.injector });
    return customElements.define(name, element);
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
