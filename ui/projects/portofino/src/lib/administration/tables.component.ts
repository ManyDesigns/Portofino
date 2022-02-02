import {Page} from "../page";
import {Component, OnInit} from "@angular/core";
import {ConnectionProviderDetails, ConnectionProviderSummary} from "./support";
import {Button} from "../buttons";
import {NotificationService} from "../notifications/notification.services";
import {ActivatedRoute, Router} from "@angular/router";
import {HttpClient} from "@angular/common/http";
import {PortofinoService} from "../portofino.service";
import {AuthenticationService} from "../security/authentication.service";
import {TranslateService} from "@ngx-translate/core";
import {FlatTreeControl} from "@angular/cdk/tree";
import {Field, Form} from "../form";
import {FormGroup} from "@angular/forms";
import {ClassAccessor} from "../class-accessor";
import {BehaviorSubject, merge, Observable, Subscription} from "rxjs";
import {CollectionViewer, SelectionChange} from "@angular/cdk/collections";
import {map} from "rxjs/operators";
import {CdkDragDrop, moveItemInArray} from "@angular/cdk/drag-drop";
import {Location} from "@angular/common";

@Component({
  templateUrl: '../../../assets/administration/tables.component.html'
})
export class TablesComponent extends Page implements OnInit {

  connectionProviders: ConnectionProviderSummary[];
  tableTreeDataSource: TableTreeDataSource;
  tableInfo: any;
  column: any;
  annotationsForm: Form;
  readonly annotations = new FormGroup({});

  constructor(portofino: PortofinoService, http: HttpClient, router: Router, route: ActivatedRoute,
              authenticationService: AuthenticationService, notificationService: NotificationService,
              translate: TranslateService, location: Location) {
    super(portofino, http, router, route, authenticationService, notificationService, translate, location);
    const tableTreeControl = new FlatTreeControl<TableFlatNode>(this._getLevel, this._isExpandable);
    this.tableTreeDataSource = new TableTreeDataSource(tableTreeControl, http, portofino.apiRoot, notificationService, translate);
  }

  private _getLevel = (node: TableFlatNode) => node.level;

  private _isExpandable = (node: TableFlatNode) => { return node.expandable };

  isExpandable = (_: number, node: TableFlatNode) => { return this._isExpandable(node); };

  ngOnInit(): void {
    this.loadConnectionProviders();
  }

  loadConnectionProviders() {
    const url = `${this.portofino.apiRoot}portofino-upstairs/database/connections`;
    this.page.http.get<ConnectionProviderSummary[]>(url).subscribe(s => {
      this.connectionProviders = s;
      this.tableTreeDataSource.data = s.map(c => new TableFlatNode(c.name, null, null));
    });
  }

  editTable(table: TableFlatNode) {
    const url = `${this.portofino.apiRoot}portofino-upstairs/database/tables/${table.db}/${table.schema.name}/${table.table}`;
    this.http.get(url).subscribe(tableInfo => {
      this.tableInfo = tableInfo;
      this.tableInfo.db = table.db;
      this.tableInfo.schema = table.schema.name;
      this.prepareTableInfo();
    });
  }

  protected prepareTableInfo() {
    this.tableInfo.table.columns.forEach(c => {
      if (!c.javaType) {
        c.javaType = "default";
      }
    });
    if(this.tableInfo.table.query) {
      this.tableInfo.table.query.forEach(q => {
        q.isHql = !!q.hql;
      });
    }
    this.tableInfo.permissions.groups.forEach(g => {
      if(!g.permissionMap) {
        g.permissionMap = {};
        g.permissions.forEach(p => {
          g.permissionMap[p] = true;
        });
      }
    });
  }

  @Button({ list: "table", text: "Save", icon: "save", color: "primary" })
  saveTable() {
    const table = this.tableInfo.table;
    table.columns.forEach(c => {
      if(c.javaType == "default") {
        c.javaType = null;
      }
    });
    this.tableInfo.table.query.forEach(q => {
      this.cancelSelectionProvider(q);
      delete q.isHql;
    });
    const permissions: any = { groups: [] };
    this.tableInfo.permissions.groups.forEach(g => {
      const group = {
        name: g.name,
        permissions: []
      };
      permissions.groups.push(group);
      for(let p in g.permissionMap) {
        if(g.permissionMap[p]) {
          group.permissions.push(p);
        }
      }
    });

    const url = `${this.portofino.apiRoot}portofino-upstairs/database/tables/${this.tableInfo.db}/${this.tableInfo.schema}/${table.tableName}`;
    this.http.put(url, { table: this.tableInfo.table, permissions: permissions }).subscribe(
      () => {
        this.prepareTableInfo();
        this.notificationService.info(this.translate.instant("Table saved"));
      }, () => {
        this.prepareTableInfo();
        this.notificationService.error(this.translate.instant("Table not saved"));
      });
  }

