package study.springbatch.chapter9;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.data.RepositoryItemWriter;
import org.springframework.batch.item.data.builder.RepositoryItemWriterBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.Resource;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;


@EnableBatchProcessing
@SpringBootApplication
@EnableJpaRepositories(basePackageClasses = Customer.class)
@ConditionalOnProperty(name = "spring.batch.job.names", havingValue = "repositoryJob")
public class RepositoryImportJob {

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
    public RepositoryItemWriter<Customer> customerItemWriter(CustomerRepository customerRepository) {
        return new RepositoryItemWriterBuilder<Customer>()
                .repository(customerRepository)
                .methodName("save")
                .build();
    }

    @Bean
    public Step repositoryStep() {
        return this.stepBuilderFactory.get("repositoryStep")
                .<Customer, Customer>chunk(10)
                .reader(customerFileReader(null))
                .writer(customerItemWriter(null))
                .build();
    }

    @Bean
    public Job repositoryJob() {
        return this.jobBuilderFactory.get("repositoryJob")
                .start(repositoryStep())
                .incrementer(new RunIdIncrementer())
                .build();
    }

    public static void main(String[] args) {
        SpringApplication.run(
                RepositoryImportJob.class,
                "--spring.batch.job.names=repositoryJob",
                "inputFile=/input/chapter9/customer.csv"
        );
    }

}
