import {BrowserModule} from '@angular/platform-browser';
import {BrowserAnimationsModule} from '@angular/platform-browser/animations';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {ErrorHandler, ModuleWithProviders, NgModule, Type} from '@angular/core';
import {
  DefaultFooterComponent,
  DefaultToolbarComponent, FOOTER_COMPONENT, FooterDirective,
  PortofinoAppComponent,
  TOOLBAR_COMPONENT,
  ToolbarDirective
} from './portofino-app.component';
import {
  BulkEditComponentHolder,
  CreateComponentHolder,
  CrudComponent,
  DetailComponentHolder,
  SearchComponentHolder
} from './pages/crud/crud.component';
import {
  ApiVersionInterceptor,
  LOCALE_STORAGE_SERVICE,
  LOCALES,
  PortofinoService,
  ProgressInterceptor
} from './portofino.service';
import {HTTP_INTERCEPTORS, HttpClientModule} from '@angular/common/http';
import {
  AuthenticationInterceptor,
  AuthenticationService, AuthenticationStrategy, TOKEN_STORAGE_SERVICE
} from "./security/authentication.service";
import {LoginComponent} from './security/login/login.component';
import {SearchFieldComponent} from './pages/crud/search/search-field.component';
import { MatAutocompleteModule } from '@angular/material/autocomplete';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatDialogModule } from '@angular/material/dialog';
import { MatDividerModule } from '@angular/material/divider';
import { MatExpansionModule } from '@angular/material/expansion';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatGridListModule } from '@angular/material/grid-list';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatListModule } from '@angular/material/list';
import { MatMenuModule } from '@angular/material/menu';
import { MatPaginatorModule } from '@angular/material/paginator';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatRadioModule } from '@angular/material/radio';
import { MatSelectModule } from '@angular/material/select';
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatSnackBarModule } from '@angular/material/snack-bar';
import { MatSortModule } from '@angular/material/sort';
import { MatStepperModule } from '@angular/material/stepper';
import { MatTableModule } from '@angular/material/table';
import { MatTabsModule } from '@angular/material/tabs';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatTreeModule } from '@angular/material/tree';
import {MatLuxonDateModule} from "@angular/material-luxon-adapter";
import {FlexLayoutModule} from "@angular/flex-layout";
import {ExtraOptions, RouterModule, Routes} from '@angular/router';
import {SearchComponent} from './pages/crud/search/search.component';
import {BreadcrumbsComponent} from "./breadcrumbs/breadcrumbs.component";
import {DetailComponent} from './pages/crud/detail/detail.component';
import {CreateComponent} from './pages/crud/detail/create.component';
import {MainPageDirective, NavigationDirective} from './content.directive';
import {
  DefaultNavigationComponent,
  PageLayout,
  NAVIGATION_COMPONENT,
  PageHeader, PageService, TemplatesComponent, PageFactoryComponent, PageSettingsPanelComponent
} from './page';
import {BulkEditComponent} from "./pages/crud/bulk/bulk-edit.component";
import {BlobFieldComponent} from "./fields/blob-field.component";
import {FileInputAccessorModule} from "file-input-accessor";
import {ManyToManyComponent} from './pages/many-to-many/many-to-many.component';
import {ButtonComponent, ButtonsComponent} from "./button.component";
import {QuillModule} from "ngx-quill";
import {DynamicFormComponentDirective, FormComponent} from "./form";
import {TranslateModule} from "@ngx-translate/core";
import {ContentComponent} from "./content.component";
import {
  NOTIFICATION_HANDLERS,
  NotificationDispatcher,
  NotificationErrorHandler,
  NotificationInterceptor,
  NotificationService, NotificationsHolder
} from "./notifications/notification.services";
import {ScrollingModule} from "@angular/cdk/scrolling";
import { NgxdModule } from '@ngxd/core';
import {FieldFactory, FieldFactoryComponent} from "./fields/field.factory";
import {DateTimeValueAccessor, DateTimeFieldComponent} from "./fields/date-time-field.component";
import {BooleanFieldComponent} from "./fields/boolean-field.component";
import {NumberFieldComponent} from "./fields/number-field.component";
import {TextFieldComponent} from "./fields/text-field.component";
import {SelectFieldComponent} from "./fields/select-field.component";
import {LanguageSelectorComponent} from "./i18n/language.selector.component";
import {LanguageInterceptor} from "./i18n/language.interceptor";
import {CreatePageComponent, DeletePageComponent, MovePageComponent, PageCrudService} from "./administration/page-crud.service";
import {
  GenericCrudComponent,
  MailSettingsComponent,
  PermissionsComponent,
  SettingsComponent,
  UpstairsComponent
} from "./administration/upstairs.component";
import {ConnectionsComponent} from "./administration/connections.component";
import {WizardComponent} from "./administration/wizard.component";
import {TablesComponent} from "./administration/tables.component";
import {ActionsComponent, CreateActionComponent, GenericPage} from "./administration/actions.component";
import {TRANSLATIONS_EN} from "./i18n/en";
import {TRANSLATIONS_ES} from "./i18n/es";
import {TRANSLATIONS_IT} from "./i18n/it";
import {ChangePasswordComponent} from "./security/login/change-password.component";
import {SignupComponent} from "./security/login/signup.component";
import {ForgottenPasswordComponent} from "./security/login/forgotten-password.component";
import {ResetPasswordComponent} from "./security/login/reset-password.component";
import {SidenavService} from "./sidenav.service";
import {RichTextComponent} from "./fields/rich-text.component";
import {TextPageComponent} from "./pages/text/text.component";
import {VarDirective} from "./var.directive";
import {MatTooltipModule} from "@angular/material/tooltip";
import {MatBadgeModule} from "@angular/material/badge";
import {LocalStorageService} from "./storage/storage.services";
import {CustomPageComponent} from "./pages/custom/custom.component";
import {
  CHANGE_PASSWORD_COMPONENT,
  LOGIN_COMPONENT,
  RESET_PASSWORD_COMPONENT,
  InAppAuthenticationStrategy,
} from "./security/login/in-app-authentication-strategy";
import {DragDropModule} from "@angular/cdk/drag-drop";

