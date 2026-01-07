// src/app/features/external/external-flights/external-flights.component.ts
import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { PageHeaderComponent, DataTableComponent, TableColumn, TableAction, SkeletonLoaderComponent } from '../../../shared/components';
import { ExternalFlightService, FlightSearchParams } from '../../../services/external-flight.service';
import { FlightService } from '../../../services/flight.service';
import { FlightDTO, Flight } from '../../../models';
import { SweetAlertService } from '../../../core/sweetalert.service';
import { firstValueFrom } from 'rxjs';

@Component({
    selector: 'app-external-flights',
    standalone: true,
    imports: [CommonModule, FormsModule, PageHeaderComponent, DataTableComponent, SkeletonLoaderComponent],
    template: `
    <div class="p-8">
      <app-page-header 
        icon="✈️" 
        title="External Flights" 
        subtitle="Search and import flights from Amadeus API"
        source="api">
        <div headerActions class="flex items-center gap-3">
          <button *ngIf="selectedFlights.length > 0"
                  (click)="importSelected()"
                  class="px-4 py-2.5 bg-gradient-to-r from-emerald-600 to-teal-600 text-white font-semibold rounded-xl hover:from-emerald-700 hover:to-teal-700 transition-all duration-200 shadow-lg shadow-emerald-500/25 flex items-center gap-2">
            <span>📥</span>
            Import Selected ({{ selectedFlights.length }})
          </button>
          <button *ngIf="flights.length > 0"
                  (click)="importAll()"
                  class="px-4 py-2.5 border border-emerald-300 text-emerald-700 font-semibold rounded-xl hover:bg-emerald-50 transition-all duration-200 flex items-center gap-2">
            <span>📦</span>
            Import All
          </button>
        </div>
      </app-page-header>

      <!-- Search Form -->
      <div class="bg-white rounded-2xl shadow-lg border border-slate-100 p-6 mb-6">
        <h3 class="text-lg font-bold text-slate-800 mb-4 flex items-center gap-2">
          <span>🔍</span> Search Parameters
        </h3>
        <form (ngSubmit)="search()" class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-5 gap-4">
          <div>
            <label class="block text-sm font-semibold text-slate-700 mb-2">Origin (IATA)</label>
            <input type="text" [(ngModel)]="params.origin" name="origin" 
                   class="w-full px-4 py-3 border border-slate-200 rounded-xl focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-all"
                   placeholder="e.g. CDG" maxlength="3">
          </div>
          <div>
            <label class="block text-sm font-semibold text-slate-700 mb-2">Destination (IATA)</label>
            <input type="text" [(ngModel)]="params.destination" name="destination"
                   class="w-full px-4 py-3 border border-slate-200 rounded-xl focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-all"
                   placeholder="e.g. JFK" maxlength="3">
          </div>
          <div>
            <label class="block text-sm font-semibold text-slate-700 mb-2">Departure Date</label>
            <input type="date" [(ngModel)]="params.departureDate" name="departureDate"
                   class="w-full px-4 py-3 border border-slate-200 rounded-xl focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-all">
          </div>
          <div>
            <label class="block text-sm font-semibold text-slate-700 mb-2">Adults</label>
            <input type="number" [(ngModel)]="params.adults" name="adults" min="1" max="9"
                   class="w-full px-4 py-3 border border-slate-200 rounded-xl focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-all">
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

      <!-- Results -->
      <app-skeleton-loader *ngIf="isLoading" [rowCount]="5" [columnCount]="6"></app-skeleton-loader>

      <app-data-table 
        *ngIf="!isLoading"
        [data]="flights"
        [columns]="columns"
        [actions]="actions"
        [selectable]="true"
        [loading]="isLoading"
        idKey="flightNumber"
        searchPlaceholder="Search flights..."
        emptyTitle="No flights found"
        emptyMessage="Try searching with different parameters or adjust your criteria"
        (selectionChange)="onSelectionChange($event)"
        (actionClick)="onActionClick($event)">
      </app-data-table>
    </div>
  `
})
export class ExternalFlightsComponent implements OnInit {
    private externalFlightService = inject(ExternalFlightService);
    private flightService = inject(FlightService);
    private sweetAlert = inject(SweetAlertService);

    params: FlightSearchParams = {
        origin: 'CDG',
        destination: 'JFK',
        departureDate: this.getDefaultDate(),
        adults: 2
    };

