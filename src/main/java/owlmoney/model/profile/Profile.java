package owlmoney.model.profile;

import java.text.ParseException;
import java.time.YearMonth;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import owlmoney.model.bank.Investment;
import owlmoney.model.bank.Saving;
import owlmoney.model.card.exception.CardException;
import owlmoney.model.bank.Bank;
import owlmoney.model.bank.BankList;
import owlmoney.model.bank.exception.BankException;
import owlmoney.model.bond.Bond;
import owlmoney.model.bond.exception.BondException;
import owlmoney.model.card.Card;
import owlmoney.model.card.CardList;
import owlmoney.model.goals.Goals;
import owlmoney.model.goals.GoalsList;
import owlmoney.model.goals.exception.GoalsException;
import owlmoney.model.profile.exception.ProfileException;
import owlmoney.model.transaction.Deposit;
import owlmoney.model.transaction.Expenditure;
import owlmoney.model.transaction.Transaction;
import owlmoney.model.transaction.exception.TransactionException;
import owlmoney.storage.Storage;
import owlmoney.ui.Ui;

/**
 * Stores details of the user which includes bank accounts, cards, names.
 */
public class Profile {
    private String username;
    private BankList bankList;
    private CardList cardList;
    private GoalsList goalsList;
    private Storage storage;
    private Ui ui;

    private static final String BANK = "bank";
    private static final String SAVING = "saving";
    private static final String BONDS = "bonds";
    private static final String INVESTMENT = "investment";
    private static final String CARD = "card";
    private static final String ISBANK = "savings transfer";
    private static final String ISINVESTMENT = "investment transfer";
    private static final String TRANSFERCATEGORY = "Fund Transfer";
    private static final String DEPOSITCATEGORY = "Deposit";
    private static final String FILE_PATH = "data/";
    private static final String PROFILE_BANK_LIST_FILE_NAME = "profile_banklist.csv";
    private static final String PROFILE_GOAL_LIST_FILE_NAME = "profile_goallist.csv";
    private static final String INVESTMENT_BOND_LIST_FILE_NAME = "_investment_bondList.csv";
    private static final String INVESTMENT_TRANSACTION_LIST_FILE_NAME = "_investment_transactionList.csv";
    private static final String SAVING_TRANSACTION_LIST_FILE_NAME = "_saving_transactionList.csv";
    private static final String SAVING_RECURRING_TRANSACTION_LIST_FILE_NAME = "_saving_recurring_transactionList.csv";
    private static final String PROFILE_FILE_NAME = "profile.csv";
    private static final String HAS_SPENT = "true";
    private static final String NOT_SPENT = "false";
    private static final String NO_BANK_ACCOUNT = "";

    /**
     * Creates a new instance of the user profile.
     *
     * @param newUserName The username that the user desires to use.
     * @param ui required for printing.
     */
    public Profile(String newUserName, Ui ui) {
        storage = new Storage(FILE_PATH);
        this.username = newUserName;
        this.bankList = new BankList(storage);
        this.cardList = new CardList(storage);
        this.goalsList = new GoalsList(storage);
        this.ui = ui;
        try {
            loadBanksFromImportedData();
        } catch (BankException exceptionMessage) {
            ui.printError("Error importing banks");
        }
        try {
            iterateBanksToAddTransaction();
        } catch (BankException | ParseException exceptionMessage) {
            ui.printError("Error importing transactions, recurring transactions and "
                    + "bonds for bank accounts.");
        }
        try {
            loadGoalsFromImportedData();
        } catch (ParseException | BankException exceptionMessage) {
            ui.printError("Error importing goals for bank accounts.");
        }
    }

    /**
     * Gets the username of the user.
     *
     * @return The username of the profile.
     */
    public String profileGetUsername() {
        return this.username;
    }

    /**
     * Sets the username of the user.
     *
     * @param name existing name of user.
     * @param newName new name that user wants to change to.
     * @param ui required for printing
     * @throws ProfileException if name don't match or change same name or name contain special character.
     */
    public void profileSetUsername(String name, String newName, Ui ui) throws ProfileException {
        checkProfileName(name, newName);
        this.username = newName;
        ui.printMessage("\nProfile name was: " + name);
        ui.printMessage("Now changed to: " + newName);
        try {
            storage.writeProfileFile(new String[]{profileGetUsername()},PROFILE_FILE_NAME);
        } catch (IOException ex) {
            ui.printError("Unable to save profile now, your data is at risk, but we will"
                    + " try saving again, feel free to continue using the program.");
        }
    }

    /**
     * Checks if profile name exist and not changed to same name.
     *
     * @param name existing name of user.
     * @param newName new name that user wants to change to.
     * @throws ProfileException if name don't match or change same name.
     */
    public void checkProfileName(String name, String newName) throws ProfileException {
        if (!name.equals(this.username)) {
            throw new ProfileException("No profile name with " + name + "found!\nTry this instead: " + this.username);
        }
        if (newName.equals(this.username)) {
            throw new ProfileException("Profile name is already " + this.username);
        }
    }

