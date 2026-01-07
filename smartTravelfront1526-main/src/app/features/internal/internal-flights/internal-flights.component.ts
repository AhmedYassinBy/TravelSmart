// src/app/features/internal/internal-flights/internal-flights.component.ts
import { Component, inject, OnInit, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { PageHeaderComponent, DataTableComponent, TableColumn, TableAction, ModalComponent, SkeletonLoaderComponent } from '../../../shared/components';
import { FlightService } from '../../../services/flight.service';
import { Flight } from '../../../models';
import { SweetAlertService } from '../../../core/sweetalert.service';

@Component({
    selector: 'app-internal-flights',
    standalone: true,
    imports: [CommonModule, FormsModule, PageHeaderComponent, DataTableComponent, ModalComponent, SkeletonLoaderComponent],
    template: `
    <div class="p-8">
      <app-page-header 
        icon="✈️" 
        title="Internal Flights" 
        subtitle="Manage your flight offerings stored in the database"
        source="database">
        <button (click)="openCreateModal()" 
                class="px-6 py-3 bg-gradient-to-r from-emerald-600 to-teal-600 text-white font-bold rounded-xl hover:from-emerald-700 hover:to-teal-700 transition-all duration-200 shadow-lg shadow-emerald-500/25 flex items-center gap-2">
          <span>✚</span> Add Flight
        </button>
      </app-page-header>

      <!-- Statistics -->
      <div class="grid grid-cols-1 md:grid-cols-4 gap-6 mb-6">
        <div class="bg-white rounded-2xl shadow-lg border border-slate-100 p-6">
          <div class="flex items-center justify-between">
            <div>
              <p class="text-sm font-medium text-slate-500">Total Flights</p>
              <p class="text-3xl font-bold text-slate-800">{{ flights.length }}</p>
            </div>
            <div class="w-12 h-12 bg-blue-100 rounded-xl flex items-center justify-center text-2xl">✈️</div>
          </div>
        </div>
        <div class="bg-white rounded-2xl shadow-lg border border-slate-100 p-6">
          <div class="flex items-center justify-between">
            <div>
              <p class="text-sm font-medium text-slate-500">Active Airlines</p>
              <p class="text-3xl font-bold text-slate-800">{{ uniqueAirlines }}</p>
            </div>
            <div class="w-12 h-12 bg-emerald-100 rounded-xl flex items-center justify-center text-2xl">🏢</div>
          </div>
        </div>
        <div class="bg-white rounded-2xl shadow-lg border border-slate-100 p-6">
          <div class="flex items-center justify-between">
            <div>
              <p class="text-sm font-medium text-slate-500">Avg. Price</p>
              <p class="text-3xl font-bold text-slate-800">{{ avgPrice | currency }}</p>
            </div>
            <div class="w-12 h-12 bg-amber-100 rounded-xl flex items-center justify-center text-2xl">💰</div>
          </div>
        </div>
        <div class="bg-white rounded-2xl shadow-lg border border-slate-100 p-6">
          <div class="flex items-center justify-between">
            <div>
              <p class="text-sm font-medium text-slate-500">Total Seats</p>
              <p class="text-3xl font-bold text-slate-800">{{ totalSeats }}</p>
            </div>
            <div class="w-12 h-12 bg-purple-100 rounded-xl flex items-center justify-center text-2xl">💺</div>
          </div>
        </div>
      </div>

      <!-- Data Table -->
      <app-skeleton-loader *ngIf="isLoading" [rowCount]="5" [columnCount]="7"></app-skeleton-loader>

      <app-data-table 
        *ngIf="!isLoading"
        [data]="flights"
        [columns]="columns"
        [actions]="actions"
        [selectable]="true"
        [loading]="isLoading"
        (actionClick)="onAction($event)"
        (selectionChange)="onSelectionChange($event)"
        searchPlaceholder="Search flights..."
        emptyTitle="No flights found"
        emptyMessage="Start by adding your first flight offering">
      </app-data-table>
    </div>

    <!-- Create/Edit Modal -->
    <app-modal [isOpen]="isModalOpen" [title]="isEditing ? 'Edit Flight' : 'Add New Flight'" (close)="closeModal()" [showFooter]="false">
      <form (ngSubmit)="saveFlight()" class="space-y-4">
        <div class="grid grid-cols-2 gap-4">
          <div>
            <label class="block text-sm font-semibold text-slate-700 mb-2">Flight Number *</label>
            <input type="text" [(ngModel)]="formData.flightNumber" name="flightNumber" required
                   class="w-full px-4 py-3 border border-slate-200 rounded-xl focus:ring-2 focus:ring-blue-500"
                   placeholder="e.g. AF1234">
          </div>
          <div>
            <label class="block text-sm font-semibold text-slate-700 mb-2">Airline *</label>
            <input type="text" [(ngModel)]="formData.airline" name="airline" required
                   class="w-full px-4 py-3 border border-slate-200 rounded-xl focus:ring-2 focus:ring-blue-500"
                   placeholder="e.g. Air France">
          </div>
        </div>
        <div class="grid grid-cols-2 gap-4">
          <div>
            <label class="block text-sm font-semibold text-slate-700 mb-2">Origin *</label>
            <input type="text" [(ngModel)]="formData.origin" name="origin" required
                   class="w-full px-4 py-3 border border-slate-200 rounded-xl focus:ring-2 focus:ring-blue-500"
                   placeholder="e.g. CDG">
          </div>
          <div>
            <label class="block text-sm font-semibold text-slate-700 mb-2">Destination *</label>
            <input type="text" [(ngModel)]="formData.destination" name="destination" required
                   class="w-full px-4 py-3 border border-slate-200 rounded-xl focus:ring-2 focus:ring-blue-500"
                   placeholder="e.g. JFK">
          </div>
        </div>
        <div class="grid grid-cols-2 gap-4">
          <div>
            <label class="block text-sm font-semibold text-slate-700 mb-2">Departure Time</label>
            <input type="datetime-local" [(ngModel)]="formData.departureTime" name="departureTime"
                   class="w-full px-4 py-3 border border-slate-200 rounded-xl focus:ring-2 focus:ring-blue-500">
          </div>
          <div>
            <label class="block text-sm font-semibold text-slate-700 mb-2">Arrival Time</label>
            <input type="datetime-local" [(ngModel)]="formData.arrivalTime" name="arrivalTime"
                   class="w-full px-4 py-3 border border-slate-200 rounded-xl focus:ring-2 focus:ring-blue-500">
          </div>
        </div>
        <div class="grid grid-cols-2 gap-4">
          <div>
            <label class="block text-sm font-semibold text-slate-700 mb-2">Price ($)</label>
            <input type="number" [(ngModel)]="formData.price" name="price" min="0" step="0.01"
                   class="w-full px-4 py-3 border border-slate-200 rounded-xl focus:ring-2 focus:ring-blue-500"
                   placeholder="0.00">
          </div>
          <div>
            <label class="block text-sm font-semibold text-slate-700 mb-2">Seats Available</label>
            <input type="number" [(ngModel)]="formData.seatsAvailable" name="seatsAvailable" min="0"
                   class="w-full px-4 py-3 border border-slate-200 rounded-xl focus:ring-2 focus:ring-blue-500"
                   placeholder="0">
          </div>
        </div>
        <div class="flex justify-end gap-3 pt-4 border-t">
          <button type="button" (click)="closeModal()"
                  class="px-6 py-3 bg-slate-100 text-slate-700 font-semibold rounded-xl hover:bg-slate-200 transition-colors">
            Cancel
          </button>
          <button type="submit" [disabled]="isSaving"
                  class="px-6 py-3 bg-gradient-to-r from-blue-600 to-indigo-600 text-white font-bold rounded-xl hover:from-blue-700 hover:to-indigo-700 transition-all disabled:opacity-50 flex items-center gap-2">
            <span *ngIf="isSaving" class="w-5 h-5 border-2 border-white/30 border-t-white rounded-full animate-spin"></span>
            {{ isEditing ? 'Update Flight' : 'Create Flight' }}
          </button>
        </div>
      </form>
    </app-modal>
  `
})
export class InternalFlightsComponent implements OnInit {
    private flightService = inject(FlightService);
    private sweetAlert = inject(SweetAlertService);

    flights: Flight[] = [];
    selectedFlights: Flight[] = [];
    isLoading = false;
    isModalOpen = false;
    isEditing = false;
    isSaving = false;

    formData: Flight = this.getEmptyForm();

    columns: TableColumn[] = [
        { key: 'flightNumber', label: 'Flight #', width: '100px' },
        { key: 'airline', label: 'Airline' },
        { key: 'origin', label: 'From', width: '80px' },
        { key: 'destination', label: 'To', width: '80px' },
        { key: 'departureTime', label: 'Departure', type: 'date' },
        { key: 'price', label: 'Price', type: 'currency', width: '100px' },
        { key: 'seatsAvailable', label: 'Seats', width: '80px' }
    ];

    actions: TableAction[] = [
        { key: 'edit', label: 'Edit', icon: '✏️', color: 'blue' },
        { key: 'delete', label: 'Delete', icon: '🗑️', color: 'red' }
    ];

    get uniqueAirlines(): number {
        return new Set(this.flights.map(f => f.airline)).size;
    }

    get avgPrice(): number {
        if (!this.flights.length) return 0;
        const total = this.flights.reduce((sum, f) => sum + (f.price || 0), 0);
        return total / this.flights.length;
    }

    get totalSeats(): number {
        return this.flights.reduce((sum, f) => sum + (f.seatsAvailable || 0), 0);
    }

    ngOnInit(): void {
        this.loadFlights();
    }

    loadFlights(): void {
        this.isLoading = true;
        this.flightService.findAll().subscribe({
            next: (flights) => {
                this.flights = flights;
                this.isLoading = false;
            },
            error: (err) => {
                console.error('Error loading flights:', err);
                this.sweetAlert.error('Failed to load flights');
                this.isLoading = false;
            }
        });
    }

    getEmptyForm(): Flight {
        return {
            flightNumber: '',
            airline: '',
            origin: '',
            destination: '',
            departureTime: '',
            arrivalTime: '',
            price: 0,
            seatsAvailable: 0
        };
    }

    openCreateModal(): void {
        this.isEditing = false;
        this.formData = this.getEmptyForm();
        this.isModalOpen = true;
    }

    openEditModal(flight: Flight): void {
        this.isEditing = true;
        this.formData = { ...flight };
        this.isModalOpen = true;
    }

    closeModal(): void {
        this.isModalOpen = false;
        this.formData = this.getEmptyForm();
    }

    saveFlight(): void {
        if (!this.formData.flightNumber || !this.formData.airline || !this.formData.origin || !this.formData.destination) {
            this.sweetAlert.warning('Please fill in all required fields');
            return;
        }

        this.isSaving = true;

        const operation = this.isEditing
            ? this.flightService.update(this.formData.id!, this.formData)
            : this.flightService.create(this.formData);

        operation.subscribe({
            next: () => {
                this.sweetAlert.success(this.isEditing ? 'Flight updated successfully' : 'Flight created successfully');
                this.closeModal();
                this.loadFlights();
                this.isSaving = false;
            },
            error: (err) => {
                console.error('Error saving flight:', err);
                this.sweetAlert.error('Failed to save flight');
                this.isSaving = false;
            }
        });
    }

    onAction(event: { action: string; item: Flight }): void {
        if (event.action === 'edit') {
            this.openEditModal(event.item);
        } else if (event.action === 'delete') {
            this.deleteFlight(event.item);
        }
    }

    deleteFlight(flight: Flight): void {
        this.sweetAlert.confirm(
            `Are you sure you want to delete flight ${flight.flightNumber}?`,
            'This action cannot be undone.'
        ).then((confirmed) => {
            if (confirmed) {
                this.flightService.delete(flight.id!).subscribe({
                    next: () => {
                        this.sweetAlert.success('Flight deleted successfully');
                        this.loadFlights();
                    },
                    error: (err) => {
                        console.error('Error deleting flight:', err);
                        this.sweetAlert.error('Failed to delete flight');
                    }
                });
            }
        });
    }

    onSelectionChange(selected: Flight[]): void {
        this.selectedFlights = selected;
    }
}
