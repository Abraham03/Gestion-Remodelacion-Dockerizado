import { Component, OnInit, signal, ViewChild } from '@angular/core';
import { MatTableDataSource, MatTableModule } from '@angular/material/table';
import {
  MatPaginator,
  MatPaginatorModule,
  PageEvent,
} from '@angular/material/paginator';
import { MatSort, MatSortModule, Sort } from '@angular/material/sort';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { CommonModule } from '@angular/common';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { Role } from '../../../../../core/models/role.model';
import { AuthService } from '../../../../../core/services/auth.service';
import { RoleService } from '../../../services/role.service';
import { FormsModule } from '@angular/forms'; // ¡Importante para ngModel!
import { RoleFormComponent } from '../../role-form/role-form/role-form.component';
import { Permission } from '../../../../../core/models/permission.model';
import { PermissionDialogComponent } from '../../dialogs/permission-dialog/permission-dialog.component';
@Component({
  selector: 'app-role-list',
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
    MatButtonModule,
    MatSnackBarModule,
    FormsModule,
  ],
  templateUrl: './role-list.component.html',
  styleUrls: ['./role-list.component.scss'],
})
export class RoleListComponent implements OnInit {
  displayedColumns: string[] = [
    'name',
    'description',
    'permissions',
    'actions',
  ];
  dataSource = new MatTableDataSource<Role>();
  totalElements = signal(0);
  pageSize = signal(5);
  pageIndex = signal(0);
  sortColumn = signal('name');
  sortDirection = signal('asc');
  searchTerm = signal('');

  @ViewChild(MatPaginator) paginator!: MatPaginator;
  @ViewChild(MatSort) sort!: MatSort;

  constructor(
    private roleService: RoleService,
    private dialog: MatDialog,
    private authService: AuthService,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    this.loadRoles();
  }

  ngAfterViewInit(): void {
    this.dataSource.paginator = this.paginator;
    this.dataSource.sort = this.sort;
    this.dataSource.sort.sortChange.subscribe(() => (this.paginator.pageIndex = 0));
    this.sort.sortChange.subscribe(() => this.paginator.pageIndex = 0);
    this.totalElements.set(this.dataSource.data.length);
  }

  loadRoles(): void {
    this.roleService
      .getRoles(
        this.pageIndex(),
        this.pageSize(),
        this.sortColumn(),
        this.sortDirection(),
        this.searchTerm()
      )
      .subscribe({
        next: (data) => {
          // 'data' es del tipo Page<Role>
          this.dataSource.data = data.content;
          this.totalElements.set(data.totalElements);

          this.paginator.length = data.totalElements;
          this.paginator.pageIndex = data.number;
          this.paginator.pageSize = data.size;
        },
        error: (err) => {
          console.error('Error loading roles:', err);
          this.snackBar.open('Error al cargar roles.', 'Cerrar', {
            duration: 3000,
          });
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
    this.pageIndex.set(0); // Reset page to 0 when applying filter
    this.loadRoles();
  }

  applyFilterIfEmpty(filterValue: string): void {
    // Si el usuario ha borrado todo el texto del campo de búsqueda
    if (filterValue === '') {
      this.applyFilter(); // Llama al filtro con un string vacío
    }
  }

  openRoleForm(role?: Role): void {
    const dialogRef = this.dialog.open(RoleFormComponent, {
      width: '600px',
      data: role, // Pass role object for editing, or undefined for creating
    });

    dialogRef.afterClosed().subscribe((result) => {
      if (result) {
        this.loadRoles(); // Refresh the list if changes were made
      }
    });
  }

  deleteRole(id: number): void {
    if (confirm('¿Estás seguro de que quieres eliminar este rol?')) {
      this.roleService.deleteRole(id).subscribe({
        next: () => {
          this.snackBar.open('Rol eliminado correctamente.', 'Cerrar', {
            duration: 3000,
          });
          this.loadRoles();
        },
        error: (err) => {
          console.error('Error deleting role:', err);
          this.snackBar.open('Error al eliminar el rol.', 'Cerrar', {
            duration: 3000,
          });
        },
      });
    }
  }

  viewPermissions(role: Role): void {
    this.dialog.open(PermissionDialogComponent, {
      width: '400px', // Adjust size as needed
      data: { roleName: role.name, permissions: role.permissions },
    });
  }

  // Este método asume que authService.hasPermission existe. Lo veremos en AuthService.
  hasPermission(permission: string): boolean {
    return this.authService.hasPermission(permission);
  }
}
