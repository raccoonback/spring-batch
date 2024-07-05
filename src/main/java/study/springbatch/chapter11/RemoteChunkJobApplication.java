package study.springbatch.chapter11;

import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.step.tasklet.TaskletStep;
import org.springframework.batch.integration.chunk.RemoteChunkingMasterStepBuilderFactory;
import org.springframework.batch.integration.chunk.RemoteChunkingWorkerBuilder;
import org.springframework.batch.integration.config.annotation.EnableBatchIntegration;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.Resource;
import org.springframework.integration.amqp.dsl.Amqp;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.channel.QueueChannel;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import study.springbatch.chapter11.domain.Transaction;

import javax.sql.DataSource;

@EnableBatchIntegration
@Configuration
public class RemoteChunkJobApplication {

    @Configuration
    @Profile("!worker")
    public static class MasterConfiguration {

        @Autowired
        private JobBuilderFactory jobBuilderFactory;

        @Autowired
        private RemoteChunkingMasterStepBuilderFactory remoteChunkingMasterStepBuilderFactory;

        @Bean
        public DirectChannel requests() {
            return new DirectChannel();
        }

        @Bean
        public IntegrationFlow outboundFlow(AmqpTemplate amqpTemplate) {
            return IntegrationFlows.from(requests())
                    .handle(
                            Amqp.outboundAdapter(amqpTemplate)
                                    .routingKey("requests")
                    )
                    .get();
        }

        @Bean
        public QueueChannel replies() {
            return new QueueChannel();
        }

        @Bean
        public IntegrationFlow inboundFlow(ConnectionFactory connectionFactory) {
            return IntegrationFlows
                    .from(Amqp.inboundAdapter(connectionFactory, "replies"))
                    .channel(replies())
                    .get();
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
        public TaskletStep masterStep() {
            return remoteChunkingMasterStepBuilderFactory.get("masterStep")
                    .<Transaction, Transaction> chunk(100)
                    .reader(fileTransactionReader(null))
                    .outputChannel(requests())
                    .inputChannel(replies())
                    .build();
        }

        @Bean
        public Job remoteChunkingJob() {
            return jobBuilderFactory.get("remoteChunkingJob")
                    .start(masterStep())
                    .build();
        }
    }

    @Configuration
    @Profile("worker")
    public static class WorkerConfiguration {

        @Autowired
        private RemoteChunkingWorkerBuilder<Transaction, Transaction> workerBuilder;

        @Bean
        public DirectChannel requests() {
            return new DirectChannel();
        }

        @Bean
        public DirectChannel replies() {
            return new DirectChannel();
        }

        @Bean
        public IntegrationFlow inboundFlow(ConnectionFactory connectionFactory) {
            return IntegrationFlows
                    .from(Amqp.inboundAdapter(connectionFactory, "requests"))
                    .channel(requests())
                    .get();
        }

        @Bean
        public IntegrationFlow outboundFlow(AmqpTemplate amqpTemplate) {
            return IntegrationFlows.from(replies())
                    .handle(
                            Amqp.outboundAdapter(amqpTemplate)
                                    .routingKey("replies")
                    )
                    .get();
        }

        @Bean
        public IntegrationFlow integrationFlow() {
            return workerBuilder
                    .itemProcessor(processor())
                    .itemWriter(writer(null))
                    .inputChannel(requests())
                    .outputChannel(replies())
                    .build();
        }

        @Bean
        public ItemProcessor<Transaction, Transaction> processor() {
            return transaction -> {
                System.out.println("processing transaction: " + transaction);
                return transaction;
            };
        }

        public JdbcBatchItemWriter<Transaction> writer(DataSource dataSource) {
            return new JdbcBatchItemWriterBuilder<Transaction>()
                    .dataSource(dataSource)
                    .beanMapped()
                    .sql("insert into transactions (account, amount, timestamp) values (:account, :amount, :timestamp)")
                    .build();
        }
    }
}
