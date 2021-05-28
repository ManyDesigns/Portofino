import {Component, Input, TemplateRef} from "@angular/core";
import {Page} from "./page";
import {MatTableDataSource} from "@angular/material/table";

@Component({
  selector: 'portofino-page-settings-panel',
  templateUrl: '../../assets/page-settings-panel.component.html'
})
export class PageSettingsPanelComponent {
  @Input()
  page: Page;
  @Input()
  extraConfiguration: TemplateRef<any>;

  emptyDataSource = new MatTableDataSource([]);

  get template() {
    return this.page.template || { sections: ["default"] };
  }
}
