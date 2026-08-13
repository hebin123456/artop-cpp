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
package org.eclipse.sphinx.tests.platform.util;

import junit.framework.TestCase;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.util.NLS;
import org.eclipse.sphinx.platform.messages.PlatformMessages;
import org.eclipse.sphinx.platform.util.ReflectUtil;
import org.eclipse.sphinx.platform.util.StatusUtil;
import org.eclipse.sphinx.tests.platform.internal.Activator;
import org.osgi.framework.Bundle;

@SuppressWarnings("nls")
public class StatusUtilTest extends TestCase {

	Bundle testBundle = Activator.getDefault().getBundle();

	InterruptedException interruptedEx = null;

	CoreException coreEx = null;

	OperationCanceledException opCancelEx = null;
	Throwable throwableEx = null;
	Throwable throwableEx_noCause = null;

	String message = "Status Message";

	IStatus cancel_interruptedEx = new Status(IStatus.CANCEL, testBundle.getSymbolicName(), 10, message, interruptedEx);
	IStatus cancel_throwableEx = new Status(IStatus.CANCEL, testBundle.getSymbolicName(), 10, message, throwableEx);

	IStatus error_coreEx = new Status(IStatus.ERROR, testBundle.getSymbolicName(), 10, message, coreEx);
	IStatus error_throwableEx = new Status(IStatus.ERROR, testBundle.getSymbolicName(), 10, message, throwableEx);

	IStatus info_opCancelEx = new Status(IStatus.INFO, testBundle.getSymbolicName(), 10, message, opCancelEx);

	IStatus warning_interruptedEx = new Status(IStatus.WARNING, testBundle.getSymbolicName(), 10, message, interruptedEx);

	IStatus ok_interruptedEx = new Status(IStatus.OK, testBundle.getSymbolicName(), 10, message, interruptedEx);

	@Override
	protected void setUp() throws Exception {
		interruptedEx = new InterruptedException("Interrupted Exception");

		coreEx = new CoreException(error_coreEx);
		coreEx.initCause(interruptedEx);
		// Create a status with a not null exception.
		ReflectUtil.invokeInvisibleMethod(error_coreEx, "setException", coreEx);
		opCancelEx = new OperationCanceledException("Operation Cancel Exception");
		opCancelEx.initCause(interruptedEx);

		throwableEx = new Throwable("Throwable Exception");
		throwableEx.initCause(coreEx);
		assertNotNull(throwableEx.getCause());

		throwableEx_noCause = new Throwable("Throwable Exception without cause");
		throwableEx_noCause.initCause(null);
		assertNull(throwableEx_noCause.getCause());
	}

	protected IStatus getFirstChildStatus(IStatus status) {
		if (status instanceof MultiStatus && ((MultiStatus) status).getChildren().length > 0) {
			status = ((MultiStatus) status).getChildren()[0];
		}
		return status;
	}

	protected void assertStatusEquals(int expectedSeverity, IStatus expectedStatus, IStatus actualStatus) {
		assertEquals(expectedStatus.getCode(), actualStatus.getCode());
		assertEquals(expectedStatus.getPlugin(), actualStatus.getPlugin());
		assertEquals(expectedSeverity, actualStatus.getSeverity());
		assertEquals(expectedStatus.getMessage(), actualStatus.getMessage());
		assertEquals(expectedStatus.getException(), expectedStatus.getException());

	}

	/**
	 * Test method for {@link StatusUtil#createErrorStatus(org.osgi.framework.Bundle, Object)}
	 */

