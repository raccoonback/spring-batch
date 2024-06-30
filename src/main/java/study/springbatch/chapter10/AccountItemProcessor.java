package study.springbatch.chapter10;

import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import study.springbatch.chapter10.domain.Account;
import study.springbatch.chapter10.domain.Statement;

@Component
public class AccountItemProcessor implements ItemProcessor<Statement, Statement> {

    @Autowired
    private JdbcTemplate jdbcTemplate;


    @Override
    public Statement process(Statement item) throws Exception {
        item.setAccounts(
                jdbcTemplate.query(
                        "SELECT " +
                                "a.account_id, a.balance, a.last_state_date, " +
                                "t.transaction_id, t.description, t.credit, t.debit, t.timestamp " +
                            "FROM account a LEFT JOIN transaction t ON a.account_id = t.account_account_id " +
                            "WHERE a.account_id IN ( " +
                                                        "SELECT account_account_id " +
                                                        "FROM customer_account " +
                                                        "WHERE customer_customer_id = ? " +
                                                    ") " +
                            "ORDER BY t.timestamp",
                        new Object[] {
                                item.getCustomer().getId()
                        },
                        new AccountresultSetExtractor()
                )
        );

        return item;
    }

}
