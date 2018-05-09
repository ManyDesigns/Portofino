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
import {LoginComponent, UserDeclinedLogin} from "./login/login.component";
import "rxjs/add/observable/fromPromise";
import "rxjs/add/operator/mergeMap";

@Injectable()
export class AuthenticationService implements HttpInterceptor {

  loginModal;

  constructor(private http: HttpClient, private modal: NgbModal) { }

  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    if(this.loginModal) {
      return next.handle(req);
    }
    req = this.withAuthenticationHeader(req);
    let observable = next.handle(req);
    let http = this.http;
    return observable.catch((error) => {
      if (error.status === 401) {
        localStorage.removeItem('jwt');
        return this.askForCredentials().concat(http.request(this.withAuthenticationHeader(req)));
      }
      return Observable.throw(error);
    });
  }

  protected askForCredentials() {
    let self = this;
    function prompt(): Observable<any> {
      return Observable.create(function (observer) {
        if(!self.loginModal) {
          self.loginModal = self.modal.open(LoginComponent);
        }
        let promise = self.loginModal.result;
        promise.then(function (result) {
          self.loginModal = null;
          observer.next(result);
          observer.complete();
        }).catch(function (error) {
          self.loginModal = null;
          observer.error(error);
        });
      });
    }

    return prompt().catch((error, _) => {
      if(error instanceof UserDeclinedLogin) {
        throw error;
      } else {
        return prompt();
      }
    }).do(result => {
      if (result['jwt']) {
        localStorage.setItem('jwt', result['jwt']);
      }
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
