import {Injectable} from "@angular/core";
import {MediaObserver} from "@angular/flex-layout";

@Injectable()
export class SidenavService {
  position: SideNavPosition = 'page';
  open = true;
  mode = 'side';

  constructor(protected media: MediaObserver) {
    let mobile = false;
    this.media.asObservable().subscribe(() => {
      if(!mobile && !this.media.isActive('gt-xs')) {
        this.mode = 'over';
        this.open = false;
        mobile = true;
      } else if(mobile && this.media.isActive('gt-xs')) {
        this.mode = 'side';
        this.open = true;
        mobile = false;
      }
    });
  }

  toggle(){
    this.open = !this.open;
  }
}

export declare type SideNavPosition = 'body' | 'page' | undefined;
