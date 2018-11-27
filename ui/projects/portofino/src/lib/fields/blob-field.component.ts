import {Component, Host, Inject, Input, Optional, SkipSelf} from '@angular/core';
import {FIELD_FACTORY, FieldComponent} from "./field.component";
import {PortofinoService} from "../portofino.service";
import {AuthenticationService} from "../security/authentication.service";
import {ControlContainer} from "@angular/forms";

@Component({
  selector: 'portofino-blob-field',
  templateUrl: './blob-field.component.html'
})
export class BlobFieldComponent extends FieldComponent {

  @Input()
  objectUrl: string;

  constructor(portofino: PortofinoService, @Inject(FIELD_FACTORY) factory,
              @Optional() @Host() @SkipSelf() controlContainer: ControlContainer,
              protected auth: AuthenticationService) {
    super(portofino, factory, controlContainer);
  }

  deleteBlob() {
    this.control.reset(null);
  }

  get blobUrl() {
    const blobUrl = this.objectUrl + '/:blob/' + this.property.name;
    if(this.portofino.localApiPath) {
      return `${this.portofino.localApiPath}/blobs?path=${encodeURIComponent(blobUrl)}` +
             `&token=${encodeURIComponent(this.auth.jsonWebToken)}`;
    } else {
      return blobUrl;
    }
  }

}
