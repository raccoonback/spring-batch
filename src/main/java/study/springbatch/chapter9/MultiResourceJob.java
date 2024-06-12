package study.springbatch.chapter9;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.MultiResourceItemWriter;
import org.springframework.batch.item.file.builder.MultiResourceItemWriterBuilder;
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.batch.item.file.transform.FormatterLineAggregator;
import org.springframework.batch.item.xml.StaxEventItemWriter;
import org.springframework.batch.item.xml.builder.StaxEventItemWriterBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.FileSystemResource;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.oxm.xstream.XStreamMarshaller;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@EnableBatchProcessing
@SpringBootApplication
@ConditionalOnProperty(name = "spring.batch.job.names", havingValue = "multiXmlGeneratorJob")
public class MultiResourceJob {

    @Autowired
    private JobBuilderFactory jobBuilderFactory;

    @Autowired
    private StepBuilderFactory stepBuilderFactory;

    @Bean
    public JdbcCursorItemReader<Customer> jdbcCursorItemReader(DataSource dataSource) {
        return new JdbcCursorItemReaderBuilder<Customer>()
                .name("customerItemReader")
                .dataSource(dataSource)
                .sql("select * from CUSTOMER")
                .rowMapper(new BeanPropertyRowMapper<>(Customer.class))
                .build();
    }

    @Bean
    @StepScope
    public StaxEventItemWriter<Customer> delegateItemWriter() {
        Map<String, Class> aliases = new HashMap<>();
        aliases.put("customer", Customer.class);

        XStreamMarshaller marshaller = new XStreamMarshaller();
        marshaller.setAliases(aliases);
        marshaller.afterPropertiesSet();
        return new StaxEventItemWriterBuilder<Customer>()
                .name("customerItemWriter")
                .marshaller(marshaller)
                .rootTagName("customers")
                .build();
    }

    @Bean
    @StepScope
    public FlatFileItemWriter<Customer> delegateCustomerItemWriter(CustomerRecordCountFooterCallback footerCallback) {
        BeanWrapperFieldExtractor<Customer> fieldExtractor = new BeanWrapperFieldExtractor<>();
        fieldExtractor.setNames(new String[]{
                "firstName",
                "lastName",
                "address",
                "city",
                "state",
                "zipCode",
        });
        fieldExtractor.afterPropertiesSet();

        FormatterLineAggregator<Customer> lineAggregator = new FormatterLineAggregator<>();
        lineAggregator.setFormat("%s %s lives at %s %s in %s, %s.");
        lineAggregator.setFieldExtractor(fieldExtractor);

        FlatFileItemWriter<Customer> itemWriter = new FlatFileItemWriter<>();
        itemWriter.setName("delegateCustomerItemWriter");
        itemWriter.setLineAggregator(lineAggregator);
        itemWriter.setAppendAllowed(true);
        itemWriter.setFooterCallback(footerCallback);

        return itemWriter;
    }

    @Bean
    public MultiResourceItemWriter<Customer> multiCustomerFileWriter(CustomerOutputFileSuffixCreator suffixCreator) {
        return new MultiResourceItemWriterBuilder<Customer>()
                .name("multiCustomerFileWriter")
                .delegate(delegateCustomerItemWriter(null))
                .itemCountLimitPerResource(3)
                .resource(new FileSystemResource("test-customer"))
                .resourceSuffixCreator(suffixCreator)
                .build();
    }

    @Bean
    public Step multiXmlGeneratorStep() {
        return this.stepBuilderFactory.get("multiXmlGeneratorStep")
                .<Customer, Customer>chunk(10)
                .reader(jdbcCursorItemReader(null))
                .writer(multiCustomerFileWriter(null))
                .build();
    }

    @Bean
    public Job multiXmlGeneratorJob() {
        return this.jobBuilderFactory.get("multiXmlGeneratorJob")
                .start(multiXmlGeneratorStep())
                .incrementer(new RunIdIncrementer())
                .build();
    }

    public static void main(String[] args) {
        SpringApplication.run(
                MultiResourceJob.class,
                "--spring.batch.job.names=multiXmlGeneratorJob",
                "inputFile=/input/chapter9/customer.csv"
        );
    }

}
