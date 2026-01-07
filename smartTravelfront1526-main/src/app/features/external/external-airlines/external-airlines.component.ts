// src/app/features/external/external-airlines/external-airlines.component.ts
import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { PageHeaderComponent, DataTableComponent, TableColumn, SkeletonLoaderComponent } from '../../../shared/components';
import { AirlineService } from '../../../services/airline.service';
import { AirlineDTO } from '../../../models';
import { SweetAlertService } from '../../../core/sweetalert.service';

@Component({
    selector: 'app-external-airlines',
    standalone: true,
    imports: [CommonModule, FormsModule, PageHeaderComponent, DataTableComponent, SkeletonLoaderComponent],
    template: `
    <div class="p-8">
      <app-page-header 
        icon="🦅" 
        title="External Airlines" 
        subtitle="Search airlines from AviationStack API (Read-only)"
        source="api">
      </app-page-header>

      <!-- Search Form -->
      <div class="bg-white rounded-2xl shadow-lg border border-slate-100 p-6 mb-6">
        <h3 class="text-lg font-bold text-slate-800 mb-4 flex items-center gap-2">
          <span>🔍</span> Search Parameters
        </h3>
        <form (ngSubmit)="search()" class="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div>
            <label class="block text-sm font-semibold text-slate-700 mb-2">Airline Name</label>
            <input type="text" [(ngModel)]="airlineName" name="airlineName"
                   class="w-full px-4 py-3 border border-slate-200 rounded-xl focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-all"
                   placeholder="e.g. Air France, British Airways">
          </div>
          <div class="flex items-end">
            <button type="submit" [disabled]="isLoading || !airlineName.trim()"
                    class="w-full px-6 py-3 bg-gradient-to-r from-blue-600 to-cyan-600 text-white font-bold rounded-xl hover:from-blue-700 hover:to-cyan-700 transition-all duration-200 shadow-lg shadow-blue-500/25 disabled:opacity-50 flex items-center justify-center gap-2">
              <span *ngIf="!isLoading">🔍 Search Airlines</span>
              <span *ngIf="isLoading" class="flex items-center gap-2">
                <span class="w-5 h-5 border-2 border-white/30 border-t-white rounded-full animate-spin"></span>
                Searching...
              </span>
            </button>
          </div>
        </form>
      </div>

      <!-- Quick Search Tags -->
      <div class="mb-6 flex flex-wrap gap-2">
        <span class="text-sm text-slate-500 mr-2">Quick search:</span>
        <button *ngFor="let airline of popularAirlines"
                (click)="quickSearch(airline)"
                class="px-3 py-1.5 bg-slate-100 hover:bg-blue-100 text-slate-700 hover:text-blue-700 rounded-full text-sm font-medium transition-colors">
          {{ airline }}
        </button>
      </div>

      <!-- Info Banner -->
      <div class="bg-amber-50 border border-amber-200 rounded-xl p-4 mb-6 flex items-center gap-3">
        <span class="text-2xl">ℹ️</span>
        <p class="text-amber-800 text-sm">
          <strong>Read-only access:</strong> Airline data is fetched from external APIs for reference purposes.
        </p>
      </div>

      <!-- Results -->
      <app-skeleton-loader *ngIf="isLoading" [rowCount]="5" [columnCount]="5"></app-skeleton-loader>

      <app-data-table 
        *ngIf="!isLoading"
        [data]="airlines"
        [columns]="columns"
        [actions]="[]"
        [selectable]="false"
        [loading]="isLoading"
        idKey="airlineCode"
        searchPlaceholder="Search airlines..."
        emptyTitle="No airlines found"
        emptyMessage="Try searching with a different airline name">
      </app-data-table>
    </div>
  `
})
export class ExternalAirlinesComponent {
    private airlineService = inject(AirlineService);
    private sweetAlert = inject(SweetAlertService);

    airlineName = '';
    airlines: AirlineDTO[] = [];
    isLoading = false;

    popularAirlines = [
        'Air France', 'British Airways', 'Lufthansa', 'Emirates',
        'Delta', 'United', 'American Airlines', 'Qatar Airways'
    ];

    columns: TableColumn[] = [
        { key: 'iataCode', label: 'IATA', width: '80px' },
        { key: 'icaoCode', label: 'ICAO', width: '80px' },
        { key: 'airlineName', label: 'Airline Name' },
        { key: 'country', label: 'Country' },
        { key: 'active', label: 'Status', type: 'boolean' }
    ];

    quickSearch(name: string): void {
        this.airlineName = name;
        this.search();
    }

    search(): void {
        if (!this.airlineName.trim()) {
            this.sweetAlert.warning('Please enter an airline name');
            return;
        }

        this.isLoading = true;
        this.airlines = [];

        this.airlineService.search(this.airlineName).subscribe({
            next: (results: AirlineDTO[]) => {
                this.airlines = results;
                this.isLoading = false;
                if (results.length === 0) {
                    this.sweetAlert.info('No airlines found');
                }
            },
            error: (err: Error) => {
                console.error('Error searching airlines:', err);
                this.sweetAlert.error('Failed to search airlines. The API might be unavailable.');
                this.isLoading = false;
            }
        });
    }
}
