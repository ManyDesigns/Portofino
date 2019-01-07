import {Injectable} from "@angular/core";
import {HttpEvent, HttpHandler, HttpInterceptor, HttpRequest} from "@angular/common/http";
import {Observable} from "rxjs";
import {PortofinoService} from "../portofino.service";

@Injectable()
export class LanguageInterceptor implements HttpInterceptor {

  constructor(protected portofino: PortofinoService) {}

  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    req = req.clone({
      setHeaders: {
        "Accept-Language": this.portofino.currentLocale.key
      }
    });
    return next.handle(req);
  }
}
