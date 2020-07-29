import {Component, Input, TemplateRef} from "@angular/core";
import {Page} from "./page";

@Component({
  selector: 'portofino-page-settings-panel',
  templateUrl: '../../assets/page-settings-panel.component.html'
})
export class PageSettingsPanelComponent {
  @Input()
  page: Page;
  @Input()
  extraConfiguration: TemplateRef<any>;
}
