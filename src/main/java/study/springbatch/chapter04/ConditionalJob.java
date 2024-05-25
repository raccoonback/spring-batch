package study.springbatch.chapter04;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.job.flow.JobExecutionDecider;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.batch.repeat.CompletionPolicy;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.batch.repeat.policy.CompositeCompletionPolicy;
import org.springframework.batch.repeat.policy.SimpleCompletionPolicy;
import org.springframework.batch.repeat.policy.TimeoutTerminationPolicy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.ArrayList;
import java.util.UUID;

@EnableBatchProcessing
@SpringBootApplication
public class ConditionalJob {

    @Autowired
    private JobBuilderFactory jobBuilderFactory;

    @Autowired
    private StepBuilderFactory stepBuilderFactory;

    @Bean
    public Tasklet passTasklet() {
        return (contribution, chunkContext) -> {
//            return RepeatStatus.FINISHED;
            throw new RuntimeException("Causing a failure");
        };
    }

    @Bean
    public Tasklet successTasklet() {
        return (contribution, context) -> {
            System.out.println("Success!");
            return RepeatStatus.FINISHED;
        };
    }

    @Bean
    public Tasklet failTasklet() {
        return (contribution, context) -> {
            System.out.println("Failure!");
            return RepeatStatus.FINISHED;
        };
    }

    @Bean
    public Job conditionalJob1() {
        return this.jobBuilderFactory.get("conditionalJob1")
                .start(firstStep())
                .on("FAILED").stopAndRestart(successStep())
                .from(firstStep()).on("*").to(successStep()).end()
                .build();
    }


    @Bean
    public Step firstStep() {
        return this.stepBuilderFactory.get("firstStep")
                .tasklet(passTasklet())
                .build();
    }

    @Bean
    public Step successStep() {
        return this.stepBuilderFactory.get("successStep")
                .tasklet(successTasklet())
                .build();
    }

    @Bean
    public Step failStep() {
        return this.stepBuilderFactory.get("failureStep")
                .tasklet(failTasklet())
                .build();
    }

    @Bean
    public JobExecutionDecider decider() {
        return new RandomDecider();
    }


    public static void main(String[] args) {
        SpringApplication.run(ConditionalJob.class, args);
    }

}
