'use strict';
//Adapted from https://github.com/vmware/clarity/blob/d160f0c9fe5ca89388f054c02b9e18fdd18631b1/scripts/ngcc-clean.js
const fs = require('fs');

// We have to run ngcc during postinstall since it does not support parallel builds https://github.com/angular/angular/issues/32431
// ngcc pollutes package.json files in the monorepo use case https://github.com/angular/angular/issues/33395
// this script removes the breaking generated properties from ngcc

cleanNGCCPollution('./projects/portofino/package.json');

function cleanNGCCPollution(file) {
  const data = JSON.parse(fs.readFileSync(file));
  delete data['__processed_by_ivy_ngcc__'];
  delete data['scripts'];
  fs.writeFileSync(file, JSON.stringify(data, null, 2) + '\n');
}
