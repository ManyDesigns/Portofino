import {Component, ComponentFactoryResolver, Injectable, Injector} from "@angular/core";
import {PortofinoService} from "../portofino.service";
import {HttpClient} from "@angular/common/http";
import {MatDialog, MatDialogRef} from "@angular/material";
import {Field, Form} from "../form";
import {Property} from "../class-accessor";
import {FormGroup} from "@angular/forms";
import {PageFactoryComponent} from "../page.factory";
import {PageConfiguration, PageService} from "../page";
import {throwError} from "rxjs";
import {TranslateService} from "@ngx-translate/core";
import {mergeMap, tap} from "rxjs/operators";
import {Router, UrlSegment} from "@angular/router";
import {AuthenticationService} from "../security/authentication.service";

@Injectable()
export class PageCrudService {

  protected pageFactory: PageFactoryComponent;

  constructor(
    protected portofino: PortofinoService, protected pageService: PageService, protected http: HttpClient,
    protected dialog: MatDialog, protected router: Router, protected translate: TranslateService,
    authenticationService: AuthenticationService, componentFactoryResolver: ComponentFactoryResolver,
    injector: Injector) {
    this.pageFactory = new PageFactoryComponent(
      portofino, http, router, null, authenticationService, componentFactoryResolver, injector, null);
  }

  showCreatePageDialog() {
    this.dialog.open(CreatePageComponent);
  }

  confirmDeletePage() {
    this.dialog.open(DeletePageComponent);
  }

  showMovePageDialog() {
    this.dialog.open(MovePageComponent);
  }

  createPage(page: PageConfiguration & { position: {v: string, l: string} }) {
    let parentPage;
    const position = page.position.v;
    if(position == 'CHILD') {
      parentPage = this.pageService.page;
    } else if(position == 'SIBLING') {
      parentPage = this.pageService.page.parent;
    }  else if(position == 'TOP') {
      parentPage = this.pageService.page.root;
    } else {
      return throwError("Unsupported position: " + position)
    }
    delete page.position;
    const path = parentPage.getConfigurationLocation(`${parentPage.path}/${page.source}`);
    const reloadPageConfiguration = () => parentPage.loadConfiguration(); //Update the navigation
    return this.http.post(`${this.portofino.localApiPath}/${path}`, page, { params: {
        actionPath: `${parentPage.computeSourceUrl()}/${page.source}`,
        actionClass: PageFactoryComponent.components[page.type].defaultActionClass,
        childrenProperty: parentPage.childrenProperty,
        loginPath: this.portofino.loginPath
      }}).pipe(mergeMap(reloadPageConfiguration));
  }

  deletePage() {
    const page = this.pageService.page;
    const parentPage = page.parent;
    if(!parentPage) {
      return this.translate.get("You cannot delete the root page.").pipe(mergeMap(s => throwError(s)));
    }
    const path = page.getConfigurationLocation();
    const goUpOnePage = () => this.router.navigateByUrl(parentPage.url);
    return this.http.delete(`${this.portofino.localApiPath}/${path}`, { params: {
        actionPath: page.computeSourceUrl(),
        childrenProperty: parentPage.childrenProperty,
        loginPath: this.portofino.loginPath
      }}).pipe(tap(goUpOnePage));
  }

  movePage(moveInstruction: { destination: string, detail: boolean }) {
    const page = this.pageService.page;
    const parentPage = page.parent;
    if(!parentPage) {
      return this.translate.get("You cannot move the root page.").pipe(mergeMap(s => throwError(s)));
    }
    const path = page.getConfigurationLocation();
    const goUpOnePage = () => this.router.navigateByUrl(parentPage.url); //TODO could navigate to the destination instead, but needs to handle detail
    const sanitizedDestination = moveInstruction.destination; //TODO
    const destPath = sanitizedDestination.split("/");
    const segments = destPath.slice(0, destPath.length - 1).map(s => new UrlSegment(s, {}));
    return this.pageFactory.load(segments).pipe(mergeMap(newParent => {
      return this.http.post(`${this.portofino.localApiPath}/${sanitizedDestination}/config.json`, path, {
        headers: {
          "Content-Type": "application/vnd.com.manydesigns.portofino.page-move"
        },
        params: {
          sourceActionPath: `${page.computeSourceUrl()}`,
          detail: moveInstruction.detail + "",
          loginPath: this.portofino.loginPath,
          newActionParent: newParent.instance.computeSourceUrl()
        }}).pipe(mergeMap(goUpOnePage));
    }));
  }

  get available() {
    return this.portofino.localApiAvailable && !!this.pageService.page
  }

}

