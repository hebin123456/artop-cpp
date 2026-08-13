/**
 * <copyright>
 * 
 * Copyright (c) 2008-2010 See4sys and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 * 
 * Contributors: 
 *     See4sys - Initial API and implementation
 * 
 * </copyright>
 */
package org.eclipse.sphinx.platform.stats;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;
import java.util.PropertyPermission;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.osgi.framework.log.FrameworkLog;
import org.eclipse.osgi.framework.log.FrameworkLogEntry;
import org.eclipse.sphinx.platform.util.PlatformLogUtil;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;
import org.osgi.framework.FrameworkEvent;

/**
 * The FrameworkLog implementation for Eclipse.
 * <p>
 * Clients may extend this class.
 * </p>
 * 
 * @since 0.7.0
 */
public class PerformanceStatsLog implements FrameworkLog {

	private static final String PASSWORD = "-password"; //$NON-NLS-1$	
	/** The session tag */
	protected static final String SESSION = "!SESSION"; //$NON-NLS-1$
	/** The entry tag */
	protected static final String ENTRY = "!ENTRY"; //$NON-NLS-1$
	/** The sub-entry tag */
	protected static final String SUBENTRY = "!SUBENTRY"; //$NON-NLS-1$
	/** The message tag */
	protected static final String MESSAGE = "!MESSAGE"; //$NON-NLS-1$
	/** The stacktrace tag */
	protected static final String STACK = "!STACK"; //$NON-NLS-1$

	/** The line separator used in the log output */
	protected static final String LINE_SEPARATOR;
	/** The tab character used in the log output */
	protected static final String TAB_STRING = "\t"; //$NON-NLS-1$
	private static final String USING_SYSTEM_PROPERTIES_KEY = "osgi.framework.useSystemProperties";//$NON-NLS-1$
	// Constants for rotating log file
	/** The default size a log file can grow before it is rotated */
	public static final int DEFAULT_LOG_SIZE = 1000000;
	/** The default number of backup log files */
	public static final int DEFAULT_LOG_FILES = 10;
	/** The minimum size limit for log rotation */
	public static final int LOG_SIZE_MIN = 1000000;

	/** The system property used to specify the log level */
	public static final String PROP_LOG_LEVEL = "eclipse.log.level"; //$NON-NLS-1$
	/** The system property used to specify size a log file can grow before it is rotated */
	public static final String PROP_LOG_SIZE_MAX = "eclipse.log.size.max"; //$NON-NLS-1$
	/** The system property used to specify the maximum number of backup log files to use */
	public static final String PROP_LOG_FILE_MAX = "eclipse.log.backup.max"; //$NON-NLS-1$
	/** The extension used for log files */
	public static final String LOG_EXT = ".log"; //$NON-NLS-1$
	/** The extension markup to use for backup log files */
	public static final String BACKUP_MARK = ".bak_"; //$NON-NLS-1$
	private static Properties properties;
	public static final String PROP_LOGFILE = "osgi.logfile"; //$NON-NLS-1$
	/** Indicates if the console messages should be printed to the console (System.out) */
	protected boolean consoleLog = false;
	/** Indicates if the next log message is part of a new session */
	protected boolean newSession = true;
	/**
	 * The File object to store messages. This value may be null.
	 */
	protected File outFile;

	/**
	 * The Writer to log messages to.
	 */
	protected Writer writer;

	int maxLogSize = DEFAULT_LOG_SIZE; // The value is in KB.
	int maxLogFiles = DEFAULT_LOG_FILES;
	int backupIdx = 0;

	private int logLevel = FrameworkLogEntry.OK;

	/**
	 * Constructs an EclipseLog which uses the specified File to log messages to
	 * 
	 * @param outFile
	 *            a file to log messages to
	 */
	static {
		String s = System.getProperty("line.separator"); //$NON-NLS-1$
		LINE_SEPARATOR = s == null ? "\n" : s; //$NON-NLS-1$
		Properties systemProperties = System.getProperties();
		String usingSystemProperties = systemProperties.getProperty(USING_SYSTEM_PROPERTIES_KEY);

		if (usingSystemProperties == null || usingSystemProperties.equalsIgnoreCase(Boolean.TRUE.toString())) {
			properties = systemProperties;
		} else {
			// use systemProperties for a snapshot
			// also see requirements in Bundlecontext.getProperty(...))
			properties = new Properties();
			// snapshot of System properties for uses of getProperties who expect to see framework properties set as
			// System properties
			// we need to do this for all system properties because the properties object is used to back
			// BundleContext#getProperty method which expects all system properties to be available
			properties.putAll(systemProperties);
		}
	}

