import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { PageComponent } from './page.component';
import {Field, Form, FormComponent} from "./form";
import {FormGroup, ReactiveFormsModule} from "@angular/forms";
import {FIELD_FACTORY, FieldComponent} from "./fields/field.component";
import {NgxdModule} from "@ngxd/core";
import {Component, Injectable, ViewChild} from "@angular/core";
import {Property} from "./class-accessor";
import {TextFieldComponent} from "./fields/text-field.component";
import {QuillModule} from "ngx-quill";
import {MatCheckboxModule, MatFormFieldModule} from "@angular/material";
import {TranslateModule} from "@ngx-translate/core";
import {PortofinoFormsModule, PortofinoModule} from "./portofino.module";
import {PortofinoService} from "./portofino.service";
import {FieldFactory} from "./fields/field.factory";

@Component({
  template: `
    <portofino-form [form]="formDefinition" [formGroup]="formGroup">
      <portofino-text-field [property]="nameProperty"></portofino-text-field>
    </portofino-form>`
})
class Wrapper {
  @ViewChild(FormComponent)
  formComponent;
  formGroup = new FormGroup({});
  nameProperty = Property.create({ name: 'p1'});
  formDefinition = new Form([new Field(this.nameProperty), new Field(Property.create({ name: 'other' }))]);
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
      providers: [{provide: PortofinoService, useValue: null }, { provide: FIELD_FACTORY, useClass: DummyFactory }]
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
    expect(component.contentChildren.length).toBe(1);
  });
});
