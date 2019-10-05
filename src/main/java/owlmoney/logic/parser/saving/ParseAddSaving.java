package owlmoney.logic.parser.saving;

import java.util.Iterator;

import owlmoney.logic.command.Command;

import owlmoney.logic.command.bank.AddSavingsCommand;
import owlmoney.logic.parser.exception.ParserException;

public class ParseAddSaving extends ParseSaving {

    public ParseAddSaving(String data) throws ParserException {
        super(data);
        checkRedundantParameter(NEW_NAME);
    }

    public void checkParameter() throws ParserException {
        // Getting an iterator
        Iterator<String> savingsIterator = savingsParameters.keySet().iterator();

        while (savingsIterator.hasNext()) {
            String key = savingsIterator.next();
            String value = savingsParameters.get(key);
            if (!NEW_NAME.equals(key) && (value.isBlank() || value.isEmpty())) {
                throw new ParserException(key + " cannot be empty when adding savings account");
            }
            if (INCOME.equals(key) || AMOUNT.equals(key)) {
                checkIfDouble(key, value);
            }
        }
    }

    //current name is just a place holder. This is to create the command and execute it
    //might need to restructure in future
    public Command getCommand() {
        AddSavingsCommand newAddSavingsCommand = new AddSavingsCommand(savingsParameters.get(NAME),
                Double.parseDouble(savingsParameters.get(INCOME)),
                Double.parseDouble(savingsParameters.get(AMOUNT)));
        return newAddSavingsCommand;
    }

}