  @Button({ list: "table", text: "Cancel", icon: 'arrow_back' })
  cancelTable() {
    this.tableInfo = null;
  }

  editColumn(column, index) {
    this.column = column;
    this.column.index = index;
    this.changeType(this.column, this.column.javaType);
  }

  @Button({ list: "column", text: "Save", icon: "save", color: "primary" })
  saveColumn() {
    const url = `${this.portofino.apiRoot}portofino-upstairs/database/tables/${this.tableInfo.db}/${this.tableInfo.schema}/${this.tableInfo.table.tableName}/${this.column.columnName}`;
    const column = Object.assign({}, this.column);
    delete column.index;
    if(column.javaType == "default") {
      column.javaType = null;
    }
    this.http.put(url, { column: column, annotations: this.annotations.value }).subscribe(
      () => { this.notificationService.info(this.translate.instant("Column saved")); });
  }

  @Button({ list: "column", text: "Cancel" })
  cancelColumn() {
    this.column = null;
  }

  moveColumn(event: CdkDragDrop<string[]>) {
    moveItemInArray(this.tableInfo.table.columns, event.previousIndex, event.currentIndex);
  }

  changeType(column, newType) {
    const url = `${this.portofino.apiRoot}portofino-upstairs/database/tables/${this.tableInfo.db}/${this.tableInfo.schema}/${this.tableInfo.table.tableName}/${this.column.columnName}/annotations/${newType}`;
    this.http.get<{ classAccessor: ClassAccessor, annotations: any}>(url).subscribe(c => {
      const form = Form.fromClassAccessor(ClassAccessor.create(c.classAccessor));
      form.contents.forEach(f => {
        if(f instanceof Field) {
          f.initialState = c.annotations[f.name];
        }
      });
      this.annotationsForm = form;
    });
  }

  getFromColumns(sp) {
    return sp.reference.map(r => r.fromColumn).join(", ");
  }

  getToColumns(fk) {
    return fk.reference.map(r => r.toColumn).join(", ");
  }

  getReferencedTableName(fk, tableInfo) {
    let prefix = "";
    if(fk.toDatabase != tableInfo.db) {
      prefix += `${fk.toDatabase}.${fk.toSchema}.`;
    } else if(fk.toSchema != tableInfo.schema) {
      prefix += `${fk.toSchema}.`;
    }
    return prefix + fk.toTable;
  }

  editSelectionProvider(sp) {
    sp.beingEdited = Object.assign({}, sp);
    sp.columns = this.getFromColumns(sp);
  }

  deleteSelectionProvider(sp) {
    this.tableInfo.table.query = this.tableInfo.table.query.filter(q => q != sp);
  }

  saveSelectionProvider(sp) {
    if(!sp.name || !sp.columns || !sp.toDatabase || (!sp.hql && !sp.sql)) {
      return;
    }
    delete sp.beingEdited;
    delete sp.new;
    if(sp.columns) {
      sp.reference = [];
      const columns = sp.columns.split(',');
      columns.forEach(c => { sp.reference.push({ fromColumn: c.trim() }); });
    }
    delete sp.columns;
    if(sp.hql && sp.sql) {
      delete sp.sql;
    }
    sp.isHql = !!sp.hql;
  }

  cancelSelectionProvider(sp) {
    if(sp.new) {
      this.deleteSelectionProvider(sp);
    } else if(sp.beingEdited) {
      const old = sp.beingEdited;
      delete sp.beingEdited;
      Object.assign(sp, old);
      delete sp.columns;
    }
  }

  addSelectionProvider() {
    this.tableInfo.table.query.push({ new: true, beingEdited: true });
  }

  @Button({ list: "misc", text: "Reload model", color: "primary", icon: "refresh" })
  reloadModel() {
    this.http.post(this.portofino.apiRoot + "portofino-upstairs/model/:reload", null).subscribe(
      () => {
        this.loadConnectionProviders();
        this.notificationService.info(this.translate.get("Model reloaded."));
      }
    );
  }

