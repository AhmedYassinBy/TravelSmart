// src/app/features/internal/internal-circuits/internal-circuits.component.ts
import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { PageHeaderComponent, DataTableComponent, TableColumn, TableAction, ModalComponent, SkeletonLoaderComponent } from '../../../shared/components';
import { Circuit, Hotel, Activity, Etape } from '../../../models';
import { SweetAlertService } from '../../../core/sweetalert.service';
import { HttpService } from '../../../core/http.service';

@Component({
    selector: 'app-internal-circuits',
    standalone: true,
    imports: [CommonModule, FormsModule, PageHeaderComponent, DataTableComponent, ModalComponent, SkeletonLoaderComponent],
    template: `
    <div class="p-8">
      <app-page-header 
        icon="🗺️" 
        title="Gestion des Circuits" 
        subtitle="Gérer les circuits touristiques avec itinéraires détaillés"
        source="database">
        <button headerActions (click)="openCreateModal()" 
                class="px-6 py-3 bg-gradient-to-r from-emerald-600 to-teal-600 text-white font-bold rounded-xl hover:from-emerald-700 hover:to-teal-700 transition-all duration-200 shadow-lg shadow-emerald-500/25 flex items-center gap-2">
          <span>✚</span> Nouveau Circuit
        </button>
      </app-page-header>

      <!-- Statistics -->
      <div class="grid grid-cols-1 md:grid-cols-4 gap-6 mb-6">
        <div class="bg-white rounded-2xl shadow-lg border border-slate-100 p-6">
          <div class="flex items-center justify-between">
            <div>
              <p class="text-sm font-medium text-slate-500">Total Circuits</p>
              <p class="text-3xl font-bold text-slate-800">{{ circuits.length }}</p>
            </div>
            <div class="w-12 h-12 bg-indigo-100 rounded-xl flex items-center justify-center text-2xl">🗺️</div>
          </div>
        </div>
        <div class="bg-white rounded-2xl shadow-lg border border-slate-100 p-6">
          <div class="flex items-center justify-between">
            <div>
              <p class="text-sm font-medium text-slate-500">Avg. Duration</p>
              <p class="text-3xl font-bold text-slate-800">{{ avgDuration | number:'1.0-0' }} days</p>
            </div>
            <div class="w-12 h-12 bg-blue-100 rounded-xl flex items-center justify-center text-2xl">📅</div>
          </div>
        </div>
        <div class="bg-white rounded-2xl shadow-lg border border-slate-100 p-6">
          <div class="flex items-center justify-between">
            <div>
              <p class="text-sm font-medium text-slate-500">Avg. Price</p>
              <p class="text-3xl font-bold text-slate-800">{{ avgPrice | currency }}</p>
            </div>
            <div class="w-12 h-12 bg-emerald-100 rounded-xl flex items-center justify-center text-2xl">💰</div>
          </div>
        </div>
        <div class="bg-white rounded-2xl shadow-lg border border-slate-100 p-6">
          <div class="flex items-center justify-between">
            <div>
              <p class="text-sm font-medium text-slate-500">Total Steps</p>
              <p class="text-3xl font-bold text-slate-800">{{ totalEtapes }}</p>
            </div>
            <div class="w-12 h-12 bg-amber-100 rounded-xl flex items-center justify-center text-2xl">📍</div>
          </div>
        </div>
      </div>

      <!-- Info Banner -->
      <div class="bg-blue-50 border border-blue-200 rounded-xl p-4 mb-6 flex items-center gap-3">
        <span class="text-2xl">💡</span>
        <p class="text-blue-800 text-sm">
          <strong>Gestion des circuits:</strong> Créez des circuits complets avec des étapes, durée, description et prix. Associez des hôtels et activités à chaque circuit.
        </p>
      </div>

      <!-- Data Table -->
      <app-skeleton-loader *ngIf="isLoading" [rowCount]="5" [columnCount]="5"></app-skeleton-loader>

      <app-data-table 
        *ngIf="!isLoading"
        [data]="circuits"
        [columns]="columns"
        [actions]="actions"
        [selectable]="true"
        [loading]="isLoading"
        (actionClick)="onAction($event)"
        searchPlaceholder="Rechercher des circuits..."
        emptyTitle="Aucun circuit trouvé"
        emptyMessage="Créez votre premier circuit touristique">
      </app-data-table>
    </div>

    <!-- Create/Edit Modal (Multi-step Form) -->
    <app-modal [isOpen]="isModalOpen" [title]="getModalTitle()" (close)="closeModal()" size="xl" [showFooter]="false">
      <div class="space-y-6">
        <!-- Step Indicator -->
        <div class="flex items-center justify-between mb-6">
          <div *ngFor="let step of formSteps; let i = index" 
               class="flex items-center"
               [class.flex-1]="i < formSteps.length - 1">
            <div (click)="goToStep(i)"
                 class="w-10 h-10 rounded-full flex items-center justify-center font-bold text-sm cursor-pointer transition-all"
                 [class.bg-indigo-600]="currentStep >= i"
                 [class.text-white]="currentStep >= i"
                 [class.bg-slate-200]="currentStep < i"
                 [class.text-slate-500]="currentStep < i">
              {{ i + 1 }}
            </div>
            <div *ngIf="i < formSteps.length - 1" 
                 class="flex-1 h-1 mx-2 rounded"
                 [class.bg-indigo-600]="currentStep > i"
                 [class.bg-slate-200]="currentStep <= i">
            </div>
          </div>
        </div>
        <div class="flex justify-between text-xs text-slate-500 px-2 -mt-4 mb-4">
          <span *ngFor="let step of formSteps">{{ step }}</span>
        </div>

        <!-- Step 1: Basic Info -->
        <div *ngIf="currentStep === 0" class="space-y-4 animate-fadeIn">
          <div>
            <label class="block text-sm font-semibold text-slate-700 mb-2">Titre du circuit *</label>
            <input type="text" [(ngModel)]="formData.title" name="title" required
                   class="w-full px-4 py-3 border border-slate-200 rounded-xl focus:ring-2 focus:ring-indigo-500 focus:border-transparent"
                   placeholder="ex: Aventure Méditerranéenne">
          </div>
          <div>
            <label class="block text-sm font-semibold text-slate-700 mb-2">Description</label>
            <textarea [(ngModel)]="formData.description" name="description" rows="4"
                      class="w-full px-4 py-3 border border-slate-200 rounded-xl focus:ring-2 focus:ring-indigo-500 focus:border-transparent"
                      placeholder="Décrivez l'expérience de voyage..."></textarea>
          </div>
          <div class="grid grid-cols-2 gap-4">
            <div>
              <label class="block text-sm font-semibold text-slate-700 mb-2">Durée totale (jours)</label>
              <div class="relative">
                <input type="number" [(ngModel)]="formData.duree" name="duree" min="1"
                       class="w-full px-4 py-3 border border-slate-200 rounded-xl focus:ring-2 focus:ring-indigo-500"
                       placeholder="7">
                <button *ngIf="tempEtapes.length > 0" type="button" (click)="autoCalculateDuration()"
                        class="absolute right-2 top-1/2 -translate-y-1/2 px-2 py-1 text-xs bg-indigo-100 text-indigo-700 rounded-lg hover:bg-indigo-200"
                        title="Calculer depuis les étapes">
                  ⟳ Auto
                </button>
              </div>
              <p *ngIf="tempEtapes.length > 0" class="text-xs text-slate-500 mt-1">
                💡 Durée des étapes: {{ calculatedDuration }} jours
              </p>
            </div>
            <div>
              <label class="block text-sm font-semibold text-slate-700 mb-2">Prix ($) *</label>
              <input type="number" [(ngModel)]="formData.prix" name="prix" min="0" step="0.01" required
                     class="w-full px-4 py-3 border border-slate-200 rounded-xl focus:ring-2 focus:ring-indigo-500"
                     [class.border-red-300]="formData.prix !== undefined && formData.prix < 0"
                     placeholder="1299.00">
              <p *ngIf="formData.prix !== undefined && formData.prix < 0" class="text-xs text-red-500 mt-1">
                Le prix ne peut pas être négatif
              </p>
            </div>
          </div>
          <div class="grid grid-cols-2 gap-4">
            <div>
              <label class="block text-sm font-semibold text-slate-700 mb-2">Difficulté</label>
              <select [(ngModel)]="formData.difficulty" name="difficulty"
                      class="w-full px-4 py-3 border border-slate-200 rounded-xl focus:ring-2 focus:ring-indigo-500">
                <option value="EASY">Facile</option>
                <option value="MODERATE">Modéré</option>
                <option value="DIFFICULT">Difficile</option>
              </select>
            </div>
            <div>
              <label class="block text-sm font-semibold text-slate-700 mb-2">Max. Participants</label>
              <input type="number" [(ngModel)]="formData.maxParticipants" name="maxParticipants" min="1"
                     class="w-full px-4 py-3 border border-slate-200 rounded-xl focus:ring-2 focus:ring-indigo-500"
                     placeholder="20">
            </div>
          </div>
          <div>
            <label class="block text-sm font-semibold text-slate-700 mb-2">URL Image</label>
            <input type="url" [(ngModel)]="formData.imageUrl" name="imageUrl"
                   class="w-full px-4 py-3 border border-slate-200 rounded-xl focus:ring-2 focus:ring-indigo-500"
                   placeholder="https://example.com/image.jpg">
          </div>
        </div>

        <!-- Step 2: Étapes (Steps/Stages) -->
        <div *ngIf="currentStep === 1" class="space-y-4 animate-fadeIn">
          <div class="flex items-center justify-between mb-4">
            <h3 class="text-lg font-bold text-slate-800">Étapes du circuit</h3>
            <button (click)="addEtape()" type="button"
                    class="px-4 py-2 bg-indigo-100 text-indigo-700 font-semibold rounded-lg hover:bg-indigo-200 transition-colors flex items-center gap-2">
              <span>+</span> Ajouter une étape
            </button>
          </div>
          
          <div *ngIf="tempEtapes.length === 0" class="text-center py-8 bg-slate-50 rounded-xl">
            <span class="text-4xl mb-3 block">📍</span>
            <p class="text-slate-500">Aucune étape définie. Ajoutez des étapes pour créer votre itinéraire.</p>
          </div>

          <div class="space-y-4 max-h-[400px] overflow-y-auto pr-2">
            <div *ngFor="let etape of tempEtapes; let i = index" 
                 class="bg-white border border-slate-200 rounded-xl p-4 shadow-sm hover:shadow-md transition-shadow">
              <div class="flex items-start gap-4">
                <div class="flex flex-col items-center">
                  <div class="w-10 h-10 bg-indigo-600 text-white rounded-full flex items-center justify-center font-bold">
                    {{ i + 1 }}
                  </div>
                  <div *ngIf="i < tempEtapes.length - 1" class="w-0.5 h-8 bg-indigo-200 mt-2"></div>
                </div>
                <div class="flex-1 space-y-3">
                  <div class="grid grid-cols-2 gap-3">
                    <div>
                      <label class="block text-xs font-medium text-slate-500 mb-1">Lieu *</label>
                      <input type="text" [(ngModel)]="etape.lieu" [name]="'lieu-' + i"
                             class="w-full px-3 py-2 border border-slate-200 rounded-lg focus:ring-2 focus:ring-indigo-500 text-sm"
                             placeholder="ex: Paris, Tour Eiffel">
                    </div>
                    <div>
                      <label class="block text-xs font-medium text-slate-500 mb-1">Durée (jours)</label>
                      <input type="number" [(ngModel)]="etape.dureeJours" [name]="'duree-' + i" min="1"
                             class="w-full px-3 py-2 border border-slate-200 rounded-lg focus:ring-2 focus:ring-indigo-500 text-sm"
                             placeholder="2">
                    </div>
                  </div>
                  <div>
                    <label class="block text-xs font-medium text-slate-500 mb-1">Description</label>
                    <textarea [(ngModel)]="etape.description" [name]="'desc-' + i" rows="2"
                              class="w-full px-3 py-2 border border-slate-200 rounded-lg focus:ring-2 focus:ring-indigo-500 text-sm"
                              placeholder="Décrivez cette étape du voyage..."></textarea>
                  </div>
                  <div>
                    <label class="block text-xs font-medium text-slate-500 mb-1">Hôtel associé (optionnel)</label>
                    <select [(ngModel)]="etape.hotelId" [name]="'hotel-' + i"
                            class="w-full px-3 py-2 border border-slate-200 rounded-lg focus:ring-2 focus:ring-indigo-500 text-sm">
                      <option value="">-- Aucun hôtel --</option>
                      <option *ngFor="let hotel of availableHotels" [value]="hotel.id">
                        {{ hotel.name }} ({{ hotel.city }})
                      </option>
                    </select>
                  </div>
                </div>
                <div class="flex flex-col gap-2">
                  <button *ngIf="i > 0" (click)="moveEtapeUp(i)" type="button"
                          class="w-8 h-8 bg-slate-100 text-slate-600 rounded-lg hover:bg-slate-200 flex items-center justify-center">
                    ↑
                  </button>
                  <button *ngIf="i < tempEtapes.length - 1" (click)="moveEtapeDown(i)" type="button"
                          class="w-8 h-8 bg-slate-100 text-slate-600 rounded-lg hover:bg-slate-200 flex items-center justify-center">
                    ↓
                  </button>
                  <button (click)="removeEtape(i)" type="button"
                          class="w-8 h-8 bg-red-100 text-red-600 rounded-lg hover:bg-red-200 flex items-center justify-center">
                    ✕
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Step 3: Hotels & Activities -->
        <div *ngIf="currentStep === 2" class="space-y-6 animate-fadeIn">
          <!-- Hotels Selection -->
          <div>
            <h3 class="text-lg font-bold text-slate-800 mb-3 flex items-center gap-2">
              <span>🏨</span> Hôtels associés
            </h3>
            <div class="border border-slate-200 rounded-xl p-4 max-h-48 overflow-y-auto bg-slate-50">
              <div *ngFor="let hotel of availableHotels" class="flex items-center gap-3 py-2 px-3 bg-white rounded-lg mb-2 last:mb-0">
                <input type="checkbox" [id]="'hotel-' + hotel.id"
                       [checked]="isHotelSelected(hotel)"
                       (change)="toggleHotel(hotel)"
                       class="w-4 h-4 text-indigo-600 rounded focus:ring-indigo-500">
                <label [for]="'hotel-' + hotel.id" class="flex-1 cursor-pointer flex items-center justify-between">
                  <div>
                    <span class="font-medium text-slate-800">{{ hotel.name }}</span>
                    <span class="text-slate-500 text-sm ml-2">{{ hotel.city }}, {{ hotel.country }}</span>
                  </div>
                  <span class="text-amber-500">{{ getStars(hotel.etoile) }}</span>
                </label>
              </div>
              <p *ngIf="availableHotels.length === 0" class="text-slate-500 text-sm text-center py-4">
                Aucun hôtel disponible. Ajoutez des hôtels d'abord.
              </p>
            </div>
          </div>

          <!-- Activities Selection -->
          <div>
            <h3 class="text-lg font-bold text-slate-800 mb-3 flex items-center gap-2">
              <span>🎯</span> Activités incluses
            </h3>
            <div class="border border-slate-200 rounded-xl p-4 max-h-48 overflow-y-auto bg-slate-50">
              <div *ngFor="let activity of availableActivities" class="flex items-center gap-3 py-2 px-3 bg-white rounded-lg mb-2 last:mb-0">
                <input type="checkbox" [id]="'activity-' + activity.id"
                       [checked]="isActivitySelected(activity)"
                       (change)="toggleActivity(activity)"
                       class="w-4 h-4 text-indigo-600 rounded focus:ring-indigo-500">
                <label [for]="'activity-' + activity.id" class="flex-1 cursor-pointer flex items-center justify-between">
                  <div>
                    <span class="font-medium text-slate-800">{{ activity.name }}</span>
                    <span class="text-slate-500 text-sm ml-2" *ngIf="activity.description">{{ activity.description.substring(0, 50) }}...</span>
                  </div>
                  <span class="text-emerald-600 font-semibold">{{ activity.price | currency }}</span>
                </label>
              </div>
              <p *ngIf="availableActivities.length === 0" class="text-slate-500 text-sm text-center py-4">
                Aucune activité disponible. Ajoutez des activités d'abord.
              </p>
            </div>
          </div>
        </div>

        <!-- Navigation Buttons -->
        <div class="flex justify-between pt-4 border-t border-slate-200">
          <button *ngIf="currentStep > 0" (click)="previousStep()" type="button"
                  class="px-6 py-3 bg-slate-100 text-slate-700 font-semibold rounded-xl hover:bg-slate-200 transition-colors flex items-center gap-2">
            ← Précédent
          </button>
          <div *ngIf="currentStep === 0"></div>
          
          <div class="flex gap-3">
            <button type="button" (click)="closeModal()"
                    class="px-6 py-3 bg-slate-100 text-slate-700 font-semibold rounded-xl hover:bg-slate-200 transition-colors">
              Annuler
            </button>
            <button *ngIf="currentStep < formSteps.length - 1" (click)="nextStep()" type="button"
                    class="px-6 py-3 bg-indigo-600 text-white font-bold rounded-xl hover:bg-indigo-700 transition-colors flex items-center gap-2">
              Suivant →
            </button>
            <button *ngIf="currentStep === formSteps.length - 1" (click)="saveCircuit()" [disabled]="isSaving"
                    class="px-6 py-3 bg-gradient-to-r from-emerald-600 to-teal-600 text-white font-bold rounded-xl hover:from-emerald-700 hover:to-teal-700 transition-all disabled:opacity-50 flex items-center gap-2">
              <span *ngIf="isSaving" class="w-5 h-5 border-2 border-white/30 border-t-white rounded-full animate-spin"></span>
              {{ isEditing ? 'Mettre à jour' : 'Créer le circuit' }}
            </button>
          </div>
        </div>
      </div>
    </app-modal>

    <!-- View Details Modal -->
    <app-modal [isOpen]="isDetailsModalOpen" [title]="'Détails du circuit'" (close)="closeDetailsModal()" size="xl" [showFooter]="false">
      <div *ngIf="selectedCircuit" class="space-y-6">
        <!-- Header -->
        <div class="flex items-start gap-4">
          <div *ngIf="selectedCircuit.imageUrl" class="w-24 h-24 rounded-2xl overflow-hidden">
            <img [src]="selectedCircuit.imageUrl" alt="" class="w-full h-full object-cover">
          </div>
          <div *ngIf="!selectedCircuit.imageUrl" class="w-24 h-24 bg-indigo-100 rounded-2xl flex items-center justify-center text-4xl">🗺️</div>
          <div class="flex-1">
            <div class="flex items-center gap-3">
              <h3 class="text-2xl font-bold text-slate-800">{{ selectedCircuit.title }}</h3>
              <span class="px-3 py-1 rounded-full text-xs font-bold"
                    [class.bg-green-100]="selectedCircuit.difficulty === 'EASY'"
                    [class.text-green-700]="selectedCircuit.difficulty === 'EASY'"
                    [class.bg-amber-100]="selectedCircuit.difficulty === 'MODERATE'"
                    [class.text-amber-700]="selectedCircuit.difficulty === 'MODERATE'"
                    [class.bg-red-100]="selectedCircuit.difficulty === 'DIFFICULT'"
                    [class.text-red-700]="selectedCircuit.difficulty === 'DIFFICULT'">
                {{ getDifficultyLabel(selectedCircuit.difficulty) }}
              </span>
            </div>
            <p class="text-slate-500 mt-2">{{ selectedCircuit.description }}</p>
          </div>
        </div>
        
        <!-- Stats -->
        <div class="grid grid-cols-4 gap-4">
          <div class="bg-slate-50 rounded-xl p-4 text-center">
            <p class="text-sm text-slate-500">Durée</p>
            <p class="text-2xl font-bold text-slate-800">{{ selectedCircuit.duree }} jours</p>
          </div>
          <div class="bg-slate-50 rounded-xl p-4 text-center">
            <p class="text-sm text-slate-500">Prix</p>
            <p class="text-2xl font-bold text-emerald-600">{{ selectedCircuit.prix | currency }}</p>
          </div>
          <div class="bg-slate-50 rounded-xl p-4 text-center">
            <p class="text-sm text-slate-500">Étapes</p>
            <p class="text-2xl font-bold text-slate-800">{{ selectedCircuit.etapes?.length || 0 }}</p>
          </div>
          <div class="bg-slate-50 rounded-xl p-4 text-center">
            <p class="text-sm text-slate-500">Max. Participants</p>
            <p class="text-2xl font-bold text-slate-800">{{ selectedCircuit.maxParticipants || '-' }}</p>
          </div>
        </div>

        <!-- Étapes Timeline -->
        <div *ngIf="selectedCircuit.etapes?.length">
          <h4 class="font-semibold text-slate-800 mb-4 flex items-center gap-2">
            <span class="text-xl">📍</span> Itinéraire ({{ selectedCircuit.etapes?.length }} étapes)
          </h4>
          <div class="relative">
            <div class="absolute left-5 top-0 bottom-0 w-0.5 bg-indigo-200"></div>
            <div class="space-y-4">
              <div *ngFor="let etape of selectedCircuit.etapes; let i = index" 
                   class="relative flex items-start gap-4 ml-0">
                <div class="w-10 h-10 bg-indigo-600 text-white rounded-full flex items-center justify-center font-bold z-10 shrink-0">
                  {{ etape.ordre || i + 1 }}
                </div>
                <div class="flex-1 bg-white border border-slate-200 rounded-xl p-4 shadow-sm">
                  <div class="flex items-center justify-between mb-2">
                    <h5 class="font-bold text-slate-800">{{ etape.lieu }}</h5>
                    <span class="text-sm text-indigo-600 font-medium">{{ etape.dureeJours }} jour(s)</span>
                  </div>
                  <p class="text-slate-600 text-sm">{{ etape.description }}</p>
                  <div *ngIf="etape.hotel" class="mt-3 flex items-center gap-2 text-sm text-slate-500">
                    <span>🏨</span>
                    <span>{{ etape.hotel.name }}</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Hotels -->
        <div *ngIf="selectedCircuit.hotels?.length">
          <h4 class="font-semibold text-slate-800 mb-3 flex items-center gap-2">
            <span class="text-xl">🏨</span> Hôtels inclus ({{ selectedCircuit.hotels?.length }})
          </h4>
          <div class="grid grid-cols-2 gap-3">
            <div *ngFor="let hotel of selectedCircuit.hotels" class="flex items-center gap-3 p-3 bg-slate-50 rounded-xl">
              <div class="w-12 h-12 bg-blue-100 rounded-lg flex items-center justify-center text-xl">🏨</div>
              <div>
                <p class="font-medium text-slate-800">{{ hotel.name }}</p>
                <p class="text-sm text-slate-500">{{ hotel.city }}, {{ hotel.country }} • {{ getStars(hotel.etoile) }}</p>
              </div>
            </div>
          </div>
        </div>

        <!-- Activities -->
        <div *ngIf="selectedCircuit.activities?.length">
          <h4 class="font-semibold text-slate-800 mb-3 flex items-center gap-2">
            <span class="text-xl">🎯</span> Activités incluses ({{ selectedCircuit.activities?.length }})
          </h4>
          <div class="flex flex-wrap gap-2">
            <span *ngFor="let activity of selectedCircuit.activities" 
                  class="px-4 py-2 bg-purple-100 text-purple-700 rounded-full text-sm font-medium flex items-center gap-2">
              {{ activity.name }}
              <span class="text-purple-500">({{ activity.price | currency }})</span>
            </span>
          </div>
        </div>

        <!-- Action Buttons -->
        <div class="flex justify-end gap-3 pt-4 border-t">
          <button (click)="openEditFromDetails()" 
                  class="px-6 py-3 bg-indigo-100 text-indigo-700 font-semibold rounded-xl hover:bg-indigo-200 transition-colors flex items-center gap-2">
            ✏️ Modifier
          </button>
          <button (click)="deleteFromDetails()" 
                  class="px-6 py-3 bg-red-100 text-red-700 font-semibold rounded-xl hover:bg-red-200 transition-colors flex items-center gap-2">
            🗑️ Supprimer
          </button>
        </div>
      </div>
    </app-modal>
  `,
    styles: [`
    .animate-fadeIn {
      animation: fadeIn 0.3s ease-in-out;
    }
    @keyframes fadeIn {
      from { opacity: 0; transform: translateY(10px); }
      to { opacity: 1; transform: translateY(0); }
    }
  `]
})
export class InternalCircuitsComponent implements OnInit {
    private http = inject(HttpService);
    private sweetAlert = inject(SweetAlertService);

