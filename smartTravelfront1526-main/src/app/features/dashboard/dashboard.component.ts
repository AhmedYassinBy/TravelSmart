// src/app/features/dashboard/dashboard.component.ts
import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { HttpService } from '../../core/http.service';
import { StatCardComponent } from '../../shared/components';

interface DashboardStats {
    totalFlights: number;
    totalHotels: number;
    totalCircuits: number;
    totalUsers: number;
    recentActivity: ActivityItem[];
}

interface ActivityItem {
    icon: string;
    title: string;
    description: string;
    time: string;
    color: string;
}

@Component({
    selector: 'app-dashboard',
    standalone: true,
    imports: [CommonModule, RouterModule, StatCardComponent],
    template: `
    <div class="p-8">
      <!-- Header -->
      <div class="mb-8">
        <h1 class="text-4xl font-bold bg-gradient-to-r from-slate-800 via-blue-700 to-indigo-700 bg-clip-text text-transparent mb-2">
          Welcome to TravelSmart Admin
        </h1>
        <p class="text-slate-500 text-lg">Here's an overview of your travel management system</p>
      </div>

      <!-- Stats Grid -->
      <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
        <app-stat-card 
          icon="✈️" 
          title="Flights" 
          [value]="stats.totalFlights" 
          trend="+12%" 
          [trendUp]="true"
          gradientFrom="blue-500" 
          gradientTo="indigo-600">
        </app-stat-card>
        <app-stat-card 
          icon="🏨" 
          title="Hotels" 
          [value]="stats.totalHotels" 
          trend="+8%" 
          [trendUp]="true"
          gradientFrom="emerald-500" 
          gradientTo="teal-600">
        </app-stat-card>
        <app-stat-card 
          icon="🗺️" 
          title="Circuits" 
          [value]="stats.totalCircuits" 
          trend="+5%" 
          [trendUp]="true"
          gradientFrom="orange-500" 
          gradientTo="amber-600">
        </app-stat-card>
        <app-stat-card 
          icon="👥" 
          title="Users" 
          [value]="stats.totalUsers" 
          trend="+23%" 
          [trendUp]="true"
          gradientFrom="purple-500" 
          gradientTo="pink-600">
        </app-stat-card>
      </div>

      <!-- Quick Actions & Info -->
      <div class="grid grid-cols-1 lg:grid-cols-3 gap-6 mb-8">
        <!-- Quick Actions -->
        <div class="lg:col-span-2 bg-white rounded-2xl shadow-lg border border-slate-100 p-6">
          <h2 class="text-xl font-bold text-slate-800 mb-6 flex items-center gap-2">
            <span>⚡</span> Quick Actions
          </h2>
          <div class="grid grid-cols-2 md:grid-cols-4 gap-4">
            <a routerLink="/admin/internal/flights" 
               class="group p-4 bg-gradient-to-br from-blue-50 to-indigo-50 rounded-xl border border-blue-100 hover:shadow-lg hover:scale-105 transition-all duration-200 text-center">
              <div class="w-12 h-12 mx-auto mb-3 bg-blue-100 rounded-xl flex items-center justify-center text-2xl group-hover:scale-110 transition-transform">
                ✈️
              </div>
              <p class="font-semibold text-slate-700">Add Flight</p>
            </a>
            <a routerLink="/admin/internal/hotels" 
               class="group p-4 bg-gradient-to-br from-emerald-50 to-teal-50 rounded-xl border border-emerald-100 hover:shadow-lg hover:scale-105 transition-all duration-200 text-center">
              <div class="w-12 h-12 mx-auto mb-3 bg-emerald-100 rounded-xl flex items-center justify-center text-2xl group-hover:scale-110 transition-transform">
                🏨
              </div>
              <p class="font-semibold text-slate-700">Add Hotel</p>
            </a>
            <a routerLink="/admin/internal/circuits" 
               class="group p-4 bg-gradient-to-br from-orange-50 to-amber-50 rounded-xl border border-orange-100 hover:shadow-lg hover:scale-105 transition-all duration-200 text-center">
              <div class="w-12 h-12 mx-auto mb-3 bg-orange-100 rounded-xl flex items-center justify-center text-2xl group-hover:scale-110 transition-transform">
                🗺️
              </div>
              <p class="font-semibold text-slate-700">New Circuit</p>
            </a>
            <a routerLink="/admin/external/flights" 
               class="group p-4 bg-gradient-to-br from-purple-50 to-pink-50 rounded-xl border border-purple-100 hover:shadow-lg hover:scale-105 transition-all duration-200 text-center">
              <div class="w-12 h-12 mx-auto mb-3 bg-purple-100 rounded-xl flex items-center justify-center text-2xl group-hover:scale-110 transition-transform">
                🔍
              </div>
              <p class="font-semibold text-slate-700">Search API</p>
            </a>
          </div>
        </div>

        <!-- System Status -->
        <div class="bg-white rounded-2xl shadow-lg border border-slate-100 p-6">
          <h2 class="text-xl font-bold text-slate-800 mb-6 flex items-center gap-2">
            <span>📊</span> System Status
          </h2>
          <div class="space-y-4">
            <div class="flex items-center justify-between p-3 bg-emerald-50 rounded-xl">
              <div class="flex items-center gap-3">
                <div class="w-3 h-3 bg-emerald-500 rounded-full animate-pulse"></div>
                <span class="font-medium text-slate-700">Database</span>
              </div>
              <span class="text-emerald-600 font-semibold">Online</span>
            </div>
            <div class="flex items-center justify-between p-3 bg-emerald-50 rounded-xl">
              <div class="flex items-center gap-3">
                <div class="w-3 h-3 bg-emerald-500 rounded-full animate-pulse"></div>
                <span class="font-medium text-slate-700">Amadeus API</span>
              </div>
              <span class="text-emerald-600 font-semibold">Connected</span>
            </div>
            <div class="flex items-center justify-between p-3 bg-emerald-50 rounded-xl">
              <div class="flex items-center gap-3">
                <div class="w-3 h-3 bg-emerald-500 rounded-full animate-pulse"></div>
                <span class="font-medium text-slate-700">AviationStack</span>
              </div>
              <span class="text-emerald-600 font-semibold">Connected</span>
            </div>
            <div class="flex items-center justify-between p-3 bg-amber-50 rounded-xl">
              <div class="flex items-center gap-3">
                <div class="w-3 h-3 bg-amber-500 rounded-full"></div>
                <span class="font-medium text-slate-700">Booking.com</span>
              </div>
              <span class="text-amber-600 font-semibold">Limited</span>
            </div>
          </div>
        </div>
      </div>

      <!-- Recent Activity & Tips -->
      <div class="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <!-- Recent Activity -->
        <div class="bg-white rounded-2xl shadow-lg border border-slate-100 p-6">
          <h2 class="text-xl font-bold text-slate-800 mb-6 flex items-center gap-2">
            <span>📋</span> Recent Activity
          </h2>
          <div class="space-y-4">
            <div *ngFor="let activity of stats.recentActivity" 
                 class="flex items-start gap-4 p-4 bg-slate-50 rounded-xl hover:bg-slate-100 transition-colors">
              <div [class]="'w-10 h-10 rounded-xl flex items-center justify-center text-lg ' + getActivityBg(activity.color)">
                {{ activity.icon }}
              </div>
              <div class="flex-1">
                <p class="font-semibold text-slate-800">{{ activity.title }}</p>
                <p class="text-sm text-slate-500">{{ activity.description }}</p>
              </div>
              <span class="text-xs text-slate-400 whitespace-nowrap">{{ activity.time }}</span>
            </div>
          </div>
        </div>

        <!-- Tips & Guide -->
        <div class="bg-gradient-to-br from-indigo-600 to-purple-700 rounded-2xl shadow-lg p-6 text-white">
          <h2 class="text-xl font-bold mb-6 flex items-center gap-2">
            <span>💡</span> Getting Started
          </h2>
          <div class="space-y-4">
            <div class="flex items-start gap-3 p-4 bg-white/10 rounded-xl backdrop-blur">
              <span class="text-2xl">1️⃣</span>
              <div>
                <p class="font-semibold">Search External APIs</p>
                <p class="text-sm text-white/80">Use the External Search section to find flights and hotels from Amadeus and other APIs.</p>
              </div>
            </div>
            <div class="flex items-start gap-3 p-4 bg-white/10 rounded-xl backdrop-blur">
              <span class="text-2xl">2️⃣</span>
              <div>
                <p class="font-semibold">Import to Database</p>
                <p class="text-sm text-white/80">Import the best offers to your internal database for customization.</p>
              </div>
            </div>
            <div class="flex items-start gap-3 p-4 bg-white/10 rounded-xl backdrop-blur">
              <span class="text-2xl">3️⃣</span>
              <div>
                <p class="font-semibold">Create Circuits</p>
                <p class="text-sm text-white/80">Combine hotels, flights, and activities into complete travel packages.</p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  `
})
export class DashboardComponent implements OnInit {
    private http = inject(HttpService);

