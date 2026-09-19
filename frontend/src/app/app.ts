import { Component } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { ButtonModule } from 'primeng/button';
import { ToastModule } from 'primeng/toast';
import { ConfirmDialogModule } from 'primeng/confirmdialog';
import { MessageService, ConfirmationService } from 'primeng/api';

@Component({
  selector: 'app-root',
  imports: [RouterLink, RouterLinkActive, RouterOutlet, ButtonModule, ToastModule, ConfirmDialogModule],
  providers: [MessageService, ConfirmationService],
  template: `
    <p-toast />
    <p-confirmDialog />
    <div class="shell">
      <aside class="sidebar">
        <a class="brand" routerLink="/dashboard" aria-label="Salary Management home">
          <span class="brand-mark">S</span><span>Salary<span class="brand-accent">Flow</span></span>
        </a>
        <p class="workspace-label">HR workspace</p>
        <nav aria-label="Primary navigation">
          <a routerLink="/dashboard" routerLinkActive="active"><span>▦</span>Dashboard</a>
          <a routerLink="/employees" routerLinkActive="active"><span>◎</span>Employees</a>
        </nav>
        <div class="sidebar-note"><span>10k-ready</span><p>Server-side data views keep the HR workspace fast.</p></div>
      </aside>
      <main class="content"><router-outlet /></main>
    </div>
  `,
  styleUrl: './app.scss'
})
export class App {}