    circuits: Circuit[] = [];
    availableHotels: Hotel[] = [];
    availableActivities: Activity[] = [];
    selectedCircuit: Circuit | null = null;
    
    isLoading = false;
    isModalOpen = false;
    isDetailsModalOpen = false;
    isEditing = false;
    isSaving = false;
    
    currentStep = 0;
    formSteps = ['Informations', 'Étapes', 'Associations'];
    
    formData: Circuit = this.getEmptyForm();
    tempEtapes: Etape[] = [];
    selectedHotelIds: string[] = [];
    selectedActivityIds: string[] = [];

    columns: TableColumn[] = [
        { key: 'title', label: 'Nom du circuit' },
        { key: 'duree', label: 'Durée', width: '100px' },
        { key: 'prix', label: 'Prix', type: 'currency', width: '120px' },
        { key: 'difficulty', label: 'Difficulté', width: '100px' },
        { key: 'etapesCount', label: 'Étapes', width: '80px' }
    ];

    actions: TableAction[] = [
        { key: 'view', label: 'Voir', icon: '👁️', color: 'blue' },
        { key: 'edit', label: 'Modifier', icon: '✏️', color: 'green' },
        { key: 'delete', label: 'Supprimer', icon: '🗑️', color: 'red' }
    ];

    get avgDuration(): number {
        if (!this.circuits.length) return 0;
        return this.circuits.reduce((sum, c) => sum + (c.duree || 0), 0) / this.circuits.length;
    }

