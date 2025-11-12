import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { LoginRequest, loginService } from '../services/login-service.service'; // ✅ ton AuthService

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

  constructor(private authService: loginService, private router: Router) {}

  login(): void {
    console.log("jjjjjjj");
    

    this.authService.login(this.user).subscribe({
      next: (response) => {
        
          console.log('✅ Connexion réussie !');
          this.router.navigate(['/admin']); // Redirection après succès
          if (response.message) {
          this.errorMessage = response.message;
          console.log("aaaa");
          
        }
      },
      error: (err) => {
        console.error('❌ Erreur de connexion:', err);
        this.errorMessage = 'Nom d’utilisateur ou mot de passe incorrect.';
      },

    });
    return
  }
}
