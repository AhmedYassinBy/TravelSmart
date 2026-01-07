// src/app/features/internal/internal-activities/internal-activities.component.ts
import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { PageHeaderComponent, DataTableComponent, TableColumn, TableAction, ModalComponent, SkeletonLoaderComponent } from '../../../shared/components';
import { Activity } from '../../../models';
import { SweetAlertService } from '../../../core/sweetalert.service';
import { HttpService } from '../../../core/http.service';

@Component({
    selector: 'app-internal-activities',
    standalone: true,
    imports: [CommonModule, FormsModule, PageHeaderComponent, DataTableComponent, ModalComponent, SkeletonLoaderComponent],
    template: `
    <div class="p-8">
      <app-page-header 
        icon="🎯" 
        title="Gestion des Activités" 
        subtitle="Gérer les activités touristiques à associer aux circuits"
        source="database">
        <button headerActions (click)="openCreateModal()" 
                class="px-6 py-3 bg-gradient-to-r from-purple-600 to-indigo-600 text-white font-bold rounded-xl hover:from-purple-700 hover:to-indigo-700 transition-all duration-200 shadow-lg shadow-purple-500/25 flex items-center gap-2">
          <span>✚</span> Nouvelle Activité
        </button>
      </app-page-header>

      <!-- Statistics -->
      <div class="grid grid-cols-1 md:grid-cols-3 gap-6 mb-6">
        <div class="bg-white rounded-2xl shadow-lg border border-slate-100 p-6">
          <div class="flex items-center justify-between">
            <div>
              <p class="text-sm font-medium text-slate-500">Total Activités</p>
              <p class="text-3xl font-bold text-slate-800">{{ activities.length }}</p>
            </div>
            <div class="w-12 h-12 bg-purple-100 rounded-xl flex items-center justify-center text-2xl">🎯</div>
          </div>
        </div>
        <div class="bg-white rounded-2xl shadow-lg border border-slate-100 p-6">
          <div class="flex items-center justify-between">
            <div>
              <p class="text-sm font-medium text-slate-500">Prix Moyen</p>
              <p class="text-3xl font-bold text-slate-800">{{ avgPrice | currency }}</p>
            </div>
            <div class="w-12 h-12 bg-emerald-100 rounded-xl flex items-center justify-center text-2xl">💰</div>
          </div>
        </div>
        <div class="bg-white rounded-2xl shadow-lg border border-slate-100 p-6">
          <div class="flex items-center justify-between">
            <div>
              <p class="text-sm font-medium text-slate-500">Prix Max</p>
              <p class="text-3xl font-bold text-slate-800">{{ maxPrice | currency }}</p>
            </div>
            <div class="w-12 h-12 bg-amber-100 rounded-xl flex items-center justify-center text-2xl">⭐</div>
          </div>
        </div>
      </div>

      <!-- Info Banner -->
      <div class="bg-purple-50 border border-purple-200 rounded-xl p-4 mb-6 flex items-center gap-3">
        <span class="text-2xl">💡</span>
        <p class="text-purple-800 text-sm">
          <strong>Astuce:</strong> Créez des activités ici puis associez-les à vos circuits touristiques pour créer des packages complets.
        </p>
      </div>

      <!-- Data Table -->
      <app-skeleton-loader *ngIf="isLoading" [rowCount]="5" [columnCount]="4"></app-skeleton-loader>

      <app-data-table 
        *ngIf="!isLoading"
        [data]="activities"
        [columns]="columns"
        [actions]="actions"
        [selectable]="true"
        [loading]="isLoading"
        (actionClick)="onAction($event)"
        searchPlaceholder="Rechercher des activités..."
        emptyTitle="Aucune activité trouvée"
        emptyMessage="Créez votre première activité touristique">
      </app-data-table>
    </div>

    <!-- Create/Edit Modal -->
    <app-modal [isOpen]="isModalOpen" [title]="isEditing ? 'Modifier l\\'activité' : 'Nouvelle activité'" (close)="closeModal()" size="md">
      <div class="space-y-5">
        <div>
          <label class="block text-sm font-semibold text-slate-700 mb-2">Nom de l'activité *</label>
          <input type="text" [(ngModel)]="formData.name" name="name" required
                 class="w-full px-4 py-3 border border-slate-200 rounded-xl focus:ring-2 focus:ring-purple-500 focus:border-transparent transition-all"
                 placeholder="ex: Excursion en bateau, Randonnée guidée...">
        </div>
        
        <div>
          <label class="block text-sm font-semibold text-slate-700 mb-2">Description</label>
          <textarea [(ngModel)]="formData.description" name="description" rows="4"
                    class="w-full px-4 py-3 border border-slate-200 rounded-xl focus:ring-2 focus:ring-purple-500 focus:border-transparent transition-all"
                    placeholder="Décrivez l'activité en détail..."></textarea>
        </div>
        
        <div>
          <label class="block text-sm font-semibold text-slate-700 mb-2">Prix ($) *</label>
          <div class="relative">
            <span class="absolute left-4 top-1/2 -translate-y-1/2 text-slate-400">$</span>
            <input type="number" [(ngModel)]="formData.price" name="price" min="0" step="0.01" required
                   class="w-full pl-10 pr-4 py-3 border border-slate-200 rounded-xl focus:ring-2 focus:ring-purple-500 focus:border-transparent transition-all"
                   placeholder="0.00">
          </div>
        </div>
      </div>
      
      <div footer class="flex justify-end gap-3 pt-4">
        <button type="button" (click)="closeModal()"
                class="px-6 py-3 bg-slate-100 text-slate-700 font-semibold rounded-xl hover:bg-slate-200 transition-colors">
          Annuler
        </button>
        <button type="button" (click)="saveActivity()" [disabled]="isSaving || !formData.name"
                class="px-6 py-3 bg-gradient-to-r from-purple-600 to-indigo-600 text-white font-bold rounded-xl hover:from-purple-700 hover:to-indigo-700 transition-all disabled:opacity-50 flex items-center gap-2">
          <span *ngIf="isSaving" class="w-5 h-5 border-2 border-white/30 border-t-white rounded-full animate-spin"></span>
          {{ isEditing ? 'Mettre à jour' : 'Créer' }}
        </button>
      </div>
    </app-modal>

    <!-- View Details Modal -->
    <app-modal [isOpen]="isDetailsModalOpen" [title]="'Détails de l\\'activité'" (close)="closeDetailsModal()" size="md" [showFooter]="false">
      <div *ngIf="selectedActivity" class="space-y-6">
        <!-- Header -->
        <div class="flex items-start gap-4">
          <div class="w-16 h-16 bg-purple-100 rounded-2xl flex items-center justify-center text-3xl">🎯</div>
          <div class="flex-1">
            <h3 class="text-2xl font-bold text-slate-800">{{ selectedActivity.name }}</h3>
            <p class="text-emerald-600 font-bold text-xl mt-1">{{ selectedActivity.price | currency }}</p>
          </div>
        </div>
        
        <!-- Description -->
        <div *ngIf="selectedActivity.description" class="bg-slate-50 rounded-xl p-4">
          <h4 class="font-semibold text-slate-700 mb-2">Description</h4>
          <p class="text-slate-600">{{ selectedActivity.description }}</p>
        </div>
        
        <div *ngIf="!selectedActivity.description" class="bg-slate-50 rounded-xl p-4 text-center">
          <p class="text-slate-400 italic">Aucune description disponible</p>
        </div>

        <!-- Action Buttons -->
        <div class="flex justify-end gap-3 pt-4 border-t">
          <button (click)="openEditFromDetails()" 
                  class="px-6 py-3 bg-purple-100 text-purple-700 font-semibold rounded-xl hover:bg-purple-200 transition-colors flex items-center gap-2">
            ✏️ Modifier
          </button>
          <button (click)="deleteFromDetails()" 
                  class="px-6 py-3 bg-red-100 text-red-700 font-semibold rounded-xl hover:bg-red-200 transition-colors flex items-center gap-2">
            🗑️ Supprimer
          </button>
        </div>
      </div>
    </app-modal>
  `
})
export class InternalActivitiesComponent implements OnInit {
    private http = inject(HttpService);
    private sweetAlert = inject(SweetAlertService);

