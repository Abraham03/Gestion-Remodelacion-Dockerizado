// src/app/core/services/cache.service.ts
import { Injectable } from '@angular/core';
import { HttpResponse } from '@angular/common/http';

@Injectable({
  providedIn: 'root'
})
export class CacheService {
  private cache = new Map<string, { response: HttpResponse<any>; expiry: number }>();
  private defaultTtl = 300000; // 5 minutos por defecto

  get(key: string): HttpResponse<any> | null {
    const cached = this.cache.get(key);
    if (!cached) {
      return null;
    }

    const isExpired = Date.now() > cached.expiry;
    if (isExpired) {
      this.cache.delete(key);
      return null;
    }

    return cached.response;
  }

  set(key: string, response: HttpResponse<any>, ttl = this.defaultTtl): void {
    const expiry = Date.now() + ttl;
    this.cache.set(key, { response, expiry });
  }

  invalidate(key: string): void {
    this.cache.delete(key);
  }

  invalidateAll(): void {
    this.cache.clear();
  }

  invalidateStartingWith(prefix: string): void {
    this.cache.forEach((value, key) => {
      if (key.startsWith(prefix)) {
        this.cache.delete(key);
      }
    });
  }
}