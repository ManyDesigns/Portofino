import { Injectable } from '@angular/core';
import {
  HttpClient,
  HttpErrorResponse, HttpEvent, HttpHandler, HttpInterceptor, HttpParams,
  HttpRequest
} from "@angular/common/http";
import {Observable} from "rxjs/Observable";
import "rxjs/add/operator/catch";
import "rxjs/add/operator/retryWhen";
import "rxjs/add/operator/concat";
import "rxjs/add/operator/do";
import "rxjs/add/operator/skip";
import "rxjs/add/operator/skipWhile";
import 'rxjs/add/observable/throw';
import "rxjs/add/observable/of";
import "rxjs/add/operator/takeWhile";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {LoginComponent} from "./login/login.component";
import "rxjs/add/observable/fromPromise";
import "rxjs/add/operator/mergeMap";
import {identity} from "rxjs/util/identity";

@Injectable()
export class AuthenticationService implements HttpInterceptor {

  constructor(private http: HttpClient, private modal: NgbModal) { }

  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    req = this.withAuthenticationHeader(req);
    let observable = next.handle(req);
    let http = this.http;
    return observable.catch((error, caught) => {
      if (error.status === 401) {
        localStorage.removeItem('jwt');
        let promise = this.modal.open(LoginComponent).result;
        let askForCredentials = Observable.fromPromise(promise);
        return askForCredentials.do(result => {
          if(result['jwt']) {
            localStorage.setItem('jwt', result['jwt']);
          }
        }).concat(http.request(this.withAuthenticationHeader(req)));
      }
      return Observable.throw(error);
    });
  }

  protected withAuthenticationHeader(req: HttpRequest<any>) {
    if(!localStorage.getItem('jwt')) {
      return req;
    }
    req = req.clone({
      setHeaders: {
        Authorization: `Bearer ${localStorage.getItem('jwt')}`
      }
    });
    return req;
  }
}