    /**
     * Adds a new bank account into the BankList stored in this profile.
     *
     * @param newBank an instance of the new bank account.
     * @param ui      required for printing.
     * @throws BankException If duplicated bank name found.
     */
    public void profileAddNewBank(Bank newBank, Ui ui) throws BankException {
        bankList.bankListAddBank(newBank, ui);
    }

    /**
     * Deletes a bank account from the BankList.
     *
     * @param bankName name of the Bank account.
     * @param ui       required for printing.
     * @throws BankException If bank account fails check criteria.
     */
    public void profileDeleteBank(String bankName, String bankType, Ui ui) throws BankException {
        bankList.bankListDeleteBank(bankName, bankType, ui);
        if (bankType.equals(SAVING)) {
            goalsList.changeTiedAccountsToNull(bankName);
        }
    }

    /**
     * Lists all the bank accounts in the BankList.
     *
     * @param ui required for printing.
     * @throws BankException If there are no bank account of specified type.
     */
    public void profileListBanks(String bankType, Ui ui) throws BankException {
        bankList.bankListListBankAccount(bankType, ui);
    }

    /**
     * Adds a new expenditure tied to a specific bank account or credit card.
     *
     * @param bankName The name of the bank account or credit card.
     * @param expenditure     An expenditure object.
     * @param ui      required for printing.
     * @param type    Represents type of expenditure to be added.
     * @throws BankException If bank amount becomes negative after adding expenditure.
     */
    public void profileAddNewExpenditure(String bankName, Transaction expenditure, Ui ui, String type)
            throws BankException, CardException {
        if (CARD.equals(type)) {
            cardList.cardListAddExpenditure(bankName, expenditure, ui, type);
        } else if (BANK.equals(type) || BONDS.equals(type)) {
            bankList.bankListAddExpenditure(bankName, expenditure, ui, type);
        }
    }

    /**
     * Deletes an expenditure tied to a specific bank account.
     *
     * @param expenditureIndex    The index of the expenditure in the expenditureList tied to a specific bank account.
     * @param accountName The name of the card or bank account.
     * @param ui          required for printing.
     * @throws BankException        If bank account does not exist.
     * @throws TransactionException If invalid transaction.
     * @throws CardException        If card does not exist.
     */
    public void profileDeleteExpenditure(int expenditureIndex, String accountName, Ui ui,
            String type) throws BankException, TransactionException, CardException {
        if ("bank".equals(type)) {
            bankList.bankListDeleteExpenditure(expenditureIndex, accountName, ui);
        } else if ("card".equals(type)) {
            cardList.cardListDeleteExpenditure(expenditureIndex, accountName, ui);
        }
    }

    /**
     * Lists deposits from a specific bank account.
     *
     * @param listedBank Bank account to list from.
     * @param ui         required for printing.
     * @param displayNum Number of deposits to list.
     * @throws BankException        If bank account does not exist.
     * @throws TransactionException If invalid transaction
     */
    public void profileListDeposit(String listedBank, Ui ui, int displayNum)
            throws BankException, TransactionException {
        bankList.bankListListBankDeposit(listedBank, ui, displayNum);
    }

    /**
     * Lists expenditure from a specific a bank account or credit card.
     *
     * @param listedBankOrCard Bank account or credit card to list from.
     * @param ui               required for printing.
     * @param expendituresToDisplay       Number of expenditure to list.
     * @param type             Type of account to add expenditure into.
     * @throws BankException        If bank account does not exist.
     * @throws TransactionException If no expenditure found or no expenditure is in the list..
     * @throws CardException        If the credit card name cannot be found.
     */
    public void profileListExpenditure(String listedBankOrCard, Ui ui, int expendituresToDisplay, String type)
            throws BankException, TransactionException, CardException {
        if ("card".equals(type)) {
            cardList.cardListListCardExpenditure(listedBankOrCard, ui, expendituresToDisplay);
        } else if ("bank".equals(type)) {
            bankList.bankListListBankExpenditure(listedBankOrCard, ui, expendituresToDisplay);
        }
    }

    /**
     * Edits a specific expenditure from a specific bank account.
     *
     * @param expenditureIndex       Transaction number of the expenditure.
     * @param editFromBank Bank account of expenditure.
     * @param description         New description of expenditure.
     * @param amount       New amount of expenditure.
     * @param date         New date of expenditure.
     * @param category     New category of expenditure.
     * @param ui           required for printing.
     * @throws BankException        If bank account does not exist.
     * @throws TransactionException If incorrect date format.
     */
    public void profileEditExpenditure(int expenditureIndex, String editFromBank, String description,
            String amount, String date, String category, Ui ui, String type)
            throws BankException, TransactionException, CardException {
        if ("card".equals(type)) {
            cardList.cardListEditExpenditure(expenditureIndex, editFromBank, description, amount, date,
                    category, ui);
        } else if ("bank".equals(type)) {
            bankList.bankListEditExpenditure(expenditureIndex, editFromBank, description, amount, date,
                    category, ui);
        }
    }

