/**
 * <copyright>
 *
 * Copyright (c) 2011-2014 See4sys, itemis and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 *
 * Contributors:
 *     See4sys - Initial API and implementation
 *     itemis - [424028] Improve AbstractCLIApplication
 *     itemis - [446340] Add support for dynamic execution of MWE workflows such that workflows can have direct access shared model instances already loaded in Sphinx
 *     itemis - [455002] AbstractCLIApplication should not only print Exception message, but also stack trace
 *
 * </copyright>
 */
package org.eclipse.sphinx.platform.cli;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.Map;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.MissingOptionException;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.sphinx.platform.internal.Activator;
import org.eclipse.sphinx.platform.internal.messages.Messages;
import org.eclipse.sphinx.platform.util.PlatformLogUtil;

public abstract class AbstractCLIApplication implements IApplication {

	/**
	 * Error code to indicate that everything is fine.
	 */
	public static final Object ERROR_NO = 0;

	/**
	 * General purpose error code used to indicate that something went wrong.
	 * <p>
	 * Clients typically define application-specific error codes with values greater than <code>1</code> to give a hint
	 * at the cause that has led to a failure. Applications which do not require such sophisticated error handling may
	 * rely on this error code instead.
	 * </p>
	 */
	public static final Object ERROR_UNSPECIFIED = 1;

	/**
	 * Default with used when printing command line usage help.
	 *
	 * @see #printHelp()
	 */
	protected static final int DEFAULT_HELP_WIDTH = 160;

	private String[] applicationArgs;
	private Options options = new Options();
	private CommandLineParser parser;
	private CommandLine commandLine = null;

	protected PrintStream out = System.out;
	protected PrintStream err = System.err;

	/**
	 * Returns the name of this {@link AbstractCLIApplication application}.
	 *
	 * @return The application's name.
	 * @deprecated Use {@link #getCommandLineSyntax()} instead.
	 */
	@Deprecated
	protected String getApplicationName() {
		return null;
	}

	/**
	 * Returns a brief description of what this {@link AbstractCLIApplication application} is doing.
	 *
	 * @return Theapplication's description.
	 */
	protected String getApplicationDescription() {
		return null;
	}

	/**
	 * Returns the command line syntax for this {@link AbstractCLIApplication application}. Is expected to include
	 * relevant Eclipse runtime options, application id, and arguments but not a detailed description of the command
	 * line options. The latter is automatically added upon printing the application's help with {@link #printHelp()}.
	 * E.g.,
	 *
	 * <pre>
	 * eclipse -noSplash -data <workspace location> -application org.example.cli.SomeApplication [options] file
	 * </pre>
	 *
	 * @return The application's command line syntax.
	 * @see #printHelp()
	 */
	protected String getCommandLineSyntax() {
		return getApplicationName();
	}

	/**
	 * Returns the list of arguments passed to this {@link AbstractCLIApplication application}.
	 *
	 * @return The application's argument list.
	 */
	protected String[] getApplicationArgs() {
		return applicationArgs;
	}

	/**
	 * Returns the supported {@link Options command line options}.
	 *
	 * @return The supported command line options.
	 */
	protected Options getOptions() {
		return options;
	}

	/**
	 * Registers given {@link Option option} as supported command line option.
	 *
	 * @param option
	 */
	protected void addOption(Option option) {
		if (option != null) {
			options.addOption(option);
		}
	}

	/**
	 * Returns the {@link CommandLine command line} resulting of the parsing operation on application arguments.
	 *
	 * @return The command line resulting of the parsing operation on application arguments.
	 */
	protected CommandLine getCommandLine() {
		return commandLine;
	}

	/**
	 * Uses given {@link PrintStream print stream} as substitution for {@link System#out} and uses it for all subsequent
	 * writes to the "standard" output stream.
	 *
	 * @param out
	 *            The print stream to be used instead of {@link System#out}.
	 * @see System#out
	 */
	public void setOutputStream(PrintStream out) {
		this.out = out;
	}