@NgModule({
    declarations: [
        FieldFactoryComponent, BlobFieldComponent, BooleanFieldComponent, DateTimeValueAccessor, DateTimeFieldComponent,
        NumberFieldComponent, RichTextComponent, SelectFieldComponent, TextFieldComponent,
        FormComponent, DynamicFormComponentDirective
    ],
    imports: [
        BrowserModule, BrowserAnimationsModule, ReactiveFormsModule, FormsModule, FlexLayoutModule,
        MatAutocompleteModule, MatCheckboxModule, MatDatepickerModule, MatFormFieldModule, MatIconModule, MatInputModule,
        MatLuxonDateModule, MatRadioModule, MatSelectModule, FileInputAccessorModule, NgxdModule, QuillModule,
        TranslateModule.forChild()
    ],
    providers: [FieldFactory],
    exports: [
        FieldFactoryComponent, BlobFieldComponent, BooleanFieldComponent, DateTimeValueAccessor, DateTimeFieldComponent,
        NumberFieldComponent, RichTextComponent, SelectFieldComponent, TextFieldComponent,
        FormComponent,
        MatAutocompleteModule, MatCheckboxModule, MatDatepickerModule, MatFormFieldModule, MatIconModule, MatInputModule,
        MatLuxonDateModule, MatRadioModule, MatSelectModule
    ]
})
export class PortofinoFormsModule {}

