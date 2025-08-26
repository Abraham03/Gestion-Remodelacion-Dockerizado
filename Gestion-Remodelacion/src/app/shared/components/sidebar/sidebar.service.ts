// src/app/shared/components/sidebar/sidebar.service.ts
import { Injectable, inject, OnDestroy } from '@angular/core';
import { BehaviorSubject, Subscription } from 'rxjs';
import { BreakpointObserver, Breakpoints } from '@angular/cdk/layout';

@Injectable({
  providedIn: 'root'
})
export class SidebarService implements OnDestroy {
  // Using BehaviorSubject to hold the current state and emit updates
  public isSidebarOpenSubject = new BehaviorSubject<boolean>(true); // Default to open for desktop
  // Expose as an Observable for components to subscribe to
  public isSidebarOpen$ = this.isSidebarOpenSubject.asObservable();

  private breakpointObserver = inject(BreakpointObserver);
  private subscriptions = new Subscription();

  constructor() {
    this.subscriptions.add(
      this.breakpointObserver.observe(Breakpoints.Handset)
        .subscribe(result => {
          if (result.matches) {
            // On handset, default to closed
            this.closeSidebar();
          } else {
            // On desktop, default to open
            this.openSidebar();
          }
        })
    );
  }

  toggleSidebar() {
    this.isSidebarOpenSubject.next(!this.isSidebarOpenSubject.value);
  }

  openSidebar() {
    this.isSidebarOpenSubject.next(true);
  }

  closeSidebar() {
    this.isSidebarOpenSubject.next(false);
  }

  ngOnDestroy(): void {
    this.subscriptions.unsubscribe();
  }
}