// src/app/shared/components/modal/modal.component.ts
import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
    selector: 'app-modal',
    standalone: true,
    imports: [CommonModule],
    template: `
    <div *ngIf="isOpen" 
         class="fixed inset-0 z-50 flex items-center justify-center p-4"
         (click)="onBackdropClick($event)">
      <!-- Backdrop -->
      <div class="absolute inset-0 bg-slate-900/60 backdrop-blur-sm animate-fadeIn"></div>
      
      <!-- Modal Content -->
      <div class="relative bg-white rounded-2xl shadow-2xl w-full overflow-hidden animate-slideUp"
           [style.max-width]="modalMaxWidth"
           (click)="$event.stopPropagation()">
        
        <!-- Header -->
        <div class="flex items-center justify-between p-6 border-b border-slate-100 bg-gradient-to-r from-slate-50 to-white">
          <div class="flex items-center gap-3">
            <span *ngIf="icon" class="text-2xl">{{ icon }}</span>
            <h2 class="text-xl font-bold text-slate-800">{{ title }}</h2>
          </div>
          <button (click)="close.emit()" 
                  class="p-2 rounded-lg hover:bg-slate-100 text-slate-400 hover:text-slate-600 transition-colors">
            ✕
          </button>
        </div>
        
        <!-- Body -->
        <div class="p-6 max-h-[70vh] overflow-y-auto">
          <ng-content></ng-content>
        </div>
        
        <!-- Footer -->
        <div *ngIf="showFooter" class="flex items-center justify-end gap-3 p-6 border-t border-slate-100 bg-slate-50/50">
          <ng-content select="[modalFooter]"></ng-content>
          <button *ngIf="showCancel" 
                  (click)="close.emit()"
                  class="px-5 py-2.5 rounded-xl border border-slate-200 text-slate-600 font-semibold hover:bg-slate-100 transition-colors">
            {{ cancelText }}
          </button>
          <button *ngIf="showConfirm"
                  (click)="confirm.emit()"
                  [disabled]="confirmDisabled"
                  class="px-5 py-2.5 rounded-xl font-semibold text-white transition-all duration-200 disabled:opacity-50 disabled:cursor-not-allowed"
                  [ngClass]="confirmButtonClass">
            {{ confirmText }}
          </button>
        </div>
      </div>
    </div>
  `,
    styles: [`
    @keyframes fadeIn {
      from { opacity: 0; }
      to { opacity: 1; }
    }
    @keyframes slideUp {
      from { opacity: 0; transform: translateY(20px) scale(0.95); }
      to { opacity: 1; transform: translateY(0) scale(1); }
    }
    .animate-fadeIn {
      animation: fadeIn 0.2s ease-out;
    }
    .animate-slideUp {
      animation: slideUp 0.3s ease-out;
    }
  `]
})
export class ModalComponent {
    @Input() isOpen = false;
    @Input() title = 'Modal Title';
    @Input() icon?: string;
    @Input() maxWidth = '32rem';
    @Input() size: 'sm' | 'md' | 'lg' | 'xl' = 'md';
    @Input() showFooter = true;
    @Input() showCancel = true;
    @Input() showConfirm = true;
    @Input() cancelText = 'Cancel';
    @Input() confirmText = 'Confirm';
    @Input() confirmDisabled = false;
    @Input() confirmColor: 'primary' | 'danger' | 'success' = 'primary';
    @Input() closeOnBackdrop = true;

    @Output() close = new EventEmitter<void>();
    @Output() confirm = new EventEmitter<void>();

    get modalMaxWidth(): string {
        if (this.maxWidth !== '32rem') return this.maxWidth; // Use explicit maxWidth if set
        const sizes: Record<string, string> = {
            sm: '24rem',
            md: '32rem',
            lg: '48rem',
            xl: '64rem'
        };
        return sizes[this.size] || sizes['md'];
    }

    get confirmButtonClass(): string {
        const colors: Record<string, string> = {
            primary: 'bg-gradient-to-r from-blue-600 to-cyan-600 hover:from-blue-700 hover:to-cyan-700 shadow-lg shadow-blue-500/25',
            danger: 'bg-gradient-to-r from-red-600 to-rose-600 hover:from-red-700 hover:to-rose-700 shadow-lg shadow-red-500/25',
            success: 'bg-gradient-to-r from-emerald-600 to-teal-600 hover:from-emerald-700 hover:to-teal-700 shadow-lg shadow-emerald-500/25'
        };
        return colors[this.confirmColor] || colors['primary'];
    }

    onBackdropClick(event: MouseEvent): void {
        if (this.closeOnBackdrop) {
            this.close.emit();
        }
    }
}
