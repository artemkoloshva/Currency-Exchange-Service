package mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public interface ResultSetMapper<R> extends Mapper<ResultSet, R> {
    R map(ResultSet resultSet) throws SQLException;
}
