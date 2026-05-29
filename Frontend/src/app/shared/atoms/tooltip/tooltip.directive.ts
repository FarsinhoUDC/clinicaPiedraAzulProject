import { Directive, ElementRef, HostListener, Input, Renderer2 } from '@angular/core';

@Directive({
  selector: '[appTooltip]',
  standalone: true,
})
export class TooltipDirective {
  @Input('appTooltip') tooltipText = '';
  private tooltipEl: HTMLDivElement | null = null;
  private readonly MARGIN = 12;

  constructor(
    private el: ElementRef<HTMLElement>,
    private renderer: Renderer2,
  ) {}

  private show(): void {
    if (!this.tooltipText || this.tooltipEl) return;
    const div = this.renderer.createElement('div');
    this.renderer.addClass(div, 'app-tooltip');
    this.renderer.setProperty(div, 'textContent', this.tooltipText);
    this.renderer.appendChild(document.body, div);

    void div.offsetWidth;

    const rect = this.el.nativeElement.getBoundingClientRect();
    const tipRect = div.getBoundingClientRect();

    const vw = window.innerWidth;
    const vh = window.innerHeight;

    let left = rect.left + rect.width / 2 - tipRect.width / 2;
    let top = rect.top - tipRect.height - 10;

    if (top < this.MARGIN) {
      top = rect.bottom + 10;
    }

    if (top + tipRect.height > vh - this.MARGIN && rect.top - tipRect.height - 10 >= this.MARGIN) {
      top = rect.top - tipRect.height - 10;
    }

    top = Math.max(this.MARGIN, Math.min(top, vh - tipRect.height - this.MARGIN));

    const maxLeft = vw - tipRect.width - this.MARGIN;
    left = Math.max(this.MARGIN, Math.min(left, maxLeft));

    this.renderer.setStyle(div, 'top', `${top}px`);
    this.renderer.setStyle(div, 'left', `${left}px`);

    this.tooltipEl = div;
  }

  private hide(): void {
    if (this.tooltipEl) {
      this.renderer.removeChild(document.body, this.tooltipEl);
      this.tooltipEl = null;
    }
  }

  @HostListener('mouseenter') onMouseEnter(): void { this.show(); }
  @HostListener('mouseleave') onMouseLeave(): void { this.hide(); }

  @HostListener('touchstart', ['$event']) onTouchStart(event: TouchEvent): void {
    this.show();
  }
  @HostListener('touchend') onTouchEnd(): void { this.hide(); }
  @HostListener('touchcancel') onTouchCancel(): void { this.hide(); }
}
