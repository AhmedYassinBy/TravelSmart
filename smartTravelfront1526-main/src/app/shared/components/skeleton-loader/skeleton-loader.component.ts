// src/app/shared/components/skeleton-loader/skeleton-loader.component.ts
import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
    selector: 'app-skeleton-loader',
    standalone: true,
    imports: [CommonModule],
    template: `
    <div class="bg-white rounded-2xl shadow-lg border border-slate-100 overflow-hidden">
      <!-- Header Skeleton -->
      <div class="p-6 border-b border-slate-100 bg-slate-50/50">
        <div class="flex items-center justify-between">
          <div class="flex items-center gap-4">
            <div class="w-80 h-11 bg-slate-200 rounded-xl animate-pulse"></div>
          </div>
          <div class="flex items-center gap-3">
            <div class="w-28 h-10 bg-slate-200 rounded-xl animate-pulse"></div>
            <div class="w-36 h-10 bg-slate-200 rounded-xl animate-pulse"></div>
          </div>
        </div>
      </div>
      
      <!-- Table Header Skeleton -->
      <div class="flex items-center gap-4 px-6 py-4 bg-slate-50/80 border-b border-slate-100">
        <div *ngFor="let col of columns" 
             class="h-4 bg-slate-200 rounded animate-pulse"
             [style.width]="col.width || '120px'">
        </div>
      </div>
      
      <!-- Table Rows Skeleton -->
      <div *ngFor="let row of rows" class="flex items-center gap-4 px-6 py-5 border-b border-slate-50">
        <div *ngFor="let col of columns" 
             class="h-5 bg-slate-100 rounded animate-pulse"
             [style.width]="col.width || '120px'"
             [style.animation-delay]="row * 0.1 + 's'">
        </div>
      </div>
      
      <!-- Footer Skeleton -->
      <div class="flex items-center justify-between px-6 py-4 bg-slate-50/50">
        <div class="w-40 h-8 bg-slate-200 rounded-lg animate-pulse"></div>
        <div class="w-64 h-8 bg-slate-200 rounded-lg animate-pulse"></div>
      </div>
    </div>
  `
})
export class SkeletonLoaderComponent {
    @Input() rowCount = 5;
    @Input() columnCount = 5;

    get rows(): number[] {
        return Array(this.rowCount).fill(0).map((_, i) => i);
    }

    get columns(): { width: string }[] {
        const widths = ['60px', '150px', '120px', '100px', '80px', '140px'];
        return Array(this.columnCount).fill(0).map((_, i) => ({
            width: widths[i % widths.length]
        }));
    }
}
