import { Routes } from '@angular/router';
import { LoginComponent } from './login/login.component';
import { UserLayoutComponent } from './user/user-layout/user-layout.component';
import { AdminLayoutComponent } from './admin/admin-layout/admin-layout.component';
import { authGuard } from './guards/auth.guard';
import { SignUpComponent } from './sign-up/sign-up.component';
import { ForgotPasswordComponent } from './forgot-password/forgot-password.component';
import { ResetPasswordComponent } from './reset-password/reset-password.component';

export const routes: Routes = [
    { path: '', redirectTo: 'log', pathMatch: 'full' },
    {

        path: 'log',
        component:LoginComponent
    },
    {
        path: 'user',
        component:UserLayoutComponent,
        canActivate: [authGuard]
        
    },
    {
        path:'admin',
        component:AdminLayoutComponent,
        
    },
    {
        path: 'registre',
        component:SignUpComponent
    },
      { path: 'forgot-password', component: ForgotPasswordComponent },

  { path: 'reset-password', component: ResetPasswordComponent },
];
