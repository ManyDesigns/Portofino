import {Component, NgModule} from '@angular/core';
import {PortofinoModule, Page, CrudComponent, NAVIGATION_COMPONENT, NavigationComponent, DefaultNavigationComponent, PortofinoComponent} from "portofino";
import {
  MatAutocompleteModule,
  MatButtonModule, MatCheckboxModule, MatDatepickerModule, MatDialogModule,
  MatFormFieldModule,
  MatIconModule, MatInputModule, MatMenuModule, MatPaginatorModule, MatRadioModule, MatSelectModule,
  MatSidenavModule,
  MatSnackBarModule,
  MatSortModule,
  MatTableModule, MatToolbarModule
} from "@angular/material";
import {BrowserModule} from "@angular/platform-browser";
import {BrowserAnimationsModule} from "@angular/platform-browser/animations";
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {QuillModule} from "ngx-quill";
import {HttpClientModule} from "@angular/common/http";
import {FlexLayoutModule} from "@angular/flex-layout";
import {MatMomentDateModule} from "@angular/material-moment-adapter";
import {FileInputAccessorModule} from "file-input-accessor";
import {TranslateModule} from "@ngx-translate/core";

@Component({
  selector: 'portofino-hello',
  template: `<p>Welcome to Portofino 5!</p>`
})
export class HelloPortofino {}

@Component({
  selector: 'custom-navigation',
  template: `<h3>Custom navigation</h3><p><a routerLink="/start">Start here</a> </p>`
})
export class CustomNavigation implements NavigationComponent {
  page: Page;
}

@Component({
  selector: 'app-root',
  template: `<portofino-app appTitle="Demo-TT" apiRoot="http://localhost:8080/demo-tt/"></portofino-app>`
})
export class AppComponent {}

@PortofinoComponent({ name: 'customcrud' })
export class CustomCrud extends CrudComponent {

  ngOnInit(): void {
    console.log("custom");
    super.ngOnInit();
    this.configuration.title = 'custom';
  }
}

@NgModule({
  declarations: [AppComponent, HelloPortofino, CustomNavigation, CustomCrud],
  providers: [
    { provide: NAVIGATION_COMPONENT, useFactory: AppModule.navigation },
  ],
  imports: [
    PortofinoModule.withRoutes([{ path: "start", component: HelloPortofino }]),
    BrowserModule, BrowserAnimationsModule, FlexLayoutModule, FormsModule, HttpClientModule, ReactiveFormsModule,
    MatAutocompleteModule, MatButtonModule, MatCheckboxModule, MatDatepickerModule, MatDialogModule, MatFormFieldModule,
    MatIconModule, MatInputModule, MatMenuModule, MatPaginatorModule, MatRadioModule, MatSelectModule, MatSidenavModule,
    MatSnackBarModule, MatSortModule, MatTableModule, MatToolbarModule, MatMomentDateModule,
    FileInputAccessorModule, QuillModule, TranslateModule.forRoot()],
  entryComponents: [ CustomNavigation, CustomCrud ],
  bootstrap: [AppComponent]
})
export class AppModule {
  static navigation() {
    return DefaultNavigationComponent
    //return CustomNavigation
  }
}
