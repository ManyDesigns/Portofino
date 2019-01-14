import {Component, Injectable} from "@angular/core";
import {PortofinoService} from "../portofino.service";
import {HttpClient} from "@angular/common/http";
import {MatDialog} from "@angular/material";
import {Field, Form} from "../form";
import {Property} from "../class-accessor";
import {FormGroup} from "@angular/forms";

@Injectable()
export class PageCrudService {

  constructor(protected portofino: PortofinoService, protected http: HttpClient, protected dialog: MatDialog) {}

  createPage() {
    this.dialog.open(CreatePageComponent);
  }

}

@Component({
  template: `
    <portofino-form [form]="form" [controls]="controls"></portofino-form>
  TODO`
})
export class CreatePageComponent {
  readonly form = new Form([new Field(Property.create({ name: "segment", type: "string", label: "Segment" }).required())])
  readonly controls = new FormGroup({})
}
