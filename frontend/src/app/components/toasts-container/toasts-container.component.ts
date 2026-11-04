import { Component, HostBinding } from '@angular/core';
import {
  NotificationService,
  Toast,
} from '../../services/notification.service';
import { Observable } from 'rxjs';

@Component({
  selector: 'app-toasts-container',
  templateUrl: './toasts-container.component.html',
})
export class ToastsContainerComponent {
  @HostBinding('class.ngb-toasts') classNgbToasts = 'true';

  public toasts$: Observable<Toast[]>;

  constructor(private notificationService: NotificationService) {
    this.toasts$ = notificationService.toasts$;
  }

  remove(toast: Toast): void {
    this.notificationService.remove(toast);
  }
}
