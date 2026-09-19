import { CommonModule } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
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

@Component({
  selector: 'app-employees',
  imports: [CommonModule, FormsModule, RouterLink, ButtonModule, CardModule, InputTextModule, MessageModule, PaginatorModule, ProgressSpinnerModule, SelectModule, TagModule],
  template: `
    <header class="page-header"><div><p class="eyebrow">People directory</p><h1>Employees</h1><p class="subtitle">Search, filter, and maintain the organisation's salary records.</p></div><a pButton routerLink="/employees/new" label="Add employee" icon="pi pi-plus"></a></header>
    <p-card><div class="filters"><div class="search-field"><label for="employee-search">Search</label><input pInputText id="employee-search" [(ngModel)]="search" (keyup.enter)="applyFilters()" placeholder="Name or employee ID" /></div><div><label for="department-filter">Department</label><p-select id="department-filter" [options]="departmentOptions" optionLabel="name" optionValue="name" [(ngModel)]="department" placeholder="All departments" [showClear]="true" (onChange)="applyFilters()" /></div><div><label for="country-filter">Country</label><p-select id="country-filter" [options]="countries" [(ngModel)]="country" placeholder="All countries" [showClear]="true" (onChange)="applyFilters()" /></div><div><label for="status-filter">Status</label><p-select id="status-filter" [options]="statusOptions" optionLabel="label" optionValue="value" [(ngModel)]="status" placeholder="All statuses" [showClear]="true" (onChange)="applyFilters()" /></div><div><label for="sort-filter">Sort</label><p-select id="sort-filter" [options]="sortOptions" optionLabel="label" optionValue="value" [(ngModel)]="sort" (onChange)="applyFilters()" /></div><button pButton severity="secondary" [text]="true" label="Clear" icon="pi pi-filter-slash" (click)="clearFilters()"></button></div></p-card>
    <section class="results-card"><div class="results-heading"><div><h2>Employee records</h2><span>{{ page()?.totalElements ?? 0 | number }} records</span></div>@if (loading()) { <p-progressSpinner ariaLabel="Loading employees" [style]="{width:'24px',height:'24px'}" /> }</div><div class="results-body">
      @if (error()) { <p-message severity="error" [text]="error()!" /> } @else if (!loading() && page()?.content?.length === 0) { <div class="empty"><span>⌕</span><h3>No employees found</h3><p>Try broadening your search or clearing a filter.</p></div> } @else { <div class="table-wrap"><table><thead><tr><th>Employee</th><th>Department</th><th>Location</th><th>Status</th><th>Base salary</th><th></th></tr></thead><tbody>@for (employee of page()?.content ?? []; track employee.id) { <tr><td><a class="employee-link" [routerLink]="['/employees', employee.id]"><span class="avatar">{{ initials(employee) }}</span><span><strong>{{ employee.firstName }} {{ employee.lastName }}</strong><small>{{ employee.employeeNumber }} · {{ employee.email }}</small></span></a></td><td>{{ employee.department }}<small class="muted-block">{{ employee.jobTitle }}</small></td><td>{{ employee.location }}<small class="muted-block">{{ employee.country }}</small></td><td><p-tag [value]="statusLabel(employee.status)" [severity]="statusSeverity(employee.status)" /></td><td><strong>{{ employee.currency }} {{ employee.baseSalary | number:'1.0-0' }}</strong><small class="muted-block">{{ employee.currency }} {{ employee.totalCompensation | number:'1.0-0' }} total</small></td><td><a pButton [routerLink]="['/employees', employee.id]" icon="pi pi-chevron-right" [text]="true" [rounded]="true" aria-label="Open employee"></a></td></tr> }</tbody></table></div><p-paginator [rows]="size" [first]="(page()?.page ?? 0) * size" [totalRecords]="page()?.totalElements ?? 0" [rowsPerPageOptions]="[10,20,50]" (onPageChange)="pageChanged($event)" /> }
    </div></section>
  `,
  styles: [`.page-header{align-items:flex-start;display:flex;justify-content:space-between;margin-bottom:2rem}.eyebrow{color:#2f855a;font-size:.75rem;font-weight:800;letter-spacing:.1em;margin:0 0 .45rem;text-transform:uppercase}h1{color:#102a43;font-size:clamp(1.65rem,3vw,2.25rem);letter-spacing:-.04em;margin:0}.subtitle{color:#627d98;margin:.55rem 0 0}.filters{align-items:end;display:grid;gap:.9rem;grid-template-columns:1.6fr repeat(4,1fr) auto}.filters label{color:#486581;display:block;font-size:.72rem;font-weight:700;margin-bottom:.4rem}.filters input,.filters p-select{width:100%}.results-card{background:#fff;border:1px solid #e6edf3;border-radius:12px;margin-top:1.25rem;overflow:hidden}.results-heading{align-items:center;display:flex;justify-content:space-between;padding:1.2rem 1.25rem}.results-heading h2{color:#243b53;font-size:1rem;margin:0}.results-heading span{color:#829ab1;font-size:.75rem}.table-wrap{overflow:auto}table{border-collapse:collapse;min-width:850px;width:100%}th{background:#f8fafc;color:#829ab1;font-size:.68rem;letter-spacing:.06em;padding:.7rem 1.25rem;text-align:left;text-transform:uppercase}td{border-top:1px solid #edf2f7;color:#486581;font-size:.82rem;padding:.85rem 1.25rem;vertical-align:middle}td small{font-size:.71rem}.employee-link{align-items:center;color:#243b53;display:flex;gap:.65rem;text-decoration:none}.employee-link strong{display:block}.employee-link small{color:#829ab1;display:block;margin-top:.2rem}.avatar{align-items:center;background:#e6fffa;border-radius:50%;color:#16846d;display:inline-flex;font-size:.7rem;font-weight:800;height:32px;justify-content:center;width:32px}.muted-block{color:#829ab1;display:block;margin-top:.2rem}.empty{padding:4rem 1rem;text-align:center}.empty>span{color:#9fb3c8;font-size:2rem}.empty h3{color:#334e68;margin:.5rem 0 .3rem}.empty p{color:#829ab1;margin:0}.results-card p-paginator{display:block;padding:.6rem 1rem}@media(max-width:1100px){.filters{grid-template-columns:repeat(3,1fr)}.search-field{grid-column:span 2}}@media(max-width:720px){.page-header{align-items:stretch;flex-direction:column;gap:1rem}.filters{grid-template-columns:1fr 1fr}.search-field{grid-column:span 2}.filters>button{grid-column:span 2}}`]
})
export class EmployeesComponent implements OnInit {
  private readonly api = inject(ApiService);
  readonly loading = signal(false);
  readonly error = signal<string | null>(null);
  readonly page = signal<PageResponse<Employee> | null>(null);
  readonly countries = ['Australia', 'Canada', 'Germany', 'India', 'United Kingdom', 'United States'];
  readonly statusOptions = [{ label: 'Active', value: 'ACTIVE' as EmploymentStatus }, { label: 'On leave', value: 'ON_LEAVE' as EmploymentStatus }, { label: 'Inactive', value: 'INACTIVE' as EmploymentStatus }];
  readonly sortOptions = [{ label: 'Recently updated', value: 'updatedAt,desc' }, { label: 'Last name A–Z', value: 'lastName,asc' }, { label: 'Base salary high–low', value: 'baseSalary,desc' }];
  departmentOptions: Department[] = [];
  search = ''; department = ''; country = ''; status: EmploymentStatus | undefined; sort = 'updatedAt,desc'; pageIndex = 0; size = 20;
  ngOnInit(): void { this.api.departments().subscribe({ next: departments => this.departmentOptions = departments }); this.load(); }
  load(): void { this.loading.set(true); this.error.set(null); this.api.employees({ page: this.pageIndex, size: this.size, sort: this.sort, search: this.search, department: this.department, country: this.country, status: this.status }).subscribe({ next: result => { this.page.set(result); this.loading.set(false); }, error: () => { this.error.set('We could not load employee records. Check the API connection and try again.'); this.loading.set(false); } }); }
  applyFilters(): void { this.pageIndex = 0; this.load(); }
  clearFilters(): void { this.search = ''; this.department = ''; this.country = ''; this.status = undefined; this.sort = 'updatedAt,desc'; this.applyFilters(); }
  pageChanged(event: PaginatorState): void { this.pageIndex = event.page ?? 0; this.size = event.rows ?? 20; this.load(); }
  initials(employee: Employee): string { return `${employee.firstName[0]}${employee.lastName[0]}`.toUpperCase(); }
  statusLabel(status: EmploymentStatus): string { return status === 'ON_LEAVE' ? 'On leave' : status[0] + status.slice(1).toLowerCase(); }
  statusSeverity(status: EmploymentStatus): 'success' | 'warn' | 'danger' { return status === 'ACTIVE' ? 'success' : status === 'ON_LEAVE' ? 'warn' : 'danger'; }
}
