package study.springbatch.chapter6.pause;

import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.annotation.AfterStep;
import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.batch.item.*;
import org.springframework.batch.item.file.transform.FieldSet;

public class TransactionReader implements ItemStreamReader<Transaction> {

    private ItemStreamReader<FieldSet> fieldSetReader;
    private int recordCount = 0;
    private int expectedRecordCount = 0;

    private StepExecution stepExecution;

    public TransactionReader(ItemStreamReader<FieldSet> fieldSetReader) {
        this.fieldSetReader = fieldSetReader;
    }

    @Override
    public Transaction read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
//        if(this.recordCount == 2) {
//            throw new ParseException("This isn't what I hoped to happen");
//        }

        return process(fieldSetReader.read());
    }

    private Transaction process(FieldSet fieldSet) {
        Transaction result = null;

        if(fieldSet != null) {
            if(fieldSet.getFieldCount() > 1) {
                result = new Transaction();
                result.setAccountNumber(fieldSet.readString(0));
                result.setTimestamp(fieldSet.readDate(1, "yyyy-MM-DD HH:mm:ss"));
                result.setAmount(fieldSet.readDouble(2));

                recordCount++;
            } else {
                expectedRecordCount = fieldSet.readInt(0);

                if(expectedRecordCount != this.recordCount) {
                    this.stepExecution.setTerminateOnly();
                }
            }
        }

        return result;
    }

    public void setFieldSetReader(ItemStreamReader<FieldSet> fieldSetReader) {
        this.fieldSetReader = fieldSetReader;
    }

//    @AfterStep
//    public ExitStatus afterStep(StepExecution execution) {
//        if(recordCount == expectedRecordCount) {
//            return execution.getExitStatus();
//        }
//
//        return ExitStatus.STOPPED;
//    }

    @BeforeStep
    public void beforeStep(StepExecution execution) {
        this.stepExecution = execution;
    }

    @Override
    public void open(ExecutionContext executionContext) throws ItemStreamException {
        this.fieldSetReader.open(executionContext);
    }

    @Override
    public void update(ExecutionContext executionContext) throws ItemStreamException {
        this.fieldSetReader.update(executionContext);
    }

    @Override
    public void close() throws ItemStreamException {
        this.fieldSetReader.close();
    }
}
