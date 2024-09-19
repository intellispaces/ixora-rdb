package intellispaces.ixora.rdb;

import intellispaces.framework.core.annotation.Mapper;
import intellispaces.framework.core.annotation.Mover;
import intellispaces.framework.core.annotation.ObjectHandle;
import intellispaces.ixora.rdb.exception.TransactionException;

@ObjectHandle(value = TransactionFactoryDomain.class, name = "TransactionFactoryHandleImpl")
public abstract class TransactionFactoryHandle implements MovableTransactionFactory {
  private final MovableDataSource dataSource;

  public TransactionFactoryHandle(MovableDataSource dataSource) {
    this.dataSource = dataSource;
  }

  @Mapper
  @Override
  public DataSource dataSource() {
    return dataSource;
  }

  @Mover
  @Override
  public Transaction getTransaction() throws TransactionException {
    MovableConnection connection = (MovableConnection) dataSource.getConnection();
    return new TransactionHandleImpl(connection);
  }
}