    get avgPrice(): number {
        if (!this.circuits.length) return 0;
        return this.circuits.reduce((sum, c) => sum + (c.prix || 0), 0) / this.circuits.length;
    }

    get totalEtapes(): number {
        return this.circuits.reduce((sum, c) => sum + (c.etapes?.length || 0), 0);
    }

    get calculatedDuration(): number {
        return this.tempEtapes.reduce((sum, e) => sum + (e.dureeJours || 0), 0);
    }

    autoCalculateDuration(): void {
        const duration = this.calculatedDuration;
        if (duration > 0) {
            this.formData.duree = duration;
        }
    }

    ngOnInit(): void {
        this.loadCircuits();
        this.loadHotels();
        this.loadActivities();
    }

    loadCircuits(): void {
        this.isLoading = true;
        this.http.get<Circuit[]>('/admin/offers/circuits').subscribe({
            next: (circuits) => {
                // Add computed property for table display
                this.circuits = circuits.map(c => ({
                    ...c,
                    etapesCount: c.etapes?.length || 0
                }));
                this.isLoading = false;
            },
            error: (err) => {
                console.error('Error loading circuits:', err);
                this.sweetAlert.error('Échec du chargement des circuits');
                this.isLoading = false;
            }
        });
    }

    loadHotels(): void {
        this.http.get<Hotel[]>('/admin/offers/hotels').subscribe({
            next: (hotels) => this.availableHotels = hotels,
            error: (err) => console.error('Error loading hotels:', err)
        });
    }

