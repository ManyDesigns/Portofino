import {Page, PageConfiguration, PageSettingsPanel, PortofinoComponent} from "../../page";
import {Component} from "@angular/core";
import {Field} from "../../form";
import {Annotation, RICH_TEXT_ANNOTATION} from "../../class-accessor";

@Component({
  selector: 'portofino-text',
  templateUrl: '../../../../assets/pages/text/text.component.html',
  styleUrls: ['../../../../assets/pages/text/text.component.scss']
})
@PortofinoComponent({
  name: 'text',
  computeSecurityCheckUrl: TextPageComponent.computeSecurityCheckUrl
})
export class TextPageComponent extends Page {

  static computeSecurityCheckUrl(apiRoot, parent) {
    return Page.defaultComputeSecurityCheckUrl(apiRoot, parent, '');
  }

  get text(): string {
    return this.processLinks(this.configuration.text);
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

  protected processLinks(text: any) {
    if(!text || typeof(text.replaceAll) !== 'function') {
      return text;
    }
    return text.replaceAll(/<a.*?<\/a>/g,
      function (match) {
        const container = document.createElement("span");
        container.innerHTML = match;
        const link = container.children[0] as any;
        link.removeAttribute("target");
        let href = link.getAttribute("href");
        const out = href.startsWith("http:") || href.startsWith("https:");
        if(out) {
          link.target = "_blank";
        }
        return link.outerHTML;
    });
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
