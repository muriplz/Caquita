package com.kryeit.auth.currency;

import com.kryeit.Database;
import com.kryeit.sync.SyncManager;

public class CurrencySyncProvider implements SyncManager.SyncDataProvider {

    private final CurrencyService currencyService;

    public CurrencySyncProvider(CurrencyService currencyService) {
        this.currencyService = currencyService;
    }

    @Override
    public Object getInitialData(Long userId) {
        try {
            return currencyService.getCurrencies(userId);
        } catch (Exception e) {
            return null;
        }
    }
}