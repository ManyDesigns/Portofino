import {Injectable, InjectionToken} from "@angular/core";
import {Observable, of} from "rxjs";
import {map} from "rxjs/operators";
import {Type} from "@angular/core/src/type";
import {MatSnackBar, MatSnackBarConfig} from "@angular/material";
import {HttpEvent, HttpEventType, HttpHandler, HttpInterceptor, HttpRequest} from "@angular/common/http";
import {AuthenticationService, NO_AUTH_HEADER} from "../security/authentication.service";

export enum NotificationLevel {
  INFO, WARN, ERROR
}

@Injectable()
export abstract class NotificationService {
  abstract show(message: string, level: NotificationLevel): Observable<void>;
  info(message) {
    return this.show(message, NotificationLevel.INFO);
  }
  warn(message) {
    return this.show(message, NotificationLevel.WARN);
  }
  error(message) {
    return this.show(message, NotificationLevel.ERROR);
  }
}

@Injectable()
export class WindowAlertNotificationService extends NotificationService {
  show(message: string, level: NotificationLevel) {
    return of(null).pipe(map(_ => {
      alert(message);
    }));
  }
}

@Injectable()
export class MatSnackBarNotificationService extends NotificationService {

  configuration: MatSnackBarConfig = { duration: 10000, verticalPosition: 'bottom' }; //TODO injection token?

  constructor(protected snackBar: MatSnackBar) {
    super();
  }

  show(message: string, level: NotificationLevel) {
    return this.snackBar.open(message, null, this.configuration).afterOpened();
  }
}

@Injectable()
export class NotificationInterceptor implements HttpInterceptor {

  readonly headerRegex = /([^:]+): (.+)/.compile()

  constructor(protected notificationService: NotificationService) {}

  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    return next.handle(req).pipe(map(value => {
      if(value.type == HttpEventType.Response) {
        let messages = value.headers.getAll("X-Portofino-Message") || [];
        messages.forEach(message => {
          const result = this.headerRegex.exec(message);
          if(result) {
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
