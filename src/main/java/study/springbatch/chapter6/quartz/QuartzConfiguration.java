package study.springbatch.chapter6.quartz;

import org.quartz.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class QuartzConfiguration {

//    @Bean
//    public JobDetail quartzJobDetail() {
//        return JobBuilder.newJob(BatchScheduledJob.class)
//                .storeDurably()
//                .build();
//    }
//
//    @Bean
//    public Trigger jobTrigger() {
//        SimpleScheduleBuilder scheduleBuilder = SimpleScheduleBuilder.simpleSchedule()
//                .withIntervalInSeconds(5)
//                .withRepeatCount(4);
//
//        return TriggerBuilder.newTrigger()
//                .forJob(quartzJobDetail())
//                .withSchedule(scheduleBuilder)
//                .build();
//
//    }
}
