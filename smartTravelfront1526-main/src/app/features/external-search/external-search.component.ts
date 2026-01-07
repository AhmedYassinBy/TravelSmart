// src/app/features/external-search/external-search.component.ts
import { Component, inject, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ExternalFlightService, FlightSearchParams } from '../../services/external-flight.service';
import { ExternalHotelService } from '../../services/external-hotel.service';
import { AirportService } from '../../services/airport.service';
import { AirlineService } from '../../services/airline.service';
import { FlightService } from '../../services/flight.service';
import { HotelService } from '../../services/hotel.service';
import { FlightDTO, HotelDTO, AirportDTO, AirlineDTO, Flight, Hotel } from '../../models';
import { SweetAlertService } from '../../core/sweetalert.service';
import { firstValueFrom } from 'rxjs';

type TabType = 'flights' | 'hotels' | 'airports' | 'airlines';

@Component({
    selector: 'app-external-search',
    standalone: true,
    imports: [CommonModule, FormsModule],
    templateUrl: './external-search.component.html',
    styleUrl: './external-search.component.css'
})
export class ExternalSearchComponent implements OnInit {
    // Services
    private externalFlightService = inject(ExternalFlightService);
    private externalHotelService = inject(ExternalHotelService);
    private airportService = inject(AirportService);
    private airlineService = inject(AirlineService);
    private flightService = inject(FlightService);
    private hotelService = inject(HotelService);

    private sweetAlert = inject(SweetAlertService);
    private route = inject(ActivatedRoute);

    // Active tab
    activeTab: TabType = 'flights';

    // Loading states
    isLoading = false;

    // Results
    flights: FlightDTO[] = [];
    hotels: HotelDTO[] = [];
    airports: AirportDTO[] = [];
    airlines: AirlineDTO[] = [];

    // Flight search params
    flightParams: FlightSearchParams = {
        origin: 'CDG',
        destination: 'JFK',
        departureDate: this.getDefaultDate(),
        adults: 2
    };

    // Hotel search params
    hotelCityCode = 'PAR';

    // Airport search params
    airportCountry = 'France';
    airportName = '';

    // Airline search params
    airlineName = '';

    // Selected items for import
    selectedFlights: Set<string> = new Set();
    selectedHotels: Set<string> = new Set();

    // Hotel Date Params
    checkinDate = this.getDefaultDate();
    checkoutDate = this.getPlusDaysDate(5);

    // City codes for hotel search
    // City codes for hotel search (Amadeus + Booking IDs)
    cityCodes = [
        { code: 'PAR', bookingId: '-553173', name: 'Paris' },
        { code: 'LON', bookingId: '-2601889', name: 'London' },
        { code: 'NYC', bookingId: '-2092174', name: 'New York' },
        { code: 'DXB', bookingId: '-782831', name: 'Dubai' },
        { code: 'BCN', bookingId: '-372490', name: 'Barcelona' },
        { code: 'ROM', bookingId: '-126693', name: 'Rome' },
        { code: 'BER', bookingId: '-1746443', name: 'Berlin' },
        { code: 'MAD', bookingId: '-390625', name: 'Madrid' },
        { code: 'AMS', bookingId: '-2140479', name: 'Amsterdam' },
        { code: 'LAX', bookingId: '-1781005', name: 'Los Angeles' }
    ];

    // Countries for airport search
    countries = [
        'France', 'United Kingdom', 'United States', 'Germany', 'Spain',
        'Italy', 'Japan', 'United Arab Emirates', 'Australia', 'Canada'
    ];

    ngOnInit(): void {
        // Subscribe to query params to set active tab
        this.route.queryParams.subscribe(params => {
            if (params['tab']) {
                this.activeTab = params['tab'] as TabType;
            }
        });
    }

    private getDefaultDate(): string {
        const date = new Date();
        date.setDate(date.getDate() + 1); // Tomorrow
        return date.toISOString().split('T')[0];
    }

    private getPlusDaysDate(days: number): string {
        const date = new Date();
        date.setDate(date.getDate() + 1 + days);
        return date.toISOString().split('T')[0];
    }

    setActiveTab(tab: TabType): void {
        this.activeTab = tab;
    }

    // ======== FLIGHTS ========
    searchFlights(): void {
        this.isLoading = true;
        this.flights = [];
        this.selectedFlights.clear();

        this.externalFlightService.searchFlights(this.flightParams).subscribe({
            next: (results) => {
                this.flights = results;
                this.isLoading = false;
                if (results.length === 0) {
                    this.sweetAlert.info('No flights found for the selected criteria');
                }
            },
            error: (err) => {
                console.error('Error searching flights:', err);
                this.sweetAlert.error('Failed to search flights. The API might be unavailable.');
                this.isLoading = false;
            }
        });
    }

