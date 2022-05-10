package io.cloudwalk.cots.poc2205031110.utilities;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.discover.mpos.sdk.data.external.TerminalCAPublicKey;
import com.discover.mpos.sdk.data.external.initiateapplicationprocessingconnect.ExtendedLoggingDataRequest;
import com.discover.mpos.sdk.data.external.initiateapplicationprocessingconnect.ExtendedLoggingDataResponse;
import com.discover.mpos.sdk.data.external.readdatarecord.DataStorageRequest;
import com.discover.mpos.sdk.data.external.readdatarecord.DataStorageResponse;
import com.discover.mpos.sdk.transaction.Transaction;
import com.discover.mpos.sdk.transaction.TransactionHandler;
import com.discover.mpos.sdk.transaction.outcome.TransactionOutcome;
import com.discover.mpos.sdk.transaction.outcome.UiRequest;

import java.util.concurrent.atomic.AtomicReference;

import io.cloudwalk.cots.poc2205031110.Kernel;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;

public class CallbackUtility {
    private static final String
            TAG = CallbackUtility.class.getSimpleName();

    private static final AtomicReference<TransactionHandler>
            sKernelCallback         = new AtomicReference<>(null);

    private static final AtomicReference<TransactionOutcome>
            sLastTransactionOutcome = new AtomicReference<>(null);

    private CallbackUtility() {
        Log.d(TAG, "CallbackUtility");

        /* Nothing to do */
    }

    public static TransactionHandler getKernelCallback() {
        Log.d(TAG, "getKernelCallback");

        TransactionHandler kernelCallback = sKernelCallback.get();

        if (kernelCallback != null) {
            return kernelCallback;
        }

        kernelCallback = new TransactionHandler() {
            @Override
            public void onComplete(@NonNull Transaction transaction, @NonNull TransactionOutcome transactionOutcome) {
                Log.d(TAG, "onComplete::transactionOutcome [" + transactionOutcome + "]");

                sLastTransactionOutcome.set(transactionOutcome);
            }

            @Override
            public void onUIRequest(@NonNull Transaction transaction, @NonNull UiRequest uiRequest) {
                Log.d(TAG, "onUIRequest::uiRequest [" + uiRequest + "]");

                UiRequest.MessageIdentifier message = uiRequest.getMessageIdentifier();

                if (message != null
                 && message == UiRequest.MessageIdentifier.PRESENT_CARD) {
                    sLastTransactionOutcome.set(null);
                }

                TransactionOutcome transactionOutcome = sLastTransactionOutcome.get();

                if (transactionOutcome != null) {
                    Kernel.finish();
                }
            }

            @Nullable
            @Override
            public TerminalCAPublicKey onCAPKeyRequest(@NonNull Transaction transaction, @NonNull String s, @NonNull String s1) {
                Log.d(TAG, "onCAPKeyRequest");

                return null;
            }

            @Override
            public void onDataStorageProcessingRequest(@NonNull DataStorageRequest dataStorageRequest, @NonNull Function1<? super DataStorageResponse, Unit> function1) {
                Log.d(TAG, "onDataStorageProcessingRequest");
            }

            @Override
            public void onExtendedLoggingDataProcessingRequest(@NonNull ExtendedLoggingDataRequest extendedLoggingDataRequest, @NonNull Function1<? super ExtendedLoggingDataResponse, Unit> function1) {
                Log.d(TAG, "onExtendedLoggingDataProcessingRequest");
            }
        };

        sKernelCallback.set(kernelCallback);

        return kernelCallback;
    }

    public static TransactionOutcome getLastTransactionOutcome() {
        Log.d(TAG, "getLastTransactionOutcome");

        return sLastTransactionOutcome.get();
    }
}
