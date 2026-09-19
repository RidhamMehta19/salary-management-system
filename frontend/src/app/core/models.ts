export type EmploymentStatus = 'ACTIVE' | 'INACTIVE' | 'ON_LEAVE';

export interface Employee {
  id: string;
  employeeNumber: string;
  firstName: string;
  lastName: string;
  email: string;
  department: string;
  jobTitle: string;
  country: string;
  location: string;
  status: EmploymentStatus;
  hireDate: string;
  currency: string;
  baseSalary: number;
  bonus: number;
  totalCompensation: number;
  createdAt: string;
  updatedAt: string;
}

export interface EmployeeRequest extends Omit<Employee, 'id' | 'totalCompensation' | 'createdAt' | 'updatedAt'> {
  salaryChangeReason?: string;
}

export interface PageResponse<T> { content: T[]; page: number; size: number; totalElements: number; totalPages: number; }
export interface Department { id: string; name: string; }
export interface SalaryHistory { id: string; currency: string; baseSalary: number; bonus: number; totalCompensation: number; effectiveDate: string; changeReason: string; recordedAt: string; }
export interface CurrencySummary { currency: string; employeeCount: number; totalCompensation: number; averageBaseSalary: number; }
export interface DashboardSummary { activeEmployees: number; totalEmployees: number; compensationByCurrency: CurrencySummary[]; }
export interface CompensationBreakdown { label: string; currency: string; employeeCount: number; totalCompensation: number; averageBaseSalary: number; }
export interface SalaryDistributionBucket { currency: string; band: string; employeeCount: number; }
export interface ApiError { message: string; fieldErrors?: Record<string, string>; }
