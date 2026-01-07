import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { loginService } from '../services/login-service.service';

@Component({
  selector: 'app-reset-password',
  standalone: true,
  imports: [CommonModule,FormsModule],
  templateUrl: './reset-password.component.html',
  styleUrl: './reset-password.component.css'
})
export class ResetPasswordComponent {
  token = '';
  newPassword = '';
  message = '';
  error = '';
  

  constructor(
    private route: ActivatedRoute,
    private login: loginService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.token = this.route.snapshot.queryParamMap.get('token') || '';
  }

  onSubmit() {
    this.login.resetPassword(this.token, this.newPassword).subscribe({
      next: (res) => {
        this.message = res.message || 'Password reset successful!';
        
        setTimeout(() => {
        this.router.navigate(['/log']);
      }, 2000);
        
      },
      error: (err) => {
        this.error = err.error?.message || 'Invalid or expired token.';
      },
    });
  }

}
