package study.springbatch.chapter04;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.batch.item.file.mapping.PassThroughLineMapper;
import org.springframework.batch.item.file.transform.PassThroughLineAggregator;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.batch.item.support.ListItemWriter;
import org.springframework.batch.repeat.CompletionPolicy;
import org.springframework.batch.repeat.policy.CompositeCompletionPolicy;
import org.springframework.batch.repeat.policy.SimpleCompletionPolicy;
import org.springframework.batch.repeat.policy.TimeoutTerminationPolicy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.Resource;

import java.util.ArrayList;
import java.util.UUID;

@EnableBatchProcessing
@SpringBootApplication
public class ChunkJob {

    @Autowired
    private JobBuilderFactory jobBuilderFactory;

    @Autowired
    private StepBuilderFactory stepBuilderFactory;

    @Bean
    public Job chunkBasedJob1() {
        return this.jobBuilderFactory.get("chunkBasedJob1")
                .start(chunkStep())
                .incrementer(new RunIdIncrementer())
                .build();
    }

    @Bean
    public Step chunkStep() {
        return this.stepBuilderFactory.get("chunkStep")
                .<String, String>chunk(1000)
                .reader(itemReader())
                .writer(itemWriter())
                .listener(new LoggingStepStartStopListener())
                .build();
    }

    @Bean
    public ListItemReader<String> itemReader() {
        ArrayList<String> items = new ArrayList<>(100_000);

        for (int i = 0; i < 100_000; i++) {
            items.add(UUID.randomUUID().toString());
        }

        return new ListItemReader<>(items);
    }

    @Bean
    public ItemWriter<String> itemWriter() {
        return items -> {
            for (String item : items) {
                System.out.println("item = " + item);
            }
        };
    }

    private CompletionPolicy randomCompletionPolicy() {
        return new RandomChunkSizePolicy();
    }

    @Bean
    public CompletionPolicy completionPolicy() {
        CompositeCompletionPolicy policy = new CompositeCompletionPolicy();

        policy.setPolicies(
                new CompletionPolicy[]{
                        new TimeoutTerminationPolicy(3),
                        new SimpleCompletionPolicy(1000)
                }
        );

        return policy;
    }

    public static void main(String[] args) {
        SpringApplication.run(ChunkJob.class, args);
    }

}
