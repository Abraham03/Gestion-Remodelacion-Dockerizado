import { AfterViewInit, ChangeDetectorRef, Component, OnInit, inject, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatTableDataSource, MatTableModule } from '@angular/material/table';
import { MatPaginator, MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatSort, MatSortModule } from '@angular/material/sort';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { FormsModule } from '@angular/forms';
import { MatTooltipModule } from '@angular/material/tooltip';
import { HttpErrorResponse } from '@angular/common/http';
import { TranslateModule, TranslateService } from '@ngx-translate/core';

import { User } from '../../../../../core/models/user.model';
import { UserService } from '../../../services/user.service';
import { AuthService } from '../../../../../core/services/auth.service';
import { UserFormComponent } from '../../user-form/user-form/user-form.component';

@Component({
  selector: 'app-user-list',
  standalone: true,
  imports: [
    CommonModule, MatTableModule, MatPaginatorModule, MatSortModule, MatDialogModule,
    MatInputModule, MatFormFieldModule, MatIconModule, MatTooltipModule,
    MatButtonModule, MatSnackBarModule, FormsModule, TranslateModule
  ],
  templateUrl: './user-list.component.html',
  styleUrls: ['./user-list.component.scss']
})
export class UserListComponent implements OnInit, AfterViewInit {
  canCreate = false;
  canEdit = false;
  canDelete = false;

  isSuperAdmin = false;

  displayedColumns: string[] = ['username', 'enabled', 'roles', 'actions'];
  dataSource = new MatTableDataSource<User>();
  totalElements = 0;
  pageSize = 5;
  currentPage = 0;
  filterValue = '';

  @ViewChild(MatPaginator) paginator!: MatPaginator;
  @ViewChild(MatSort) sort!: MatSort;

  private userService = inject(UserService);
  private dialog = inject(MatDialog);
  private snackBar = inject(MatSnackBar);
  private cdr = inject(ChangeDetectorRef);
  private authService = inject(AuthService);
  private translate = inject(TranslateService);

  ngOnInit(): void {
    this.setPermissions();
    // ✅ 2. SE VERIFICA EL ROL Y SE AJUSTAN LAS COLUMNAS
    this.isSuperAdmin = this.authService.isSuperAdmin;
    if (this.isSuperAdmin && !this.displayedColumns.includes('nombreEmpresa')) {
      // Inserta la columna de empresa después de la de roles
      this.displayedColumns.splice(3, 0, 'nombreEmpresa');
    }    
  }

  ngAfterViewInit() {
    this.paginator.page.subscribe((event: PageEvent) => {
      this.currentPage = event.pageIndex;
      this.pageSize = event.pageSize;
      this.loadUsers();
    });
    this.loadUsers();
  }

  private setPermissions(): void {
    this.canCreate = this.authService.hasPermission('USER_CREATE');
    this.canEdit = this.authService.hasPermission('USER_UPDATE');
    this.canDelete = this.authService.hasPermission('USER_DELETE');
  }

  loadUsers(): void {
    this.userService.getUsers(this.currentPage, this.pageSize, this.filterValue)
      .subscribe({
        next: (response) => {
          this.dataSource.data = response.content;
          this.totalElements = response.totalElements;
          if (this.paginator) {
            this.paginator.length = response.totalElements;
            this.paginator.pageIndex = response.number;
          }
          this.cdr.detectChanges();
        },
        error: (error) => {
          console.error('Error al cargar usuarios:', error);
          this.snackBar.open(this.translate.instant('USERS.ERROR_LOADING'), this.translate.instant('GLOBAL.CLOSE'), { duration: 5000 });
        },
      });
  }

  openUserForm(user?: User): void {
    const dialogRef = this.dialog.open(UserFormComponent, {
      width: '500px',
      data: user
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) this.loadUsers();
    });
  }

  applyFilter(filterValue: string): void {
    this.filterValue = filterValue.trim().toLowerCase();
    this.paginator.pageIndex = 0;
    this.currentPage = 0;
    this.loadUsers();
  }

  applyFilterIfEmpty(filterValue: string): void {
    if (filterValue === '') {
      this.applyFilter('');
    }
  }

  deleteUser(id: number): void {
    if (confirm(this.translate.instant('USERS.CONFIRM_DELETE'))) {
      this.userService.deleteUser(id).subscribe({
        next: () => {
          this.snackBar.open(this.translate.instant('USERS.SUCCESSFULLY_DELETED'), this.translate.instant('GLOBAL.CLOSE'), { duration: 3000 });
          this.loadUsers();
        },
        error: (err: HttpErrorResponse) => {
          const errorKey = err.error?.message || 'error.unexpected';
          const translatedMessage = this.translate.instant(errorKey);
          this.snackBar.open(translatedMessage, this.translate.instant('GLOBAL.CLOSE'), { duration: 7000 });
        }
      });
    }
  }
}