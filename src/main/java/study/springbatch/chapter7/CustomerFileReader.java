package study.springbatch.chapter7;

import org.springframework.batch.item.*;
import org.springframework.batch.item.file.ResourceAwareItemReaderItemStream;
import org.springframework.core.io.Resource;

import java.util.ArrayList;

//public class CustomerFileReader implements ResourceAwareItemReaderItemStream<Customer> {
//
//    private Object curItem = null;
//
//    private ResourceAwareItemReaderItemStream<Customer> delegate;
//
//    public CustomerFileReader(ResourceAwareItemReaderItemStream<Customer> delegate) {
//        this.delegate = delegate;
//    }
//
//    @Override
//    public Customer read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
//        if(curItem == null) {
//            curItem = delegate.read();
//        }
//
//        Customer item = (Customer) curItem;
//        curItem = null;
//
//        if(item != null) {
//            item.setTransactions(new ArrayList<>());
//
//            while(peek() instanceof  Transaction) {
//                item.getTransactions().add((Transaction) curItem);
//                curItem = null;
//            }
//        }
//
//        return item;
//    }
//
//    private Object peek() throws Exception {
//        if(curItem == null) {
//            curItem = delegate.read();
//        }
//        return curItem;
//    }
//
//    @Override
//    public void open(ExecutionContext executionContext) throws ItemStreamException {
//        delegate.open(executionContext);
//    }
//
//    @Override
//    public void update(ExecutionContext executionContext) throws ItemStreamException {
//        delegate.update(executionContext);
//    }
//
//    @Override
//    public void close() throws ItemStreamException {
//        delegate.close();
//    }
//
//    @Override
//    public void setResource(Resource resource) {
//        this.delegate.setResource(resource);
//    }
//}
