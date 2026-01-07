// src/app/shared/dialog.component.ts
import { Component, Input, OnDestroy, OnChanges } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { CrudService } from '../core/crud.service';
import { TitleCasePipe } from '@angular/common';

@Component({
  selector: 'app-dialog',
  standalone: true,
  imports: [CommonModule, FormsModule, TitleCasePipe],
template: `
<style>
    
</style>
  <!-- Trigger Button - Clean Design -->
  <button
    (click)="open = true"
    class="bg-gradient-to-r from-red-600 to-rose-600 hover:from-red-700 hover:to-rose-700 text-white font-semibold px-6 py-3 rounded-xl shadow-lg hover:shadow-xl transition-all duration-300 transform hover:scale-105 flex items-center gap-2"
  >
    <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4"></path>
    </svg>
    Add New
  </button>

  <!-- Modal Overlay -->
  <div
    *ngIf="open"
    class="fixed inset-0 z-[10000] flex items-center justify-center p-4 bg-black/60 backdrop-blur-sm animate-fadeIn"
    (click)="closeDialog()"
  >
    <!-- Modal Content -->
    <div
      (click)="$event.stopPropagation()"
      class="bg-white w-full max-w-2xl rounded-2xl shadow-2xl animate-scaleIn overflow-hidden"
      style="max-height: 90vh;"
    >
      <!-- Header -->
      <div class="px-8 py-6 border-b border-gray-200 bg-gradient-to-r from-red-600 to-rose-600">
        <div class="flex items-center justify-between">
          <h2 class="text-2xl font-bold text-white">
            {{ entity?.id ? 'Edit Item' : 'Add New Item' }}
          </h2>
          <button
            type="button"
            (click)="closeDialog()"
            class="text-white hover:text-red-100 transition-colors p-1 hover:bg-white/10 rounded-lg"
          >
            <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path>
            </svg>
          </button>
        </div>
      </div>

      <!-- Form -->
      <form #f="ngForm" (ngSubmit)="save()" class="px-8 py-6 overflow-y-auto bg-gray-50" style="max-height: calc(90vh - 180px);">
        <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
          <div *ngFor="let col of columns" [ngClass]="col.toLowerCase().includes('description') ? 'md:col-span-2' : ''" class="space-y-2">
            <label class="text-sm font-semibold text-gray-700 flex items-center gap-1">
              {{ col | titlecase }}
              <span class="text-red-500">*</span>
            </label>
            <div class="relative">
              <input
                *ngIf="!col.toLowerCase().includes('description')"
                [(ngModel)]="model[col]"
                [name]="col"
                [type]="col.toLowerCase().includes('time') || col.toLowerCase().includes('date') ? 'datetime-local' : col.toLowerCase().includes('email') ? 'email' : col.toLowerCase().includes('price') || col.toLowerCase().includes('seats') || col.toLowerCase().includes('etoile') || col.toLowerCase().includes('duree') || col.toLowerCase().includes('prix') ? 'number' : 'text'"
                required
                (focus)="focusedField = col"
                (blur)="focusedField = null"
                class="w-full px-4 py-3 rounded-lg border-2 border-gray-300 focus:border-red-500 focus:ring-2 focus:ring-red-100 outline-none transition-all bg-white"
                [placeholder]="'Enter ' + (col | titlecase)"
              >
              <textarea
                *ngIf="col.toLowerCase().includes('description')"
                [(ngModel)]="model[col]"
                [name]="col"
                required
                (focus)="focusedField = col"
                (blur)="focusedField = null"
                rows="4"
                class="w-full px-4 py-3 rounded-lg border-2 border-gray-300 focus:border-red-500 focus:ring-2 focus:ring-red-100 outline-none transition-all bg-white resize-none"
                [placeholder]="'Enter ' + (col | titlecase)"
              ></textarea>
            </div>
            <!-- Helper Text -->
            <div 
              *ngIf="focusedField === col" 
              class="flex items-start gap-2 p-3 bg-blue-50 border-l-4 border-blue-400 rounded-r-lg animate-slideDown"
            >
              <svg class="w-4 h-4 text-blue-600 flex-shrink-0 mt-0.5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"></path>
              </svg>
              <p class="text-xs text-blue-700 font-medium">{{ getFieldHint(col) }}</p>
            </div>
          </div>
        </div>
      </form>

      <!-- Footer -->
      <div class="px-8 py-5 border-t border-gray-200 bg-white flex items-center justify-end gap-3">
        <button
          type="button"
          (click)="closeDialog()"
          class="px-6 py-3 rounded-lg border-2 border-gray-300 hover:bg-gray-50 text-gray-700 font-semibold transition-all hover:border-gray-400"
        >
          Cancel
        </button>
        <button
          type="submit"
          (click)="f.ngSubmit.emit()"
          [disabled]="!f.valid"
          class="px-6 py-3 rounded-lg bg-gradient-to-r from-red-600 to-rose-600 hover:from-red-700 hover:to-rose-700 text-white font-semibold shadow-lg hover:shadow-xl transition-all disabled:opacity-50 disabled:cursor-not-allowed flex items-center gap-2"
        >
          <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7"></path>
          </svg>
          {{ entity?.id ? 'Update' : 'Create' }}
        </button>
      </div>
    </div>
  </div>
`,

})
export class DialogComponent<T extends { id?: string }> implements OnChanges, OnDestroy {
  @Input() service!: CrudService<T>;
  @Input() columns!: (keyof T & string)[];  // ← THIS IS THE KEY FIX
  @Input() entity?: T;

