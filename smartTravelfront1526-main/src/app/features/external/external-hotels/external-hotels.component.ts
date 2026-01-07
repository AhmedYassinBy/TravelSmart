// src/app/features/external/external-hotels/external-hotels.component.ts
import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { PageHeaderComponent, DataTableComponent, TableColumn, TableAction, SkeletonLoaderComponent } from '../../../shared/components';
import { ExternalHotelService } from '../../../services/external-hotel.service';
import { HotelService } from '../../../services/hotel.service';
import { HotelDTO, Hotel } from '../../../models';
import { SweetAlertService } from '../../../core/sweetalert.service';
import { firstValueFrom } from 'rxjs';

@Component({
    selector: 'app-external-hotels',
    standalone: true,
    imports: [CommonModule, FormsModule, PageHeaderComponent, DataTableComponent, SkeletonLoaderComponent],
    template: `
    <div class="p-8">
      <app-page-header 
        icon="🏨" 
        title="External Hotels" 
        subtitle="Search and import hotels from Amadeus & Booking.com APIs"
        source="api">
        <div headerActions class="flex items-center gap-3">
          <button *ngIf="selectedHotels.length > 0"
                  (click)="importSelected()"
                  class="px-4 py-2.5 bg-gradient-to-r from-emerald-600 to-teal-600 text-white font-semibold rounded-xl hover:from-emerald-700 hover:to-teal-700 transition-all duration-200 shadow-lg shadow-emerald-500/25 flex items-center gap-2">
            <span>📥</span>
            Import Selected ({{ selectedHotels.length }})
          </button>
        </div>
      </app-page-header>

      <!-- Search Form -->
      <div class="bg-white rounded-2xl shadow-lg border border-slate-100 p-6 mb-6">
        <h3 class="text-lg font-bold text-slate-800 mb-4 flex items-center gap-2">
          <span>🔍</span> Search Parameters
        </h3>
        
        <!-- API Source Tabs -->
        <div class="flex gap-2 mb-6">
          <button (click)="apiSource = 'amadeus'"
                  [class]="apiSource === 'amadeus' ? 'bg-blue-600 text-white' : 'bg-slate-100 text-slate-600 hover:bg-slate-200'"
                  class="px-4 py-2 rounded-lg font-semibold text-sm transition-all">
            Amadeus API
          </button>
          <button (click)="apiSource = 'booking'"
                  [class]="apiSource === 'booking' ? 'bg-blue-600 text-white' : 'bg-slate-100 text-slate-600 hover:bg-slate-200'"
                  class="px-4 py-2 rounded-lg font-semibold text-sm transition-all">
            Booking.com API
          </button>
        </div>

        <!-- Amadeus Search -->
        <form *ngIf="apiSource === 'amadeus'" (ngSubmit)="searchAmadeus()" class="grid grid-cols-1 md:grid-cols-3 gap-4">
          <div>
            <label class="block text-sm font-semibold text-slate-700 mb-2">City</label>
            <select [(ngModel)]="selectedCity" name="city"
                    class="w-full px-4 py-3 border border-slate-200 rounded-xl focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-all">
              <option *ngFor="let city of cityCodes" [ngValue]="city">{{ city.name }}</option>
            </select>
          </div>
          <div class="flex items-end">
            <button type="submit" [disabled]="isLoading"
                    class="w-full px-6 py-3 bg-gradient-to-r from-blue-600 to-cyan-600 text-white font-bold rounded-xl hover:from-blue-700 hover:to-cyan-700 transition-all duration-200 shadow-lg shadow-blue-500/25 disabled:opacity-50 flex items-center justify-center gap-2">
              <span *ngIf="!isLoading">🔍 Search Amadeus</span>
              <span *ngIf="isLoading" class="flex items-center gap-2">
                <span class="w-5 h-5 border-2 border-white/30 border-t-white rounded-full animate-spin"></span>
                Searching...
              </span>
            </button>
          </div>
        </form>

        <!-- Booking Search -->
        <form *ngIf="apiSource === 'booking'" (ngSubmit)="searchBooking()" class="grid grid-cols-1 md:grid-cols-4 gap-4">
          <div>
            <label class="block text-sm font-semibold text-slate-700 mb-2">Destination</label>
            <select [(ngModel)]="selectedCity" name="city"
                    class="w-full px-4 py-3 border border-slate-200 rounded-xl focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-all">
              <option *ngFor="let city of cityCodes" [ngValue]="city">{{ city.name }}</option>
            </select>
          </div>
          <div>
            <label class="block text-sm font-semibold text-slate-700 mb-2">Check-in</label>
            <input type="date" [(ngModel)]="checkinDate" name="checkin"
                   class="w-full px-4 py-3 border border-slate-200 rounded-xl focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-all">
          </div>
          <div>
            <label class="block text-sm font-semibold text-slate-700 mb-2">Check-out</label>
            <input type="date" [(ngModel)]="checkoutDate" name="checkout"
                   class="w-full px-4 py-3 border border-slate-200 rounded-xl focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-all">
          </div>
          <div class="flex items-end">
            <button type="submit" [disabled]="isLoading"
                    class="w-full px-6 py-3 bg-gradient-to-r from-purple-600 to-pink-600 text-white font-bold rounded-xl hover:from-purple-700 hover:to-pink-700 transition-all duration-200 shadow-lg shadow-purple-500/25 disabled:opacity-50 flex items-center justify-center gap-2">
              <span *ngIf="!isLoading">🔍 Search Booking</span>
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
        [data]="hotels"
        [columns]="columns"
        [actions]="actions"
        [selectable]="true"
        [loading]="isLoading"
        idKey="hotelId"
        searchPlaceholder="Search hotels..."
        emptyTitle="No hotels found"
        emptyMessage="Try searching with different parameters"
        (selectionChange)="onSelectionChange($event)"
        (actionClick)="onActionClick($event)">
      </app-data-table>
    </div>
  `
})
export class ExternalHotelsComponent implements OnInit {
    private externalHotelService = inject(ExternalHotelService);
    private hotelService = inject(HotelService);
    private sweetAlert = inject(SweetAlertService);

