package org.eclipse.sphinx.emf.validation.markers;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.List;

import org.eclipse.sphinx.emf.util.EObjectUtil;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class ValidationMarkerManagerUriComparisonTest {

	private static final String SELECTED_ELEMENT_URI = "platform:/resource/validation-1/default.arxml#/ARRoot/Adc"; //$NON-NLS-1$

	private String markerUri;
	private boolean showMarkerForDepthZero, showMarkerForDepthOne, showMarkerForDepthInfinite;
	private static ValidationMarkerManager markerManager;

	// EObjectUtil.DEPTH_ZERO, EObjectUtil.DEPTH_ONE, EObjectUtil.DEPTH_INFINITE

	@Parameterized.Parameters
	public static List<Object[]> markerUriTestData() {
		// | Marker URI | Show marker for DEPTH_ZERO | DEPTH_ONE | DEPTH_INFINITE |
		return Arrays.asList(new Object[][] { { "platform:/resource/validation-1/default.arxml#/ARRoot/Adc", true, true, true }, //$NON-NLS-1$
				{ "platform:/resource/validation-1/default.arxml#/ARRoot/Adc/AdcConfigSet", false, true, true }, //$NON-NLS-1$
				{ "platform:/resource/validation-1/default.arxml#/ARRoot/Adc/AdcConfigSet/Abc", false, false, true }, //$NON-NLS-1$
				{ "platform:/resource/validation-1/default.arxml#/ARRoot/Adc2", false, false, false }, //$NON-NLS-1$
				{ "platform:/resource/validation-1/default.arxml#/ARRoot/Adc2/AdcConfigSet", false, false, false }, //$NON-NLS-1$
				{ "platform:/resource/validation-1/default.arxml#/ARRoot/Adc2/AdcConfigSet/Abc", false, false, false } }); //$NON-NLS-1$
	}

	public ValidationMarkerManagerUriComparisonTest(String markerUri, boolean showMarkerForDepthZero, boolean showMarkerForDepthOne,
			boolean showMarkerForDepthInfinite) {
		this.markerUri = markerUri;
		this.showMarkerForDepthZero = showMarkerForDepthZero;
		this.showMarkerForDepthOne = showMarkerForDepthOne;
		this.showMarkerForDepthInfinite = showMarkerForDepthInfinite;
	}

	@BeforeClass
	public static void setUpOnce() {
		markerManager = ValidationMarkerManager.getInstance();
	}

	@Test
	public void checkMarkerCorrectlyFilteredForDepthZero() {
		int depth = EObjectUtil.DEPTH_ZERO;

		boolean showMarker = markerManager.showMarker(SELECTED_ELEMENT_URI, markerUri, "EObject", null, depth); //$NON-NLS-1$

		assertThat(showMarker, is(showMarkerForDepthZero));

		showMarker = markerManager.showMarker(SELECTED_ELEMENT_URI, markerUri, "EObject", "EObject", depth); //$NON-NLS-1$ //$NON-NLS-2$

		assertThat(showMarker, is(showMarkerForDepthZero));

		showMarker = markerManager.showMarker(SELECTED_ELEMENT_URI, markerUri, "EObject", "OtherType", depth); //$NON-NLS-1$ //$NON-NLS-2$

		assertThat(showMarker, is(false));
	}

	@Test
	public void checkMarkerCorrectlyFilteredForDepthOne() {
		int depth = EObjectUtil.DEPTH_ONE;

		boolean showMarker = markerManager.showMarker(SELECTED_ELEMENT_URI, markerUri, "EObject", null, depth); //$NON-NLS-1$

		assertThat(showMarker, is(showMarkerForDepthOne));
	}

	@Test
	public void checkMarkerCorrectlyFilteredForDepthInfinite() {
		int depth = EObjectUtil.DEPTH_INFINITE;

		boolean showMarker = markerManager.showMarker(SELECTED_ELEMENT_URI, markerUri, "EObject", null, depth); //$NON-NLS-1$

		assertThat(showMarker, is(showMarkerForDepthInfinite));
	}

}
