package study.springbatch.chapter6.pause;

import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.util.List;

public class TransactionDaoSupport extends JdbcTemplate implements TransactionDao {

    public TransactionDaoSupport(DataSource dataSource) {
        super(dataSource);
    }

    @SuppressWarnings("unchecked")
    public List<Transaction> getTransactionsByAccountNumber(String accountNumber) {
        return query(
                "select t.id, t.timestamp, t.amount " +
                        "from TRANSACTION t inner join ACCOUNT_SUMMARY a on " +
                        "a.id = t.account_summary_id " +
                        "where a.account_number = ?",
                new Object[] { accountNumber },
                (rs, rowNum) -> {
                    Transaction transaction = new Transaction();
                    transaction.setAmount(rs.getDouble("amount"));
                    transaction.setTimestamp(rs.getDate("timestamp"));
                    return transaction;
                }
        );
    }

}
