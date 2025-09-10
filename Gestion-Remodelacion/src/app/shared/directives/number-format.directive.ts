// TypeScript file: number-format.directive.ts
import { Directive, ElementRef, HostListener, forwardRef } from '@angular/core';
import { ControlValueAccessor, NG_VALUE_ACCESSOR } from '@angular/forms';

@Directive({
  selector: 'input[appNumberFormat]', // Así la usaremos: <input appNumberFormat>
  standalone: true,
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      useExisting: forwardRef(() => NumberFormatDirective),
      multi: true,
    },
  ],
})
export class NumberFormatDirective implements ControlValueAccessor {
  private _onChange: (val: string | null) => void = () => {};
  private _onTouched: () => void = () => {};

  constructor(private el: ElementRef<HTMLInputElement>) {}

  // Se activa cuando el valor del formulario cambia
  writeValue(value: any): void {
    this.formatValue(value);
  }

  registerOnChange(fn: any): void {
    this._onChange = fn;
  }

  registerOnTouched(fn: any): void {
    this._onTouched = fn;
  }

  // Escucha el evento 'input' mientras el usuario escribe
  @HostListener('input', ['$event.target.value'])
  onInput(value: string) {
    // 1. Limpiamos el valor de todo lo que no sea un número (para obtener el valor real)
    const cleanValue = value.replace(/[^0-9.]/g, ''); 
    
    // 2. Formateamos el valor limpio para mostrarlo en el input
    this.formatValue(cleanValue);

    // 3. Enviamos el valor LIMPIO (sin comas) de vuelta al FormControl de Angular
    this._onChange(cleanValue);
  }

  @HostListener('blur')
  onBlur() {
    this._onTouched();
  }

  private formatValue(value: string | number | null): void {
    if (value !== null && value !== undefined) {
      const [integerPart, decimalPart] = String(value).split('.');
      // Usamos una expresión regular para añadir comas a los miles
      const formattedInteger = integerPart.replace(/\B(?=(\d{3})+(?!\d))/g, ',');
      // Unimos la parte entera formateada con la parte decimal (si existe)
      this.el.nativeElement.value = decimalPart !== undefined ? `${formattedInteger}.${decimalPart}` : formattedInteger;
    } else {
      this.el.nativeElement.value = '';
    }
  }
}