import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink, RouterLinkActive } from '@angular/router';

interface NavSection {
    title: string;
    icon: string;
    key: string;
    badge?: string;
    badgeColor?: string;
    items: NavItem[];
}

interface NavItem {
    label: string;
    icon: string;
    route: string;
    queryParams?: { [key: string]: string };
}

@Component({
    selector: 'app-sidebar',
    standalone: true,
    imports: [CommonModule, RouterLink, RouterLinkActive],
    templateUrl: './sidebar.component.html',
    styleUrl: './sidebar.component.css'
})
export class SidebarComponent {
    // Collapsible sections state
    expandedSections: { [key: string]: boolean } = {
        external: true,
        internal: true
    };

    // Navigation sections
    navSections: NavSection[] = [
        {
            title: 'External Search',
            icon: '🌐',
            key: 'external',
            badge: 'API',
            badgeColor: 'bg-purple-500',
            items: [
                { label: 'Flights', icon: '✈️', route: '/admin/external/flights' },
                { label: 'Hotels', icon: '🏨', route: '/admin/external/hotels' },
                { label: 'Airports', icon: '🛫', route: '/admin/external/airports' },
                { label: 'Airlines', icon: '🦅', route: '/admin/external/airlines' }
            ]
        },
        {
            title: 'Internal Management',
            icon: '🗄️',
            key: 'internal',
            badge: 'DB',
            badgeColor: 'bg-emerald-500',
            items: [
                { label: 'Flights', icon: '✈️', route: '/admin/internal/flights' },
                { label: 'Hotels', icon: '🏨', route: '/admin/internal/hotels' },
                { label: 'Circuits', icon: '🗺️', route: '/admin/internal/circuits' },
                { label: 'Activités', icon: '🎯', route: '/admin/internal/activities' }
            ]
        }
    ];

    toggleSection(key: string): void {
        this.expandedSections[key] = !this.expandedSections[key];
    }

    isSectionExpanded(key: string): boolean {
        return this.expandedSections[key] ?? true;
    }
}
