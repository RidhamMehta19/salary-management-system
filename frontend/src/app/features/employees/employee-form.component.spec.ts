import { provideHttpClient } from '@angular/common/http';
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
  });
});
