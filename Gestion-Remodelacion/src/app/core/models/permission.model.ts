export interface Permission {
  id: number;
  name: string;
  description: string;
  scope: 'SYSTEM' | 'TENANT';
}

export interface PermissionRequest {
  id?: number;
  name: string;
  description: string;
  scope: 'SYSTEM' | 'TENANT';
}