  get groups() {
    return this.tableInfo.permissions.groups.sort((g1, g2) => g1.name.localeCompare(g2.name));
  }
}

class TableFlatNode {
  loading = false;
  readonly children: TableFlatNode[] = [];
  constructor(public db: string, public schema: {name: string, liquibase: boolean }, public table: string) {}

  get level() {
    if(this.table) {
      return 2;
    } else if(this.schema) {
      return 1;
    } else {
      return 0;
    }
  }

  get expandable() {
    return !this.table;
  }

  get displayName() {
    if(this.table) return this.table;
    if(this.schema) return this.schema.name;
    if(this.db) return this.db;
  }
}

class TableTreeDataSource {

  readonly dataChange = new BehaviorSubject<TableFlatNode[]>([]);
  protected _data: TableFlatNode[];
  protected _filter: string;
  protected subscriptions: Subscription[] = [];

  constructor(public treeControl: FlatTreeControl<TableFlatNode>, private http: HttpClient, private apiRoot: string,
              private notificationService: NotificationService, private translate: TranslateService) {}

  get data(): TableFlatNode[] { return this.dataChange.value; }
  set data(value: TableFlatNode[]) {
    this._data = value;
    const filteredData = this.filteredData();
    this.treeControl.dataNodes = filteredData;
    this.dataChange.next(filteredData);
  }

  protected filteredData() {
    return this._data.filter(n => {
      return !n.table || !this._filter || n.table.toLowerCase().includes(this._filter.toLowerCase());
    });
  }

  get filter() { return this._filter; }
  set filter(value) {
    this._filter = value;
    this.data = this._data;
    if(value) {
      this.treeControl.dataNodes.forEach(n => this.treeControl.expandDescendants(n));
    }
  }

  connect(collectionViewer: CollectionViewer): Observable<TableFlatNode[]> {
    this.subscriptions.push(this.treeControl.expansionModel.changed.subscribe(change => {
      if ((change as SelectionChange<TableFlatNode>).added ||
          (change as SelectionChange<TableFlatNode>).removed) {
        this.handleTreeControl(change as SelectionChange<TableFlatNode>);
      }
    }));

    return merge(collectionViewer.viewChange, this.dataChange).pipe(map(() => this.data));
  }

  disconnect() {
    this.subscriptions.forEach(s => s.unsubscribe());
    this.subscriptions = [];
  }

  /** Handle expand/collapse behaviors */
  handleTreeControl(change: SelectionChange<TableFlatNode>) {
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
  toggleNode(node: TableFlatNode, expand: boolean) {
    const index = this.data.indexOf(node);
    if (!node.expandable || index < 0) {
      return;
    }
    if(expand) {
      if(node.children.length > 0) {
        //Already loaded
        this._data.splice(index + 1, 0, ...node.children);
        this.dataChange.next(this.filteredData());
        node.children.forEach(n => {
          if(this.treeControl.isExpanded(n)) {
            this.toggleNode(n, true);
          }
        })
      } else {
        this.loadNode(node, index);
      }
    } else {
      let count = 0;
      for (let i = index + 1; i < this._data.length && this._data[i].level > node.level; i++, count++) {}
      this._data.splice(index + 1, count);
      this.dataChange.next(this.filteredData());
    }
  }

  protected loadNode(node: TableFlatNode, index) {
    node.loading = true;
    if(node.schema) {
      const url = `${this.apiRoot}portofino-upstairs/database/tables/${node.db}/${node.schema.name}`;
      this.http.get(url).subscribe((tables: any[]) => {
        tables.forEach(table => {
          node.children.push(new TableFlatNode(node.db, node.schema, table.name));
        });
        this._data.splice(index + 1, 0, ...node.children);
        node.loading = false;
        this.dataChange.next(this.filteredData());
      }, () => {
        node.loading = false;
        this.notificationService.error(this.translate.get("Could not load tables"));
      });
    } else {
      const url = `${this.apiRoot}portofino-upstairs/database/connections/${node.db}`;
      this.http.get<ConnectionProviderDetails>(url).subscribe(c => {
        c.schemas.forEach(schema => {
          node.children.push(new TableFlatNode(node.db, { name: schema.schema, liquibase: false }, null)); //TODO
        });
        this._data.splice(index + 1, 0, ...node.children);
        node.loading = false;
        this.dataChange.next(this.filteredData());
      }, () => {
        node.loading = false;
        this.notificationService.error(this.translate.get("Could not load schemas"));
      });
    }
  }
}
