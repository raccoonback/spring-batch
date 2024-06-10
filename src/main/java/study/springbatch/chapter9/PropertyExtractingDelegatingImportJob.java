package study.springbatch.chapter9;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.adapter.ItemWriterAdapter;
import org.springframework.batch.item.adapter.PropertyExtractingDelegatingItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.Resource;


@EnableBatchProcessing
@SpringBootApplication
@ConditionalOnProperty(name = "spring.batch.job.names", havingValue = "propertyExtractingDelegatingJob")
public class PropertyExtractingDelegatingImportJob {

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
                                "zip",
                        }
                )
                .targetType(Customer.class)
                .build();
    }

    @Bean
    public PropertyExtractingDelegatingItemWriter<Customer> customerItemWriter(CustomerService customerService) {
        PropertyExtractingDelegatingItemWriter<Customer> itemWriter = new PropertyExtractingDelegatingItemWriter<>();

        itemWriter.setTargetObject(customerService);
        itemWriter.setTargetMethod("logCustomerAddress");
        itemWriter.setFieldsUsedAsTargetMethodArguments(
                new String[]{
                        "address", "city", "state", "zipCode"
                }
        );

        return itemWriter;
    }

    @Bean
    public Step propertyExtractingDelegatingStep() {
        return this.stepBuilderFactory.get("propertyExtractingDelegatingStep")
                .<Customer, Customer>chunk(10)
                .reader(customerFileReader(null))
                .writer(customerItemWriter(null))
                .build();
    }

    @Bean
    public Job propertyExtractingDelegatingJob() {
        return this.jobBuilderFactory.get("propertyExtractingDelegatingJob")
                .start(propertyExtractingDelegatingStep())
                .incrementer(new RunIdIncrementer())
                .build();
    }

    public static void main(String[] args) {
        SpringApplication.run(
                PropertyExtractingDelegatingImportJob.class,
                "--spring.batch.job.names=propertyExtractingDelegatingJob",
                "inputFile=/input/chapter9/customer.csv"
        );
    }

}
