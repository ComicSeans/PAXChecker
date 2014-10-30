/*******************************************************************************
 * PAXCheckerCMD
 *
 * This software is created under an MIT License. Originally created by
 * Sunnybat, this version has been forked and modified by ComicSeans.
 *
 * Contributors:
 *		SunnyBat
 *		ComicSeans
 *******************************************************************************/

package paxchecker;

import static paxchecker.PrintHandler.verbosePrintln;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Scanner;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/**
 *
 * @author SunnyBat
 * @author ComicSeans
 */
public class PAXChecker {

	public static final String VERSION = "0.0.1";
	private static volatile int secondsBetweenRefresh = 10;
	private static volatile boolean forceRefresh;
	private static final Scanner cmdScanner = new Scanner(System.in);
	private static boolean exitThreads = false;
	private static boolean printingHelp = false;

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
		if(printingHelp)
		{
			return;
		}
		if (!Browser.isCheckingPaxWebsite()
				&& !Browser.isCheckingShowclix()) {
			System.out.println("Program is not checking PAX or Showclix website. Program will now exit.");
			return;
		}
		if(Email.getAddressList().isEmpty())
		{
			System.out.println("No emails to alert! Program will now exit");
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
						input = cmdScanner.next();
					} catch (Exception e) {
						continue;
					}
					switch (input.toLowerCase()) {
					case "exit":
						exitThreads = true;
						System.exit(0);
						break;
					case "testalert":
					case "test":
						sendBackgroundTestEmail();
						break;
					case "refresh":
					case "check":
						forceRefresh = true;
						break;
					default:
						System.out.println("Unknown command: " + input.toLowerCase());
						System.out.println("Commands:");
						System.out.println("exit        - Exit the program");
						System.out.println("testalert   - Send a test email to all addresses");
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
				long startMS;
				int seconds = getRefreshTime();
				// Saves time from accessing volatile variable; can be moved to
				// inside do while
				// if secondsBetweenRefresh can be changed when do while is
				// running

				System.out.println("PAX Ticket Scanning begun on "+seconds+" second(s) interval");
				System.out.println();
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
					else
					{
						System.out.println("Showclix website: no new events");
					}
					System.out.println("Data used: "
							+ DataTracker.getDataUsedMB() + "MB");
					System.out.println();
					while (System.currentTimeMillis() - startMS < (seconds * 1000)) {
						if (forceRefresh) {
							forceRefresh = false;
							break;
						}
						try {
							Thread.sleep(100);
						} catch (InterruptedException iE) {
							iE.printStackTrace();
						}
					}
				}
				System.out.println("Finished!");
			}
		}, "PAXChecker-check-for-tickets");
	}

	/**
	 * Configures PAXCheckerCMD based on command line input
	 * 
	 * @param args
	 * 			cmd line args
	 * @throws ParseException
	 * 			if args cannot be parsed
	 */
	@SuppressWarnings("static-access")
	public static void parseCommandLineArgs(String[] args) throws ParseException {
		Options helpOptions = new Options();
		helpOptions.addOption("help", false, "helptext");
		
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
		options.addOption(OptionBuilder.hasArg()
									   .withArgName("Period between checking for tickets")
									   .withType(Number.class)
									   .create("delay"));
		options.addOption(OptionBuilder.hasArgs()
									   .withArgName("email number to alert")
									   .create("notify"));
		options.addOption(OptionBuilder.hasArg()
									   .withArgName("file of emails to alert")
									   .create("alertfile"));
		
		CommandLineParser parser = new BasicParser();
		
		CommandLine helpCmd = parser.parse(helpOptions, args, true);
		if(helpCmd.hasOption("help") || args.length == 0)
		{
			HelpFormatter hf = new HelpFormatter();
			hf.printHelp("gnu", options);
			printingHelp = true;
			return;
		}
		 
		CommandLine cmd = parser.parse(options, args);

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
		if(cmd.hasOption("notify"))
		{
			String alertEmails[] = cmd.getOptionValues("notify");
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
		if(cmd.hasOption("alertfile"))
		{
			verbosePrintln("processing alert email file....!");
			String filename = cmd.getOptionValue("alertfile");
			try {
				FileInputStream fis = new FileInputStream(filename);
				BufferedReader br = new BufferedReader(new InputStreamReader(fis));
				String line = null;
				while ((line = br.readLine()) != null) {
					verbosePrintln("Adding email to alert: "+line);
					Email.addEmailAddress(line);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
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
