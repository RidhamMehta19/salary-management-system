import { EmploymentStatus } from './models';

export const COUNTRIES = ['Australia', 'Canada', 'Germany', 'India', 'United Kingdom', 'United States'];
export const STATUS_OPTIONS: { label: string; value: EmploymentStatus }[] = [
  { label: 'Active', value: 'ACTIVE' },
  { label: 'On leave', value: 'ON_LEAVE' },
  { label: 'Inactive', value: 'INACTIVE' },
];

export function statusLabel(status: EmploymentStatus): string {
  return status === 'ON_LEAVE' ? 'On leave' : status[0] + status.slice(1).toLowerCase();
}

export function statusSeverity(status: EmploymentStatus): 'success' | 'warn' | 'danger' {
  return status === 'ACTIVE' ? 'success' : status === 'ON_LEAVE' ? 'warn' : 'danger';
}
