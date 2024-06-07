package study.springbatch.chapter8;


import org.springframework.batch.item.ItemProcessor;
import org.springframework.classify.Classifier;

public class ZipCodeClassifier implements Classifier<Customer, ItemProcessor<Customer, Customer>> {

    private ItemProcessor<Customer, Customer> oddItemProcessor;
    private ItemProcessor<Customer, Customer> eventItemProcessor;

    public ZipCodeClassifier(ItemProcessor<Customer, Customer> oddItemProcessor, ItemProcessor<Customer, Customer> eventItemProcessor) {
        this.oddItemProcessor = oddItemProcessor;
        this.eventItemProcessor = eventItemProcessor;
    }

    @Override
    public ItemProcessor<Customer, Customer> classify(Customer customer) {
        if(Integer.parseInt(customer.getZip()) % 2 == 0) {
            return eventItemProcessor;
        } else {
            return oddItemProcessor;
        }
    }
}
