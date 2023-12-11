package app.unicast.nio;

public class Constants {
    public static final String HOST = "localhost";
    public static final int PORT = 6009;
    public static final int MESSAGE_LIMIT = 300;
    public static final int USERNAME_LIMIT = 30;

    public static final String COMMAND_DELIMITER = "|";
    public static final String ARRAY_DELIMITER = ";";

    public static final String TO_COMMAND = "#to";
    public static final String JOIN_COMMAND = "#join";
    public static final String QUIT_COMMAND = "#quit";
    public static final String MEMBERS_COMMAND = "#members";
    public static final String PUBLIC_MESSAGE_COMMAND = "#public";

    public static final String FROM_FLAG = "#from";
    public static final String JOINED_FLAG = "#joined";
    public static final String CHANGED_FLAG = "#changed";
    public static final String LEFT_FLAG = "#left";
    public static final String USERNAME_EXCEPTION_FLAG = "#usernameException";

    public static final String DEFAULT_KEY = "default";
}

