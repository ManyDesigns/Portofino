import { MatFormFieldControl } from "@angular/material/form-field";
import {ControlValueAccessor, FormControl, NgControl} from "@angular/forms";
import {Subject} from "rxjs";
import {
  AfterViewInit,
  Component,
  HostBinding,
  Input,
  OnDestroy,
  Optional,
  Self,
  ViewChild,
  ViewEncapsulation
} from "@angular/core";
import {QuillEditorComponent} from "ngx-quill";
import {coerceBooleanProperty} from "@angular/cdk/coercion";

@Component({
  providers: [{provide: MatFormFieldControl, useExisting: RichTextComponent}],
  selector: 'portofino-rich-text',
  template: `
      <quill-editor [formControl]="control" [required]="required" *ngIf="enabled"></quill-editor>
      <p [innerHTML]="control.value" *ngIf="!enabled" class="rich-text read-only" [ngStyle]="{ 'margin-bottom': control.value ? 0 : null }"></p>`,
  styles: [`p.rich-text.read-only p { margin-bottom: 0; }`],
  encapsulation: ViewEncapsulation.None
})
export class RichTextComponent implements MatFormFieldControl<string>, ControlValueAccessor, OnDestroy, AfterViewInit {
  @ViewChild(QuillEditorComponent, { static: true })
  editor: QuillEditorComponent;
  @Input()
  enabled: boolean;

  readonly autofilled: boolean;
  readonly controlType = 'portofino-rich-text';
  readonly errorState = false;
  focused: boolean;
  static nextId = 0;
  @HostBinding()
  id = `portofino-rich-text-${RichTextComponent.nextId++}`;
  @HostBinding('class.floating')
  get shouldLabelFloat() {
    return !!(this.value || this.enabled);
  }
  readonly stateChanges = new Subject<void>();
  @Input()
  get placeholder() {
    return this.editor ? this.editor.placeholder : null;
  }
  set placeholder(placeholder) {
    if(this.editor) {
      this.editor.placeholder = placeholder;
    }
    this.stateChanges.next();
  }
  get empty() {
    return !this.editor || !this.editor.content;
  }
  get disabled() {
    return !this.enabled;
  }
  set disabled(dis) {
    if(this.editor) {
      this.editor.setDisabledState(dis);
    }
    this.enabled = !dis;
  }
  @Input()
  get required() {
    return this._required;
  }
  set required(req) {
    this._required = coerceBooleanProperty(req);
    this.stateChanges.next();
  }
  private _required = false;
  get control() {
    return this.ngControl.control as FormControl;
  }
  private onChangeFn;
  private onTouchedFn;
  private _value: string;

  constructor(@Optional() @Self() public ngControl: NgControl) {
    if (this.ngControl != null) {
      // Setting the value accessor directly (instead of using providers) to avoid running into a circular import.
      this.ngControl.valueAccessor = this;
    }
  }

  ngAfterViewInit(): void {
    if(this.editor) {
      this.editor.onFocus.subscribe(() => this.focused = true);
      this.editor.onBlur.subscribe(() => this.focused = false);
      this.editor.registerOnChange(this.onChangeFn);
      this.editor.registerOnTouched(this.onTouchedFn);
      this.editor.writeValue(this._value);
      this.editor.setDisabledState(!this.enabled);
    }
  }

  onContainerClick(event: MouseEvent): void {
  }

  @HostBinding('attr.aria-describedby')
  describedBy = '';
  setDescribedByIds(ids: string[]) {
    this.describedBy = ids.join(' ');
  }

  get value() {
    return this.editor ? this.editor.content : this._value;
  }

  set value(v: string | null) {
    this._value = v;
    if(this.editor) {
      this.editor.content = v;
      this.stateChanges.next();
    }
  }

  ngOnDestroy(): void {
    this.stateChanges.complete();
  }

  registerOnChange(fn: any): void {
    this.onChangeFn = fn;
    if(this.editor) {
      this.editor.registerOnChange(fn);
    }
  }

  registerOnTouched(fn: any): void {
    this.onTouchedFn = fn;
    if(this.editor) {
      this.editor.registerOnTouched(fn);
    }
  }

  setDisabledState(isDisabled: boolean): void {
    this.disabled = isDisabled;
    if(this.editor) {
      this.editor.setDisabledState(isDisabled);
    }
  }

  writeValue(obj: any): void {
    this._value = obj;
    if(this.editor) {
      this.editor.writeValue(obj);
    }
  }
}
