package flags;


public class Flags { 
  private static String[][] flag_definition;
  final private static int kFlagNamePos = 0;
  final private static int kFlagValuePos = 1;
  final private static int kFlagExplanationPos = 2;
  final static String kFlagStart = "--";
  final static String kFlagNameDelimiter = "=";
  final public static String kTrue = "true";
  
  //In fact, everything different to kTrue is considered false.
  final public static String kFalse = "false";
  
  /** 
   * @throws FlagErrorException 
   * @Param flag_definition: Triple of form 
   *                         {"Flagname", "Default value"/null, "Explanation"}
   * @Param args: argument flags of the main method.
   */
  public static void setFlags(String[][] flag_definition, String[] args)
      throws FlagErrorException {
    Flags.flag_definition = flag_definition;
    parseArguments(args);
  }
  
  private static void parseArguments(String[] args) throws FlagErrorException {
    // Search for flags in argument array.
    for (int i = 0; i < args.length; i++) {
      final int flag_delimiter_pos = args[i].indexOf(kFlagNameDelimiter);
      if (args[i].startsWith(kFlagStart) && flag_delimiter_pos >= 0) {
        // This argument is a flag. Find the corresponding flag definition.
        final String flag_name = 
          args[i].substring(kFlagStart.length(), flag_delimiter_pos);
        System.out.println("Flag name: " + flag_name);
        final String flag_value = 
          args[i].substring(flag_delimiter_pos + kFlagNameDelimiter.length());
        System.out.println("Flag value: " + flag_value);
        int flag_index = -1;
        
        // Iterate flag definition and set corresponding flag.
        for (int j = 0; j < flag_definition.length; j++) {
          if (flag_definition[j][kFlagNamePos].equals(flag_name)) {
            if (flag_index != -1) {
              throw new FlagErrorException("Flag " +
                                           flag_name +
                                           " has been defined twice.");
            }
            flag_definition[j][kFlagValuePos] = flag_value;
            flag_index = j;
          }
        }
      }
    }
    
    // Check, whether all flags are set now.
    String missing_flags = "";
    for (int i = 0; i < flag_definition.length; i++) {
      if (flag_definition[i][kFlagValuePos] == null) {
        missing_flags += flag_definition[i][kFlagNamePos] + ": " +
                         flag_definition[i][kFlagExplanationPos] + "\n";
      }
    }
    if (!missing_flags.equals("")) {
      throw new FlagErrorException(
          "Please specify values for the following flags:\n" +
          missing_flags);
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
  
  public static boolean isTrue(String flagName) throws FlagErrorException {
    return getFlagValue(flagName).equals(kTrue);
  }
}
