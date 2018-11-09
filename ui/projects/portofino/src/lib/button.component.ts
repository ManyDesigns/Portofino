import {Component, Input} from '@angular/core';
import {ButtonInfo} from "./buttons";

@Component({
  selector: 'portofino-button',
  templateUrl: './button.component.html'
})
export class ButtonComponent {

  @Input()
  button: ButtonInfo;
  @Input()
  component: any;

}
