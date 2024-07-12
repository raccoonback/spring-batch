package study.springbatch.chapter13;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.test.MetaDataInstanceFactory;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import study.springbatch.chapter10.AccountItemProcessor;
import study.springbatch.chapter10.domain.CustomerAddressUpdate;
import study.springbatch.chapter10.domain.CustomerContactUpdate;
import study.springbatch.chapter10.domain.CustomerNameUpdate;
import study.springbatch.chapter10.domain.CustomerUpdate;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
        ImportJobConfiguration.class,
        CustomerItemValidator.class,
        AccountItemProcessor.class
})
@JdbcTest
@EnableBatchProcessing
@SpringBatchTest
class ImportJobConfigurationTest {

    @Autowired
    private FlatFileItemReader<CustomerUpdate> customerUpdateItemReader;


    public StepExecution getStepExecution() {
        JobParameters jobParameters = new JobParametersBuilder()
                .addString("customerUpdateFile", "classpath:customerUpdateFile.csv")
                .toJobParameters();

        return MetaDataInstanceFactory.createStepExecution(jobParameters);
    }

    @Test
    void testTypeConversion() throws Exception {
        customerUpdateItemReader.open(new ExecutionContext());

        assertTrue(customerUpdateItemReader.read() instanceof CustomerAddressUpdate);
        assertTrue(customerUpdateItemReader.read() instanceof CustomerContactUpdate);
        assertTrue(customerUpdateItemReader.read() instanceof CustomerNameUpdate);
    }
}