package intellispaces.ixora.rdb;

import intellispaces.framework.core.annotation.Mapper;
import intellispaces.framework.core.annotation.Mover;
import intellispaces.framework.core.annotation.ObjectHandle;
import intellispaces.ixora.rdb.exception.RdbException;

import java.sql.PreparedStatement;
import java.sql.SQLException;

@ObjectHandle(value = PreparedStatementDomain.class, name = "PreparedStatementHandleImpl")
abstract class PreparedStatementHandle implements MovablePreparedStatement {
  private final PreparedStatement preparedStatement;

  PreparedStatementHandle(PreparedStatement preparedStatement) {
    this.preparedStatement = preparedStatement;
  }

  @Mapper
  @Override
  public ResultSet executeQuery() {
    try {
      java.sql.ResultSet rs = preparedStatement.executeQuery();
      return new ResultSetHandleImpl(rs);
    } catch (SQLException e) {
      throw RdbException.withCauseAndMessage(e, "Could not execute prepared statement");
    }
  }

  @Mover
  @Override
  public intellispaces.ixora.rdb.PreparedStatement setInt(int parameterIndex, int value) {
    try {
      preparedStatement.setInt(parameterIndex, value);
      return this;
    } catch (SQLException e) {
      throw RdbException.withCauseAndMessage(e, "Could not set parameter of the prepared statement");
    }
  }
}
