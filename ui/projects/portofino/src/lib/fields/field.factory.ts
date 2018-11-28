import {Component, Injectable, Input, Type} from "@angular/core";
import {BlobFieldComponent} from "./blob-field.component";
import {FieldComponent} from "./field.component";
import {DateTimeFieldComponent} from "./date-time-field.component";
import {BooleanFieldComponent} from "./boolean-field.component";
import {NumberFieldComponent} from "./number-field.component";
import {TextFieldComponent} from "./text-field.component";
import {SelectFieldComponent} from "./select-field.component";

@Injectable()
export class FieldFactory {

  factories: ((_: FieldComponent) => (Type<any> | null))[] = [
    f => f.property.selectionProvider ? SelectFieldComponent : null,
    byKind('string', TextFieldComponent),
    byKind('number', NumberFieldComponent),
    byKind('date', DateTimeFieldComponent),
    byKind('boolean', BooleanFieldComponent),
    byKind('blob', BlobFieldComponent)
  ];

  get(f: FieldComponent): Type<any> | null {
    for(let i in this.factories) {
      const type = this.factories[i](f);
      if(type) {
        return type;
      }
    }
  }
}

@Component({
  selector: 'portofino-field',
  templateUrl: './field.component.html'
})
export class FieldFactoryComponent extends FieldComponent {

  @Input()
  type: Type<any>;
  @Input()
  context = {};
  field: FieldComponent;

  constructor(protected factory: FieldFactory) {
    super(null);
  }

  get fieldComponentType() {
    return this.type ? this.type : this.factory ? this.factory.get(this) : null;
  }
}

export function byKind(kind: string, type: Type<any>) {
  return f => {
    if(f.property.kind == kind) {
      return type;
    } else {
      return null;
    }
  }
}
