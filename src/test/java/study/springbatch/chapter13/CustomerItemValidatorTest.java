package study.springbatch.chapter13;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.batch.item.validator.ValidationException;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import study.springbatch.chapter10.domain.CustomerUpdate;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

class CustomerItemValidatorTest {

    @Mock
    private NamedParameterJdbcTemplate template;

    private CustomerItemValidator validator;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        this.validator = new CustomerItemValidator(template);
    }

    @Test
    void testValidCustomer() {
        // given
        CustomerUpdate customer = new CustomerUpdate(5L);

        // when
        ArgumentCaptor<Map<String, Long>> parameterMap = ArgumentCaptor.forClass(Map.class);
        when(template.queryForObject(
                eq(CustomerItemValidator.FIND_CUSTOMER),
                parameterMap.capture(),
                eq(Long.class)
        )).thenReturn(2L);

        validator.validate(customer);

        // then
        assertEquals(5L, parameterMap.getValue().get("id"));
    }

    @Test
    void testInvalidCustomer() {
        // given
        CustomerUpdate customer = new CustomerUpdate(5L);

        // when
        ArgumentCaptor<Map<String, Long>> parameterMap = ArgumentCaptor.forClass(Map.class);
        when(template.queryForObject(
                eq(CustomerItemValidator.FIND_CUSTOMER),
                parameterMap.capture(),
                eq(Long.class)
        )).thenReturn(0L);

        Throwable exception = assertThrows(ValidationException.class, () -> validator.validate(customer));

        // then
        assertEquals(
                "Customer id 5 was not able to be found",
                exception.getMessage()
        );
    }
}