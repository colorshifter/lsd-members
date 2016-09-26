import {Component, OnInit, OnDestroy} from '@angular/core';
import {Subscription} from 'rxjs';
import {ActivatedRoute} from '@angular/router';
import * as moment from 'moment';

import {MemberEditService, Member} from './member-edit.service';
import {MemberViewService} from "../member-view/member-view.service";
import {FormBuilder} from "@angular/forms";

@Component({
  selector: 'app-member-edit',
  templateUrl: 'member-edit.component.html',
  styleUrls: ['member-edit.component.sass']
})
export class MemberEditComponent implements OnInit, OnDestroy {

  private displayMemberSub: Subscription;

  private member: Member;

  constructor(private builder: FormBuilder,
              private service: MemberEditService, // TODO: Refactor as one MemberService
              private viewService: MemberViewService,
              private route: ActivatedRoute) {
    this.ctrlDate = new FormControl(moment().format('YYYY-MM-DD'), Validators.required);
    this.ctrlOrganiser = new FormControl('', Validators.required);
    this.ctrlSecondaryOrganiser = new FormControl('');
    this.ctrlNumSpaces = new FormControl('8', Validators.required);
  }

  showMember(uuid: string) {
    this.viewService.getMember(uuid).subscribe(member => this.member = member);
  }

  ngOnInit() {
    this.displayMemberSub = this.route.params
      .subscribe(params => {
        let uuid: string = params['uuid'];

        this.showMember(uuid);
      });
  }

  ngOnDestroy() {
    this.displayMemberSub.unsubscribe();
  }

  parseStatus(statusId: number) {
    switch (statusId) {
      case 0: return 'Pending';
      case 1: return 'Sent';
      case 2: return 'Error';
      case 3: return 'Received';
      default: return 'Unknown (' + statusId + ')';
    }
  }

}