    flights: FlightDTO[] = [];
    selectedFlights: FlightDTO[] = [];
    isLoading = false;

    columns: TableColumn[] = [
        { key: 'flightNumber', label: 'Flight #', width: '100px' },
        { key: 'airlineName', label: 'Airline' },
        { key: 'departureAirportCode', label: 'From', width: '80px' },
        { key: 'arrivalAirportCode', label: 'To', width: '80px' },
        { key: 'departureTime', label: 'Departure', type: 'date' },
        { key: 'duration', label: 'Duration' },
        { key: 'price', label: 'Price', type: 'currency' },
        { key: 'cabinClass', label: 'Class', type: 'badge', badgeColors: {
            'ECONOMY': 'bg-slate-100 text-slate-700',
            'BUSINESS': 'bg-purple-100 text-purple-700',
            'FIRST': 'bg-amber-100 text-amber-700'
        }}
    ];

    actions: TableAction[] = [
        { key: 'import', icon: '📥', label: 'Import to Database', color: 'success' }
    ];

    ngOnInit(): void {
        // Auto-search on load
        this.search();
    }

    private getDefaultDate(): string {
        const date = new Date();
        date.setDate(date.getDate() + 7);
        return date.toISOString().split('T')[0];
    }

    search(): void {
        this.isLoading = true;
        this.flights = [];
        this.selectedFlights = [];

        this.externalFlightService.searchFlights(this.params).subscribe({
            next: (results) => {
                this.flights = results;
                this.isLoading = false;
                if (results.length === 0) {
                    this.sweetAlert.info('No flights found for the selected criteria');
                }
            },
            error: (err) => {
                console.error('Error searching flights:', err);
                this.sweetAlert.error('Failed to search flights. The API might be unavailable.');
                this.isLoading = false;
            }
        });
    }

    onSelectionChange(selected: FlightDTO[]): void {
        this.selectedFlights = selected;
    }

    async onActionClick(event: { action: string, item: FlightDTO }): Promise<void> {
        if (event.action === 'import') {
            await this.importFlight(event.item);
        }
    }

    private async importFlight(dto: FlightDTO): Promise<void> {
        const flight: Flight = {
            airline: dto.airlineName || 'Unknown',
            flightNumber: dto.flightNumber || '',
            origin: dto.departureAirportCode || '',
            destination: dto.arrivalAirportCode || '',
            departureTime: dto.departureTime,
            arrivalTime: dto.arrivalTime,
            price: dto.price,
            seatsAvailable: dto.availableSeats
        };

        try {
            await firstValueFrom(this.flightService.create(flight));
            this.sweetAlert.success('Flight imported successfully!');
        } catch (err) {
            console.error('Error importing flight:', err);
            this.sweetAlert.error('Failed to import flight');
        }
    }

    async importSelected(): Promise<void> {
        const confirmed = await this.sweetAlert.confirmDelete(`Import ${this.selectedFlights.length} flight(s) to database?`);
        if (!confirmed.isConfirmed) return;

        this.sweetAlert.showLoading(`Importing ${this.selectedFlights.length} flights...`);

        let successCount = 0;
        let errorCount = 0;

        for (const dto of this.selectedFlights) {
            try {
                const flight: Flight = {
                    airline: dto.airlineName || 'Unknown',
                    flightNumber: dto.flightNumber || '',
                    origin: dto.departureAirportCode || '',
                    destination: dto.arrivalAirportCode || '',
                    departureTime: dto.departureTime,
                    arrivalTime: dto.arrivalTime,
                    price: dto.price,
                    seatsAvailable: dto.availableSeats
                };
                await firstValueFrom(this.flightService.create(flight));
                successCount++;
            } catch (err) {
                errorCount++;
            }
        }

        this.sweetAlert.closeLoading();

        if (successCount > 0) {
            this.sweetAlert.success(`Imported ${successCount} flight(s)!` + (errorCount > 0 ? ` (${errorCount} failed)` : ''));
        }
        if (errorCount > 0 && successCount === 0) {
            this.sweetAlert.error('Failed to import flights');
        }
    }

    async importAll(): Promise<void> {
        const confirmed = await this.sweetAlert.confirmDelete(`Import all ${this.flights.length} flights to database?`);
        if (!confirmed.isConfirmed) return;

        this.selectedFlights = [...this.flights];
        await this.importSelected();
    }
}
