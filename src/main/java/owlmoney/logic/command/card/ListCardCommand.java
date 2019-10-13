package owlmoney.logic.command.card;

import owlmoney.logic.command.Command;
import owlmoney.logic.parser.exception.CardException;
import owlmoney.model.profile.Profile;
import owlmoney.ui.Ui;

/**
 * ListCardCommand class which contains the execution function to list card objects.
 */
public class ListCardCommand extends Command {

    /**
     * Executes the function to list cards in the profile.
     *
     * @param profile Profile of the user.
     * @param ui Ui of OwlMoney.
     * @return false so OwlMoney will not terminate yet.
     */
    @Override
    public boolean execute(Profile profile, Ui ui) throws CardException {
        profile.listCards(ui);
        return this.isExit;
    }
}