    /**
     * Edits a specific savings account.
     *
     * @param name    Name of savings account to be edited.
     * @param newName New name of the savings account.
     * @param amount  New amount of the savings account.
     * @param income  New income of the saving account.
     * @param ui      required for printing.
     * @throws BankException If duplicate bank account name is found.
     */
    public void profileEditSavingsAccount(String name, String newName, String amount, String income, Ui ui)
            throws BankException {
        bankList.bankListEditSavings(name, newName, amount, income, ui);
    }

    /**
     * Edits a specific investment account.
     *
     * @param name    Name of investment account to be edited.
     * @param newName New name of the investment account.
     * @param amount  New amount of the investment account.
     * @param ui      required for printing.
     * @throws BankException If duplicate bank account name is found.
     */
    public void profileEditInvestmentAccount(String name, String newName, String amount, Ui ui)
            throws BankException {
        bankList.bankListEditInvestment(name, newName, amount, ui);
    }

    /**
     * Deletes a specific deposit from a specific bank account.
     *
     * @param depositIndex Transaction number of the deposit.
     * @param bankName Bank name of the deposit.
     * @param ui       required for printing.
     * @throws BankException        If bank account does not exist.
     * @throws TransactionException If transaction is not a deposit.
     */
    public void profileDeleteDeposit(int depositIndex, String bankName, Ui ui)
            throws BankException, TransactionException {
        bankList.bankListDeleteDeposit(bankName, depositIndex, ui);
    }

    /**
     * Adds a new deposit to a specific bank account.
     *
     * @param accountName  Bank account name.
     * @param deposit      Deposit to be added.
     * @param ui       required for printing.
     * @param bankType Type of bank to add deposit into
     * @throws BankException If bank account does not exist.
     */
    public void profileAddNewDeposit(String accountName, Transaction deposit, Ui ui, String bankType)
            throws BankException {
        bankList.bankListAddDeposit(accountName, deposit, ui, bankType);
    }

    /**
     * Edits a specific deposit from a specific bank account.
     *
     * @param expenditureIndex       Transaction number of the deposit.
     * @param editFromBank Bank account of deposit.
     * @param description         New description of deposit.
     * @param amount       New amount of deposit.
     * @param date         New date of deposit.
     * @param ui           required for deposit.
     * @throws BankException        If bank account does not exist.
     * @throws TransactionException If incorrect date format.
     */
    public void profileEditDeposit(int expenditureIndex, String editFromBank, String description, String amount,
            String date, Ui ui) throws BankException, TransactionException {
        bankList.bankListEditDeposit(expenditureIndex, editFromBank, description, amount, date, ui);
    }

    /**
     * Adds a new credit card into the CardList stored in this profile.
     *
     * @param newCard an instance of the new credit card.
     * @param ui      required for printing.
     */
    public void profileAddNewCard(Card newCard, Ui ui) throws CardException {
        cardList.cardListAddCard(newCard, ui);
    }

    /**
     * Edits a card from the CardList.
     *
     * @param name    name of the credit card.
     * @param newName new name of the credit card if any.
     * @param limit   new limit of the credit card if any.
     * @param rebate  new rebate of the credit card if any.
     * @param ui      The ui object required for printing.
     * @throws CardException If card cannot be found.
     */
    public void profileEditCardDetails(String name, String newName, String limit, String rebate, Ui ui)
            throws CardException {
        cardList.cardListEditCard(name, newName, limit, rebate, ui);
    }

    /**
     * Deletes a card from the CardList.
     *
     * @param name The name of the credit card.
     * @param ui   The ui object required for printing.
     * @throws CardException If card does not exist.
     */
    public void profileDeleteCard(String name, Ui ui) throws CardException {
        cardList.cardListDeleteCard(name, ui);
    }

    /**
     * Lists all the cards in the CardList.
     *
     * @param ui The ui object required for printing.
     * @throws CardException If CardList is empty.
     */
    public void profileListCards(Ui ui) throws CardException {
        cardList.cardListListCards(ui);
    }

    /**
     * Deletes bond from the bondList in the specified investment account.
     *
     * @param bankName the name of the bank account.
     * @param bondName the name of the bond to delete.
     * @param ui       required for printing.
     * @throws BankException if the bank is not found.
     */
    public void profileDeleteBond(String bankName, String bondName, Ui ui)
            throws BankException, BondException {
        bankList.bankListDeleteBond(bankName, bondName, ui);
    }

    /**
     * Gets the bond object from the investment account if it exists.
     *
     * @param bankName the name of the bank account.
     * @param bondName the name of the bond to retrieve.
     * @return the bond object.
     * @throws BankException if the bank does not exist.
     * @throws BondException if the bond does not exist.
     */
    public Bond profileGetBond(String bankName, String bondName) throws BankException, BondException {
        return bankList.bankListGetBond(bankName, bondName);
    }

    /**
     * Adds bond to a specific bank account.
     *
     * @param bankName the name of the bank account.
     * @param newBond  the bond object.
     * @param ui       required for printing.
     * @throws BankException If bank account does not exist.
     */
    public void profileAddNewBond(String bankName, Bond newBond, Ui ui) throws BankException {
        bankList.bankListAddBond(bankName, newBond, ui);
    }

