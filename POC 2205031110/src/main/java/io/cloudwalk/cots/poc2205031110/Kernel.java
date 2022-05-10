package io.cloudwalk.cots.poc2205031110;

import android.util.Log;

import com.discover.mpos.sdk.card.connectors.ConnectorType;
import com.discover.mpos.sdk.cardreader.CardReader;
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
import com.discover.mpos.sdk.initialization.CustomInitializer;
import com.discover.mpos.sdk.module.CardReaderModule;
import com.discover.mpos.sdk.security.RandomNumberGenerator;
import com.discover.mpos.sdk.transaction.Transaction;
import com.discover.mpos.sdk.transaction.TransactionData;
import com.discover.mpos.sdk.transaction.TransactionType;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Currency;
import java.util.Date;
import java.util.List;
// import java.util.concurrent.Semaphore;
// import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import io.cloudwalk.cots.poc2205031110.utilities.CallbackUtility;

public class Kernel {
    private static final String
            TAG = Kernel.class.getSimpleName();

    private static final AtomicReference<MPosConfiguration>
            sConfiguration      = new AtomicReference<>(null);

    private static final AtomicReference<CustomInitializer>
            sCustomInitializer  = new AtomicReference<>(null);

    private static final AtomicReference<DiscoverMPos>
            sMobilePOS          = new AtomicReference<>(null);

    private static final AtomicReference<Transaction>
            sTransaction        = new AtomicReference<>(null);

    private static final AtomicReference<TransactionData>
            sTransactionData    = new AtomicReference<>(null);

    private static final AtomicReference<CardReaderModule>
            sReaderModule       = new AtomicReference<>(null);

    // private static final Semaphore
    //         sSemaphore = new Semaphore(1, true);

    private static void _finish() {
        // Log.d(TAG, "_finish");

        DiscoverMPos mobilePOS = sMobilePOS.get();

        if (mobilePOS != null) {
            mobilePOS.clear();
        }

        sConfiguration      .set(null);
        sCustomInitializer  .set(null);
        sMobilePOS          .set(null);
        sTransaction        .set(null);
        sTransactionData    .set(null);
        sReaderModule       .set(null);

        // try {
        //     sSemaphore.tryAcquire(0, TimeUnit.MILLISECONDS);
        // } catch (InterruptedException exception) {
        //     Log.e(TAG, Log.getStackTraceString(exception));
        // } finally {
        //     sSemaphore.release();
        // }
    }

