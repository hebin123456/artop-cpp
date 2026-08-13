/**
 * <copyright>
 * 
 * Copyright (c) 2013 itemis and others.
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
package org.eclipse.sphinx.examples.hummingbird20.diagram.gmf.providers;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.gmf.runtime.common.core.service.AbstractProvider;
import org.eclipse.gmf.runtime.common.core.service.IOperation;
import org.eclipse.gmf.runtime.common.ui.services.parser.GetParserOperation;
import org.eclipse.gmf.runtime.common.ui.services.parser.IParser;
import org.eclipse.gmf.runtime.common.ui.services.parser.IParserProvider;
import org.eclipse.gmf.runtime.common.ui.services.parser.ParserService;
import org.eclipse.gmf.runtime.emf.type.core.IElementType;
import org.eclipse.gmf.runtime.emf.ui.services.parser.ParserHintAdapter;
import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.sphinx.examples.hummingbird20.common.Common20Package;
import org.eclipse.sphinx.examples.hummingbird20.diagram.gmf.edit.parts.ComponentNameEditPart;
import org.eclipse.sphinx.examples.hummingbird20.diagram.gmf.edit.parts.ConnectionLabelEditPart;
import org.eclipse.sphinx.examples.hummingbird20.diagram.gmf.parsers.ConnectionLabelExpressionLabelParser;
import org.eclipse.sphinx.examples.hummingbird20.diagram.gmf.parsers.MessageFormatParser;
import org.eclipse.sphinx.examples.hummingbird20.diagram.gmf.part.Hummingbird20VisualIDRegistry;

/**
 * @generated
 */
public class Hummingbird20ParserProvider extends AbstractProvider implements IParserProvider {

	/**
	 * @generated
	 */
	private IParser componentName_5001Parser;

	/**
	 * @generated
	 */
	private IParser getComponentName_5001Parser() {
		if (componentName_5001Parser == null) {
			EAttribute[] features = new EAttribute[] { Common20Package.eINSTANCE.getIdentifiable_Name() };
			MessageFormatParser parser = new MessageFormatParser(features);
			componentName_5001Parser = parser;
		}
		return componentName_5001Parser;
	}

	/**
	 * @generated
	 */
	private ConnectionLabelExpressionLabelParser connectionLabel_6001Parser;

	/**
	 * @generated
	 */
	private IParser getConnectionLabel_6001Parser() {
		if (connectionLabel_6001Parser == null) {
			connectionLabel_6001Parser = new ConnectionLabelExpressionLabelParser();
		}
		return connectionLabel_6001Parser;
	}

	/**
	 * @generated
	 */
	protected IParser getParser(int visualID) {
		switch (visualID) {
		case ComponentNameEditPart.VISUAL_ID:
			return getComponentName_5001Parser();
		case ConnectionLabelEditPart.VISUAL_ID:
			return getConnectionLabel_6001Parser();
		}
		return null;
	}

	/**
	 * Utility method that consults ParserService
	 * 
	 * @generated
	 */
	public static IParser getParser(IElementType type, EObject object, String parserHint) {
		return ParserService.getInstance().getParser(new HintAdapter(type, object, parserHint));
	}

	/**
	 * @generated
	 */
	@Override
	public IParser getParser(IAdaptable hint) {
		String vid = (String) hint.getAdapter(String.class);
		if (vid != null) {
			return getParser(Hummingbird20VisualIDRegistry.getVisualID(vid));
		}
		View view = (View) hint.getAdapter(View.class);
		if (view != null) {
			return getParser(Hummingbird20VisualIDRegistry.getVisualID(view));
		}
		return null;
	}

	/**
	 * @generated
	 */
	@Override
	public boolean provides(IOperation operation) {
		if (operation instanceof GetParserOperation) {
			IAdaptable hint = ((GetParserOperation) operation).getHint();
			if (Hummingbird20ElementTypes.getElement(hint) == null) {
				return false;
			}
			return getParser(hint) != null;
		}
		return false;
	}

	/**
	 * @generated
	 */
	private static class HintAdapter extends ParserHintAdapter {

		/**
		 * @generated
		 */
		private final IElementType elementType;

		/**
		 * @generated
		 */
		public HintAdapter(IElementType type, EObject object, String parserHint) {
			super(object, parserHint);
			assert type != null;
			elementType = type;
		}

		/**
		 * @generated
		 */
		@Override
		public Object getAdapter(Class adapter) {
			if (IElementType.class.equals(adapter)) {
				return elementType;
			}
			return super.getAdapter(adapter);
		}
	}

}
