import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import {  RegisterRequest } from '../services/sign-up.service';
import { SignUpService } from '../services/sign-up.service';

@Component({
  selector: 'app-sign-up',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './sign-up.component.html',
  styleUrl: './sign-up.component.css'
})
export class SignUpComponent {
  user = {
    username: '',      // 👈 Added username
    email: '',
    firstName: '',
    lastName: '',
    password: '',
    confirmPassword: ''
  };

  errorMessage = '';

  constructor(private signupService: SignUpService, private router: Router) {}

  register() {
    if (this.user.password !== this.user.confirmPassword) {
      this.errorMessage = 'Passwords do not match!';
      return;
    }

    // Prepare request body matching backend's expected fields
    const registerRequest: RegisterRequest = {
      username: this.user.username,
      password: this.user.password,
      email: this.user.email,
      firstName: this.user.firstName,
      lastName: this.user.lastName
    };

    this.signupService.register(registerRequest).subscribe({
      next: (response) => {
        console.log('✅ Registration successful', response);
        localStorage.setItem('token', response.token);
        localStorage.setItem('username', response.username);
        this.router.navigate(['/admin']); // redirect after signup
      },
      error: (err) => {
        console.error('❌ Registration failed:', err);
        this.errorMessage = err.error?.message || 'Registration failed. Please try again.';
      }
    });
  }
}