    loadActivities(): void {
        this.http.get<Activity[]>('/admin/offers/activities').subscribe({
            next: (activities) => this.availableActivities = activities,
            error: (err) => console.error('Error loading activities:', err)
        });
    }

    getEmptyForm(): Circuit {
        return { 
            title: '', 
            description: '', 
            duree: 7, 
            prix: 0, 
            difficulty: 'MODERATE',
            maxParticipants: 20,
            imageUrl: '',
            hotels: [], 
            activities: [],
            etapes: []
        };
    }

    getModalTitle(): string {
        const action = this.isEditing ? 'Modifier' : 'Créer';
        return `${action} un circuit - ${this.formSteps[this.currentStep]}`;
    }

    getDifficultyLabel(difficulty: string | undefined): string {
        const labels: Record<string, string> = {
            'EASY': 'Facile',
            'MODERATE': 'Modéré',
            'DIFFICULT': 'Difficile'
        };
        return labels[difficulty || 'MODERATE'] || 'Modéré';
    }

    getStars(count: number | undefined): string {
        return '★'.repeat(count || 0);
    }

    // Step navigation
    nextStep(): void {
        if (this.currentStep === 0 && !this.formData.title) {
            this.sweetAlert.warning('Veuillez entrer un titre pour le circuit');
            return;
        }
        if (this.currentStep < this.formSteps.length - 1) {
            this.currentStep++;
        }
    }