    toggleFlightSelection(flightNumber: string): void {
        if (this.selectedFlights.has(flightNumber)) {
            this.selectedFlights.delete(flightNumber);
        } else {
            this.selectedFlights.add(flightNumber);
        }
    }

    isFlightSelected(flightNumber: string): boolean {
        return this.selectedFlights.has(flightNumber);
    }

    async importSelectedFlights(): Promise<void> {
        if (this.selectedFlights.size === 0) {
            this.sweetAlert.warning('Please select at least one flight to import');
            return;
        }

        const confirmed = await this.sweetAlert.confirmDelete(`Import ${this.selectedFlights.size} flight(s) to database?`);
        if (!confirmed.isConfirmed) return;

        this.sweetAlert.showLoading(`Importing ${this.selectedFlights.size} flights...`);

        let successCount = 0;
        let errorCount = 0;

        for (const flightNumber of this.selectedFlights) {
            const flightDTO = this.flights.find(f => f.flightNumber === flightNumber);
            if (!flightDTO) continue;

            // Convert FlightDTO to Flight entity
            const flight: Flight = {
                airline: flightDTO.airlineName || 'Unknown',
                flightNumber: flightDTO.flightNumber || '',
                origin: flightDTO.departureAirportCode || '',
                destination: flightDTO.arrivalAirportCode || '',
                departureTime: flightDTO.departureTime,
                arrivalTime: flightDTO.arrivalTime,
                price: flightDTO.price,
                seatsAvailable: flightDTO.availableSeats
            };

            try {
                await firstValueFrom(this.flightService.create(flight));
                successCount++;
            } catch (err) {
                console.error('Error importing flight:', err);
                errorCount++;
            }
        }

        this.sweetAlert.closeLoading();

        if (successCount > 0) {
            this.sweetAlert.success(`Successfully imported ${successCount} flight(s)!`);
            this.selectedFlights.clear();
        }
        if (errorCount > 0) {
            this.sweetAlert.error(`Failed to import ${errorCount} flight(s)`);
        }
    }

    async importSingleFlight(flightDTO: FlightDTO): Promise<void> {
        this.sweetAlert.showLoading('Importing flight...');

        const flight: Flight = {
            airline: flightDTO.airlineName || 'Unknown',
            flightNumber: flightDTO.flightNumber || '',
            origin: flightDTO.departureAirportCode || '',
            destination: flightDTO.arrivalAirportCode || '',
            departureTime: flightDTO.departureTime,
            arrivalTime: flightDTO.arrivalTime,
            price: flightDTO.price,
            seatsAvailable: flightDTO.availableSeats
        };

        this.flightService.create(flight).subscribe({
            next: () => {
                this.sweetAlert.closeLoading();
                this.sweetAlert.success(`Flight ${flightDTO.flightNumber} imported successfully!`);
            },
            error: (err) => {
                this.sweetAlert.closeLoading();
                console.error('Error importing flight:', err);
                this.sweetAlert.error('Failed to import flight');
            }
        });
    }

    // ======== HOTELS ========
    async searchHotels(): Promise<void> {
        this.isLoading = true;
        this.hotels = [];
        this.selectedHotels.clear();

        const selectedCity = this.cityCodes.find(c => c.code === this.hotelCityCode);
        const bookingId = selectedCity?.bookingId;

        // Create promises for both APIs
        const amadeusPromise = firstValueFrom(this.externalHotelService.searchByCity(this.hotelCityCode))
            .catch(err => {
                console.error('Amadeus API Error:', err);
                return [] as HotelDTO[];
            });

        const bookingPromise = bookingId
            ? firstValueFrom(this.externalHotelService.searchViaBooking({
                destination: bookingId,
                checkinDate: this.checkinDate,
                checkoutDate: this.checkoutDate
            })).catch(err => {
                console.error('Booking.com API Error:', err);
                return [] as HotelDTO[];
            })
            : Promise.resolve([] as HotelDTO[]);

        try {
            const [amadeusResults, bookingResults] = await Promise.all([amadeusPromise, bookingPromise]);

            // Mark source
            amadeusResults.forEach(h => h.name = `[Amadeus] ${h.name}`);
            bookingResults.forEach(h => h.name = `[Booking] ${h.name}`);

            this.hotels = [...amadeusResults, ...bookingResults];

            if (this.hotels.length === 0) {
                this.sweetAlert.info('No hotels found for the selected criteria');
            }
        } catch (err) {
            this.sweetAlert.error('Failed to search hotels');
        } finally {
            this.isLoading = false;
        }
    }

