// src/app/core/sweetalert.service.ts
import { Injectable } from '@angular/core';
import Swal, { SweetAlertResult } from 'sweetalert2';

export interface FormField {
  name: string;
  label: string;
  type: 'text' | 'number' | 'email' | 'password' | 'textarea' | 'select' | 'datetime-local';
  required?: boolean;
  placeholder?: string;
  options?: { value: string; label: string }[]; // For select type
}

@Injectable({ providedIn: 'root' })
export class SweetAlertService {

  /**
   * Show success message
   */
  success(message: string, title: string = 'Success!'): Promise<SweetAlertResult> {
    return Swal.fire({
      icon: 'success',
      title,
      text: message,
      confirmButtonColor: '#10B981',
      timer: 2000,
      timerProgressBar: true
    });
  }

  /**
   * Show error message
   */
  error(message: string, title: string = 'Error!'): Promise<SweetAlertResult> {
    return Swal.fire({
      icon: 'error',
      title,
      text: message,
      confirmButtonColor: '#EF4444'
    });
  }

  /**
   * Show warning message
   */
  warning(message: string, title: string = 'Warning!'): Promise<SweetAlertResult> {
    return Swal.fire({
      icon: 'warning',
      title,
      text: message,
      confirmButtonColor: '#F59E0B'
    });
  }

  /**
   * Confirm delete action
   */
  confirmDelete(message: string = 'You won\'t be able to revert this!'): Promise<SweetAlertResult> {
    return Swal.fire({
      title: 'Are you sure?',
      text: message,
      icon: 'warning',
      showCancelButton: true,
      confirmButtonColor: '#EF4444',
      cancelButtonColor: '#6B7280',
      confirmButtonText: 'Yes, delete it!',
      cancelButtonText: 'Cancel'
    });
  }

  /**
   * Generic confirm dialog
   */
  async confirm(title: string, message: string = ''): Promise<boolean> {
    const result = await Swal.fire({
      title,
      text: message,
      icon: 'warning',
      showCancelButton: true,
      confirmButtonColor: '#3B82F6',
      cancelButtonColor: '#6B7280',
      confirmButtonText: 'Yes, proceed',
      cancelButtonText: 'Cancel'
    });
    return result.isConfirmed;
  }

  /**
   * Show loading indicator
   */
  showLoading(message: string = 'Please wait...'): void {
    Swal.fire({
      title: message,
      allowOutsideClick: false,
      allowEscapeKey: false,
      didOpen: () => {
        Swal.showLoading();
      }
    });
  }

  /**
   * Close loading indicator
   */
  closeLoading(): void {
    Swal.close();
  }

  /**
   * Build HTML form from fields configuration
   */
  private buildFormHtml(fields: FormField[], initialData: any = {}): string {
    let html = '<div class="swal2-form-container" style="text-align: left; padding: 0 20px;">';

    fields.forEach(field => {
      const value = initialData[field.name] || '';
      const required = field.required ? 'required' : '';

      html += `<div style="margin-bottom: 15px;">`;
      html += `<label style="display: block; margin-bottom: 5px; font-weight: 600; color: #374151;">${field.label}${field.required ? ' *' : ''}</label>`;

      if (field.type === 'textarea') {
        html += `<textarea id="swal-${field.name}" class="swal2-textarea" placeholder="${field.placeholder || ''}" ${required} style="width: 100%; min-height: 80px;">${value}</textarea>`;
      } else if (field.type === 'select') {
        html += `<select id="swal-${field.name}" class="swal2-select" ${required} style="width: 100%;">`;
        html += `<option value="">Select...</option>`;
        field.options?.forEach(opt => {
          const selected = value === opt.value ? 'selected' : '';
          html += `<option value="${opt.value}" ${selected}>${opt.label}</option>`;
        });
        html += `</select>`;
      } else {
        html += `<input id="swal-${field.name}" type="${field.type}" class="swal2-input" value="${value}" placeholder="${field.placeholder || ''}" ${required} style="width: 100%; margin: 0;">`;
      }

      html += `</div>`;
    });

    html += '</div>';
    return html;
  }

  /**
   * Show form dialog for creating/editing entities
   */
  async showForm<T>(
    title: string,
    fields: FormField[],
    initialData: any = {},
    confirmButtonText: string = 'Save'
  ): Promise<SweetAlertResult<any>> {
    return Swal.fire({
      title,
      html: this.buildFormHtml(fields, initialData),
      showCancelButton: true,
      confirmButtonText,
      confirmButtonColor: '#3B82F6',
      cancelButtonColor: '#6B7280',
      width: '500px',
      preConfirm: () => {
        const result: any = {};
        let hasError = false;

        fields.forEach(field => {
          const element = document.getElementById(`swal-${field.name}`) as HTMLInputElement;
          const value = element?.value?.trim() || '';

          if (field.required && !value) {
            Swal.showValidationMessage(`${field.label} is required`);
            hasError = true;
            return;
          }

          // Type conversion
          if (field.type === 'number' && value) {
            result[field.name] = parseFloat(value);
          } else {
            result[field.name] = value;
          }
        });

        if (hasError) return false;
        return result as T;
      }
    });
  }

  /**
   * Show info message
   */
  info(message: string, title: string = 'Info'): Promise<SweetAlertResult> {
    return Swal.fire({
      icon: 'info',
      title,
      text: message,
      confirmButtonColor: '#3B82F6'
    });
  }
}
