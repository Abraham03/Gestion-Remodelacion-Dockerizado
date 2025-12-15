import { Injectable } from "@angular/core";
import { Store, StoreConfig } from "@datorama/akita";
import { DashboardState } from "../models/dashboard.model";

export function createInitialState(): DashboardState {
    return {
        summary: null,
        clientesSummary: null,
        availableYears: [],
        isLoading: false
    };
}

@Injectable({ providedIn: 'root' })
@StoreConfig({ 
    name: 'dashboard',
    resettable: true
})
export class DashboardStore extends Store<DashboardState> {
    constructor() {
        super(createInitialState());
    }
}