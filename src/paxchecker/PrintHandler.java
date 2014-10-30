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

/**
 * Handles printing according to verbose flag
 * 
 * @author Sean
 */
public class PrintHandler {

	private static boolean verbose = false;

	/**
	 * 
	 * @return is the verbose flag set
	 */
	public static boolean isVerbose() {
		return verbose;
	}

	/**
	 * 
	 * @param verbose
	 *            set the verbose flag
	 */
	public static void setVerbose(boolean verbose) {
		PrintHandler.verbose = verbose;
	}

	/**
	 * Print if the verbose flag is set
	 * 
	 * @param str
	 *            string to set
	 */
	public static void verbosePrintln(String str) {
		if (verbose) {
			System.out.println(str);
		}
	}

}
