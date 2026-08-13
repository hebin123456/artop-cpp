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
package org.eclipse.sphinx.examples.hummingbird20.ide.ui.providers;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.edit.provider.IItemFontProvider;
import org.eclipse.emf.edit.provider.StyledString;

public interface IHummingbird20Styles {

	StyledString.Style TYPE_GROUP_STYLE = StyledString.Style.newBuilder()
			.setFont(URI.createURI("font:///+1/normal")).setForegroundColor(URI.createURI("color://rgb/0/150/0")).toStyle(); //$NON-NLS-1$ //$NON-NLS-2$

	StyledString.Style INSTANCE_GROUP_STYLE = StyledString.Style.newBuilder()
			.setFont(URI.createURI("font:///+1/normal")).setForegroundColor(URI.createURI("color://rgb/0/0/180")).toStyle(); //$NON-NLS-1$ //$NON-NLS-2$

	StyledString.Style SUB_GROUP_STYLE = StyledString.Style.newBuilder()
			.setFont(URI.createURI("font:////bold")).setForegroundColor(URI.createURI("color://rgb/111/111/111")).toStyle(); //$NON-NLS-1$ //$NON-NLS-2$

	StyledString.Style INSTANCE_STYLE = StyledString.Style.newBuilder().setFont(IItemFontProvider.BOLD_FONT)
			.setUnderlineStyle(StyledString.Style.UnderLineStyle.SINGLE).toStyle();
}
