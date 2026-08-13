/**
 * <copyright>
 * 
 * Copyright (c) 2011 See4sys and others.
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
package org.eclipse.sphinx.xtendxpand;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.Assert;
import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;

public class CheckEvaluationRequest {

	/**
	 * Returns the <code>modelObject</code> object and its {@link EObject#eContents direct contents} and indirect
	 * contents.
	 * 
	 * @param modelObject
	 *            the selected model object.
	 * @return the <code>modelObject</code> object and its {@link EObject#eContents direct contents} and indirect
	 *         contents.
	 */
	protected static Collection<Object> getAllContents(Object modelObject) {
		Assert.isNotNull(modelObject);

		Collection<Object> modelObjects = new ArrayList<Object>();
		modelObjects.add(modelObject);
		if (modelObject instanceof EObject) {
			TreeIterator<EObject> eAllContents = ((EObject) modelObject).eAllContents();
			while (eAllContents.hasNext()) {
				EObject element = eAllContents.next();
				modelObjects.add(element);
			}
		}
		return modelObjects;
	}

	/**
	 * The check files (with chk extention) to be used for checking models.
	 */
	private Collection<IFile> checkFiles;

	/**
	 * The collection of elements concerning of model checking.
	 */
	private Collection<Object> modelObjects;

	/**
	 * Constructs a Check model evaluation request.
	 * 
	 * @param checkFile
	 *            a Check file.
	 * @param modelRootObject
	 *            the root model to be check.
	 */
	public CheckEvaluationRequest(IFile checkFile, Object modelRootObject) {
		this(Collections.singleton(checkFile), modelRootObject);
	}

	/**
	 * Constructs a Check model evaluation request.
	 * 
	 * @param checkFiles
	 *            a collection of Check files.
	 * @param modelRootObject
	 *            the root model to be check.
	 */
	public CheckEvaluationRequest(Collection<IFile> checkFiles, Object modelRootObject) {
		this(checkFiles, getAllContents(modelRootObject));
	}

	/**
	 * Constructs a Check model evaluation request.
	 * 
	 * @param checkFile
	 *            a Check file.
	 * @param modelObjects
	 *            all model objects to be check.
	 */
	public CheckEvaluationRequest(IFile checkFile, Collection<Object> modelObjects) {
		this(Collections.singleton(checkFile), modelObjects);
	}

	/**
	 * Constructs a Check model evaluation request.
	 * 
	 * @param checkFiles
	 *            a collection of Check files.
	 * @param modelObjects
	 *            all model objects to be check.
	 */
	public CheckEvaluationRequest(Collection<IFile> checkFiles, Collection<Object> modelObjects) {
		Assert.isNotNull(checkFiles);

		this.checkFiles = checkFiles;
		this.modelObjects = modelObjects;
	}

	/**
	 * Gets the Check files to be use.
	 */
	public Collection<IFile> getCheckFiles() {
		return checkFiles;
	}

	/**
	 * Gets all the model objects to be check.
	 */
	public Collection<Object> getModelObjects() {
		return modelObjects;
	}

	/**
	 * Gets the root model element. This is computed from the <code>modelObject</code> element.
	 * 
	 * @see {@link EcoreUtil#getRootContainer(EObject)} method.
	 */
	public Object getModelRootObject() {
		if (modelObjects != null) {
			Object object = modelObjects.iterator().next();
			if (object instanceof EObject) {
				return EcoreUtil.getRootContainer((EObject) object);
			}
		}
		return null;
	}
}
