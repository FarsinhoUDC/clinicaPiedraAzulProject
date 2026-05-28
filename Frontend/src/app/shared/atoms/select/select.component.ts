import { Component, Input, Output, EventEmitter, forwardRef, ViewChild, ElementRef, AfterViewInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ControlValueAccessor, NG_VALUE_ACCESSOR, ReactiveFormsModule } from '@angular/forms';

export interface SelectOption {
  label: string;
  value: string | number;
}

@Component({
  selector: 'atom-select',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './select.component.html',
  styleUrls: ['./select.component.css'],
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      useExisting: forwardRef(() => AtomSelectComponent),
      multi: true
    }
  ]
})
export class AtomSelectComponent implements ControlValueAccessor, AfterViewInit {
  @Input() id = '';
  @Input() label = '';
  @Input() placeholder = 'Seleccione una opción';
  @Input() required = false;
  @Input() disabled = false;
  @Input() options: readonly SelectOption[] = [];
  @Input() hasError = false;
  @Input() errorMessage = '';
  @Output() onSelectChange = new EventEmitter<string | number>();

  @ViewChild('selectEl') private selectRef!: ElementRef<HTMLSelectElement>;
  private pendingValue: string | number | null = null;

  value: string | number = '';

  private onChange: (value: string | number) => void = () => {};
  private onTouched: () => void = () => {};

  handleSelectChange(event: Event): void {
    const target = event.target as HTMLSelectElement;
    this.value = target.value;
    this.onChange(this.value);
    this.onSelectChange.emit(this.value);
  }

  onBlur(): void {
    this.onTouched();
  }

  private applyValue(): void {
    if (!this.selectRef) return;
    this.selectRef.nativeElement.value = String(this.value);
  }

  writeValue(value: string | number): void {
    this.value = value ?? '';
    if (this.selectRef) {
      this.applyValue();
    } else {
      this.pendingValue = this.value;
    }
  }

  ngAfterViewInit(): void {
    if (this.pendingValue !== null) {
      this.value = this.pendingValue;
      this.pendingValue = null;
      this.applyValue();
    }
  }

  registerOnChange(fn: (value: string | number) => void): void {
    this.onChange = fn;
  }

  registerOnTouched(fn: () => void): void {
    this.onTouched = fn;
  }

  setDisabledState(isDisabled: boolean): void {
    this.disabled = isDisabled;
  }

  trackByOption(_: number, opt: SelectOption): string | number {
    return opt.value;
  }
}
