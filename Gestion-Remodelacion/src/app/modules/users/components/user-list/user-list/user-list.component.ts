import { AfterViewInit, ChangeDetectorRef, Component, OnInit, inject, ViewChild, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatTableDataSource, MatTableModule } from '@angular/material/table';
import { MatPaginator, MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatSort, MatSortModule } from '@angular/material/sort';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatInputModule } from '@angular/material/input';
import { MatProgressBarModule } from '@angular/material/progress-bar';
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
import { InviteUserDialogComponent } from '../../invite-user-dialog/invite-user-dialog.component';

import { UserQuery } from '../../../state/users.query';
import { AsyncPipe } from '@angular/common';
import { Observable, Subscription } from 'rxjs';

@Component({
  selector: 'app-user-list',
  standalone: true,
  imports: [
    CommonModule, MatTableModule, MatPaginatorModule, MatSortModule, MatDialogModule,
    MatInputModule, MatFormFieldModule, MatIconModule, MatTooltipModule,
    MatButtonModule, MatSnackBarModule, FormsModule, TranslateModule, AsyncPipe, MatProgressBarModule
  ],
  templateUrl: './user-list.component.html',
  styleUrls: ['./user-list.component.scss']
})
export class UserListComponent implements OnInit, AfterViewInit, OnDestroy {
  canCreate = false;
  canEdit = false;
  canDelete = false;

  isSuperAdmin = false;

  displayedColumns: string[] = ['username', 'enabled', 'roles', 'actions'];

  // Se inyecta el Query de users
  private userQuery = inject(UserQuery);

  // Se define Observables para los datos y el estado de carga
  users$: Observable<User[]> = this.userQuery.selectAll();
  loading$: Observable<boolean> = this.userQuery.selectLoading();

  // Observables para la paginacion (directos al HTML con async pipe)
  totalElements$: Observable<number> = this.userQuery.selectTotalElements();
  pageSize$: Observable<number> = this.userQuery.selectPageSize();
  currentPage$: Observable<number> = this.userQuery.selectCurrentPage();

  pageSizeLocal = 5;
  currentPageLocal = 0;
  filterValue = '';

  @ViewChild(MatPaginator) paginator!: MatPaginator;
  @ViewChild(MatSort) sort!: MatSort;

  private userService = inject(UserService);
  private dialog = inject(MatDialog);
  private snackBar = inject(MatSnackBar);
  private cdr = inject(ChangeDetectorRef);
  private authService = inject(AuthService);
  private translate = inject(TranslateService);

  // Subscripcion para actualizar paginacion
  private paginatorSubscription: Subscription | null = null;

  ngOnInit(): void {
    this.setPermissions();
    // SE VERIFICA EL ROL Y SE AJUSTAN LAS COLUMNAS
    this.isSuperAdmin = this.authService.isSuperAdmin;
    if (this.isSuperAdmin && !this.displayedColumns.includes('nombreEmpresa')) {
      // Inserta la columna de empresa después de la de roles
      this.displayedColumns.splice(3, 0, 'nombreEmpresa');
    }    
  }

  ngAfterViewInit() {
    this.loadUsers();

    // Suscribirse a los cambios del store para mantener sincronizado el paginador
    this.paginatorSubscription = this.userQuery.selectPagination().subscribe(pagination => {
      if (pagination && this.paginator) {
        // Actualiza el estado visual del paginador
        this.paginator.length = pagination.totalElements;
        this.paginator.pageIndex = pagination.currentPage;
        this.paginator.pageSize = pagination.pageSize;
      }
    })
  }

  ngOnDestroy() {
      this.paginatorSubscription?.unsubscribe();
  }

  private setPermissions(): void {
    this.canCreate = this.authService.hasPermission('USER_CREATE');
    this.canEdit = this.authService.hasPermission('USER_UPDATE');
    this.canDelete = this.authService.hasPermission('USER_DELETE');
  }

  loadUsers(): void {
    this.userService.getUsers(this.currentPageLocal, this.pageSizeLocal, this.filterValue)
      .subscribe();
  }

  // Agrega el nuevo método para abrir el diálogo de invitación
openInviteDialog(): void {
  const dialogRef = this.dialog.open(InviteUserDialogComponent, {
    width: '400px',
    disableClose: true 
  });

  dialogRef.afterClosed().subscribe(result => {
    // El diálogo devuelve 'true' si la invitación fue exitosa.
    if (result) {
      // Opcional: No es necesario recargar la lista de usuarios, ya que el nuevo usuario aún no se ha registrado.
      // this.loadUsers();
    }
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
    this.currentPageLocal = 0;
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