    previousStep(): void {
        if (this.currentStep > 0) {
            this.currentStep--;
        }
    }

    goToStep(step: number): void {
        if (step <= this.currentStep || (step === 1 && this.formData.title)) {
            this.currentStep = step;
        }
    }

    // Étapes management
    addEtape(): void {
        this.tempEtapes.push({
            ordre: this.tempEtapes.length + 1,
            lieu: '',
            description: '',
            dureeJours: 1,
            hotelId: ''
        });
    }

    removeEtape(index: number): void {
        this.tempEtapes.splice(index, 1);
        this.updateEtapeOrders();
    }

    moveEtapeUp(index: number): void {
        if (index > 0) {
            [this.tempEtapes[index], this.tempEtapes[index - 1]] = [this.tempEtapes[index - 1], this.tempEtapes[index]];
            this.updateEtapeOrders();
        }
    }

    moveEtapeDown(index: number): void {
        if (index < this.tempEtapes.length - 1) {
            [this.tempEtapes[index], this.tempEtapes[index + 1]] = [this.tempEtapes[index + 1], this.tempEtapes[index]];
            this.updateEtapeOrders();
        }
    }

    updateEtapeOrders(): void {
        this.tempEtapes.forEach((etape, index) => {
            etape.ordre = index + 1;
        });
    }

