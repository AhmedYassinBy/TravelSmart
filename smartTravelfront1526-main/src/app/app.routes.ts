import { Routes } from '@angular/router';
import { LoginComponent } from './login/login.component';
import { AdminLayoutComponent } from './admin/admin-layout/admin-layout.component';
import { authGuard } from './guards/auth.guard';
import { ForgotPasswordComponent } from './forgot-password/forgot-password.component';
import { ResetPasswordComponent } from './reset-password/reset-password.component';

export const routes: Routes = [
  { path: '', redirectTo: 'log', pathMatch: 'full' },
  {
    path: 'log',
    component: LoginComponent
  },
  {
    path: 'admin',
    component: AdminLayoutComponent,
    canActivate: [authGuard],
    children: [
      // Dashboard
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
      { path: 'dashboard', loadComponent: () => import('./features/dashboard/dashboard.component').then(m => m.DashboardComponent) },
      
      // External API Search (Read-only)
      { path: 'external/flights', loadComponent: () => import('./features/external/external-flights/external-flights.component').then(m => m.ExternalFlightsComponent) },
      { path: 'external/hotels', loadComponent: () => import('./features/external/external-hotels/external-hotels.component').then(m => m.ExternalHotelsComponent) },
      { path: 'external/airports', loadComponent: () => import('./features/external/external-airports/external-airports.component').then(m => m.ExternalAirportsComponent) },
      { path: 'external/airlines', loadComponent: () => import('./features/external/external-airlines/external-airlines.component').then(m => m.ExternalAirlinesComponent) },
      
      // Internal Database Management (Full CRUD)
      { path: 'internal/flights', loadComponent: () => import('./features/internal/internal-flights/internal-flights.component').then(m => m.InternalFlightsComponent) },
      { path: 'internal/hotels', loadComponent: () => import('./features/internal/internal-hotels/internal-hotels.component').then(m => m.InternalHotelsComponent) },
      { path: 'internal/circuits', loadComponent: () => import('./features/internal/internal-circuits/internal-circuits.component').then(m => m.InternalCircuitsComponent) },
      { path: 'internal/activities', loadComponent: () => import('./features/internal/internal-activities/internal-activities.component').then(m => m.InternalActivitiesComponent) },
      
      // Legacy route (redirect to new structure)
      { path: 'external-search', redirectTo: 'external/flights', pathMatch: 'full' },
    ]
  },
  { path: 'forgot-password', component: ForgotPasswordComponent },
  { path: 'reset-password', component: ResetPasswordComponent },
];


