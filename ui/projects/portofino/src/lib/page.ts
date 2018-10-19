import {Component, ContentChild, InjectionToken, Input, OnInit, TemplateRef} from "@angular/core";
import {ANNOTATION_REQUIRED, ClassAccessor, Property} from "./class-accessor";
import {FormControl, FormGroup} from "@angular/forms";
import {PortofinoService} from "./portofino.service";
import {HttpClient, HttpHeaders} from "@angular/common/http";
import {Field, Form} from "./form";
import {Router} from "@angular/router";
import {AuthenticationService, NO_AUTH_HEADER} from "./security/authentication.service";
import {ButtonInfo, BUTTONS, declareButton} from "./buttons";
import {Observable, of} from "rxjs";
import {catchError, map} from "rxjs/operators";

export const NAVIGATION_COMPONENT = new InjectionToken('Navigation Component');

export interface NavigationComponent {
  page: Page;
}

@Component({
  selector: 'portofino-default-navigation',
  templateUrl: './navigation.component.html',
  styleUrls: ['./navigation.component.css']
})
export class DefaultNavigationComponent implements NavigationComponent {
  page: Page;
}

export class PageConfiguration {
  type: string;
  title: string;
  children: PageChild[];
  source: string;
  securityCheckPath: string = ':description';
}

export class PageChild {
  path: string;
  title: string;
  embedded: boolean;
  accessible: boolean;
}

@Component({
  selector: 'portofino-page-source-selector',
  templateUrl: 'source-selector.html'
})
export class SourceSelector implements OnInit {
  @Input()
  page: Page;
  @Input()
  property: Property;
  @Input()
  initialValue: string;
  form: FormGroup;

  constructor(public portofino: PortofinoService, protected http: HttpClient) {}

  ngOnInit(): void {
    const relativeToParent = new FormControl({
      value: this.page.parent && !this.initialValue.startsWith('/'),
      disabled: !this.page.parent
    });
    const source = new FormControl(this.initialValue);
    this.form = new FormGroup({ source: source, relativeToParent: relativeToParent });
    relativeToParent.valueChanges.subscribe(value => {
      this.updateSourceValue(value, source, source.value);
    });
    this.http.get(this.portofino.apiRoot + ':description')
      .subscribe(page => console.log("Page", page));
  }

  updateSourceValue(value, source, currentValue) {
    const parentSourceUrl = this.page.parent.computeSourceUrl() + '/';
    if (value) {
      source.setValue(currentValue.substring((parentSourceUrl).length));
    } else {
      source.setValue(parentSourceUrl + currentValue);
    }
  }

  select() {
    //TODO implement with tree
  }

}

export class PageSettingsPanel {
  active: boolean;
  readonly form = new FormGroup({});
  readonly formDefinition = new Form();
  classAccessor: ClassAccessor = {
    name: 'configuration',
    properties: [],
    keyProperties: []
  };

  constructor(public page: Page) {}

  refresh() {
    this.formDefinition.contents = [{
      component: SourceSelector,
      dependencies: {
        page: this.page,
        property: Object.assign(new Property(), {
          name: 'source',
          type: 'string',
          label: 'Path or URL',
          annotations: [{ type: ANNOTATION_REQUIRED, properties: [true] }]
        }),
        initialValue: this.page.configuration.source
      }
    }];
    this.classAccessor.properties.forEach(p => {
      this.createFieldForProperty(p);
    });
  }

  protected createFieldForProperty(p) {
    const field = new Field();
    field.property = p;
    field.initialState = this.page.configuration[p.name];
    this.formDefinition.contents.push(field);
  }
}

export abstract class Page {

  configuration: PageConfiguration & any;
  configurationLocation: string;
  readonly settingsPanel = new PageSettingsPanel(this);
  path: string;
  baseUrl: string;
  url: string;
  segment: string;
  parent: Page;
  allowEmbeddedComponents: boolean = true;

  readonly operationsPath = '/:operations';
  readonly configurationPath = '/:configuration';
  readonly page = this;