	public void testCreateErrorStatus() {
		assertNotNull(testBundle);
		IStatus testStatusChild;
		IStatus testStatus;

		// --------------------------------------------STATUS-------------------------------------------------------
		// Create from CANCEL Status
		Throwable nullObject = null;
		testStatus = StatusUtil.createErrorStatus(testBundle, cancel_throwableEx);

		assertTrue(testStatus instanceof MultiStatus);
		assertStatusEquals(
				IStatus.ERROR,
				new MultiStatus(testBundle.getSymbolicName(), 0, NLS.bind(PlatformMessages.problem_whenInvokingPlugin, testBundle.getSymbolicName(),
						cancel_throwableEx.getMessage()), nullObject), testStatus);

		testStatusChild = getFirstChildStatus(testStatus);
		assertStatusEquals(IStatus.ERROR, cancel_throwableEx, testStatusChild);

		// Create from ERROR status
		testStatus = StatusUtil.createErrorStatus(testBundle, error_coreEx);
		assertTrue(testStatus instanceof MultiStatus);
		assertStatusEquals(
				IStatus.ERROR,
				new MultiStatus(testBundle.getSymbolicName(), 0, NLS.bind(PlatformMessages.error_exceptionWhenInvokingPlugin, new Object[] {
						coreEx.getClass().getSimpleName(), testBundle.getSymbolicName(), coreEx.getLocalizedMessage() }), nullObject), testStatus);

		testStatusChild = getFirstChildStatus(testStatus);
		assertStatusEquals(IStatus.ERROR, error_coreEx, testStatusChild);

		// create from WARNING Status
		testStatus = StatusUtil.createErrorStatus(testBundle, warning_interruptedEx);
		assertTrue(testStatus instanceof MultiStatus);
		assertStatusEquals(
				IStatus.ERROR,
				new MultiStatus(testBundle.getSymbolicName(), 0, NLS.bind(PlatformMessages.problem_whenInvokingPlugin, testBundle.getSymbolicName(),
						warning_interruptedEx.getMessage()), nullObject), testStatus);

		testStatusChild = getFirstChildStatus(testStatus);
		assertStatusEquals(IStatus.ERROR, warning_interruptedEx, testStatusChild);

		// create from INFO status
		testStatus = StatusUtil.createErrorStatus(testBundle, info_opCancelEx);
		assertTrue(testStatus instanceof MultiStatus);
		assertStatusEquals(
				IStatus.ERROR,
				new MultiStatus(testBundle.getSymbolicName(), 0, NLS.bind(PlatformMessages.problem_whenInvokingPlugin, testBundle.getSymbolicName(),
						info_opCancelEx.getMessage()), nullObject), testStatus);

		testStatusChild = getFirstChildStatus(testStatus);
		assertStatusEquals(IStatus.ERROR, info_opCancelEx, testStatusChild);

		// create from OK Status
		testStatus = StatusUtil.createErrorStatus(testBundle, ok_interruptedEx);
		assertTrue(testStatus instanceof MultiStatus);
		assertStatusEquals(
				IStatus.ERROR,
				new MultiStatus(testBundle.getSymbolicName(), 0, NLS.bind(PlatformMessages.problem_whenInvokingPlugin, testBundle.getSymbolicName(),
						ok_interruptedEx.getMessage()), nullObject), testStatus);

		testStatusChild = getFirstChildStatus(testStatus);
		assertStatusEquals(IStatus.ERROR, ok_interruptedEx, testStatusChild);

		// -----------------------------------------EXCEPTION---------------------------------------------------------
		// Create from CoreException
		testStatus = StatusUtil.createErrorStatus(testBundle, coreEx);
		assertTrue(testStatus instanceof MultiStatus);
		assertStatusEquals(
				IStatus.ERROR,
				new MultiStatus(testBundle.getSymbolicName(), 0, NLS.bind(PlatformMessages.error_exceptionWhenInvokingPlugin, new Object[] {
						coreEx.getClass().getSimpleName(), testBundle.getSymbolicName(), coreEx.getLocalizedMessage() }), coreEx), testStatus);

		testStatusChild = getFirstChildStatus(testStatus);
		assertStatusEquals(IStatus.ERROR, new Status(IStatus.ERROR, coreEx.getStatus().getPlugin(), coreEx.getStatus().getCode(), coreEx.getStatus()
				.getMessage(), coreEx.getStatus().getException()), testStatusChild);

		// Create from Operation Cancel Exception
		testStatus = StatusUtil.createErrorStatus(testBundle, opCancelEx);
		assertStatusEquals(IStatus.INFO, new Status(IStatus.INFO, testBundle.getSymbolicName(), 0, opCancelEx.getLocalizedMessage(), opCancelEx),
				testStatus);

		// Create from Throwable
		// ------------Throwable with Cause
		testStatus = StatusUtil.createErrorStatus(testBundle, throwableEx);
		assertTrue(testStatus instanceof MultiStatus);
		assertStatusEquals(
				IStatus.ERROR,
				new MultiStatus(testBundle.getSymbolicName(), 0, NLS.bind(PlatformMessages.error_exceptionWhenInvokingPlugin, new Object[] {
						throwableEx.getClass().getSimpleName(), testBundle.getSymbolicName(), throwableEx.getLocalizedMessage() }), throwableEx),
				testStatus);

		testStatusChild = getFirstChildStatus(testStatus);
		assertStatusEquals(IStatus.ERROR, new Status(IStatus.ERROR, testBundle.getSymbolicName(), 0, throwableEx.getCause().getLocalizedMessage(),
				throwableEx.getCause()), testStatusChild);

		// ------------Throwable without Cause
		testStatus = StatusUtil.createErrorStatus(testBundle, throwableEx_noCause);
		assertTrue(testStatus instanceof MultiStatus);
		assertStatusEquals(
				IStatus.ERROR,
				new MultiStatus(testBundle.getSymbolicName(), 0, NLS.bind(PlatformMessages.error_exceptionWhenInvokingPlugin, new Object[] {
						throwableEx_noCause.getClass().getSimpleName(), testBundle.getSymbolicName(), throwableEx_noCause.getLocalizedMessage() }),
						throwableEx_noCause), testStatus);

		testStatusChild = getFirstChildStatus(testStatus);
		assertStatusEquals(IStatus.ERROR, new Status(IStatus.ERROR, testBundle.getSymbolicName(), 0, throwableEx_noCause.getLocalizedMessage(),
				throwableEx_noCause), testStatusChild);

		// -------------------------------------------OTHERS--------------------------------------------------------
		// Create from Others objects
		String object = "Information";
		testStatus = StatusUtil.createErrorStatus(testBundle, object);
		assertTrue(testStatus instanceof MultiStatus);
		assertStatusEquals(
				IStatus.ERROR,
				new MultiStatus(testBundle.getSymbolicName(), 0, NLS.bind(PlatformMessages.problem_whenInvokingPlugin, testBundle.getSymbolicName(),
						object.toString()), null), testStatus);

		testStatusChild = getFirstChildStatus(testStatus);
		assertStatusEquals(IStatus.ERROR, new Status(IStatus.ERROR, testBundle.getSymbolicName(), 0, object.toString(), null), testStatusChild);

		// Create from NULL object
		testStatus = StatusUtil.createErrorStatus(testBundle, null);
		assertTrue(testStatus instanceof MultiStatus);
		assertStatusEquals(
				IStatus.ERROR,
				new MultiStatus(testBundle.getSymbolicName(), 0, NLS.bind(PlatformMessages.problem_whenInvokingPlugin, testBundle.getSymbolicName(),
						PlatformMessages.message_none), null), testStatus);

		testStatusChild = getFirstChildStatus(testStatus);
		assertStatusEquals(IStatus.ERROR, new Status(IStatus.ERROR, testBundle.getSymbolicName(), 0, PlatformMessages.message_none, null),
				testStatusChild);

		// Create from Unknown Bundle and unknown Error
		testStatus = StatusUtil.createErrorStatus((Plugin) null, null);
		assertTrue(testStatus instanceof MultiStatus);
		assertStatusEquals(
				IStatus.ERROR,
				new MultiStatus(PlatformMessages.pluginId_unknown, 0, NLS.bind(PlatformMessages.problem_whenInvokingUnknownPlugin,
						PlatformMessages.message_none), null), testStatus);

		testStatusChild = getFirstChildStatus(testStatus);
		assertStatusEquals(IStatus.ERROR, new Status(IStatus.ERROR, PlatformMessages.pluginId_unknown, 0, PlatformMessages.message_none, null),
				testStatusChild);

		// Create from Unknown Bundle and an Error
		testStatus = StatusUtil.createErrorStatus((Plugin) null, coreEx);
		assertTrue(testStatus instanceof MultiStatus);
		assertStatusEquals(
				IStatus.ERROR,
				new MultiStatus(PlatformMessages.pluginId_unknown, 0, NLS.bind(PlatformMessages.error_exceptionWhenInvokingUnknownPlugin,
						new Object[] { coreEx.getClass().getSimpleName(), coreEx.getLocalizedMessage() }), null), testStatus);

		testStatusChild = getFirstChildStatus(testStatus);
		assertStatusEquals(IStatus.ERROR, new Status(IStatus.ERROR, coreEx.getStatus().getPlugin(), coreEx.getStatus().getCode(), coreEx.getStatus()
				.getMessage(), coreEx.getStatus().getException()), testStatusChild);

	}

