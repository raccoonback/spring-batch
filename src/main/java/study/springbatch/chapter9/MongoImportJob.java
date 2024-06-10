package study.springbatch.chapter9;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.data.MongoItemWriter;
import org.springframework.batch.item.data.builder.MongoItemWriterBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.Resource;


//@EnableBatchProcessing
//@SpringBootApplication
//@ConditionalOnProperty(name = "spring.batch.job.names", havingValue = "mongoJob")
//public class MongoImportJob {
//
//    @Autowired
//    private JobBuilderFactory jobBuilderFactory;
//
//    @Autowired
//    private StepBuilderFactory stepBuilderFactory;
//
//    @Bean
//    @StepScope
//    public FlatFileItemReader<Customer> customerFileReader(@Value("#{jobParameters['inputFile']}") Resource inputFile) {
//        return new FlatFileItemReaderBuilder<Customer>()
//                .name("customerFileReader")
//                .resource(inputFile)
//                .delimited()
//                .names(
//                        new String[]{
//                                "firstName",
//                                "middleInitial",
//                                "lastName",
//                                "address",
//                                "city",
//                                "state",
//                                "zip",
//                        }
//                )
//                .targetType(Customer.class)
//                .build();
//    }
//
//    @Bean
//    public MongoItemWriter<Customer> customerItemWriter(MongoOperations mongoOperations) {
//        return new MongoItemWriterBuilder<Customer>()
//                .collection("customers")
//                .template(mongoOperations)
//                .build();
//    }
//
//    @Bean
//    public Step mongoStep() {
//        return this.stepBuilderFactory.get("mongoStep")
//                .<Customer, Customer> chunk(10)
//                .reader(customerFileReader(null))
//                .writer(customerItemWriter(null))
//                .build();
//    }
//
//    @Bean
//    public Job mongoJob() {
//        return this.jobBuilderFactory.get("mongoJob")
//                .start(mongoStep())
//                .incrementer(new RunIdIncrementer())
//                .build();
//    }
//
//    public static void main(String[] args) {
//        SpringApplication.run(
//                MongoImportJob.class,
//                "--spring.batch.job.names=mongoJob",
//                "inputFile=/input/chapter9/customer.csv"
//        );
//    }
//
//}
