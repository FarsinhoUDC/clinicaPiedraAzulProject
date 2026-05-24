import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'atom-button',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './button.component.html',
  styleUrls: ['./button.component.css']
})
export class AtomButtonComponent {
  @Input() type: 'button' | 'submit' = 'button';
  @Input() variant: 'primary' | 'secondary' | 'danger' | 'success' | 'ghost' | 'gradient' = 'primary';
  @Input() size: 'sm' | 'md' | 'lg' = 'md';
  @Input() block = false;
  @Input() disabled = false;
  @Output() onClick = new EventEmitter<Event>();

  get btnClass(): string {
    return [
      `btn-${this.variant}`,
      this.size !== 'md' ? `btn-${this.size}` : '',
      this.block ? 'btn-block' : ''
    ].filter(Boolean).join(' ');
  }
}
