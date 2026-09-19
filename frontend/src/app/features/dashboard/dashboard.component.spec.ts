import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { DashboardComponent } from './dashboard.component';

describe('DashboardComponent', () => {
  let fixture: ComponentFixture<DashboardComponent>;
  let component: DashboardComponent;
  let http: HttpTestingController;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DashboardComponent],
      providers: [provideRouter([]), provideHttpClient(), provideHttpClientTesting()]
    }).compileComponents();
    fixture = TestBed.createComponent(DashboardComponent);
    component = fixture.componentInstance;
    http = TestBed.inject(HttpTestingController);
    fixture.detectChanges();
    http.expectOne(call => call.url.endsWith('/dashboard/summary')).flush({ activeEmployees: 8, totalEmployees: 10, compensationByCurrency: [{ currency: 'USD', employeeCount: 8, totalCompensation: 800000, averageBaseSalary: 90000 }] });
    http.expectOne(call => call.url.endsWith('/dashboard/by-department')).flush([{ label: 'Engineering', currency: 'USD', employeeCount: 8, totalCompensation: 800000, averageBaseSalary: 90000 }]);
    http.expectOne(call => call.url.endsWith('/dashboard/by-country')).flush([]);
    http.expectOne(call => call.url.endsWith('/dashboard/salary-distribution')).flush([]);
  });

  afterEach(() => http.verify());

  it('maps summary and aggregate responses into dashboard state', () => {
    expect(component.summary()?.activeEmployees).toBe(8);
    expect(component.topDepartments[0].label).toBe('Engineering');
    expect(component.totalCompensation(component.summary()!)).toContain('USD 800,000');
  });
});
