import {PortofinoService} from "../portofino.service";
import {Router} from "@angular/router";
import {Component, OnDestroy, OnInit} from "@angular/core";
import {AuthenticationService} from "../security/authentication.service";
import {Page, PageService} from "../page";
import {Subscription} from "rxjs";

export class Breadcrumb {
  title: string;
  url: string;
}

@Component({
  selector: 'breadcrumbs',
  templateUrl: '../../../assets/breadcrumbs/breadcrumbs.component.html',
  styleUrls: ['../../../assets/breadcrumbs/breadcrumbs.component.scss']
})
export class BreadcrumbsComponent implements OnInit, OnDestroy {

  breadcrumbList: Array<Breadcrumb> = [];
  protected subscriptions: Subscription[] = [];

  constructor(
    public portofino: PortofinoService, protected router: Router,
    public authenticationService: AuthenticationService, public pageService: PageService) {
  }

  ngOnInit() {
    this.subscriptions.push(this.pageService.pageLoaded.subscribe(page => {
      this.breadcrumbList = [];
      this.addBreadcrumb(page);
    }));
  }

  ngOnDestroy(): void {
    this.subscriptions.forEach(s => s.unsubscribe());
    this.subscriptions = [];
  }

  protected addBreadcrumb(page: Page) {
    if (page.parent) {
      this.addBreadcrumb(page.parent);
    }
    if(page.url != page.baseUrl) {
      this.breadcrumbList.push({title: page.title, url: page.baseUrl});
      this.breadcrumbList.push({title: page.url.substring(page.baseUrl.length + 1), url: page.url});
    } else {
      this.breadcrumbList.push({title: page.title, url: page.url});
    }
  }

}
