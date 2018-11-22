import {Injectable, InjectionToken, Type} from "@angular/core";
import {BlobFieldComponent} from "./blob-field.component";
import {FieldComponent} from "./field.component";

@Injectable()
export class FieldFactory {

  factories: ((_: FieldComponent) => (Type<any> | null))[] = [
    f => {
      if(f.property.kind == 'blob') {
        return BlobFieldComponent;
      } else {
        return null;
      }
    }
  ];

  get(f: FieldComponent): Type<any> | null {
    let result = null;
    this.factories.forEach(factory => {
      const type = factory(f);
      if(type) {
        result = type;
      }
    });
    return result;
  }

}
