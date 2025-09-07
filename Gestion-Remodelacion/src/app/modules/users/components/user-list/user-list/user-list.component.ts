import { AfterViewInit, ChangeDetectorRef, Component, OnInit, signal, ViewChild } from '@angular/core';
import { MatTableDataSource, MatTableModule } from '@angular/material/table';
import { MatPaginator, MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatSort, MatSortModule, Sort } from '@angular/material/sort';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { CommonModule, DatePipe } from '@angular/common';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { User } from '../../../../../core/models/user.model';
import { UserService } from '../../../services/user.service';
import { AuthService } from '../../../../../core/services/auth.service';
import { FormsModule } from '@angular/forms'; // ¡Importante para ngModel!
import { UserFormComponent } from '../../user-form/user-form/user-form.component';
import { MatTooltipModule } from '@angular/material/tooltip';

@Component({
  selector: 'app-user-list',
  standalone: true,
  imports: [
    CommonModule,
    MatTableModule,
    MatPaginatorModule,
    MatSortModule,
    MatDialogModule,
    MatInputModule,
    MatFormFieldModule,
    MatIconModule,
    MatTooltipModule,
    MatButtonModule,
    MatSnackBarModule,
    FormsModule 
  ],
  providers: [DatePipe],
  templateUrl: './user-list.component.html',
  styleUrls: ['./user-list.component.scss']
})
export class UserListComponent implements OnInit, AfterViewInit{
  // Propiedades de permisos basados en el plan
  canCreate = false;
  canEdit = false;
  canDelete = false;

  displayedColumns: string[] = ['username', 'enabled', 'roles', 'actions'];
  dataSource = new MatTableDataSource<User>();
  totalElements: number = 0;
  pageSize: number = 5;
  currentPage: number = 0;
  filterValue: string  = '';


  @ViewChild(MatPaginator) paginator!: MatPaginator;
  @ViewChild(MatSort) sort!: MatSort;

  constructor(
    private userService: UserService,
    private dialog: MatDialog,
    private snackBar: MatSnackBar,
    private cdr: ChangeDetectorRef,
    private authService: AuthService
  ) { }

  ngOnInit(): void {
    this.setPermissions();
  }
  ngAfterViewInit(){
    this.sort.sortChange.subscribe(() => this.paginator.pageIndex = 0);
    this.paginator.page.subscribe((event: PageEvent) => {
      this.currentPage = event.pageIndex;
      this.pageSize = event.pageSize;
      this.loadUsers();
    })
    this.loadUsers();
  }

  private setPermissions(): void {
    this.canCreate = this.authService.hasPermission('USER_CREATE');
    this.canEdit = this.authService.hasPermission('USER_EDIT');
    this.canDelete = this.authService.hasPermission('USER_DELETE');
  }

  loadUsers(): void {
    this.userService.getUsers(
      this.currentPage,
      this.pageSize,
      this.filterValue
    )
    .subscribe({
        next: (response) => {
            this.dataSource.data = response.content;
            this.totalElements = response.totalElements;
            if(this.paginator){
            this.paginator.length = response.totalElements;
            this.paginator.pageIndex = response.number;
            }
            this.cdr.detectChanges();
          
        },
        error: (error) => {
          console.error('Error al cargar empleados:', error);
          this.snackBar.open(
            'Error al cargar los empleados. Inténtalo de nuevo más tarde.',
            'Cerrar',
            { duration: 5000 }
          );
        },
      });
  }


  openUserForm(user?: User): void {
    const dialogRef = this.dialog.open(UserFormComponent, {
      width: '600px',
      data: user // Pass user object for editing, or undefined for creating
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.loadUsers(); // Refresh the list if changes were made
      }
    });
  }

    applyFilter(filterValue: String): void {
    this.filterValue = filterValue
      .trim()
      .toLowerCase();
    this.paginator.pageIndex = 0; // Resetear a la primera página al aplicar un nuevo filtro
    this.loadUsers();
  }

    applyFilterIfEmpty(filterValue: string): void {
    // Si el usuario ha borrado todo el texto del campo de búsqueda
    if (filterValue === '') {
      this.applyFilter(''); // Llama al filtro con un string vacío
    }
  }

  deleteUser(id: number): void {
    if (confirm('¿Estás seguro de que quieres eliminar este usuario?')) {
      this.userService.deleteUser(id).subscribe({
        next: () => {
          this.snackBar.open('Usuario eliminado correctamente.', 'Cerrar', { duration: 3000 });
          this.loadUsers();
        },
        error: (err) => {
          console.error('Error deleting user:', err);
          this.snackBar.open('Error al eliminar el usuario.', 'Cerrar', { duration: 3000 });
        }
      });
    }
  }
}