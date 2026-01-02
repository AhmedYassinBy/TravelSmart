# Script de test pour TravelSmart API
# Ce script teste tous les endpoints des APIs externes

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "   TravelSmart API - Tests Endpoints   " -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

$baseUrl = "http://localhost:8085"

# Fonction pour tester un endpoint
function Test-Endpoint {
    param(
        [string]$name,
        [string]$url
    )

    Write-Host "Testing: $name" -ForegroundColor Yellow
    Write-Host "URL: $url" -ForegroundColor Gray

    try {
        $response = Invoke-RestMethod -Uri $url -Method Get -TimeoutSec 30
        Write-Host "✓ SUCCESS" -ForegroundColor Green
        Write-Host "Response items: $($response.Count)" -ForegroundColor Green
        Write-Host ""
        return $true
    }
    catch {
        Write-Host "✗ FAILED: $($_.Exception.Message)" -ForegroundColor Red
        Write-Host ""
        return $false
    }
}

# Compteurs
$total = 0
$success = 0

Write-Host "Starting tests..." -ForegroundColor White
Write-Host ""

# Test 1: Airlines
$total++
if (Test-Endpoint "Airlines - All" "$baseUrl/api/airlines/search") { $success++ }

$total++
if (Test-Endpoint "Airlines - Emirates" "$baseUrl/api/airlines/search?airlineName=Emirates") { $success++ }

# Test 2: Airports
$total++
if (Test-Endpoint "Airports - France" "$baseUrl/api/airports/search?country=France") { $success++ }

$total++
if (Test-Endpoint "Airports - By Name" "$baseUrl/api/airports/search?airportName=Charles") { $success++ }

# Test 3: Hotels Amadeus
$total++
if (Test-Endpoint "Hotels Amadeus - Paris" "$baseUrl/api/hotels/search?cityCode=PAR") { $success++ }

$total++
if (Test-Endpoint "Hotels Amadeus - London" "$baseUrl/api/hotels/search?cityCode=LON") { $success++ }

# Test 4: Flights
$total++
if (Test-Endpoint "Flights - Paris to NY" "$baseUrl/api/flights/search?origin=CDG&destination=JFK&departureDate=2025-12-31&adults=1") { $success++ }

# Résumé
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "           Test Summary                 " -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Total tests: $total" -ForegroundColor White
Write-Host "Passed: $success" -ForegroundColor Green
Write-Host "Failed: $($total - $success)" -ForegroundColor Red
Write-Host ""

if ($success -eq $total) {
    Write-Host "✓ All tests passed!" -ForegroundColor Green
} else {
    Write-Host "⚠ Some tests failed. Check your API keys and configuration." -ForegroundColor Yellow
}

Write-Host ""
Write-Host "Press any key to exit..."
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")

