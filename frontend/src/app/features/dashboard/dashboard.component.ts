import { CommonModule } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { forkJoin } from 'rxjs';
import { CardModule } from 'primeng/card';
import { ProgressBarModule } from 'primeng/progressbar';
import { ProgressSpinnerModule } from 'primeng/progressspinner';
import { MessageModule } from 'primeng/message';
import { ButtonModule } from 'primeng/button';
import { ApiService } from '../../core/api.service';
import { CompensationBreakdown, DashboardSummary, SalaryDistributionBucket } from '../../core/models';

@Component({
  selector: 'app-dashboard',
  imports: [CommonModule, RouterLink, CardModule, ProgressBarModule, ProgressSpinnerModule, MessageModule, ButtonModule],
  template: `
    <header class="page-header"><div><p class="eyebrow">Overview</p><h1>Good morning, HR team</h1><p class="subtitle">A clear view of your organisation's current compensation picture.</p></div><a pButton routerLink="/employees" label="View employees" icon="pi pi-arrow-right" iconPos="right"></a></header>
    @if (loading()) { <div class="loading-panel"><p-progressSpinner ariaLabel="Loading dashboard" /><span>Loading salary insights…</span></div> }
    @else if (error()) { <p-message severity="error" [text]="error()!" /><a pButton class="retry-button" label="Try again" (click)="load()"></a> }
    @else if (summary(); as data) {
      <section class="metric-grid" aria-label="Key metrics">
        <p-card><div class="metric"><span class="metric-icon mint">◉</span><div><span class="metric-label">Active employees</span><strong>{{ data.activeEmployees | number }}</strong><small>of {{ data.totalEmployees | number }} total records</small></div></div></p-card>
        <p-card><div class="metric"><span class="metric-icon blue">$</span><div><span class="metric-label">Total compensation</span><strong>{{ totalCompensation(data) }}</strong><small>across stored currencies</small></div></div></p-card>
        <p-card><div class="metric"><span class="metric-icon orange">↗</span><div><span class="metric-label">Average base salary</span><strong>{{ averageSalary(data) }}</strong><small>active workforce</small></div></div></p-card>
        <p-card><div class="metric"><span class="metric-icon purple">◈</span><div><span class="metric-label">Currencies</span><strong>{{ data.compensationByCurrency.length }}</strong><small>local salary ledgers</small></div></div></p-card>
      </section>
      <section class="insight-grid">
        <p-card header="Compensation by department"><p class="card-note">Grouped by local currency; no exchange-rate conversion is applied.</p><div class="bars">@for (item of topDepartments; track item.label + item.currency) { <div class="bar-row"><div class="bar-label"><span>{{ item.label }}</span><small>{{ item.employeeCount }} people · {{ item.currency }}</small></div><div class="bar-track"><span [style.width.%]="barWidth(item.totalCompensation, maxDepartmentCompensation)"></span></div><strong>{{ item.totalCompensation | number:'1.0-0' }}</strong></div> } @empty { <p class="empty-copy">No department data yet.</p> }</div></p-card>
        <p-card header="Salary distribution by currency"><p class="card-note">Each currency has its own demo-data salary bands. Amounts are not compared or converted across currencies.</p><div class="distribution">@for (item of distribution; track item.currency + item.band) { <div class="distribution-row"><div><strong>{{ item.band }} {{ item.currency }}</strong><small>Stored {{ item.currency }} salary</small></div><span>{{ item.employeeCount }}</span></div> } @empty { <p class="empty-copy">No salary data yet.</p> }</div></p-card>
      </section>
      <section class="insight-grid lower-grid"><p-card header="Compensation by country"><div class="country-list">@for (item of countries; track item.label + item.currency) { <div class="country-row"><div class="country-name"><span class="country-dot"></span><span>{{ item.label }}</span><small>{{ item.currency }}</small></div><div class="country-value"><strong>{{ item.employeeCount }}</strong><small>{{ item.totalCompensation | number:'1.0-0' }} total</small></div></div> } @empty { <p class="empty-copy">No country data yet.</p> }</div></p-card><p-card header="Currency snapshot"><div class="country-list">@for (item of data.compensationByCurrency; track item.currency) { <div class="country-row"><div class="country-name"><span class="currency-badge">{{ item.currency }}</span><span>{{ item.currency }} ledger</span></div><div class="country-value"><strong>{{ item.averageBaseSalary | number:'1.0-0' }}</strong><small>{{ item.employeeCount }} active employees</small></div></div> }</div></p-card></section>
    }
  `,
  styles: [`.page-header{align-items:flex-start;display:flex;justify-content:space-between;margin-bottom:2rem}.eyebrow{color:#2f855a;font-size:.75rem;font-weight:800;letter-spacing:.1em;margin:0 0 .45rem;text-transform:uppercase}h1{color:#102a43;font-size:clamp(1.65rem,3vw,2.25rem);letter-spacing:-.04em;margin:0}.subtitle{color:#627d98;margin:.55rem 0 0}.metric-grid{display:grid;gap:1rem;grid-template-columns:repeat(4,1fr);margin-bottom:1.25rem}.metric{align-items:flex-start;display:flex;gap:.85rem}.metric-icon{align-items:center;border-radius:9px;display:inline-flex;font-size:1.1rem;height:38px;justify-content:center;width:38px}.mint{background:#e6fffa;color:#16846d}.blue{background:#e6f6ff;color:#146da5}.orange{background:#fff4e6;color:#c05621}.purple{background:#f3e8ff;color:#805ad5}.metric-label{color:#627d98;display:block;font-size:.76rem;font-weight:650;margin-bottom:.2rem}.metric strong{color:#102a43;display:block;font-size:1.45rem;line-height:1.25}.metric small{color:#829ab1;font-size:.73rem}.insight-grid{display:grid;gap:1.25rem;grid-template-columns:1.4fr 1fr;margin-bottom:1.25rem}.lower-grid{grid-template-columns:1fr 1fr}.card-note{color:#829ab1;font-size:.78rem;margin:-.6rem 0 1rem}.bars{display:grid;gap:1rem}.bar-row{align-items:center;display:grid;gap:.8rem;grid-template-columns:minmax(130px,1.2fr) 2fr auto}.bar-label{display:flex;flex-direction:column;font-size:.82rem;font-weight:650}.bar-label small,.country-name small{color:#829ab1;font-size:.7rem;font-weight:400;margin-top:.15rem}.bar-track{background:#edf2f7;border-radius:5px;height:8px;overflow:hidden}.bar-track span{background:#2cb67d;border-radius:5px;display:block;height:100%;min-width:3px}.bar-row>strong{color:#486581;font-size:.75rem;font-weight:650}.distribution,.country-list{display:grid;gap:.2rem}.distribution-row,.country-row{align-items:center;border-bottom:1px solid #f0f4f8;display:flex;justify-content:space-between;padding:.7rem 0}.distribution-row:last-child,.country-row:last-child{border-bottom:0}.distribution-row div{display:flex;flex-direction:column}.distribution-row strong{color:#334e68;font-size:.82rem}.distribution-row small{color:#829ab1;font-size:.7rem;margin-top:.15rem}.distribution-row>span{background:#e6fffa;border-radius:99px;color:#16846d;font-size:.75rem;font-weight:750;padding:.25rem .55rem}.country-name{align-items:center;display:flex;font-size:.82rem;gap:.5rem}.country-dot{background:#2cb67d;border-radius:50%;height:8px;width:8px}.currency-badge{background:#e6f6ff;border-radius:6px;color:#146da5;font-size:.68rem;font-weight:800;padding:.35rem}.country-value{align-items:flex-end;display:flex;flex-direction:column}.country-value strong{color:#334e68;font-size:.85rem}.country-value small{color:#829ab1;font-size:.7rem;margin-top:.15rem}.empty-copy{color:#829ab1;font-size:.85rem}.loading-panel{align-items:center;background:#fff;border-radius:12px;display:flex;flex-direction:column;gap:.8rem;justify-content:center;min-height:250px}.retry-button{display:inline-flex;margin-top:1rem}@media(max-width:1000px){.metric-grid{grid-template-columns:repeat(2,1fr)}}@media(max-width:720px){.page-header{align-items:stretch;flex-direction:column;gap:1rem}.insight-grid,.lower-grid{grid-template-columns:1fr}.bar-row{grid-template-columns:1fr auto}.bar-track{grid-column:1/-1;grid-row:2}.bar-row>strong{grid-column:2;grid-row:1}.metric-grid{grid-template-columns:1fr 1fr}}`]
})
export class DashboardComponent implements OnInit {
  private readonly api = inject(ApiService);
  readonly loading = signal(true);
  readonly error = signal<string | null>(null);
  readonly summary = signal<DashboardSummary | null>(null);
  distribution: SalaryDistributionBucket[] = [];
  countries: CompensationBreakdown[] = [];
  topDepartments: CompensationBreakdown[] = [];
  maxDepartmentCompensation = 1;

  ngOnInit(): void { this.load(); }
  load(): void { this.loading.set(true); this.error.set(null); forkJoin({ summary: this.api.summary(), departments: this.api.byDepartment(), countries: this.api.byCountry(), distribution: this.api.salaryDistribution() }).subscribe({ next: result => { this.summary.set(result.summary); this.topDepartments = result.departments; this.countries = result.countries; this.distribution = result.distribution; this.maxDepartmentCompensation = Math.max(...this.topDepartments.map(item => item.totalCompensation), 1); this.loading.set(false); }, error: () => { this.error.set('We could not load salary insights. Check the API connection and try again.'); this.loading.set(false); } }); }
  totalCompensation(summary: DashboardSummary): string { return summary.compensationByCurrency.map(item => `${item.currency} ${Math.round(item.totalCompensation).toLocaleString()}`).join(' · '); }
  averageSalary(summary: DashboardSummary): string { return summary.compensationByCurrency.map(item => `${item.currency} ${Math.round(item.averageBaseSalary).toLocaleString()}`).join(' · '); }
  barWidth(value: number, max: number): number { return Math.round((value / max) * 100); }
}
