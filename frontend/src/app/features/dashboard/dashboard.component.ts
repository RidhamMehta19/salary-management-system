import { CommonModule } from '@angular/common';
import { Component, DestroyRef, OnInit, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { forkJoin } from 'rxjs';
import { CardModule } from 'primeng/card';
import { ButtonModule } from 'primeng/button';
import { ApiService } from '../../core/api.service';
import { CompensationBreakdown, DashboardSummary, SalaryDistributionBucket } from '../../core/models';

@Component({
  selector: 'app-dashboard',
  imports: [CommonModule, RouterLink, CardModule, ButtonModule],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.scss',
})
export class DashboardComponent implements OnInit {
  private readonly api = inject(ApiService);
  private readonly destroyRef = inject(DestroyRef);
  private animationFrame: number | null = null;
  readonly loading = signal(true);
  readonly error = signal<string | null>(null);
  readonly summary = signal<DashboardSummary | null>(null);
  distribution: SalaryDistributionBucket[] = [];
  countries: CompensationBreakdown[] = [];
  topDepartments: CompensationBreakdown[] = [];
  maxDepartmentCompensation = 1;
  maxDistributionEmployees = 1;
  readonly animatedMetrics = signal({
    activeEmployees: 0,
    currencyCount: 0,
    totalByCurrency: {} as Partial<Record<string, number>>,
    averageByCurrency: {} as Partial<Record<string, number>>,
  });

  ngOnInit(): void {
    this.destroyRef.onDestroy(() => {
      if (this.animationFrame !== null) cancelAnimationFrame(this.animationFrame);
    });
    this.load();
  }

  load(): void {
    this.loading.set(true);
    this.error.set(null);
    forkJoin({
      summary: this.api.summary(),
      departments: this.api.byDepartment(),
      countries: this.api.byCountry(),
      distribution: this.api.salaryDistribution(),
    }).subscribe({
      next: result => {
        this.summary.set(result.summary);
        this.topDepartments = result.departments;
        this.countries = result.countries;
        this.distribution = result.distribution;
        this.maxDepartmentCompensation = Math.max(
          ...this.topDepartments.map(item => item.totalCompensation),
          1,
        );
        this.maxDistributionEmployees = Math.max(...this.distribution.map(item => item.employeeCount), 1);
        this.animateMetrics(result.summary);
        this.loading.set(false);
      },
      error: () => {
        this.error.set('We could not load salary insights. Check the API connection and try again.');
        this.loading.set(false);
      },
    });
  }

  barWidth(value: number, max: number): number {
    return Math.round((value / max) * 100);
  }

  private animateMetrics(data: DashboardSummary): void {
    const target = {
      activeEmployees: data.activeEmployees,
      currencyCount: data.compensationByCurrency.length,
      totalByCurrency: Object.fromEntries(data.compensationByCurrency.map(item => [item.currency, item.totalCompensation])),
      averageByCurrency: Object.fromEntries(data.compensationByCurrency.map(item => [item.currency, item.averageBaseSalary])),
    };
    if (this.animationFrame !== null) cancelAnimationFrame(this.animationFrame);
    const reduced = window.matchMedia('(prefers-reduced-motion: reduce)').matches;
    if (reduced) { this.animatedMetrics.set(target); return; }
    const started = performance.now();
    const tick = (now: number) => {
      const progress = Math.min((now - started) / 700, 1);
      const eased = 1 - Math.pow(1 - progress, 3);
      this.animatedMetrics.set({ activeEmployees: Math.round(target.activeEmployees * eased), currencyCount: Math.round(target.currencyCount * eased), totalByCurrency: Object.fromEntries(Object.entries(target.totalByCurrency).map(([key, value]) => [key, Math.round(value * eased)])), averageByCurrency: Object.fromEntries(Object.entries(target.averageByCurrency).map(([key, value]) => [key, Math.round(value * eased)])) });
      if (progress < 1) this.animationFrame = requestAnimationFrame(tick);
    };
    this.animationFrame = requestAnimationFrame(tick);
  }
}
