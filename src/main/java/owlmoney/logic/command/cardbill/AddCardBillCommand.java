package owlmoney.logic.command.cardbill;

import java.time.YearMonth;
import java.util.Date;

import owlmoney.logic.command.Command;
import owlmoney.model.bank.exception.BankException;
import owlmoney.model.card.exception.CardException;
import owlmoney.model.profile.Profile;
import owlmoney.model.transaction.Expenditure;
import owlmoney.model.transaction.Transaction;
import owlmoney.ui.Ui;

/**
 * AddCardBillCommand class which contains the functions to add a new card bill object.
 */
public class AddCardBillCommand extends Command {
    private final String card;
    private final YearMonth cardDate;
    private final Date expDate;
    private final String to;
    private final String type;
    private final String description;
    private final String category;

    /**
     * Constructor to create an instance of AddExpenditureCommand.
     *
     * @param card  Credit card name of bill to be paid.
     * @param date  Month and year of bill to be paid.
     * @param to    Bank account name to charge the credit card bill to.
     */
    public AddCardBillCommand(String card, YearMonth date, String to) {
        this.card = card;
        this.cardDate = date;
        this.expDate = new Date();
        this.to = to;
        this.type = "bank";
        this.description = "Payment for Credit Card Bill - " + card + " " + date;
        this.category = "Credit Card Bill";
    }

    public boolean execute(Profile profile, Ui ui) throws CardException, BankException {
        double amount = profile.getCardBillAmount(card, cardDate);
        Transaction newExpenditure = new Expenditure(this.description, amount, this.expDate, this.category);
        profile.payCardBill(card, to, newExpenditure, cardDate, ui, type);
        return this.isExit;
    }
}
