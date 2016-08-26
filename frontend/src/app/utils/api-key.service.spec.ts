/* tslint:disable:no-unused-variable */

import { addProviders, async, inject } from '@angular/core/testing';
import { TestBed } from "@angular/core/testing/test_bed";

import { ApiKeyService, ApiKeyServiceImpl } from './api-key.service';


describe('Service: ApiKey', () => {

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        {provide: ApiKeyService, useClass: ApiKeyServiceImpl}
      ]
    });
  });

  it('should store the key it\'s given', inject([ApiKeyService], (service: ApiKeyService) => {
    service.setKey("1478175871287182731");

    expect(service.getKey()).toEqual("1478175871287182731");
  }));

  it('should remove the key when no new key is given', inject([ApiKeyService], (service: ApiKeyService) => {
    expect(service).toBeTruthy();
  }));

  it('should not return authenticated when it has no key', inject([ApiKeyService], (service: ApiKeyService) => {
    expect(service).toBeTruthy();
  }));

  it('should return authenticated when it has a key', inject([ApiKeyService], (service: ApiKeyService) => {
    expect(service).toBeTruthy();
  }));

});
