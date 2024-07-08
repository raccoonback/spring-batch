package study.springbatch.chapter12;

import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.listener.JobExecutionListenerSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.util.StreamUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class DownloadingJobExecutionListener extends JobExecutionListenerSupport {

    @Autowired
    private ResourcePatternResolver resourcePatternResolver;

    @Value("${job.resource-path}")
    private String path;

    @Override
    public void beforeJob(JobExecution jobExecution) {
        try {
            Resource[] resources = this.resourcePatternResolver.getResources(this.path);

            StringBuilder paths = new StringBuilder();
            for(Resource resource : resources) {
                File file = File.createTempFile("input", ".csv");

                StreamUtils.copy(resource.getInputStream(), new FileOutputStream(file));

                paths.append(file.getAbsolutePath() + ",");
                System.out.println(">> downloaded file: " + file.getAbsolutePath());
            }

            jobExecution.getExecutionContext()
                    .put("localFiles", paths.substring(0, paths.length() - 1));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
