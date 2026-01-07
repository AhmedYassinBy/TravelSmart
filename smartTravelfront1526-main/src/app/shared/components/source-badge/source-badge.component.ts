// src/app/shared/components/source-badge/source-badge.component.ts
import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
    selector: 'app-source-badge',
    standalone: true,
    imports: [CommonModule],
    template: `
    <span class="inline-flex items-center gap-1.5 px-3 py-1 text-xs font-bold rounded-full transition-all duration-200"
          [ngClass]="badgeClasses">
      <span class="w-2 h-2 rounded-full animate-pulse" [ngClass]="dotClasses"></span>
      {{ label }}
    </span>
  `
})
export class SourceBadgeComponent {
    @Input() source: 'api' | 'database' = 'api';

    get label(): string {
        return this.source === 'api' ? 'External API' : 'Database';
    }

    get badgeClasses(): string {
        return this.source === 'api'
            ? 'bg-gradient-to-r from-purple-100 to-blue-100 text-purple-700 border border-purple-200'
            : 'bg-gradient-to-r from-emerald-100 to-teal-100 text-emerald-700 border border-emerald-200';
    }

    get dotClasses(): string {
        return this.source === 'api' ? 'bg-purple-500' : 'bg-emerald-500';
    }
}
