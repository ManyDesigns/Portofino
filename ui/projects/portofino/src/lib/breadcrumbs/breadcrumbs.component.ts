import {PortofinoService} from "../portofino.service";
import {ActivatedRoute, Router} from "@angular/router";
import {Component, OnInit} from "@angular/core";
import {AuthenticationService} from "../security/authentication.service";
import {PortofinoComponent} from "../portofino-app.component";
import {PageService} from "../page";

export interface BreadCrumb {
  name: string;
  path: string;
}

@Component({
  selector: 'breadcrumbs',
  templateUrl: 'breadcrumbs.component.html',
  styleUrls: ['./breadcrumbs.component.css']
})
@PortofinoComponent({name: 'breadcrumbs'})
export class BreadcrumbsComponent implements OnInit {

  breadcrumbList: Array<any> = [];

  constructor(
    public portofino: PortofinoService, protected router: Router, private activatedRoute: ActivatedRoute,
    public authenticationService: AuthenticationService, public pageService: PageService) {
  }

  ngOnInit() {
    this.listenRouting();
  }

  listenRouting() {
    let routerUrl: string, routerList: Array<any>, target: any;
    this.router.events.subscribe((router: any) => {
      routerUrl = router.urlAfterRedirects;
      if (routerUrl && typeof routerUrl === 'string') {
        //target = this.pageService.page;
        this.breadcrumbList.length = 0;
        routerList = routerUrl.slice(1).split('/');
        routerList.forEach((router, index) => {
          router = router.split('?')[0];
          this.breadcrumbList.push({
            name: router,
            path: (index === 0) ? router : `${this.breadcrumbList[index-1].path}/${router}`
          });
        });
      }
    });
  }

  getTitle(item:BreadCrumb){
    let currentPage =  this.pageService.page;
      while ( currentPage!= undefined  && currentPage!=null ) {
        if( currentPage.baseUrl==('/'+item.path) )
          return currentPage.configuration.title;
        currentPage=currentPage.parent;
      }
    return item.name;
  }

}