	public void testCreateInfoStatus() {
		IStatus testStatusChild;
		IStatus testStatus;

		// ----------------------------------------------STATUS-------------------------------------------------------
		// Create from CANCEL Status
		testStatus = StatusUtil.createInfoStatus(testBundle, cancel_throwableEx);
		assertStatusEquals(IStatus.INFO, cancel_throwableEx, testStatus);

		// Create from ERROR status
		testStatus = StatusUtil.createInfoStatus(testBundle, error_coreEx);
		assertStatusEquals(IStatus.INFO, error_coreEx, testStatus);

		// create from WARNING Status
		testStatus = StatusUtil.createInfoStatus(testBundle, warning_interruptedEx);
		assertStatusEquals(IStatus.INFO, warning_interruptedEx, testStatus);

		// create from INFO status
		testStatus = StatusUtil.createInfoStatus(testBundle, info_opCancelEx);
		assertStatusEquals(IStatus.INFO, info_opCancelEx, testStatus);

		// create from OK Status
		testStatus = StatusUtil.createInfoStatus(testBundle, ok_interruptedEx);
		assertStatusEquals(IStatus.INFO, ok_interruptedEx, testStatus);

		// ---------------------------------------------EXCEPTION--------------------------------------------------------------------
		// Create from CoreException
		testStatus = StatusUtil.createInfoStatus(testBundle, coreEx);
		assertStatusEquals(IStatus.INFO, new Status(IStatus.ERROR, coreEx.getStatus().getPlugin(), coreEx.getStatus().getCode(), coreEx.getStatus()
				.getMessage(), coreEx.getStatus().getException()), testStatus);

		// Create from Operation Cancel Exception
		testStatus = StatusUtil.createInfoStatus(testBundle, opCancelEx);
		assertStatusEquals(IStatus.INFO, new Status(IStatus.INFO, testBundle.getSymbolicName(), 0, opCancelEx.getLocalizedMessage(), opCancelEx),
				testStatus);

		// Create from Throwable
		// ------------Throwable with Cause
		testStatus = StatusUtil.createInfoStatus(testBundle, throwableEx);
		assertTrue(testStatus instanceof MultiStatus);
		assertStatusEquals(IStatus.INFO, new MultiStatus(testBundle.getSymbolicName(), 0, throwableEx.getLocalizedMessage(), throwableEx), testStatus);

		testStatusChild = getFirstChildStatus(testStatus);
		assertStatusEquals(IStatus.INFO, new Status(IStatus.INFO, testBundle.getSymbolicName(), 0, throwableEx.getCause().getLocalizedMessage(),
				throwableEx.getCause()), testStatusChild);

		// ------------Throwable without Cause
		testStatus = StatusUtil.createInfoStatus(testBundle, throwableEx_noCause);
		assertStatusEquals(IStatus.INFO, new Status(IStatus.INFO, testBundle.getSymbolicName(), 0, throwableEx_noCause.getLocalizedMessage(),
				throwableEx_noCause), testStatus);

		// ----------------------------------------------OTHERS-------------------------------------------------------------
		// Create from Others objects
		String object = "Information";
		testStatus = StatusUtil.createInfoStatus(testBundle, object);
		assertStatusEquals(IStatus.INFO, new Status(IStatus.INFO, testBundle.getSymbolicName(), 0, object.toString(), null), testStatus);
		// Create from NULL object
		testStatus = StatusUtil.createInfoStatus(testBundle, null);
		assertStatusEquals(IStatus.INFO, new Status(IStatus.INFO, testBundle.getSymbolicName(), 0, PlatformMessages.message_none, null), testStatus);
		// Create from Unknown Bundle and unknown Error
		testStatus = StatusUtil.createInfoStatus((Plugin) null, null);
		assertStatusEquals(IStatus.INFO, new Status(IStatus.INFO, PlatformMessages.pluginId_unknown, 0, PlatformMessages.message_none, null),
				testStatus);
		// Create from Unknown Bundle and an Error
		testStatus = StatusUtil.createInfoStatus((Plugin) null, coreEx);
		assertStatusEquals(IStatus.INFO, new Status(IStatus.INFO, coreEx.getStatus().getPlugin(), coreEx.getStatus().getCode(), coreEx.getStatus()
				.getMessage(), coreEx.getStatus().getException()), testStatus);

	}

