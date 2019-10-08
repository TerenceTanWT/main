package owlmoney.logic.parser.transaction.expenditure;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import owlmoney.logic.parser.ParseRawData;
import owlmoney.logic.parser.exception.ParserException;

public abstract class ParseExpenditure {
    HashMap<String, String> expendituresParameters = new HashMap<String, String>();
    private ParseRawData parseRawData = new ParseRawData();
    private String rawData;
    private static final String[] EXPENDITURE_KEYWORD = new String[] {
            "/amount", "/date", "/desc", "/category", "/from", "/transno", "/num"
    };
    private static final List<String> EXPENDITURE_KEYWORD_LISTS = Arrays.asList(EXPENDITURE_KEYWORD);
    static final String AMOUNT = "/amount";
    static final String DATE = "/date";
    static final String DESCRIPTION = "/desc";
    static final String CATEGORY = "/category";
    static final String FROM = "/from";
    static final String TRANSNO = "/transno";
    static final String NUM = "/num";

    ParseExpenditure(String data) {
        this.rawData = data;
    }

    void checkRedundantParameter(String parameter, String command) throws ParserException {
        if (rawData.contains(parameter)) {
            throw new ParserException(command + " /expenditure should not contain " + parameter);
        }
    }

    void checkFirstParameter() throws ParserException {
        String[] rawDateSplit = rawData.split(" ", 2);
        if(!EXPENDITURE_KEYWORD_LISTS.contains(rawDateSplit[0])) {
            throw new ParserException("Incorrect command syntax");
        }
    }

    public void fillHashTable() throws ParserException {
        expendituresParameters.put(AMOUNT,
                parseRawData.extractParameter(rawData, AMOUNT, EXPENDITURE_KEYWORD));
        expendituresParameters.put(DATE,
                parseRawData.extractParameter(rawData, DATE, EXPENDITURE_KEYWORD));
        expendituresParameters.put(DESCRIPTION,
                parseRawData.extractParameter(rawData, DESCRIPTION, EXPENDITURE_KEYWORD));
        expendituresParameters.put(CATEGORY,
                parseRawData.extractParameter(rawData, CATEGORY, EXPENDITURE_KEYWORD));
        expendituresParameters.put(FROM,
                parseRawData.extractParameter(rawData, FROM, EXPENDITURE_KEYWORD));
        expendituresParameters.put(TRANSNO,
                parseRawData.extractParameter(rawData, TRANSNO, EXPENDITURE_KEYWORD));
        expendituresParameters.put(NUM,
                parseRawData.extractParameter(rawData, NUM, EXPENDITURE_KEYWORD));
    }

    void checkIfDouble(String valueString) throws ParserException {
        try {
            Double value = Double.parseDouble(valueString);
        } catch (NumberFormatException e) {
            throw new ParserException("Amount can only be numbers with at most 2 decimal places");
        }
    }

    void checkIfInt(String variable, String valueString) throws ParserException {
        try {
            int value = Integer.parseInt(valueString);
        } catch (NumberFormatException e) {
            throw new ParserException(variable + " can only be integers");
        }
    }

    public abstract void checkParameter() throws ParserException;
}
