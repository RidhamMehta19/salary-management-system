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
});
