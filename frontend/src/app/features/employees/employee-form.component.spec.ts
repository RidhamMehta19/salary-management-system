import { HttpErrorResponse, provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { EmployeeFormComponent } from './employee-form.component';
import { MessageService } from 'primeng/api';

describe('EmployeeFormComponent', () => {
  let fixture: ComponentFixture<EmployeeFormComponent>;
  let component: EmployeeFormComponent;
  let http: HttpTestingController;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [EmployeeFormComponent],
      providers: [provideRouter([]), provideHttpClient(), provideHttpClientTesting(), MessageService]
    }).compileComponents();
    fixture = TestBed.createComponent(EmployeeFormComponent);
    component = fixture.componentInstance;
    http = TestBed.inject(HttpTestingController);
    fixture.detectChanges();
    http.expectOne(call => call.url.endsWith('/departments')).flush([]);
  });

  afterEach(() => http.verify());

  it('blocks submission while required fields are invalid', () => {
    component.save();
    expect(component.form.invalid).toBeTrue();
    expect(component.saving()).toBeFalse();
    expect(component.validationMessage('department')).toBe('Department is required.');
  });

  it('provides specific validation messages for email and negative compensation', () => {
    component.form.controls.email.setValue('invalid-email');
    component.form.controls.email.markAsTouched();
    component.form.controls.baseSalary.setValue(-1);
    component.form.controls.baseSalary.markAsTouched();

    expect(component.validationMessage('email')).toBe('Enter a valid email address.');
    expect(component.validationMessage('baseSalary')).toBe('Salary cannot be negative.');
  });

  it('maps conflict, server, and network save errors to safe user messages', () => {
    const errorMessage = (error: HttpErrorResponse) => component['saveErrorMessage'](error);

    expect(errorMessage(new HttpErrorResponse({ status: 409, error: { message: 'Email address already exists' } })))
      .toBe('An employee with this email already exists.');
    expect(errorMessage(new HttpErrorResponse({ status: 409, error: { message: 'Employee ID already exists' } })))
      .toBe('An employee with this employee number already exists.');
    expect(errorMessage(new HttpErrorResponse({ status: 500 }))).toBe('Something went wrong while saving the employee. Please try again.');
    expect(errorMessage(new HttpErrorResponse({ status: 0 }))).toBe('Unable to reach the server. Please try again.');
  });

  it('creates an employee successfully', () => {
    fillForm();
    component.save();

    const request = http.expectOne(call => call.url.endsWith('/employees'));
    expect(request.request.method).toBe('POST');
    request.flush({ id: 'new-id', firstName: 'Ada', lastName: 'Lovelace' });
    expect(component.saving()).toBeTrue();
  });

  it('updates an employee with its current version', () => {
    component.editing = true;
    component.employeeId = 'employee-id';
    fillForm();
    component.form.controls.version.setValue(7);
    component.save();

    const request = http.expectOne(call => call.url.endsWith('/employees/employee-id'));
    expect(request.request.method).toBe('PUT');
    expect(request.request.body.version).toBe(7);
    request.flush({ id: 'employee-id', firstName: 'Ada', lastName: 'Lovelace' });
  });

  it('shows a conflict message when saving a stale employee', () => {
    fillForm();
    component.save();
    http.expectOne(call => call.url.endsWith('/employees')).flush(
      { message: 'The employee was changed by another request; reload and try again' },
      { status: 409, statusText: 'Conflict' },
    );

    expect(component.error()).toBe('This record is stale. Reload the employee before saving again.');
    expect(component.saving()).toBeFalse();
  });

  function fillForm(): void {
    component.form.patchValue({
      employeeNumber: 'EMP-1', firstName: 'Ada', lastName: 'Lovelace', email: 'ada@example.org',
      department: 'Engineering', jobTitle: 'Engineer', country: 'United States', location: 'New York',
      status: 'ACTIVE', hireDate: '2020-01-01', currency: 'USD', baseSalary: 90000, bonus: 5000,
    });
  }
});
