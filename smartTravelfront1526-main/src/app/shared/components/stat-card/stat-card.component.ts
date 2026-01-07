// src/app/shared/components/stat-card/stat-card.component.ts
import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
    selector: 'app-stat-card',
    standalone: true,
    imports: [CommonModule],
    template: `
    <div class="relative overflow-hidden bg-white rounded-2xl shadow-lg border border-slate-100 p-6 hover:shadow-xl transition-all duration-300 group">
      <!-- Background decoration -->
      <div class="absolute top-0 right-0 w-32 h-32 opacity-5 transform translate-x-8 -translate-y-8 group-hover:scale-110 transition-transform duration-500">
        <span class="text-8xl">{{ icon }}</span>
      </div>
      
      <div class="relative z-10">
        <div class="flex items-center justify-between mb-4">
          <div class="w-12 h-12 rounded-xl flex items-center justify-center text-2xl"
               [ngClass]="iconContainerClass">
            {{ icon }}
          </div>
          <div *ngIf="trend" class="flex items-center gap-1 text-sm font-semibold"
               [ngClass]="trendUp ? 'text-emerald-600' : 'text-red-600'">
            <span>{{ trendUp ? '↑' : '↓' }}</span>
            <span>{{ trend }}</span>
          </div>
        </div>
        
        <div class="space-y-1">
          <p class="text-sm font-medium text-slate-500 uppercase tracking-wide">{{ title }}</p>
          <p class="text-3xl font-black text-slate-800">{{ value | number }}</p>
          <p *ngIf="subtitle" class="text-xs text-slate-400">{{ subtitle }}</p>
        </div>
      </div>
    </div>
  `
})
export class StatCardComponent {
    @Input() icon = '📊';
    @Input() title = 'Statistic';
    @Input() value = 0;
    @Input() subtitle?: string;
    @Input() trend?: string;
    @Input() trendUp = true;
    @Input() gradientFrom = 'blue-500';
    @Input() gradientTo = 'indigo-600';
    @Input() color: 'blue' | 'emerald' | 'purple' | 'amber' | 'rose' | 'orange' = 'blue';

    get iconContainerClass(): string {
        const colors: Record<string, string> = {
            blue: 'bg-blue-100 text-blue-600',
            emerald: 'bg-emerald-100 text-emerald-600',
            purple: 'bg-purple-100 text-purple-600',
            amber: 'bg-amber-100 text-amber-600',
            rose: 'bg-rose-100 text-rose-600',
            orange: 'bg-orange-100 text-orange-600'
        };
        return colors[this.color] || colors['blue'];
    }
}