@NgModule({
    declarations: [
        PageLayout, ButtonComponent, ButtonsComponent,
        ContentComponent, PageFactoryComponent, PageHeader, MainPageDirective, TemplatesComponent,
        LanguageSelectorComponent,
        NavigationDirective, DefaultNavigationComponent, VarDirective,
        ToolbarDirective, DefaultToolbarComponent, FooterDirective, DefaultFooterComponent, BreadcrumbsComponent,
        CreatePageComponent, DeletePageComponent, MovePageComponent, PageSettingsPanelComponent
    ],
    imports: [
        BrowserModule, BrowserAnimationsModule, ReactiveFormsModule, FormsModule, FlexLayoutModule,
        HttpClientModule, PortofinoFormsModule,
        MatBadgeModule, MatButtonModule, MatCardModule, MatDialogModule, MatDividerModule, MatExpansionModule,
        MatMenuModule, MatProgressBarModule, MatSidenavModule, MatSnackBarModule, MatProgressSpinnerModule,
        MatStepperModule, MatTabsModule, MatTableModule, MatTreeModule, MatListModule, MatToolbarModule,
        NgxdModule, RouterModule.forChild([]), ScrollingModule, TranslateModule.forChild()
    ],
    providers: [PortofinoService, AuthenticationService, PageService, PageCrudService, SidenavService],
    exports: [
        PageLayout, ButtonComponent, ButtonsComponent, BreadcrumbsComponent,
        ContentComponent, PageFactoryComponent, PageHeader, TemplatesComponent,
        DefaultNavigationComponent, NavigationDirective, DefaultToolbarComponent, ToolbarDirective,
        DefaultFooterComponent, FooterDirective,
        MatButtonModule, MatCardModule, MatDialogModule, MatDividerModule, MatExpansionModule, MatMenuModule,
        MatProgressBarModule, MatSidenavModule, MatSnackBarModule, MatProgressSpinnerModule, MatStepperModule,
        MatTabsModule, MatTableModule, MatTreeModule, MatListModule, MatToolbarModule
    ]
})
export class PortofinoPagesModule {}

@NgModule({
    declarations: [
        CrudComponent, SearchFieldComponent, SearchComponent, DetailComponent, CreateComponent, BulkEditComponent,
        SearchComponentHolder, DetailComponentHolder, CreateComponentHolder, BulkEditComponentHolder,
        ManyToManyComponent
    ],
    imports: [
        BrowserModule, BrowserAnimationsModule, ReactiveFormsModule, FormsModule, FlexLayoutModule,
        HttpClientModule, PortofinoFormsModule, PortofinoPagesModule,
        MatPaginatorModule, MatSortModule,
        NgxdModule, RouterModule.forChild([]), ScrollingModule, TranslateModule
    ],
    providers: [],
    exports: [
        CrudComponent, SearchFieldComponent, SearchComponent, DetailComponent, CreateComponent, BulkEditComponent,
        SearchComponentHolder, DetailComponentHolder, CreateComponentHolder, BulkEditComponentHolder,
        ManyToManyComponent,
        MatPaginatorModule, MatSortModule
    ]
})
export class PortofinoCrudModule {}

@NgModule({
    declarations: [
        UpstairsComponent,
        ActionsComponent, GenericCrudComponent, GenericPage, CreateActionComponent,
        ConnectionsComponent, MailSettingsComponent, PermissionsComponent, SettingsComponent, TablesComponent,
        WizardComponent
    ],
    imports: [
        BrowserModule, BrowserAnimationsModule, DragDropModule, ReactiveFormsModule, FormsModule, FlexLayoutModule,
        HttpClientModule, PortofinoFormsModule, PortofinoCrudModule, PortofinoPagesModule,
        MatGridListModule, MatTooltipModule, NgxdModule, RouterModule.forChild([]), TranslateModule.forChild()
    ],
    providers: [],
    exports: [UpstairsComponent]
})
export class PortofinoUpstairsModule {}

