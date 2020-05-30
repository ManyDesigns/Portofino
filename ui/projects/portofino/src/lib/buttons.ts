import {ThemePalette} from "@angular/material/core";

export const BUTTONS = "__portofinoButtons__";

export class ButtonInfo {
  list: string = 'default';
  class: Function;
  methodName: string;
  propertyDescriptor: PropertyDescriptor;
  color: ThemePalette;
  action: (self, event: any | undefined) => any;
  presentIf: (self) => boolean = () => true;
  enabledIf: (self) => boolean = () => true;
  icon?: string;
  text?: string;
}

export function declareButton(info: ButtonInfo | any, target, methodName: string, descriptor: PropertyDescriptor) {
  info = Object.assign({}, new ButtonInfo(), info);
  info.class = target.constructor;
  info.methodName = methodName;
  info.propertyDescriptor = descriptor;
  info.action = (self, event) => {
    return self[methodName].call(self, event);
  };
  if(!target.hasOwnProperty(BUTTONS)) {
    const parentButtons = target[BUTTONS];
    target[BUTTONS] = {};
    if(parentButtons) {
      for(let list in parentButtons) {
        target[BUTTONS][list] = parentButtons[list].slice();
      }
    }
  }
  if(!target[BUTTONS].hasOwnProperty(info.list)) {
    target[BUTTONS][info.list] = [];
  }
  //Override
  target[BUTTONS][info.list] = target[BUTTONS][info.list].filter(i => i.methodName != info.methodName);
  target[BUTTONS][info.list].push(info);
}

export function Button(info: ButtonInfo | any) {
  return function (target, methodName: string, descriptor: PropertyDescriptor) {
    declareButton(info, target, methodName, descriptor)
  }
}

export function getButtons(component, list: string = 'default'): ButtonInfo[] | null {
  const allButtons = component[BUTTONS];
  return allButtons ? allButtons[list] : null;
}

export function getAvailableButtonLists(component) {
  const allButtons = component[BUTTONS];
  let lists = [];
  for(let l in allButtons) {
    lists.push(l);
  }
  return lists;
}

export interface WithButtons {
  getButtons(list: string): ButtonInfo[] | null;
}
