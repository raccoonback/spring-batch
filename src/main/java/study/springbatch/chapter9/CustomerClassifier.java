package study.springbatch.chapter9;

import org.springframework.batch.item.ItemWriter;
import org.springframework.classify.Classifier;

public class CustomerClassifier implements Classifier<Customer, ItemWriter<? super Customer>> {

    private ItemWriter<Customer> fileItemWriter;
    private ItemWriter<Customer> jdbcItemWriter;

    public CustomerClassifier(ItemWriter<Customer> fileItemWriter, ItemWriter<Customer> jdbcItemWriter) {
        this.fileItemWriter = fileItemWriter;
        this.jdbcItemWriter = jdbcItemWriter;
    }

    @Override
    public ItemWriter<? super Customer> classify(Customer customer) {
        if (customer.getState().matches("^[A-M].*]")) {
            return fileItemWriter;
        } else {
            return jdbcItemWriter;
        }
    }

}
