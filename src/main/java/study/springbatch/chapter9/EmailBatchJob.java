package study.springbatch.chapter9;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.mail.SimpleMailMessageItemWriter;
import org.springframework.batch.item.mail.builder.SimpleMailMessageItemWriterBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;

import javax.sql.DataSource;

@EnableBatchProcessing
@SpringBootApplication
@ConditionalOnProperty(name = "spring.batch.job.names", havingValue = "emailJob")
public class EmailBatchJob {

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
                                "email",
                        }
                )
                .targetType(Customer.class)
                .build();
    }

    @Bean
    @StepScope
    public JdbcBatchItemWriter<Customer> customerItemWriter(DataSource dataSource) {
        return new JdbcBatchItemWriterBuilder<Customer>()
                .namedParametersJdbcTemplate(
                        new NamedParameterJdbcTemplate(dataSource)
                )
                .sql("INSERT INTO CUSTOMER (firstName, middleInitial, lastName, address, city, state, zipCode, email) " +
                        "VALUES (:firstName, :middleInitial, :lastName, :address, :city, :state, :zipCode, :email)")
                .beanMapped()
                .build();
    }

    @Bean
    public JdbcCursorItemReader<Customer> customerItemReader(DataSource dataSource) {
        return new JdbcCursorItemReaderBuilder<Customer>()
                .name("customerItemReader")
                .dataSource(dataSource)
                .sql("select * from customer")
                .rowMapper(new BeanPropertyRowMapper<>(Customer.class))
                .build();
    }

    @Bean
    public SimpleMailMessageItemWriter emailItemWriter(MailSender mailSender) {
        return new SimpleMailMessageItemWriterBuilder()
                .mailSender(mailSender)
                .build();
    }

    @Bean
    public Step importStep() {
        return this.stepBuilderFactory.get("importStep")
                .<Customer, Customer> chunk(10)
                .reader(customerFileReader(null))
                .writer(customerItemWriter(null))
                .build();
    }

    @Bean
    public Step emailStep() {
        return this.stepBuilderFactory.get("emailStep")
                .<Customer, SimpleMailMessage> chunk(10)
                .reader(customerFileReader(null))
                .processor((ItemProcessor<Customer, SimpleMailMessage>) item -> {
                    SimpleMailMessage mail = new SimpleMailMessage();
                    mail.setFrom("test@gmail.com");
                    mail.setTo(item.getEmail());
                    mail.setSubject("Welcome!");
                    mail.setText(
                            String.format("Welcome %s %s,\nYou were imported into the system using Spring Batch!",
                                    item.getFirstName(), item.getLastName())

                    );
                    return mail;
                })
                .writer(emailItemWriter(null))
                .build();
    }

    @Bean
    public Job emailJob() {
        return this.jobBuilderFactory.get("emailJob")
                .start(importStep())
                .next(emailStep())
                .incrementer(new RunIdIncrementer())
                .build();
    }

    public static void main(String[] args) {
        SpringApplication.run(
                EmailBatchJob.class,
                "--spring.batch.job.names=emailJob",
                "inputFile=/input/chapter9/customerWithEmail.csv"
        );
    }

}
