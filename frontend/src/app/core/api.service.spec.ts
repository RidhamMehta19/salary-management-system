import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ApiService } from './api.service';
import { PageResponse, Employee } from './models';

describe('ApiService', () => {
  let service: ApiService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({ providers: [ApiService, provideHttpClient(), provideHttpClientTesting()] });
    service = TestBed.inject(ApiService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('builds server-side employee query parameters', () => {
    service.employees({ page: 2, size: 10, search: 'Ada', department: 'Engineering', sort: 'lastName,asc' }).subscribe();

    const request = http.expectOne(call => call.url.endsWith('/employees'));
    expect(request.request.method).toBe('GET');
    expect(request.request.params.get('page')).toBe('2');
    expect(request.request.params.get('size')).toBe('10');
    expect(request.request.params.get('search')).toBe('Ada');
    expect(request.request.params.get('department')).toBe('Engineering');
    expect(request.request.params.get('sort')).toBe('lastName,asc');
    request.flush({ content: [], page: 2, size: 10, totalElements: 0, totalPages: 0 } satisfies PageResponse<Employee>);
  });
});
