import {Injectable} from '@angular/core';
import {Http} from '@angular/http';
import * as moment from 'moment';
import {Observable} from 'rxjs';

import {MassTextSendResponse} from './mass-text-send-response';
import {BaseService} from '../utils/base.service';
import {ApiKeyService} from '../utils/api-key.service';


export abstract class MassTextService extends BaseService {

  /**
   * Send a mass text to users that signed up between the given dates.
   *
   * @param startDate Send to users that signed up on or after this date (inclusive)
   * @param endDate Send to users that signed up BEFORE this date (exclusive)
   * @param template Template to use for the mass text
   * @param expectedRendered How the template is expected to look when rendered
   * @return
   */
  abstract send(startDate: moment.Moment, endDate: moment.Moment, template: string,
                expectedRendered: string): Observable<MassTextSendResponse>

}


@Injectable()
export class MassTextServiceImpl extends MassTextService {

  private sendUrl = 'http://localhost:8080/api/v1/mass-texts/send';

  constructor(http: Http, apiKeyService: ApiKeyService) {
    super(http, apiKeyService);
  }

  send(startDate: moment.Moment, endDate: moment.Moment, template: string, expectedRendered: string): Observable<MassTextSendResponse> {
    let body = {
      startDate: startDate,
      endDate: endDate,
      template: template,
      expectedRendered: expectedRendered
    };

    return this.post(this.sendUrl, body)
      .map(r => this.extractJson<MassTextSendResponse>(r))
      .catch(this.handleError());
  }

}
