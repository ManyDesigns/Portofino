import {Directive, ViewContainerRef} from '@angular/core';

@Directive({
  selector: '[portofino-main-page]'
})
export class MainPageDirective {

  constructor(public viewContainerRef: ViewContainerRef) { }

}

@Directive({
  selector: '[portofino-navigation]'
})
export class NavigationDirective {

  constructor(public viewContainerRef: ViewContainerRef) { }

}
