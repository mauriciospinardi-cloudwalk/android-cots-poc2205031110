package io.cloudwalk.cots.poc2205031110;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.discover.mpos.sdk.card.connectors.ConnectorType;
import com.discover.mpos.sdk.cardreader.config.CombinationConfiguration;
import com.discover.mpos.sdk.cardreader.config.EntryPointConfigurationData;
import com.discover.mpos.sdk.cardreader.config.ReaderConfiguration;
import com.discover.mpos.sdk.cardreader.config.TerminalCapabilitiesQualifiers;
import com.discover.mpos.sdk.cardreader.config.TerminalConfiguration;
import com.discover.mpos.sdk.cardreader.config.TerminalTransactionQualifiers;
import com.discover.mpos.sdk.cardreader.config.TerminalType;
import com.discover.mpos.sdk.cardreader.config.TransactionTypeConfiguration;
import com.discover.mpos.sdk.config.MPosConfiguration;
import com.discover.mpos.sdk.core.DiscoverMPos;
import com.discover.mpos.sdk.core.data.Amount;
import com.discover.mpos.sdk.core.debug.logger.AndroidLogger;
import com.discover.mpos.sdk.core.debug.logger.Message;
import com.discover.mpos.sdk.core.debug.logger.MessageType;
import com.discover.mpos.sdk.data.external.TerminalCAPublicKey;
import com.discover.mpos.sdk.data.external.initiateapplicationprocessingconnect.ExtendedLoggingDataRequest;
import com.discover.mpos.sdk.data.external.initiateapplicationprocessingconnect.ExtendedLoggingDataResponse;
import com.discover.mpos.sdk.data.external.readdatarecord.DataStorageRequest;
import com.discover.mpos.sdk.data.external.readdatarecord.DataStorageResponse;
import com.discover.mpos.sdk.initialization.CustomInitializer;
import com.discover.mpos.sdk.module.CardReaderModule;
import com.discover.mpos.sdk.security.RandomNumberGenerator;
import com.discover.mpos.sdk.transaction.Transaction;
import com.discover.mpos.sdk.transaction.TransactionData;
import com.discover.mpos.sdk.transaction.TransactionHandler;
import com.discover.mpos.sdk.transaction.TransactionType;
import com.discover.mpos.sdk.transaction.outcome.TransactionOutcome;
import com.discover.mpos.sdk.transaction.outcome.UiRequest;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Currency;
import java.util.Date;
import java.util.List;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;

public class DiscoverUtility {
    private final static String
            TAG = DiscoverUtility.class.getSimpleName();

    private static MPosConfiguration _getConfiguration(TransactionType SPE_TRNTYPE, String SPE_APPTYPE, Currency SPE_TRNCURR, Amount SPE_AMOUNT) {
        Log.d(TAG, "_getConfiguration");

        String          T1_APPVERx    = "0001";
        String          T1_TRMCNTRY   = "0076";
        TerminalType    T1_TRMTYP     = TerminalType.ATTENDED_MERCHANT_ONLINE_ONLY;
        String          T1_MERCHID    = "000000000000000";
        String          T1_MCC        = "0000";
        String          T1_TRMID      = "00000000";

        Amount          T1_CTLSTRNLIM = new Amount(new BigDecimal(999999999L / 100), SPE_TRNCURR);
        Amount          T1_CTLSFLRLIM = new Amount(new BigDecimal(        0L / 100), SPE_TRNCURR);
        Amount          T1_CTLSCVMLIM = new Amount(new BigDecimal(    20000L / 100), SPE_TRNCURR);
        final boolean   T1_CTLSZEROAM = false;
        String          T1_CTLSMODE   = "06"; // 2022-04-22: naturally differs from ABECS specification

        TerminalCapabilitiesQualifiers
                        T1_TRMCAPAB   = new TerminalCapabilitiesQualifiers("20D0E8");

        /* 2022-04-22: 9F330320D0E8
         *
         * EMV Book 4 page 116
         *
         * (T) 9F33 Terminal Capabilities
         * (L) 03
         * (V) 20D0E8 // 00100000 11010000 11101000
         */

        TerminalTransactionQualifiers
                        TAG_9F66      = new TerminalTransactionQualifiers("35200000");

        /* 2022-04-25: 9F660435200000
         *
         * EMV Book C-6 page 109
         *
         * (T) 9F66 Terminal Transaction Qualifier
         * (L) 04
         * (V) 35200000 // 00110101 00100000 00000000 00000000
         */

        TerminalConfiguration terminal = new TerminalConfiguration(
                T1_TRMCNTRY,        T1_TRMCAPAB,
                T1_TRMTYP,          T1_TRMID,
                T1_APPVERx,         new ArrayList<>(0),
                null,               null,
                T1_MCC,             T1_MERCHID);

        List<CombinationConfiguration> candidateList = new ArrayList<>(0);

        // if (SPE_APPTYPE.contains("CREDIT")) {
            candidateList.add(
                    new CombinationConfiguration(
                            "A0000004942010",
                            T1_CTLSMODE,
                            new EntryPointConfigurationData(
                                    true,
                                    T1_CTLSZEROAM,
                                    T1_CTLSZEROAM,
                                    T1_CTLSTRNLIM,
                                    T1_CTLSFLRLIM,
                                    T1_CTLSCVMLIM,
                                    T1_CTLSFLRLIM,
                                    TAG_9F66,
                                    true
                            ),
                            false,
                            false,
                            false,
                            false,
                            new ArrayList<>(0)
                    )
            );
        // } else {
            candidateList.add(
                    new CombinationConfiguration(
                            "A0000004941010",
                            T1_CTLSMODE,
                            new EntryPointConfigurationData(
                                    true,
                                    T1_CTLSZEROAM,
                                    T1_CTLSZEROAM,
                                    T1_CTLSTRNLIM,
                                    T1_CTLSFLRLIM,
                                    T1_CTLSCVMLIM,
                                    T1_CTLSFLRLIM,
                                    TAG_9F66,
                                    true
                            ),
                            false,
                            false,
                            false,
                            false,
                            new ArrayList<>(0)
                    )
            );
        // }

        List<TransactionTypeConfiguration> operationList = new ArrayList<>(0);

        operationList.add(new TransactionTypeConfiguration(SPE_TRNTYPE, candidateList));

        return new MPosConfiguration(new ReaderConfiguration(terminal, T1_TRMID, operationList));
    }

