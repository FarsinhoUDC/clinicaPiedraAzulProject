import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'molecule-user-badge',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './user-badge.component.html',
  styleUrls: ['./user-badge.component.css']
})
export class MoleculeUserBadgeComponent {
  @Input() name = '';
  @Input() initials = '';
  @Input() role = '';
}
