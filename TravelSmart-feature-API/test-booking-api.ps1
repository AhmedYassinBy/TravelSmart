# Test Booking API endpoint
Write-Host "Waiting for server to start..." -ForegroundColor Yellow
Start-Sleep -Seconds 15

Write-Host "`nTesting Booking Hotel API..." -ForegroundColor Cyan
try {
    $response = Invoke-RestMethod `
        -Uri "http://localhost:8085/api/hotels/booking?destination=-553173&checkinDate=2026-01-15&checkoutDate=2026-01-20" `
        -Method GET `
        -Headers @{"Accept" = "application/json"} `
        -ErrorAction Stop
    
    Write-Host "SUCCESS! Response:" -ForegroundColor Green
    $response | ConvertTo-Json -Depth 3
} catch {
    Write-Host "ERROR:" -ForegroundColor Red
    Write-Host "StatusCode: $($_.Exception.Response.StatusCode.value__)" -ForegroundColor Red
    Write-Host "Message: $($_.Exception.Message)" -ForegroundColor Red
    
    if ($_.ErrorDetails.Message) {
        Write-Host "Details:" -ForegroundColor Red
        Write-Host $_.ErrorDetails.Message
    }
}

Write-Host "`n`nCheck the server logs in the other terminal for detailed error information." -ForegroundColor Yellow