    // Hotel & Activity selection
    isHotelSelected(hotel: Hotel): boolean {
        return this.selectedHotelIds.includes(hotel.id!);
    }

    toggleHotel(hotel: Hotel): void {
        const index = this.selectedHotelIds.indexOf(hotel.id!);
        if (index > -1) {
            this.selectedHotelIds.splice(index, 1);
        } else {
            this.selectedHotelIds.push(hotel.id!);
        }
    }

    isActivitySelected(activity: Activity): boolean {
        return this.selectedActivityIds.includes(activity.id!);
    }

    toggleActivity(activity: Activity): void {
        const index = this.selectedActivityIds.indexOf(activity.id!);
        if (index > -1) {
            this.selectedActivityIds.splice(index, 1);
        } else {
            this.selectedActivityIds.push(activity.id!);
        }
    }

    // Modal operations
    openCreateModal(): void {
        this.isEditing = false;
        this.formData = this.getEmptyForm();
        this.tempEtapes = [];
        this.selectedHotelIds = [];
        this.selectedActivityIds = [];
        this.currentStep = 0;
        this.isModalOpen = true;
    }

    openEditModal(circuit: Circuit): void {
        this.isEditing = true;
        this.formData = { ...circuit };
        // Map etapes and populate hotelId from hotel object for select binding
        this.tempEtapes = (circuit.etapes || []).map(e => ({ 
            ...e,
            hotelId: e.hotel?.id || e.hotelId || ''
        }));
        this.selectedHotelIds = circuit.hotels?.map(h => h.id!) || [];
        this.selectedActivityIds = circuit.activities?.map(a => a.id!) || [];
        this.currentStep = 0;
        this.isModalOpen = true;
    }

