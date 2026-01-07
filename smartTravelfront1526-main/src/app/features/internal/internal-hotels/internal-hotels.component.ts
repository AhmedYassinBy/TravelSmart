// src/app/features/internal/internal-hotels/internal-hotels.component.ts
import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { PageHeaderComponent, DataTableComponent, TableColumn, TableAction, ModalComponent, SkeletonLoaderComponent } from '../../../shared/components';
import { HotelService } from '../../../services/hotel.service';
import { Hotel, Room } from '../../../models';
import { SweetAlertService } from '../../../core/sweetalert.service';

@Component({
    selector: 'app-internal-hotels',
    standalone: true,
    imports: [CommonModule, FormsModule, PageHeaderComponent, DataTableComponent, ModalComponent, SkeletonLoaderComponent],
    template: `
    <div class="p-8">
      <app-page-header 
        icon="🏨" 
        title="Internal Hotels" 
        subtitle="Manage your hotel inventory stored in the database"
        source="database">
        <button (click)="openCreateModal()" 
                class="px-6 py-3 bg-gradient-to-r from-emerald-600 to-teal-600 text-white font-bold rounded-xl hover:from-emerald-700 hover:to-teal-700 transition-all duration-200 shadow-lg shadow-emerald-500/25 flex items-center gap-2">
          <span>✚</span> Add Hotel
        </button>
      </app-page-header>

      <!-- Statistics -->
      <div class="grid grid-cols-1 md:grid-cols-4 gap-6 mb-6">
        <div class="bg-white rounded-2xl shadow-lg border border-slate-100 p-6">
          <div class="flex items-center justify-between">
            <div>
              <p class="text-sm font-medium text-slate-500">Total Hotels</p>
              <p class="text-3xl font-bold text-slate-800">{{ hotels.length }}</p>
            </div>
            <div class="w-12 h-12 bg-blue-100 rounded-xl flex items-center justify-center text-2xl">🏨</div>
          </div>
        </div>
        <div class="bg-white rounded-2xl shadow-lg border border-slate-100 p-6">
          <div class="flex items-center justify-between">
            <div>
              <p class="text-sm font-medium text-slate-500">Total Rooms</p>
              <p class="text-3xl font-bold text-slate-800">{{ totalRooms }}</p>
            </div>
            <div class="w-12 h-12 bg-emerald-100 rounded-xl flex items-center justify-center text-2xl">🛏️</div>
          </div>
        </div>
        <div class="bg-white rounded-2xl shadow-lg border border-slate-100 p-6">
          <div class="flex items-center justify-between">
            <div>
              <p class="text-sm font-medium text-slate-500">Avg. Stars</p>
              <p class="text-3xl font-bold text-slate-800">{{ avgStars | number:'1.1-1' }} ⭐</p>
            </div>
            <div class="w-12 h-12 bg-amber-100 rounded-xl flex items-center justify-center text-2xl">⭐</div>
          </div>
        </div>
        <div class="bg-white rounded-2xl shadow-lg border border-slate-100 p-6">
          <div class="flex items-center justify-between">
            <div>
              <p class="text-sm font-medium text-slate-500">Cities</p>
              <p class="text-3xl font-bold text-slate-800">{{ uniqueCities }}</p>
            </div>
            <div class="w-12 h-12 bg-purple-100 rounded-xl flex items-center justify-center text-2xl">🌆</div>
          </div>
        </div>
      </div>

      <!-- Data Table -->
      <app-skeleton-loader *ngIf="isLoading" [rowCount]="5" [columnCount]="6"></app-skeleton-loader>

      <app-data-table 
        *ngIf="!isLoading"
        [data]="hotels"
        [columns]="columns"
        [actions]="actions"
        [selectable]="true"
        [loading]="isLoading"
        (actionClick)="onAction($event)"
        searchPlaceholder="Search hotels..."
        emptyTitle="No hotels found"
        emptyMessage="Start by adding your first hotel">
      </app-data-table>
    </div>

    <!-- Create/Edit Hotel Modal -->
    <app-modal [isOpen]="isHotelModalOpen" [title]="isEditing ? 'Edit Hotel' : 'Add New Hotel'" (close)="closeHotelModal()" [showFooter]="false">
      <form (ngSubmit)="saveHotel()" class="space-y-4">
        <div>
          <label class="block text-sm font-semibold text-slate-700 mb-2">Hotel Name *</label>
          <input type="text" [(ngModel)]="formData.name" name="name" required
                 class="w-full px-4 py-3 border border-slate-200 rounded-xl focus:ring-2 focus:ring-blue-500"
                 placeholder="e.g. Grand Hotel Paris">
        </div>
        <div>
          <label class="block text-sm font-semibold text-slate-700 mb-2">Address *</label>
          <input type="text" [(ngModel)]="formData.address" name="address" required
                 class="w-full px-4 py-3 border border-slate-200 rounded-xl focus:ring-2 focus:ring-blue-500"
                 placeholder="e.g. 123 Main Street">
        </div>
        <div class="grid grid-cols-2 gap-4">
          <div>
            <label class="block text-sm font-semibold text-slate-700 mb-2">City</label>
            <input type="text" [(ngModel)]="formData.city" name="city"
                   class="w-full px-4 py-3 border border-slate-200 rounded-xl focus:ring-2 focus:ring-blue-500"
                   placeholder="e.g. Paris">
          </div>
          <div>
            <label class="block text-sm font-semibold text-slate-700 mb-2">Country</label>
            <input type="text" [(ngModel)]="formData.country" name="country"
                   class="w-full px-4 py-3 border border-slate-200 rounded-xl focus:ring-2 focus:ring-blue-500"
                   placeholder="e.g. France">
          </div>
        </div>
        <div>
          <label class="block text-sm font-semibold text-slate-700 mb-2">Stars Rating</label>
          <div class="flex gap-2">
            <button *ngFor="let star of [1,2,3,4,5]" type="button"
                    (click)="formData.etoile = star"
                    [class]="formData.etoile && formData.etoile >= star ? 'text-amber-400' : 'text-slate-300'"
                    class="text-3xl hover:scale-110 transition-transform">
              ★
            </button>
          </div>
        </div>
        <div class="flex justify-end gap-3 pt-4 border-t">
          <button type="button" (click)="closeHotelModal()"
                  class="px-6 py-3 bg-slate-100 text-slate-700 font-semibold rounded-xl hover:bg-slate-200 transition-colors">
            Cancel
          </button>
          <button type="submit" [disabled]="isSaving"
                  class="px-6 py-3 bg-gradient-to-r from-blue-600 to-indigo-600 text-white font-bold rounded-xl hover:from-blue-700 hover:to-indigo-700 transition-all disabled:opacity-50 flex items-center gap-2">
            <span *ngIf="isSaving" class="w-5 h-5 border-2 border-white/30 border-t-white rounded-full animate-spin"></span>
            {{ isEditing ? 'Update Hotel' : 'Create Hotel' }}
          </button>
        </div>
      </form>
    </app-modal>

    <!-- Rooms Modal -->
    <app-modal [isOpen]="isRoomsModalOpen" [title]="'Manage Rooms - ' + (selectedHotel?.name || '')" (close)="closeRoomsModal()" size="lg" [showFooter]="false">
      <div class="space-y-4">
        <!-- Add Room Form -->
        <div class="bg-slate-50 rounded-xl p-4">
          <h4 class="font-semibold text-slate-800 mb-3 flex items-center gap-2">
            <span>➕</span> Add New Room
          </h4>
          <div class="grid grid-cols-4 gap-3">
            <input type="text" [(ngModel)]="roomFormData.roomNumber" 
                   class="px-3 py-2 border border-slate-200 rounded-lg focus:ring-2 focus:ring-blue-500"
                   placeholder="Room #">
            <select [(ngModel)]="roomFormData.type"
                    class="px-3 py-2 border border-slate-200 rounded-lg focus:ring-2 focus:ring-blue-500">
              <option value="">Type</option>
              <option value="Standard">Standard</option>
              <option value="Deluxe">Deluxe</option>
              <option value="Suite">Suite</option>
              <option value="Presidential">Presidential</option>
            </select>
            <input type="number" [(ngModel)]="roomFormData.price" 
                   class="px-3 py-2 border border-slate-200 rounded-lg focus:ring-2 focus:ring-blue-500"
                   placeholder="Price">
            <button (click)="addRoom()" [disabled]="!roomFormData.roomNumber"
                    class="px-4 py-2 bg-emerald-600 text-white font-semibold rounded-lg hover:bg-emerald-700 disabled:opacity-50 transition-colors">
              Add
            </button>
          </div>
        </div>

        <!-- Rooms List -->
        <div class="space-y-2 max-h-80 overflow-y-auto">
          <div *ngFor="let room of rooms" 
               class="flex items-center justify-between p-4 bg-white border border-slate-200 rounded-xl hover:border-blue-200 transition-colors">
            <div class="flex items-center gap-4">
              <div class="w-12 h-12 bg-blue-100 rounded-lg flex items-center justify-center text-xl">🛏️</div>
              <div>
                <p class="font-semibold text-slate-800">Room {{ room.roomNumber }}</p>
                <p class="text-sm text-slate-500">{{ room.type || 'Standard' }}</p>
              </div>
            </div>
            <div class="flex items-center gap-4">
              <span class="font-bold text-emerald-600">{{ room.price | currency }}</span>
              <span [class]="room.available ? 'bg-emerald-100 text-emerald-700' : 'bg-red-100 text-red-700'"
                    class="px-3 py-1 rounded-full text-xs font-semibold">
                {{ room.available ? 'Available' : 'Occupied' }}
              </span>
            </div>
          </div>
          <div *ngIf="rooms.length === 0" class="text-center py-8 text-slate-500">
            No rooms added yet
          </div>
        </div>
      </div>
    </app-modal>
  `
})
export class InternalHotelsComponent implements OnInit {
    private hotelService = inject(HotelService);
    private sweetAlert = inject(SweetAlertService);