    apiSource: 'amadeus' | 'booking' = 'amadeus';
    
    cityCodes = [
        { code: 'PAR', bookingId: '-553173', name: 'Paris' },
        { code: 'LON', bookingId: '-2601889', name: 'London' },
        { code: 'NYC', bookingId: '-2092174', name: 'New York' },
        { code: 'DXB', bookingId: '-782831', name: 'Dubai' },
        { code: 'BCN', bookingId: '-372490', name: 'Barcelona' },
        { code: 'ROM', bookingId: '-126693', name: 'Rome' }
    ];

    selectedCity = this.cityCodes[0];
    checkinDate = this.getDatePlus(7);
    checkoutDate = this.getDatePlus(14);

    hotels: HotelDTO[] = [];
    selectedHotels: HotelDTO[] = [];
    isLoading = false;

    columns: TableColumn[] = [
        { key: 'name', label: 'Hotel Name' },
        { key: 'city', label: 'City' },
        { key: 'stars', label: 'Stars', type: 'rating' },
        { key: 'rating', label: 'Rating', type: 'rating' },
        { key: 'minPrice', label: 'Price', type: 'currency' },
        { key: 'reviewCount', label: 'Reviews', type: 'number' }
    ];

    actions: TableAction[] = [
        { key: 'import', icon: '📥', label: 'Import to Database', color: 'success' },
        { key: 'view', icon: '👁️', label: 'View Details', color: 'primary' }
    ];

    ngOnInit(): void {}

    private getDatePlus(days: number): string {
        const date = new Date();
        date.setDate(date.getDate() + days);
        return date.toISOString().split('T')[0];
    }

    searchAmadeus(): void {
        this.isLoading = true;
        this.hotels = [];
        this.selectedHotels = [];

        this.externalHotelService.searchByCity(this.selectedCity.code).subscribe({
            next: (results) => {
                this.hotels = results;
                this.isLoading = false;
                if (results.length === 0) {
                    this.sweetAlert.info('No hotels found');
                }
            },
            error: (err) => {
                console.error('Error searching hotels:', err);
                this.sweetAlert.error('Failed to search hotels');
                this.isLoading = false;
            }
        });
    }

    searchBooking(): void {
        this.isLoading = true;
        this.hotels = [];
        this.selectedHotels = [];

        this.externalHotelService.searchViaBooking({
            destination: this.selectedCity.bookingId,
            checkinDate: this.checkinDate,
            checkoutDate: this.checkoutDate
        }).subscribe({
            next: (results) => {
                this.hotels = results;
                this.isLoading = false;
                if (results.length === 0) {
                    this.sweetAlert.info('No hotels found');
                }
            },
            error: (err) => {
                console.error('Error searching hotels:', err);
                this.sweetAlert.error('Failed to search hotels. The API might be unavailable.');
                this.isLoading = false;
            }
        });
    }

    onSelectionChange(selected: HotelDTO[]): void {
        this.selectedHotels = selected;
    }

    async onActionClick(event: { action: string, item: HotelDTO }): Promise<void> {
        if (event.action === 'import') {
            await this.importHotel(event.item);
        } else if (event.action === 'view') {
            this.sweetAlert.info(`Hotel: ${event.item.name}\nAddress: ${event.item.address || 'N/A'}\nRating: ${event.item.rating || 'N/A'}`);
        }
    }

    private async importHotel(dto: HotelDTO): Promise<void> {
        const hotel: Hotel = {
            name: dto.name || 'Unknown Hotel',
            address: dto.address || '',
            city: dto.city,
            country: dto.country,
            etoile: dto.stars
        };

        try {
            await firstValueFrom(this.hotelService.create(hotel));
            this.sweetAlert.success('Hotel imported successfully!');
        } catch (err) {
            console.error('Error importing hotel:', err);
            this.sweetAlert.error('Failed to import hotel');
        }
    }

    async importSelected(): Promise<void> {
        const confirmed = await this.sweetAlert.confirmDelete(`Import ${this.selectedHotels.length} hotel(s) to database?`);
        if (!confirmed.isConfirmed) return;

        this.sweetAlert.showLoading(`Importing ${this.selectedHotels.length} hotels...`);

        let successCount = 0;
        let errorCount = 0;

        for (const dto of this.selectedHotels) {
            try {
                const hotel: Hotel = {
                    name: dto.name || 'Unknown Hotel',
                    address: dto.address || '',
                    city: dto.city,
                    country: dto.country,
                    etoile: dto.stars
                };
                await firstValueFrom(this.hotelService.create(hotel));
                successCount++;
            } catch (err) {
                errorCount++;
            }
        }

        this.sweetAlert.closeLoading();

        if (successCount > 0) {
            this.sweetAlert.success(`Imported ${successCount} hotel(s)!` + (errorCount > 0 ? ` (${errorCount} failed)` : ''));
        }
    }
}
