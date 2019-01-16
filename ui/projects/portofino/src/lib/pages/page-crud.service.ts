import {Component, Injectable} from "@angular/core";
import {PortofinoService} from "../portofino.service";
import {HttpClient} from "@angular/common/http";
import {MatDialog, MatDialogRef} from "@angular/material";
import {Field, Form} from "../form";
import {Property} from "../class-accessor";
import {FormGroup} from "@angular/forms";
import {PageFactoryComponent} from "../page.factory";
import {PageConfiguration, PageService} from "../page";

@Injectable()
export class PageCrudService {

  constructor(
    protected portofino: PortofinoService, protected pageService: PageService,
    protected http: HttpClient, protected dialog: MatDialog) {}

  showCreatePageDialog() {
    this.dialog.open(CreatePageComponent).afterClosed().subscribe(page => {
      if(!page) {
        return;
      }
      const path = this.pageService.page.path;
      this.http.post(`${this.portofino.localApiPath}/pages${path}?loginPath=${this.portofino.loginPath}`, page);
    });
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

  constructor(protected dialog: MatDialogRef<CreatePageComponent>) {}

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
    this.dialog.close(Object.assign(new PageConfiguration(), this.controls.value));
  }
}
