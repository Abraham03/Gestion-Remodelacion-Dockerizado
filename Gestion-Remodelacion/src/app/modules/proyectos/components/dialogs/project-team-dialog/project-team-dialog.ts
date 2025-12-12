import { CommonModule } from '@angular/common';
import { Component, Inject } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatListModule } from '@angular/material/list';
import { MatIconModule } from '@angular/material/icon';
import { MatTooltipModule } from '@angular/material/tooltip';
import { TranslateModule } from '@ngx-translate/core';

// Usamos una interfaz simple para lo que recibimos del backend
export interface TeamMember {
  id: number;
  nombre: string;
  modeloDePago: string;
}

@Component({
  selector: 'app-project-team-dialog',
  standalone: true,
  imports: [
    CommonModule, MatDialogModule, MatButtonModule, MatListModule,
    MatIconModule, MatTooltipModule, TranslateModule
  ],
  templateUrl: './project-team-dialog.component.html',
  styleUrls: ['./project-team-dialog.component.scss']
})
export class ProjectTeamDialogComponent {
  projectName: string;
  equipoAsignado: TeamMember[];

  constructor(
    public dialogRef: MatDialogRef<ProjectTeamDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: { projectName: string, equipoAsignado: TeamMember[] }
  ) {
    this.projectName = data.projectName;
    this.equipoAsignado = data.equipoAsignado || [];
  }

  onClose(): void {
    this.dialogRef.close();
  }
}