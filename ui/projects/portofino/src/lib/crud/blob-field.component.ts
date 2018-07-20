import {Component, Input} from '@angular/core';
import {FieldComponent} from "./field.component";

@Component({
  selector: 'portofino-crud-blob-field',
  templateUrl: './blob-field.component.html',
  styleUrls: ['./blob-field.component.css']
})
export class BlobFieldComponent extends FieldComponent {

  @Input()
  field: FieldComponent;

  ngOnInit(): void {
    Object.keys(this.field).forEach(k => this[k] = this.field[k]);
    super.ngOnInit();
  }

  deleteBlob() {
    this.control.reset(null);
  }

  get enabled() {
    return this.field.enabled;
  }

  set enabled(enabled) {
    this.field.enabled = enabled;
  }
}
