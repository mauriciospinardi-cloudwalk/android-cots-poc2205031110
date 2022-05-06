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

import io.cloudwalk.cots.poc2205031110.Kernel;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;

public class CallbackUtility {
    private static final String
            TAG = CallbackUtility.class.getSimpleName();

    private static final TransactionHandler
            sKernelCallback = _getTransactionHandler();

    private CallbackUtility() {
        Log.d(TAG, "CallbackUtility");

        /* Nothing to do */
    }

    private static TransactionHandler _getTransactionHandler() {
        Log.d(TAG, "_getTransactionHandler");

        return new TransactionHandler() {
            @Override
            public void onComplete(@NonNull Transaction transaction, @NonNull TransactionOutcome transactionOutcome) {
                Log.d(TAG, "onComplete::transactionOutcome [" + transactionOutcome + "]");

                Kernel.interrupt();
            }

            @Override
            public void onUIRequest(@NonNull Transaction transaction, @NonNull UiRequest uiRequest) {
                Log.d(TAG, "onUIRequest::uiRequest [" + uiRequest + "]");

                UiRequest.MessageIdentifier identifier = uiRequest.getMessageIdentifier();

                if (uiRequest.getStatus() != UiRequest.Status.PROCESSING_ERROR) {
                    if (identifier != null) {
                        switch (identifier) {
                            case PROCESSING_ERROR:
                            case INSERT_SWIPE_OR_TRY_ANOTHER_CARD:
                            case NOT_AUTHORISED:
                            case PLEASE_INSERT_CARD:
                            case PLEASE_INSERT_OR_SWIPE_CARD:
                            case PLEASE_PRESENT_ONE_CARD_ONLY:
                                /* Nothing to do */
                                break;

                            default:
                                return;
                        }
                    }
                }

                Kernel.interrupt();
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
    }

    public static TransactionHandler getKernelCallback() {
        Log.d(TAG, "getKernelCallback");

        return sKernelCallback;
    }
}