	public void testCreateWarningStatus() {
		assertNotNull(testBundle);
		IStatus testStatusChild;
		IStatus testStatus;

		// --------------------------------------------STATUS-------------------------------------------------------
		// Create from CANCEL Status
		Throwable nullObject = null;
		testStatus = StatusUtil.createWarningStatus(testBundle, cancel_throwableEx);

		assertTrue(testStatus instanceof MultiStatus);
		assertStatusEquals(
				IStatus.WARNING,
				new MultiStatus(testBundle.getSymbolicName(), 0, NLS.bind(PlatformMessages.problem_whenInvokingPlugin, testBundle.getSymbolicName(),
						cancel_throwableEx.getMessage()), nullObject), testStatus);

		testStatusChild = getFirstChildStatus(testStatus);
		assertStatusEquals(IStatus.WARNING, cancel_throwableEx, testStatusChild);

		// Create from ERROR status
		testStatus = StatusUtil.createWarningStatus(testBundle, error_coreEx);
		assertTrue(testStatus instanceof MultiStatus);
		assertStatusEquals(
				IStatus.WARNING,
				new MultiStatus(testBundle.getSymbolicName(), 0, NLS.bind(PlatformMessages.error_exceptionWhenInvokingPlugin, new Object[] {
						coreEx.getClass().getSimpleName(), testBundle.getSymbolicName(), coreEx.getLocalizedMessage() }), nullObject), testStatus);

		testStatusChild = getFirstChildStatus(testStatus);
		assertStatusEquals(IStatus.WARNING, error_coreEx, testStatusChild);

		// create from WARNING Status
		testStatus = StatusUtil.createWarningStatus(testBundle, warning_interruptedEx);
		assertTrue(testStatus instanceof MultiStatus);
		assertStatusEquals(
				IStatus.WARNING,
				new MultiStatus(testBundle.getSymbolicName(), 0, NLS.bind(PlatformMessages.problem_whenInvokingPlugin, testBundle.getSymbolicName(),
						warning_interruptedEx.getMessage()), nullObject), testStatus);

		testStatusChild = getFirstChildStatus(testStatus);
		assertStatusEquals(IStatus.WARNING, warning_interruptedEx, testStatusChild);

		// create from INFO status
		testStatus = StatusUtil.createWarningStatus(testBundle, info_opCancelEx);
		assertTrue(testStatus instanceof MultiStatus);
		assertStatusEquals(
				IStatus.WARNING,
				new MultiStatus(testBundle.getSymbolicName(), 0, NLS.bind(PlatformMessages.problem_whenInvokingPlugin, testBundle.getSymbolicName(),
						info_opCancelEx.getMessage()), nullObject), testStatus);

		testStatusChild = getFirstChildStatus(testStatus);
		assertStatusEquals(IStatus.WARNING, info_opCancelEx, testStatusChild);

		// create from OK Status
		testStatus = StatusUtil.createWarningStatus(testBundle, ok_interruptedEx);
		assertTrue(testStatus instanceof MultiStatus);
		assertStatusEquals(
				IStatus.WARNING,
				new MultiStatus(testBundle.getSymbolicName(), 0, NLS.bind(PlatformMessages.problem_whenInvokingPlugin, testBundle.getSymbolicName(),
						ok_interruptedEx.getMessage()), nullObject), testStatus);

		testStatusChild = getFirstChildStatus(testStatus);
		assertStatusEquals(IStatus.WARNING, ok_interruptedEx, testStatusChild);

		// -----------------------------------------EXCEPTION---------------------------------------------------------
		// Create from CoreException
		testStatus = StatusUtil.createWarningStatus(testBundle, coreEx);
		assertTrue(testStatus instanceof MultiStatus);
		assertStatusEquals(
				IStatus.WARNING,
				new MultiStatus(testBundle.getSymbolicName(), 0, NLS.bind(PlatformMessages.error_exceptionWhenInvokingPlugin, new Object[] {
						coreEx.getClass().getSimpleName(), testBundle.getSymbolicName(), coreEx.getLocalizedMessage() }), coreEx), testStatus);

		testStatusChild = getFirstChildStatus(testStatus);
		assertStatusEquals(IStatus.WARNING, new Status(IStatus.WARNING, coreEx.getStatus().getPlugin(), coreEx.getStatus().getCode(), coreEx
				.getStatus().getMessage(), coreEx.getStatus().getException()), testStatusChild);

		// Create from Operation Cancel Exception
		testStatus = StatusUtil.createWarningStatus(testBundle, opCancelEx);
		assertStatusEquals(IStatus.INFO, new Status(IStatus.INFO, testBundle.getSymbolicName(), 0, opCancelEx.getLocalizedMessage(), opCancelEx),
				testStatus);

		// Create from Throwable
		// ------------Throwable with Cause
		testStatus = StatusUtil.createWarningStatus(testBundle, throwableEx);
		assertTrue(testStatus instanceof MultiStatus);
		assertStatusEquals(
				IStatus.WARNING,
				new MultiStatus(testBundle.getSymbolicName(), 0, NLS.bind(PlatformMessages.error_exceptionWhenInvokingPlugin, new Object[] {
						throwableEx.getClass().getSimpleName(), testBundle.getSymbolicName(), throwableEx.getLocalizedMessage() }), throwableEx),
				testStatus);

		testStatusChild = getFirstChildStatus(testStatus);
		assertStatusEquals(IStatus.WARNING, new Status(IStatus.WARNING, testBundle.getSymbolicName(), 0,
				throwableEx.getCause().getLocalizedMessage(), throwableEx.getCause()), testStatusChild);

		// ------------Throwable without Cause
		testStatus = StatusUtil.createWarningStatus(testBundle, throwableEx_noCause);
		assertTrue(testStatus instanceof MultiStatus);
		assertStatusEquals(
				IStatus.WARNING,
				new MultiStatus(testBundle.getSymbolicName(), 0, NLS.bind(PlatformMessages.error_exceptionWhenInvokingPlugin, new Object[] {
						throwableEx_noCause.getClass().getSimpleName(), testBundle.getSymbolicName(), throwableEx_noCause.getLocalizedMessage() }),
						throwableEx_noCause), testStatus);

		testStatusChild = getFirstChildStatus(testStatus);
		assertStatusEquals(IStatus.WARNING, new Status(IStatus.WARNING, testBundle.getSymbolicName(), 0, throwableEx_noCause.getLocalizedMessage(),
				throwableEx_noCause), testStatusChild);

		// -------------------------------------------OTHERS--------------------------------------------------------
		// Create from Others objects
		String object = "Information";
		testStatus = StatusUtil.createWarningStatus(testBundle, object);
		assertTrue(testStatus instanceof MultiStatus);
		assertStatusEquals(
				IStatus.WARNING,
				new MultiStatus(testBundle.getSymbolicName(), 0, NLS.bind(PlatformMessages.problem_whenInvokingPlugin, testBundle.getSymbolicName(),
						object.toString()), null), testStatus);

		testStatusChild = getFirstChildStatus(testStatus);
		assertStatusEquals(IStatus.WARNING, new Status(IStatus.WARNING, testBundle.getSymbolicName(), 0, object.toString(), null), testStatusChild);

		// Create from NULL object
		testStatus = StatusUtil.createWarningStatus(testBundle, null);
		assertTrue(testStatus instanceof MultiStatus);
		assertStatusEquals(
				IStatus.WARNING,
				new MultiStatus(testBundle.getSymbolicName(), 0, NLS.bind(PlatformMessages.problem_whenInvokingPlugin, testBundle.getSymbolicName(),
						PlatformMessages.message_none), null), testStatus);

		testStatusChild = getFirstChildStatus(testStatus);
		assertStatusEquals(IStatus.WARNING, new Status(IStatus.WARNING, testBundle.getSymbolicName(), 0, PlatformMessages.message_none, null),
				testStatusChild);

		// Create from Unknown Bundle and unknown Error
		testStatus = StatusUtil.createWarningStatus((Plugin) null, null);
		assertTrue(testStatus instanceof MultiStatus);
		assertStatusEquals(
				IStatus.WARNING,
				new MultiStatus(PlatformMessages.pluginId_unknown, 0, NLS.bind(PlatformMessages.problem_whenInvokingUnknownPlugin,
						PlatformMessages.message_none), null), testStatus);

		testStatusChild = getFirstChildStatus(testStatus);
		assertStatusEquals(IStatus.WARNING, new Status(IStatus.WARNING, PlatformMessages.pluginId_unknown, 0, PlatformMessages.message_none, null),
				testStatusChild);

		// Create from Unknown Bundle and an Error
		testStatus = StatusUtil.createWarningStatus((Plugin) null, coreEx);
		assertTrue(testStatus instanceof MultiStatus);
		assertStatusEquals(
				IStatus.WARNING,
				new MultiStatus(PlatformMessages.pluginId_unknown, 0, NLS.bind(PlatformMessages.error_exceptionWhenInvokingUnknownPlugin,
						new Object[] { coreEx.getClass().getSimpleName(), coreEx.getLocalizedMessage() }), null), testStatus);

		testStatusChild = getFirstChildStatus(testStatus);
		assertStatusEquals(IStatus.WARNING, new Status(IStatus.WARNING, coreEx.getStatus().getPlugin(), coreEx.getStatus().getCode(), coreEx
				.getStatus().getMessage(), coreEx.getStatus().getException()), testStatusChild);
	}