    /**
     * Checks if the bond exists before adding.
     *
     * @param bankName the bank account name.
     * @param bond     the bond object.
     * @throws BankException If bank account does not exist.
     * @throws BondException If duplicate bond name is found.
     */
    public void profileIsBondUnique(String bankName, Bond bond) throws BankException, BondException {
        bankList.bankListIsBondExist(bankName, bond);
    }

    /**
     * Edits the bond in the bank account.
     *
     * @param bankName the name of the bank.
     * @param bondName the name of the bond to edit.
     * @param year     the new year of the bond.
     * @param rate     the new rate
     * @param ui       required for printing.
     * @throws BankException If bank account does not exist.
     * @throws BondException If there are no bonds.
     */
    public void profileEditBond(String bankName, String bondName, String year, String rate, Ui ui)
            throws BankException, BondException {
        bankList.bankListEditBond(bankName, bondName, year, rate, ui);
    }

    /**
     * Lists the bonds in the bank specified bank account.
     *
     * @param bankName   the name of the bank account.
     * @param ui         required for printing.
     * @param displayNum the number of bonds to display.
     * @throws BankException If bank account does not exist.
     * @throws BondException If there are no bonds.
     */
    public void profileListBonds(String bankName, Ui ui, int displayNum)
            throws BankException, BondException {
        bankList.bankListListBond(bankName, ui, displayNum);
    }

    /**
     * Lists all goals in GoalsList.
     *
     * @param ui required for printing.
     */
    public void profileListGoals(Ui ui) {
        goalsList.listGoals(ui);
    }

    /**
     * Adds a new financial goal.
     *
     * @param goals the goals object.
     * @param ui    required for printing.
     * @throws GoalsException If invalid parameters / attempt to add the same goal name.
     */
    public void profileAddGoals(Goals goals, Ui ui) throws GoalsException {
        goalsList.addToGoals(goals, ui);
    }

    /**
     * Deletes a goal from GoalsList.
     *
     * @param name name of the goal.
     * @param ui   required for printing.
     * @throws GoalsException If goal does not exists.
     */
    public void profileDeleteGoals(String name, Ui ui) throws GoalsException {
        goalsList.deleteFromGoalList(name, ui);
    }

    /**
     * Edits goals from GoalsList.
     *
     * @param goalName   name of goal.
     * @param amount     new target amount to reach for the goal.
     * @param date       new targeted date to meet goal.
     * @param newName    new name for the goal.
     * @param savingName new saving name for goal.
     * @param ui         required for printing.
     * @throws GoalsException If goal does not exists.
     */
    public void profileEditGoals(String goalName, String amount, Date date, String newName, Bank savingName, Ui ui)
            throws GoalsException {
        goalsList.editGoals(goalName, amount, date, newName, savingName, ui);
    }

    /**
     * Retrieves a Saving object.
     *
     * @param savingBankName Account name of Saving.
     * @return Total amount in Saving Account.
     * @throws BankException If no savingBankName is found.
     */
    public Bank profileGetSavingAccount(String savingBankName) throws BankException {
        return bankList.bankListGetSavingAccount(savingBankName);
    }

    /**
     * Adds a recurring expenditure to the specified account.
     *
     * @param accountName             Bank account name to add the recurring expenditure to.
     * @param newRecurringExpenditure New recurring expenditure to be added.
     * @param ui                      Used for printing.
     * @param type                    Type of account to add to.
     * @throws BankException        If bank account is not found or if bank account is an investment account.
     * @throws TransactionException If the recurring expenditure list is full.
     */
    public void profileAddRecurringExpenditure(
            String accountName, Transaction newRecurringExpenditure, Ui ui, String type)
            throws BankException, TransactionException {
        if ("card".equals(type)) {
            //card recurring transaction
            System.out.println("Do card recurring transaction here");
        } else if ("bank".equals(type)) {
            bankList.bankListAddRecurringExpenditure(accountName, newRecurringExpenditure, ui);
        }
    }

    /**
     * Deletes a recurring expenditure from the specified account.
     *
     * @param accountName Account name.
     * @param index       Index of the recurring expenditure.
     * @param ui          Used for printing.
     * @param type        Type of account to delete from.
     * @throws BankException        If bank account does not exist or is an investment account.
     * @throws TransactionException If there are 0 recurring expenditure in the list or if index is out of range.
     */
    public void profileDeleteRecurringExpenditure(String accountName, int index, Ui ui, String type)
            throws BankException, TransactionException {
        if ("card".equals(type)) {
            //card recurring transaction
            System.out.println("Do card recurring transaction here");
        } else if ("bank".equals(type)) {
            bankList.bankListDeleteRecurringExpenditure(accountName, index, ui);
        }
    }

    /**
     * Lists all recurring expenditure from the specified account.
     *
     * @param accountName Name of account.
     * @param ui          Used for printing.
     * @param type        Account type.
     * @throws BankException        If bank is not found or is an investment account.
     * @throws TransactionException If there are 0 recurring expenditures in the account.
     */
    public void profileListRecurringExpenditure(String accountName, Ui ui, String type)
            throws BankException, TransactionException {
        if ("card".equals(type)) {
            //card recurring transaction
            System.out.println("Do card recurring transaction here");
        } else if ("bank".equals(type)) {
            bankList.bankListListRecurringExpenditure(accountName, ui);
        }
    }