@Component({
  template: `
    <h4 mat-dialog-title>{{ 'Add new page' | translate }}</h4>
    <mat-dialog-content>
      <mat-error *ngIf="error">{{error|translate}}</mat-error>
      <form (submit)="save()">
        <portofino-form [form]="form" [controls]="controls"></portofino-form>
        <button type="submit" style="display:none">{{ 'Save' | translate }}</button>
      </form>
    </mat-dialog-content>
    <mat-dialog-actions>
      <button mat-button color="primary" (click)="save()" [disabled]="!controls.valid">
        <mat-icon>save</mat-icon>
        {{'Save'|translate}}
      </button>
      <button mat-button (click)="cancel()">
        <mat-icon>close</mat-icon>
        {{'Cancel'|translate}}
      </button>
    </mat-dialog-actions>`
})
export class CreatePageComponent {
  readonly form = new Form([
    new Field(Property.create({ name: "source", type: "string", label: "Segment" }).required()),
    new Field(Property.create({ name: "type", type: "string", label: "Type" }).required().withSelectionProvider({
      options: this.getPageTypes()
    })),
    new Field(Property.create({ name: "position", type: "string", label: "Position" }).required().withSelectionProvider({
      options: [
        {v: 'CHILD', l: this.translate.instant("As a child of the current page")},
        {v: 'SIBLING', l: this.translate.instant("As a sibling of the current page")},
        {v: 'TOP', l: this.translate.instant("At the top level")}]
    })),
    { html: '<br />' },
    new Field(Property.create({ name: "title", type: "string", label: "Title" }).required()),
    new Field(Property.create({ name: "icon", type: "string", label: "Icon" }))
  ]);
  readonly controls = new FormGroup({});
  error: any;

  constructor(protected dialog: MatDialogRef<CreatePageComponent>, protected pageCrud: PageCrudService,
              protected translate: TranslateService) {}

  protected getPageTypes() {
    const types = [];
    for (let k in PageFactoryComponent.components) {
      types.push({v: k, l: `page type: ${k}`})
    }
    return types;
  }

  cancel() {
    this.dialog.close();
  }

  save() {
    const page = Object.assign(new PageConfiguration(), this.controls.value);
    page.type = this.controls.value.type.v;
    this.pageCrud.createPage(page).subscribe(
      () => this.dialog.close(),
      error => {
        if(typeof error === 'string') {
          this.error = error;
        } else {
          this.error = "Error";
          console.error(error);
        }
      });
  }
}

@Component({
  template: `
    <h4 mat-dialog-title>{{ 'Move page' | translate }}</h4>
    <mat-dialog-content>
      <mat-error *ngIf="error">{{error|translate}}</mat-error>
      <form (submit)="move()">
        <portofino-form [form]="form" [controls]="controls"></portofino-form>
        <button type="submit" style="display:none">{{ 'Move' | translate }}</button>
      </form>
    </mat-dialog-content>
    <mat-dialog-actions>
      <button mat-button color="primary" (click)="move()" [disabled]="!controls.valid">
        <mat-icon>save</mat-icon>
        {{'Move'|translate}}
      </button>
      <button mat-button (click)="cancel()">
        <mat-icon>close</mat-icon>
        {{'Cancel'|translate}}
      </button>
    </mat-dialog-actions>`
})
export class MovePageComponent {
  readonly form = new Form([
    new Field(Property.create({ name: "destination", type: "string", label: "Destination" }).required()),
    new Field(Property.create({ name: "detail", type: "boolean", label: "Detail" }).required())
  ]);
  readonly controls = new FormGroup({});
  error: any;

  constructor(protected dialog: MatDialogRef<CreatePageComponent>, protected pageCrud: PageCrudService,
              protected translate: TranslateService) {}

  cancel() {
    this.dialog.close();
  }

  move() {
    const moveInstruction = this.controls.value;
    this.pageCrud.movePage(moveInstruction).subscribe(
      () => this.dialog.close(),
      error => {
        if(typeof error === 'string') {
          this.error = error;
        } else {
          this.error = "Error";
          console.error(error);
        }
      });
  }
}

@Component({
  template: `
    <h4 mat-dialog-title>{{ 'Confirm page deletion' | translate }}</h4>
    <mat-dialog-content>
      <mat-error *ngIf="error">{{error|translate}}</mat-error>
      <p>{{"Delete the current page and all its children?"|translate}}</p>
    </mat-dialog-content>
    <mat-dialog-actions>
      <button mat-button color="primary" (click)="delete()">
        <mat-icon>delete</mat-icon>
        {{'Delete'|translate}}
      </button>
      <button mat-button (click)="cancel()">
        <mat-icon>close</mat-icon>
        {{'Cancel'|translate}}
      </button>
    </mat-dialog-actions>`
})
export class DeletePageComponent {
  error: any;

  constructor(protected dialog: MatDialogRef<CreatePageComponent>, protected pageCrud: PageCrudService,
              protected translate: TranslateService) {}

  cancel() {
    this.dialog.close();
  }

  delete() {
    this.pageCrud.deletePage().subscribe(
      () => this.dialog.close(),
      error => {
        if(typeof error === 'string') {
          this.error = error;
        } else {
          this.error = "Error";
          console.error(error);
        }
      });
  }
}
