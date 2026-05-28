import { Directive, ElementRef, HostListener, Input, Renderer2 } from '@angular/core';

@Directive({
  selector: '[appTooltip]',
  standalone: true,
})
export class TooltipDirective {
  @Input('appTooltip') tooltipText = '';
  private tooltipEl: HTMLDivElement | null = null;

  constructor(
    private el: ElementRef<HTMLElement>,
    private renderer: Renderer2,
  ) {}

  @HostListener('mouseenter') onMouseEnter(): void {
    if (!this.tooltipText) return;
    const div = this.renderer.createElement('div');
    this.renderer.addClass(div, 'app-tooltip');
    this.renderer.setProperty(div, 'textContent', this.tooltipText);
    this.renderer.appendChild(document.body, div);

    const rect = this.el.nativeElement.getBoundingClientRect();
    const tipRect = div.getBoundingClientRect();
    const left = rect.left + rect.width / 2 - tipRect.width / 2 + window.scrollX;
    const top = rect.top - tipRect.height - 10 + window.scrollY;

    this.renderer.setStyle(div, 'left', `${left}px`);
    if (top < 0) {
      this.renderer.setStyle(div, 'top', `${rect.bottom + 10 + window.scrollY}px`);
    } else {
      this.renderer.setStyle(div, 'top', `${top}px`);
    }

    this.tooltipEl = div;
  }

  @HostListener('mouseleave') onMouseLeave(): void {
    if (this.tooltipEl) {
      this.renderer.removeChild(document.body, this.tooltipEl);
      this.tooltipEl = null;
    }
  }
}
