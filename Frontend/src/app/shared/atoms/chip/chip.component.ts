import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'atom-chip',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './chip.component.html',
  styleUrls: ['./chip.component.css']
})
export class AtomChipComponent {
  @Input() active = false;
  @Input() disabled = false;
  @Input() variant: 'pill' | 'square' = 'pill';
  @Input() size: 'sm' | 'md' = 'md';
  @Input() type: 'button' | 'submit' = 'button';
  @Output() onClick = new EventEmitter<MouseEvent>();
}