    closeModal(): void {
        this.isModalOpen = false;
        this.formData = this.getEmptyForm();
        this.tempEtapes = [];
        this.selectedHotelIds = [];
        this.selectedActivityIds = [];
        this.currentStep = 0;
    }

    saveCircuit(): void {
        // Validation
        if (!this.formData.title?.trim()) {
            this.sweetAlert.warning('Veuillez entrer un titre pour le circuit');
            this.currentStep = 0;
            return;
        }

        if (this.formData.prix === undefined || this.formData.prix < 0) {
            this.sweetAlert.warning('Veuillez entrer un prix valide (≥ 0)');
            this.currentStep = 0;
            return;
        }

        if (this.formData.duree === undefined || this.formData.duree < 1) {
            this.sweetAlert.warning('La durée doit être d\'au moins 1 jour');
            this.currentStep = 0;
            return;
        }

        // Check if any etape is missing required lieu
        const invalidEtapes = this.tempEtapes.filter(e => !e.lieu?.trim());
        if (invalidEtapes.length > 0) {
            this.sweetAlert.warning('Certaines étapes n\'ont pas de lieu défini. Veuillez compléter ou supprimer ces étapes.');
            this.currentStep = 1;
            return;
        }

        this.isSaving = true;
        
        // Prepare payload
        this.formData.hotels = this.availableHotels.filter(h => this.selectedHotelIds.includes(h.id!));
        this.formData.activities = this.availableActivities.filter(a => this.selectedActivityIds.includes(a.id!));
        this.formData.etapes = this.tempEtapes.filter(e => e.lieu?.trim());

        const operation = this.isEditing
            ? this.http.put<Circuit>(`/admin/offers/circuits/${this.formData.id}`, this.formData)
            : this.http.post<Circuit>('/admin/offers/circuits', this.formData);

        operation.subscribe({
            next: (savedCircuit) => {
                // If editing and we have etapes, update them separately
                if (this.isEditing && this.formData.id) {
                    this.saveEtapes(this.formData.id);
                    this.saveAssociations(this.formData.id);
                } else if (savedCircuit.id) {
                    this.saveEtapes(savedCircuit.id);
                    this.saveAssociations(savedCircuit.id);
                }
                
                this.sweetAlert.success(this.isEditing ? 'Circuit mis à jour avec succès' : 'Circuit créé avec succès');
                this.closeModal();
                this.loadCircuits();
                this.isSaving = false;
            },
            error: (err) => {
                console.error('Error saving circuit:', err);
                this.sweetAlert.error('Échec de la sauvegarde du circuit');
                this.isSaving = false;
            }
        });
    }

