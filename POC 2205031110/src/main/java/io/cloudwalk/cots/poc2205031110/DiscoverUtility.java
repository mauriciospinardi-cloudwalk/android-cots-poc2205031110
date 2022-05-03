package io.cloudwalk.cots.poc2205031110;

import android.util.Log;

import com.discover.mpos.sdk.core.DiscoverMPos;
import com.discover.mpos.sdk.core.data.Amount;
import com.discover.mpos.sdk.core.debug.logger.AndroidLogger;
import com.discover.mpos.sdk.core.debug.logger.Message;
import com.discover.mpos.sdk.core.debug.logger.MessageType;
import com.discover.mpos.sdk.transaction.TransactionType;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Currency;
import java.util.Date;

public class DiscoverUtility {
    private final static String
            TAG = DiscoverUtility.class.getSimpleName();

    private DiscoverUtility() {
        Log.d(TAG, "DiscoverUtility");

        /* Nothing to do */
    }

    public static void run() {
        Log.d(TAG, "run");

        TransactionType     SPE_TRNTYPE     = TransactionType.PURCHASE;
        String              SPE_APPTYPE     = "CREDIT";
        Currency            SPE_TRNCURR     = Currency.getInstance("BRL");
        Amount              SPE_AMOUNT      = new Amount(new BigDecimal(100L), SPE_TRNCURR);
        Amount              SPE_CASHBACK    = new Amount(new BigDecimal(  0L), SPE_TRNCURR);
        Date                SPE_TRNDATE     = Calendar.getInstance().getTime();

        AndroidLogger logger = new AndroidLogger();

        logger.setDebugEnabled(true);
        logger.setErrorEnabled(true);
        logger.setInfoEnabled (true);

        logger.show(new Message(MessageType.DEBUG, TAG, "run::logger [" + logger + "]", SPE_TRNDATE));

        DiscoverMPos.Companion.getDebugger().register(logger);

        /*

        MPosConfiguration   config          = _getConfiguration(SPE_TRNTYPE, SPE_APPTYPE, SPE_TRNCURR, SPE_AMOUNT);
        CardReaderModule    cardReader      = new CardReaderModule(config, ApplicationUtility.getApplication(), _getCustomInitializer());
        TransactionData     transac         = new TransactionData(SPE_AMOUNT, SPE_TRNCURR, SPE_TRNTYPE, SPE_CASHBACK, ConnectorType.NFC, SPE_TRNDATE);

        (new DiscoverMPos()).init(cardReader);

        mSemaphore = new Semaphore(0, true);

        mCardReader = cardReader.getCardReader();

        mCardReader.startTransaction(transac, _getTransactionHandler());

        try {
            mSemaphore.tryAcquire(60, TimeUnit.SECONDS); // TODO: timeout
        } catch (Exception exception) {
            Log.e(TAG, Log.getStackTraceString(exception));
        } finally {
            mSemaphore = new Semaphore(0, true);
        }

         */
    }
}