    /**
     * Edits a recurring expenditure from the specified account.
     *
     * @param accountName Name of the account.
     * @param index       Index of the recurring expenditure.
     * @param description New description of the recurring expenditure.
     * @param amount      New amount of the recurring expenditure.
     * @param category    New category of the recurring expenditure.
     * @param ui          Used for printing.
     * @param type        The account type.
     * @throws BankException        If the bank is not found or is an investment account.
     * @throws TransactionException If there are 0 recurring expenditure in the account or index is out of range.
     */
    public void profileEditRecurringExpenditure(
            String accountName, int index, String description, String amount, String category, Ui ui, String type)
            throws BankException, TransactionException {
        if ("card".equals(type)) {
            //card recurring transaction
            System.out.println("Do card recurring transaction here");
        } else if ("bank".equals(type)) {
            bankList.bankListEditRecurringExpenditure(accountName, index, description, amount, category, ui);
        }
    }

    /**
     * Updates all outdated objects in the profile.
     *
     * @param ui Used for printing.
     */
    public void profileUpdate(Ui ui) {
        bankList.bankListUpdateRecurringTransactions(ui);
        //card update recurring
        ui.printMessage("Profile has been updated");
    }

    /**
     * Transfers fund from one bank account to another bank account.
     *
     * @param from   The account name for transferring the fund.
     * @param to     The account name to receive the fund.
     * @param amount The amount to be transferred.
     * @param date   The date that the fund was transferred.
     * @param ui     The ui object Required for printing.
     * @throws BankException If any of the bank does not exist or insufficient fund to transfer.
     */
    public void transferFund(String from, String to, double amount, Date date,
            Ui ui) throws BankException {
        String fromType = bankList.bankListIsAccountExistToTransfer(from, amount);
        String toType = bankList.bankListIsAccountExistToReceive(to);
        String descriptionTo = "Fund Transfer to " + to;
        Transaction newExpenditure = new Expenditure(descriptionTo, amount, date, TRANSFERCATEGORY);
        bankList.bankListAddExpenditure(from, newExpenditure, ui, checkBankType(fromType));
        String descriptionFrom = "Fund Received from " + from;
        Transaction newDeposit = new Deposit(descriptionFrom, amount, date, DEPOSITCATEGORY);
        bankList.bankListAddDeposit(to, newDeposit, ui, checkBankType(toType));
    }

    /**
     * Checks whether the bank account is a savings or investment account.
     *
     * @param type The type of bank account.
     * @return The result whether it is to be transfer or deposit to a savings or investment account.
     */
    private String checkBankType(String type) {
        if (SAVING.equals(type)) {
            return ISBANK;
        } else {
            return ISINVESTMENT;
        }
    }

    /**
     * Finds either the bank or card that matches the name provided by user.
     *
     * @param name The name to be matched against.
     * @param type The type which specify whether the search is for saving, investment or card object.
     * @param ui   The object required for printing.
     * @throws BankException If there is no matches for saving or investment object.
     * @throws CardException If there is no matches for card object.
     */
    public void findBankOrCard(String name, String type, Ui ui) throws BankException, CardException {
        if (SAVING.equals(type)) {
            bankList.findBankAccount(name, type, ui);
        } else if (INVESTMENT.equals(type)) {
            bankList.findBankAccount(name, type, ui);
        } else if (CARD.equals(type)) {
            cardList.findCard(name, ui);
        }
    }

    /**
     * Finds the bonds that matches the name provided by user.
     *
     * @param bondName The bond name to be searched for.
     * @param from     The investment account name to search for the bonds.
     * @param ui       The object required for printing.
     * @throws BankException If investment account does not exist.
     * @throws BondException If no bonds could be found.
     */
    public void findBond(String bondName, String from, Ui ui) throws BankException, BondException {
        bankList.checkInvestmentAccountExist(bondName, from, ui);
    }

    /**
     * Finds the transactions in either bank or card object that matches with the keywords provided by user.
     *
     * @param name        The bank or card name to be searched for.
     * @param fromDate    The date to search from.
     * @param toDate      The date to search until.
     * @param description The description keyword to match against.
     * @param category    The category keyword to match against.
     * @param ui          The object required for printing.
     * @throws BankException        If bank name specified does not exist.
     * @throws TransactionException If parsing of date fails.
     * @throws CardException        If card with the name does not exist.
     */
    public void findTransaction(String name, String fromDate, String toDate, String description, String category,
            String type, Ui ui) throws BankException, TransactionException, CardException {
        if (type.equals(BANK)) {
            bankList.bankListFindTransaction(name, fromDate, toDate, description, category, ui);
        } else if (type.equals(CARD)) {
            cardList.cardListFindTransaction(name, fromDate, toDate, description, category, ui);
        }
    }

