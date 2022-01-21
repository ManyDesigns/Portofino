import {
  AuthenticationService, Button, ClassAccessor,
  DATE_TYPE,
  Form,
  NotificationService,
  Page,
  PortofinoComponent,
  PortofinoService
} from "portofino";
import {Component, OnInit} from "@angular/core";
import {HttpClient, HttpParams} from "@angular/common/http";
import {ActivatedRoute, Router} from "@angular/router";
import {TranslateService} from "@ngx-translate/core";
import {DomSanitizer, SafeResourceUrl} from "@angular/platform-browser";
import {FormGroup} from "@angular/forms";
import {Location} from "@angular/common";

@PortofinoComponent({ name: 'user-profile' })
@Component({
  selector: 'demo-tt-profile',
  template: `
    <portofino-page-layout [page]="this">
      <ng-template #content>
        <div gdAreas="content sidebar" gdColumns="auto 300px" gdGap="10px">
          <mat-card gdArea="content">
            <mat-card-header>
              <img mat-card-avatar [src]="photo" alt="User avatar">
              <mat-card-title>{{authenticationService.currentUser.displayName}}</mat-card-title>
            </mat-card-header>
            <mat-card-content>
              <portofino-form [controls]="form" [form]="formDefinition"
                              fxLayout="row wrap" fxLayoutGap="20px" fxLayoutAlign="default center"></portofino-form>
            </mat-card-content>
            <mat-card-actions *ngIf="editMode">
              <portofino-buttons [component]="this" list="edit"></portofino-buttons>
            </mat-card-actions>
          </mat-card>
          <mat-card gdArea="sidebar">
            <mat-card-title>{{'Actions' | translate}}</mat-card-title>
            <mat-card-content>
              <div fxLayout="column" fxLayoutGap="10px">
                <span><!-- Divider --></span>
                <a mat-raised-button color="accent" (click)="authenticationService.goToChangePassword()">
                  Change your password
                </a>
                <a mat-raised-button color="primary" (click)="edit()">Edit your data</a>
                <a mat-raised-button color="warn" (click)="photoForm()">Change your photo</a>
              </div>
            </mat-card-content>
          </mat-card>
        </div>
      </ng-template>
    </portofino-page-layout>`
})
export class ProfileComponent extends Page implements OnInit {
  photo: SafeResourceUrl;
  formDefinition = new Form();
  user: any;
  editMode: boolean;
  readonly form = new FormGroup({});

  constructor(
    portofino: PortofinoService, http: HttpClient, router: Router, route: ActivatedRoute,
    authenticationService: AuthenticationService, notificationService: NotificationService,
    translate: TranslateService, location: Location, private sanitizer: DomSanitizer) {
    super(portofino, http, router, route, authenticationService, notificationService, translate, location);
    this.photo = sanitizer.bypassSecurityTrustUrl(`data:image/png;base64, iVBORw0KGgoAAAANSUhEUgAAAAUA
    AAAFCAYAAACNbyblAAAAHElEQVQI12P4//8/w38GIAXDIBKE0DHxgljNBAAO
        9TXL0Y4OHwAAAABJRU5ErkJggg=="`);
  }

  ngOnInit(): void {
    this.http.get<any>(this.portofino.apiRoot + "profile").subscribe(u => {
      this.user = u;
      this.setupForm(this.user);
      this.formDefinition.editable = false;
    });
    this.http.get(this.portofino.apiRoot + 'profile/photo', { responseType: "blob" }).subscribe(data => {
      let reader = new FileReader();
      reader.onload = (e: any) => {
        this.photo = this.sanitizer.bypassSecurityTrustUrl(e.target.result);
      }
      reader.readAsDataURL(data);
    });
  }

  edit() {
    this.setupForm(this.user, [ "first_name", "last_name" ]);
    this.formDefinition.editable = true;
    this.editMode = true;
  }

  @Button({ list: 'edit', text: "Save", color: "primary" })
  save() {
    this.http.put(
      this.portofino.apiRoot + "profile",
      new HttpParams({ fromObject: this.form.getRawValue() })).subscribe(u => {
      this.user = u;
      this.cancel();
    }, () => {
      this.notificationService.error("Update failed");
    });
  }

  @Button({ list: 'edit', text: "Cancel" })
  @Button({ list: 'photo', text: "Cancel" })
  cancel() {
    this.setupForm(this.user);
    this.formDefinition.editable = false;
    this.editMode = false;
  }

  photoForm() {
    this.notificationService.error("Not yet implemented");
  }

  protected setupForm(user, properties?: string[]) {
    //@ts-ignore
    this.formDefinition = Form.fromClassAccessor(ClassAccessor.forObject(user, {
      properties: {
        "registration": { type: DATE_TYPE },
        "last_access": { type: DATE_TYPE },
        "validated": { type: DATE_TYPE },
      }
    }), { object: user, properties });
  }
}
