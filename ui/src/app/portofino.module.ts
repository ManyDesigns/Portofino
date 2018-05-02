import { BrowserModule } from '@angular/platform-browser';
import { FormsModule } from '@angular/forms';
import { NgModule } from '@angular/core';
import { NgbModule } from '@ng-bootstrap/ng-bootstrap';

import { PortofinoComponent } from './portofino.component';
import { CrudComponent } from './crud/crud.component';
import { PortofinoService } from './portofino.service';
import { HttpClientModule } from '@angular/common/http';


@NgModule({
  declarations: [
    PortofinoComponent,
    CrudComponent
  ],
  imports: [
    BrowserModule, FormsModule, HttpClientModule, NgbModule.forRoot()
  ],
  providers: [PortofinoService],
  bootstrap: [PortofinoComponent]
})
export class PortofinoModule { }
