import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';

export interface MetricCard {
  label: string;
  value: number;
  variant?: 'default' | 'warning' | 'danger';
}

@Component({
  selector: 'molecule-metric-cards',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './metric-cards.component.html',
  styleUrls: ['./metric-cards.component.css']
})
export class MoleculeMetricCardsComponent {
  @Input() cards: MetricCard[] = [];
}
