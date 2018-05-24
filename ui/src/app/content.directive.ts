import {Directive, ViewContainerRef} from '@angular/core';

@Directive({
  selector: '[portofino-content]'
})
export class ContentDirective {

  constructor(public viewContainerRef: ViewContainerRef) { }

}
