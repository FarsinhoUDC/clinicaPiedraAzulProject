import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'organismo-header',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.css']
})
export class OrganismoHeaderComponent {
  @Input() isLoggedIn = false;
  @Input() userLabel = '';
  @Input() userInitials = '';
  @Output() toggleMenu = new EventEmitter<void>();
  @Output() logout = new EventEmitter<void>();
  @Output() login = new EventEmitter<void>();
  @Output() register = new EventEmitter<void>();
}
