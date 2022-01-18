import {Component, Input} from '@angular/core';
import {ButtonInfo, WithButtons} from "./buttons";
import {isObservable} from "rxjs";

@Component({
  selector: 'portofino-button',
  template: `
    <button mat-flat-button [color]="button.color" type="button" (click)="executeAction($event)"
            *ngIf="!isIconOnly() && isPresent()" [disabled]="isDisabled()">
      <mat-icon *ngIf="button.icon">{{button.icon}}</mat-icon>
      {{button.text | translate }}
    </button>
    <button mat-icon-button [color]="button.color" type="button" (click)="executeAction($event)"
            *ngIf="isIconOnly() && isPresent()" [disabled]="isDisabled()">
      <mat-icon>{{button.icon}}</mat-icon>
    </button>`
})
export class ButtonComponent {
  @Input()
  button: ButtonInfo;
  @Input()
  component: any;
  disabled: boolean;

  isIconOnly() {
    return this.button.icon && !this.button.text;
  }

  isPresent() {
    return this.button.presentIf(this.component);
  }

  isDisabled() {
    return this.disabled || !this.button.enabledIf(this.component);
  }

  executeAction($event) {
    const result = this.button.action(this.component, $event);
    if(isObservable(result)) {
      this.disabled = true;
      const enable = () => { this.disabled = false };
      result.subscribe(enable, enable, enable);
    }
  }
}

@Component({
  selector: 'portofino-buttons',
  template: `<portofino-button *ngFor="let button of component.getButtons(list)"
                               [button]="button" [component]="component"></portofino-button>`
})
export class ButtonsComponent {
  @Input()
  component: WithButtons;
  @Input()
  list: string = 'default';
}