    hotels: Hotel[] = [];
    rooms: Room[] = [];
    selectedHotel: Hotel | null = null;
    isLoading = false;
    isHotelModalOpen = false;
    isRoomsModalOpen = false;
    isEditing = false;
    isSaving = false;

    formData: Hotel = this.getEmptyHotelForm();
    roomFormData: Room = this.getEmptyRoomForm();

    columns: TableColumn[] = [
        { key: 'name', label: 'Hotel Name' },
        { key: 'address', label: 'Address' },
        { key: 'city', label: 'City', width: '120px' },
        { key: 'country', label: 'Country', width: '120px' },
        { key: 'etoile', label: 'Stars', width: '80px' }
    ];

    actions: TableAction[] = [
        { key: 'rooms', label: 'Rooms', icon: '🛏️', color: 'green' },
        { key: 'edit', label: 'Edit', icon: '✏️', color: 'blue' },
        { key: 'delete', label: 'Delete', icon: '🗑️', color: 'red' }
    ];

    get totalRooms(): number {
        return this.hotels.reduce((sum, h) => sum + (h.rooms?.length || 0), 0);
    }

    get avgStars(): number {
        if (!this.hotels.length) return 0;
        const total = this.hotels.reduce((sum, h) => sum + (h.etoile || 0), 0);
        return total / this.hotels.length;
    }

