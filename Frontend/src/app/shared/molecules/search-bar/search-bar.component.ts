import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'molecule-search-bar',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './search-bar.component.html',
  styleUrls: ['./search-bar.component.css']
})
export class MoleculeSearchBarComponent {
  @Input() id = 'search-input';
  @Input() placeholder = 'Buscar...';
  @Input() value = '';
  @Input() showButton = false;
  @Input() buttonText = 'Buscar';
  @Input() dark = false;

  @Output() onSearch = new EventEmitter<string>();
  @Output() onSearchSubmit = new EventEmitter<string>();
  @Output() onKeyUp = new EventEmitter<KeyboardEvent>();

  onInput(event: Event): void {
    const target = event.target as HTMLInputElement;
    this.onSearch.emit(target.value);
  }
}
