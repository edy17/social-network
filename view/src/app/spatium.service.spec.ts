import { TestBed } from '@angular/core/testing';

import { SpatiumService } from './spatium.service';

describe('SpatiumService', () => {
  beforeEach(() => TestBed.configureTestingModule({}));

  it('should be created', () => {
    const service: SpatiumService = TestBed.get(SpatiumService);
    expect(service).toBeTruthy();
  });
});
