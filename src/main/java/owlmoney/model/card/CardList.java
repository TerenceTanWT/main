package owlmoney.model.card;

import java.util.ArrayList;

import owlmoney.model.card.exception.CardException;
import owlmoney.model.transaction.Transaction;
import owlmoney.model.transaction.exception.TransactionException;
import owlmoney.ui.Ui;

/**
 * CardList class that provides a layer of abstraction for the ArrayList that stores credit cards.
 */

public class CardList {
    private ArrayList<Card> cardLists;

    /**
     * Creates an arrayList of Cards.
     */
    public CardList() {
        cardLists = new ArrayList<Card>();
    }

    /**
     * Adds an instance of card into the CardList.
     *
     * @param newCard a new card object to be added.
     * @param ui required for printing.
     * @throws CardException If duplicate credit card name found.
     */
    public void cardListAddCard(Card newCard, Ui ui) throws CardException {
        if (cardExists(newCard.getName())) {
            throw new CardException("There is already a credit card with the name " + newCard.getName());
        }
        cardLists.add(newCard);
        ui.printMessage("Added new card: ");
        ui.printMessage(newCard.getDetails());
    }

    /**
     * Deletes an instance of a card from the CardList.
     *
     * @param name name of the card to be deleted.
     * @param ui required for printing.
     * @throws CardException If CardList is empty or card to be deleted do not exist.
     */
    public void cardListDeleteCard(String name, Ui ui) throws CardException {
        cardListCheckListEmpty();
        boolean isDeleted = false;
        for (int i = 0; i < cardLists.size(); i++) {
            if (cardLists.get(i).getName().equals(name)) {
                ui.printMessage("Removing " + cardLists.get(i).getName());
                cardLists.remove(i);
                isDeleted = true;
                break;
            }
        }
        if (!isDeleted) {
            throw new CardException("No such card exist for deletion.");
        }
    }

    /**
     * Throws CardException if CardList is empty.
     *
     * @throws CardException If CardList is empty.
     */
    public void cardListCheckListEmpty() throws CardException {
        if (cardLists.size() <= 0) {
            throw new CardException("There are 0 cards in your profile");
        }
    }

    /**
     * Gets the size of the cardList which counts the number of credit cards object within.
     *
     * @return size of cardList.
     */
    private int getCardListSize() {
        return cardLists.size();
    }

