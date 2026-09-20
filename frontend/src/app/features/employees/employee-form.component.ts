import { CommonModule } from '@angular/common';
import { Component, DestroyRef, OnInit, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { forkJoin, switchMap } from 'rxjs';
import { ButtonModule } from 'primeng/button';
import { CardModule } from 'primeng/card';
import { InputTextModule } from 'primeng/inputtext';
import { MessageModule } from 'primeng/message';
import { ProgressSpinnerModule } from 'primeng/progressspinner';
import { SelectModule } from 'primeng/select';
import { MessageService } from 'primeng/api';
import { ApiService } from '../../core/api.service';
import { Department, Employee, EmployeeRequest, EmploymentStatus } from '../../core/models';
import { COUNTRIES, STATUS_OPTIONS } from '../../core/constants';

@Component({
  selector: 'app-employee-form',
  imports: [CommonModule, ReactiveFormsModule, RouterLink, ButtonModule, CardModule, InputTextModule, MessageModule, ProgressSpinnerModule, SelectModule],
  templateUrl: './employee-form.component.html',
  styleUrl: './employee-form.component.scss',
})
export class EmployeeFormComponent implements OnInit {
  private readonly fb = inject(NonNullableFormBuilder);
  private readonly api = inject(ApiService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly messages = inject(MessageService);
  private readonly destroyRef = inject(DestroyRef);
  readonly loading = signal(true);
  readonly saving = signal(false);
  readonly error = signal<string | null>(null);
  editing = false;
  employeeId = '';
  departments: Department[] = []; countries = COUNTRIES; statusOptions = STATUS_OPTIONS;
  readonly form = this.fb.group({
    employeeNumber: ['', [Validators.required, Validators.maxLength(30)]],
    firstName: ['', [Validators.required, Validators.maxLength(100)]],
    lastName: ['', [Validators.required, Validators.maxLength(100)]],
    email: ['', [Validators.required, Validators.email]],
    department: ['', Validators.required],
    jobTitle: ['', Validators.required],
    country: ['', Validators.required],
    location: ['', Validators.required],
    status: ['ACTIVE' as EmploymentStatus, Validators.required],
    hireDate: ['', Validators.required],
    currency: ['USD', [Validators.required, Validators.pattern(/^(USD|INR|GBP|EUR|CAD|AUD)$/)]],
    baseSalary: [0, [Validators.required, Validators.min(0)]],
    bonus: [0, [Validators.required, Validators.min(0)]],
    salaryChangeReason: [''],
    version: [0],
  });
  ngOnInit(): void {
    this.employeeId = this.route.snapshot.paramMap.get('id') ?? '';
    this.editing = Boolean(this.employeeId);
    if (this.editing) {
      forkJoin({ departments: this.api.departments(), employee: this.api.employee(this.employeeId) })
        .pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
          next: result => { this.departments = result.departments; this.form.patchValue(result.employee); this.loading.set(false); },
          error: () => { this.error.set('The employee record could not be loaded.'); this.loading.set(false); },
        });
    } else {
      this.api.departments().pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
        next: departments => { this.departments = departments; this.loading.set(false); },
        error: () => { this.error.set('Departments could not be loaded.'); this.loading.set(false); },
      });
    }
  }
  save(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.error.set(null);
    this.saving.set(true);
    const value = this.form.getRawValue();
    const request: EmployeeRequest = {
      ...value,
      salaryChangeReason: value.salaryChangeReason || undefined,
    };
    const action = this.editing
      ? this.api.updateEmployee(this.employeeId, request)
      : this.api.createEmployee(request);
    action.pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: employee => {
        this.messages.add({
          severity: 'success',
          summary: this.editing ? 'Employee updated' : 'Employee created',
          detail: `${employee.firstName} ${employee.lastName} was saved successfully.`,
        });
        this.router.navigate(['/employees', employee.id]);
      },
      error: error => {
        const message = this.saveErrorMessage(error);
        this.error.set(message);
        this.messages.add({ severity: 'error', summary: 'Save failed', detail: message });
        this.saving.set(false);
      },
    });
  }

  validationMessage(field: string): string {
    const control = this.form.get(field);
    if (!control?.touched || !control.invalid) return '';
    if (control.hasError('required')) return `${this.fieldLabel(field)} is required.`;
    if (control.hasError('email')) return 'Enter a valid email address.';
    if (control.hasError('min')) return `${this.fieldLabel(field)} cannot be negative.`;
    if (control.hasError('pattern')) return 'Enter a three-letter ISO currency code.';
    if (control.hasError('maxlength')) return `${this.fieldLabel(field)} is too long.`;
    return `${this.fieldLabel(field)} is invalid.`;
  }

  private fieldLabel(field: string): string {
    return ({
      employeeNumber: 'Employee ID', firstName: 'First name', lastName: 'Last name', email: 'Email',
      department: 'Department', jobTitle: 'Job title', country: 'Country', location: 'Location',
      status: 'Status', hireDate: 'Hire date', currency: 'Currency', baseSalary: 'Salary', bonus: 'Bonus',
    } as Record<string, string>)[field] ?? field;
  }

  private saveErrorMessage(error: unknown): string {
    if (!(error instanceof HttpErrorResponse)) {
      return 'Something went wrong while saving the employee. Please try again.';
    }
    const message = typeof error.error?.message === 'string' ? error.error.message : '';
    if (error.status === 400) return message || 'Please correct the highlighted form values.';
    if (error.status === 409 && message.toLowerCase().includes('changed')) {
      return 'This record is stale. Reload the employee before saving again.';
    }
    if (error.status === 409) {
      if (message.toLowerCase().includes('email')) return 'An employee with this email already exists.';
      if (message.toLowerCase().includes('employee')) {
        return 'An employee with this employee number already exists.';
      }
      return 'An employee with those details already exists.';
    }
    if (error.status === 404) return 'This employee record no longer exists.';
    if (error.status === 0) return 'Unable to reach the server. Please try again.';
    return 'Something went wrong while saving the employee. Please try again.';
  }
}
