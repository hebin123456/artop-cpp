package org.eclipse.sphinx.tests.emf.search.ui.internal.mock;

import java.util.Collection;

import org.eclipse.sphinx.emf.metamodel.IMetaModelDescriptor;
import org.eclipse.sphinx.emf.search.ui.services.BasicModelSearchService;

public class BasicModelSearchServiceMock extends BasicModelSearchService {

	public BasicModelSearchServiceMock(Collection<IMetaModelDescriptor> mmDescriptors) {
		super(mmDescriptors);
		super.helper = new RetrieveNameAttributeHelperMock();
	}
}
