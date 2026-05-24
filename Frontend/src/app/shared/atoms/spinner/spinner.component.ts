import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'atom-spinner',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './spinner.component.html',
  styleUrls: ['./spinner.component.css']
})
export class AtomSpinnerComponent {
  @Input() size: 'sm' | 'md' | 'lg' = 'md';
  @Input() text = '';
}
