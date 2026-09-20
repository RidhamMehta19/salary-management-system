import { CommonModule } from '@angular/common';
import { Component, DestroyRef, OnInit, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { ButtonModule } from 'primeng/button';
import { CardModule } from 'primeng/card';
import { InputTextModule } from 'primeng/inputtext';
import { MessageModule } from 'primeng/message';
import { PaginatorModule, PaginatorState } from 'primeng/paginator';
import { ProgressSpinnerModule } from 'primeng/progressspinner';
import { SelectModule } from 'primeng/select';
import { TagModule } from 'primeng/tag';
import { ApiService } from '../../core/api.service';
import { Department, Employee, EmploymentStatus, PageResponse } from '../../core/models';
import { COUNTRIES, STATUS_OPTIONS, statusLabel, statusSeverity } from '../../core/constants';

@Component({
  selector: 'app-employees',
  imports: [
    CommonModule, FormsModule, RouterLink, ButtonModule, CardModule, InputTextModule, MessageModule,
    PaginatorModule, ProgressSpinnerModule, SelectModule, TagModule,
  ],
  templateUrl: './employees.component.html',
  styleUrl: './employees.component.scss',
})
export class EmployeesComponent implements OnInit {
  private readonly api = inject(ApiService);
  readonly loading = signal(false);
  readonly error = signal<string | null>(null);
  readonly departmentError = signal<string | null>(null);
  readonly page = signal<PageResponse<Employee> | null>(null);
  readonly countries = COUNTRIES;
  readonly statusOptions = STATUS_OPTIONS;
  private readonly destroyRef = inject(DestroyRef);
  readonly sortOptions = [
    { label: 'Recently updated', value: 'updatedAt,desc' },
    { label: 'Last name A–Z', value: 'lastName,asc' },
    { label: 'Base salary high–low', value: 'baseSalary,desc' },
  ];
  departmentOptions: Department[] = [];
  search = ''; department = ''; country = ''; status: EmploymentStatus | undefined; sort = 'updatedAt,desc'; pageIndex = 0; size = 20;
  ngOnInit(): void {
    this.api.departments().pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: departments => this.departmentOptions = departments,
      error: () => this.error.set('Departments could not be loaded; employee records are still available.'),
    });
    this.load();
  }
  load(): void {
    this.loading.set(true);
    this.error.set(null);
    this.api.employees({
      page: this.pageIndex,
      size: this.size,
      sort: this.sort,
      search: this.search,
      department: this.department,
      country: this.country,
      status: this.status,
    }).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: result => {
        this.page.set(result);
        this.loading.set(false);
      },
      error: () => {
        this.error.set('We could not load employee records. Check the API connection and try again.');
        this.loading.set(false);
      },
      });
  }
  applyFilters(): void { this.pageIndex = 0; this.load(); }
  clearFilters(): void {
    this.search = '';
    this.department = '';
    this.country = '';
    this.status = undefined;
    this.sort = 'updatedAt,desc';
    this.applyFilters();
  }

  pageChanged(event: PaginatorState): void {
    this.pageIndex = event.page ?? 0;
    this.size = event.rows ?? 20;
    this.load();
  }
  initials(employee: Employee): string { return `${employee.firstName[0]}${employee.lastName[0]}`.toUpperCase(); }
  statusLabel = statusLabel;
  statusSeverity = statusSeverity;
}
