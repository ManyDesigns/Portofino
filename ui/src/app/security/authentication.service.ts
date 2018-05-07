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

@Injectable()
export class AuthenticationService implements HttpInterceptor {
  constructor(private http: HttpClient) { }

  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    req = this.withAuthenticationHeader(req);
    let observable = next.handle(req);
    let http = this.http;
    return observable.catch((error, caught) => {
      if (error.status === 401) {
        localStorage.removeItem('jwt');
        let askForCredentials: Observable<any> = Observable.create(function(observer) {
          http.post(
            "http://localhost:8080/demo-tt/api/login",
            new HttpParams({fromObject: {"username": "alessiostalla@gmail.com", "password": "admin"}}),
            {headers: {'Content-Type': 'application/x-www-form-urlencoded'}}
          ).subscribe(result => {
            if(result['jwt']) {
              localStorage.setItem('jwt', result['jwt']);
            }
            observer.complete();
          }, error => observer.error(error));
        });
        return askForCredentials.concat(http.request(this.withAuthenticationHeader(req)));

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
