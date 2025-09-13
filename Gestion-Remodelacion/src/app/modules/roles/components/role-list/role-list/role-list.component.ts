import { AfterViewInit, ChangeDetectorRef, Component, OnInit, signal, ViewChild, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatTableDataSource, MatTableModule } from '@angular/material/table';
import { MatPaginator, MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatSort, MatSortModule, Sort } from '@angular/material/sort';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatInputModule } from '@angular/material/input';
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

@Component({
  selector: 'app-role-list',
  standalone: true,
  imports: [
    CommonModule, MatTableModule, MatPaginatorModule, MatSortModule, MatDialogModule,
    MatInputModule, MatFormFieldModule, MatIconModule, MatButtonModule,
    MatSnackBarModule, FormsModule, TranslateModule, MatTooltipModule
  ],
  templateUrl: './role-list.component.html',
  styleUrls: ['./role-list.component.scss'],
})
export class RoleListComponent implements OnInit, AfterViewInit {
  displayedColumns: string[] = ['name', 'description', 'permissions', 'actions'];
  dataSource = new MatTableDataSource<Role>();
  totalElements = signal(0);
  pageSize = signal(5);
  pageIndex = signal(0);
  sortColumn = signal('name');
  sortDirection = signal('asc');
  searchTerm = signal('');

  @ViewChild(MatPaginator) paginator!: MatPaginator;
  @ViewChild(MatSort) sort!: MatSort;

  private roleService = inject(RoleService);
  private dialog = inject(MatDialog);
  private authService = inject(AuthService);
  private snackBar = inject(MatSnackBar);
  private translate = inject(TranslateService);
  private cdr = inject(ChangeDetectorRef);

  ngOnInit(): void {}

  ngAfterViewInit(): void {
    this.dataSource.sort = this.sort;
    this.loadRoles();
  }

  hasPermission(permission: string): boolean {
    return this.authService.hasPermission(permission);
  }

  loadRoles(): void {
    this.roleService.getRoles(this.pageIndex(), this.pageSize(), this.sortColumn(), this.sortDirection(), this.searchTerm())
      .subscribe({
        next: (data) => {
          this.dataSource.data = data.content;
          this.totalElements.set(data.totalElements);
          this.pageIndex.set(data.number);
          this.pageSize.set(data.size);
          
          if (this.paginator) {
            this.paginator.length = data.totalElements;
            this.paginator.pageIndex = data.number;
          }
          this.cdr.detectChanges();
        },
        error: (err) => {
          console.error('Error loading roles:', err);
          this.snackBar.open(this.translate.instant('ROLES.ERROR_LOADING'), this.translate.instant('GLOBAL.CLOSE'), { duration: 3000 });
        },
      });
  }

  onPageChange(event: PageEvent): void {
    this.pageIndex.set(event.pageIndex);
    this.pageSize.set(event.pageSize);
    this.loadRoles();
  }

  onSortChange(event: Sort): void {
    this.sortColumn.set(event.active);
    this.sortDirection.set(event.direction);
    this.loadRoles();
  }

  applyFilter(): void {
    this.pageIndex.set(0);
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