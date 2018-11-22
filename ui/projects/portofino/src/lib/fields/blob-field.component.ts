import {Component} from '@angular/core';
import {FieldComponent} from "./field.component";
import {PortofinoService} from "../portofino.service";
import {AuthenticationService} from "../security/authentication.service";

@Component({
  selector: 'portofino-blob-field',
  templateUrl: './blob-field.component.html',
  styleUrls: ['./blob-field.component.css']
})
export class BlobFieldComponent extends FieldComponent {

  constructor(portofino: PortofinoService, protected auth: AuthenticationService) {
    super(portofino, null);
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
