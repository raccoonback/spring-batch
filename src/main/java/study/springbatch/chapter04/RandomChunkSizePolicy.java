package study.springbatch.chapter04;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.batch.repeat.CompletionPolicy;
import org.springframework.batch.repeat.RepeatContext;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.batch.repeat.policy.CompositeCompletionPolicy;
import org.springframework.batch.repeat.policy.SimpleCompletionPolicy;
import org.springframework.batch.repeat.policy.TimeoutTerminationPolicy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.ArrayList;
import java.util.Random;
import java.util.UUID;


public class RandomChunkSizePolicy implements CompletionPolicy {
    private int chunkSize;
    private int totalProcessed;
    private Random random = new Random();


    @Override
    public boolean isComplete(RepeatContext context, RepeatStatus result) {
        if(RepeatStatus.FINISHED == result) {
            return true;
        } else {
            return isComplete(context);
        }
    }

    @Override
    public boolean isComplete(RepeatContext repeatContext) {
        return this.totalProcessed >= chunkSize;
    }

    @Override
    public RepeatContext start(RepeatContext parent) {
        this.chunkSize = random.nextInt(20);
        this.totalProcessed = 0;

        System.out.println("this.chunkSize = " + this.chunkSize);

        return parent;
    }

    @Override
    public void update(RepeatContext context) {
        this.totalProcessed++;
    }
}
