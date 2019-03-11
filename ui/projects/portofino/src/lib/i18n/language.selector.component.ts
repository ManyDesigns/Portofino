import {Component} from "@angular/core";
import {PortofinoService} from "../portofino.service";

@Component({
  selector: 'portofino-language-selector',
  template: `
    <mat-form-field>
      <mat-select [(ngModel)]="portofino.currentLocale">
        <mat-option *ngFor="let key of portofino.locales" [value]="portofino.localeDefinitions[key]">
          {{ portofino.localeDefinitions[key].name }}
        </mat-option>
      </mat-select>
    </mat-form-field>`,
  styles: ['mat-form-field { font-size: 14px; }']
})
export class LanguageSelectorComponent {
  constructor(public portofino: PortofinoService) {}
}
