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
package org.eclipse.sphinx.platform.ui.internal.util;

import org.eclipse.jface.resource.CompositeImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;

/**
 * This class is used for overlaying image icons
 */
public class OverlayImageIcon extends CompositeImageDescriptor {

	/**
	 * Base image of the object
	 */
	private Image fBaseImage;

	/**
	 * Size of the base image
	 */
	private Point fSizeOfImage;

	/**
	 * 
	 */
	private Image fDecoImage;

	// private static final int TOP_LEFT = 0;
	// private static final int TOP_RIGHT = 1;
	// private static final int BOTTOM_LEFT = 2;
	// private static final int BOTTOM_RIGHT = 3;

	/**
	 * Constructor for overlayImageIcon.
	 */
	public OverlayImageIcon(Image baseImage, Image decoImage) {
		// Base image of the object
		fBaseImage = baseImage;
		// Deco Image Object
		fDecoImage = decoImage;
		fSizeOfImage = new Point(baseImage.getBounds().width, baseImage.getBounds().height);
	}

	/**
	 * @see org.eclipse.jface.resource.CompositeImageDescriptor#drawCompositeImage(int, int) DrawCompositeImage is
	 *      called to draw the composite image.
	 */
	@Override
	protected void drawCompositeImage(int arg0, int arg1) {
		// Draw the base image
		drawImage(fBaseImage.getImageData(), 0, 0);
		// int[] locations = organizeImages();
		// for (int i = 0; i < imageKey_.size(); i++) {
		// ImageData imageData = demoImage_.getImageData((String) imageKey_
		// .get(i));
		// switch (locations[i]) {
		// // Draw on the top left corner
		// case TOP_LEFT:
		// drawImage(imageData, 0, 0);
		// break;
		//
		// // Draw on top right corner
		// case TOP_RIGHT:
		// drawImage(imageData, sizeOfImage_.x - imageData.width, 0);
		// break;
		//
		// // Draw on bottom left
		// case BOTTOM_LEFT:
		drawImage(fDecoImage.getImageData(), 0, fSizeOfImage.y - fDecoImage.getImageData().height);
		// break;
		//
		// // Draw on bottom right corner
		// case BOTTOM_RIGHT:
		// drawImage(imageData, sizeOfImage_.x - imageData.width,
		// sizeOfImage_.y - imageData.height);
		// break;
		//
		// }
		// }

	}

	// /**
	// * Organize the images. This function scans through the image key and
	// finds
	// * out the location of the images
	// */
	// private int[] organizeImages() {
	// int[] locations = new int[imageKey_.size()];
	// String imageKeyValue;
	// for (int i = 0; i < imageKey_.size(); i++) {
	// imageKeyValue = (String) imageKey_.get(i);
	// if (imageKeyValue.equals("Lock")) {
	// // Draw he lock icon in top left corner.
	// locations[i] = TOP_LEFT;
	// }
	// if (imageKeyValue.equals("Dirty")) {
	// // Draw dirty flag indicator in the top right corner
	// locations[i] = TOP_RIGHT;
	// }
	// if (imageKeyValue.equals("Extract")) {
	// // Draw the extract indicator in the top right corner.
	// locations[i] = TOP_RIGHT;
	// }
	// if (imageKeyValue.equals("Owner")) {
	// // Draw he lock icon in top left corner.
	// locations[i] = BOTTOM_LEFT;
	// }
	//
	// }
	// return locations;
	// }

	/**
	 * @see org.eclipse.jface.resource.CompositeImageDescriptor#getSize() get the size of the object
	 */
	@Override
	protected Point getSize() {
		return fSizeOfImage;
	}

	/**
	 * Get the image formed by overlaying different images on the base image
	 * 
	 * @return composite image
	 */
	public Image getImage() {
		return createImage();
	}
}
