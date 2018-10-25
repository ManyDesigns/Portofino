import {Injectable, InjectionToken} from "@angular/core";
import {Observable, of} from "rxjs";
import {map} from "rxjs/operators";
import {Type} from "@angular/core/src/type";
import {MatSnackBar, MatSnackBarConfig} from "@angular/material";

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
