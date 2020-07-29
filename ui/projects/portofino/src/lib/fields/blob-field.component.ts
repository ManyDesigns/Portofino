import {Component, Host, Input, Optional, SkipSelf} from '@angular/core';
import {FieldComponent} from "./field.component";
import {PortofinoService} from "../portofino.service";
import {AuthenticationService} from "../security/authentication.service";
import {ControlContainer} from "@angular/forms";
import {DomSanitizer} from "@angular/platform-browser";

@Component({
  selector: 'portofino-blob-field',
  templateUrl: '../../../assets/fields/blob-field.component.html'
})
export class BlobFieldComponent extends FieldComponent {

  @Input()
  objectUrl: string;

  constructor(protected portofino: PortofinoService, protected auth: AuthenticationService,
              @Optional() @Host() @SkipSelf() controlContainer: ControlContainer,
              protected sanitizer: DomSanitizer) {
    super(controlContainer);
  }

  deleteBlob() {
    this.control.reset(null);
  }

  get blobUrl() {
    const blobUrl = this.objectUrl + '/:blob/' + this.property.name;
    if(this.portofino.localApiPath) {
      const localApiUrl = `${this.portofino.localApiPath}/blobs?path=${encodeURIComponent(blobUrl)}`;
      let unsafeUrl = localApiUrl + (this.auth.jsonWebToken ? `&token=${encodeURIComponent(this.auth.jsonWebToken)}` : '');
      return this.sanitizer.bypassSecurityTrustUrl(unsafeUrl);
    } else {
      return blobUrl;
    }
  }

}
