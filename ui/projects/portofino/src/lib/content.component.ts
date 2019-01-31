import {
  AfterViewInit,
  Component,
  ComponentFactoryResolver, ComponentRef, Injector, OnDestroy,
  OnInit, ViewChild
} from '@angular/core';
import {ActivatedRoute, Router, UrlSegment} from "@angular/router";
import {HttpClient} from "@angular/common/http";
import {MainPageDirective} from "./content.directive";
import {Observable, Subscription} from "rxjs";
import {mergeMap} from "rxjs/operators";
import {PortofinoService} from "./portofino.service";
import {AuthenticationService} from "./security/authentication.service";
import {Page, PageChild, PageService} from "./page";
import {PageFactoryComponent} from "./page.factory";

@Component({
  selector: 'portofino-content',
  templateUrl: './content.component.html',
  styleUrls: ['./content.component.css']
})
export class ContentComponent implements AfterViewInit, OnInit, OnDestroy {
  @ViewChild(MainPageDirective)
  contentHost: MainPageDirective;
  protected pageFactory: PageFactoryComponent;

  protected readonly subscriptions: Subscription[] = [];

  constructor(public pageService: PageService,
              protected route: ActivatedRoute, protected http: HttpClient, protected router: Router,
              protected componentFactoryResolver: ComponentFactoryResolver, injector: Injector,
              public portofino: PortofinoService, protected authenticationService: AuthenticationService) {
    this.pageFactory = new PageFactoryComponent(
      portofino, http, router, route, authenticationService, componentFactoryResolver, injector, null);
  }

  ngOnInit() {
    const reload = () => this.reloadPage();
    this.subscriptions.push(this.authenticationService.logins.subscribe(reload));
    this.subscriptions.push(this.authenticationService.logouts.subscribe(reload));
  }

  ngAfterViewInit() {
    this.subscriptions.push(this.route.url.subscribe(segments => {
      this.pageService.reset();
      this.loadAndDisplayPage(segments);
    }));
  }

  ngOnDestroy() {
    this.subscriptions.forEach(subscription => subscription.unsubscribe());
  }

  protected reloadPage() {
    if (this.router.url && this.router.url != "/") {
      this.router.navigateByUrl(this.router.url);
    } else {
      window.location.reload(); //TODO
    }
  }

  protected loadAndDisplayPage(segments: UrlSegment[]) {
    this.pageFactory.load(segments).subscribe(
      componentRef => {
        const page = componentRef.instance;
        page.initialize();
        let viewContainerRef = this.contentHost.viewContainerRef;
        viewContainerRef.clear(); //Remove main component;
        viewContainerRef.insert(componentRef.hostView);
        this.pageService.notifyPage(page);
        page.children.forEach(child => {
          this.checkAccessibility(page, child);
        });
        const parent = page.parent;
        if(parent) {
          parent.children.forEach(child => {
            if(`${parent.path}/${child.path}` != page.path) {
              this.checkAccessibility(parent, child);
            }
          });
        }
      },
      error => this.pageService.notifyError(error));
  }

  checkAccessibility(parent: Page, child: PageChild) {
    let dummy = new DummyPage(this.portofino, this.http, this.router, this.route, this.authenticationService);
    dummy.parent = parent;
    dummy.loadPageConfiguration(`${parent.path}/${child.path}`).pipe(mergeMap(config => {
      dummy.configuration = config;
      return dummy.accessPermitted;
    })).subscribe(flag => child.accessible = flag);
  }

}

class DummyPage extends Page {}
