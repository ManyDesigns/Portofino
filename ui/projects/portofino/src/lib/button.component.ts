import {Component, Input} from '@angular/core';
import {ButtonInfo, WithButtons} from "./buttons";

@Component({
  selector: 'portofino-button',
  template: `
    <button mat-flat-button [color]="button.color" type="button" (click)="button.action(component, $event)"
            *ngIf="(!button.icon || button.text) && button.presentIf(component)" [disabled]="!button.enabledIf(component)">
      <mat-icon *ngIf="button.icon">{{button.icon}}</mat-icon>
      {{button.text | translate }}
    </button>
    <button mat-icon-button [color]="button.color" type="button" (click)="button.action(component, $event)"
            *ngIf="button.icon && !button.text && button.presentIf(component)" [disabled]="!button.enabledIf(component)">
      <mat-icon>{{button.icon}}</mat-icon>
    </button>`
})
export class ButtonComponent {
  @Input()
  button: ButtonInfo;
  @Input()
  component: any;
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