    private static CustomInitializer _getCustomInitializer() {
        Log.d(TAG, "_getCustomInitializer");

        RandomNumberGenerator randomizer = () -> {
            Log.d(TAG, "RandomNumberGenerator::nextRandomValue");

            byte[] seed = new byte[8];

            try {
                SecureRandom.getInstanceStrong().nextBytes(seed);
            } catch (Exception exception) {
                Log.e(TAG, Log.getStackTraceString(exception));
            }

            return seed;
        };

        return new CustomInitializer(randomizer, null);
    }

    private static TransactionHandler _getTransactionHandler() {
        Log.d(TAG, "_getTransactionHandler");

        return new TransactionHandler() {
            @Override
            public void onComplete(@NonNull Transaction transaction, @NonNull TransactionOutcome transactionOutcome) {
                Log.d(TAG, "TransactionHandler::onComplete::transactionOutcome [" + transactionOutcome + "]");
            }

            @Override
            public void onUIRequest(@NonNull Transaction transaction, @NonNull UiRequest uiRequest) {
                Log.d(TAG, "TransactionHandler::onUIRequest::uiRequest [" + uiRequest + "]");
            }

            @Nullable
            @Override
            public TerminalCAPublicKey onCAPKeyRequest(@NonNull Transaction transaction, @NonNull String s, @NonNull String s1) {
                Log.d(TAG, "TransactionHandler::onCAPKeyRequest");

                return null;
            }

            @Override
            public void onDataStorageProcessingRequest(@NonNull DataStorageRequest dataStorageRequest, @NonNull Function1<? super DataStorageResponse, Unit> function1) {
                Log.d(TAG, "TransactionHandler::onDataStorageProcessingRequest");
            }

            @Override
            public void onExtendedLoggingDataProcessingRequest(@NonNull ExtendedLoggingDataRequest extendedLoggingDataRequest, @NonNull Function1<? super ExtendedLoggingDataResponse, Unit> function1) {
                Log.d(TAG, "TransactionHandler::onExtendedLoggingDataProcessingRequest");
            }
        };
    }

    private DiscoverUtility() {
        Log.d(TAG, "DiscoverUtility");

        /* Nothing to do */
    }

    public static void run() {
        Log.d(TAG, "run");

        TransactionType     SPE_TRNTYPE     = TransactionType.PURCHASE;
        String              SPE_APPTYPE     = "CREDIT";
        Currency            SPE_TRNCURR     = Currency.getInstance("BRL");
        Amount              SPE_AMOUNT      = new Amount(new BigDecimal(100L / 100), SPE_TRNCURR);
        Amount              SPE_CASHBACK    = new Amount(new BigDecimal(  0L / 100), SPE_TRNCURR);
        Date                SPE_TRNDATE     = Calendar.getInstance().getTime();

        AndroidLogger logger = new AndroidLogger();

        logger.setDebugEnabled(true);
        logger.setErrorEnabled(true);
        logger.setInfoEnabled (true);

        logger.show(new Message(MessageType.DEBUG, TAG, "run::logger [" + logger + "]", SPE_TRNDATE));

        DiscoverMPos.Companion.getDebugger().register(logger);

        MPosConfiguration   configuration   = _getConfiguration(SPE_TRNTYPE, SPE_APPTYPE, SPE_TRNCURR, SPE_AMOUNT);
        CardReaderModule    cardReader      = new CardReaderModule(configuration, Application.getInstance(), _getCustomInitializer());
        TransactionData     transaction     = new TransactionData(SPE_AMOUNT, SPE_TRNCURR, SPE_TRNTYPE, SPE_CASHBACK, ConnectorType.NFC, SPE_TRNDATE);

        DiscoverMPos        mobilePOS       = new DiscoverMPos();

        mobilePOS.clear();
        mobilePOS.init(cardReader);

        DiscoverMPos.Companion.getDebugger().setEnabled(true);

        cardReader.getCardReader().startTransaction(transaction, _getTransactionHandler());
    }
}