    activities: Activity[] = [];
    selectedActivity: Activity | null = null;
    
    isLoading = false;
    isModalOpen = false;
    isDetailsModalOpen = false;
    isEditing = false;
    isSaving = false;
    
    formData: Activity = this.getEmptyForm();

    columns: TableColumn[] = [
        { key: 'name', label: 'Nom de l\'activité' },
        { key: 'description', label: 'Description', width: '300px' },
        { key: 'price', label: 'Prix', type: 'currency', width: '120px' }
    ];

    actions: TableAction[] = [
        { key: 'view', label: 'Voir', icon: '👁️', color: 'blue' },
        { key: 'edit', label: 'Modifier', icon: '✏️', color: 'green' },
        { key: 'delete', label: 'Supprimer', icon: '🗑️', color: 'red' }
    ];

    get avgPrice(): number {
        if (!this.activities.length) return 0;
        return this.activities.reduce((sum, a) => sum + (a.price || 0), 0) / this.activities.length;
    }

    get maxPrice(): number {
        if (!this.activities.length) return 0;
        return Math.max(...this.activities.map(a => a.price || 0));
    }

    ngOnInit(): void {
        this.loadActivities();
    }

    loadActivities(): void {
        this.isLoading = true;
        this.http.get<Activity[]>('/admin/offers/activities').subscribe({
            next: (activities) => {
                this.activities = activities;
                this.isLoading = false;
            },
            error: (err) => {
                console.error('Error loading activities:', err);
                this.sweetAlert.error('Échec du chargement des activités');
                this.isLoading = false;
            }
        });
    }

