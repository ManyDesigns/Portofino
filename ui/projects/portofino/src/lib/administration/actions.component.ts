import {Component, OnInit} from "@angular/core";
import {FlatTreeControl} from "@angular/cdk/tree";
import {HttpClient} from "@angular/common/http";
import {PortofinoService} from "../portofino.service";
import {BehaviorSubject, merge, Observable} from "rxjs";
import {CollectionViewer, SelectionChange} from "@angular/cdk/collections";
import {map} from "rxjs/operators";
import {Page, PageConfiguration} from "../page";
import {ActivatedRoute, Router} from "@angular/router";
import {AuthenticationService} from "../security/authentication.service";
import {PageFactoryComponent} from "../page.factory";

@Component({
  template: `
    <div fxLayout="row" fxLayoutAlign="start start">
      <div>
        <mat-tree [dataSource]="dataSource" [treeControl]="treeControl">
          <mat-tree-node *matTreeNodeDef="let node" matTreeNodeToggle matTreeNodePadding>
            <button mat-icon-button disabled></button>
            <strong style="margin-right: 0.5em;">{{node.name}}</strong>{{node.type ? node.type.substring(node.type.lastIndexOf('.') + 1) : ''}}
            <button mat-icon-button type="button" (click)="select(node)">
              <mat-icon>settings</mat-icon>
            </button>
          </mat-tree-node>

          <mat-tree-node *matTreeNodeDef="let node;when: isExpandable" matTreeNodePadding>
            <button mat-icon-button matTreeNodeToggle
                    [attr.aria-label]="'toggle ' + node.name">
              <mat-icon class="mat-icon-rtl-mirror">
                {{treeControl.isExpanded(node) ? 'expand_more' : 'chevron_right'}}
              </mat-icon>
            </button>
            <strong style="margin-right: 0.5em;">{{node.name}}</strong>{{node.type ? node.type.substring(node.type.lastIndexOf('.') + 1) : ''}}
            <button mat-icon-button type="button" (click)="select(node)">
              <mat-icon>settings</mat-icon>
            </button>
            <mat-progress-bar *ngIf="node.isLoading" mode="indeterminate"></mat-progress-bar>
          </mat-tree-node>
        </mat-tree>
      </div>
      <div>
        <mat-divider vertical="true"></mat-divider>
        <portofino-page
          *ngIf="selected"
          embedded="true" [path]="selected.pagePath" [configuration]="selected.configuration" (pageCreated)="configurePage($event)"></portofino-page>
      </div>
    </div>
  `
})
export class ActionsComponent extends Page implements OnInit {
  treeControl: FlatTreeControl<ActionFlatNode>;
  dataSource: PageTreeDataSource;
  selected: ActionFlatNode;

  constructor(http: HttpClient, portofino: PortofinoService, router: Router, route: ActivatedRoute, authenticationService: AuthenticationService) {
    super(portofino, http, router, route, authenticationService);
    this.treeControl = new FlatTreeControl<ActionFlatNode>(this._getLevel, this._isExpandable);
    this.dataSource = new PageTreeDataSource(this.treeControl, this.portofino.apiRoot, this.http);
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
    this.loadPageConfiguration(node.pagePath).subscribe(config => {
      node.configuration = config;
      this.selected = node;
    }, error => {
      if(error.status == 404) {
        if(node.type) {
          for(let c in PageFactoryComponent.components) {
            const cdef = PageFactoryComponent.components[c];
            if(cdef.defaultActionClass == node.type) {
              node.configuration = Object.assign(new PageConfiguration(), { type: c, source: 'portofino-upstairs/actions/' + node.path + '/action' });
              this.selected = node;
              break;
            }
          }
        }
      }
    });
  }

  configurePage(page: Page) {
    page.configure();
  }

}

class ActionFlatNode {
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
