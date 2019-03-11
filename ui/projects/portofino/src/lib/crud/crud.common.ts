import {SelectionOption} from "../class-accessor";

export class Configuration {
  rowsPerPage: number;
}

export class SelectionProvider {
  name: string;
  fieldNames: string[];
  displayMode: string;
  searchDisplayMode: string;
  options: SelectionOption[];
}

export class BlobFile {
  lastModified: number;
  lastModifiedDate: Date;
  name: string;
  size: number;
  type: string;
  code: string;
}
