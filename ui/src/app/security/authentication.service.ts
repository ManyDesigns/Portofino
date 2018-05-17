import { Injectable } from '@angular/core';
import {
  HttpClient,
  HttpEvent, HttpHandler, HttpInterceptor, HttpRequest
} from "@angular/common/http";
import {LoginComponent} from "./login/login.component";
import {TokenStorageService} from "./token-storage.service";
import {MatDialog, MatDialogRef} from "@angular/material";
import {concat, Observable, throwError} from "rxjs";
import { catchError, map, mergeMap } from "rxjs/operators";

@Injectable()
export class AuthenticationService implements HttpInterceptor {

  dialogRef: MatDialogRef<LoginComponent>;

  constructor(private http: HttpClient, private dialog: MatDialog, private storage: TokenStorageService) { }

  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    if(this.dialogRef) {
      return next.handle(req);
    }
    req = this.withAuthenticationHeader(req);
    let observable = next.handle(req);
    return observable.pipe(catchError((error) => {
      if (error.status === 401) {
        this.storage.remove();
        return this.askForCredentials().pipe(mergeMap(_ => this.http.request(this.withAuthenticationHeader(req))));
      }
      return throwError(error);
    }));
  }

  protected askForCredentials(): Observable<any> {
    this.dialogRef = this.dialog.open(LoginComponent);
    return this.dialogRef.afterClosed().pipe(map(result => {
      this.dialogRef = null;
      if (result && result['jwt']) {
        this.storage.set(result['jwt']);
        return result;
      } else {
        throw new Error("User declined login");
      }
    }));
  }

  protected withAuthenticationHeader(req: HttpRequest<any>) {
    if(!this.storage.get()) {
      return req;
    }
    req = req.clone({
      setHeaders: {
        Authorization: `Bearer ${this.storage.get()}`
      }
    });
    return req;
  }
}
