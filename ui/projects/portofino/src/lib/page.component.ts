import {Component, ComponentFactoryResolver, OnDestroy, OnInit, ViewChild} from '@angular/core';
import {ActivatedRoute, UrlSegment} from "@angular/router";
import {PortofinoAppComponent} from "./portofino-app.component";
import {HttpClient} from "@angular/common/http";
import {EmbeddedContentDirective, MainContentDirective} from "./content.directive";
import {Observable, Subscription} from "rxjs/index";
import {map} from "rxjs/operators";
import {ThemePalette} from "@angular/material/core/typings/common-behaviors/color";

@Component({
  selector: 'portofino-page',
  templateUrl: './page.component.html',
  styleUrls: ['./page.component.css']
})
export class PageComponent implements OnInit, OnDestroy {

  @ViewChild(MainContentDirective)
  contentHost: MainContentDirective;
  @ViewChild(EmbeddedContentDirective)
  embeddedContentHost: EmbeddedContentDirective;

  page: Page = null;
  error;

  protected subscription: Subscription;

  constructor(protected route: ActivatedRoute, protected http: HttpClient,
              protected componentFactoryResolver: ComponentFactoryResolver) { }

  ngOnInit() {
    this.subscription = this.route.url.subscribe(segment => {
      this.page = null;
      this.error = null;
      this.loadPageInPath("", null, segment, 0, false);
    });
  }

  ngOnDestroy() {
    if(this.subscription) {
      this.subscription.unsubscribe();
    }
  }

  protected loadPageInPath(path: string, parent: Page, segments: UrlSegment[], index: number, embed: boolean) {
    this.loadPage(path, embed).subscribe(
      (page: Page) => {
        page.parent = parent;
        page.baseUrl = '/' + segments.slice(0, index).join('/');
        page.url = page.baseUrl;
        for(let i = index; i < segments.length; i++) {
          let s = segments[i];
          if (page.consumePathSegment(s.path)) {
            path += `/${s.path}`;
            let child = page.getChild(s.path);
            if(child) {
              this.loadPageInPath(path, page, segments, i + 1, false);
              return;
            } else {
              this.error = `Nonexistent child of ${page.url}: ${s.path}`;
              return;
            }
          } else {
            page.url += `/${s.path}`;
          }
        }
        //If we arrive here, there are no more children in the URL to process
        if(!embed) {
          this.page = page;
          if(page.allowEmbeddedComponents) {
            page.children.forEach(child => {
              if(child.embedded) {
                let newSegments = segments.slice(0, segments.length);
                newSegments.push(new UrlSegment(child.path, {}));
                this.loadPageInPath(path + `/${child.path}`, page, newSegments, newSegments.length, true);
              }
            })
          }
        }
      },
      error => this.handleErrorInLoadingPage(path, error));
  }

  private handleErrorInLoadingPage(path, error) {
    console.log("Error in loading page " + path, error);
    this.error = error;
  }

  protected loadPage(path: string, embed: boolean): Observable<Page> {
    return this.http.get<PageConfiguration>(`pages${path}/config.json`).pipe(
      map(
        config => {
          const componentType = PortofinoAppComponent.components[config.type];
          if (!componentType) {
            this.error = Error("Unknown component type: " + config.type);
            return;
          }

          let componentFactory = this.componentFactoryResolver.resolveComponentFactory(componentType);

          let viewContainerRef;
          if(!embed) {
            viewContainerRef = this.contentHost.viewContainerRef;
            viewContainerRef.clear(); //Remove main component
            if(this.embeddedContentHost) {
              this.embeddedContentHost.viewContainerRef.clear();  //Remove embedded components
            }
          } else {
            viewContainerRef = this.embeddedContentHost.viewContainerRef;
            //TODO insert some kind of separator?
          }

          let componentRef = viewContainerRef.createComponent(componentFactory);
          const component = <Page>componentRef.instance;
          component.configuration = config;
          component.path = path;
          const lastIndexOf = path.lastIndexOf('/');
          if(lastIndexOf >= 0) {
            component.segment = path.substring(lastIndexOf + 1);
          } else {
            component.segment = path;
          }
          return component;
        }));
  }
}

export class PageConfiguration {
  type: string;
  title: string;
  children: PageChild[];
}

export class PageChild {
  path: string;
  title: string;
  embedded: boolean;
}

export abstract class Page {

  configuration: PageConfiguration & any;
  path: string;
  baseUrl: string;
  url: string;
  segment: string;
  parent: Page;
  allowEmbeddedComponents: boolean = true;

  readonly operationsPath = '/:operations';
  readonly configurationPath = '/:configuration';

  consumePathSegment(fragment: string): boolean {
    return true;
  }

  get children(): PageChild[] {
    return this.configuration.children
  }

  getChild(segment: string) {
    return this.children.find(c => c.path == segment);
  }

  getButtons(list: string = 'default'): ButtonInfo[] | null {
    //TODO handle inheritance using prototype chain, either here or in @Button when registering
    const allButtons = this[BUTTONS];
    return allButtons ? allButtons[list] : null;
  }
}

export class ButtonInfo {
  list: string = 'default';
  class: Function;
  methodName: string;
  propertyDescriptor: PropertyDescriptor;
  color: ThemePalette;
  action: (self, event: any | undefined) => void;
  presentIf: (self) => boolean = () => true;
  enabledIf: (self) => boolean = () => true;
  icon: string;
  text: string;
}

export const BUTTONS = "__portofinoButtons__";

export function Button(info: ButtonInfo | any) {
  return function (target, methodName: string, descriptor: PropertyDescriptor) {
    info = Object.assign({}, new ButtonInfo(), info);
    info.class = target.constructor;
    info.methodName = methodName;
    info.propertyDescriptor = descriptor;
    info.action = (self, event) => {
      self[methodName].call(self, event);
    };
    if(!target.hasOwnProperty(BUTTONS)) {
      target[BUTTONS] = {};
    }
    if(!target[BUTTONS].hasOwnProperty(info.list)) {
      target[BUTTONS][info.list] = [];
    }
    target[BUTTONS][info.list].push(info);
  }
}

export class Operation {
  name: string;
  signature: string;
  parameters: string[];
  available: boolean;
}
