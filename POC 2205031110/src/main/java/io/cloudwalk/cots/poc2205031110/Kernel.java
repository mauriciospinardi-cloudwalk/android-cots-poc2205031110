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
import com.discover.mpos.sdk.core.debug.logger.Message;
import com.discover.mpos.sdk.core.debug.logger.MessageType;
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
import java.util.concurrent.atomic.AtomicReference;

import io.cloudwalk.cots.poc2205031110.utilities.CallbackUtility;

public class Kernel {
    private static final String
            TAG = Kernel.class.getSimpleName();

    private static final AtomicReference<Transaction>
            sTransaction = new AtomicReference<>(null);

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
                            false,  false,
                            false,  false,  new ArrayList<>(0)
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
                            false,  false,
                            false,  false,  new ArrayList<>(0)
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

    private static TransactionData _getTransactionData(Amount SPE_AMOUNT, Currency SPE_TRNCURR, TransactionType SPE_TRNTYPE, Amount SPE_CASHBACK, Date SPE_TRNDATE) {
        Log.d(TAG, "_getTransactionData");

        return new TransactionData(SPE_AMOUNT, SPE_TRNCURR, SPE_TRNTYPE, SPE_CASHBACK, ConnectorType.NFC, SPE_TRNDATE);
    }

    private static CardReaderModule _getReaderModule(MPosConfiguration configuration) {
        Log.d(TAG, "_getReaderModule");

        return new CardReaderModule(configuration, Application.getInstance(), _getCustomInitializer());
    }

    private Kernel() {
        Log.d(TAG, "Kernel");

        /* Nothing to do */
    }

    public static void interrupt() {
        Log.d(TAG, "interrupt");

        Transaction current = sTransaction.get();

        if (current != null) {
            current.cancel();
        }
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

        MPosConfiguration   configuration   = _getConfiguration  (SPE_TRNTYPE, SPE_APPTYPE, SPE_TRNCURR, SPE_AMOUNT);
        TransactionData     transaction     = _getTransactionData(SPE_AMOUNT,  SPE_TRNCURR, SPE_TRNTYPE, SPE_CASHBACK, SPE_TRNDATE);
        CardReaderModule    readerModule    = _getReaderModule   (configuration);

        DiscoverMPos mobilePOS  = new DiscoverMPos();

        mobilePOS.clear();
        mobilePOS.init (readerModule);

        DiscoverMPos.Companion.getDebugger().setEnabled(true);

        CardReader   cardReader = readerModule.getCardReader();

        sTransaction.set(cardReader.startTransaction(transaction, CallbackUtility.getKernelCallback()));
    }
}
