import { CommonModule } from '@angular/common';
import { Component, Inject } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatListModule } from '@angular/material/list';
import { MatIconModule } from '@angular/material/icon';
import { MatTooltipModule } from '@angular/material/tooltip';
import { TranslateModule } from '@ngx-translate/core';

import { Permission } from '../../../../../core/models/permission.model';

@Component({
  selector: 'app-permission-dialog',
  standalone: true,
  imports: [
    CommonModule, MatDialogModule, MatButtonModule, MatListModule,
    MatIconModule, MatTooltipModule, TranslateModule
  ],
  templateUrl: './permission-dialog.component.html',
  styleUrl: './permission-dialog.component.scss'
})
export class PermissionDialogComponent {
  roleName: string;
  permissions: Permission[];

  constructor(
    public dialogRef: MatDialogRef<PermissionDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: { roleName: string, permissions: Permission[] }
  ) {
    this.roleName = data.roleName;
    this.permissions = data.permissions;
  }

  onClose(): void {
    this.dialogRef.close();
  }
}