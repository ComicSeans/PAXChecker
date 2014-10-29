package paxchecker;

public class ThreadHandler {

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
	 * This makes a new daemon, low-priority Thread and runs it.
	 *
	 * @param run
	 *            The Runnable to make into a Thread and run
	 */
	public static void startBackgroundThread(Runnable run) {
		startBackgroundThread(run, "General Background Thread");
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
	
	

}
