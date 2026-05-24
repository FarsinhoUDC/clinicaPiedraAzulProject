import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AtomButtonComponent } from '../button/button.component';

@Component({
  selector: 'atom-dialog',
  standalone: true,
  imports: [CommonModule, AtomButtonComponent],
  templateUrl: './dialog.component.html',
  styleUrls: ['./dialog.component.css']
})
export class AtomDialogComponent {
  @Input() visible = false;
  @Input() title = '';
  @Input() variant: 'default' | 'error' = 'default';
  @Input() showActions = false;
  @Input() customActions = false;
  @Input() confirmText = 'Confirmar';
  @Input() cancelText = 'Cancelar';

  @Output() onClose = new EventEmitter<void>();
  @Output() onConfirm = new EventEmitter<void>();
  @Output() onCancel = new EventEmitter<void>();
  @Output() onBackdropClick = new EventEmitter<void>();
}
