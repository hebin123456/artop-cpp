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
 *     itemis - [436112] Rework XML Persistence Mapping & XSD generation menu items to make them less prominent in the Eclipse UI
 *
 * </copyright>
 */
package org.eclipse.sphinx.tests.emf.serialization.generators;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.XMLResource;
import org.eclipse.emf.ecore.xmi.impl.EcoreResourceFactoryImpl;
import org.eclipse.sphinx.emf.serialization.XMLPersistenceMappingResourceFactoryImpl;
import org.eclipse.sphinx.emf.serialization.XMLPersistenceMappingResourceSetImpl;
import org.eclipse.sphinx.emf.serialization.generators.xsd.Ecore2XSDGenerator;
import org.eclipse.sphinx.tests.emf.serialization.generators.internal.Activator;
import org.eclipse.sphinx.tests.emf.serialization.generators.model.ModelBuilder;
import org.eclipse.sphinx.testutils.EcoreEqualityAssert;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;

@SuppressWarnings("nls")
public abstract class AbstractTestCase extends org.eclipse.sphinx.testutils.AbstractTestCase {
	public static final String ADDITIONAL_SCHEMA_FOLDER_NAME = "/schema/";
	public static final String MODEL_FILE_EXTESNION = "nodes";
	public static final boolean SKIP_SCHEMA_VALIDATION = true;

	protected void validate(List<EPackage> metamodel, EObject model, String modelName, boolean skipSchemaValidation) throws Exception {

		// write metamodel
		ResourceSet metamodelResourceSet = new ResourceSetImpl();
		Resource metamodelResource;
		for (int i = 0; i < metamodel.size(); i++) {
			java.net.URI workingFileURI = getTestFileAccessor().getWorkingFileURI(getMetaModelFileName(modelName + (i + 1)));
			URI emfURI = getTestFileAccessor().convertToEMFURI(workingFileURI);
			metamodelResource = new EcoreResourceFactoryImpl().createResource(emfURI);
			metamodelResource.getContents().add(metamodel.get(i));
			metamodelResourceSet.getResources().add(metamodelResource);
		}

		for (int i = 0; i < metamodel.size(); i++) {
			metamodelResourceSet.getResources().get(i).save(null);
		}

		// save instance
		saveWorkingFile(getModelFileName(modelName), model, new XMLPersistenceMappingResourceFactoryImpl(), null);

		// load instance
		// register dynamic packages and file name extension with local resource set
		ResourceSet resourceSet = new XMLPersistenceMappingResourceSetImpl();
		for (int i = 0; i < metamodel.size(); i++) {
			EPackage ePackage = metamodel.get(i);
			resourceSet.getPackageRegistry().put(ePackage.getNsURI(), ePackage);
		}
		resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put(MODEL_FILE_EXTESNION, new XMLPersistenceMappingResourceFactoryImpl());
		// load the resource
		EObject loadedModel = loadWorkingFile(getModelFileName(modelName), resourceSet, null);

		// compare
		EcoreEqualityAssert.assertEquals(model, loadedModel);

		// check load problems

		// generate schema
		for (int i = 0; i < metamodel.size(); i++) {
			java.net.URI xsdFileURI = getTestFileAccessor().getWorkingFileURI(getSchemaFileName(metamodel.get(i).getName()));
			URI emfXsdFileURI = getTestFileAccessor().convertToEMFURI(xsdFileURI);
			final Ecore2XSDGenerator ecore2XSDGenerator = new Ecore2XSDGenerator(emfXsdFileURI, metamodel.get(i));
			ecore2XSDGenerator.run(new NullProgressMonitor());
		}

		if (!skipSchemaValidation) {
			// validate instance
			validateAgainstSchema(getTestFileAccessor().getWorkingFileURI(getModelFileName(modelName)),
					getTestFileAccessor().getWorkingFileURI(getSchemaFileName(metamodel.get(0).getName())));
		} else {
			System.err.println("[WARNING] Skipping schema validation since feature is not yet implemented in XSD generator: " + modelName);
		}
	}

	protected EObject loadWorkingFile(String workingFileName, ResourceSet resourceSet, Map<?, ?> options) throws Exception {
		return loadFile(getTestFileAccessor().getWorkingFileURI(workingFileName), resourceSet, options);
	}

	private EObject loadFile(java.net.URI fileURI, ResourceSet resourceSet, Map<?, ?> options) throws Exception {
		URI emfURI = getTestFileAccessor().convertToEMFURI(fileURI);
		XMLResource resource = (XMLResource) resourceSet.getResource(emfURI, true);
		resource.load(options);

		assertHasNoLoadProblems(resource);

		return resource.getContents().get(0);
	}

	protected String getModelFileName(String modelName) {
		return getTestCaseFolderName() + modelName + "." + MODEL_FILE_EXTESNION;
	}

	protected String getMetaModelFileName(String modelName) {
		return getTestCaseFolderName() + modelName + ".ecore";
	}

	protected String getSchemaFileName(String modelName) {
		return getTestCaseFolderName() + modelName + ".xsd";
	}

	protected String getTestCaseFolderName() {
		return this.getClass().getName() + "/";
	}

	public class Input implements LSInput {

		private String publicId;

		private String systemId;

		@Override
		public String getPublicId() {
			return publicId;
		}

		@Override
		public void setPublicId(String publicId) {
			this.publicId = publicId;
		}

		@Override
		public String getBaseURI() {
			return null;
		}

