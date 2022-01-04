import {Page, PageConfiguration, PageSettingsPanel, PortofinoComponent} from "../../page";
import {Location} from "@angular/common";
import {Component, OnDestroy, Optional} from "@angular/core";
import {Field} from "../../form";
import {PortofinoService} from "../../portofino.service";
import {HttpClient} from "@angular/common/http";
import {ActivatedRoute, Router} from "@angular/router";
import {AuthenticationService} from "../../security/authentication.service";
import {NotificationService} from "../../notifications/notification.services";
import {TranslateService} from "@ngx-translate/core";
import {DomSanitizer, SafeHtml} from "@angular/platform-browser";
import {BehaviorSubject} from "rxjs";

export enum HtmlLoadStatus {
  NOT_YET_LOADED, LOADED, ERRORED
}

@Component({
  selector: 'portofino-custom',
  templateUrl: '../../../../assets/pages/custom/custom.component.html'
})
@PortofinoComponent({
  name: 'custom',
  computeSecurityCheckUrl: CustomPageComponent.computeSecurityCheckUrl
})
export class CustomPageComponent extends Page implements OnDestroy {

  html: SafeHtml;
  readonly htmlLoadStatus = new BehaviorSubject(HtmlLoadStatus.NOT_YET_LOADED);

  constructor(
    portofino: PortofinoService, http: HttpClient, router: Router, @Optional() route: ActivatedRoute,
    authenticationService: AuthenticationService, notificationService: NotificationService,
    translate: TranslateService, location: Location, protected domSanitizer: DomSanitizer) {
    super(portofino, http, router, route, authenticationService, notificationService, translate, location);
  }

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
    const config = this.configuration;
    if(config && config.html) {
      this.http.get(Page.removeDoubleSlashesFromUrl(`pages${this.path}/${config.html}`), {
        responseType: "text"
      }).subscribe(html => {
        this.html = this.domSanitizer.bypassSecurityTrustHtml(html);
        setTimeout(() => {
          this.htmlLoadStatus.next(HtmlLoadStatus.LOADED);
          super.initialize();
        }, 0);
      }, () => {
        this.notificationService.error(this.translate.get("Could not load page HTML"));
        this.htmlLoadStatus.next(HtmlLoadStatus.ERRORED);
        super.initialize();
      });
    }
  }

  ngOnDestroy() {
    this.htmlLoadStatus.complete();
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
