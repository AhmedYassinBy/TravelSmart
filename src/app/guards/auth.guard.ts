import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';

export const authGuard: CanActivateFn = (route, state) => {
  const router =inject(Router);
  const username = localStorage.getItem('username');
  const password = localStorage.getItem('password');
    if (username==null || password==null) { router.navigateByUrl(''); return false;}

  return true;
};
