export interface Permission {
  id: number;
  name: string;
  description: string;
  scope: 'PLATFORM' | 'TENANT';
}

export interface PermissionRequest {
  id?: number;
  name: string;
  description: string;
  scope: 'PLATFORM' | 'TENANT';
}

export interface PermissionDropdownResponse {
  id: number;
  name: string;
  description: string;
}