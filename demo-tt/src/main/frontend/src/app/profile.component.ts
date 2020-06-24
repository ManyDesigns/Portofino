import {
  AuthenticationService, DATE_TYPE,
  Form, forObject,
  NotificationService,
  Page,
  PortofinoComponent,
  PortofinoService
} from "portofino";
import {Component, OnInit} from "@angular/core";
import {HttpClient} from "@angular/common/http";
import {ActivatedRoute, Router} from "@angular/router";
import {TranslateService} from "@ngx-translate/core";
import {DomSanitizer, SafeResourceUrl} from "@angular/platform-browser";
import {FormGroup} from "@angular/forms";

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
          <mat-card-content>
            <portofino-form [controls]="form" [form]="formDefinition"
                            fxLayout="row wrap" fxLayoutGap="20px" fxLayoutAlign="default center"></portofino-form>
          </mat-card-content>
        </mat-card>
      </ng-template>
    </portofino-page-layout>`
})
export class ProfileComponent extends Page implements OnInit {
  photo: SafeResourceUrl;
  formDefinition = new Form();
  readonly form = new FormGroup({});

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
      let form = Form.fromClassAccessor(forObject(u, {
        types: {
          "registration": DATE_TYPE,
          "last_access": DATE_TYPE,
          "validated": DATE_TYPE,
        }
      }), { object: u });
      form.editable = false;
      this.formDefinition = form;
    });
    this.http.get(this.portofino.apiRoot + 'profile/photo', { responseType: "blob" }).subscribe(data => {
      let reader = new FileReader();
      reader.onload = (e: any) => {
        this.photo = this.sanitizer.bypassSecurityTrustResourceUrl(e.target.result);
      }
      reader.readAsDataURL(data);
    });
  }

}
