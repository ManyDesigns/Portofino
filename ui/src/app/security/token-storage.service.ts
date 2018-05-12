import { Injectable } from '@angular/core';

@Injectable()
export abstract class TokenStorageService {

  abstract get(): any;
  abstract set(value);
  abstract remove();
}

@Injectable()
export class LocalTokenStorageService extends TokenStorageService {
  key: string = "portofino-jwt";

  get(): any {
    return localStorage.getItem(this.key);
  }

  set(value) {
    localStorage.setItem(this.key, value);
  }

  remove() {
    localStorage.removeItem(this.key);
  }
}

@Injectable()
export class SessionTokenStorageService extends TokenStorageService {
  key: string = "portofino-jwt";

  get(): any {
    return sessionStorage.getItem(this.key);
  }

  set(value) {
    sessionStorage.setItem(this.key, value);
  }

  remove() {
    sessionStorage.removeItem(this.key);
  }
}

@Injectable()
export class NoOpTokenStorageService extends TokenStorageService {
  get(): any {
    return null;
  }

  set(value) {}

  remove() {}
}
