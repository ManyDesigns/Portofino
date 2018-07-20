import {Directive, ViewContainerRef} from '@angular/core';

@Directive({
  selector: '[portofino-main-content]'
})
export class MainContentDirective {

  constructor(public viewContainerRef: ViewContainerRef) { }

}

@Directive({
  selector: '[portofino-embedded-content]'
})
export class EmbeddedContentDirective {

  constructor(public viewContainerRef: ViewContainerRef) { }

}