  protected constructor(
    protected portofino: PortofinoService, protected http: HttpClient, protected router: Router,
    public authenticationService: AuthenticationService) {
    //Declarative approach does not work for some reason:
    //"Metadata collected contains an error that will be reported at runtime: Lambda not supported."
    //TODO investigate with newer versions
    declareButton({
      color: 'primary', icon: 'save', text: 'Save', list: 'configuration'
    }, this, 'saveConfiguration', null);
    declareButton({
      icon: 'arrow_back', text: 'Cancel', list: 'configuration'
    }, this, 'cancelConfiguration', null);
  }

  consumePathSegment(fragment: string): boolean {
    return true;
  }

  get children(): PageChild[] {
    return this.configuration.children
  }

  getChild(segment: string) {
    return this.children.find(c => c.path == segment);
  }

  getButtons(list: string = 'default'): ButtonInfo[] | null {
    const allButtons = this[BUTTONS];
    return allButtons ? allButtons[list] : null;
  }

  prepare(): Observable<Page> {
    return this.checkAccess(true).pipe<Page>(map(() => this));
  }

  checkAccess(askForLogin: boolean): Observable<any> {
    let headers = new HttpHeaders();
    if(!askForLogin) {
      headers = headers.set(NO_AUTH_HEADER, 'true');
    }
    let sourceUrl = this.computeSourceUrl();
    const securityCheckPath = (this.configuration.securityCheckPath || ':description');
    if(!sourceUrl.endsWith('/') && !securityCheckPath.startsWith('/')) {
      sourceUrl += '/';
    } else if(sourceUrl.endsWith('/') && securityCheckPath.startsWith('/')) {
      sourceUrl = sourceUrl.substring(0, sourceUrl.length - 1);
    }
    return this.http.get<any>(
      sourceUrl + securityCheckPath,
      { headers: headers });
  }

  get accessPermitted(): Observable<boolean> {
    return this.checkAccess(false).pipe(map(() => true), catchError(() => of(false)));
  }

  computeSourceUrl() {
    let source = "";
    if(!this.configuration.source || !this.configuration.source.startsWith('/') ||
      !source.startsWith('http://') || !source.startsWith('https://')) {
      if(this.parent) {
        source = this.parent.computeSourceUrl() + '/';
      }
    }
    if(!source) {
      source = this.portofino.apiRoot;
    }
    return (source + (this.configuration.source ? this.configuration.source : ''))
    //replace double slash, but not in http://
      .replace(new RegExp("([^:])//"), '$1/');
  }

  operationAvailable(ops: Operation[], signature: string) {
    return ops.some(op => op.signature == signature && op.available);
  }

  get supportedSourceTypes(): string[] {
    return [];
  }

  configure() {
    this.settingsPanel.refresh();
    this.settingsPanel.active = true;
  }

  saveConfiguration() {
    const config = this.getConfigurationToSave(this.settingsPanel.form.value);
    this.portofino.saveConfiguration(this.configurationLocation, config).subscribe(
      () => {
        this.settingsPanel.active = false;
        this.router.navigateByUrl(this.router.url);
      },
      error => console.log(error));
  }

  protected getConfigurationToSave(formValue) {
    const config = Object.assign({}, this.configuration, formValue);
    delete config.relativeToParent;
    return config;
  }

  cancelConfiguration() {
    this.settingsPanel.active = false;
  }

}

@Component({
  selector: 'portofino-page-header',
  templateUrl: './page-header.component.html',
  styleUrls: ['./page-header.component.css']
})
export class PageHeader {
  @Input()
  page: Page;
  constructor(public authenticationService: AuthenticationService, public portofino: PortofinoService) {}
}

@Component({
  selector: 'portofino-default-page-layout',
  templateUrl: './default-page-layout.component.html',
  styleUrls: ['./default-page-layout.component.css']
})
export class DefaultPageLayout {
  @Input()
  page: Page;
  @ContentChild("content")
  content: TemplateRef<any>
}

export class Operation {
  name: string;
  signature: string;
  parameters: string[];
  available: boolean;
}
