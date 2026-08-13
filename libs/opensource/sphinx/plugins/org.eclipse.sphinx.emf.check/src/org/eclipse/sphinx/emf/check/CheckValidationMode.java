/**
 * <copyright>
 *
 * Copyright (c) 2014 itemis and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 *
 * Contributors:
 *     itemis - Initial API and implementation
 *
 * </copyright>
 */
package org.eclipse.sphinx.emf.check;

import java.util.Map;

public enum CheckValidationMode {

	FAST_ONLY {
		@Override
		public boolean shouldCheck(CheckValidationType type) {
			return type == CheckValidationType.FAST;
		}

		@Override
		public String toString() {
			return CheckValidationType.FAST.toString();
		}
	},

	NORMAL_ONLY {
		@Override
		public boolean shouldCheck(CheckValidationType type) {
			return type == CheckValidationType.NORMAL;
		}

		@Override
		public String toString() {
			return CheckValidationType.NORMAL.toString();
		}
	},

	EXPENSIVE_ONLY {
		@Override
		public boolean shouldCheck(CheckValidationType type) {
			return type == CheckValidationType.EXPENSIVE;
		}

		@Override
		public String toString() {
			return CheckValidationType.EXPENSIVE.toString();
		}
	},

	NORMAL_AND_FAST {
		@Override
		public boolean shouldCheck(CheckValidationType type) {
			return type == CheckValidationType.NORMAL || type == CheckValidationType.FAST;
		}

		@Override
		public String toString() {
			return CheckValidationType.NORMAL + "|" + CheckValidationType.FAST; //$NON-NLS-1$
		}
	},

	ALL {
		@Override
		public boolean shouldCheck(CheckValidationType type) {
			return true;
		}

		@Override
		public String toString() {
			return "ALL"; //$NON-NLS-1$
		}
	};

	public static CheckValidationMode getFromContext(Map<Object, Object> context) {
		CheckValidationMode mode = CheckValidationMode.ALL;
		if (context != null) {
			Object object2 = context.get(CheckValidationMode.KEY);
			if (object2 instanceof CheckValidationMode) {
				mode = (CheckValidationMode) object2;
			} else if (object2 != null) {
				throw new IllegalArgumentException(
						"Context object for key " + CheckValidationMode.KEY + " should be of Type " + CheckValidationMode.class.getName() //$NON-NLS-1$//$NON-NLS-2$
								+ " but was " + object2.getClass().getName()); //$NON-NLS-1$
			}
		}
		return mode;
	}

	public final static String KEY = "check.mode"; //$NON-NLS-1$

	public abstract boolean shouldCheck(CheckValidationType type);
}