	public PerformanceStatsLog(File outFile) {
		this.outFile = outFile;
		writer = null;
		readLogProperties();
	}

	/**
	 * Constructs an EclipseLog which uses the specified Writer to log messages to
	 * 
	 * @param writer
	 *            a writer to log messages to
	 */
	public PerformanceStatsLog(Writer writer) {
		if (writer == null) {
			// log to System.err by default
			this.writer = logForStream(System.err);
		} else {
			this.writer = writer;
		}
	}

	private Throwable getRoot(Throwable t) {
		Throwable root = null;
		if (t instanceof BundleException) {
			root = ((BundleException) t).getNestedException();
		}
		if (t instanceof InvocationTargetException) {
			root = ((InvocationTargetException) t).getTargetException();
		}
		// skip inner InvocationTargetExceptions and BundleExceptions
		if (root instanceof InvocationTargetException || root instanceof BundleException) {
			Throwable deeplyNested = getRoot(root);
			if (deeplyNested != null) {
				// if we have something more specific, use it, otherwise keep what we have
				root = deeplyNested;
			}
		}
		return root;
	}

	/**
	 * Helper method for writing out argument arrays.
	 * 
	 * @param header
	 *            the header
	 * @param args
	 *            the list of arguments
	 */
	protected void writeArgs(String header, String[] args) throws IOException {
		if (args == null || args.length == 0) {
			return;
		}
		write(header);
		for (int i = 0; i < args.length; i++) {
			// mask out the password argument for security
			if (i > 0 && PASSWORD.equals(args[i - 1])) {
				write(" (omitted)"); //$NON-NLS-1$
			} else {
				write(" " + args[i]); //$NON-NLS-1$
			}
		}
		writeln();
	}

	/**
	 * Returns the session timestamp. This is the time the platform was started
	 * 
	 * @return the session timestamp
	 */
	protected String getSessionTimestamp() {
		// Main should have set the session start-up timestamp so return that.
		// Return the "now" time if not available.
		String ts = getProperty("eclipse.startTime"); //$NON-NLS-1$
		if (ts != null) {
			try {
				return getDate(new Date(Long.parseLong(ts)));
			} catch (NumberFormatException e) {
				// fall through and use the timestamp from right now
			}
		}
		return getDate(new Date());
	}

	/**
	 * Writes the session
	 * 
	 * @throws IOException
	 *             if an error occurs writing to the log
	 */