    toggleHotelSelection(hotelId: string): void {
        if (this.selectedHotels.has(hotelId)) {
            this.selectedHotels.delete(hotelId);
        } else {
            this.selectedHotels.add(hotelId);
        }
    }

    isHotelSelected(hotelId: string): boolean {
        return this.selectedHotels.has(hotelId);
    }

    async importSelectedHotels(): Promise<void> {
        if (this.selectedHotels.size === 0) {
            this.sweetAlert.warning('Please select at least one hotel to import');
            return;
        }

        const confirmed = await this.sweetAlert.confirmDelete(`Import ${this.selectedHotels.size} hotel(s) to database?`);
        if (!confirmed.isConfirmed) return;

        this.sweetAlert.showLoading(`Importing ${this.selectedHotels.size} hotels...`);

        let successCount = 0;
        let errorCount = 0;

        for (const hotelId of this.selectedHotels) {
            const hotelDTO = this.hotels.find(h => h.hotelId === hotelId);
            if (!hotelDTO) continue;

            // Convert HotelDTO to Hotel entity
            const hotel: Hotel = {
                name: hotelDTO.name || 'Unknown Hotel',
                address: hotelDTO.address || '',
                city: hotelDTO.city,
                country: hotelDTO.country,
                etoile: hotelDTO.stars
            };

            try {
                await firstValueFrom(this.hotelService.create(hotel));
                successCount++;
            } catch (err) {
                console.error('Error importing hotel:', err);
                errorCount++;
            }
        }

        this.sweetAlert.closeLoading();

        if (successCount > 0) {
            this.sweetAlert.success(`Successfully imported ${successCount} hotel(s)!`);
            this.selectedHotels.clear();
        }
        if (errorCount > 0) {
            this.sweetAlert.error(`Failed to import ${errorCount} hotel(s)`);
        }
    }

    async importSingleHotel(hotelDTO: HotelDTO): Promise<void> {
        this.sweetAlert.showLoading('Importing hotel...');

        const hotel: Hotel = {
            name: hotelDTO.name || 'Unknown Hotel',
            address: hotelDTO.address || '',
            city: hotelDTO.city,
            country: hotelDTO.country,
            etoile: hotelDTO.stars
        };

        this.hotelService.create(hotel).subscribe({
            next: () => {
                this.sweetAlert.closeLoading();
                this.sweetAlert.success(`Hotel "${hotelDTO.name}" imported successfully!`);
            },
            error: (err) => {
                this.sweetAlert.closeLoading();
                console.error('Error importing hotel:', err);
                this.sweetAlert.error('Failed to import hotel');
            }
        });
    }

    // ======== AIRPORTS ========
    searchAirports(): void {
        this.isLoading = true;
        this.airports = [];

        this.airportService.search({
            country: this.airportCountry || undefined,
            airportName: this.airportName || undefined
        }).subscribe({
            next: (results) => {
                this.airports = results;
                this.isLoading = false;
                if (results.length === 0) {
                    this.sweetAlert.info('No airports found');
                }
            },
            error: (err) => {
                console.error('Error searching airports:', err);
                this.sweetAlert.error('Failed to search airports');
                this.isLoading = false;
            }
        });
    }

    // ======== AIRLINES ========
    searchAirlines(): void {
        this.isLoading = true;
        this.airlines = [];

        this.airlineService.search(this.airlineName || undefined).subscribe({
            next: (results) => {
                this.airlines = results;
                this.isLoading = false;
                if (results.length === 0) {
                    this.sweetAlert.info('No airlines found');
                }
            },
            error: (err) => {
                console.error('Error searching airlines:', err);
                this.sweetAlert.error('Failed to search airlines');
                this.isLoading = false;
            }
        });
    }

    // Import all results at once
    async importAllFlights(): Promise<void> {
        if (this.flights.length === 0) {
            this.sweetAlert.warning('No flights to import');
            return;
        }

        const confirmed = await this.sweetAlert.confirmDelete(`Import ALL ${this.flights.length} flights to database?`);
        if (!confirmed.isConfirmed) return;

        // Select all and import
        this.flights.forEach(f => {
            if (f.flightNumber) this.selectedFlights.add(f.flightNumber);
        });
        await this.importSelectedFlights();
    }

    async importAllHotels(): Promise<void> {
        if (this.hotels.length === 0) {
            this.sweetAlert.warning('No hotels to import');
            return;
        }

        const confirmed = await this.sweetAlert.confirmDelete(`Import ALL ${this.hotels.length} hotels to database?`);
        if (!confirmed.isConfirmed) return;

        // Select all and import
        this.hotels.forEach(h => {
            if (h.hotelId) this.selectedHotels.add(h.hotelId);
        });
        await this.importSelectedHotels();
    }
}
