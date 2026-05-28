import { Injectable } from '@angular/core';
import { Subject } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class ModalService {
  private readonly openRegisterSource = new Subject<void>();
  openRegister$ = this.openRegisterSource.asObservable();

  openRegister(): void {
    this.openRegisterSource.next();
  }
}
