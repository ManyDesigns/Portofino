import { Injectable } from '@angular/core';

@Injectable()
export abstract class TokenStorageService {

  abstract get(key: string): any;
  abstract set(key: string, value);
  abstract remove(key: string);
}

@Injectable()
export class LocalTokenStorageService extends TokenStorageService {
  prefix = "portofino-";

  get(key): any {
    return localStorage.getItem(this.prefix + key);
  }

  set(key, value) {
    localStorage.setItem(this.prefix + key, value);
  }

  remove(key) {
    localStorage.removeItem(this.prefix + key);
  }
}

@Injectable()
export class SessionTokenStorageService extends TokenStorageService {
  prefix = "portofino-";

  get(key): any {
    return sessionStorage.getItem(this.prefix + key);
  }

  set(key, value) {
    sessionStorage.setItem(this.prefix + key, value);
  }

  remove(key) {
    sessionStorage.removeItem(this.prefix + key);
  }
}

