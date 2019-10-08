package owlmoney.logic.parser.transaction.expenditure;

import java.util.Iterator;

import owlmoney.logic.command.Command;
import owlmoney.logic.command.transaction.AddExpenditureCommand;
import owlmoney.logic.parser.exception.ParserException;

public class ParseAddExpenditure extends ParseExpenditure {

    static final String ADD = "/add";

    public ParseAddExpenditure(String data) throws ParserException {
        super(data);
        checkRedundantParameter(TRANSNO, ADD);
        checkRedundantParameter(NUM, ADD);
        checkFirstParameter();
    }

    public void checkParameter() throws ParserException {
        // Getting an iterator
        Iterator<String> savingsIterator = expendituresParameters.keySet().iterator();

        while (savingsIterator.hasNext()) {
            String key = savingsIterator.next();
            String value = expendituresParameters.get(key);
            if (!TRANSNO.equals(key) && !NUM.equals(key) && !CATEGORY.equals(key)
                    && (value.isBlank() || value.isEmpty())) {
                throw new ParserException(key + " cannot be empty when adding a new expenditure");
            } else if (CATEGORY.equals(key) && "deposit".equals(value)) {
                throw new ParserException(key + " cannot be deposit when adding a new expenditure");
            } else if (CATEGORY.equals(key) && (value.isBlank() || value.isEmpty())) {
                expendituresParameters.put(CATEGORY, "miscellaneous");
            }
            if (AMOUNT.equals(key)) {
                checkIfDouble(value);
            }
        }
    }

    public Command getCommand() {
        AddExpenditureCommand newAddExpenditureCommand = new AddExpenditureCommand(expendituresParameters.get(FROM),
                Double.parseDouble(expendituresParameters.get(AMOUNT)), (expendituresParameters.get(DATE)),
                (expendituresParameters.get(DESCRIPTION)), (expendituresParameters.get(CATEGORY)));
        return newAddExpenditureCommand;
    }
}
