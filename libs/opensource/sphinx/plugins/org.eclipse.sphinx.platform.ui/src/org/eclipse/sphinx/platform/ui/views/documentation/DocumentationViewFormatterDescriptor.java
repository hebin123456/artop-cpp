/**
 * <copyright>
 *
 * Copyright (c) 2015 itemis and others.
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
package org.eclipse.sphinx.platform.ui.views.documentation;

import org.eclipse.core.expressions.EvaluationContext;
import org.eclipse.core.expressions.EvaluationResult;
import org.eclipse.core.expressions.Expression;
import org.eclipse.core.expressions.ExpressionConverter;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.sphinx.platform.ui.internal.Activator;
import org.eclipse.sphinx.platform.util.ExtensionClassDescriptor;
import org.eclipse.sphinx.platform.util.PlatformLogUtil;

public class DocumentationViewFormatterDescriptor extends ExtensionClassDescriptor<IDocumentationViewFormatter> {

	private static final String NODE_INSTANCEOF = "instanceof"; //$NON-NLS-1$
	private static final String ATTR_PRIORITY = "priority"; //$NON-NLS-1$

	private int priority = 0;
	private IConfigurationElement instanceOfConfigElement;

	public DocumentationViewFormatterDescriptor(IConfigurationElement configurationElement) {
		super(configurationElement);

		// Gets priority
		String priorityAttributeValue = configurationElement.getAttribute(ATTR_PRIORITY);
		if (priorityAttributeValue != null && !priorityAttributeValue.isEmpty()) {
			try {
				priority = Integer.valueOf(priorityAttributeValue);
			} catch (NumberFormatException ex) {
				// Ignore Exception
			}
		}

		// Applicable for
		IConfigurationElement[] instanceOfConfigElements = configurationElement.getChildren(NODE_INSTANCEOF);
		if (instanceOfConfigElements.length > 0) {
			instanceOfConfigElement = instanceOfConfigElements[0];
		}
	}

	public int getPriority() {
		return priority;
	}

	public boolean isApplicableFor(final Object selectedObject) {
		if (instanceOfConfigElement != null) {
			try {
				Expression expression = ExpressionConverter.getDefault().perform(instanceOfConfigElement);
				EvaluationResult evaluate = expression.evaluate(new EvaluationContext(null, selectedObject));
				return evaluate.equals(EvaluationResult.TRUE);
			} catch (Exception ex) {
				PlatformLogUtil.logAsError(Activator.getDefault(), ex);
				return false;
			}
		}
		return false;
	}
}
