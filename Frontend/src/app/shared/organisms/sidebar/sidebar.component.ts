import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { TooltipDirective } from '../../atoms/tooltip/tooltip.directive';

@Component({
  selector: 'organismo-sidebar',
  standalone: true,
  imports: [CommonModule, RouterLink, RouterLinkActive, TooltipDirective],
  templateUrl: './sidebar.component.html',
  styleUrls: ['./sidebar.component.css']
})
export class OrganismoSidebarComponent {
  @Input() brandName = 'Piedrazul';
  @Input() isAgendador = false;
  @Input() isPaciente = false;
  @Input() isAdmin = false;
  @Input() isMedico = false;
  @Input() isLoggedIn = false;
  @Input() sidebarOpen = false;

  @Output() menuClose = new EventEmitter<void>();
  @Output() logout = new EventEmitter<void>();

  onLogout(): void {
    this.logout.emit();
  }

  close(): void {
    this.menuClose.emit();
  }
}
