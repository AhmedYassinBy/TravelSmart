import { Component,ElementRef, HostListener, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';

interface UserInfo {
  username: string;
  email: string;
  firstName: string;
  lastName: string;
  role: string;
}

@Component({
  selector: 'app-topbar',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './topbar.component.html',
  styleUrls: ['./topbar.component.css'],
})
export class TopbarComponent implements OnInit {
  isOpen = false;
  userInfo: UserInfo = {
    username: 'admin',
    email: 'admin@travelsmart.com',
    firstName: 'Admin',
    lastName: 'User',
    role: 'ROLE_ADMIN'
  };

  constructor(private eRef: ElementRef, private router: Router) {}

  ngOnInit() {
    this.loadUserInfo();
  }

  logout() {
    // Clear all localStorage data
    localStorage.clear();
    // Or remove specific items:
    // localStorage.removeItem('token');
    // localStorage.removeItem('username');
    // localStorage.removeItem('role');
    
    // Navigate to login page
    this.router.navigate(['/log']);
  }

  loadUserInfo() {
    const username = localStorage.getItem('username');
    const role = localStorage.getItem('role');
    
    if (username) {
      this.userInfo = {
        username: username,
        email: `${username}@travelsmart.com`,
        firstName: username,
        lastName: '',
        role: role || 'ROLE_ADMIN'
      };
    }
  }

  getInitials(): string {
    const username = this.userInfo.username || 'AD';
    return username.substring(0, 2).toUpperCase();
  }

  getFullName(): string {
    return this.userInfo.username;
  }

  getRoleDisplay(): string {
    return this.userInfo.role === 'ROLE_ADMIN' ? 'Administrator' : 'User';
  }

  toggleDropdown() {
    this.isOpen = !this.isOpen;
  }

  closeDropdown() {
    this.isOpen = false;
  }

  @HostListener('document:click', ['$event'])
  clickOutside(event: Event) {
    if (!this.eRef.nativeElement.contains(event.target)) {
      this.closeDropdown();
    }
  }
}
