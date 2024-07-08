package study.springbatch.chapter12;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.MultiResourceItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.web.client.RestTemplate;

import javax.sql.DataSource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Configuration
public class JobConfiguration {

    @Autowired
    private StepBuilderFactory stepBuilderFactory;

    @Autowired
    private JobBuilderFactory jobBuilderFactory;

    @Bean
    public DownloadingJobExecutionListener downloadingJobExecutionListener() {
        return new DownloadingJobExecutionListener();
    }

    @Bean
    @StepScope
    public MultiResourceItemReader reader(@Value("#{jobParameters['localFiles']}") String paths) throws IOException {

        System.out.println(">> paths = " + paths);
        MultiResourceItemReader<Foo> reader = new MultiResourceItemReader<>();

        reader.setName("multiReader");
        reader.setDelegate(delegate());

        String[] parsedPaths = paths.split(",");
        System.out.println(">> parsedPaths = " + parsedPaths.length);

        List<Resource> resources = new ArrayList<>(parsedPaths.length);
        for (String parsedPath : parsedPaths) {
            Resource resource = new FileSystemResource(parsedPath);
            System.out.println(">> resource = " + resource.getURI());

            resources.add(resource);
        }

        reader.setResources(resources.toArray(new Resource[resources.size()]));

        return reader;
    }

    @Bean
    @StepScope
    public FlatFileItemReader<Foo> delegate() {
        FlatFileItemReader<Foo> reader = new FlatFileItemReaderBuilder<Foo>()
                .name("fooReader")
                .delimited()
                .names(new String[]{"first", "second", "third"})
                .targetType(Foo.class)
                .build();

        return reader;
    }

    @Bean
    @StepScope
    public EnrichmentProcessor processor() {
        return new EnrichmentProcessor();
    }

    @Bean
    public JdbcBatchItemWriter<Foo> writer(DataSource dataSource) {
        return new JdbcBatchItemWriterBuilder<Foo>()
                .dataSource(dataSource)
                .beanMapped()
                .sql("INSERT INTO foo VALUES (:first, :second, :third, :message)")
                .build();
    }

    @Bean
    public Step load() {
        return stepBuilderFactory.get("load")
                .<Foo, Foo>chunk(20)
                .reader(reader(null))
                .processor(processor())
                .writer(writer(null))
                .build();
    }

    @Bean
    public Job job(JobExecutionListener jobExecutionListener) {
        return jobBuilderFactory.get("s3jdbc")
                .listener(jobExecutionListener)
                .start(load())
                .build();
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
