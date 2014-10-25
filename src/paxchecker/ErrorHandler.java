package paxchecker;

/**
 *
 * @author SunnyBat
 */
public class ErrorHandler {

  /**
   * Displays a window clearly indicating something has gone wrong. This should be used only when the program encounters an error that impedes its
   * function, not for notifications to the user.
   *
   * @param message The error message to display to the user
   */
  public static void printError(String message) {
    printError("ERROR", message, null);
  }

  /**
   * Displays a window clearly indicating something has gone wrong. This should be used only when the program encounters an error that impedes its
   * function, not for notifications to the user.
   *
   * @param message The error message to display to the user
   * @param t The error to display
   */
  public static void printError(String message, Throwable t) {
    printError("ERROR", message, t);
  }

  /**
   * Displays a window clearly indicating something has gone wrong. This should be used only when the program encounters an error that impedes its
   * function, not for notifications to the user.
   *
   * @param title The title of the error message
   * @param message The error message to display to the user
   * @param t The error to display
   */
  public static void printError(String title, String message, Throwable t) {
	  System.out.println("PAXChecker ERROR :: " + title + " -- " + message);
	  t.printStackTrace();
  }

  /**
   * Shows the error information of t. It outputs all the information into an {@link Error} window. This should only be accessible from a currently
   * open {@link Error}.
   *
   * @param t The error object
   */
  public static void detailedReport(Throwable t) {
      t.printStackTrace();
  }


}
