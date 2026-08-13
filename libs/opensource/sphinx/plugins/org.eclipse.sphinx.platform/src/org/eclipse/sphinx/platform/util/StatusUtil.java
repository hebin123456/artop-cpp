/**
 * <copyright>
 * 
 * Copyright (c) 2008-2010 See4sys, BMW Car IT and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 * 
 * Contributors: 
 *     See4sys - Initial API and implementation
 *     BMW Car IT - Added support for MultiStatus
 * 
 * </copyright>
 */
package org.eclipse.sphinx.platform.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.util.NLS;
import org.eclipse.sphinx.platform.messages.PlatformMessages;
import org.osgi.framework.Bundle;

public class StatusUtil {

	/**
	 * Creates a status object associated with the plug-in.
	 * 
	 * @param severity
	 *            the severity; one of <code>OK</code>, <code>ERROR</code>, <code>INFO</code>, <code>WARNING</code>, or
	 *            <code>CANCEL</code>
	 * @param aCode
	 *            the plug-in-specific status code, or <code>OK</code>
	 * @param aMessage
	 *            a human-readable message, localized to the current locale
	 * @param exception
	 *            a low-level exception, or <code>null</code> if not applicable
	 * @param pluginId
	 *            the unique identifier of the relevant plug-in
	 * @return A status configured with the plug-in id and the given parameters.
	 */
	public static IStatus createStatus(int severity, int aCode, String aMessage, String pluginId, Throwable exception) {
		return new Status(severity, pluginId, aCode, aMessage != null ? aMessage : PlatformMessages.message_none, exception);
	}

	public static IStatus createMultiErrorStatus(Plugin plugin, int aCode, String aMessage, List<Throwable> exceptions, Throwable exception) {
		IStatus[] children = new IStatus[exceptions.size()];
		for (Throwable ex : exceptions) {
			children[exceptions.indexOf(ex)] = createErrorStatus(plugin, ex);
		}
		return new MultiStatus(plugin.getBundle().getSymbolicName(), aCode, children, aMessage != null ? aMessage : PlatformMessages.message_none,
				exception);
	}

	// FIXME (aakar) createErrorStatus methods are ambiguous for the type StatusUtil when calling with null as plugin
	public static IStatus createErrorStatus(Plugin plugin, Object object) {
		return createErrorStatus(plugin != null ? plugin.getBundle() : null, object);
	}

	public static IStatus createErrorStatus(Bundle bundle, Object object) {
		return createStatus(bundle, Collections.singletonList(object), IStatus.ERROR);
	}

	public static IStatus createWarningStatus(Plugin plugin, Object object) {
		return createWarningStatus(plugin != null ? plugin.getBundle() : null, object);
	}

	public static IStatus createWarningStatus(Bundle bundle, Object object) {
		return createStatus(bundle, Collections.singletonList(object), IStatus.WARNING);
	}

	public static IStatus createInfoStatus(Plugin plugin, Object object) {
		return createInfoStatus(plugin != null ? plugin.getBundle() : null, object);
	}

	public static IStatus createInfoStatus(Bundle bundle, Object object) {
		return createStatus(bundle, Collections.singletonList(object), IStatus.INFO);
	}

	/**
	 * Compares two instances of <code>IStatus</code>. The more severe is returned.
	 */
	public static IStatus getMoreSevereStatus(IStatus firstStatus, IStatus secondStatus) {
		if (firstStatus.getSeverity() > secondStatus.getSeverity()) {
			return firstStatus;
		} else {
			return secondStatus;
		}
	}

	/**
	 * Finds the most severe status from a array of status.
	 */
	public static IStatus getMostSevereStatus(IStatus[] status) {
		IStatus max = null;
		for (IStatus curr : status) {
			if (curr.matches(IStatus.ERROR)) {
				return curr;
			}
			if (max == null || curr.getSeverity() > max.getSeverity()) {
				max = curr;
			}
		}
		return max;
	}

	private static IStatus createStatus(Bundle bundle, Collection<Object> objects, int severity) {
		List<IStatus> statuses = new ArrayList<IStatus>();
		for (Object object : objects) {
			statuses.add(convertToStatus(bundle, object, severity));
		}
		return createEnclosingStatus(bundle, statuses);
	}