    getEmptyForm(): Activity {
        return { 
            name: '', 
            description: '', 
            price: 0
        };
    }

    // Modal operations
    openCreateModal(): void {
        this.isEditing = false;
        this.formData = this.getEmptyForm();
        this.isModalOpen = true;
    }

    openEditModal(activity: Activity): void {
        this.isEditing = true;
        this.formData = { ...activity };
        this.isModalOpen = true;
    }

    closeModal(): void {
        this.isModalOpen = false;
        this.formData = this.getEmptyForm();
    }

    saveActivity(): void {
        if (!this.formData.name?.trim()) {
            this.sweetAlert.warning('Veuillez entrer un nom pour l\'activité');
            return;
        }

        if (this.formData.price === undefined || this.formData.price < 0) {
            this.sweetAlert.warning('Veuillez entrer un prix valide');
            return;
        }

        this.isSaving = true;

        const operation = this.isEditing
            ? this.http.put<Activity>(`/admin/offers/activities/${this.formData.id}`, this.formData)
            : this.http.post<Activity>('/admin/offers/activities', this.formData);

        operation.subscribe({
            next: () => {
                this.sweetAlert.success(this.isEditing ? 'Activité mise à jour avec succès' : 'Activité créée avec succès');
                this.closeModal();
                this.loadActivities();
                this.isSaving = false;
            },
            error: (err) => {
                console.error('Error saving activity:', err);
                this.sweetAlert.error('Échec de la sauvegarde de l\'activité');
                this.isSaving = false;
            }
        });
    }

    onAction(event: { action: string; item: Activity }): void {
        if (event.action === 'view') {
            this.viewDetails(event.item);
        } else if (event.action === 'edit') {
            this.openEditModal(event.item);
        } else if (event.action === 'delete') {
            this.deleteActivity(event.item);
        }
    }

    viewDetails(activity: Activity): void {
        this.selectedActivity = activity;
        this.isDetailsModalOpen = true;
    }

    closeDetailsModal(): void {
        this.isDetailsModalOpen = false;
        this.selectedActivity = null;
    }

    openEditFromDetails(): void {
        if (this.selectedActivity) {
            this.closeDetailsModal();
            this.openEditModal(this.selectedActivity);
        }
    }

    deleteFromDetails(): void {
        if (this.selectedActivity) {
            const activity = this.selectedActivity;
            this.closeDetailsModal();
            this.deleteActivity(activity);
        }
    }

    deleteActivity(activity: Activity): void {
        this.sweetAlert.confirm(
            `Êtes-vous sûr de vouloir supprimer "${activity.name}" ?`,
            'Cette action est irréversible. L\'activité sera retirée de tous les circuits associés.'
        ).then((confirmed) => {
            if (confirmed) {
                this.http.delete(`/admin/offers/activities/${activity.id}`).subscribe({
                    next: () => {
                        this.sweetAlert.success('Activité supprimée avec succès');
                        this.loadActivities();
                    },
                    error: (err) => {
                        console.error('Error deleting activity:', err);
                        this.sweetAlert.error('Échec de la suppression de l\'activité');
                    }
                });
            }
        });
    }
}
