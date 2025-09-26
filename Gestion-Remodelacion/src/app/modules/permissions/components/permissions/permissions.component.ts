import { Component, OnInit, ViewChild, ChangeDetectorRef, inject } from '@angular/core';
import { MatTableDataSource, MatTableModule } from '@angular/material/table';
import { MatPaginator, MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatSort, MatSortModule } from '@angular/material/sort';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
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

@Component({
  selector: 'app-permissions',
  standalone: true,
  imports: [
    CommonModule, MatTableModule, MatPaginatorModule, MatSortModule,
    MatSnackBarModule, TranslateModule, MatDialogModule, MatButtonModule,
    MatIconModule, MatTooltipModule,
  ],
  templateUrl: './permissions.component.html',
  styleUrls: ['./permissions.component.scss']
})
export class PermissionsComponent implements OnInit {
  displayedColumns: string[] = ['name', 'description', 'scope', 'actions']; // <-- Añadir 'actions'
  dataSource = new MatTableDataSource<Permission>();
  totalElements = 0;
  pageSize = 10;
  currentPage = 0;

  canCreate = false;
  canEdit = false;
  canDelete = false;

  @ViewChild(MatPaginator) paginator!: MatPaginator;
  @ViewChild(MatSort) sort!: MatSort;

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
    this.permissionService.getPaginated(this.currentPage, this.pageSize).subscribe({
      next: (response) => {
        this.dataSource.data = response.content;
        this.totalElements = response.totalElements;
        if (this.paginator) {
          this.paginator.length = response.totalElements;
        }
        this.cdr.detectChanges();
      },
      error: (error: HttpErrorResponse) => {
        console.error('Error al cargar permisos:', error);
        this.snackBar.open(this.translate.instant('PERMISSIONS.ERROR_LOADING'), this.translate.instant('GLOBAL.CLOSE'), { duration: 5000 });
      }
    });
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
    this.currentPage = event.pageIndex;
    this.pageSize = event.pageSize;
    this.loadPermissions();
  }
}