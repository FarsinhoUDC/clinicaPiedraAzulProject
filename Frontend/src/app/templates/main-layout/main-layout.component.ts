import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { OrganismoHeaderComponent } from '../../shared/organisms/header/header.component';
import { OrganismoSidebarComponent } from '../../shared/organisms/sidebar/sidebar.component';

@Component({
  selector: 'template-main-layout',
  standalone: true,
  imports: [CommonModule, OrganismoHeaderComponent, OrganismoSidebarComponent],
  templateUrl: './main-layout.template.html',
  styleUrls: ['./main-layout.component.css']
})
export class MainLayoutComponent {
  @Input() isLoggedIn = false;
  @Input() showSidebar = false;
  @Input() brandName = 'Piedrazul';
  @Input() userLabel = '';
  @Input() userInitials = '';
  @Input() menuOpen = false;
  @Input() isAgendador = false;
  @Input() isPaciente = false;
  @Input() isAdmin = false;
  @Input() isMedico = false;

  @Output() onToggleMenu = new EventEmitter<void>();
  @Output() onMenuClose = new EventEmitter<void>();
  @Output() onLogout = new EventEmitter<void>();
  @Output() onLogin = new EventEmitter<void>();
  @Output() onRegister = new EventEmitter<void>();
}
