package paxchecker;

import java.util.Arrays;
import java.util.Scanner;

import static paxchecker.PrintHandler.verbosePrintln;

import org.apache.commons.cli.*;

/**
 *
 * @author SunnyBat
 */
public class PAXChecker {

	public static final String VERSION = "0.0.1";
	private static volatile int secondsBetweenRefresh = 10;
	private static volatile boolean forceRefresh;
	private static final Scanner myScanner = new Scanner(System.in);

	/**
	 * @param args
	 *            the command line arguments
	 */
	public static void main(String[] args) {
		System.out.println("Initializing...");
		Email.init();
		try {
			parseCommandLineArgs(args);
		} catch (ParseException e) {
	        System.err.println( "Parsing failed.  Reason: " + e.getMessage() );
	        return;
		}
		if (!Browser.isCheckingPaxWebsite()
				&& !Browser.isCheckingShowclix()) {
			System.out.println("ERROR: Program is not checking PAX or Showclix website. Program will now exit.");
			return;
		}
		startCommandLineWebsiteChecking();
		
	}
	

	/**
	 * Prompts the user for the required program information, including
	 * username, password, email, and other options. Note that this does NOT
	 * start the command-line website checking.
	 */
//	public static void promptUserForMissingInput() {
//		if (Email.getUsername() == null) {
//			System.out.print("Email: ");
//			Email.setUsername(myScanner.next());
//			System.out.println("Password: ");
//			Email.setPassword(myScanner.next());
//		}
//		if (Email.getAddressList().isEmpty()) {
//			System.out.print("Cell Number: ");
//			Email.addEmailAddress(myScanner.next());
//			System.out.println();
//		}
////		if (Browser.isCheckingPaxWebsite()) {
////			System.out.print("Check PAX Website (Y/N): ");
////			if (myScanner.next().toLowerCase().length() == 0 || myScanner.next().toLowerCase().charAt(0) != 'n') {
////				Browser.enablePaxWebsiteChecking();
////			}
////			System.out.println();
////		}
////		if (Browser.isCheckingPaxWebsite()) {
////			System.out.print("Check Showclix Website (Y/N): ");
////			try {
////				if (myScanner.next().toLowerCase().length() == 0 || myScanner.next().toLowerCase().charAt(0) != 'n') {
////					Browser.enableShowclixWebsiteChecking();
////				}
////				System.out.println();
////			} catch (Exception e) {
////			}
////		}
////		if (getRefreshTime() == 10) {
////			System.out
////					.print("Refresh Time (seconds, no input limit at the moment): ");
////			try {
////				setRefreshTime(Integer.parseInt(myScanner.next(), 10));
////				System.out.println();
////			} catch (Exception e) {
////			}
////		}
//		if (Browser.getExpo() == null) {
//			System.out.print("Expo: ");
//			String input = myScanner.next();
//			switch (input.toLowerCase().replaceAll(" ", "")) {
//			case "prime":
//			case "paxprime":
//				Browser.setExpo("PAX Prime");
//				break;
//			case "east":
//			case "paxeast":
//				Browser.setExpo("PAX East");
//				break;
//			case "south":
//			case "paxsouth":	
//				Browser.setExpo("PAX South");
//				break;
//			case "aus":
//			case "australia":
//			case "paxaus":
//			case "paxaustralia":
//			case "thelanddownunder":
//				Browser.setExpo("PAX Aus");
//				break;
//			default:
//				System.out.println("Invalid expo! Setting to Prime...");
//				Browser.setExpo("PAX Prime");
//				break;
//			}
//			System.out.println("Set to search for Expo " + Browser.getExpo());
//			System.out.println();
//		}
//	}

	/**
	 * Starts checking for website updates and listening for commands given
	 * through the console.
	 */
	public static void startCommandLineWebsiteChecking() {
		continueProgram(new Runnable() {
			@Override
			public void run() {
				String input;
				while (true) {
					try {
						input = myScanner.next();
					} catch (Exception e) {
						// e.printStackTrace();
						// System.out.println("Error parsing input -- please try again.");
						continue;
					}
					switch (input.toLowerCase()) {
					case "exit":
						System.exit(0);
						break;
					case "testtext":
						sendBackgroundTestEmail();
						break;
					case "refresh":
						break;
					case "check":
						forceRefresh = true;
						break;
					default:
						System.out.println("Unknown command: "
								+ input.toLowerCase());
						System.out.println("Commands:");
						System.out.println("exit        - Exit the program");
						System.out.println("testtext    - Send a test text");
						System.out.println("refresh     - Force check");
						System.out.println("check       - Force check");
						System.out.println("Commands are NOT case sensitive.");
					}
				}
			}
		}, "PAXChecker-input-scanning");
		continueProgram(new Runnable() {
			@Override
			public void run() {
				// System.gc();
				long startMS;
				int seconds = getRefreshTime();
				// Saves time from accessing volatile variable; can be moved to
				// inside do while
				// if secondsBetweenRefresh can be changed when do while is
				// running

				do {
					startMS = System.currentTimeMillis();
					if (Browser.isPAXWebsiteUpdated()) {
						final String link = Browser.parseHRef(Browser
								.getCurrentButtonLinkLine());
						System.out.println("LINK FOUND: " + link);
						Email.sendEmailInBackground("PAX Tickets ON SALE!",
								"The PAX website has been updated! URL found: "
										+ link);
						Browser.openLinkInBrowser(link);
						// Audio.playAlarm();
						break;
					}
					if (Browser.isShowclixUpdated()) {
						final String link = Browser.getShowclixLink();
						System.out.println("LINK FOUND: " + link);
						Email.sendEmailInBackground("PAX Tickets ON SALE!",
								"The Showclix website has been updated! URL found: "
										+ link);
						// Browser.openLinkInBrowser(link);
						// Audio.playAlarm();
						break;
					}
					System.out.println("Data used: "
							+ DataTracker.getDataUsedMB() + "MB");
					while (System.currentTimeMillis() - startMS < (seconds * 1000)) {
						if (forceRefresh) {
							forceRefresh = false;
							break;
						}
						try {
							Thread.sleep(100);
						} catch (InterruptedException iE) {
						}
					}
				} while (true); // Change later
				System.out.println("Finished!");
			}
		}, "PAXChecker-check-for-tickets");
	}

