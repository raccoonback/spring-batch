package study.springbatch.chapter11.message;

import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.partition.support.MultiResourcePartitioner;
import org.springframework.batch.integration.config.annotation.EnableBatchIntegration;
import org.springframework.batch.integration.partition.RemotePartitioningMasterStepBuilderFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.Resource;
import org.springframework.integration.amqp.dsl.Amqp;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;


@Configuration
@Profile("master")
@EnableBatchIntegration
public class MasterConfiguration {

    private final JobBuilderFactory jobBuilderFactory;

    private final RemotePartitioningMasterStepBuilderFactory masterStepBuilderFactory;

    public MasterConfiguration(JobBuilderFactory jobBuilderFactory, RemotePartitioningMasterStepBuilderFactory masterStepBuilderFactory) {
        this.jobBuilderFactory = jobBuilderFactory;
        this.masterStepBuilderFactory = masterStepBuilderFactory;
    }

    @Bean
    public DirectChannel requests() {
        return new DirectChannel();
    }

    @Bean
    public IntegrationFlow outboundFlow(AmqpTemplate amqpTemplate) {
        return IntegrationFlows.from(requests())
                .handle(Amqp.outboundAdapter(amqpTemplate))
                .channel("requests")
                .get();
    }

    @Bean
    public DirectChannel replies() {
        return new DirectChannel();
    }

    @Bean
    public IntegrationFlow inboundFlow(ConnectionFactory connectionFactory) {
        return IntegrationFlows.from(Amqp.inboundAdapter(connectionFactory, "replies"))
                .channel(replies())
                .get();
    }

    @Bean
    @StepScope
    public MultiResourcePartitioner partitioner(@Value("#{jobParameters['inputFiles']}") Resource[] resources) {
        MultiResourcePartitioner partitioner = new MultiResourcePartitioner();

        partitioner.setKeyName("file");
        partitioner.setResources(resources);

        return partitioner;
    }

    @Bean
    public Step masterStep() {
        return masterStepBuilderFactory.get("masterStep")
                .partitioner("workerStep", partitioner(null))
                .outputChannel(requests())
                .inputChannel(replies())
                .build();
    }

    @Bean
    public Job remotePartitioningJob() {
        return jobBuilderFactory.get("remotePartitioningJob")
                .start(masterStep())
                .build();
    }
}
