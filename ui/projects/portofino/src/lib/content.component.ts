import {
  AfterViewInit,
  Component,
  ComponentFactoryResolver, ComponentRef, Injector, OnDestroy,
  OnInit, ViewChild
} from '@angular/core';
import {ActivatedRoute, Router, UrlSegment} from "@angular/router";
import {HttpClient} from "@angular/common/http";
import {MainPageDirective} from "./content.directive";
import {Subscription} from "rxjs";
import {PortofinoService} from "./portofino.service";
import {AuthenticationService} from "./security/authentication.service";
import {Page, PageService} from "./page";
import {PageFactoryComponent} from "./page.factory";
import {NotificationService} from "./notifications/notification.service";
import {TranslateService} from "@ngx-translate/core";

@Component({
  selector: 'portofino-content',
  templateUrl: './content.component.html',
  styleUrls: ['./content.component.css']
})
export class ContentComponent implements AfterViewInit, OnInit, OnDestroy {
  @ViewChild(MainPageDirective, { static: false })
  contentHost: MainPageDirective;
  protected pageFactory: PageFactoryComponent;

  protected readonly subscriptions: Subscription[] = [];

  constructor(public pageService: PageService,
              protected route: ActivatedRoute, protected http: HttpClient, protected router: Router,
              protected componentFactoryResolver: ComponentFactoryResolver, injector: Injector,
              public portofino: PortofinoService, protected authenticationService: AuthenticationService,
              protected notificationService: NotificationService, protected translate: TranslateService) {
    this.pageFactory = new PageFactoryComponent(
      portofino, http, router, route, authenticationService, notificationService, translate, componentFactoryResolver,
      injector, null);
  }

  ngOnInit() {
    this.subscriptions.push(this.authenticationService.logins.subscribe(() => this.reloadPage()));
    this.subscriptions.push(this.authenticationService.logouts.subscribe(() => this.router.navigateByUrl("/")));
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
        this.displayPage(componentRef);
      },
      error => this.pageService.notifyError(error));
  }

  protected displayPage(componentRef: ComponentRef<Page>) {
    const page = componentRef.instance;
    page.initialize();
    let viewContainerRef = this.contentHost.viewContainerRef;
    viewContainerRef.clear(); //Remove main component;
    viewContainerRef.insert(componentRef.hostView);
    this.pageService.notifyPageLoaded(page);
    page.children.forEach(child => {
      page.checkAccessibility(child);
    });
    const parent = page.parent;
    if (parent) {
      parent.children.forEach(child => {
        if (`${parent.path}/${child.path}` != page.path) {
          parent.checkAccessibility(child);
        }
      });
    }
  }

}