    private saveEtapes(circuitId: string): void {
        // For each valid etape, add it to the circuit
        this.tempEtapes.filter(e => e.lieu?.trim()).forEach(etape => {
            if (!etape.id) {
                this.http.post(`/admin/offers/circuits/${circuitId}/etapes`, etape).subscribe({
                    error: (err) => console.error('Error adding etape:', err)
                });
            }
        });
    }

    private saveAssociations(circuitId: string): void {
        // Update hotels
        if (this.selectedHotelIds.length > 0) {
            this.http.put(`/admin/offers/circuits/${circuitId}/hotels`, this.selectedHotelIds).subscribe({
                error: (err) => console.error('Error updating hotels:', err)
            });
        }
        
        // Update activities
        if (this.selectedActivityIds.length > 0) {
            this.http.put(`/admin/offers/circuits/${circuitId}/activities`, this.selectedActivityIds).subscribe({
                error: (err) => console.error('Error updating activities:', err)
            });
        }
    }

    onAction(event: { action: string; item: Circuit }): void {
        if (event.action === 'view') {
            this.viewDetails(event.item);
        } else if (event.action === 'edit') {
            this.openEditModal(event.item);
        } else if (event.action === 'delete') {
            this.deleteCircuit(event.item);
        }
    }

    viewDetails(circuit: Circuit): void {
        this.selectedCircuit = circuit;
        this.isDetailsModalOpen = true;
    }

    closeDetailsModal(): void {
        this.isDetailsModalOpen = false;
        this.selectedCircuit = null;
    }

    openEditFromDetails(): void {
        if (this.selectedCircuit) {
            this.closeDetailsModal();
            this.openEditModal(this.selectedCircuit);
        }
    }

    deleteFromDetails(): void {
        if (this.selectedCircuit) {
            const circuit = this.selectedCircuit;
            this.closeDetailsModal();
            this.deleteCircuit(circuit);
        }
    }

    deleteCircuit(circuit: Circuit): void {
        this.sweetAlert.confirm(
            `Êtes-vous sûr de vouloir supprimer "${circuit.title}" ?`,
            'Cette action est irréversible.'
        ).then((confirmed) => {
            if (confirmed) {
                this.http.delete(`/admin/offers/circuits/${circuit.id}`).subscribe({
                    next: () => {
                        this.sweetAlert.success('Circuit supprimé avec succès');
                        this.loadCircuits();
                    },
                    error: (err) => {
                        console.error('Error deleting circuit:', err);
                        this.sweetAlert.error('Échec de la suppression du circuit');
                    }
                });
            }
        });
    }
}
