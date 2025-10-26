import { Component, OnInit, ViewChild, ChangeDetectorRef, inject } from '@angular/core';
import { MatTableDataSource, MatTableModule } from '@angular/material/table';
import { MatPaginator, MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatSort, MatSortModule } from '@angular/material/sort';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { CommonModule } from '@angular/common';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { Permission } from '../../../../core/models/permission.model';
import { PermissionService } from '../../services/permission.service';
import { HttpErrorResponse } from '@angular/common/http';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { AuthService } from '../../../../core/services/auth.service';
import { PermissionFormComponent } from '../permission-form-component/permission-form-component.component';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatTooltipModule } from '@angular/material/tooltip';
import { PermissionQuery } from '../../state/permission.query';
import { AsyncPipe } from '@angular/common';
import { Observable, Subscription } from 'rxjs';

@Component({
  selector: 'app-permissions',
  standalone: true,
  imports: [
    CommonModule, MatTableModule, MatPaginatorModule, MatSortModule,
    MatSnackBarModule, TranslateModule, MatDialogModule, MatButtonModule,
    MatIconModule, MatTooltipModule, AsyncPipe, MatProgressBarModule
  ],
  templateUrl: './permissions.component.html',
  styleUrls: ['./permissions.component.scss']
})
export class PermissionsComponent implements OnInit {
  displayedColumns: string[] = ['name', 'description', 'scope', 'actions']; // <-- Añadir 'actions'
  totalElements = 0;
  pageSizeLocal = 5;
  currentPageLocal = 0;
  sortColumn = 'name';

  canCreate = false;
  canEdit = false;
  canDelete = false;

  // Se inyecta el Query de permissions
  private permissionQuery = inject(PermissionQuery);

  // Se define Observables para los datos y el estado de carga
  permissions$: Observable<Permission[]> = this.permissionQuery.selectAll();
  loading$: Observable<boolean> = this.permissionQuery.selectLoading();

  @ViewChild(MatPaginator) paginator!: MatPaginator;
  @ViewChild(MatSort) sort!: MatSort;

  // Observables para la paginacion (directos al HTML con async pipe)
  totalElements$: Observable<number> = this.permissionQuery.selectTotalElements();
  pageSize$: Observable<number> = this.permissionQuery.selectPageSize();
  currentPage$: Observable<number> = this.permissionQuery.selectCurrentPage();

  private permissionService = inject(PermissionService);
  private snackBar = inject(MatSnackBar);
  private translate = inject(TranslateService);
  private cdr = inject(ChangeDetectorRef);
  private dialog = inject(MatDialog);
  private authService = inject(AuthService);

  ngOnInit(): void {
    this.setPermissions();
    this.loadPermissions();
  }

  private setPermissions(): void {
    this.canCreate = this.authService.hasPermission('PERMISSION_CREATE');
    this.canEdit = this.authService.hasPermission('PERMISSION_UPDATE');
    this.canDelete = this.authService.hasPermission('PERMISSION_DELETE');
  }

  loadPermissions(): void {
    this.permissionService.getPaginated(this.currentPageLocal, this.pageSizeLocal).subscribe();
  }

  openPermissionForm(permission?: Permission): void {
    const dialogRef = this.dialog.open(PermissionFormComponent, {
      width: '500px',
      data: permission
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.loadPermissions();
      }
    });
  }

  deletePermission(id: number): void {
    const confirmMessage = this.translate.instant('GLOBAL.CONFIRM_DELETE');
    if (confirm(confirmMessage)) {
      this.permissionService.deletePermission(id).subscribe({
        next: () => {
          this.snackBar.open(this.translate.instant('GLOBAL.SUCCESSFULLY_DELETED'), this.translate.instant('GLOBAL.CLOSE'), { duration: 3000 });
          this.loadPermissions();
        },
        error: (err: HttpErrorResponse) => {
          const errorKey = err.error?.message || 'error.unexpected';
          const translatedMessage = this.translate.instant(errorKey);
          this.snackBar.open(translatedMessage, this.translate.instant('GLOBAL.CLOSE'), { duration: 7000 });
        }
      });
    }
  }

  onPageChange(event: PageEvent): void {
    this.currentPageLocal = event.pageIndex;
    this.pageSizeLocal = event.pageSize;
    this.loadPermissions();
  }
}