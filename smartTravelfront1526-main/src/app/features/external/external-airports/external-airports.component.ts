// src/app/features/external/external-airports/external-airports.component.ts
import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { PageHeaderComponent, DataTableComponent, TableColumn, SkeletonLoaderComponent } from '../../../shared/components';
import { AirportService } from '../../../services/airport.service';
import { AirportDTO } from '../../../models';
import { SweetAlertService } from '../../../core/sweetalert.service';

@Component({
    selector: 'app-external-airports',
    standalone: true,
    imports: [CommonModule, FormsModule, PageHeaderComponent, DataTableComponent, SkeletonLoaderComponent],
    template: `
    <div class="p-8">
      <app-page-header 
        icon="🛫" 
        title="External Airports" 
        subtitle="Search airports from AviationStack API (Read-only)"
        source="api">
      </app-page-header>

      <!-- Search Form -->
      <div class="bg-white rounded-2xl shadow-lg border border-slate-100 p-6 mb-6">
        <h3 class="text-lg font-bold text-slate-800 mb-4 flex items-center gap-2">
          <span>🔍</span> Search Parameters
        </h3>
        <form (ngSubmit)="search()" class="grid grid-cols-1 md:grid-cols-3 gap-4">
          <div>
            <label class="block text-sm font-semibold text-slate-700 mb-2">Country</label>
            <select [(ngModel)]="country" name="country"
                    class="w-full px-4 py-3 border border-slate-200 rounded-xl focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-all">
              <option *ngFor="let c of countries" [value]="c">{{ c }}</option>
            </select>
          </div>
          <div>
            <label class="block text-sm font-semibold text-slate-700 mb-2">Airport Name (Optional)</label>
            <input type="text" [(ngModel)]="airportName" name="airportName"
                   class="w-full px-4 py-3 border border-slate-200 rounded-xl focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-all"
                   placeholder="e.g. Charles de Gaulle">
          </div>
          <div class="flex items-end">
            <button type="submit" [disabled]="isLoading"
                    class="w-full px-6 py-3 bg-gradient-to-r from-blue-600 to-cyan-600 text-white font-bold rounded-xl hover:from-blue-700 hover:to-cyan-700 transition-all duration-200 shadow-lg shadow-blue-500/25 disabled:opacity-50 flex items-center justify-center gap-2">
              <span *ngIf="!isLoading">🔍 Search</span>
              <span *ngIf="isLoading" class="flex items-center gap-2">
                <span class="w-5 h-5 border-2 border-white/30 border-t-white rounded-full animate-spin"></span>
                Searching...
              </span>
            </button>
          </div>
        </form>
      </div>

      <!-- Info Banner -->
      <div class="bg-amber-50 border border-amber-200 rounded-xl p-4 mb-6 flex items-center gap-3">
        <span class="text-2xl">ℹ️</span>
        <p class="text-amber-800 text-sm">
          <strong>Read-only access:</strong> Airport data is fetched from external APIs for reference purposes. 
          Import functionality is not available for airports.
        </p>
      </div>

      <!-- Results -->
      <app-skeleton-loader *ngIf="isLoading" [rowCount]="5" [columnCount]="6"></app-skeleton-loader>

      <app-data-table 
        *ngIf="!isLoading"
        [data]="airports"
        [columns]="columns"
        [actions]="[]"
        [selectable]="false"
        [loading]="isLoading"
        idKey="airportCode"
        searchPlaceholder="Search airports..."
        emptyTitle="No airports found"
        emptyMessage="Try searching with different parameters">
      </app-data-table>
    </div>
  `
})
export class ExternalAirportsComponent {
    private airportService = inject(AirportService);
    private sweetAlert = inject(SweetAlertService);

    countries = [
        'France', 'United Kingdom', 'United States', 'Germany', 'Spain',
        'Italy', 'Japan', 'United Arab Emirates', 'Australia', 'Canada'
    ];

    country = 'France';
    airportName = '';
    airports: AirportDTO[] = [];
    isLoading = false;

    columns: TableColumn[] = [
        { key: 'iataCode', label: 'IATA', width: '80px' },
        { key: 'icaoCode', label: 'ICAO', width: '80px' },
        { key: 'airportName', label: 'Airport Name' },
        { key: 'city', label: 'City' },
        { key: 'country', label: 'Country' },
        { key: 'timezone', label: 'Timezone' }
    ];

    search(): void {
        this.isLoading = true;
        this.airports = [];

        this.airportService.search({ airportName: this.airportName, country: this.country }).subscribe({
            next: (results: AirportDTO[]) => {
                this.airports = results;
                this.isLoading = false;
                if (results.length === 0) {
                    this.sweetAlert.info('No airports found');
                }
            },
            error: (err: Error) => {
                console.error('Error searching airports:', err);
                this.sweetAlert.error('Failed to search airports. The API might be unavailable.');
                this.isLoading = false;
            }
        });
    }
}
