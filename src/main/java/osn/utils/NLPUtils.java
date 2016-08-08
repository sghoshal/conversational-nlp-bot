package main.java.osn.utils;

import java.util.HashSet;
import java.util.Set;

public class NLPUtils {

    public static boolean isStringNotBlank(String input) {
        return (input != null) && (!input.trim().isEmpty());
    }

    public static boolean isStringBlank(String input) {
        return !isStringNotBlank(input);
    }

    public static boolean isConversationOver(String userInput) {
        if (userInput == null)
            return true;

        Set<String> closeWords = new HashSet<String>();

        closeWords.add("end");
        closeWords.add("close");
        closeWords.add("done");
        closeWords.add("skip");

        return (closeWords.contains(userInput.toLowerCase()));
    }

    public static boolean isChatOver(String userInput) {
        if (userInput == null)
            return true;

        return userInput.toLowerCase().equals("exit");
    }
}
