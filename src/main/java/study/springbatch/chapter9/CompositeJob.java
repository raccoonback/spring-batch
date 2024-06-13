package study.springbatch.chapter9;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.support.CompositeItemWriter;
import org.springframework.batch.item.support.builder.CompositeItemWriterBuilder;
import org.springframework.batch.item.xml.StaxEventItemWriter;
import org.springframework.batch.item.xml.builder.StaxEventItemWriterBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.oxm.xstream.XStreamMarshaller;

import javax.sql.DataSource;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@EnableBatchProcessing
@SpringBootApplication
@ConditionalOnProperty(name = "spring.batch.job.names", havingValue = "compositeWriterJob")
public class CompositeJob {

    @Autowired
    private JobBuilderFactory jobBuilderFactory;

    @Autowired
    private StepBuilderFactory stepBuilderFactory;

    @Bean
    @StepScope
    public FlatFileItemReader<Customer> customerFileReader(@Value("#{jobParameters['inputFile']}") Resource inputFile) {
        return new FlatFileItemReaderBuilder<Customer>()
                .name("customerFileReader")
                .resource(inputFile)
                .delimited()
                .names(
                        new String[]{
                                "firstName",
                                "middleInitial",
                                "lastName",
                                "address",
                                "city",
                                "state",
                                "zipCode",
                                "email"
                        }
                )
                .targetType(Customer.class)
                .build();
    }

    @Bean
    @StepScope
    public StaxEventItemWriter<Customer> xmlDelegateItemWriter(@Value("#{jobParameters['outputFile']}") Resource outputFile) {
        Map<String, Class> aliases = new HashMap<>();
        aliases.put("customer", Customer.class);

        XStreamMarshaller marshaller = new XStreamMarshaller();
        marshaller.setAliases(aliases);
        marshaller.afterPropertiesSet();
        return new StaxEventItemWriterBuilder<Customer>()
                .name("customerItemWriter")
                .resource(outputFile)
                .marshaller(marshaller)
                .rootTagName("customers")
                .build();
    }

    @Bean
    public JdbcBatchItemWriter<Customer> jdbcDelegateItemWriter(DataSource dataSource) {
        return new JdbcBatchItemWriterBuilder<Customer>()
                .namedParametersJdbcTemplate(
                        new NamedParameterJdbcTemplate(dataSource)
                )
                .sql("INSERT INTO CUSTOMER (firstName, middleInitial, lastName, address, city, state, zipCode, email) " +
                        "VALUES(:firstName, :middleInitial, :lastName, :address, :city, :state, :zipCode, :email)")
                .beanMapped()
                .build();
    }

    @Bean
    public CompositeItemWriter<Customer> compositeItemWriter() {
        return new CompositeItemWriterBuilder<Customer>()
                .delegates(
                        Arrays.asList(
                                xmlDelegateItemWriter(null),
                                jdbcDelegateItemWriter(null)
                        )
                )
                .build();
    }


    @Bean
    public Step compositeStep() {
        return this.stepBuilderFactory.get("compositeStep")
                .<Customer, Customer>chunk(10)
                .reader(customerFileReader(null))
                .writer(compositeItemWriter())
                .build();
    }

    @Bean
    public Job compositeWriterJob() {
        return this.jobBuilderFactory.get("compositeWriterJob")
                .start(compositeStep())
                .incrementer(new RunIdIncrementer())
                .build();
    }

    public static void main(String[] args) {
        SpringApplication.run(
                CompositeJob.class,
                "--spring.batch.job.names=compositeWriterJob",
                "inputFile=/input/chapter9/customerWithEmail.csv"
        );
    }

}
