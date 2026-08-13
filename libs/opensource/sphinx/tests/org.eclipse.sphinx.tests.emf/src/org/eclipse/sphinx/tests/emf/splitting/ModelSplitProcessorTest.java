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
package org.eclipse.sphinx.tests.emf.splitting;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.Plugin;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.sphinx.emf.resource.ModelResourceDescriptor;
import org.eclipse.sphinx.emf.splitting.ModelSplitProcessor;
import org.eclipse.sphinx.emf.util.EObjectUtil;
import org.eclipse.sphinx.examples.hummingbird20.Hummingbird20MMDescriptor;
import org.eclipse.sphinx.examples.hummingbird20.common.Description;
import org.eclipse.sphinx.examples.hummingbird20.common.LanguageCultureName;
import org.eclipse.sphinx.examples.hummingbird20.common.Translation;
import org.eclipse.sphinx.examples.hummingbird20.splitting.Hummingbird20TypeModelSplitPolicy;
import org.eclipse.sphinx.examples.hummingbird20.typemodel.ComponentType;
import org.eclipse.sphinx.examples.hummingbird20.typemodel.Interface;
import org.eclipse.sphinx.examples.hummingbird20.typemodel.Parameter;
import org.eclipse.sphinx.examples.hummingbird20.typemodel.Platform;
import org.eclipse.sphinx.examples.hummingbird20.typemodel.Port;
import org.eclipse.sphinx.examples.hummingbird20.util.Hummingbird20ResourceFactoryImpl;
import org.eclipse.sphinx.tests.emf.internal.Activator;
import org.eclipse.sphinx.testutils.AbstractTestCase;

@SuppressWarnings("nls")
public class ModelSplitProcessorTest extends AbstractTestCase {

	@Override
	protected Plugin getTestPlugin() {
		return Activator.getPlugin();
	}

	public void testHummingbirdTypeModelSplit() throws Exception {
		EObject platform = loadInputFile("hbFile20.typemodel", new Hummingbird20ResourceFactoryImpl(), null);
		assertTrue(platform instanceof Platform);

		// Split hbFile20.typemodel into Interfaces.typemodel and ComponentTypes.typemodel
		ModelSplitProcessor processor = new ModelSplitProcessor(new Hummingbird20TypeModelSplitPolicy());
		processor.getEObjectsToSplit().add(platform);
		processor.run(null);

		// Retrieve and verify split resource contents
		List<ModelResourceDescriptor> splitResourceDescriptors = new ArrayList<ModelResourceDescriptor>(processor.getSplitResourceDescriptors());
		assertNotNull(splitResourceDescriptors);
		assertEquals(3, splitResourceDescriptors.size());

		verifyInterfacesResource(splitResourceDescriptors.get(0));
		verifyComponentTypesResource(splitResourceDescriptors.get(1));
		verifyMandatoryParametersResource(splitResourceDescriptors.get(2));
	}

	private void verifyInterfacesResource(ModelResourceDescriptor splitResourceDescriptor) {
		assertSame(Hummingbird20MMDescriptor.XMI_CONTENT_TYPE_ID, splitResourceDescriptor.getContentTypeId());
		assertEquals(URI.createPlatformResourceURI(Activator.getPlugin().getSymbolicName() + "/resources/input/Interfaces.typemodel", true),
				splitResourceDescriptor.getURI());

		List<EObject> splitResourceContents = splitResourceDescriptor.getContents();
		assertEquals(1, splitResourceContents.size());
		EObject platform = splitResourceContents.get(0);
		assertTrue(platform instanceof Platform);

		// Make sure that ancestor object features of interfaces as required by Hummingbird 2.0 split policy DO have
		// been replicated in depth
		Description description = ((Platform) platform).getDescription();
		assertNotNull(description);
		assertEquals(LanguageCultureName.EN_US, description.getLanguage());
		assertEquals("Description1", EObjectUtil.getMixedText(description.getMixed()));
		EList<Translation> translations = description.getTranslations();
		assertEquals(2, translations.size());
		Translation translation1 = translations.get(0);
		assertNotNull(translation1);
		assertEquals(LanguageCultureName.DE_DE, translation1.getLanguage());
		assertEquals(URI.createURI("file:/deutsch.txt"), translation1.getResourceURI());
		Translation translation2 = translations.get(1);
		assertNotNull(translation2);
		assertEquals(LanguageCultureName.FR_FR, translation2.getLanguage());
		assertEquals(URI.createURI("file:/français.txt"), translation2.getResourceURI());

		// Make sure that all interfaces are present and complete
		List<Interface> interfaces = ((Platform) platform).getInterfaces();
		assertEquals(2, interfaces.size());
		Interface interface1 = interfaces.get(0);
		assertNotNull(interface1);
		assertEquals("Interface1", interface1.getName());
		Interface interface2 = interfaces.get(1);
		assertNotNull(interface2);
		assertEquals("Interface2", interface2.getName());

		// Make sure that no component types are present
		List<ComponentType> componentTypes = ((Platform) platform).getComponentTypes();
		assertEquals(0, componentTypes.size());
	}

