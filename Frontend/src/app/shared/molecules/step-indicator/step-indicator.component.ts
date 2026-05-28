import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';

export interface Step {
  index: number;
  title: string;
}

@Component({
  selector: 'molecule-step-indicator',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './step-indicator.component.html',
  styleUrls: ['./step-indicator.component.css']
})
export class MoleculeStepIndicatorComponent {
  @Input() steps: Step[] = [];
  @Input() currentStep = 1;
  @Input() variant: 'numbered' | 'dots' = 'numbered';
}
