import { Injectable } from '@angular/core';
import {HttpEvent, HttpHandler, HttpInterceptor, HttpRequest} from "@angular/common/http";
import {Observable} from "rxjs/Observable";

@Injectable()
export class AuthenticationService implements HttpInterceptor {
  constructor() { }

  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    if(localStorage.getItem('jwt')) {
      req = req.clone({
        setHeaders: {
          Authorization: `Bearer ${localStorage.getItem('jwt')}`
        }
      });
    }

    return next.handle(req);
  }


}
