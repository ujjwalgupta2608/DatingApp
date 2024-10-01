package com.ripenapps.adoreandroid.utils;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.widget.Toast;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;

import java.util.List;

public class BillingService extends Service implements PurchasesUpdatedListener {

    private BillingClient billingClient;

    @Override
    public void onCreate() {
        super.onCreate();
        // Initialize the billing client
        billingClient = BillingClient.newBuilder(this).setListener(this).build();
        // Start connection to the billing client
        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(BillingResult billingResult) {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    // Billing client is ready
                }
            }

            @Override
            public void onBillingServiceDisconnected() {
                // Try to restart the connection to the billing client
                billingClient.startConnection(this);
            }
        });
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onPurchasesUpdated(BillingResult billingResult, List<Purchase> purchases) {
        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && purchases != null) {
            for (Purchase purchase : purchases) {
                handlePurchase(purchase);
            }
        }
    }

    private void handlePurchase(Purchase purchase) {
        if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED &&
               /* purchase.getPurchaseType() == Purchase.PurchaseType.SUBS &&*/
                purchase.isAutoRenewing()) {
            // Subscription renewed, handle it accordingly
//            String sku = purchase.getSku();
            String orderId = purchase.getOrderId();
            // Show notification, update database, etc.
            Toast.makeText(this, "Subscription renewed", Toast.LENGTH_SHORT).show();
        }
    }
}
