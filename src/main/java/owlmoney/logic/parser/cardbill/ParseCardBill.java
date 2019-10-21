package owlmoney.logic.parser.cardbill;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.YearMonth;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import owlmoney.logic.command.Command;
import owlmoney.logic.parser.ParseRawData;
import owlmoney.logic.parser.exception.ParserException;
import owlmoney.logic.regex.RegexUtil;

public abstract class ParseCardBill {
    HashMap<String, String> cardBillParameters = new HashMap<String, String>();
    private ParseRawData parseRawData = new ParseRawData();
    private String rawData;
    private static final String[] CARDBILL_KEYWORD = new String[] {
            "/card", "/date", "/to"
    };
    private static final List<String> CARDBILL_KEYWORD_LISTS = Arrays.asList(CARDBILL_KEYWORD);
    static final String CARD = "/card";
    static final String TO = "/to";
    static final String DATE = "/date";

    /**
     * Constructor which creates an instance of CardBill object.
     *
     * @param data Raw user input date.
     */
    ParseCardBill(String data) {
        this.rawData = data;
    }

    /**
     * Fills the hash table to map each user input to each parameter.
     *
     * @throws ParserException If duplicate parameters are detected.
     */
    public void fillHashTable() throws ParserException {
        cardBillParameters.put(CARD,
                parseRawData.extractParameter(rawData, CARD, CARDBILL_KEYWORD));
        cardBillParameters.put(TO,
                parseRawData.extractParameter(rawData, TO, CARDBILL_KEYWORD));
        cardBillParameters.put(DATE,
                parseRawData.extractParameter(rawData, DATE, CARDBILL_KEYWORD));
    }

    /**
     * Checks if the first parameter is a valid parameter.
     *
     * @throws ParserException If the first parameter is invalid.
     */
    void checkFirstParameter() throws ParserException {
        String[] rawDateSplit = rawData.split(" ", 2);
        if (!CARDBILL_KEYWORD_LISTS.contains(rawDateSplit[0])) {
            throw new ParserException("Incorrect parameter " + rawDateSplit[0]);
        }
    }

    /**
     * Checks if the card name entered by the user does not contain special character and not too long.
     *
     * @param nameString Name of the card
     * @throws ParserException If the name is too long or contain special characters.
     */
    void checkName(String nameString, String type) throws ParserException {
        if (!RegexUtil.regexCheckName(nameString)) {
            throw new ParserException(type + " name can only contain letters and at most 30 characters");
        }
    }

    /**
     * Checks if the date is of valid YearMonth format and not after now.
     *
     * @param dateString Date to be checked.
     * @return Date if checks pass.
     * @throws ParserException If date format is invalid.
     */
    YearMonth checkDate(String dateString) throws ParserException {
        if (RegexUtil.regexCheckMonthYearFormat(dateString)) {
            YearMonth date = YearMonth.parse(dateString);
            if (date.compareTo(YearMonth.now()) > 0) {
                throw new ParserException("/date cannot be after this month");
            }
            return date;
        }
        throw new ParserException("Incorrect date format." + " Date format is mm/yyyy in year range of 1900-2099");
    }

    /**
     * Checks the user input for any redundant parameters.
     *
     * @param parameter Redundant parameter to check for,
     * @param command   Command the user performed.
     * @throws ParserException If a redundant parameter is detected.
     */
    void checkRedundantParameter(String parameter, String command) throws ParserException {
        if (rawData.contains(parameter)) {
            throw new ParserException(command + "/card should not contain " + parameter);
        }
    }

    /**
     * Abstract method where each card parser performs different checks on the user entered parameters.
     *
     * @throws ParserException If any parameters fail the check.
     */
    public abstract void checkParameter() throws ParserException;

    /**
     * Abstract method where each card parser creates different commands.
     *
     * @return Command to be executed.
     */
    public abstract Command getCommand();
}

