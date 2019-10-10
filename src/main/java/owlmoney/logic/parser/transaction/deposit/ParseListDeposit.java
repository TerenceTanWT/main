package owlmoney.logic.parser.transaction.deposit;

import java.util.Iterator;

import owlmoney.logic.command.Command;
import owlmoney.logic.command.transaction.ListDepositCommand;
import owlmoney.logic.parser.exception.ParserException;

/**
 * Represents the parsing of inputs for listing deposits.
 */
public class ParseListDeposit extends ParseDeposit {
    private static final String LIST = "/list";

    /**
     * Constructor which creates an instance of ParseListDeposit.
     *
     * @param data Raw user input data.
     * @throws ParserException If there are redundant parameters.
     */
    public ParseListDeposit(String data) throws ParserException {
        super(data);
        checkRedundantParameter(TO, LIST);
        checkRedundantParameter(AMOUNT, LIST);
        checkRedundantParameter(DATE, LIST);
        checkRedundantParameter(DESCRIPTION, LIST);
        checkRedundantParameter(TRANSNO, LIST);
    }

    /**
     * Checks each user input for each parameter.
     *
     * @throws ParserException If the user input is invalid.
     */
    public void checkParameter() throws ParserException {
        Iterator<String> savingsIterator = expendituresParameters.keySet().iterator();

        while (savingsIterator.hasNext()) {
            String key = savingsIterator.next();
            String value = expendituresParameters.get(key);
            if (FROM.equals(key) && (value.isBlank() || value.isEmpty())) {
                throw new ParserException(key + " cannot be empty when listing deposits from a bank");
            } else if (NUM.equals(key) && (value.isBlank() || value.isEmpty())) {
                expendituresParameters.put(key, "30");
            } else if (NUM.equals(key)) {
                checkInt(NUM, expendituresParameters.get(NUM));
                if (Integer.parseInt(expendituresParameters.get(NUM)) <= 0) {
                    throw new ParserException("/num must be at least 1");
                }
            }
        }
    }

    /**
     * Returns the command to execute the listing of deposits.
     *
     * @return ListDepositCommand to be executed.
     */
    public Command getCommand() {
        ListDepositCommand newListDepositCommand = new ListDepositCommand(expendituresParameters.get(FROM),
                Integer.parseInt(expendituresParameters.get(NUM)));
        return newListDepositCommand;
    }
}