	/**
	 * Uses given {@link PrintStream print stream} as substitution for {@link System#err} and uses it for all subsequent
	 * writes to the "standard" error output stream.
	 *
	 * @param err
	 *            The print stream to be used instead of {@link System#err}.
	 * @see System#err
	 */
	public void setErrorStream(PrintStream err) {
		this.err = err;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.equinox.app.IApplication#start(org.eclipse.equinox.app.IApplicationContext)
	 */
	@Override
	public Object start(IApplicationContext context) {
		// Initialize application arguments
		initApplicationArgs(context);

		// Run application
		return doRun();
	}

	/*
	 * @see org.eclipse.equinox.app.IApplication#stop()
	 */
	@Override
	public void stop() {
	}

	/**
	 * Alternative entry point that can be used to invoke this {@link AbstractCLIApplication application} from
	 * non-Eclipse headless contexts (e.g., Ant task, Java standalone application).
	 *
	 * @param args
	 *            The arguments for the application. The content of this argument will be checked for if it conforms to
	 *            the expectations of the application being invoked.
	 * @return The return code of the application indicating normal or abnormal termination.
	 */
	public Object run(String[] args) {
		// Initialize application arguments
		initApplicationArgs(args);

		// Run application
		return doRun();
	}

	/**
	 * Common run method to which Eclipse headless and non-Eclipse headless invocations of this
	 * {@link AbstractCLIApplication application} get redirected.
	 *
	 * @return The return code of the application indicating normal or abnormal termination.
	 */
	private Object doRun() {
		try {
			// Definition stage
			defineOptions();

			// Parsing stage
			parse();

			// Interrogation stage
			return interrogate();

		} catch (Throwable t) {
			return handleError(t);
		}
	}

	/**
	 * Initializes the application arguments from given {@link IApplicationContext application context}.
	 *
	 * @param context
	 *            The application context that has been passed to this application.
	 */
	private void initApplicationArgs(IApplicationContext context) {
		Map<?, ?> arguments = context.getArguments();
		Object args = arguments.get(IApplicationContext.APPLICATION_ARGS);
		applicationArgs = args instanceof String[] ? (String[]) args : new String[0];
	}

	/**
	 * Initializes the application arguments from given <code>args</code>.
	 *
	 * @param args
	 *            The arguments that have been passed to this application.
	 */
	private void initApplicationArgs(String[] args) {
		applicationArgs = args != null ? args : new String[0];
	}

	/**
	 * Returns the set of command line {@link Option option}s to be used for parsing the application arguments. The
	 * command line usage help option is supported by default.
	 * <p>
	 * Clients should override this method to provide their own command line options.
	 * </p>
	 *
	 * @see http://commons.apache.org/cli/usage.html
	 */
	protected void defineOptions() {
		addOption(new Option(ICommonCLIConstants.OPTION_HELP, ICommonCLIConstants.OPTION_HELP_DESCRIPTION));
	}

	/**
	 * Parses the application arguments based on the command line {@link Option}s supported by this
	 * {@link AbstractCLIApplication application}.
	 *
	 * @throws ParseException
	 * @see {@link #defineOptions()}
	 */
	protected void parse() throws ParseException {
		commandLine = getParser().parse(getOptions(), getApplicationArgs());
	}

	/**
	 * Returns the {@link CommandLineParser command line parser} to be used for analyzing the application arguments.
	 *
	 * @return The command line parser to be used for analyzing application arguments.
	 */
	protected CommandLineParser getParser() {
		if (parser == null) {
			parser = createParser();
		}
		return parser;
	}

	/**
	 * Creates an instance of the {@link CommandLineParser command line parser} to be used for analyzing the application
	 * arguments. Three command line parser types are available out-of-the box: {@link BasicParser}, {@link GnuParser},
	 * {@link PosixParser}. In addition, clients are free to provide and use their own command line parser
	 * implementations.
	 *
	 * @return The command line parser instance to be used for analyzing application arguments.
	 * @see #defineOptions()
	 * @see http://commons.apache.org/cli/usage.html
	 */
	protected CommandLineParser createParser() {
		return new PosixParser() {
			@Override
			protected void checkRequiredOptions() throws MissingOptionException {
				// Don't complain about missing required options when command line usage help needs to be printed
				if (isHelpNeeded(cmd)) {
					return;
				}
				super.checkRequiredOptions();
			}
		};
	}

	/**
	 * Implements the interrogation stage that is invoked after the parsing stage analyzing the command line options. It
	 * is the place where this {@link AbstractCLIApplication application}'s semantics according to the command line
	 * options that have been detected in the application arguments needs to be defined. The semantics for the command
	 * line usage help option is implemented by default.
	 * <p>
	 * Clients should override this method to implement the semantics for their own command line options.
	 * </p>
	 *
	 * @see #defineOptions()
	 * @see http://commons.apache.org/cli/usage.html
	 */
	protected Object interrogate() throws Throwable {
		if (isHelpNeeded(getCommandLine())) {
			printHelp();
			throw new OperationCanceledException();
		}
		return ERROR_NO;
	}

	/**
	 * Determines if command line usage help needs to be printed.
	 *
	 * @return <code>true</code> if command line usage help needs to be printed, <code>false</code> otherwise.
	 */
	protected boolean isHelpNeeded(CommandLine commandLine) {
		return commandLine.getOptions().length == 0 || commandLine.hasOption(ICommonCLIConstants.OPTION_HELP);
	}

	/**
	 * Prints the command line usage help.
	 */
	protected void printHelp() {
		// Print optional application description
		String description = getApplicationDescription();
		if (description != null && description.length() > 0) {
			out.println(description);
			out.println();
		}

		// Print help including usage with command line syntax and description of supported options
		HelpFormatter formatter = new HelpFormatter();
		formatter.setSyntaxPrefix(Messages.cliHelp_usagePrefix);
		PrintWriter pw = new PrintWriter(out);
		formatter.printHelp(pw, DEFAULT_HELP_WIDTH, getCommandLineSyntax(), "\n" + Messages.cliHelp_optionsHeader, getOptions(), //$NON-NLS-1$
				HelpFormatter.DEFAULT_LEFT_PAD, HelpFormatter.DEFAULT_DESC_PAD, null);
		pw.flush();
	}

	/**
	 * Central place for handling errors occurring while this {@link AbstractCLIApplication application} is run.
	 *
	 * @param throwable
	 *            The {@link Throwable exception} describing the error that has occurred.
	 * @return The corresponding error code to be returned by this application.
	 */
	protected Object handleError(Throwable throwable) {
		if (throwable instanceof OperationCanceledException) {
			return ERROR_NO;
		} else if (throwable instanceof ParseException) {
			err.println(throwable.getMessage());
			err.println(Messages.cliHelp_useHelpOptionForMoreInformation);
		} else {
			err.println(throwable.getMessage());
			PlatformLogUtil.logAsError(Activator.getDefault(), throwable);
		}
		return ERROR_UNSPECIFIED;
	}
}
