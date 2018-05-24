import { TestBed, async } from '@angular/core/testing';
import { PortofinoComponent } from './portofino.component';
describe('PortofinoComponent', () => {
  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [
        PortofinoComponent
      ],
    }).compileComponents();
  }));
  it('should create the app', async(() => {
    const fixture = TestBed.createComponent(PortofinoComponent);
    const app = fixture.debugElement.componentInstance;
    expect(app).toBeTruthy();
  }));
  it(`should have as title 'app'`, async(() => {
    const fixture = TestBed.createComponent(PortofinoComponent);
    const app = fixture.debugElement.componentInstance;
    expect(app.title).toEqual('Portofino UI');
  }));
  it('should render title in a h1 tag', async(() => {
    const fixture = TestBed.createComponent(PortofinoComponent);
    fixture.detectChanges();
    const compiled = fixture.debugElement.nativeElement;
    expect(compiled.querySelector('h1').textContent).toContain('Portofino UI');
  }));
});