    /**
     * Imports data generally for further processing based on file name specified.
     *
     * @param fileName the name of the file to be imported.
     * @param ui required for printing.
     * @return the imported data from the file formatted in List of String arrays.`
     */
    private List<String[]> importListDataFromStorage(String fileName,Ui ui) {
        List<String[]> importData = null;
        try {
            importData = storage.readFile(fileName);
        } catch (IOException | NullPointerException e) {
            ui.printError("Unable");
        }
        return importData;
    }

    /**
     * Add banks from imported data.
     *
     * @throws BankException if there are errors importing data.
     */
    private void loadBanksFromImportedData() throws BankException {
        if (storage.isFileExist(PROFILE_BANK_LIST_FILE_NAME)) {
            List<String[]> importData = importListDataFromStorage(PROFILE_BANK_LIST_FILE_NAME,ui);
            for (String[] importDataRow : importData) {
                String bankName = importDataRow[0];
                String bankType = importDataRow[1];
                String amount = importDataRow[2];
                double doubleAmount = Double.parseDouble(amount);
                String income = importDataRow[3];
                double doubleIncome = Double.parseDouble(income);
                if (bankType.equals(INVESTMENT)) {
                    Bank newInvestment = new Investment(bankName, doubleAmount);
                    profileImportNewBank(newInvestment);
                } else if (bankType.equals(SAVING)) {
                    Bank newSaving = new Saving(bankName, doubleAmount, doubleIncome);
                    profileImportNewBank(newSaving);
                } else {
                    throw new BankException("Error importing banks, "
                            + "data related to some bank accounts are not available");
                }
            }
        }
    }

    /**
     * Iterates the bank file line by line to add specific details tied to the bank account.
     *
     * @throws BankException if there are errors importing data.
     * @throws ParseException if there are errors parsing date.
     */
    private void iterateBanksToAddTransaction() throws ParseException, BankException {
        if (storage.isFileExist(PROFILE_BANK_LIST_FILE_NAME)) {
            List<String[]> importBankData = importListDataFromStorage(PROFILE_BANK_LIST_FILE_NAME, ui);
            for (int i = 0; i < importBankData.size(); i++) {
                String bankName = importBankData.get(i)[0];
                String bankType = importBankData.get(i)[1];
                if (bankType.equals(INVESTMENT)) {
                    String transactionFileName = i + INVESTMENT_TRANSACTION_LIST_FILE_NAME;
                    String bondsFileName = i + INVESTMENT_BOND_LIST_FILE_NAME;
                    if (storage.isFileExist(bondsFileName)) {
                        loadBondsForInvestmentBanks(bondsFileName, bankName);
                    }
                    if (storage.isFileExist(transactionFileName)) {
                        loadTransactionsForBanks(transactionFileName, bankName, bankType);
                    }
                } else if (bankType.equals(SAVING)) {
                    String transactionFileName = i + SAVING_TRANSACTION_LIST_FILE_NAME;
                    String recurringTransactionFileName = i + SAVING_RECURRING_TRANSACTION_LIST_FILE_NAME;
                    if (storage.isFileExist(transactionFileName)) {
                        loadTransactionsForBanks(transactionFileName, bankName, bankType);
                    }
                    if (storage.isFileExist(recurringTransactionFileName)) {
                        loadRecurringTransactionsForBanks(recurringTransactionFileName, bankName, bankType);
                    }
                }
            }
        }
    }

    /**
     * Loads the transactions tied to the bank account.
     *
     * @param fileName the name of the file to obtain transactions from.
     * @param bankName the name of the bank account.
     * @param bankType the type of bank account.
     * @throws BankException if there are errors importing data.
     * @throws ParseException if there are errors parsing date.
     */
    private void loadTransactionsForBanks(String fileName, String bankName, String bankType)
            throws BankException, ParseException {
        List<String[]> importData = importListDataFromStorage(fileName,ui);
        for (String[] importDataRow : importData) {
            String description = importDataRow[0];
            String amount = importDataRow[1];
            double doubleAmount = Double.parseDouble(amount);
            String date = importDataRow[2];
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
            Date dateInFormat = dateFormat.parse(date);
            String category = importDataRow[3];
            String hasSpent = importDataRow[4];
            if (bankType.equals(INVESTMENT)) {
                if (hasSpent.equals(HAS_SPENT)) {
                    Transaction newExpenditure = new Expenditure(description, doubleAmount, dateInFormat, category);
                    profileImportNewExpenditure(bankName, newExpenditure, BONDS);
                } else if (hasSpent.equals(NOT_SPENT)) {
                    Transaction newDeposit = new Deposit(description, doubleAmount, dateInFormat, category);
                    profileImportNewDeposit(bankName, newDeposit, BONDS);
                }
            } else if (bankType.equals(SAVING)) {
                if (hasSpent.equals(HAS_SPENT)) {
                    Transaction newExpenditure = new Expenditure(description, doubleAmount, dateInFormat, category);
                    profileImportNewExpenditure(bankName, newExpenditure, BANK);
                } else if (hasSpent.equals(NOT_SPENT)) {
                    Transaction newDeposit = new Deposit(description, doubleAmount, dateInFormat, category);
                    profileImportNewDeposit(bankName, newDeposit, BANK);
                }
            }
        }
    }

