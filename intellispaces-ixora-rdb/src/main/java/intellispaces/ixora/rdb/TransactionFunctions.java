package intellispaces.ixora.rdb;

import intellispaces.common.base.exception.CoveredCheckedException;
import intellispaces.common.base.function.ThrowableFunction;
import intellispaces.framework.core.system.ContextProjections;
import intellispaces.ixora.rdb.exception.TransactionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;

/**
 * Transaction functions.
 */
public class TransactionFunctions {
  private static final String TRANSACTION_PROJECTION_NAME = "tx";
  private static final Logger LOG = LoggerFactory.getLogger(TransactionFunctions.class);

  private TransactionFunctions() {}

  public static void transactional(TransactionFactory transactionFactory, Runnable operation) {
    transactional(transactionFactory,
        data -> {
          operation.run();
          return null;
        },
        null
    );
  }

  public static void transactional(
      TransactionFactory transactionFactory, Consumer<Transaction> operation
  ) {
    transactional(transactionFactory,
        data -> {
          operation.accept(Transactions.current());
          return null;
        },
        null
    );
  }

  public static <R, E extends Throwable> R transactional(
      TransactionFactory transactionFactory,
      ThrowableFunction<Object[], R, E> operation,
      Object[] data
  ) {
    R result;
    Transaction tx = null;
    try {
      tx = transactionFactory.getTransaction();
      storeTransactionInContext(tx);
      result = operation.apply(data);
      tx.commit();
    } catch (TransactionException e) {
      LOG.error("Unexpected exception occurred while transaction was executed. Transaction will be rolled back");
      rollback(tx, e);
      throw e;
    } catch (RuntimeException | Error e) {
      if (tx == null) {
        throw TransactionException.withCauseAndMessage(e, "Could not get transaction");
      }
      LOG.error("Runtime exception " + e.getClass().getCanonicalName() + " occurred while transaction was executed. " +
          "Transaction will be rolled back");
      rollback(tx, e);
      throw TransactionException.withCauseAndMessage(e, "Could not execute transaction");
    } catch (Throwable e) {
      if (tx == null) {
        throw TransactionException.withCauseAndMessage(e, "Could not get transaction");
      }
      LOG.info("Checked exception " + e.getClass().getCanonicalName() + " occurred while transaction was executed. " +
          "Transaction will be committed");
      commit(tx, e);
      throw CoveredCheckedException.withCause(e);
    } finally {
      if (tx != null) {
        removeTransactionFromContext();
      }
    }
    return result;
  }

  private static void storeTransactionInContext(Transaction tx) {
    Transactions.setCurrent(tx);
    ContextProjections.addProjection(TRANSACTION_PROJECTION_NAME, Transaction.class, tx);
  }

  private static void removeTransactionFromContext() {
    Transactions.setCurrent(null);
    ContextProjections.removeProjection(TRANSACTION_PROJECTION_NAME);
  }

  private static void commit(Transaction tx, Throwable reason) {
    try {
      tx.commit();
    } catch (Throwable e) {
      if (reason != null) {
        TransactionException te = TransactionException.withCauseAndMessage(e,
            "Could not commit transaction after exception {0}", reason.getClass().getCanonicalName());
        te.addSuppressed(reason);
        throw te;
      }
      throw TransactionException.withCauseAndMessage(e, "Could not commit transaction");
    }
  }

  private static void rollback(Transaction tx, Throwable reason) {
    try {
      tx.rollback();
    } catch (Throwable e) {
      if (reason != null) {
        TransactionException te = TransactionException.withCauseAndMessage(e,
            "Could not roll back transaction after exception {0}", reason.getClass().getCanonicalName());
        te.addSuppressed(reason);
        throw te;
      }
      throw TransactionException.withCauseAndMessage(e, "Could not roll back transaction");
    }
  }
}
