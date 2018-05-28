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
export class AuthenticationService {

  dialogRef: MatDialogRef<LoginComponent>;
  currentUser: UserInfo;

  constructor(private http: HttpClient, protected dialog: MatDialog, protected storage: TokenStorageService) {
    this.currentUser = new UserInfo(this.storage.get('user.displayName'));
  }

  request(req: HttpRequest<any>, observable: Observable<HttpEvent<any>>): Observable<HttpEvent<any>> {
    if(this.dialogRef) {
      return observable;
    }
    return observable.pipe(catchError((error) => {
      if (error.status === 401) {
        this.storage.remove('jwt');
        this.storage.remove('user.displayName');
        this.currentUser = null;
        return this.askForCredentials().pipe(mergeMap(_ => this.http.request(this.withAuthenticationHeader(req))));
      }
      return throwError(error);
    }));
  }

  protected askForCredentials(): Observable<any> {
    this.dialogRef = this.dialog.open(LoginComponent);
    return this.dialogRef.afterClosed().pipe(map(result => {
      this.dialogRef = null;
      if (result && result.jwt) {
        this.storage.set('jwt', result.jwt);
        this.storage.set('user.displayName', result.displayName);
        this.currentUser = new UserInfo(result.displayName);
        return result;
      } else {
        throw new Error("User declined login");
      }
    }));
  }

  withAuthenticationHeader(req: HttpRequest<any>) {
    if(!this.storage.get('jwt')) {
      return req;
    }
    req = req.clone({
      setHeaders: {
        Authorization: `Bearer ${this.storage.get('jwt')}`
      }
    });
    return req;
  }
}

@Injectable()
export class AuthenticationInterceptor implements HttpInterceptor {

  constructor(protected authenticationService: AuthenticationService) {}

  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    req = this.authenticationService.withAuthenticationHeader(req);
    let observable = next.handle(req);
    return this.authenticationService.request(req, observable);
  }
}

export class UserInfo {
  constructor(public displayName: string) {}
}