	private void verifyComponentTypesResource(ModelResourceDescriptor splitResourceDescriptor) {
		assertSame(Hummingbird20MMDescriptor.XMI_CONTENT_TYPE_ID, splitResourceDescriptor.getContentTypeId());
		assertEquals(URI.createPlatformResourceURI(Activator.getPlugin().getSymbolicName() + "/resources/input/ComponentTypes.typemodel", true),
				splitResourceDescriptor.getURI());

		List<EObject> splitResourceContents = splitResourceDescriptor.getContents();
		assertEquals(1, splitResourceContents.size());
		EObject platform = splitResourceContents.get(0);
		assertTrue(platform instanceof Platform);

		// Make sure that ancestor object features of component types have NOT been replicated
		Description description = ((Platform) platform).getDescription();
		assertNull(description);

		// Make sure that no interfaces are present
		List<Interface> interfaces = ((Platform) platform).getInterfaces();
		assertEquals(0, interfaces.size());

		// Make sure that all component types are present and complete
		List<ComponentType> componentTypes = ((Platform) platform).getComponentTypes();
		assertEquals(2, componentTypes.size());
		ComponentType componentType1 = componentTypes.get(0);
		assertNotNull(componentType1);
		assertEquals("ComponentType1", componentType1.getName());
		EList<Port> componentType1Ports = componentType1.getPorts();
		assertEquals(2, componentType1Ports.size());
		Port port1a = componentType1Ports.get(0);
		assertNotNull(port1a);
		assertEquals("Port1a", port1a.getName());
		Port port1b = componentType1Ports.get(1);
		assertNotNull(port1b);
		assertEquals("Port1b", port1b.getName());
		ComponentType componentType2 = componentTypes.get(1);
		assertNotNull(componentType2);
		assertEquals("ComponentType2", componentType2.getName());
		EList<Port> componentType2Ports = componentType2.getPorts();
		assertEquals(2, componentType2Ports.size());
		Port port2a = componentType2Ports.get(0);
		assertNotNull(port2a);
		assertEquals("Port2a", port2a.getName());
		Port port2b = componentType2Ports.get(1);
		assertNotNull(port2b);
		assertEquals("Port2b", port2b.getName());

		// Make sure that only optional parameters are present
		EList<Parameter> componentType1Parameters = componentType1.getParameters();
		assertEquals(1, componentType1Parameters.size());
		Parameter parameter1b = componentType1Parameters.get(0);
		assertNotNull(parameter1b);
		assertEquals("Parameter1b", parameter1b.getName());
		assertTrue(parameter1b.isOptional());
		EList<Parameter> componentType2Parameters = componentType2.getParameters();
		assertEquals(1, componentType2Parameters.size());
		Parameter parameter2b = componentType2Parameters.get(0);
		assertNotNull(parameter2b);
		assertEquals("Parameter2b", parameter2b.getName());
		assertTrue(parameter2b.isOptional());
	}

	private void verifyMandatoryParametersResource(ModelResourceDescriptor splitResourceDescriptor) {
		assertSame(Hummingbird20MMDescriptor.XMI_CONTENT_TYPE_ID, splitResourceDescriptor.getContentTypeId());
		assertEquals(URI.createPlatformResourceURI(Activator.getPlugin().getSymbolicName() + "/resources/input/MandatoryParameters.typemodel", true),
				splitResourceDescriptor.getURI());

		List<EObject> splitResourceContents = splitResourceDescriptor.getContents();
		assertEquals(1, splitResourceContents.size());
		EObject platform = splitResourceContents.get(0);
		assertTrue(platform instanceof Platform);

		// Make sure that ancestor object features of mandatory parameters as required by Hummingbird 2.0 split policy
		// DO have been replicated in depth
		Description description = ((Platform) platform).getDescription();
		assertNotNull(description);
		assertEquals(LanguageCultureName.EN_US, description.getLanguage());
		assertEquals("Description1", EObjectUtil.getMixedText(description.getMixed()));
		EList<Translation> translations = description.getTranslations();
		assertEquals(2, translations.size());
		Translation translation1 = translations.get(0);
		assertNotNull(translation1);
		assertEquals(LanguageCultureName.DE_DE, translation1.getLanguage());
		assertEquals(URI.createURI("file:/deutsch.txt"), translation1.getResourceURI());
		Translation translation2 = translations.get(1);
		assertNotNull(translation2);
		assertEquals(LanguageCultureName.FR_FR, translation2.getLanguage());
		assertEquals(URI.createURI("file:/français.txt"), translation2.getResourceURI());

		// Make sure that no interfaces are present
		List<Interface> interfaces = ((Platform) platform).getInterfaces();
		assertEquals(0, interfaces.size());

		// Make sure that all mandatory parameters are present and complete
		List<ComponentType> componentTypes = ((Platform) platform).getComponentTypes();
		assertEquals(2, componentTypes.size());
		ComponentType componentType1 = componentTypes.get(0);
		assertNotNull(componentType1);
		assertEquals("ComponentType1", componentType1.getName());
		EList<Port> componentType1Ports = componentType1.getPorts();
		assertEquals(0, componentType1Ports.size());
		ComponentType componentType2 = componentTypes.get(1);
		assertNotNull(componentType2);
		assertEquals("ComponentType2", componentType2.getName());
		EList<Port> componentType2Ports = componentType2.getPorts();
		assertEquals(0, componentType2Ports.size());
		EList<Parameter> componentType1Parameters = componentType1.getParameters();
		assertEquals(1, componentType1Parameters.size());
		Parameter parameter1a = componentType1Parameters.get(0);
		assertNotNull(parameter1a);
		assertEquals("Parameter1a", parameter1a.getName());
		assertFalse(parameter1a.isOptional());
		EList<Parameter> componentType2Parameters = componentType2.getParameters();
		assertEquals(1, componentType2Parameters.size());
		Parameter parameter2a = componentType2Parameters.get(0);
		assertNotNull(parameter2a);
		assertEquals("Parameter2a", parameter2a.getName());
		assertFalse(parameter2a.isOptional());
	}
}
