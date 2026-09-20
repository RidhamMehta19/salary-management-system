import { CommonModule } from '@angular/common';
import { Component, DestroyRef, OnInit, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { forkJoin, switchMap } from 'rxjs';
import { ButtonModule } from 'primeng/button';
import { CardModule } from 'primeng/card';
import { MessageModule } from 'primeng/message';
import { ProgressSpinnerModule } from 'primeng/progressspinner';
import { TableModule } from 'primeng/table';
import { TagModule } from 'primeng/tag';
import { ConfirmationService, MessageService } from 'primeng/api';
import { ApiService } from '../../core/api.service';
import { Employee, EmploymentStatus, SalaryHistory } from '../../core/models';
import { statusLabel, statusSeverity } from '../../core/constants';

@Component({
  selector: 'app-employee-detail',
  imports: [CommonModule, RouterLink, ButtonModule, CardModule, MessageModule, ProgressSpinnerModule, TableModule, TagModule],
  templateUrl: './employee-detail.component.html',
  styleUrl: './employee-detail.component.scss',
})
export class EmployeeDetailComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly api = inject(ApiService);
  private readonly confirmation = inject(ConfirmationService);
  private readonly messages = inject(MessageService);
  private readonly destroyRef = inject(DestroyRef);
  readonly loading = signal(true);
  readonly error = signal<string | null>(null);
  readonly employee = signal<Employee | null>(null);
  readonly history = signal<SalaryHistory[]>([]);
  readonly statusSaving = signal(false);

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.loading.set(true);
    this.route.paramMap.pipe(
      switchMap(params => forkJoin({
        employee: this.api.employee(params.get('id')!),
        history: this.api.salaryHistory(params.get('id')!),
      })),
      takeUntilDestroyed(this.destroyRef),
    ).subscribe({
      next: result => {
        this.employee.set(result.employee);
        this.history.set(result.history);
        this.loading.set(false);
      },
      error: () => {
        this.error.set('This employee could not be loaded. The record may no longer exist.');
        this.loading.set(false);
      },
    });
  }

  changeStatus(person: Employee): void {
    if (this.statusSaving()) return;
    const nextStatus: EmploymentStatus = person.status === 'INACTIVE' ? 'ACTIVE' : 'INACTIVE';
    this.confirmation.confirm({
      header: nextStatus === 'INACTIVE' ? 'Deactivate employee?' : 'Reactivate employee?',
      message: nextStatus === 'INACTIVE'
        ? 'The record will be retained but excluded from active workforce metrics.'
        : 'This employee will be included in active workforce metrics again.',
      icon: 'pi pi-exclamation-triangle',
      acceptLabel: nextStatus === 'INACTIVE' ? 'Deactivate' : 'Reactivate',
      rejectLabel: 'Cancel',
      accept: () => {
        this.statusSaving.set(true);
        this.api.updateStatus(person.id, nextStatus).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
          next: updated => {
            this.employee.set(updated);
            this.statusSaving.set(false);
            this.messages.add({
              severity: 'success',
              summary: 'Status updated',
              detail: `${updated.firstName} is now ${statusLabel(updated.status).toLowerCase()}.`,
            });
          },
          error: () => {
            this.statusSaving.set(false);
            this.messages.add({
              severity: 'error',
              summary: 'Update failed',
              detail: 'The employee status could not be changed.',
            });
          },
        });
      },
    });
  }
  initials(person: Employee): string { return `${person.firstName[0]}${person.lastName[0]}`.toUpperCase(); }
  statusLabel = statusLabel;
  statusSeverity = statusSeverity;
}