	/**
	 * Test method for
	 * {@link StatusUtil#getMoreSevereStatus(org.eclipse.core.runtime.IStatus, org.eclipse.core.runtime.IStatus)}
	 */
	public void testGetMostSevereStatusFromTwoGivenStatus() {
		coreEx = new CoreException(cancel_throwableEx);
		assertSame(cancel_interruptedEx, StatusUtil.getMoreSevereStatus(cancel_throwableEx, cancel_interruptedEx));
		assertSame(cancel_throwableEx, StatusUtil.getMoreSevereStatus(cancel_throwableEx, error_coreEx));
		assertSame(cancel_throwableEx, StatusUtil.getMoreSevereStatus(cancel_throwableEx, warning_interruptedEx));
		assertSame(cancel_throwableEx, StatusUtil.getMoreSevereStatus(cancel_throwableEx, info_opCancelEx));
		assertSame(cancel_throwableEx, StatusUtil.getMoreSevereStatus(cancel_throwableEx, ok_interruptedEx));

		assertSame(error_coreEx, StatusUtil.getMoreSevereStatus(error_coreEx, ok_interruptedEx));
		assertSame(error_coreEx, StatusUtil.getMoreSevereStatus(error_coreEx, warning_interruptedEx));
		assertSame(error_coreEx, StatusUtil.getMoreSevereStatus(error_coreEx, info_opCancelEx));

		assertSame(warning_interruptedEx, StatusUtil.getMoreSevereStatus(warning_interruptedEx, info_opCancelEx));
		assertSame(warning_interruptedEx, StatusUtil.getMoreSevereStatus(warning_interruptedEx, ok_interruptedEx));

		assertSame(info_opCancelEx, StatusUtil.getMoreSevereStatus(info_opCancelEx, ok_interruptedEx));
	}

