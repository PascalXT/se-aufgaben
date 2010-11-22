package flags;

import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;

public class Flags {
  private static String[][] flag_definition;
  final private static int kFlagNamePos = 0;
  final private static int kFlagValuePos = 1;
  final private static int kFlagExplanationPos = 2;
  final static String kFlagStart = "--";
  final static String kFlagNameDelimiter = "=";
  final static String kConfigFileParam = "configFile";
  final public static String kTrue = "true";
  final public static String kOne = "1";

  // In fact, everything different to kTrue is considered false.
  final public static String kFalse = "false";

  /**
   * @throws FlagErrorException
   * @Param flag_definition: Triple of form {"Flagname", "Default value"/null,
   *        "Explanation"}
   * @Param args: argument flags of the main method.
   */
  public static void setFlags(String[][] flag_definition, String[] args) throws FlagErrorException {
    Flags.flag_definition = flag_definition;
    parseArguments(args);
  }

  private static void parseArguments(String[] args) throws FlagErrorException {

    String configFilePath = searchForFlags(Arrays.asList(args));

    if (configFilePath != null && !configFilePath.isEmpty()) {
      // Add flags defined in config File.
      LineNumberReader reader;
      LinkedList<String> fileArgs = new LinkedList<String>();
      try {
        reader = new LineNumberReader(new FileReader(configFilePath));
        String nextLine;
        while ((nextLine = reader.readLine()) != null) {
          if (!nextLine.startsWith(kFlagStart)) {
            nextLine = kFlagStart + nextLine;
          }
          fileArgs.add(nextLine);
        }
      } catch (IOException e) {
        e.printStackTrace();
        System.exit(1);
      }
      searchForFlags(fileArgs);
    }

    // Check, whether all flags are set now.
    String missing_flags = "";
    for (int i = 0; i < flag_definition.length; i++) {
      if (flag_definition[i][kFlagValuePos] == null) {
        missing_flags += flag_definition[i][kFlagNamePos] + ": " + flag_definition[i][kFlagExplanationPos] + "\n";
      }
    }
    if (!missing_flags.equals("")) {
      throw new FlagErrorException("Please specify values for the following flags:\n" + missing_flags);
    }
  }

  private static String searchForFlags(Collection<String> args) throws FlagErrorException {
    String configFilePath = null;

    // Search for flags in argument array.
    for (String arg : args) {
      final int flag_delimiter_pos = arg.indexOf(kFlagNameDelimiter);
      if (arg.startsWith(kFlagStart) && flag_delimiter_pos >= 0) {
        // This argument is a flag. Find the corresponding flag definition.
        final String flag_name = arg.substring(kFlagStart.length(), flag_delimiter_pos).trim();
        System.out.println("Flag name: " + flag_name);
        final String flag_value = arg.substring(flag_delimiter_pos + kFlagNameDelimiter.length()).trim();
        System.out.println("Flag value: " + flag_value);
        setValueInFlagDefinition(flag_name, flag_value);

        // Set configFile
        if (flag_name.equals(kConfigFileParam)) {
          configFilePath = flag_value;
        }
      }
    }
    return configFilePath;
  }

  private static void setValueInFlagDefinition(final String flag_name, final String flag_value)
      throws FlagErrorException {
    int flag_index = -1;

    // Iterate flag definition and set corresponding flag.
    for (int j = 0; j < flag_definition.length; j++) {
      if (flag_definition[j][kFlagNamePos].equals(flag_name)) {
        if (flag_index != -1) {
          throw new FlagErrorException("Flag " + flag_name + " has been defined twice.");
        }
        flag_definition[j][kFlagValuePos] = flag_value;
        flag_index = j;
      }
    }
  }

  public static String getFlagValue(String flagName) throws FlagErrorException {
    for (int i = 0; i < flag_definition.length; i++) {
      if (flag_definition[i][kFlagNamePos].equals(flagName)) {
        return flag_definition[i][kFlagValuePos];
      }
    }
    throw new FlagErrorException("Flag " + flagName + " has not been defined");
  }

  public static boolean isTrue(String flagName) {
    try {
      return getFlagValue(flagName).equals(kTrue) || getFlagValue(flagName).equals(kOne);
    } catch (FlagErrorException e) {
      return false;
    }
  }
}
