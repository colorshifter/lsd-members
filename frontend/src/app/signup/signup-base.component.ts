import { Component } from '@angular/core';
import { ROUTER_DIRECTIVES } from '@angular/router';

import { NavComponent } from '../utils';

@Component({
  moduleId: module.id,
  selector: 'router-outlet',
  templateUrl: 'signup-base.component.html',
  directives: [
    ROUTER_DIRECTIVES,
    NavComponent
  ]
})
export class SignupBaseComponent { }
