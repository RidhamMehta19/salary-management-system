import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { EmployeesComponent } from './employees.component';
import { Employee } from '../../core/models';

describe('EmployeesComponent', () => {
  let fixture: ComponentFixture<EmployeesComponent>;
  let component: EmployeesComponent;
  let http: HttpTestingController;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [EmployeesComponent],
      providers: [provideRouter([]), provideHttpClient(), provideHttpClientTesting()]
    }).compileComponents();
    fixture = TestBed.createComponent(EmployeesComponent);
    component = fixture.componentInstance;
    http = TestBed.inject(HttpTestingController);
    fixture.detectChanges();
    http.expectOne(call => call.url.endsWith('/departments')).flush([{ id: '1', name: 'Engineering' }]);
    http.expectOne(call => call.url.endsWith('/employees')).flush({ content: [], page: 0, size: 20, totalElements: 0, totalPages: 0 });
  });

  afterEach(() => http.verify());

  it('resets to the first page when filters are applied', () => {
    component.pageIndex = 3;
    component.search = 'Ava';
    component.applyFilters();

    const request = http.expectOne(call => call.url.endsWith('/employees'));
    expect(request.request.params.get('page')).toBe('0');
    expect(request.request.params.get('search')).toBe('Ava');
    request.flush({ content: [], page: 0, size: 20, totalElements: 0, totalPages: 0 });
  });

  it('creates initials and human-readable status labels', () => {
    const employee = { firstName: 'Ava', lastName: 'Patel' } as Employee;
    expect(component.initials(employee)).toBe('AP');
    expect(component.statusLabel('ON_LEAVE')).toBe('On leave');
  });
});
