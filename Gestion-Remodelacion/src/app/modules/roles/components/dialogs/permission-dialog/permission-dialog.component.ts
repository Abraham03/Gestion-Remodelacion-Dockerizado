import { CommonModule } from '@angular/common';
import { Component, Inject, OnInit } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialog, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatListModule } from '@angular/material/list';
import { Permission } from '../../../../../core/models/permission.model';
import { MatIconModule } from '@angular/material/icon';
import { MatTooltipModule } from '@angular/material/tooltip';

@Component({
  selector: 'app-permission-dialog',
  standalone: true,
  imports: [
    CommonModule,
    MatDialogModule,
    MatButtonModule,
    MatListModule,
    MatIconModule,
    MatTooltipModule
  ],
  templateUrl: './permission-dialog.component.html',
  styleUrl: './permission-dialog.component.scss'
})
export class PermissionDialogComponent implements OnInit  {

    roleName: string;
  permissions: Permission[]; // Assuming PermissionResponse is the DTO type for permissions

  constructor(
    public dialogRef: MatDialogRef<PermissionDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: { roleName: string, permissions: Permission[] }
  ) {
    this.roleName = data.roleName;
    this.permissions = data.permissions;
  }

  ngOnInit(): void {
  }

  onClose(): void {
    this.dialogRef.close();
  }

}
