package flags;


public class Flags { 
  private String[][] flag_definition;
  final private int kFlagNamePos = 0;
  final private int kFlagValuePos = 1;
  final private int kFlagExplanationPos = 2;
  final String kFlagStart = "--";
  final String kFlagNameDelimiter = "=";
  
  /** 
   * @throws FlagErrorException 
   * @Param flag_definition: Triple of form 
   *                         {"Flagname", "Default value"/null, "Explanation"}
   * @Param args: argument flags of the main method.
   */
  public Flags(String[][] flag_definition, String[] args)
      throws FlagErrorException {
    this.flag_definition = flag_definition;
    parseArguments(args);
  }
  
  private void parseArguments(String[] args) throws FlagErrorException {
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
  
  public String getFlagValue(String flagName) throws FlagErrorException {
    for (int i = 0; i < flag_definition.length; i++) {
      if (flag_definition[i][kFlagNamePos].equals(flagName)) {
        return flag_definition[i][kFlagValuePos];
      }
    }
    throw new FlagErrorException("Flag " + flagName + " has not been defined");
  }
}
