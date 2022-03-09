import {Component, Inject, Injector, OnInit} from "@angular/core";
import {FlatTreeControl} from "@angular/cdk/tree";
import {HttpClient} from "@angular/common/http";
import {PortofinoService} from "../portofino.service";
import {BehaviorSubject, merge, Observable} from "rxjs";
import {CollectionViewer, SelectionChange} from "@angular/cdk/collections";
import {map} from "rxjs/operators";
import {Page, PageConfiguration, PageFactoryComponent} from "../page";
import {ActivatedRoute, Router} from "@angular/router";
import {AuthenticationService} from "../security/authentication.service";
import {NotificationService} from "../notifications/notification.services";
import {TranslateService} from "@ngx-translate/core";
import {Field, Form} from "../form";
import {Property} from "../class-accessor";
import {FormGroup} from "@angular/forms";
import { MAT_DIALOG_DATA, MatDialog, MatDialogRef } from "@angular/material/dialog";
import {Location} from "@angular/common";

@Component({ templateUrl: "../../../assets/administration/actions.component.html" })
export class ActionsComponent extends Page implements OnInit {
  treeControl: FlatTreeControl<ActionFlatNode>;
  dataSource: PageTreeDataSource;
  selected: ActionFlatNode;
  injector: Injector;
  error: string;

  constructor(http: HttpClient, portofino: PortofinoService, router: Router, route: ActivatedRoute,
              authenticationService: AuthenticationService, notificationService: NotificationService,
              translate: TranslateService, location: Location,
              parentInjector: Injector, protected dialog: MatDialog) {
    super(portofino, http, router, route, authenticationService, notificationService, translate, location);
    this.treeControl = new FlatTreeControl<ActionFlatNode>(this._getLevel, this._isExpandable);
    this.dataSource = new PageTreeDataSource(this.treeControl, this.portofino.apiRoot, this.http);
    this.injector = Injector.create({
      providers: [{ provide: PortofinoService, useValue: Object.assign({}, portofino, { localApiPath: null }) }],
      parent: parentInjector
    });
  }

  ngOnInit(): void {
    this.http.get(this.portofino.apiRoot + ':description').subscribe((page: any) => {
      const root = new ActionFlatNode("/", page.path, '/',  0);
      root.type = page.superclass;
      this.dataSource.data = [root];
    });
  }

  private _getLevel = (node: ActionFlatNode) => node.level;

  private _isExpandable = (node: ActionFlatNode) => { return node.expandable || this.treeControl.isExpanded(node) };

  isExpandable = (_: number, node: ActionFlatNode) => { return this._isExpandable(node); };

  select(node: ActionFlatNode) {
    this.selected = null;
    if(node.type) {
      for(let c in PageFactoryComponent.components) {
        const cdef = PageFactoryComponent.components[c];
        if(cdef.defaultActionClass == node.type) {
          node.configuration = Object.assign(new PageConfiguration(),
            { type: c, source: 'portofino-upstairs/actions/' + node.path + '/action' });
          this.selected = node;
          break;
        }
      }
    }
    if(!this.selected) {
      node.configuration = Object.assign(new PageConfiguration(),
        { actualType: GenericPage, source: 'portofino-upstairs/actions/' + node.path + '/action' });
      this.selected = node;
    }
  }

  configurePage(page: Page) {
    const callback = (saved: boolean) => {
      if(saved) {
        this.notificationService.info(this.translate.get("Configuration saved"));
      }
      page.settingsPanel.buttons = false;
      page.settingsPanel.children = false;
      page.settingsPanel.active = true;
    };
    page.configure(callback);
  }

  delete(node: ActionFlatNode) {
    this.translate.get("Delete the current action and all its children?").subscribe(t => {
      if(confirm(t)) {
        const actions = this.portofino.apiRoot + 'portofino-upstairs/actions/';
        this.http.delete(actions + node.path).subscribe(() => {
          this.dataSource.data.splice(this.dataSource.data.indexOf(node), 1);
          this.dataSource.dataChange.next(this.dataSource.data);
        });
      }
    });
  }

  addChild(parent: ActionFlatNode) {
    this.dialog.open(CreateActionComponent, { data: parent }).afterClosed().subscribe(node => {});
  }

}

export class ActionFlatNode {
  expandable = true;
  type: string;
  isLoading = false;
  configuration: any;
  readonly children: ActionFlatNode[] = [];
  constructor(public name: string, public path: string, public pagePath: string, public level: number) {}
}

class PageTreeDataSource {

  readonly dataChange = new BehaviorSubject<ActionFlatNode[]>([]);

  get data(): ActionFlatNode[] { return this.dataChange.value; }
  set data(value: ActionFlatNode[]) {
    this.treeControl.dataNodes = value;
    this.dataChange.next(value);
  }

  constructor(private treeControl: FlatTreeControl<ActionFlatNode>, private apiRoot: string, private http: HttpClient) {}

