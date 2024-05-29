package study.springbatch.chapter7.flatfile;

import org.springframework.batch.item.file.transform.DefaultFieldSetFactory;
import org.springframework.batch.item.file.transform.FieldSet;
import org.springframework.batch.item.file.transform.FieldSetFactory;
import org.springframework.batch.item.file.transform.LineTokenizer;

import java.util.ArrayList;
import java.util.List;

public class CustomerFileLineTokenizer implements LineTokenizer {

    private String delimiter = ",";
    private String[] names = new String[] {
            "firstName",
            "middleInitial",
            "lastName",
            "address",
            "city",
            "state",
            "zipCode"
    };

    private FieldSetFactory fieldSetFactory = new DefaultFieldSetFactory();

    @Override
    public FieldSet tokenize(String record) {

        String[] fields = record.split(delimiter);
        List<String> parsedField = new ArrayList<>();

        for (int i = 0; i < fields.length; i++) {
            if(i == 4) {
                parsedField.set(i - 1,
                        parsedField.get(i - 1) + " " + fields[i]);
            } else {
                parsedField.add(fields[i]);
            }
        }

        return fieldSetFactory.create(parsedField.toArray(new String[0]), names);
    }
}
