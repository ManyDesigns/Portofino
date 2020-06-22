import {AuthenticationService, NotificationService, Page, PortofinoComponent, PortofinoService} from "portofino";
import {Component, OnInit} from "@angular/core";
import {HttpClient} from "@angular/common/http";
import {ActivatedRoute, Router} from "@angular/router";
import {TranslateService} from "@ngx-translate/core";
import {DomSanitizer, SafeResourceUrl} from "@angular/platform-browser";

@PortofinoComponent({ name: 'user-profile' })
@Component({
  selector: 'demo-tt-profile',
  template: `
    <portofino-page-layout [page]="this">
      <ng-template #content>
        <mat-card>
          <mat-card-header>
            <img mat-card-avatar [src]="photo" alt="User avatar">
            <mat-card-title>{{authenticationService.currentUser.displayName}}</mat-card-title>
          </mat-card-header>
        </mat-card>
      </ng-template>
    </portofino-page-layout>`
})
export class ProfileComponent extends Page implements OnInit {
  photo: SafeResourceUrl;

  constructor(
    portofino: PortofinoService, http: HttpClient, router: Router, route: ActivatedRoute,
    authenticationService: AuthenticationService, notificationService: NotificationService,
    translate: TranslateService, private sanitizer: DomSanitizer) {
    super(portofino, http, router, route, authenticationService, notificationService, translate);
    this.photo = sanitizer.bypassSecurityTrustResourceUrl(`data:image/png;base64, iVBORw0KGgoAAAANSUhEUgAAAAUA
    AAAFCAYAAACNbyblAAAAHElEQVQI12P4//8/w38GIAXDIBKE0DHxgljNBAAO
        9TXL0Y4OHwAAAABJRU5ErkJggg=="`);
  }

  ngOnInit(): void {
    this.http.get<any>(this.portofino.apiRoot + "profile/view").subscribe(u => {
      console.log(u);
    });
    this.http.get(this.portofino.apiRoot + 'profile/photo', { responseType: "blob" }).subscribe(data => {
      console.log(data);
      let reader = new FileReader();
      reader.onload = (e: any) => {
        this.photo = this.sanitizer.bypassSecurityTrustResourceUrl(e.target.result);
      }
      reader.readAsDataURL(data);
    });
  }

}
