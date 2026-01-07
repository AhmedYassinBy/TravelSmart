import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { LoginRequest, loginService } from '../services/login-service.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './login.component.html',
  styleUrl: './login.component.css'
})
export class LoginComponent {
  user: LoginRequest = {
    username: '',
    password: ''
  };

  errorMessage = '';
  isLoading = false;

  constructor(private authService: loginService, private router: Router) { }

  login(): void {
    // Basic validation
    if (!this.user.username?.trim() || !this.user.password?.trim()) {
      this.errorMessage = 'Please enter both username and password.';
      return;
    }

    this.errorMessage = '';
    this.isLoading = true;

    this.authService.login(this.user).subscribe({
      next: (response) => {
        this.isLoading = false;
        if (response.token) {
          // Successful login - redirect to admin
          this.router.navigate(['/admin']);
        } else if (response.message) {
          // Error message from server
          this.errorMessage = response.message;
        }
      },
      error: (err) => {
        this.isLoading = false;
        this.errorMessage = err.error?.message || 'Incorrect username or password.';
      }
    });
  }
}

