import {ChangeDetectorRef, Component, EventEmitter, Input, NgZone, OnInit, Output} from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {PortofinoService} from "../../portofino.service";
import {isUpdatable, Property} from "../../class-accessor";
import {BaseDetailComponent} from "../common.component";
import {Operation} from "../../page.component";

@Component({
  selector: 'portofino-crud-detail',
  templateUrl: './detail.component.html',
  styleUrls: ['./detail.component.css']
})
export class DetailComponent extends BaseDetailComponent implements OnInit {

  @Input()
  id: string;
  editEnabled: boolean;
  deleteEnabled: boolean;

  editMode = false;
  @Output()
  editModeChanges = new EventEmitter();

  operationsPath = '/:operations';

  constructor(
    protected http: HttpClient, protected portofino: PortofinoService, protected changeDetector: ChangeDetectorRef) {
    super(http, portofino);
  }

  protected isEditable(property: Property): boolean {
    return isUpdatable(property);
  }

  protected isEditEnabled(): boolean {
    return this.editMode;
  }

  ngOnInit() {
    this.initClassAccessor();
    const objectUrl = `${this.sourceUrl}/${this.id}`;
    this.http.get(objectUrl, {params: {forEdit: "true"}}).subscribe(o => this.setupForm(o));
    this.http.get<Operation[]>(objectUrl + this.operationsPath).subscribe(ops => {
      this.editEnabled = ops.some(op => op.signature == "PUT" && op.available);
      this.deleteEnabled = ops.some(op => op.signature == "DELETE" && op.available);
    });
  }

  edit() {
    this.editMode = true;
    this.editModeChanges.emit(true);
    this.setupForm(this.object);
  }

  delete() {
    const objectUrl = `${this.sourceUrl}/${this.id}`;
    this.http.delete(objectUrl).subscribe(() => this.close.emit(this.object));
  }

  cancel() {
    if(this.editMode) {
      this.editMode = false;
      this.editModeChanges.emit(false);
      this.setupForm(this.object);
    } else {
      this.close.emit();
    }
  }

  save() {
    if(this.form.invalid) {
      this.triggerValidationForAllFields(this.form);
      return;
    }
    const objectUrl = `${this.sourceUrl}/${this.id}`;
    let object = this.getObjectToSave();
    this.http.put(objectUrl, object).subscribe(
      () => this.close.emit(object),
      (error) => {
        if(error.status == 500 && error.error) {
          for(let p in error.error) {
            let property = error.error[p];
            if(property.errors) {
              let control = this.form.controls[p];
              control.markAsTouched({ onlySelf: true });
              control.setErrors({ 'server-side': property.errors }, { emitEvent: false });
            }
          }
          this.changeDetector.detectChanges();
        }
      });
  }

}
