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
import {TranslateService} from "@ngx-translate/core";
import {TRANSLATIONS_EN} from "./i18n/en";
import {TRANSLATIONS_IT} from "./i18n/it";
import {NAVIGATION_COMPONENT} from "./page";
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
  title: string;
}

@Component({
  selector: 'portofino-default-toolbar',
  templateUrl: './toolbar.component.html',
  styleUrls: ['./toolbar.component.css']
})
export class DefaultToolbarComponent implements ToolbarComponent {
  title: string;
  constructor(public authenticationService: AuthenticationService,public portofino: PortofinoService) {}
}

@Component({
  selector: 'portofino-app',
  templateUrl: './portofino-app.component.html',
  styleUrls: ['./portofino-app.component.css']
})
export class PortofinoAppComponent implements OnInit, AfterViewInit {

  static components: any = {};

  @Input('appTitle')
  title = 'Portofino';
  @Input()
  apiRoot: string;
  @Input()
  sideNavPosition: SideNavPosition;
  @ViewChild(ToolbarDirective)
  toolbarHost: ToolbarDirective;

  @ViewChild(NavigationDirective)
  navigationHost: NavigationDirective;

  constructor(public portofino: PortofinoService, public authenticationService: AuthenticationService,
              protected componentFactoryResolver: ComponentFactoryResolver, translate: TranslateService,
              protected changeDetector: ChangeDetectorRef,
              @Inject(TOOLBAR_COMPONENT) protected toolbarComponent,
              @Inject(NAVIGATION_COMPONENT) protected navigationComponent) {
    this.setupTranslateService(translate);
  }

  protected setupTranslateService(translate: TranslateService) {
    translate.setDefaultLang('en');
    this.configureTranslations(translate);
    translate.use(translate.getBrowserLang());
  }

  protected configureTranslations(translate: TranslateService) {
    translate.setTranslation('en', TRANSLATIONS_EN, true);
    translate.setTranslation('it', TRANSLATIONS_IT, true);
  }

  ngOnInit(): void {
    if(this.sideNavPosition) {
      this.portofino.sideNavPosition = this.sideNavPosition;
    }
    if(this.apiRoot) {
      this.portofino.defaultApiRoot = this.apiRoot;
      this.portofino.localApiPath = null;
    }
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

export function PortofinoComponent(info: { name: string }) {
  return function(target) {
    PortofinoAppComponent.components[info.name] = target;
  };
}
