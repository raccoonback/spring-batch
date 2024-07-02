package study.springbatch.chapter11;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.integration.async.AsyncItemProcessor;
import org.springframework.batch.integration.async.AsyncItemWriter;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.Resource;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import study.springbatch.chapter11.domain.Transaction;

import javax.sql.DataSource;
import java.util.concurrent.Future;

@EnableBatchProcessing
@SpringBootApplication
@ConditionalOnProperty(name = "spring.batch.job.names", havingValue = "asyncStepsJob")
public class AsyncProcessorWriterJobApplication {

    @Autowired
    private JobBuilderFactory jobBuilderFactory;

    @Autowired
    private StepBuilderFactory stepBuilderFactory;

    @Bean
    public Job asyncStepsJob() {
        return jobBuilderFactory.get("asyncStepsJob")
                .incrementer(new RunIdIncrementer())
                .start(asyncStep())
                .build();
    }

    @Bean
    @StepScope
    public FlatFileItemReader<Transaction> fileTransactionReader(@Value("#{jobParameters['inputFlatFile']}") Resource resource) {

        return new FlatFileItemReaderBuilder<Transaction>()
                .name("transactionItemReader")
                .resource(resource)
                .saveState(false)
                .delimited()
                .names(new String[] {"account", "amount", "timestamp"})
                .fieldSetMapper(fieldSet -> {
                    Transaction transaction = new Transaction();

                    transaction.setAccount(fieldSet.readString("account"));
                    transaction.setAmount(fieldSet.readBigDecimal("amount"));
                    transaction.setTimestamp(fieldSet.readDate("timestamp", "yyyy-MM-dd HH:mm:ss"));

                    return transaction;
                })
                .build();
    }

    @Bean
    public AsyncItemProcessor<Transaction, Transaction> asyncItemProcessor() {
        AsyncItemProcessor<Transaction, Transaction> processor = new AsyncItemProcessor<>();

        processor.setDelegate(processor());
        processor.setTaskExecutor(new SimpleAsyncTaskExecutor());

        return processor;
    }

    @Bean
    public ItemProcessor<Transaction, Transaction> processor() {
        return (transaction) -> {
            Thread.sleep(5);
            return transaction;
        };
    }

    @Bean
    public AsyncItemWriter<Transaction> asyncItemWriter() {
        AsyncItemWriter<Transaction> writer = new AsyncItemWriter<>();

        writer.setDelegate(writer(null));

        return writer;
    }

    @Bean
    @StepScope
    public JdbcBatchItemWriter<Transaction> writer(DataSource dataSource) {
        return new JdbcBatchItemWriterBuilder<Transaction>()
                .dataSource(dataSource)
                .beanMapped()
                .sql("insert into transactions (account, amount, timestamp) values (:account, :amount, :timestamp)")
                .build();
    }

    @Bean
    public Step asyncStep() {
        return stepBuilderFactory.get("asyncStep")
                .<Transaction, Future<Transaction>>chunk(100)
                .reader(fileTransactionReader(null))
                .processor(asyncItemProcessor())
                .writer(asyncItemWriter())
                .build();
    }


    public static void main(String[] args) {
        String[] newArgs = new String[] {
                "--spring.batch.job.names=asyncStepsJob",
                "inputFlatFile=/input/chapter11/bigtransactions.csv",
                "1",
                "inputXmlFile=/input/chapter11/bigtransactions.xml",
        };

        SpringApplication.run(AsyncProcessorWriterJobApplication.class, newArgs);
    }
}
