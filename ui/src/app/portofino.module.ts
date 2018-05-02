import { BrowserModule } from '@angular/platform-browser';
import { FormsModule } from '@angular/forms';
import { NgModule } from '@angular/core';
import { NgbModule } from '@ng-bootstrap/ng-bootstrap';

import { PortofinoComponent } from './portofino.component';


@NgModule({
  declarations: [
    PortofinoComponent
  ],
  imports: [
    BrowserModule, FormsModule, NgbModule.forRoot()
  ],
  providers: [],
  bootstrap: [PortofinoComponent]
})
export class PortofinoModule { }
