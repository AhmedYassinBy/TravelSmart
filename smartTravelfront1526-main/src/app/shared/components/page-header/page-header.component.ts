// src/app/shared/components/page-header/page-header.component.ts
import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { SourceBadgeComponent } from '../source-badge/source-badge.component';

@Component({
    selector: 'app-page-header',
    standalone: true,
    imports: [CommonModule, SourceBadgeComponent],
    template: `
    <div class="mb-8">
      <div class="flex flex-col lg:flex-row lg:items-center justify-between gap-4">
        <div class="space-y-2">
          <div class="flex items-center gap-3">
            <span class="text-4xl">{{ icon }}</span>
            <div>
              <h1 class="text-3xl font-black text-slate-800 tracking-tight">{{ title }}</h1>
              <p class="text-slate-500 text-sm mt-1">{{ subtitle }}</p>
            </div>
          </div>
        </div>
        
        <div class="flex items-center gap-4">
          <app-source-badge [source]="source"></app-source-badge>
          <ng-content select="[headerActions]"></ng-content>
        </div>
      </div>
    </div>
  `
})
export class PageHeaderComponent {
    @Input() icon = '📋';
    @Input() title = 'Page Title';
    @Input() subtitle = 'Page description';
    @Input() source: 'api' | 'database' = 'database';
}
