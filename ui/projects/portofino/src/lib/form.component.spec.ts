import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import {Field, Form, FormComponent} from "./form";
import {FormGroup, ReactiveFormsModule} from "@angular/forms";
import {FieldComponent} from "./fields/field.component";
import {NgxdModule} from "@ngxd/core";
import {Component, Injectable, ViewChild} from "@angular/core";
import {Property} from "./class-accessor";
import {TextFieldComponent} from "./fields/text-field.component";
import {QuillModule} from "ngx-quill";
import { MatCheckboxModule } from "@angular/material/checkbox";
import { MatFormFieldModule } from "@angular/material/form-field";
import {TranslateModule} from "@ngx-translate/core";
import {PortofinoFormsModule} from "./portofino.module";
import {PortofinoService} from "./portofino.service";
import {NumberFieldComponent} from "./fields/number-field.component";
import {FieldFactory} from "./fields/field.factory";

@Component({
  template: `
    <portofino-form [form]="formDefinition" [formGroup]="formGroup"></portofino-form>`
})
class Wrapper {
  @ViewChild(FormComponent)
  formComponent;
  formGroup = new FormGroup({});
  formDefinition = new Form([
    new Field(Property.create({ name: 'p1'}), { value: 'v1' }),
    Object.assign(new Field(Property.create({ name: 'p2' }), { value: 'v2' }), { type: NumberFieldComponent })]);
}

@Injectable()
class DummyFactory {
  get(f: FieldComponent) {
    return TextFieldComponent;
  }
}

describe('FormComponent', () => {
  let component: FormComponent;
  let fixture: ComponentFixture<Wrapper>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [ ReactiveFormsModule, NgxdModule, QuillModule, MatCheckboxModule, MatFormFieldModule, TranslateModule, PortofinoFormsModule ],
      declarations: [ Wrapper ],
      providers: [{provide: PortofinoService, useValue: null }, { provide: FieldFactory, useClass: DummyFactory }]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(Wrapper);
    component = fixture.componentInstance.formComponent;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('', () => {
    expect(component.fields.length).toBe(2);
    expect(component.fields.toArray()[0].field instanceof TextFieldComponent).toBeTruthy();
    expect(component.fields.toArray()[1].field instanceof NumberFieldComponent).toBeTruthy();
  });
});