  private _open = false;

  get open(): boolean {
    return this._open;
  }

  set open(value: boolean) {
    this._open = value;
    if (value) {
      document.body.style.overflow = 'hidden';
    } else {
      document.body.style.overflow = '';
    }
  }

  model: any = {};
  focusedField: string | null = null;

  getFieldHint(field: string): string {
    const hints: Record<string, string> = {
      'airline': 'Enter the airline name (e.g., Air France, Emirates)',
      'flightNumber': 'Enter flight number (e.g., AF123, EK456)',
      'origin': 'Enter departure city or airport code (e.g., Paris CDG)',
      'destination': 'Enter arrival city or airport code (e.g., New York JFK)',
      'departureTime': 'Select departure date and time',
      'arrivalTime': 'Select arrival date and time',
      'price': 'Enter ticket price in euros (e.g., 299.99)',
      'seatsAvailable': 'Enter number of available seats',
      'name': 'Enter the hotel name (e.g., Hilton Paris)',
      'address': 'Enter complete street address',
      'city': 'Enter city name (e.g., Paris, London)',
      'country': 'Enter country name (e.g., France, UK)',
      'etoile': 'Enter star rating (1-5)',
      'title': 'Enter a descriptive title for the tour',
      'description': 'Provide detailed information about the tour',
      'duree': 'Enter duration in days',
      'prix': 'Enter total price in euros',
      'username': 'Enter unique username (letters and numbers only)',
      'email': 'Enter valid email address (e.g., user@example.com)',
      'firstName': 'Enter first name',
      'lastName': 'Enter last name',
      'role': 'Select user role (ROLE_USER or ROLE_ADMIN)'
    };
    return hints[field] || `Enter ${field}`;
  }

  ngOnChanges() {
    this.model = this.entity ? { ...this.entity } : {};
  }

  closeDialog() {
    this.open = false;
  }

  save() {
    const obs = this.model.id
      ? this.service.update(this.model.id, this.model)
      : this.service.create(this.model);
    obs.subscribe(() => {
      this.open = false;
      // Trigger refresh in parent
      this.service.findAll();
    });
  }

  ngOnDestroy() {
    // Clean up body overflow when component is destroyed
    document.body.style.overflow = '';
  }
}