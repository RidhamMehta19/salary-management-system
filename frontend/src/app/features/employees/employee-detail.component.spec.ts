import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute, provideRouter } from '@angular/router';
import { Subject } from 'rxjs';
import { ConfirmationService, MessageService } from 'primeng/api';
import { EmployeeDetailComponent } from './employee-detail.component';
import { Employee } from '../../core/models';

describe('EmployeeDetailComponent', () => {
  let fixture: ComponentFixture<EmployeeDetailComponent>;
  let component: EmployeeDetailComponent;
  let http: HttpTestingController;
  let statusResponse: Subject<Employee>;
  let routeParams: Subject<{ get: (name: string) => string | null }>;

  const employee: Employee = {
    id: 'employee-id', employeeNumber: 'EMP-1', firstName: 'Ada', lastName: 'Lovelace',
    email: 'ada@example.org', department: 'Engineering', jobTitle: 'Engineer',
    country: 'United States', location: 'New York', status: 'ACTIVE', hireDate: '2020-01-01',
    currency: 'USD', baseSalary: 90000, bonus: 5000, totalCompensation: 95000,
    createdAt: '2020-01-01T00:00:00Z', updatedAt: '2020-01-01T00:00:00Z', version: 1,
  };

  beforeEach(async () => {
    statusResponse = new Subject<Employee>();
    routeParams = new Subject();
    await TestBed.configureTestingModule({
      imports: [EmployeeDetailComponent],
      providers: [
        provideRouter([]), provideHttpClient(), provideHttpClientTesting(),
        ConfirmationService, MessageService,
        { provide: ActivatedRoute, useValue: { paramMap: routeParams.asObservable() } },
      ],
    }).compileComponents();
    fixture = TestBed.createComponent(EmployeeDetailComponent);
    component = fixture.componentInstance;
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('loads the employee and salary history', () => {
    component.ngOnInit();
    routeParams.next({ get: () => 'employee-id' });
    http.expectOne('/api/employees/employee-id').flush(employee);
    http.expectOne('/api/employees/employee-id/salary-history').flush([]);

    expect(component.employee()?.employeeNumber).toBe('EMP-1');
    expect(component.history()).toEqual([]);
    expect(component.loading()).toBeFalse();
  });

  it('transitions status and disables the action while in flight', () => {
    component.employee.set(employee);
    const confirmation = TestBed.inject(ConfirmationService);
    spyOn(confirmation, 'confirm').and.callFake(options => options.accept?.());
    spyOn(component['api'], 'updateStatus').and.returnValue(statusResponse.asObservable());

    component.changeStatus(employee);
    expect(component.statusSaving()).toBeTrue();
    component.changeStatus(employee);
    expect(confirmation.confirm).toHaveBeenCalledTimes(1);

    statusResponse.next({ ...employee, status: 'INACTIVE' });
    statusResponse.complete();
    expect(component.employee()?.status).toBe('INACTIVE');
    expect(component.statusSaving()).toBeFalse();
  });
});
