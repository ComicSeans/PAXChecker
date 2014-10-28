package paxchecker;

public class PrintHandler {
	
	private static boolean verbose = false;

	public static boolean isVerbose() {
		return verbose;
	}

	public static void setVerbose(boolean verbose) {
		PrintHandler.verbose = verbose;
	}
	
	public static void verbosePrintln(String str)
	{
		if(verbose)
		{
			System.out.println(str);
		}
	}

}
