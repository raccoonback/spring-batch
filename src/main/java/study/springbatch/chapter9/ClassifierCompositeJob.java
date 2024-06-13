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
import org.springframework.batch.item.support.ClassifierCompositeItemWriter;
import org.springframework.batch.item.support.builder.ClassifierCompositeItemWriterBuilder;
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
import java.util.HashMap;
import java.util.Map;

@EnableBatchProcessing
@SpringBootApplication
@ConditionalOnProperty(name = "spring.batch.job.names", havingValue = "classifierCompositeWriterJob")
public class ClassifierCompositeJob {

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
    public ClassifierCompositeItemWriter<Customer> classifierCompositeItemWriter() {
        CustomerClassifier classifier = new CustomerClassifier(
                xmlDelegateItemWriter(null),
                jdbcDelegateItemWriter(null)
        );

        return new ClassifierCompositeItemWriterBuilder<Customer>()
                .classifier(classifier)
                .build();
    }


    @Bean
    public Step classifierCompositeStep() {
        return this.stepBuilderFactory.get("classifierCompositeStep")
                .<Customer, Customer>chunk(10)
                .reader(customerFileReader(null))
                .writer(classifierCompositeItemWriter())
                .stream(xmlDelegateItemWriter(null))
                .build();
    }

    @Bean
    public Job classifierCompositeWriterJob() {
        return this.jobBuilderFactory.get("classifierCompositeWriterJob")
                .start(classifierCompositeStep())
                .incrementer(new RunIdIncrementer())
                .build();
    }

    public static void main(String[] args) {
        SpringApplication.run(
                ClassifierCompositeJob.class,
                "--spring.batch.job.names=classifierCompositeWriterJob",
                "inputFile=/input/chapter9/customerWithEmail.csv",
                "outputFile=/output/xmlCustomer.xml"
        );
    }

}
