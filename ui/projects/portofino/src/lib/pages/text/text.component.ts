import {Page, PageConfiguration, PageSettingsPanel} from "../../page";
import {Component} from "@angular/core";
import {PortofinoComponent} from "../../page.factory";
import {Field} from "../../form";
import {Annotation, RICH_TEXT_ANNOTATION} from "../../class-accessor";

@Component({
  selector: 'portofino-text',
  templateUrl: './text.component.html',
  styleUrls: ['./text.component.scss']
})
@PortofinoComponent({
  name: 'text',
  computeSecurityCheckUrl: TextPageComponent.computeSecurityCheckUrl
})
export class TextPageComponent extends Page {

  static computeSecurityCheckUrl(apiRoot, parent) {
    return Page.defaultComputeSourceUrl(apiRoot, parent, '');
  }

  get text(): string {
    return this.configuration.text;
  }

  protected getPageSettingsPanel(): PageSettingsPanel {
    return new TextPageSettingsPanel(this);
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
}

export class TextPageSettingsPanel extends PageSettingsPanel {

  protected setupPageConfigurationForm(pageConfiguration) {
    super.setupPageConfigurationForm(pageConfiguration);
    this.formDefinition.contents.push(Field.fromProperty({
        name: 'text',
        label: 'Text',
        type: 'string',
        annotations: [new Annotation(RICH_TEXT_ANNOTATION, { value: true })]},
      pageConfiguration));
  }

  getPageConfigurationToSave(formValue = this.form.value): PageConfiguration {
    const pageConf: any = super.getPageConfigurationToSave(formValue);
    const config = Object.assign({}, this.page.configuration, formValue);
    pageConf.text = config.text;
    return pageConf;
  }

}
