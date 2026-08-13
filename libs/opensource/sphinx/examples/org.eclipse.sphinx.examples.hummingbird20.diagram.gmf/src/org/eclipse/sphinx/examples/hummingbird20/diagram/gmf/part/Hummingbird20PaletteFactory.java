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
package org.eclipse.sphinx.examples.hummingbird20.diagram.gmf.part;

import java.util.Collections;
import java.util.List;

import org.eclipse.gef.Tool;
import org.eclipse.gef.palette.PaletteContainer;
import org.eclipse.gef.palette.PaletteDrawer;
import org.eclipse.gef.palette.PaletteRoot;
import org.eclipse.gef.palette.ToolEntry;
import org.eclipse.gmf.runtime.diagram.ui.tools.UnspecifiedTypeConnectionTool;
import org.eclipse.gmf.runtime.diagram.ui.tools.UnspecifiedTypeCreationTool;
import org.eclipse.gmf.runtime.emf.type.core.IElementType;
import org.eclipse.sphinx.examples.hummingbird20.diagram.gmf.providers.Hummingbird20ElementTypes;

/**
 * @generated
 */
public class Hummingbird20PaletteFactory {

	/**
	 * @generated
	 */
	public void fillPalette(PaletteRoot paletteRoot) {
		paletteRoot.add(createObjects1Group());
		paletteRoot.add(createConnections2Group());
	}

	/**
	 * Creates "Objects" palette tool group
	 * 
	 * @generated
	 */
	private PaletteContainer createObjects1Group() {
		PaletteDrawer paletteContainer = new PaletteDrawer(Messages.Objects1Group_title);
		paletteContainer.setId("createObjects1Group"); //$NON-NLS-1$
		paletteContainer.add(createComponent1CreationTool());
		return paletteContainer;
	}

	/**
	 * Creates "Connections" palette tool group
	 * 
	 * @generated
	 */
	private PaletteContainer createConnections2Group() {
		PaletteDrawer paletteContainer = new PaletteDrawer(Messages.Connections2Group_title);
		paletteContainer.setId("createConnections2Group"); //$NON-NLS-1$
		paletteContainer.add(createConnection1CreationTool());
		return paletteContainer;
	}

	/**
	 * @generated
	 */
	private ToolEntry createComponent1CreationTool() {
		NodeToolEntry entry = new NodeToolEntry(Messages.Component1CreationTool_title, Messages.Component1CreationTool_desc,
				Collections.singletonList(Hummingbird20ElementTypes.Component_2001));
		entry.setId("createComponent1CreationTool"); //$NON-NLS-1$
		entry.setSmallIcon(Hummingbird20ElementTypes.getImageDescriptor(Hummingbird20ElementTypes.Component_2001));
		entry.setLargeIcon(entry.getSmallIcon());
		return entry;
	}

	/**
	 * @generated
	 */
	private ToolEntry createConnection1CreationTool() {
		LinkToolEntry entry = new LinkToolEntry(Messages.Connection1CreationTool_title, Messages.Connection1CreationTool_desc,
				Collections.singletonList(Hummingbird20ElementTypes.Connection_4001));
		entry.setId("createConnection1CreationTool"); //$NON-NLS-1$
		entry.setSmallIcon(Hummingbird20ElementTypes.getImageDescriptor(Hummingbird20ElementTypes.Connection_4001));
		entry.setLargeIcon(entry.getSmallIcon());
		return entry;
	}

	/**
	 * @generated
	 */
	private static class NodeToolEntry extends ToolEntry {

		/**
		 * @generated
		 */
		private final List<IElementType> elementTypes;

		/**
		 * @generated
		 */
		private NodeToolEntry(String title, String description, List<IElementType> elementTypes) {
			super(title, description, null, null);
			this.elementTypes = elementTypes;
		}

		/**
		 * @generated
		 */
		@Override
		public Tool createTool() {
			Tool tool = new UnspecifiedTypeCreationTool(elementTypes);
			tool.setProperties(getToolProperties());
			return tool;
		}
	}

	/**
	 * @generated
	 */
	private static class LinkToolEntry extends ToolEntry {

		/**
		 * @generated
		 */
		private final List<IElementType> relationshipTypes;

		/**
		 * @generated
		 */
		private LinkToolEntry(String title, String description, List<IElementType> relationshipTypes) {
			super(title, description, null, null);
			this.relationshipTypes = relationshipTypes;
		}

		/**
		 * @generated
		 */
		@Override
		public Tool createTool() {
			Tool tool = new UnspecifiedTypeConnectionTool(relationshipTypes);
			tool.setProperties(getToolProperties());
			return tool;
		}
	}
}
