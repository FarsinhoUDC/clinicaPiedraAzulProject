import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'atom-badge',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './badge.component.html',
  styleUrls: ['./badge.component.css']
})
export class AtomBadgeComponent {
  @Input() variant: 'admin' | 'medico' | 'agendador' | 'paciente' | 'default' | 'success' | 'warning' | 'error' = 'default';

  get badgeClass(): string {
    return `badge-${this.variant}`;
  }
}
