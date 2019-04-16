import {ErrorHandler, Injectable, InjectionToken} from "@angular/core";
import {Observable, of} from "rxjs";
import {map, mergeMap, share} from "rxjs/operators";
import {Type} from "@angular/core/src/type";
import {MatSnackBar, MatSnackBarConfig} from "@angular/material";
import {HttpEvent, HttpEventType, HttpHandler, HttpInterceptor, HttpRequest} from "@angular/common/http";
import {AuthenticationService, NO_AUTH_HEADER} from "../security/authentication.service";
import {TranslateService} from "@ngx-translate/core";

export enum NotificationLevel {
  INFO, WARN, ERROR
}

@Injectable()
export abstract class NotificationService {
  abstract show(message: string, level: NotificationLevel): Observable<void>;

  showObservable(message: Observable<string> | string, level: NotificationLevel) {
    if(message instanceof Observable) {
      return message.pipe(mergeMap(m => this.show(m, level)));
    } else {
      return this.show(message, level);
    }
  }

  info(message: string | Observable<string>) {
    const observable = this.showObservable(message, NotificationLevel.INFO).pipe(share());
    observable.subscribe();
    return observable;
  }
  warn(message: string | Observable<string>) {
    const observable = this.showObservable(message, NotificationLevel.WARN).pipe(share());
    observable.subscribe();
    return observable;
  }
  error(message: string | Observable<string>) {
    const observable = this.showObservable(message, NotificationLevel.ERROR).pipe(share());
    observable.subscribe();
    return observable;
  }
}

@Injectable()
export class WindowAlertNotificationService extends NotificationService {
  show(message: string, level: NotificationLevel) {
    return of(null).pipe(map(() => {
      alert(message);
    }));
  }
}

@Injectable()
export class MatSnackBarNotificationService extends NotificationService {

  configuration: MatSnackBarConfig = { duration: 10000, verticalPosition: 'bottom' }; //TODO injection token?

  constructor(protected snackBar: MatSnackBar, protected translate: TranslateService) {
    super();
  }

  show(message: string, level: NotificationLevel) {
    return this.snackBar.open(message, this.translate.instant('Ok'), this.configuration).afterOpened();
  }
}

@Injectable()
export class NotificationInterceptor implements HttpInterceptor {

  readonly headerRegex = /^([^:]+?): (.+?)$/g;

  constructor(protected notificationService: NotificationService) {}

  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    return next.handle(req).pipe(map(value => {
      if(value.type == HttpEventType.Response) {
        let messages = value.headers.getAll("X-Portofino-Message") || [];
        messages.forEach(message => {
          const result = this.headerRegex.exec(message);
          if(result && result.length > 0) {
            const type = result[1];
            const mess = result[2];
            switch (type) {
              case 'info':
                this.notificationService.info(mess);
                break;
              case 'warning':
                this.notificationService.warn(mess);
                break;
              case 'error':
                this.notificationService.error(mess);
                break;
              default:
                this.notificationService.warn(mess);
            }
          } else {
            this.notificationService.error(message);
          }
        })
      }
      return value;
    }));
  }
}

@Injectable()
export class NotificationErrorHandler extends ErrorHandler {

  constructor(protected notificationService: NotificationService, protected translate: TranslateService) {
    super();
  }

  handleError(error: any): void {
    super.handleError(error);
    if(error.status) {
      this.notificationService.error(this.translate.get("Server error"));
    } else {
      this.notificationService.error(error);
    }
  }
}
