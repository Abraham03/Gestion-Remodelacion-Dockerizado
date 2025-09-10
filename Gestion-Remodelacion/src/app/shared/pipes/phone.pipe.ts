// TypeScript file: phone.pipe.ts
import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'phone',
  standalone: true, // Importante para que sea 'standalone'
})
export class PhonePipe implements PipeTransform {
  transform(value: string | number | null | undefined): string {
    if (!value) {
      return '';
    }

    let cleanValue = String(value).replace(/\D/g, ''); // Elimina todo lo que no sea dígito

    // Si no tiene 10 dígitos, no se puede formatear, devolvemos como está
    if (cleanValue.length !== 10) {
      return String(value);
    }

    const areaCode = cleanValue.substring(0, 3);
    const middle = cleanValue.substring(3, 6);
    const last = cleanValue.substring(6, 10);

    return `(${areaCode}) ${middle}-${last}`;
  }
}