import { TestBed, inject } from '@angular/core/testing';

import { PortofinoService } from './portofino.service';

describe('PortofinoService', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [PortofinoService]
    });
  });

  it('should be created', inject([PortofinoService], (service: PortofinoService) => {
    expect(service).toBeTruthy();
  }));
});