    /**
     * Loads the recurring transactions tied to the bank account.
     *
     * @param fileName the name of the file to obtain transactions from.
     * @param bankName the name of the bank account.
     * @param bankType the type of bank account.
     * @throws BankException if there are errors importing data.
     * @throws ParseException if there are errors parsing date.
     */
    private void loadRecurringTransactionsForBanks(String fileName, String bankName, String bankType)
            throws ParseException, BankException {
        List<String[]> importData = importListDataFromStorage(fileName,ui);
        for (String[] importDataRow : importData) {
            String description = importDataRow[0];
            String amount = importDataRow[1];
            double doubleAmount = Double.parseDouble(amount);
            String date = importDataRow[2];
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
            Date dateInFormat = dateFormat.parse(date);
            String category = importDataRow[3];
            String hasSpent = importDataRow[4];
            if (bankType.equals(SAVING)) {
                if (hasSpent.equals(HAS_SPENT)) {
                    Transaction newExpenditure = new Expenditure(description, doubleAmount, dateInFormat, category);
                    profileImportNewRecurringExpenditure(bankName, newExpenditure);
                } else if (hasSpent.equals(NOT_SPENT)) {
                    Transaction newDeposit = new Deposit(description, doubleAmount, dateInFormat, category);
                    profileImportNewRecurringExpenditure(bankName, newDeposit);
                }
            }
        }
    }


    /**
     * Loads the bonds tied to the investment bank account.
     *
     * @param fileName the name of the file to obtain transactions from.
     * @param bankName the name of the bank account.
     * @throws BankException if there are errors importing data.
     * @throws ParseException if there are errors parsing date.
     */
    private void loadBondsForInvestmentBanks(String fileName, String bankName)
            throws ParseException, BankException {
        List<String[]> importData = importListDataFromStorage(fileName,ui);
        for (String[] importDataRow : importData) {
            String bondName = importDataRow[0];
            String amount = importDataRow[1];
            double doubleAmount = Double.parseDouble(amount);
            String rate = importDataRow[2];
            double doubleRate = Double.parseDouble(rate);
            String date = importDataRow[3];
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
            Date dateInFormat = dateFormat.parse(date);
            String year = importDataRow[4];
            int integerYear = Integer.parseInt(year);
            Bond newBond = new Bond(bondName, doubleAmount, doubleRate, dateInFormat, integerYear);
            profileImportNewBonds(bankName, newBond);
        }
    }

    /**
     * Imports one instance of bonds.
     *
     * @param bankName the name of the bank account.
     * @param newBond an instance of the bond to be imported.
     * @throws BankException if the bank account does not support this feature.
     */
    private void profileImportNewBonds(String bankName, Bond newBond) throws BankException {
        bankList.bankListImportNewBonds(bankName, newBond);
    }

    /**
     * Imports one instance of deposit.
     *
     * @param bankName the name of the bank account.
     * @param deposit an instance of the deposit.
     * @param bankType the type of bank account.
     * @throws BankException if the bank account does not support this feature.
     */
    private void profileImportNewDeposit(String bankName, Transaction deposit, String bankType)
            throws BankException {
        bankList.bankListImportNewDeposit(bankName, deposit, bankType);
    }

    /**
     * Imports one instance of an expenditure.
     *
     * @param bankName the name of the bank account.
     * @param expenditure an instance of the expenditure.
     * @param type the type of expenditure.
     * @throws BankException if the bank account does not support this feature.
     */
    private void profileImportNewExpenditure(String bankName, Transaction expenditure, String type)
            throws BankException {
        bankList.bankListImportNewExpenditure(bankName, expenditure, type);
    }

    /**
     * Imports one instance of a bank account.
     *
     * @param newBank an instance of a new bank account.
     */
    private void profileImportNewBank(Bank newBank) {
        bankList.bankListImportNewBank(newBank);
    }

    /**
     * Imports one instance of recurring expenditure.
     *
     * @param bankName the name of the bank account.
     * @param newRecurringExpenditure an instance of the recurring expenditure.
     * @throws BankException if the bank account does not support this feature.
     */
    private void profileImportNewRecurringExpenditure(String bankName, Transaction newRecurringExpenditure)
            throws BankException {
        bankList.bankListImportNewRecurringExpenditure(bankName, newRecurringExpenditure);
    }

    /**
     * Checks if the bond list from the specified bank name is full.
     *
     * @param bankName Investment account name.
     * @return If the bond list from the specified investment account is full.
     * @throws BankException If used on a savings account or if investment account does not exist.
     */
    public boolean profileIsBondListFull(String bankName) throws BankException {
        return bankList.bankListIsBondListFull(bankName);
    }

