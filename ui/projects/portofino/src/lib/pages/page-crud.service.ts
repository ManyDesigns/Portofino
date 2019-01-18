import {Component, Injectable} from "@angular/core";
import {PortofinoService} from "../portofino.service";
import {HttpClient} from "@angular/common/http";
import {MatDialog, MatDialogRef} from "@angular/material";
import {Field, Form} from "../form";
import {Property} from "../class-accessor";
import {FormGroup} from "@angular/forms";
import {PageFactoryComponent} from "../page.factory";
import {PageConfiguration, PageService} from "../page";
import {throwError} from "rxjs";

@Injectable()
export class PageCrudService {

  constructor(
    protected portofino: PortofinoService, protected pageService: PageService,
    protected http: HttpClient, protected dialog: MatDialog) {}

  showCreatePageDialog() {
    this.dialog.open(CreatePageComponent);
  }

  savePage(page: PageConfiguration) {
    //TODO child/sibling/top
    let parentPage = this.pageService.page;
    const path = parentPage.getConfigurationLocation(`${parentPage.path}/${page.source}`);
    const form = new FormData();
    form.append('parentActionPath', parentPage.computeSourceUrl());
    form.append('childrenProperty', parentPage.childrenProperty);
    form.append('actionDefinition', JSON.stringify({
      segment: page.source,
      actionClassName: PageFactoryComponent.components[page.type].defaultActionClass
    }));
    form.append('pageConfiguration', JSON.stringify(page));
    console.log("a", PageFactoryComponent.components, page.type);
    return this.http.post(`${this.portofino.localApiPath}/${path}?loginPath=${this.portofino.loginPath}`, form);
  }

  get available() {
    return this.portofino.localApiAvailable && !!this.pageService.page
  }

}

@Component({
  template: `
    <h4 mat-dialog-title>{{ 'Add new page' | translate }}</h4>
    <mat-dialog-content>
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
    new Field(Property.create({ name: "title", type: "string", label: "Title" }).required()),
    new Field(Property.create({ name: "icon", type: "string", label: "Icon" }))
  ]);
  readonly controls = new FormGroup({});

  constructor(protected dialog: MatDialogRef<CreatePageComponent>, protected pageCrud: PageCrudService) {}

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
    this.pageCrud.savePage(page).subscribe(() => this.dialog.close()); //TODO handle error
  }
}
