// src/app/shared/components/data-table/data-table.component.ts
import { Component, Input, Output, EventEmitter, TemplateRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

export interface TableColumn {
    key: string;
    label: string;
    sortable?: boolean;
    type?: 'text' | 'number' | 'date' | 'badge' | 'currency' | 'rating' | 'boolean';
    badgeColors?: { [key: string]: string };
    width?: string;
}

export interface TableAction {
    key: string;       // Action identifier
    icon: string;
    label: string;
    color?: 'primary' | 'danger' | 'warning' | 'success' | 'blue' | 'red' | 'green' | 'yellow';
    condition?: (item: any) => boolean;
}

@Component({
    selector: 'app-data-table',
    standalone: true,
    imports: [CommonModule, FormsModule],
    template: `
    <div class="bg-white rounded-2xl shadow-lg border border-slate-100 overflow-hidden">
      <!-- Table Header with Search and Actions -->
      <div class="p-6 border-b border-slate-100 bg-gradient-to-r from-slate-50 to-white">
        <div class="flex flex-col lg:flex-row lg:items-center justify-between gap-4">
          <div class="flex items-center gap-3">
            <div class="relative">
              <span class="absolute left-4 top-1/2 -translate-y-1/2 text-slate-400">🔍</span>
              <input 
                type="text" 
                [(ngModel)]="searchQuery"
                (ngModelChange)="onSearchChange()"
                [placeholder]="searchPlaceholder"
                class="pl-11 pr-4 py-3 w-80 border border-slate-200 rounded-xl focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-all duration-200 bg-white/80">
            </div>
            <span class="text-sm text-slate-500" *ngIf="filteredData.length !== data.length">
              Showing {{ filteredData.length }} of {{ data.length }}
            </span>
          </div>
          
          <div class="flex items-center gap-3">
            <ng-content select="[tableActions]"></ng-content>
          </div>
        </div>
      </div>

      <!-- Loading State -->
      <div *ngIf="loading" class="p-12 flex flex-col items-center justify-center">
        <div class="w-12 h-12 border-4 border-blue-500 border-t-transparent rounded-full animate-spin mb-4"></div>
        <p class="text-slate-500 font-medium">Loading data...</p>
      </div>

      <!-- Empty State -->
      <div *ngIf="!loading && filteredData.length === 0" class="p-12 text-center">
        <div class="text-6xl mb-4">📭</div>
        <h3 class="text-xl font-bold text-slate-700 mb-2">{{ emptyTitle }}</h3>
        <p class="text-slate-500">{{ emptyMessage }}</p>
      </div>

      <!-- Table -->
      <div *ngIf="!loading && filteredData.length > 0" class="overflow-x-auto">
        <table class="w-full">
          <thead>
            <tr class="bg-slate-50/80 border-b border-slate-100">
              <!-- Selection Column -->
              <th *ngIf="selectable" class="px-6 py-4 text-left">
                <input 
                  type="checkbox" 
                  [checked]="allSelected"
                  (change)="toggleSelectAll()"
                  class="w-4 h-4 rounded border-slate-300 text-blue-600 focus:ring-blue-500">
              </th>
              <!-- Data Columns -->
              <th *ngFor="let col of columns" 
                  class="px-6 py-4 text-left text-xs font-bold text-slate-600 uppercase tracking-wider cursor-pointer hover:bg-slate-100 transition-colors"
                  [style.width]="col.width"
                  (click)="col.sortable !== false && onSort(col.key)">
                <div class="flex items-center gap-2">
                  {{ col.label }}
                  <span *ngIf="sortKey === col.key" class="text-blue-500">
                    {{ sortDirection === 'asc' ? '↑' : '↓' }}
                  </span>
                </div>
              </th>
              <!-- Actions Column -->
              <th *ngIf="actions.length > 0" class="px-6 py-4 text-right text-xs font-bold text-slate-600 uppercase tracking-wider">
                Actions
              </th>
            </tr>
          </thead>
          <tbody class="divide-y divide-slate-100">
            <tr *ngFor="let item of paginatedData; let i = index" 
                class="hover:bg-blue-50/50 transition-colors duration-150 group"
                [class.bg-blue-50]="isSelected(item)">
              <!-- Selection -->
              <td *ngIf="selectable" class="px-6 py-4">
                <input 
                  type="checkbox" 
                  [checked]="isSelected(item)"
                  (change)="toggleSelection(item)"
                  class="w-4 h-4 rounded border-slate-300 text-blue-600 focus:ring-blue-500">
              </td>
              <!-- Data Cells -->
              <td *ngFor="let col of columns" class="px-6 py-4">
                <ng-container [ngSwitch]="col.type">
                  <!-- Badge -->
                  <span *ngSwitchCase="'badge'" 
                        class="px-3 py-1 text-xs font-bold rounded-full"
                        [ngClass]="getBadgeClass(getValue(item, col.key), col.badgeColors)">
                    {{ getValue(item, col.key) }}
                  </span>
                  <!-- Currency -->
                  <span *ngSwitchCase="'currency'" class="font-semibold text-emerald-600">
                    {{ getValue(item, col.key) | currency:'EUR' }}
                  </span>
                  <!-- Rating -->
                  <span *ngSwitchCase="'rating'" class="flex items-center gap-1">
                    <span class="text-amber-400">★</span>
                    <span class="font-semibold">{{ getValue(item, col.key) }}</span>
                  </span>
                  <!-- Boolean -->
                  <span *ngSwitchCase="'boolean'" 
                        class="px-3 py-1 text-xs font-bold rounded-full"
                        [ngClass]="getValue(item, col.key) ? 'bg-emerald-100 text-emerald-700' : 'bg-red-100 text-red-700'">
                    {{ getValue(item, col.key) ? 'Yes' : 'No' }}
                  </span>
                  <!-- Date -->
                  <span *ngSwitchCase="'date'" class="text-slate-600">
                    {{ getValue(item, col.key) | date:'medium' }}
                  </span>
                  <!-- Default Text -->
                  <span *ngSwitchDefault class="text-slate-700">
                    {{ getValue(item, col.key) || '—' }}
                  </span>
                </ng-container>
              </td>
              <!-- Actions -->
              <td *ngIf="actions.length > 0" class="px-6 py-4">
                <div class="flex items-center justify-end gap-2 opacity-0 group-hover:opacity-100 transition-opacity">
                  <ng-container *ngFor="let action of actions">
                    <button *ngIf="!action.condition || action.condition(item)"
                            (click)="onAction(action.key, item)"
                            [title]="action.label"
                            class="p-2 rounded-lg transition-all duration-200 hover:scale-110"
                            [ngClass]="{
                              'hover:bg-blue-100 text-blue-600': action.color === 'primary' || action.color === 'blue' || !action.color,
                              'hover:bg-red-100 text-red-600': action.color === 'danger' || action.color === 'red',
                              'hover:bg-amber-100 text-amber-600': action.color === 'warning' || action.color === 'yellow',
                              'hover:bg-emerald-100 text-emerald-600': action.color === 'success' || action.color === 'green'
                            }">
                      {{ action.icon }}
                    </button>
                  </ng-container>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- Pagination -->
      <div *ngIf="!loading && filteredData.length > 0" class="px-6 py-4 border-t border-slate-100 bg-slate-50/50 flex items-center justify-between">
        <div class="flex items-center gap-2">
          <span class="text-sm text-slate-600">Rows per page:</span>
          <select [(ngModel)]="pageSize" (ngModelChange)="onPageSizeChange()" 
                  class="border border-slate-200 rounded-lg px-3 py-1.5 text-sm focus:ring-2 focus:ring-blue-500">
            <option [value]="10">10</option>
            <option [value]="25">25</option>
            <option [value]="50">50</option>
            <option [value]="100">100</option>
          </select>
        </div>
        
        <div class="flex items-center gap-2">
          <span class="text-sm text-slate-600">
            {{ (currentPage - 1) * pageSize + 1 }} - {{ Math.min(currentPage * pageSize, filteredData.length) }} of {{ filteredData.length }}
          </span>
          <div class="flex gap-1">
            <button (click)="goToPage(1)" [disabled]="currentPage === 1"
                    class="p-2 rounded-lg hover:bg-slate-200 disabled:opacity-40 disabled:cursor-not-allowed transition-colors">
              ⏮️
            </button>
            <button (click)="goToPage(currentPage - 1)" [disabled]="currentPage === 1"
                    class="p-2 rounded-lg hover:bg-slate-200 disabled:opacity-40 disabled:cursor-not-allowed transition-colors">
              ◀️
            </button>
            <button (click)="goToPage(currentPage + 1)" [disabled]="currentPage === totalPages"
                    class="p-2 rounded-lg hover:bg-slate-200 disabled:opacity-40 disabled:cursor-not-allowed transition-colors">
              ▶️
            </button>
            <button (click)="goToPage(totalPages)" [disabled]="currentPage === totalPages"
                    class="p-2 rounded-lg hover:bg-slate-200 disabled:opacity-40 disabled:cursor-not-allowed transition-colors">
              ⏭️
            </button>
          </div>
        </div>
      </div>
    </div>
  `
})
export class DataTableComponent {
    @Input() data: any[] = [];
    @Input() columns: TableColumn[] = [];
    @Input() actions: TableAction[] = [];
    @Input() loading = false;
    @Input() selectable = false;
    @Input() searchPlaceholder = 'Search...';
    @Input() emptyTitle = 'No data found';
    @Input() emptyMessage = 'Try adjusting your search or filters';
    @Input() idKey = 'id';

    @Output() actionClick = new EventEmitter<{ action: string, item: any }>();
    @Output() selectionChange = new EventEmitter<any[]>();

    searchQuery = '';
    sortKey = '';
    sortDirection: 'asc' | 'desc' = 'asc';
    currentPage = 1;
    pageSize = 10;
    selectedItems = new Set<any>();

    Math = Math;

    get filteredData(): any[] {
        let result = [...this.data];

        // Search filter
        if (this.searchQuery) {
            const query = this.searchQuery.toLowerCase();
            result = result.filter(item =>
                this.columns.some(col =>
                    String(this.getValue(item, col.key)).toLowerCase().includes(query)
                )
            );
        }

        // Sort
        if (this.sortKey) {
            result.sort((a, b) => {
                const aVal = this.getValue(a, this.sortKey);
                const bVal = this.getValue(b, this.sortKey);
                const comparison = aVal < bVal ? -1 : aVal > bVal ? 1 : 0;
                return this.sortDirection === 'asc' ? comparison : -comparison;
            });
        }

        return result;
    }

    get paginatedData(): any[] {
        const start = (this.currentPage - 1) * this.pageSize;
        return this.filteredData.slice(start, start + this.pageSize);
    }

    get totalPages(): number {
        return Math.ceil(this.filteredData.length / this.pageSize);
    }

    get allSelected(): boolean {
        return this.paginatedData.length > 0 && this.paginatedData.every(item => this.isSelected(item));
    }

    getValue(item: any, key: string): any {
        return key.split('.').reduce((obj, k) => obj?.[k], item);
    }

    getBadgeClass(value: string, colors?: { [key: string]: string }): string {
        if (colors && colors[value]) {
            return colors[value];
        }
        return 'bg-slate-100 text-slate-700';
    }

    onSearchChange(): void {
        this.currentPage = 1;
    }

    onSort(key: string): void {
        if (this.sortKey === key) {
            this.sortDirection = this.sortDirection === 'asc' ? 'desc' : 'asc';
        } else {
            this.sortKey = key;
            this.sortDirection = 'asc';
        }
    }

    onPageSizeChange(): void {
        this.currentPage = 1;
    }

    goToPage(page: number): void {
        if (page >= 1 && page <= this.totalPages) {
            this.currentPage = page;
        }
    }

    isSelected(item: any): boolean {
        return this.selectedItems.has(item[this.idKey]);
    }

    toggleSelection(item: any): void {
        const id = item[this.idKey];
        if (this.selectedItems.has(id)) {
            this.selectedItems.delete(id);
        } else {
            this.selectedItems.add(id);
        }
        this.emitSelectionChange();
    }

    toggleSelectAll(): void {
        if (this.allSelected) {
            this.paginatedData.forEach(item => this.selectedItems.delete(item[this.idKey]));
        } else {
            this.paginatedData.forEach(item => this.selectedItems.add(item[this.idKey]));
        }
        this.emitSelectionChange();
    }

    private emitSelectionChange(): void {
        const selected = this.data.filter(item => this.selectedItems.has(item[this.idKey]));
        this.selectionChange.emit(selected);
    }

    onAction(action: string, item: any): void {
        this.actionClick.emit({ action, item });
    }
}