@NgModule({
    declarations: [
        PortofinoAppComponent,
        CustomPageComponent, LoginComponent, SignupComponent, ChangePasswordComponent, ForgottenPasswordComponent,
        ResetPasswordComponent, TextPageComponent
    ],
    imports: [
        BrowserModule, BrowserAnimationsModule, ReactiveFormsModule, FormsModule, FlexLayoutModule,
        HttpClientModule, PortofinoFormsModule, PortofinoPagesModule, PortofinoCrudModule,
        NgxdModule, RouterModule.forChild([]), ScrollingModule, TranslateModule
    ],
    providers: [
        { provide: AuthenticationStrategy, useClass: InAppAuthenticationStrategy },
        //These are factories to avoid circular dependencies
        { provide: LOGIN_COMPONENT, useFactory: PortofinoModule.loginComponent },
        { provide: CHANGE_PASSWORD_COMPONENT, useFactory: PortofinoModule.changePasswordComponent },
        { provide: RESET_PASSWORD_COMPONENT, useFactory: PortofinoModule.resetPasswordComponent },
        { provide: NAVIGATION_COMPONENT, useFactory: PortofinoModule.navigationComponent },
        { provide: TOOLBAR_COMPONENT, useFactory: PortofinoModule.toolbarComponent },
        { provide: FOOTER_COMPONENT, useFactory: PortofinoModule.footerComponent },
        { provide: HTTP_INTERCEPTORS, useClass: ApiVersionInterceptor, multi: true },
        { provide: HTTP_INTERCEPTORS, useClass: AuthenticationInterceptor, multi: true },
        { provide: HTTP_INTERCEPTORS, useClass: LanguageInterceptor, multi: true },
        { provide: HTTP_INTERCEPTORS, useClass: NotificationInterceptor, multi: true },
        { provide: HTTP_INTERCEPTORS, useClass: ProgressInterceptor, multi: true },
        { provide: TOKEN_STORAGE_SERVICE, useClass: LocalStorageService },
        { provide: LOCALE_STORAGE_SERVICE, useClass: LocalStorageService },
        { provide: LOCALES, useValue: [
                { key: 'en', name: 'English', translations: TRANSLATIONS_EN },
                { key: 'it', name: 'Italiano', translations: TRANSLATIONS_IT },
                { key: 'es', name: 'Español', translations: TRANSLATIONS_ES }
            ] },
        NotificationsHolder,
        { provide: NOTIFICATION_HANDLERS, useExisting: NotificationsHolder, multi: true },
        { provide: NotificationService, useClass: NotificationDispatcher },
        { provide: ErrorHandler, useClass: NotificationErrorHandler }
    ],
    exports: [
        PortofinoAppComponent, PageLayout, ButtonComponent, ButtonsComponent,
        LoginComponent, SignupComponent, ChangePasswordComponent, ForgottenPasswordComponent, ResetPasswordComponent,
        TemplatesComponent, ContentComponent, PageFactoryComponent, PageHeader,
        DefaultNavigationComponent, DefaultToolbarComponent, DefaultFooterComponent,
        CrudComponent, SearchFieldComponent, SearchComponent, DetailComponent, CreateComponent, BulkEditComponent,
        SearchComponentHolder, DetailComponentHolder, CreateComponentHolder, BulkEditComponentHolder,
        CustomPageComponent,
        ManyToManyComponent, TextPageComponent
    ]
})
export class PortofinoModule {
  static changePasswordComponent() {
    return ChangePasswordComponent;
  }

  static resetPasswordComponent() {
    return ResetPasswordComponent;
  }

  static loginComponent() {
    return LoginComponent;
  }

  static navigationComponent() {
    return DefaultNavigationComponent;
  }

  static toolbarComponent() {
    return DefaultToolbarComponent;
  }

  static footerComponent() {
    return DefaultFooterComponent;
  }

  public static forRoot(): (ModuleWithProviders<RouterModule> | PortofinoModule)[] {
    return PortofinoModule.withRoutes(PortofinoModule.defaultRoutes(), PortofinoModule.defaultRouterConfig())
  }

  public static withRoutes(routes: Routes, config: ExtraOptions = {}): (ModuleWithProviders<RouterModule>|Type<PortofinoModule>)[] {
    return [RouterModule.forRoot(routes, config), PortofinoModule];
  }

  public static defaultRoutes(): Routes {
    return [{ path: "**", component: ContentComponent}];
  }

  public static defaultRouterConfig(): ExtraOptions {
    return { onSameUrlNavigation: "reload" };
  }
}