    get uniqueCities(): number {
        return new Set(this.hotels.filter(h => h.city).map(h => h.city)).size;
    }

    ngOnInit(): void {
        this.loadHotels();
    }

    loadHotels(): void {
        this.isLoading = true;
        this.hotelService.findAll().subscribe({
            next: (hotels) => {
                this.hotels = hotels;
                this.isLoading = false;
            },
            error: (err) => {
                console.error('Error loading hotels:', err);
                this.sweetAlert.error('Failed to load hotels');
                this.isLoading = false;
            }
        });
    }

    getEmptyHotelForm(): Hotel {
        return { name: '', address: '', city: '', country: '', etoile: 3 };
    }

    getEmptyRoomForm(): Room {
        return { roomNumber: '', type: '', price: 0, available: true };
    }

    openCreateModal(): void {
        this.isEditing = false;
        this.formData = this.getEmptyHotelForm();
        this.isHotelModalOpen = true;
    }

    openEditModal(hotel: Hotel): void {
        this.isEditing = true;
        this.formData = { ...hotel };
        this.isHotelModalOpen = true;
    }

    closeHotelModal(): void {
        this.isHotelModalOpen = false;
        this.formData = this.getEmptyHotelForm();
    }

    saveHotel(): void {
        if (!this.formData.name || !this.formData.address) {
            this.sweetAlert.warning('Please fill in all required fields');
            return;
        }

        this.isSaving = true;

        const operation = this.isEditing
            ? this.hotelService.update(this.formData.id!, this.formData)
            : this.hotelService.create(this.formData);

        operation.subscribe({
            next: () => {
                this.sweetAlert.success(this.isEditing ? 'Hotel updated successfully' : 'Hotel created successfully');
                this.closeHotelModal();
                this.loadHotels();
                this.isSaving = false;
            },
            error: (err) => {
                console.error('Error saving hotel:', err);
                this.sweetAlert.error('Failed to save hotel');
                this.isSaving = false;
            }
        });
    }

    onAction(event: { action: string; item: Hotel }): void {
        if (event.action === 'edit') {
            this.openEditModal(event.item);
        } else if (event.action === 'delete') {
            this.deleteHotel(event.item);
        } else if (event.action === 'rooms') {
            this.openRoomsModal(event.item);
        }
    }

    deleteHotel(hotel: Hotel): void {
        this.sweetAlert.confirm(
            `Are you sure you want to delete ${hotel.name}?`,
            'This will also delete all rooms. This action cannot be undone.'
        ).then((confirmed) => {
            if (confirmed) {
                this.hotelService.delete(hotel.id!).subscribe({
                    next: () => {
                        this.sweetAlert.success('Hotel deleted successfully');
                        this.loadHotels();
                    },
                    error: (err) => {
                        console.error('Error deleting hotel:', err);
                        this.sweetAlert.error('Failed to delete hotel');
                    }
                });
            }
        });
    }

    openRoomsModal(hotel: Hotel): void {
        this.selectedHotel = hotel;
        this.isRoomsModalOpen = true;
        this.loadRooms(hotel.id!);
    }

    closeRoomsModal(): void {
        this.isRoomsModalOpen = false;
        this.selectedHotel = null;
        this.rooms = [];
    }

    loadRooms(hotelId: string): void {
        this.hotelService.getRooms(hotelId).subscribe({
            next: (rooms) => this.rooms = rooms,
            error: (err) => console.error('Error loading rooms:', err)
        });
    }

    addRoom(): void {
        if (!this.selectedHotel || !this.roomFormData.roomNumber) return;

        this.hotelService.addRoom(this.selectedHotel.id!, this.roomFormData).subscribe({
            next: () => {
                this.sweetAlert.success('Room added successfully');
                this.loadRooms(this.selectedHotel!.id!);
                this.roomFormData = this.getEmptyRoomForm();
            },
            error: (err) => {
                console.error('Error adding room:', err);
                this.sweetAlert.error('Failed to add room');
            }
        });
    }
}