    private static MPosConfiguration _getConfiguration(TransactionType SPE_TRNTYPE, String SPE_APPTYPE, Currency SPE_TRNCURR, Amount SPE_AMOUNT) {
        // Log.d(TAG, "_getConfiguration");

        MPosConfiguration configuration = sConfiguration.get();

        if (configuration != null) {
            return configuration;
        }

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
                        T1_TRMCAPAB   = new TerminalCapabilitiesQualifiers("00D0E8");

        /* 2022-04-22: 9F33 03 00D0E8
         *
         * EMV Book 4 page 116
         *
         * (T) 9F33 Terminal Capabilities
         * (L) 03
         * (V) 00D0E8   // ........ 00000000 11010000 11101000
         */

        TerminalTransactionQualifiers
                        TAG_9F66      = new TerminalTransactionQualifiers("25000000");

        /* 2022-04-25: 9F66 04 25000000
         *
         * EMV Book C-6 page 109
         *
         * (T) 9F66 Terminal Transaction Qualifier
         * (L) 04
         * (V) 25000000 // 00100101 00000000 00000000 00000000
         */

        TerminalConfiguration terminal = new TerminalConfiguration(
                T1_TRMCNTRY,        T1_TRMCAPAB,
                T1_TRMTYP,          T1_TRMID,
                T1_APPVERx,         new ArrayList<>(0),
                null,               null,
                T1_MCC,             T1_MERCHID);

        List<CombinationConfiguration> candidateList = new ArrayList<>(0);

        if (SPE_APPTYPE.contains("CREDIT")) {
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
                            false,  false,
                            false,  false,  new ArrayList<>(0)
                    )
            );
        } else {
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
                            false,  false,
                            false,  false,  new ArrayList<>(0)
                    )
            );
        }

        List<TransactionTypeConfiguration> operationList = new ArrayList<>(0);

        operationList.add(new TransactionTypeConfiguration(SPE_TRNTYPE, candidateList));

        configuration = new MPosConfiguration(new ReaderConfiguration(terminal, T1_TRMID, operationList));

        sConfiguration.set(configuration);

        return configuration;
    }

    private static CustomInitializer _getCustomInitializer() {
        // Log.d(TAG, "_getCustomInitializer");

        CustomInitializer initializer = sCustomInitializer.get();

        if (initializer != null) {
            return initializer;
        }

        RandomNumberGenerator randomizer = () -> {
            Log.d("RandomNumberGenerator", "nextRandomValue");

            byte[] seed = new byte[8];

            try {
                SecureRandom.getInstanceStrong().nextBytes(seed);
            } catch (Exception exception) {
                Log.e(TAG, Log.getStackTraceString(exception));
            }

            return seed;
        };

        initializer = new CustomInitializer(randomizer, null);

        sCustomInitializer.set(initializer);

        return initializer;
    }

    private static TransactionData _getTransactionData(Amount SPE_AMOUNT, Currency SPE_TRNCURR, TransactionType SPE_TRNTYPE, Amount SPE_CASHBACK, Date SPE_TRNDATE) {
        // Log.d(TAG, "_getTransactionData");

        TransactionData transactionData = sTransactionData.get();

        if (transactionData != null) {
            return transactionData;
        }

        transactionData = new TransactionData(SPE_AMOUNT, SPE_TRNCURR, SPE_TRNTYPE, SPE_CASHBACK, ConnectorType.NFC, SPE_TRNDATE);

        sTransactionData.set(transactionData);

        return transactionData;
    }

    private static CardReaderModule _getReaderModule(MPosConfiguration configuration) {
        // Log.d(TAG, "_getReaderModule");

        CardReaderModule readerModule = sReaderModule.get();

        if (readerModule != null) {
            return readerModule;
        }

        readerModule = new CardReaderModule(configuration, Application.getInstance(), _getCustomInitializer());

        sReaderModule.set(readerModule);

        return readerModule;
    }

    private Kernel() {
        Log.d(TAG, "Kernel");

        /* Nothing to do */
    }

    public static void finish() {
        Log.d(TAG, "finish");

        _finish();
    }

    public static void interrupt() {
        Log.d(TAG, "interrupt");

        Transaction current = sTransaction.get();

        if (current != null) {
            current.cancel();
        }

        _finish();
    }

    public static void create() {
        Log.d(TAG, "create");

        // sSemaphore.acquireUninterruptibly();

        TransactionType     SPE_TRNTYPE     = TransactionType.PURCHASE;
        Currency            SPE_TRNCURR     = Currency.getInstance("BRL");
        Amount              SPE_AMOUNT      = new Amount(new BigDecimal(100L / 100), SPE_TRNCURR);
        Amount              SPE_CASHBACK    = new Amount(new BigDecimal(  0L / 100), SPE_TRNCURR);
        Date                SPE_TRNDATE     = Calendar.getInstance().getTime();

        AndroidLogger logger = new AndroidLogger();

        logger.setDebugEnabled(true);
        logger.setErrorEnabled(true);
        logger.setInfoEnabled (true);

        DiscoverMPos.Companion.getDebugger().register(logger);

        MPosConfiguration   configuration   = _getConfiguration  (SPE_TRNTYPE, "CREDIT",    SPE_TRNCURR, SPE_AMOUNT);
        TransactionData     transaction     = _getTransactionData(SPE_AMOUNT,  SPE_TRNCURR, SPE_TRNTYPE, SPE_CASHBACK, SPE_TRNDATE);
        CardReaderModule    readerModule    = _getReaderModule   (configuration);

        DiscoverMPos mobilePOS  = new DiscoverMPos();

        mobilePOS.clear();
        mobilePOS.init (readerModule);

        DiscoverMPos.Companion.getDebugger().setEnabled(true);

        sMobilePOS.set(mobilePOS);

        CardReader   cardReader = readerModule.getCardReader();

        sTransaction.set(cardReader.startTransaction(transaction, CallbackUtility.getKernelCallback()));
    }
}
