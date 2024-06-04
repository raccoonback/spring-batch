package study.springbatch.chapter6.quartz;

//public class BatchScheduledJob extends QuartzJobBean {
//
//    @Autowired
//    private Job job;
//
//    @Autowired
//    private JobExplorer jobExplorer;
//
//    @Autowired
//    private JobLauncher jobLauncher;
//
//    @Override
//    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
//        JobParameters jobParameters = new JobParametersBuilder(this.jobExplorer)
//                .getNextJobParameters(this.job)
//                .toJobParameters();
//
//        try {
//            this.jobLauncher.run(this.job, jobParameters);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//}