	@Override
	public void close() {
		try {
			if (writer != null) {
				Writer tmpWriter = writer;
				writer = null;
				tmpWriter.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * If a File is used to log messages to then the File opened and a Writer is created to log messages to.
	 */
	protected void openFile() {
		if (writer == null) {
			if (outFile != null) {
				try {
					writer = logForStream(new FileOutputStream(outFile, true));
				} catch (IOException e) {
					writer = logForStream(System.err);
				}
			} else {
				writer = logForStream(System.err);
			}
		}
	}

	/**
	 * If a File is used to log messages to then the writer is closed.
	 */
	protected void closeFile() {
		if (outFile != null) {
			if (writer != null) {
				try {
					writer.close();
				} catch (IOException e) {
					// we cannot log here; just print the stacktrace.
					e.printStackTrace();
				}
				writer = null;
			}
		}
	}

	@Override
	public void log(FrameworkEvent frameworkEvent) {
		Bundle b = frameworkEvent.getBundle();
		Throwable t = frameworkEvent.getThrowable();
		String entry = b.getSymbolicName() == null ? b.getLocation() : b.getSymbolicName();
		int severity;
		switch (frameworkEvent.getType()) {
		case FrameworkEvent.INFO:
			severity = FrameworkLogEntry.INFO;
			break;
		case FrameworkEvent.ERROR:
			severity = FrameworkLogEntry.ERROR;
			break;
		case FrameworkEvent.WARNING:
			severity = FrameworkLogEntry.WARNING;
			break;
		default:
			severity = FrameworkLogEntry.OK;
		}
		FrameworkLogEntry logEntry = new FrameworkLogEntry(entry, severity, 0, "", 0, t, null); //$NON-NLS-1$
		log(logEntry);
	}

	@Override
	public synchronized void log(FrameworkLogEntry logEntry) {
		if (logEntry == null) {
			return;
		}
		if (!isLoggable(logEntry)) {
			return;
		}
		try {
			checkLogFileSize();
			openFile();
			if (newSession) {

				newSession = false;
			}
			writeLog(0, logEntry);
			writer.flush();
		} catch (Exception e) {
			// any exceptions during logging should be caught
			PlatformLogUtil.logAsError(ResourcesPlugin.getPlugin(), e);
			// we failed to write, so dump log entry to console instead
			try {
				writer = logForStream(System.err);
				writeLog(0, logEntry);
				writer.flush();
			} catch (Exception e2) {
				PlatformLogUtil.logAsError(ResourcesPlugin.getPlugin(), e);
			}
		} finally {
			closeFile();
		}
	}

	@Override
	public synchronized void setWriter(Writer newWriter, boolean append) {
		setOutput(null, newWriter, append);
	}

	@Override
	public synchronized void setFile(File newFile, boolean append) throws IOException {
		if (newFile != null && !newFile.equals(outFile)) {
			// If it's a new file, then reset.
			readLogProperties();
			backupIdx = 0;
		}
		setOutput(newFile, null, append);
		setProperty(PROP_LOGFILE, newFile.getAbsolutePath());
	}

	@Override
	public synchronized File getFile() {
		return outFile;
	}

	@Override
	public void setConsoleLog(boolean consoleLog) {
		this.consoleLog = consoleLog;
	}

	private void setOutput(File newOutFile, Writer newWriter, boolean append) {
		if (newOutFile == null || !newOutFile.equals(outFile)) {
			if (writer != null) {
				try {
					writer.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				writer = null;
			}
			// Append old outFile to newWriter. We only attempt to do this
			// if the current Writer is backed by a File and this is not
			// a new session.
			File oldOutFile = outFile;
			outFile = newOutFile;
			writer = newWriter;
			boolean copyFailed = false;
			if (append && oldOutFile != null && oldOutFile.isFile()) {
				Reader fileIn = null;
				try {
					openFile();
					fileIn = new InputStreamReader(new FileInputStream(oldOutFile), "UTF-8"); //$NON-NLS-1$
					copyReader(fileIn, writer);
				} catch (IOException e) {
					copyFailed = true;
					e.printStackTrace();
				} finally {
					if (fileIn != null) {
						try {
							fileIn.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
						// delete the old file if copying didn't fail
						if (!copyFailed) {
							oldOutFile.delete();
						}
					}
					closeFile();
				}
			}
		}
	}

	private void copyReader(Reader reader, Writer aWriter) throws IOException {
		char buffer[] = new char[1024];
		int count;
		while ((count = reader.read(buffer, 0, buffer.length)) > 0) {
			aWriter.write(buffer, 0, count);
		}
	}

	/**
	 * Returns a date string using the correct format for the log.
	 * 
	 * @param date
	 *            the Date to format
	 * @return a date string.
	 */
	protected String getDate(Date date) {
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		StringBuffer sb = new StringBuffer();
		appendPaddedInt(c.get(Calendar.YEAR), 4, sb).append('-');
		appendPaddedInt(c.get(Calendar.MONTH) + 1, 2, sb).append('-');
		appendPaddedInt(c.get(Calendar.DAY_OF_MONTH), 2, sb).append(' ');
		appendPaddedInt(c.get(Calendar.HOUR_OF_DAY), 2, sb).append(':');
		appendPaddedInt(c.get(Calendar.MINUTE), 2, sb).append(':');
		appendPaddedInt(c.get(Calendar.SECOND), 2, sb).append('.');
		appendPaddedInt(c.get(Calendar.MILLISECOND), 3, sb);
		return sb.toString();
	}

	private StringBuffer appendPaddedInt(int value, int pad, StringBuffer buffer) {
		pad = pad - 1;
		if (pad == 0) {
			return buffer.append(Integer.toString(value));
		}
		int padding = (int) Math.pow(10, pad);
		if (value >= padding) {
			return buffer.append(Integer.toString(value));
		}
		while (padding > value && padding > 1) {
			buffer.append('0');
			padding = padding / 10;
		}
		buffer.append(value);
		return buffer;
	}

	/**
	 * Returns a stacktrace string using the correct format for the log
	 * 
	 * @param t
	 *            the Throwable to get the stacktrace for
	 * @return a stacktrace string
	 */
	protected String getStackTrace(Throwable t) {
		if (t == null) {
			return null;
		}

		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);

		t.printStackTrace(pw);
		// ensure the root exception is fully logged
		Throwable root = getRoot(t);
		if (root != null) {
			pw.println("Root exception:"); //$NON-NLS-1$
			root.printStackTrace(pw);
		}
		return sw.toString();
	}

	/**
	 * Returns a Writer for the given OutputStream
	 * 
	 * @param output
	 *            an OutputStream to use for the Writer
	 * @return a Writer for the given OutputStream
	 */
	protected Writer logForStream(OutputStream output) {
		try {
			return new BufferedWriter(new OutputStreamWriter(output, "UTF-8")); //$NON-NLS-1$
		} catch (UnsupportedEncodingException e) {
			return new BufferedWriter(new OutputStreamWriter(output));
		}
	}

	/**
	 * Writes the log entry to the log using the specified depth. A depth value of 0 idicates that the log entry is the
	 * root entry. Any value greater than 0 indicates a sub-entry.
	 * 
	 * @param depth
	 *            the depth of th entry
	 * @param entry
	 *            the entry to log
	 * @throws IOException
	 *             if any error occurs writing to the log
	 */
	protected void writeLog(int depth, FrameworkLogEntry entry) throws IOException {
		writeEntry(depth, entry);
		writeMessage(entry);
		writeStack(entry);

		FrameworkLogEntry[] children = entry.getChildren();
		if (children != null) {
			for (FrameworkLogEntry element : children) {
				writeLog(depth + 1, element);
			}
		}
	}

	/**
	 * Writes the ENTRY or SUBENTRY header for an entry. A depth value of 0 indicates that the log entry is the root
	 * entry. Any value greater than 0 indicates a sub-entry.
	 * 
	 * @param depth
	 *            the depth of th entry
	 * @param entry
	 *            the entry to write the header for
	 * @throws IOException
	 *             if any error occurs writing to the log
	 */
	protected void writeEntry(int depth, FrameworkLogEntry entry) throws IOException {
		if (depth == 0) {
			writeln(); // write a blank line before all !ENTRY tags bug #64406
			write(ENTRY);
		} else {
			write(SUBENTRY);
			writeSpace();
			write(Integer.toString(depth));
		}
		writeSpace();
		write(entry.getEntry());
		writeSpace();
		write(Integer.toString(entry.getSeverity()));
		writeSpace();
		write(Integer.toString(entry.getBundleCode()));
		writeSpace();
		write(getDate(new Date()));
		writeln();
	}

	/**
	 * Writes the MESSAGE header to the log for the given entry.
	 * 
	 * @param entry
	 *            the entry to write the message for
	 * @throws IOException
	 *             if any error occurs writing to the log
	 */
	protected void writeMessage(FrameworkLogEntry entry) throws IOException {
		write(MESSAGE);
		writeSpace();
		writeln(entry.getMessage());
	}

	/**
	 * Writes the STACK header to the log for the given entry.
	 * 
	 * @param entry
	 *            the entry to write the stacktrace for
	 * @throws IOException
	 *             if any error occurs writing to the log
	 */
	protected void writeStack(FrameworkLogEntry entry) throws IOException {
		Throwable t = entry.getThrowable();
		if (t != null) {
			String stack = getStackTrace(t);
			write(STACK);
			writeSpace();
			write(Integer.toString(entry.getStackCode()));
			writeln();
			write(stack);
		}
	}

	/**
	 * Writes the given message to the log.
	 * 
	 * @param message
	 *            the message
	 * @throws IOException
	 *             if any error occurs writing to the log
	 */
	protected void write(String message) throws IOException {
		if (message != null) {
			writer.write(message);
			if (consoleLog) {
				System.out.print(message);
			}
		}
	}

	/**
	 * Writes the given message to the log and a newline.
	 * 
	 * @param s
	 *            the message
	 * @throws IOException
	 *             if any error occurs writing to the log
	 */
	protected void writeln(String s) throws IOException {
		write(s);
		writeln();
	}

	/**
	 * Writes a newline log.
	 * 
	 * @throws IOException
	 *             if any error occurs writing to the log
	 */
	protected void writeln() throws IOException {
		write(LINE_SEPARATOR);
	}

	/**
	 * Writes a space to the log.
	 * 
	 * @throws IOException
	 *             if any error occurs writing to the log
	 */
	protected void writeSpace() throws IOException {
		write(" "); //$NON-NLS-1$
	}

	/**
	 * Checks the log file size. If the log file size reaches the limit then the log is rotated
	 * 
	 * @return false if an error occured trying to rotate the log
	 */
	protected boolean checkLogFileSize() {
		if (maxLogSize == 0) {
			return true; // no size limitation.
		}

		boolean isBackupOK = true;
		if (outFile != null) {
			if (outFile.length() >> 10 > maxLogSize) { // Use KB as file size unit.
				String logFilename = outFile.getAbsolutePath();

				// Delete old backup file that will be replaced.
				String backupFilename = ""; //$NON-NLS-1$
				if (logFilename.toLowerCase().endsWith(LOG_EXT)) {
					backupFilename = logFilename.substring(0, logFilename.length() - LOG_EXT.length()) + BACKUP_MARK + backupIdx + LOG_EXT;
				} else {
					backupFilename = logFilename + BACKUP_MARK + backupIdx;
				}
				File backupFile = new File(backupFilename);
				if (backupFile.exists()) {
					if (!backupFile.delete()) {
						if (backupFile.renameTo(new File(backupFile.getAbsolutePath() + System.currentTimeMillis()))) {
						} else {
							isBackupOK = false;
						}
					}
				}

				// Rename current log file to backup one.
				boolean isRenameOK = outFile.renameTo(backupFile);
				if (!isRenameOK) {
					isBackupOK = false;
				}
				File newFile = new File(logFilename);
				setOutput(newFile, null, false);

				// Write a new SESSION header to new log file.
				openFile();
				try {

					writeln();
					writeln("This is a continuation of log file " + backupFile.getAbsolutePath());//$NON-NLS-1$
					writeln("Created Time: " + getDate(new Date(System.currentTimeMillis()))); //$NON-NLS-1$
					writer.flush();
				} catch (IOException ioe) {
					PlatformLogUtil.logAsError(ResourcesPlugin.getPlugin(), ioe);
				}
				closeFile();
				backupIdx = ++backupIdx % maxLogFiles;
			}
		}
		return isBackupOK;
	}

	/**
	 * Reads the PROP_LOG_SIZE_MAX and PROP_LOG_FILE_MAX properties.
	 */
	public static String getProperty(String key) {
		SecurityManager sm = System.getSecurityManager();
		if (sm != null) {
			sm.checkPropertyAccess(key);
		}
		return properties.getProperty(key, null);
	}

	public static String setProperty(String key, String value) {
		SecurityManager sm = System.getSecurityManager();
		if (sm != null) {
			sm.checkPermission(new PropertyPermission(key, "write")); //$NON-NLS-1$
		}
		return (String) properties.put(key, value);
	}

	protected void readLogProperties() {
		String newMaxLogSize = getProperty(PROP_LOG_SIZE_MAX);
		if (newMaxLogSize != null) {
			maxLogSize = Integer.parseInt(newMaxLogSize);
			if (maxLogSize != 0 && maxLogSize < LOG_SIZE_MIN) {
				// If the value is '0', then it means no size limitation.
				// Also, make sure no inappropriate(too small) assigned value.
				maxLogSize = LOG_SIZE_MIN;
			}
		}

		String newMaxLogFiles = getProperty(PROP_LOG_FILE_MAX);
		if (newMaxLogFiles != null) {
			maxLogFiles = Integer.parseInt(newMaxLogFiles);
			if (maxLogFiles < 1) {
				// Make sure no invalid assigned value. (at least >= 1)
				maxLogFiles = DEFAULT_LOG_FILES;
			}
		}

		String newLogLevel = getProperty(PROP_LOG_LEVEL);
		if (newLogLevel != null) {
			if (newLogLevel.equals("ERROR")) { //$NON-NLS-1$
				logLevel = FrameworkLogEntry.ERROR;
			} else if (newLogLevel.equals("WARNING")) { //$NON-NLS-1$
				logLevel = FrameworkLogEntry.ERROR | FrameworkLogEntry.WARNING;
			} else if (newLogLevel.equals("INFO")) { //$NON-NLS-1$
				logLevel = FrameworkLogEntry.INFO | FrameworkLogEntry.ERROR | FrameworkLogEntry.WARNING | FrameworkLogEntry.CANCEL;
			} else {
				logLevel = FrameworkLogEntry.OK; // OK (0) means log everything
			}
		}
	}

	/**
	 * Determines if the log entry should be logged based on log level.
	 */
	private boolean isLoggable(FrameworkLogEntry entry) {
		if (logLevel == 0) {
			return true;
		}
		return (entry.getSeverity() & logLevel) != 0;
	}
}
