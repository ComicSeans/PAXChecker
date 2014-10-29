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
	private static boolean exitThreads = false;

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
	 * Starts checking for website updates and listening for commands given
	 * through the console.
	 */
	public static void startCommandLineWebsiteChecking() {
		ThreadHandler.continueProgram(new Runnable() {
			@Override
			public void run() {
				String input;
				while (!exitThreads) {
					try {
						input = myScanner.next();
					} catch (Exception e) {
						continue;
					}
					switch (input.toLowerCase()) {
					case "exit":
						exitThreads = true;
						System.exit(0);
						break;
					case "testtext":
						sendBackgroundTestEmail();
						break;
					case "refresh":
					case "check":
						forceRefresh = true;
						break;
					default:
						System.out.println("Unknown command: "
								+ input.toLowerCase());
						System.out.println("Commands:");
						System.out.println("exit        - Exit the program");
						System.out.println("testtext    - Send a test text");
						System.out.println("check       - Force check");
						System.out.println("Commands are NOT case sensitive.");
						break;
					}
				}
			}
		}, "PAXChecker-input-scanning");
		ThreadHandler.continueProgram(new Runnable() {
			@Override
			public void run() {
				// System.gc();
				long startMS;
				int seconds = getRefreshTime();
				// Saves time from accessing volatile variable; can be moved to
				// inside do while
				// if secondsBetweenRefresh can be changed when do while is
				// running

				while(!exitThreads) {
					startMS = System.currentTimeMillis();
					if (Browser.isPAXWebsiteUpdated()) {
						final String link = Browser.parseHRef(Browser
								.getCurrentButtonLinkLine());
						System.out.println("LINK FOUND: " + link);
						Email.sendEmailInBackground("PAX Tickets ON SALE!",
								"The PAX website has been updated! URL found: "
										+ link);
						exitThreads = true;
						break;
					}
					if (Browser.isShowclixUpdated()) {
						final String link = Browser.getShowclixLink();
						System.out.println("LINK FOUND: " + link);
						Email.sendEmailInBackground("PAX Tickets ON SALE!",
								"The Showclix website has been updated! URL found: "
										+ link);
						exitThreads = true;
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
				}
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
	 * Sends a test email on a daemon Thread. Note that this also updates the
	 * Status window if possible.
	 */
	public static void sendBackgroundTestEmail() {
		Email.testEmail();
	}
	
}
