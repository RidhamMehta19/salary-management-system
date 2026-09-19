import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { CompensationBreakdown, DashboardSummary, Department, Employee, EmployeeRequest, EmploymentStatus, PageResponse, SalaryDistributionBucket, SalaryHistory } from './models';

declare global { interface Window { APP_CONFIG?: { API_BASE_URL?: string }; } }

export interface EmployeeQuery { page: number; size: number; sort?: string; search?: string; department?: string; country?: string; status?: EmploymentStatus; }

@Injectable({ providedIn: 'root' })
export class ApiService {
  private readonly baseUrl = window.APP_CONFIG?.API_BASE_URL ?? '/api';

  constructor(private readonly http: HttpClient) {}

  employees(query: EmployeeQuery): Observable<PageResponse<Employee>> {
    let params = new HttpParams().set('page', query.page).set('size', query.size);
    (Object.entries(query) as [keyof EmployeeQuery, string | number | undefined][]).forEach(([key, value]) => {
      if (value !== undefined && value !== '' && key !== 'page' && key !== 'size') params = params.set(key, String(value));
    });
    return this.http.get<PageResponse<Employee>>(`${this.baseUrl}/employees`, { params });
  }

  employee(id: string): Observable<Employee> { return this.http.get<Employee>(`${this.baseUrl}/employees/${id}`); }
  createEmployee(request: EmployeeRequest): Observable<Employee> { return this.http.post<Employee>(`${this.baseUrl}/employees`, request); }
  updateEmployee(id: string, request: EmployeeRequest): Observable<Employee> { return this.http.put<Employee>(`${this.baseUrl}/employees/${id}`, request); }
  updateStatus(id: string, status: EmploymentStatus): Observable<Employee> { return this.http.patch<Employee>(`${this.baseUrl}/employees/${id}/status`, { status }); }
  salaryHistory(id: string): Observable<SalaryHistory[]> { return this.http.get<SalaryHistory[]>(`${this.baseUrl}/employees/${id}/salary-history`); }
  departments(): Observable<Department[]> { return this.http.get<Department[]>(`${this.baseUrl}/departments`); }
  summary(): Observable<DashboardSummary> { return this.http.get<DashboardSummary>(`${this.baseUrl}/dashboard/summary`); }
  byDepartment(): Observable<CompensationBreakdown[]> { return this.http.get<CompensationBreakdown[]>(`${this.baseUrl}/dashboard/by-department`); }
  byCountry(): Observable<CompensationBreakdown[]> { return this.http.get<CompensationBreakdown[]>(`${this.baseUrl}/dashboard/by-country`); }
  salaryDistribution(): Observable<SalaryDistributionBucket[]> { return this.http.get<SalaryDistributionBucket[]>(`${this.baseUrl}/dashboard/salary-distribution`); }
}
