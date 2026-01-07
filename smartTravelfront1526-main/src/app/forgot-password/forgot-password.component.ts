import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { LoginComponent } from '../login/login.component';
import { Router } from '@angular/router';
import { loginService } from '../services/login-service.service';

@Component({
  selector: 'app-forgot-password',
  standalone: true,
  imports: [CommonModule,FormsModule],
  templateUrl: './forgot-password.component.html',
  styleUrl: './forgot-password.component.css'
})
export class ForgotPasswordComponent {
  email = '';
  message = '';
  error = '';

  constructor(private login: loginService, private router: Router) {}

  onSubmit() {
    this.message = '';
    this.error = '';
    this.login.requestPasswordReset(this.email).subscribe({
      next: (res) => {
        this.message = res.message || 'If this email exists, a reset link has been sent.';
      },
      error: (err) => {
        this.error = 'An error occurred. Please try again.';
      },
    });
  }
}
