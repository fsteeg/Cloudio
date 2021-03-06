/*******************************************************************************
 * Copyright (c) 2011 Department of Computational Linguistics, University of Cologne, Germany.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Department of Computational Linguistics, University of Cologne, Germany - initial API and implementation
 ******************************************************************************/
package org.schwiebert.cloudio.util;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;

/**
 * A custom variation of a {@link Rectangle}, which stores
 * the required values as short instead of int, thus saving
 * some space. 
 * 
 * @author sschwieb
 *
 */
public class SmallRect {
	
	final short x, y, width, height;

	public SmallRect(int x, int y, int width, int height) {
		this.x = (short) x;
		this.y = (short) y;
		this.width = (short) width;
		this.height = (short) height;
	}

	public boolean intersects(SmallRect rect) {
		if (rect == null) SWT.error(SWT.ERROR_NULL_ARGUMENT);
		return rect == this || intersects (rect.x, rect.y, rect.width, rect.height);
	}
	
	public boolean intersects (final int x, final int y, final int width, final int height) {
		return (x < this.x + this.width) && (y < this.y + this.height) &&
			(x + width > this.x) && (y + height > this.y);
	}

	public String toString () {
		return "Rectangle {" + x + ", " + y + ", " + width + ", " + height + "}";
	}
	
}
