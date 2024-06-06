package study.springbatch.chapter7;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.data.RepositoryItemReader;
import org.springframework.batch.item.data.builder.RepositoryItemReaderBuilder;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.mapping.PatternMatchingCompositeLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.batch.item.file.transform.LineTokenizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.Sort;
import org.springframework.jdbc.core.ArgumentPreparedStatementSetter;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@EnableBatchProcessing
@SpringBootApplication
public class BatchConfigurations {

    @Autowired
    private JobBuilderFactory jobBuilderFactory;

    @Autowired
    private StepBuilderFactory stepBuilderFactory;

//    @Bean
//    @StepScope
//    public FlatFileItemReader<Customer> customerItemReader(@Value("#{jobParameters['customerFile']}") Resource inputFile) {
//        return new FlatFileItemReaderBuilder<Customer>()
//                .name("customerInputReader")
//                .resource(inputFile)
//                .fixedLength()
//                .columns(
//                        new Range[] {
//                                new Range(1, 11),
//                                new Range(12, 12),
//                                new Range(13, 22),
//                                new Range(23, 26),
//                                new Range(27, 46),
//                                new Range(47, 62),
//                                new Range(63, 64),
//                                new Range(65, 69)
//                        }
//                )
//                .names(
//                        new String[] {
//                                "firstName", "middleInitial", "lastName", "addressNumber",
//                                "street", "city", "state", "zipCode"
//                        }
//                )
//                .targetType(Customer.class)
//                .build();
//    }

//    @Bean
//    @StepScope
//    public FlatFileItemReader<Customer> customerItemReader(@Value("#{jobParameters['customerFile']}") Resource inputFile) {
//        return new FlatFileItemReaderBuilder<Customer>()
//                .name("customerItemReader")
//                .delimited()
//                .names(
//                        new String[] {
//                                "firstName",
//                                "middleInitial",
//                                "lastName",
//                                "addressNumber",
//                                "street",
//                                "city",
//                                "state",
//                                "zipCode"
//                        }
//                )
//                .fieldSetMapper(new CustomerFieldSetMapper())
//                .resource(inputFile)
//                .build();
//    }

//    @Bean
//    @StepScope
//    public FlatFileItemReader customerItemReader() {
//        return new FlatFileItemReaderBuilder<>()
//                .name("customerItemReader")
//                .lineMapper(lineTokenizer())
//                .build();
//    }

//    @Bean
//    @StepScope
//    public MultiResourceItemReader multiResourceItemReader(@Value("#{jobParameters['customerFile']}") Resource[] inputFiles) {
//        return new MultiResourceItemReaderBuilder<>()
//                .name("multiCustomerReader")
//                .resources(inputFiles)
//                .delegate(customerFileReader())
//                .build();
//    }

//    @Bean
//    public CustomerFileReader customerFileReader() {
//        return new CustomerFileReader(customerItemReader());
//    }

//    @Bean
//    @StepScope
//    public StaxEventItemReader<Customer> customerFileReader(@Value("#{jobParameters['customerFile']}") Resource inputFile) {
//        return new StaxEventItemReaderBuilder<Customer>()
//                .name("customerFileReader")
//                .resource(inputFile)
//                .addFragmentRootElements("customer")
//                .unmarshaller(customerMarshaller())
//                .build();
//    }