    stats: DashboardStats = {
        totalFlights: 0,
        totalHotels: 0,
        totalCircuits: 0,
        totalUsers: 0,
        recentActivity: [
            { icon: '✈️', title: 'Flight Added', description: 'New flight AF1234 Paris → New York', time: '2 min ago', color: 'blue' },
            { icon: '🏨', title: 'Hotel Updated', description: 'Grand Hotel Paris - 5 new rooms', time: '15 min ago', color: 'emerald' },
            { icon: '🗺️', title: 'Circuit Created', description: 'Mediterranean Explorer package', time: '1 hour ago', color: 'orange' },
            { icon: '👤', title: 'New User', description: 'john.doe@example.com registered', time: '3 hours ago', color: 'purple' }
        ]
    };

    ngOnInit(): void {
        this.loadStats();
    }

    loadStats(): void {
        // Load flights count
        this.http.get<any[]>('/admin/offers/flights').subscribe({
            next: (flights) => this.stats.totalFlights = flights.length,
            error: () => this.stats.totalFlights = 0
        });

        // Load hotels count
        this.http.get<any[]>('/admin/offers/hotels').subscribe({
            next: (hotels) => this.stats.totalHotels = hotels.length,
            error: () => this.stats.totalHotels = 0
        });

        // Load circuits count
        this.http.get<any[]>('/admin/offers/circuits').subscribe({
            next: (circuits) => this.stats.totalCircuits = circuits.length,
            error: () => this.stats.totalCircuits = 0
        });

        // Users would require a user endpoint
        this.stats.totalUsers = 42; // Placeholder
    }

    getActivityBg(color: string): string {
        const colors: Record<string, string> = {
            blue: 'bg-blue-100',
            emerald: 'bg-emerald-100',
            orange: 'bg-orange-100',
            purple: 'bg-purple-100'
        };
        return colors[color] || 'bg-slate-100';
    }
}
