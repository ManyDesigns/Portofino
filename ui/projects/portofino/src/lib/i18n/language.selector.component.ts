import {Component} from "@angular/core";
import {PortofinoService} from "../portofino.service";

@Component({
  selector: 'portofino-language-selector',
  template: `
    <button mat-icon-button [matMenuTriggerFor]="portofinoLanguageSelector">
      <mat-icon>language</mat-icon>
    </button>
    <mat-menu #portofinoLanguageSelector="matMenu">
      <button *ngFor="let key of portofino.locales" mat-menu-item
              (click)="portofino.currentLocale = portofino.localeDefinitions[key]">
        <span>{{ portofino.localeDefinitions[key].name | translate }}</span>
      </button>
    </mat-menu>`,
  styles: ['mat-form-field { font-size: 14px; }']
})
export class LanguageSelectorComponent {
  constructor(public portofino: PortofinoService) {}
}
