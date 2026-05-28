import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';

export interface DayOption {
  label: string;
  value: string;
}

@Component({
  selector: 'atom-day-picker',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './day-picker.component.html',
  styleUrls: ['./day-picker.component.css']
})
export class AtomDayPickerComponent {
  @Input() days: readonly DayOption[] = [];
  @Input() selectedDays: readonly string[] = [];
  @Output() onToggleDay = new EventEmitter<string>();

  isSelected(day: string): boolean {
    return this.selectedDays.includes(day);
  }

  toggle(day: string): void {
    this.onToggleDay.emit(day);
  }
}
