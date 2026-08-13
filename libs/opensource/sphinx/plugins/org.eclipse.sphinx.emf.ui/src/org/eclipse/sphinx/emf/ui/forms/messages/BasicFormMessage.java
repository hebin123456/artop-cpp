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
package org.eclipse.sphinx.emf.ui.forms.messages;

import org.eclipse.jface.dialogs.IMessageProvider;

public class BasicFormMessage implements IFormMessage {

	protected String messageText = ""; //$NON-NLS-1$
	protected Object data;
	protected int messageType = IMessageProvider.NONE;

	@Override
	public String getMessageKey() {
		return messageText == null || messageText.length() == 0 ? "UnknownMessage" : messageText.replaceAll(" ", ""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	@Override
	public String getMessageText() {
		return messageText;
	}

	public void setMessageText(String text) {
		messageText = text;
	}

	@Override
	public Object getData() {
		return data;
	}

	public void setData(Object data) {
		this.data = data;
	}

	@Override
	public int getMessageType() {
		return messageType;
	}

	public void setMessageType(int type) {
		messageType = type;
	}
}
