/* tslint:disable:no-unused-variable */

import {
  beforeEach, beforeEachProviders,
  describe, xdescribe,
  expect, it, xit,
  async, inject
} from '@angular/core/testing';
import { Title } from '@angular/platform-browser';
import { FormControl } from '@angular/forms';
import { CustomValidators } from './index';

describe('CustomValidators', () => {
  describe('email', () => {
    it('should reject undefined e-mails', () => {
      expect(CustomValidators.email(new FormControl(undefined))).toEqual({ email: true });
    });

    it('should reject blank e-mails', () => {
      expect(CustomValidators.email(new FormControl(''))).toEqual({ email: true });
    });

    it('should reject blatantly invalid e-mails', () => {
      expect(CustomValidators.email(new FormControl('foo.bar.com'))).toEqual({ email: true });
      expect(CustomValidators.email(new FormControl('rip@code'))).toEqual({ email: true });
      expect(CustomValidators.email(new FormControl('1337.hax@'))).toEqual({ email: true });
    });

    it('should accept something e-mail-like', () => {
      expect(CustomValidators.email(new FormControl('foo@bar.com'))).toBeNull(null);
    });
  });
});
