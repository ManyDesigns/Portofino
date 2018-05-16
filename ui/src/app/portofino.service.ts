import { Injectable } from '@angular/core';
import {Property} from "./class-accessor";

@Injectable()
export class PortofinoService {

  apiPath: string;

  constructor() { }

  isString(property: Property) {
    return property.type == 'java.lang.String'
  }

  isNumber(property: Property) {
    return property.type == 'java.lang.Long'
  }

  isDate(property: Property) {
    return property.type == 'java.util.Date'
  }

}
