package owlmoney.logic.parser.profile;

import owlmoney.logic.command.Command;
import owlmoney.logic.command.profile.EditProfileCommand;
import owlmoney.logic.parser.ParseRawData;
import owlmoney.logic.parser.exception.ParserException;
import owlmoney.logic.regex.RegexUtil;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * Represents the parsing of inputs for editing a profile name.
 */
public class ParseEditProfile {
    HashMap<String, String> profileParameters = new HashMap<String, String>();
    private ParseRawData parseRawData = new ParseRawData();
    private String rawData;
    private static final String[] PROFILE_KEYWORD = new String[] {"/name", "/newname"};
    static final String NAME = "/name";
    static final String NEW_NAME = "/newname";
    private static final List<String> PROFILE_KEYWORD_LISTS = Arrays.asList(PROFILE_KEYWORD);

    /**
     * Creates instance of ParseEditProfile class.
     *
     * @param data Raw user input data.
     * @throws ParserException If first parameter is of invalid type.
     */
    public ParseEditProfile(String data) throws ParserException {
        this.rawData = data;
        checkFirstParameter();
    }

    /**
     * Checks if the first parameter is a valid parameter.
     *
     * @throws ParserException If the first parameter is invalid.
     */
    void checkFirstParameter() throws ParserException {
        String[] rawDateSplit = rawData.split(" ", 2);
        if (!PROFILE_KEYWORD_LISTS.contains(rawDateSplit[0])) {
            throw new ParserException("Incorrect parameter + rawDateSplit[0]");
        }
    }

    /**
     * Fills a hash table mapping each user input to each parameter.
     *
     * @throws ParserException If duplicate parameters are detected.
     */
    public void fillHashTable() throws ParserException {
        profileParameters.put(NAME, parseRawData.extractParameter(rawData, NAME, PROFILE_KEYWORD));
        profileParameters.put(NEW_NAME, parseRawData.extractParameter(rawData, NEW_NAME, PROFILE_KEYWORD));
    }

    /**
     * Checks if the profile name entered by the user does not contain special character and not too long.
     *
     * @param key        /name and /newname
     * @param nameString Name of profile
     * @throws ParserException If the name is too long or contain special characters.
     */
    void checkName(String key, String nameString) throws ParserException {
        if (!RegexUtil.regexCheckName(nameString)) {
            throw new ParserException(key + " can only be alphanumeric and at most 30 characters");
        }
    }

    /**
     * Checks the parameters entered by the user.
     *
     * @throws ParserException If any parameters fail the check.
     */
    public void checkParameter() throws ParserException {
        Iterator<String> profileIterator = profileParameters.keySet().iterator();
        int changeCounter = 0;
        while (profileIterator.hasNext()) {
            String key = profileIterator.next();
            String value = profileParameters.get(key);

            if (NAME.equals(key) && (value.isEmpty() || value.isBlank())) {
                throw new ParserException("/name cannot be empty.");
            } else if (NAME.equals(key)) {
                checkName(NAME, value);
            }
            if (NEW_NAME.equals(key) && !(value.isEmpty() || value.isBlank())) {
                checkName(NEW_NAME, value);
                changeCounter++;
            }
        }
        if (changeCounter == 0) {
            throw new ParserException("Edit should have at least 1 differing parameter to change.");
        }
    }

    /**
     * Returns command to execute editing of profile.
     *
     * @return EditProfileCommand to be executed.
     */
    public Command getCommand() {
        EditProfileCommand newEditProfileCommand = new EditProfileCommand(profileParameters.get(NAME),
                profileParameters.get(NEW_NAME));
        return newEditProfileCommand;
    }
}