	@SuppressWarnings("static-access")
	public static void parseCommandLineArgs(String[] args) throws ParseException {		
		Options options = new Options();
		options.addOption("nopax", false, "do not check pax website for tickets");
		options.addOption("noshowclix", false, "do not check pax website for tickets");
		options.addOption("v", false, "verbose");
		options.addOption(OptionBuilder.hasArg()
									   .isRequired()
									   .withArgName("email address to send alerts from")
									   .create("email"));
		options.addOption(OptionBuilder.hasArg()
									   .isRequired()
									   .withArgName("password to email address to send alerts from")
									   .create("password"));
		options.addOption(OptionBuilder.hasArg()
									   .isRequired()
									   .withArgName("Which PAX Expo to check")
									   .create("expo"));
//		options.addOption("delay", true, "Period between checking for tickets");
		options.addOption(OptionBuilder.hasArg()
									   .withArgName("Period between checking for tickets")
									   .withType(Number.class)
									   .create("delay"));
		options.addOption(OptionBuilder.hasArgs()
									   .isRequired()
									   .withArgName("cell number to alert")
									   .create("alert"));
		
		CommandLineParser parser = new BasicParser();
		CommandLine cmd;
		
		cmd = parser.parse(options, args);

		
		PrintHandler.setVerbose(cmd.hasOption("v"));
		if(!cmd.hasOption("nopax"))
		{
			Browser.enablePaxWebsiteChecking();
		}
		if(!cmd.hasOption("noshowclix"))
		{
			Browser.enableShowclixWebsiteChecking();
		}
		if(cmd.hasOption("email"))
		{
			Email.setUsername(cmd.getOptionValue("email"));
			verbosePrintln("Username set to " + Email.getUsername());
		}
		if(cmd.hasOption("password"))
		{
			Email.setPassword(cmd.getOptionValue("password"));
			verbosePrintln("Password set");
		}
		if(cmd.hasOption("alert"))
		{
			String alertEmails[] = cmd.getOptionValues("alert");
			for(String alertEmail : alertEmails)
			{
				verbosePrintln("Adding email address to alert" + alertEmail);
				Email.addEmailAddress(alertEmail);
			}
		}
		if(cmd.hasOption("expo"))
		{
			Browser.setExpo(cmd.getOptionValue("expo"));
			verbosePrintln("Expo set to " + Browser.getExpo());
		}
		if(cmd.hasOption("delay"))
		{
			setRefreshTime(((Number)cmd.getParsedOptionValue("delay")).intValue());
			verbosePrintln("Set refresh time to "+ getRefreshTime());
		}
	}

	/**
	 * Gets the time (in seconds) between website checks. This method is
	 * thread-safe.
	 *
	 * @return The amount of time between website checks
	 */
	public static int getRefreshTime() {
		return secondsBetweenRefresh;
	}

	/**
	 * Creates a new non-daemon Thread with the given Runnable object.
	 *
	 * @param run
	 *            The Runnable object to use
	 */
	public static void continueProgram(Runnable run, String name) {
		Thread newThread = new Thread(run, name);
		newThread.setDaemon(false); // Prevent the JVM from stopping due to zero
									// non-daemon threads running
		newThread.setPriority(Thread.NORM_PRIORITY);
		newThread.start(); // Start the Thread
	}

	/**
	 * Sets the time between checking the PAX Registration website for updates.
	 * This can be called at any time, however it is recommended to only call it
	 * during Setup.
	 *
	 * @param seconds
	 *            The amount of seconds between website updates.
	 */
	public static void setRefreshTime(int seconds) {
		secondsBetweenRefresh = seconds;
	}

	/**
	 * This makes a new daemon, low-priority Thread and runs it.
	 *
	 * @param run
	 *            The Runnable to make into a Thread and run
	 */
	public static void startBackgroundThread(Runnable run) {
		startBackgroundThread(run, "General Background Thread");
	}

	/**
	 * Starts a new daemon Thread.
	 *
	 * @param run
	 *            The Runnable object to use
	 * @param name
	 *            The name to give the Thread
	 */
	public static void startBackgroundThread(Runnable run, String name) {
		Thread newThread = new Thread(run, name);
		newThread.setDaemon(true); // Kill the JVM if only daemon threads are
									// running
		newThread.setPriority(Thread.MIN_PRIORITY); // Let other Threads take
													// priority, as this will
													// probably not run for long
		newThread.start(); // Start the Thread
	}

	/**
	 * Saves program Preferences in the background. This uses the currently set
	 * values within the program (ex: current username, current password, etc).
	 */
	public static void savePrefsInBackground() {
		startBackgroundThread(new Runnable() {
			@Override
			public void run() {
				SettingsHandler.saveAllPrefs();
			}
		}, "Save Preferences");
	}

	/**
	 * Sends a test email on a daemon Thread. Note that this also updates the
	 * Status window if possible.
	 */
	public static void sendBackgroundTestEmail() {
		Email.testEmail();
	}
	
}