	/**
	 * Test method for {@link StatusUtil#getMostSevereStatus(org.eclipse.core.runtime.IStatus[])}
	 */
	public void testGetMostSevereStatusFromGivenList() {
		IStatus[] statusList1 = new IStatus[] { warning_interruptedEx, info_opCancelEx, warning_interruptedEx, ok_interruptedEx };
		IStatus[] statusList2 = new IStatus[] { error_coreEx, ok_interruptedEx, error_throwableEx, error_coreEx, info_opCancelEx };
		IStatus[] statusList3 = new IStatus[] { info_opCancelEx, cancel_interruptedEx, error_coreEx, cancel_throwableEx, warning_interruptedEx,
				ok_interruptedEx };
		IStatus[] statusList4 = new IStatus[] { info_opCancelEx, error_coreEx, cancel_interruptedEx, cancel_throwableEx, warning_interruptedEx,
				ok_interruptedEx };
		assertSame(warning_interruptedEx, StatusUtil.getMostSevereStatus(statusList1));
		assertSame(error_coreEx, StatusUtil.getMostSevereStatus(statusList2));
		assertSame(error_coreEx, StatusUtil.getMostSevereStatus(statusList3));
		assertSame(error_coreEx, StatusUtil.getMostSevereStatus(statusList4));

	}
}
