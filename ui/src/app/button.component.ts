import {Component, ComponentFactoryResolver, Input, OnDestroy, OnInit, ViewChild} from '@angular/core';
import {ActivatedRoute, UrlSegment} from "@angular/router";
import {PortofinoAppComponent} from "./portofino-app.component";
import {HttpClient} from "@angular/common/http";
import {ContentDirective} from "./content.directive";
import {Observable, Subscription} from "rxjs/index";
import {map} from "rxjs/operators";
import {ThemePalette} from "@angular/material/core/typings/common-behaviors/color";
import {ButtonInfo} from "./page.component";

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
