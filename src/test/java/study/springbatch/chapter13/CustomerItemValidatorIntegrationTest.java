package study.springbatch.chapter13;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.batch.item.validator.ValidationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import study.springbatch.chapter10.domain.CustomerUpdate;

import javax.sql.DataSource;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@JdbcTest
class CustomerItemValidatorIntegrationTest {

    @Autowired
    private DataSource dataSource;

    private CustomerItemValidator customerItemValidator;

    @BeforeEach
    public void setUp() {
        NamedParameterJdbcTemplate namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
        customerItemValidator = new CustomerItemValidator(dataSource);
    }

    @Test
    void testNoCustomers() {
        CustomerUpdate customerUpdate = new CustomerUpdate(-5L);

        ValidationException exception = assertThrows(ValidationException.class, () -> customerItemValidator.validate(customerUpdate));

        assertEquals("Customer id -5 was not able to be found", exception.getMessage());
    }

    @Test
    void testCustomers() {
        CustomerUpdate customerUpdate = new CustomerUpdate(5L);
        customerItemValidator.validate(customerUpdate);
    }
}