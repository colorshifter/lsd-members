import { Injectable } from '@angular/core';
import { Http       } from '@angular/http';

import { BaseService } from './base.service';

export abstract class ApiKeyService extends BaseService {

  constructor(http: Http) {
    super(http);
  }

  abstract getKey(): string;

  abstract setKey(key: string): string;

  abstract isAuthenticated(): boolean;

}

/**
 * Used to access the stored API key from anywhere in the application.
 *
 * Backed by local storage.
 */
@Injectable()
export class ApiKeyServiceImpl extends ApiKeyService {

  constructor(http: Http) {
    super(http);
  }

  getKey(): string {
    return undefined;
  }

  setKey(key: string): string {
    return undefined;
  }

  isAuthenticated(): boolean {
    return undefined;
  }

}
