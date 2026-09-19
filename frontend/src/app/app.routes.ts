import { Routes } from '@angular/router';
import { DashboardComponent } from './features/dashboard/dashboard.component';
import { EmployeeDetailComponent } from './features/employees/employee-detail.component';
import { EmployeeFormComponent } from './features/employees/employee-form.component';
import { EmployeesComponent } from './features/employees/employees.component';

export const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'dashboard' },
  { path: 'dashboard', component: DashboardComponent, title: 'Dashboard | Salary Management' },
  { path: 'employees', component: EmployeesComponent, title: 'Employees | Salary Management' },
  { path: 'employees/new', component: EmployeeFormComponent, title: 'Add Employee | Salary Management' },
  { path: 'employees/:id', component: EmployeeDetailComponent, title: 'Employee Details | Salary Management' },
  { path: 'employees/:id/edit', component: EmployeeFormComponent, title: 'Edit Employee | Salary Management' },
  { path: '**', redirectTo: 'dashboard' }
];