    /**
     * Add goals from imported data.
     *
     * @throws BankException if there are errors importing data.
     * @throws ParseException if there are errors parsing date.
     */
    private void loadGoalsFromImportedData() throws ParseException, BankException {
        if (storage.isFileExist(PROFILE_GOAL_LIST_FILE_NAME)) {
            List<String[]> importData = importListDataFromStorage(PROFILE_GOAL_LIST_FILE_NAME,ui);
            for (String[] importDataRow : importData) {
                Goals newGoal;
                String goalName = importDataRow[0];
                String amount = importDataRow[1];
                String date = importDataRow[2];
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
                Date dateInFormat = dateFormat.parse(date);
                String savingsAccountName = importDataRow[3];
                double doubleAmount = Double.parseDouble(amount);
                if (NO_BANK_ACCOUNT.equals(savingsAccountName)) {
                    newGoal = new Goals(goalName,doubleAmount,dateInFormat);
                } else {
                    newGoal = new Goals(goalName,doubleAmount,dateInFormat,
                            bankList.bankListGetSavingAccount(savingsAccountName));
                }
                profileImportNewGoals(newGoal);
            }
        }
    }

    /**
     * Imports one instance of goals.
     *
     * @param newGoal an instance of goals object.
     */
    private void profileImportNewGoals(Goals newGoal) {
        goalsList.bankListImportNewGoal(newGoal);
    }

    /**
     * Throws exception if credit card does not exist.
     *
     * @param card  The credit card to be checked if exist.
     * @throws CardException    Throws exception if credit card does not exist.
     */
    public void checkCardExists(String card) throws CardException {
        cardList.checkCardExists(card);
    }

    /**
     * Returns the total unpaid expenditures amount based on the specified date.
     *
     * @param card  The card which the expenditures belongs to.
     * @param date  The YearMonth date to search for expenditures.
     * @return      The total unpaid expenditures amount based on the specified date.
     * @throws CardException    If card does not exist.
     */
    public double getCardUnpaidBillAmount(String card, YearMonth date) throws CardException {
        double cardBillAmount = cardList.getUnpaidBillAmount(card, date);
        return cardBillAmount;
    }

    /**
     * Returns the total paid expenditures amount based on the specified date.
     *
     * @param card  The card which the expenditures belongs to.
     * @param date  The YearMonth date to search for expenditures.
     * @return      The total paid expenditures amount based on the specified date.
     * @throws CardException    If card does not exist.
     */
    public double getCardPaidBillAmount(String card, YearMonth date) throws CardException {
        double cardBillAmount = cardList.getPaidBillAmount(card, date);
        return cardBillAmount;
    }

    /**
     * Returns the monthly rebate of the credit card.
     *
     * @param card  The credit card to get the monthly rebate information from.
     * @return      The monthly rebate of the credit card.
     * @throws CardException    If card does not exist.
     */
    public double getCardRebateAmount(String card) throws CardException {
        return cardList.getRebateAmount(card);
    }

    /**
     * Adds the YearMonth's card bill into bank expenditure, card rebates into bank deposit,
     * and transfer card expenditures from unpaid to paid transaction list.
     *
     * @param card      The credit card name of the card transactions to be transferred.
     * @param bank      The bank name of the bank to add expenditure and deposit.
     * @param expenditure       The expenditure object to be added into the bank's transaction list.
     * @param deposit       The deposit object to be added into the bank's transaction list.
     * @param cardDate  The YearMonth date of the card bill.
     * @param ui        The Ui of OwlMoney.
     * @param type      Type of expenditure (card or bank).
     * @throws BankException        If bank account does not exist.
     * @throws TransactionException If invalid transaction when transferring transaction.
     */
    public void payCardBill(String card, String bank, Expenditure expenditure, Deposit deposit, YearMonth cardDate,
            Ui ui, String type)
            throws BankException, TransactionException {
        bankList.bankListAddExpenditure(bank, expenditure, ui, type);
        ui.printMessage("\n");
        bankList.bankListAddDeposit(bank, deposit, ui, type);
        cardList.transferExpUnpaidToPaid(card, cardDate, type);
        ui.printMessage("Credit Card bill for " + card + " for the month of " + cardDate
                + " have been successfully paid!");
    }

    /**
     * Transfers the YearMonth card expenditures from paid to unpaid transaction list.
     *
     * @param card      The credit card name of the card transactions to be transferred.
     * @param cardDate  The YearMonth date of the card transactions to be transferred.
     * @param ui        The Ui of OwlMoney.
     * @param type      Type of expenditure (card or bank).
     * @throws TransactionException If invalid transaction when transferring transaction.
     */
    public void unpayCardBill(String card, YearMonth cardDate, Ui ui, String type) throws TransactionException {
        cardList.transferExpPaidToUnpaid(card, cardDate, type);
        ui.printMessage("Credit Card bill for " + card + " for the month of " + cardDate
                + " have been successfully reverted!");
    }

    /**
     * Returns expenditure amount in bank based on specified transaction id.
     *
     * @param bank  The name of the bank to search for transaction.
     * @param expenditureId The transaction id of the transaction to be searched.
     * @return      The expenditure amount in bank based on specified transaction id.
     * @throws BankException        If bank account does not exist.
     * @throws TransactionException If transaction does not exist.
     */
    public double getBankExpAmountById(String bank, int expenditureId) throws BankException, TransactionException {
        return bankList.bankListGetExpAmountById(bank, expenditureId);
    }
}
