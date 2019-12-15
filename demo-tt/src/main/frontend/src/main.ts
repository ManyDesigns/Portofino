import { enableProdMode } from '@angular/core';
import { platformBrowserDynamic } from '@angular/platform-browser-dynamic';
import "@angular/compiler"; //Otherwise: Error: "Angular JIT compilation failed: '@angular/compiler' not loaded!
//  - JIT compilation is discouraged for production use-cases! Consider AOT mode instead.
//  - Did you bootstrap using '@angular/platform-browser-dynamic' or '@angular/platform-server'?
//  - Alternatively provide the compiler with 'import "@angular/compiler";' before bootstrapping."
//TODO can we avoid this?

import { DemoTTAppModule } from './app/app.module';
import { environment } from './environments/environment';

if (environment.production) {
  enableProdMode();
}

platformBrowserDynamic().bootstrapModule(DemoTTAppModule)
  .catch(err => console.log(err));