  connect(collectionViewer: CollectionViewer): Observable<ActionFlatNode[]> {
    this.treeControl.expansionModel.changed.subscribe(change => {
      if ((change as SelectionChange<ActionFlatNode>).added ||
        (change as SelectionChange<ActionFlatNode>).removed) {
        this.handleTreeControl(change as SelectionChange<ActionFlatNode>);
      }
    });

    return merge(collectionViewer.viewChange, this.dataChange).pipe(map(() => this.data));
  }

  /** Handle expand/collapse behaviors */
  handleTreeControl(change: SelectionChange<ActionFlatNode>) {
    if (change.added) {
      change.added.forEach(node => this.toggleNode(node, true));
    }
    if (change.removed) {
      change.removed.slice().reverse().forEach(node => this.toggleNode(node, false));
    }
  }

  /**
   * Toggle the node, remove from display list
   */
  toggleNode(node: ActionFlatNode, expand: boolean) {
    const index = this.data.indexOf(node);
    if (!node.expandable || index < 0) {
      return;
    }
    if(expand) {
      if(node.children.length > 0) {
        //Already loaded
        this.data.splice(index + 1, 0, ...node.children);
        this.dataChange.next(this.data);
      } else {
        this.loadNode(node, index);
      }
    } else {
      let count = 0;
      for (let i = index + 1; i < this.data.length
      && this.data[i].level > node.level; i++, count++) {}
      this.data.splice(index + 1, count);
      this.dataChange.next(this.data);
    }
  }

  protected loadNode(node: ActionFlatNode, index) {
    node.isLoading = true;
    const actions = this.apiRoot + 'portofino-upstairs/actions/';
    const url = actions + (node.level > 0 ? node.path + '/:description' : ':description');
    this.http.get(url).subscribe((page: any) => {
      if (!page.children || page.children.length == 0) {
        node.expandable = false;
      } else {
        page.children.forEach(child => {
          const childPath = (node.level > 0 ? page.path + child : child);

          const pagePath = '/' + childPath.split('/').filter(s => s != '_detail').join('/');
          const childPage = new ActionFlatNode(child, childPath, pagePath, node.level + 1);
          node.children.push(childPage);
          this.http.get(actions + childPath + '/:description').subscribe((page: any) => {
            childPage.type = page.superclass;
            if (!page.children || page.children.length == 0) {
              childPage.expandable = false;
            }
          });
        });
        this.data.splice(index + 1, 0, ...node.children);
      }
      node.isLoading = false;
      this.dataChange.next(this.data);
    });
  }
}

@Component({
  template: `
    <portofino-page-layout [page]="this">
      <ng-template #content>This is a generic page used only for configuration</ng-template>
    </portofino-page-layout>`
})
export class GenericPage extends Page {

}

@Component({
  template: `
    <h4 mat-dialog-title>{{ 'Create new action' | translate }}</h4>
    <mat-dialog-content>
      <mat-error *ngIf="error">{{error|translate}}</mat-error>
      <form (submit)="save()">
        <portofino-form [form]="form" [controls]="controls"></portofino-form>
        <button type="submit" style="display:none">{{ 'Save' | translate }}</button>
      </form>
    </mat-dialog-content>
    <mat-dialog-actions>
      <button mat-button color="primary" (click)="save()" [disabled]="!controls.valid">
        <mat-icon>save</mat-icon>
        {{'Save'|translate}}
      </button>
      <button mat-button (click)="cancel()">
        <mat-icon>close</mat-icon>
        {{'Cancel'|translate}}
      </button>
    </mat-dialog-actions>`
})
export class CreateActionComponent {
  readonly form = new Form([
    new Field(Property.create({ name: "segment", type: "string", label: "Segment" }).required()),
    new Field(Property.create({ name: "type", type: "string", label: "Type" }).required().withSelectionProvider({
      options: this.getActionTypes()
    }))]);
  readonly controls = new FormGroup({});
  error: any;

  constructor(protected dialog: MatDialogRef<CreateActionComponent>,
              @Inject(MAT_DIALOG_DATA) protected parent: ActionFlatNode,
              protected portofino: PortofinoService, protected http: HttpClient,
              protected translate: TranslateService) {}

  protected getActionTypes() {
    const types = [];
    for (let k in PageFactoryComponent.components) {
      if(PageFactoryComponent.components[k].defaultActionClass) {
        types.push({v: k, l: this.translate.instant(`page type: ${k}`)})
      }
    }
    return types;
  }

  cancel() {
    this.dialog.close();
  }

  save() {
    const action = this.controls.value;
    const actions = `${this.portofino.apiRoot}portofino-upstairs/actions${this.parent.path}/${action.segment}`;
    const actionClass = PageFactoryComponent.components[this.controls.value.type.v].defaultActionClass;
    this.http.post(actions, actionClass).subscribe(() => {
      this.dialog.close({ segment: action.segment, actionClass: actionClass });
    }, () => {
      this.error = "Error";
    })
  }
}