    /**
     * Checks if the credit card name that the user specified exists.
     *
     * @param cardName name of credit card.
     * @return the result specifying whether the credit card name already exists.
     */
    private boolean cardExists(String cardName) {
        for (int i = 0; i < getCardListSize(); i++) {
            if (cardName.equals(cardLists.get(i).getName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if new limit exceeds total expenditure spent of card.
     *
     * @param card The card object.
     * @param newLimit The new limit to be changed.
     * @throws CardException If total expenditure spent of card exceeds new limit.
     */
    private void checkLimitNotExceedTotalSpent(Card card, String newLimit) throws CardException {
        double currentCardSpent = card.getLimit() - card.getRemainingLimit();
        if (Double.parseDouble(newLimit) < currentCardSpent) {
            throw new CardException("New limit cannot exceed current card spent of $" + currentCardSpent);
        }
    }

    /**
     * Updates the remaining limit of card.
     *
     * @param card The card object.
     * @param newLimit The new limit to be changed.
     */
    private void updateNewRemainingLimit(Card card, String newLimit) {
        double currentCardSpent = card.getLimit() - card.getRemainingLimit();
        card.setRemainingLimit(Double.parseDouble(newLimit) - currentCardSpent);
    }

    /**
     * Edits the credit card details.
     *
     * @param name     Credit Card to be edited.
     * @param newName  New name of credit card if any.
     * @param limit    New limit of credit card if any.
     * @param rebate   New rebate of credit card if any.
     * @param ui       Required for printing.
     * @throws CardException If card cannot be found.
     */
    public void cardListEditCard(String name, String newName, String limit, String rebate, Ui ui)
            throws CardException {
        for (int i = 0; i < cardLists.size(); i++) {
            if (cardLists.get(i).getName().equals(name)) {
                if (!(newName.isEmpty() || newName.isBlank())) {
                    cardLists.get(i).setName(newName);
                }
                if (!(limit.isEmpty() || limit.isBlank())) {
                    this.checkLimitNotExceedTotalSpent(cardLists.get(i), limit);
                    this.updateNewRemainingLimit(cardLists.get(i), limit);
                    cardLists.get(i).setLimit(Double.parseDouble(limit));
                }
                if (!(rebate.isEmpty() || rebate.isBlank())) {
                    cardLists.get(i).setRebate(Double.parseDouble(rebate));
                }
                ui.printMessage("New details of the cards:\n");
                ui.printMessage(cardLists.get(i).getDetails() + "\n");
                return;
            }
        }
        throw new CardException("Card could not be found for editing card details");
    }

    /**
     * Lists all credit cards details.
     *
     * @param ui Required for printing.
     * @throws CardException If CardList is empty.
     */
    public void cardListListCards(Ui ui) throws CardException {
        cardListCheckListEmpty();
        for (int i = 0; i < cardLists.size(); i++) {
            ui.printMessage((i + 1) + ".\n" + cardLists.get(i).getDetails());
        }
    }

    /**
     * Adds an expenditure tied to a credit card.
     * This will store the expenditure in the ExpenditureList in the credit card.
     *
     * @param cardName The credit card name.
     * @param exp      The instance of the expenditure.
     * @param ui       Required for printing.
     * @throws CardException If the credit card name cannot be found.
     */
    //need change exception class in the future for this
    public void cardListAddExpenditure(String cardName, Transaction exp, Ui ui)
            throws CardException {
        for (int i = 0; i < cardLists.size(); i++) {
            if (cardLists.get(i).getName().equals(cardName)) {
                cardLists.get(i).addInExpenditure(exp, ui);
                return;
            }
        }
        throw new CardException("Card cannot be found for adding expenditure:" + cardName);
    }

    /**
     * Lists expenditures in the credit card.
     *
     * @param cardToList The name of the credit card.
     * @param ui         required for printing.
     * @param displayNum Number of expenditures to list.
     * @throws CardException If the credit card name cannot be found.
     * @throws TransactionException If no expenditure is found or no expenditure is in the list.
     */
    public void cardListListCardExpenditure(String cardToList, Ui ui, int displayNum)
            throws TransactionException, CardException {
        for (int i = 0; i < cardLists.size(); i++) {
            if (cardToList.equals(cardLists.get(i).getName())) {
                cardLists.get(i).listAllExpenditure(ui, displayNum);
                return;
            }
        }
        throw new CardException("Card cannot be found to list expenditure: " + cardToList);
    }

    /**
     * Deletes an expenditure from the transactionList in the bank account.
     *
     * @param expNum The transaction number.
     * @param deleteFromAccountCard The name of the card.
     * @param ui Required for printing.
     * @throws TransactionException If invalid transaction.
     * @throws CardException If card does not exist.
     */
    public void cardListDeleteExpenditure(int expNum, String deleteFromAccountCard, Ui ui)
            throws CardException, TransactionException {
        for (int i = 0; i < cardLists.size(); i++) {
            if (deleteFromAccountCard.equals(cardLists.get(i).getName())) {
                cardLists.get(i).deleteExpenditure(expNum, ui);
                return;
            }
        }
        throw new CardException("Card cannot be found for deleting expenditure: " + deleteFromAccountCard);
    }

    /**
     * Edits an expenditure from the transactionList in the bank account.
     *
     * @param expNum       The transaction number.
     * @param editFromCard The name of the card.
     * @param desc         The description of the expenditure.
     * @param amount       The amount of the expenditure.
     * @param date         The date of the expenditure.
     * @param category     The category of the expenditure.
     * @param ui           Required for printing.
     * @throws CardException If card does not exist.
     * @throws TransactionException If incorrect date format.
     */
    public void cardListEditExpenditure(int expNum, String editFromCard, String desc, String amount,
            String date, String category, Ui ui) throws CardException, TransactionException {
        for (int i = 0; i < cardLists.size(); i++) {
            if (cardLists.get(i).getName().equals(editFromCard)) {
                cardLists.get(i).editExpenditureDetails(expNum, desc, amount, date, category, ui);
                return;
            }
        }
        throw new CardException("Card cannot be found for editing expenditure: " + editFromCard);
    }
}
