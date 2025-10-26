import { AfterViewInit, ChangeDetectorRef, Component, OnInit, signal, ViewChild, inject, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatTableDataSource, MatTableModule } from '@angular/material/table';
import { MatPaginator, MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatSort, MatSortModule, Sort } from '@angular/material/sort';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatInputModule } from '@angular/material/input';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { FormsModule } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { MatTooltipModule } from '@angular/material/tooltip';

import { Role } from '../../../../../core/models/role.model';
import { AuthService } from '../../../../../core/services/auth.service';
import { RoleService } from '../../../services/role.service';
import { RoleFormComponent } from '../../role-form/role-form/role-form.component';
import { PermissionDialogComponent } from '../../dialogs/permission-dialog/permission-dialog.component';

import { RoleQuery } from '../../../state/roles.query';
import { AsyncPipe } from '@angular/common';
import { Observable, Subscription } from 'rxjs';

@Component({
  selector: 'app-role-list',
  standalone: true,
  imports: [
    CommonModule, MatTableModule, MatPaginatorModule, MatSortModule, MatDialogModule,
    MatInputModule, MatFormFieldModule, MatIconModule, MatButtonModule,
    MatSnackBarModule, FormsModule, TranslateModule, MatTooltipModule, AsyncPipe, MatProgressBarModule
  ],
  templateUrl: './role-list.component.html',
  styleUrls: ['./role-list.component.scss'],
})
export class RoleListComponent implements OnInit, AfterViewInit, OnDestroy {
  displayedColumns: string[] = ['name', 'description', 'permissions', 'actions'];

  @ViewChild(MatPaginator) paginator!: MatPaginator;
  @ViewChild(MatSort) sort!: MatSort;

  // Se inyecta el Query de roles
  private roleQuery = inject(RoleQuery);

  // Se define Observables para para los datos y el estado de carga
  roles$: Observable<Role[]> = this.roleQuery.selectAll();
  loading$: Observable<boolean> = this.roleQuery.selectLoading();

  // Observables para la paginacion (directos al HTML con async pipe)
  totalElements$: Observable<number> = this.roleQuery.selectTotalElements();
  pageSize$: Observable<number> = this.roleQuery.selectPageSize();
  currentPage$: Observable<number> = this.roleQuery.selectCurrentPage();


  currentPageLocal = signal(0);
  pageSizeLocal = signal(5);
  sortColumn = signal('name');
  sortDirection = signal('asc');
  searchTerm = signal('');

  private roleService = inject(RoleService);
  private dialog = inject(MatDialog);
  private authService = inject(AuthService);
  private snackBar = inject(MatSnackBar);
  private translate = inject(TranslateService);
  private cdr = inject(ChangeDetectorRef);

  // Subscripcion para actualizar paginator
  private paginatorSubscription: Subscription | null = null;

  ngOnInit(): void {}

  ngAfterViewInit(): void {
    this.loadRoles();

    // Suscribirse a los cambios del store para mantener sincronizado el paginador
    this.paginatorSubscription = this.roleQuery.selectPagination().subscribe(pagination => {
      if (pagination && this.paginator) {
        // Actualiza el estado visual del paginador
        this.paginator.length = pagination.totalElements;
        this.paginator.pageIndex = pagination.currentPage;
        this.paginator.pageSize = pagination.pageSize;
      }
    })
  }

  ngOnDestroy(): void {
      this.paginatorSubscription?.unsubscribe();
  }

  hasPermission(permission: string): boolean {
    return this.authService.hasPermission(permission);
  }

  loadRoles(): void {
    this.roleService.getRoles(this.currentPageLocal(), this.pageSizeLocal(), this.sortColumn(), this.sortDirection(), this.searchTerm())
      .subscribe();
  }

  onPageChange(event: PageEvent): void {
    this.currentPageLocal.set(event.pageIndex);
    this.pageSizeLocal.set(event.pageSize);
    this.loadRoles();
  }

  onSortChange(event: Sort): void {
    this.sortColumn.set(event.active);
    this.sortDirection.set(event.direction);
    this.loadRoles();
  }

  applyFilter(): void {
    this.currentPageLocal.set(0);
    this.loadRoles();
  }
  
  applyFilterIfEmpty(filterValue: string): void {
    if (filterValue === '') {
      this.searchTerm.set('');
      this.applyFilter();
    }
  }

  openRoleForm(role?: Role): void {
    const dialogRef = this.dialog.open(RoleFormComponent, {
      width: '600px',
      data: role,
    });
    dialogRef.afterClosed().subscribe((result) => {
      if (result) this.loadRoles();
    });
  }

  deleteRole(id: number): void {
    if (confirm(this.translate.instant('ROLES.CONFIRM_DELETE'))) {
      this.roleService.deleteRole(id).subscribe({
        next: () => {
          this.snackBar.open(this.translate.instant('ROLES.SUCCESSFULLY_DELETED'), this.translate.instant('GLOBAL.CLOSE'), { duration: 3000 });
          this.loadRoles();
        },
        error: (err: HttpErrorResponse) => {
          const errorKey = err.error?.message || 'error.unexpected';
          const translatedMessage = this.translate.instant(errorKey);
          this.snackBar.open(translatedMessage, this.translate.instant('GLOBAL.CLOSE'), { duration: 7000 });
        },
      });
    }
  }

  viewPermissions(role: Role): void {
    this.dialog.open(PermissionDialogComponent, {
      width: '400px',
      data: { roleName: role.name, permissions: role.permissions },
    });
  }
}