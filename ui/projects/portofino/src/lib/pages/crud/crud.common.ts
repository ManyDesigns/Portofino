import {SelectionOption} from "../../class-accessor";
import {PageChild} from "../../page";

export class Configuration {
  detailChildren: PageChild[];
  openDetailInSamePageWhenEmbedded: boolean;
  rowsPerPage: number;
}

export class SelectionProvider {
  name: string;
  fieldNames: string[];
  displayMode: string;
  searchDisplayMode: string;
  options: SelectionOption[];
}

