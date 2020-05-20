import {ErrorHandler, Inject, Injectable, InjectionToken} from "@angular/core";
import {concat, Observable, of} from "rxjs";
import {catchError, map, mergeMap, share} from "rxjs/operators";
import { MatSnackBar, MatSnackBarConfig } from "@angular/material/snack-bar";
import {HttpEvent, HttpEventType, HttpHandler, HttpInterceptor, HttpRequest} from "@angular/common/http";
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
export class NotificationsHolder extends NotificationService {

  notifications: { message: string, level: NotificationLevel }[] = [];
  maximumNotifications = 20;
  timeToLiveMs = 600000;

  show(message: string, level: NotificationLevel) {
    let notifications = this.notifications;
    let notification = { message: message, level: level, timeout: null };
    const len = notifications.unshift(notification);
    if(len > this.maximumNotifications) {
      notifications.pop();
    }
    this.notifications = notifications;
    if(this.timeToLiveMs > 0) {
      notification.timeout = setTimeout(() => {
        notification.timeout = null;
        this.remove(notification);
      }, this.timeToLiveMs);
    }
    return of(null);
  }

  remove(notification) {
    if(notification.timeout) {
      clearTimeout(notification.timeout);
    }
    this.notifications = this.notifications.filter(x => x != notification);
  }
}

export const NOTIFICATION_HANDLERS = new InjectionToken('NOTIFICATION_HANDLERS');

@Injectable()
export class NotificationDispatcher extends NotificationService {

  constructor(@Inject(NOTIFICATION_HANDLERS) protected handlers: NotificationService[]) {
    super();
  }

  show(message: string, level: NotificationLevel): Observable<void> {
    return this.handlers.reduce((acc, current) => concat(acc, current.show(message, level)), of(null));
  }
}

export const PORTOFINO_MESSAGE_HEADER = "X-Portofino-Message";

@Injectable()
export class NotificationInterceptor implements HttpInterceptor {

  constructor(protected notificationService: NotificationService) {}

  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    return next.handle(req).pipe(map(value => {
      if(value.type == HttpEventType.Response) {
        let messages = value.headers.getAll(PORTOFINO_MESSAGE_HEADER) || [];
        this.display(messages);
      }
      return value;
    }), catchError(e => {
      if(e.headers) {
        const messages = e.headers.getAll(PORTOFINO_MESSAGE_HEADER);
        if(messages) {
          this.display(messages);
          e.__portofino_handled = true;
        }
      }
      throw e;
    }));
  }

  protected display(messages) {
    const headerRegex = /^([^:]+?): (.+?)$/g;
    messages.forEach(message => {
      const result = headerRegex.exec(message);
      if (result && result.length > 0) {
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
    });
  }
}

@Injectable()
export class NotificationErrorHandler extends ErrorHandler {

  constructor(protected notificationService: NotificationService, protected translate: TranslateService) {
    super();
  }

  handleError(error: any): void {
    super.handleError(error);
    if(error.__portofino_handled) {
      return;
    }
    if(error.status) {
      this.notificationService.error(this.translate.get("Server error"));
    } else if(error.status === 0) {
      this.notificationService.error(this.translate.get("Communication error"));
    } else {
      this.notificationService.error(this.translate.get(error.message ? error.message : error));
    }
  }
}