		@Override
		public InputStream getByteStream() {
			return null;
		}

		@Override
		public boolean getCertifiedText() {
			return false;
		}

		@Override
		public Reader getCharacterStream() {
			return null;
		}

		@Override
		public String getEncoding() {
			return null;
		}

		@Override
		public String getStringData() {
			synchronized (inputStream) {
				try {
					byte[] input = new byte[inputStream.available()];
					inputStream.read(input);
					String contents = new String(input);
					return contents;
				} catch (IOException e) {
					e.printStackTrace();
					System.out.println("Exception " + e);
					return null;
				}
			}
		}

		@Override
		public void setBaseURI(String baseURI) {
		}

		@Override
		public void setByteStream(InputStream byteStream) {
		}

		@Override
		public void setCertifiedText(boolean certifiedText) {
		}

		@Override
		public void setCharacterStream(Reader characterStream) {
		}

		@Override
		public void setEncoding(String encoding) {
		}

		@Override
		public void setStringData(String stringData) {
		}

		@Override
		public String getSystemId() {
			return systemId;
		}

		@Override
		public void setSystemId(String systemId) {
			this.systemId = systemId;
		}

		public BufferedInputStream getInputStream() {
			return inputStream;
		}

		public void setInputStream(BufferedInputStream inputStream) {
			this.inputStream = inputStream;
		}

		private BufferedInputStream inputStream;

		public Input(String publicId, String sysId, InputStream input) {
			this.publicId = publicId;
			systemId = sysId;
			inputStream = new BufferedInputStream(input);
		}
	}

	protected void validateAgainstSchema(java.net.URI fileURI, final java.net.URI schemaFileURI) throws Exception {

		StreamSource[] schemaDocuments = new StreamSource[] { new StreamSource(schemaFileURI.toString()) };
		Source instanceDocument = new StreamSource(fileURI.toString());

		// the resolver is required in order find imported schema files
		LSResourceResolver resolver = new LSResourceResolver() {

			@Override
			public LSInput resolveResource(String type, String namespaceURI, String publicId, String systemId, String baseURI) {
				LSInput input = null;
				String schemaFileName;
				if (null != systemId) {
					int slashIndex = systemId.lastIndexOf("/");
					if (-1 == slashIndex) {
						schemaFileName = systemId;
					} else {
						schemaFileName = systemId.substring(slashIndex);
					}

					try {
						String path = getTestFileAccessor().getWorkingFileURI(getTestCaseFolderName() + schemaFileName).getPath();
						InputStream inputStream = new FileInputStream(path);
						input = new Input(publicId, systemId, inputStream);
					} catch (FileNotFoundException e) {
						try {
							String path2 = getTestFileAccessor().getInputFileURI(ADDITIONAL_SCHEMA_FOLDER_NAME + schemaFileName, true).getPath();
							InputStream inputStream2 = new FileInputStream(path2);
							input = new Input(publicId, systemId, inputStream2);
						} catch (FileNotFoundException ex) {
							throw new RuntimeException(ex);
						} catch (URISyntaxException ex) {
							throw new RuntimeException(ex);
						} catch (IOException ex) {
							throw new RuntimeException(ex);
						}
					}
				}
				return input;
			}
		};

		SchemaFactory sf = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");
		sf.setResourceResolver(resolver);
		Schema s = sf.newSchema(schemaDocuments);
		Validator v = s.newValidator();
		v.validate(instanceDocument);
	}

	protected String getName(int persistenceMappingStrategy, boolean isMany) {
		StringBuffer buffer = new StringBuffer();
		buffer.append(this.getClass().getSimpleName());
		buffer.append(toBinaryString(persistenceMappingStrategy));
		buffer.append(isMany ? "_Many" : "_Single");
		return buffer.toString();
	}

	protected String toBinaryString(int serializationStructure) {
		StringBuffer buffer = new StringBuffer();

		buffer.append(getBinaryString(serializationStructure, 8));
		buffer.append(getBinaryString(serializationStructure, 4));
		buffer.append(getBinaryString(serializationStructure, 2));
		buffer.append(getBinaryString(serializationStructure, 1));

		return buffer.toString();
	}

	protected String getBinaryString(int serializationStructure, int mask) {
		if (mask == (serializationStructure & mask)) {
			return "1";
		} else {
			return "0";
		}
	}

	protected void runTest(int persistenceMappingStrategy, boolean isMany) throws Exception {
		runTest(persistenceMappingStrategy, isMany, false);
	}

	protected void runTest(int persistenceMappingStrategy, boolean isMany, boolean skipSchemaValidation) throws Exception {
		String name = getName(persistenceMappingStrategy, isMany);
		ModelBuilder modelBuilder = getModelBuilder(getName(persistenceMappingStrategy, isMany), persistenceMappingStrategy);
		if (isMany) {
			List<EPackage> manyMetamodel = modelBuilder.getManyMetaModel();
			EObject manyModel = modelBuilder.getManyModel(3);
			validate(manyMetamodel, manyModel, name, skipSchemaValidation);
		} else {
			List<EPackage> singleMetamodel = modelBuilder.getSingleMetaModel();
			EObject singleModel = modelBuilder.getSingleModel(1);
			validate(singleMetamodel, singleModel, name, skipSchemaValidation);
		}
	}

	abstract protected ModelBuilder getModelBuilder(String name, int persistenceMappingStrategy);

	@Override
	protected Plugin getTestPlugin() {
		return Activator.getPlugin();
	}
}
