import {ChangeDetectorRef, Component, EventEmitter, Input, NgZone, OnInit, Output} from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {PortofinoService} from "../../portofino.service";
import {isUpdatable, Property} from "../../class-accessor";
import {BaseDetailComponent} from "../common.component";
import {Operation} from "../../page.component";
import {Observable} from "rxjs";
import {MatSnackBar} from "@angular/material";

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
    protected http: HttpClient, protected portofino: PortofinoService,
    protected changeDetector: ChangeDetectorRef, protected snackBar: MatSnackBar) {
    super(http, portofino, changeDetector, snackBar);
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
    if(!this.editMode) {
      throw "Not in edit mode!";
    }
    super.save();
  }

  protected doSave(object) {
    const objectUrl = `${this.sourceUrl}/${this.id}`;
    return this.http.put(objectUrl, object);
  }

}
