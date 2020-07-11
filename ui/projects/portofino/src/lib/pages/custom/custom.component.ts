import {Page, PageConfiguration, PageSettingsPanel} from "../../page";
import {Component, OnInit} from "@angular/core";
import {PortofinoComponent} from "../../page.factory";
import {Field} from "../../form";
import {Annotation, RICH_TEXT_ANNOTATION} from "../../class-accessor";

export const DEFAULT_CUSTOM_PAGE_TEMPLATE = `
<portofino-page-layout [page]="this">
  <ng-template #content>
    <div [innerHTML]="html" *ngIf="html"></div>
    <div *ngIf="!html">{{ 'This a custom page. You can either provide an HTML file to display here, or override this template entirely using Angular.' | translate }}</div>
  </ng-template>
</portofino-page-layout>`

@Component({
  selector: 'portofino-custom',
  template: DEFAULT_CUSTOM_PAGE_TEMPLATE
})
@PortofinoComponent({
  name: 'custom',
  computeSecurityCheckUrl: CustomPageComponent.computeSecurityCheckUrl
})
export class CustomPageComponent extends Page  {

  html: string;

  static computeSecurityCheckUrl(apiRoot, parent) {
    // How do we check if this page is accessible? We can either:
    // - delegate to the parent page
    return Page.defaultComputeSecurityCheckUrl(apiRoot, parent, '');
    // - return null (always accessible)
    // return null
    // - invoke a specific URL (relative or absolute)
    // return Page.defaultComputeSecurityCheckUrl(apiRoot, parent, someUrl);
    // - perform custom logic
    // return ... //Must be an observable
  }

  protected getPageSettingsPanel(): PageSettingsPanel {
    return new CustomPageSettingsPanel(this);
  }

  computeSourceUrl(): any {
    let source;
    if(this.parent) {
      source = this.parent.computeSourceUrl();
    } else {
      source = this.portofino.apiRoot;
    }
    source = Page.removeDoubleSlashesFromUrl(source);
    while (source.endsWith("/"))  {
      source = source.substring(0, source.length - 1);
    }
    return source;
  }

  hasSource(): boolean {
    return false;
  }

  initialize() {
    super.initialize();
    const config = this.configuration;
    if(config && config.html) {
      this.http.get(Page.removeDoubleSlashesFromUrl(`pages${this.path}/${config.html}`), {
        responseType: "text"
      }).subscribe(html => {
        this.html = html;
      }, e => {
        this.notificationService.error(this.translate.get("Could not load page HTML"));
      });
    }
  }
}

export class CustomPageSettingsPanel extends PageSettingsPanel {

  protected setupPageConfigurationForm(pageConfiguration) {
    super.setupPageConfigurationForm(pageConfiguration);
    this.formDefinition.contents.push(
      Field.fromProperty({ name: 'html', label: 'HTML file' }, pageConfiguration));
    this.formDefinition.contents.push(
      Field.fromProperty({ name: 'script', label: 'JavaScript file' }, pageConfiguration));
  }

  getPageConfigurationToSave(formValue = this.form.value): PageConfiguration {
    const pageConf: any = super.getPageConfigurationToSave(formValue);
    const config = Object.assign({}, this.page.configuration, formValue);
    pageConf.html = config.html;
    pageConf.script = config.script;
    return pageConf;
  }
}