    @Bean
    public Jaxb2Marshaller customerMarshaller() {
        Jaxb2Marshaller jaxb2Marshaller = new Jaxb2Marshaller();

        jaxb2Marshaller.setClassesToBeBound(Customer.class, Transaction.class);

        return jaxb2Marshaller;
    }

//    @Bean
//    @StepScope
//    public JsonItemReader<Customer> customerFileReader(@Value("#{jobParameters['customerFile']}") Resource inputFile) {
//
//        ObjectMapper objectMapper = new ObjectMapper();
//        objectMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd hh:mm:ss"));
//
//        JacksonJsonObjectReader<Customer> jsonObjectReader = new JacksonJsonObjectReader<>(Customer.class);
//        jsonObjectReader.setMapper(objectMapper);
//
//        return new JsonItemReaderBuilder<Customer>()
//                .name("customerFileReader")
//                .jsonObjectReader(jsonObjectReader)
//                .resource(inputFile)
//                .build();
//    }

//    @Bean
//    public JdbcCursorItemReader<Customer> customerItemReader(DataSource dataSource) {
//        return new JdbcCursorItemReaderBuilder<Customer>()
//                .name("customerItemReader")
//                .dataSource(dataSource)
//                .sql("select * from CUSTOMER where city = ?")
//                .rowMapper(new CustomerRowMapper())
//                .preparedStatementSetter(citySetter(null))
//                .build();
//    }

//    @Bean
//    @StepScope
//    public JdbcPagingItemReader<Customer> customerItemReader(DataSource dataSource, PagingQueryProvider queryProvider, @Value("#{jobParameters['city']}") String city) {
//
//        Map<String, Object> parameterValues = new HashMap<>();
//        parameterValues.put("city", city);
//
//        return new JdbcPagingItemReaderBuilder<Customer>()
//                .name("customerItemReader")
//                .dataSource(dataSource)
//                .queryProvider(queryProvider)
//                .parameterValues(parameterValues)
//                .pageSize(10)
//                .rowMapper(new CustomerRowMapper())
//                .build();
//    }
//
//    @Bean
//    public SqlPagingQueryProviderFactoryBean pagingQueryProvider(DataSource dataSource) {
//        SqlPagingQueryProviderFactoryBean factoryBean = new SqlPagingQueryProviderFactoryBean();
//
//        factoryBean.setSelectClause("select *");
//        factoryBean.setFromClause("from CUSTOMER");
//        factoryBean.setWhereClause("where city = :city");
//        factoryBean.setSortKey("lastName");
//        factoryBean.setDataSource(dataSource);
//
//        return factoryBean;
//    }

//    @Bean
//    @StepScope
//    public HibernatePagingItemReader<Customer> customerItemReader(EntityManagerFactory entityManagerFactory, @Value("#{jobParameters['city']}") String city) {
//
//        return new HibernatePagingItemReaderBuilder<Customer>()
//                .name("customerItemReader")
//                .sessionFactory(entityManagerFactory.unwrap(SessionFactory.class))
//                .queryString("from Customer where city = :city")
//                .parameterValues(Collections.singletonMap("city", city))
//                .pageSize(10)
//                .build();
//    }

//    @Bean
//    @StepScope
//    public JpaPagingItemReader<Customer> customerItemReader(EntityManagerFactory entityManagerFactory, @Value("#{jobParameters['city']}") String city) {
//        return new JpaPagingItemReaderBuilder<Customer>()
//                .name("customerItemReader")
//                .entityManagerFactory(entityManagerFactory)
//                .queryString("select c from Customer c where c.city = : city")
//                .parameterValues(Collections.singletonMap("city", city))
//                .build();
//    }

//    @Bean
//    @StepScope
//    public StoredProcedureItemReader<Customer> customerItemReader(DataSource dataSource, @Value("#jobParameters['city']") String city) {
//        return new StoredProcedureItemReaderBuilder<Customer>()
//                .name("customerItemReader")
//                .dataSource(dataSource)
//                .procedureName("customer_list")
//                .parameters(new SqlParameter[] {
//                        new SqlParameter("cityOption", Types.VARCHAR)
//                })
//                .preparedStatementSetter(
//                        new ArgumentPreparedStatementSetter((new Object[] {city}))
//                )
//                .rowMapper(new CustomerRowMapper())
//                .build();
//    }

//    @Bean
//    @StepScope
//    public RepositoryItemReader<Customer> customerItemReader(CustomerRepository repository, @Value("#{jobParameters['city']}") String city) {
//        return new RepositoryItemReaderBuilder<Customer>()
//                .name("customerItemReader")
//                .arguments(Collections.singletonList(city))
//                .methodName("findByCity")
//                .repository(repository)
//                .sorts(Collections.singletonMap("lastName" , Sort.Direction.ASC))
//                .build();
//    }

    @Bean
    public CustomerItemReader customerItemReader() {
        CustomerItemReader customerItemReader = new CustomerItemReader();
        customerItemReader.setName("customerItemReader");
        return customerItemReader;
    }

    @Bean
    public CustomerItemListener customerItemListener() {
        return new CustomerItemListener();
    }

    @Bean
    public EmptyInputStepFailer emptyInputStepFailer() {
        return new EmptyInputStepFailer();
    }

    @Bean
    @StepScope
    public ArgumentPreparedStatementSetter citySetter(@Value("#{jobParameters['city']}") String city) {
        return new ArgumentPreparedStatementSetter(new Object[] {city});
    }

    @Bean
    public PatternMatchingCompositeLineMapper lineTokenizer() {
        Map<String, LineTokenizer> lineTokenizers = new HashMap<>();

        lineTokenizers.put("CUST*", customerLineTokenizer());
        lineTokenizers.put("TRANS*", transactionLineTokenizer());

        Map<String, FieldSetMapper> fieldSetMappers = new HashMap<>(2);

        BeanWrapperFieldSetMapper<Customer> customerFieldSetMapper = new BeanWrapperFieldSetMapper<>();
        customerFieldSetMapper.setTargetType(Customer.class);

        fieldSetMappers.put("CUST*", customerFieldSetMapper);
        fieldSetMappers.put("TRANS*", new TransactionFieldSetMapper());

        PatternMatchingCompositeLineMapper lineMappers = new PatternMatchingCompositeLineMapper();

        lineMappers.setTokenizers(lineTokenizers);
        lineMappers.setFieldSetMappers(fieldSetMappers);

        return lineMappers;
    }

    @Bean
    public DelimitedLineTokenizer customerLineTokenizer() {
        DelimitedLineTokenizer lineTokenizer = new DelimitedLineTokenizer();

        lineTokenizer.setNames(
                "firstName",
                "middleInitial",
                "lastName",
                "address",
                "city",
                "state",
                "zipCode"
        );

        lineTokenizer.setIncludedFields(1, 2, 3, 4, 5, 6, 7);

        return lineTokenizer;
    }

    @Bean
    public DelimitedLineTokenizer transactionLineTokenizer() {
        DelimitedLineTokenizer lineTokenizer = new DelimitedLineTokenizer();
        lineTokenizer.setNames("prefix", "accountNumber", "transactionDate", "amount");
        return lineTokenizer;
    }

    @Bean
    public ItemWriter<Customer> itemWriter() {
        return (items) -> items.forEach(System.out::println);
    }

    @Bean
    public Step copyFileStep() {
        return this.stepBuilderFactory.get("copyFileStep")
                .<Customer, Customer>chunk(10)
                .reader(customerItemReader())
                .writer(itemWriter())
                .faultTolerant()
                .skipLimit(100)
                .skip(Exception.class)
                .listener(emptyInputStepFailer())
                .build();
    }

    @Bean
    public Job job() {
        return this.jobBuilderFactory.get("job")
                .start(copyFileStep())
                .build();
    }

    public static void main(String[] args) {
        SpringApplication.run(BatchConfigurations.class, args);
    }
}
