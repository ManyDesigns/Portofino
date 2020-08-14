import {Injectable} from "@angular/core";

export abstract class WebStorageService {
  abstract get(key: string);
  abstract set(key: string, value);
  abstract remove(key: string);
}

@Injectable()
export class LocalStorageService extends WebStorageService {
  get(key: string) {
    const item = localStorage.getItem(key);
    if(item) {
      return JSON.parse(item);
    } else {
      return item;
    }
  }

  set(key: string, value) {
    localStorage.setItem(key, JSON.stringify(value));
  }

  remove(key: string) {
    localStorage.removeItem(key);
  }

}