	private static IStatus convertToStatus(Bundle bundle, Object object, int severity) {
		IStatus status;
		String symbolicName = bundle != null ? bundle.getSymbolicName() : PlatformMessages.pluginId_unknown;

		if (object instanceof MultiStatus) {
			MultiStatus originalStatus = (MultiStatus) object;
			status = new MultiStatus(originalStatus.getPlugin(), originalStatus.getCode(), originalStatus.getChildren(), originalStatus.getMessage(),
					originalStatus.getException());

		} else if (object instanceof IStatus) {
			IStatus originalStatus = (IStatus) object;
			status = new Status(severity, originalStatus.getPlugin(), originalStatus.getCode(), originalStatus.getMessage(),
					originalStatus.getException());

		} else if (object instanceof CoreException) {
			CoreException cex = (CoreException) object;
			IStatus originalStatus = cex.getStatus();
			if (originalStatus.getException() == null) {
				status = new Status(severity, originalStatus.getPlugin(), originalStatus.getCode(), originalStatus.getMessage(), cex);
			} else {
				status = new Status(severity, originalStatus.getPlugin(), originalStatus.getCode(), originalStatus.getMessage(),
						originalStatus.getException());
			}

		} else if (object instanceof OperationCanceledException) {
			OperationCanceledException ocex = (OperationCanceledException) object;
			status = new Status(IStatus.INFO, symbolicName, 0, ocex.getLocalizedMessage(), ocex);

		} else if (object instanceof Throwable) {
			Throwable throwable = (Throwable) object;
			if (throwable.getCause() == null) {
				status = new Status(severity, symbolicName, 0, throwable.getLocalizedMessage(), throwable);
			} else {
				status = new MultiStatus(symbolicName, 0, throwable.getLocalizedMessage(), throwable);
				IStatus causeStatus = new Status(severity, symbolicName, 0, throwable.getCause().getLocalizedMessage(), throwable.getCause());
				((MultiStatus) status).add(causeStatus);
			}

		} else if (object != null) {
			status = new Status(severity, symbolicName, 0, object.toString(), null);
		} else {
			status = new Status(severity, symbolicName, 0, PlatformMessages.message_none, null);
		}

		return status;
	}

	private static IStatus createEnclosingStatus(Bundle bundle, Collection<IStatus> statuses) {
		Assert.isNotNull(statuses);
		Assert.isLegal(statuses.size() > 0);

		boolean isInfoOnly = true;
		for (IStatus status : statuses) {
			if (status.getSeverity() != IStatus.INFO) {
				isInfoOnly = false;
				break;
			}
		}

		String symbolicName = bundle != null ? bundle.getSymbolicName() : PlatformMessages.pluginId_unknown;
		if (isInfoOnly) {
			if (statuses.size() > 1) {
				// Create enclosing info status and add given info statuses
				IStatus enclosingStatus = new MultiStatus(symbolicName, 0, NLS.bind(PlatformMessages.infos_whenInvokingPlugin, statuses.size(),
						symbolicName), null);
				for (IStatus status : statuses) {
					((MultiStatus) enclosingStatus).merge(status);
				}
				return enclosingStatus;
			} else {
				// Don't wrap info status in enclosing status, just leave it as is
				return statuses.iterator().next();
			}
		} else {
			if (statuses.size() > 1) {
				// Create meaningful message for enclosing status
				String msg = ""; //$NON-NLS-1$
				// FIXME aakar create messages
				if (bundle != null) {
					// msg = NLS.bind(PlatformMessages.problems_whenInvokingPlugin, statuses.size(), symbolicName);
				} else {
					// msg = NLS.bind(PlatformMessages.problems_whenInvokingUnknownPlugin, statuses.size());
				}

				// Create enclosing status and add given statuses
				IStatus enclosingStatus = new MultiStatus(symbolicName, 0, msg, null);
				for (IStatus status : statuses) {
					((MultiStatus) enclosingStatus).merge(status);
				}
				return enclosingStatus;
			} else {
				// Create meaningful message for enclosing status
				String msg;
				IStatus status = statuses.iterator().next();
				Throwable throwable = status.getException();
				if (bundle != null) {
					if (throwable != null) {
						msg = NLS.bind(PlatformMessages.error_exceptionWhenInvokingPlugin, new Object[] { throwable.getClass().getSimpleName(),
								symbolicName, throwable.getLocalizedMessage() });
					} else {
						msg = NLS.bind(PlatformMessages.problem_whenInvokingPlugin, symbolicName, status.getMessage());
					}
				} else {
					if (throwable != null) {
						msg = NLS.bind(PlatformMessages.error_exceptionWhenInvokingUnknownPlugin, new Object[] {
								throwable.getClass().getSimpleName(), throwable.getLocalizedMessage() });
					} else {
						msg = NLS.bind(PlatformMessages.problem_whenInvokingUnknownPlugin, status.getMessage());
					}
				}

				// Create enclosing status and add given status
				IStatus enclosingStatus = new MultiStatus(symbolicName, 0, msg, throwable);
				((MultiStatus) enclosingStatus).merge(status);
				return enclosingStatus;
			}
		}
	}
}
