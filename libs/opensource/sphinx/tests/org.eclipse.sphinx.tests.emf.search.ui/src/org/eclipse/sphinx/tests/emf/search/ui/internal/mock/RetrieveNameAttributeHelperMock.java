package org.eclipse.sphinx.tests.emf.search.ui.internal.mock;

import static org.easymock.EasyMock.createNiceMock;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.sphinx.emf.ui.util.RetrieveNameAttributeHelper;

public class RetrieveNameAttributeHelperMock extends RetrieveNameAttributeHelper {

	@Override
	public EAttribute getNameAttribute(EObject object) {
		EAttribute eAttribMock = createNiceMock(EAttribute.class);
		return eAttribMock;
	}
}