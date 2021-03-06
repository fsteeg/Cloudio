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
package org.schwiebert.cloudio;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.ContentViewer;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseWheelListener;
import org.eclipse.swt.widgets.Control;
import org.schwiebert.cloudio.layout.ILayouter;
import org.schwiebert.cloudio.util.Word;


/**
 * 
 * @author sschwieb
 */
public class TagCloudViewer extends ContentViewer {

	private TagCloud cloud;
	
	private Set<Word> selection = new HashSet<Word>();
	
	private Map<Object, Word> objectMap = new HashMap<Object, Word>();

	private int maxWords = 300;

	private IProgressMonitor monitor;

	public TagCloudViewer(TagCloud cloud) {
		this.cloud = cloud;
		initListeners();
	}
	
	protected void initListeners() {
		cloud.addMouseListener(new MouseListener() {
			
			@Override
			public void mouseUp(MouseEvent e) {
				Word word = (Word) e.data;
				if(word == null) return;
				boolean remove = selection.remove(word);
				if(!remove) selection.add(word);
				cloud.setSelection(selection);
			}
			
			@Override
			public void mouseDown(MouseEvent e) {
				
			}
			
			@Override
			public void mouseDoubleClick(MouseEvent e) {
			}
		});
		cloud.addMouseWheelListener(new MouseWheelListener() {
			
			@Override
			public void mouseScrolled(MouseEvent e) {
				if(e.count > 0) {
					cloud.zoomIn();
				} else {
					cloud.zoomOut();
				}
				
			}
		});
	}

	@Override
	public Control getControl() {
		return getCloud();
	}

	@Override
	public ISelection getSelection() {
		List<Object> elements = new ArrayList<Object>();
		for (Word word : selection) {
			elements.add(word.data);
		}
		return new StructuredSelection(elements);
	}

	@Override
	public void refresh() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setSelection(ISelection selection, boolean reveal) {
		this.selection.clear();
		IStructuredSelection sel = (IStructuredSelection) selection;
		Iterator<?> iterator = sel.iterator();
		while(iterator.hasNext()) {
			Object next = iterator.next();
			Word word = objectMap.get(next);
			if(word != null) {
				this.selection.add(word);				
			}
		}
		cloud.setSelection(this.selection);
	}

	public void setFocus() {
		cloud.setFocus();
	}

	/**
	 * Resets the {@link TagCloud}. If <code>recalc</code> is
	 * <code>true</code>, the displayed elements will be updated
	 * with the values provided by used {@link ICloudLabelProvider}.
	 * Otherwise, the cloud will only be re-layouted, keeping fonts,
	 * colors and angles untouched.
	 * @param monitor
	 * @param recalc
	 */
	public void reset(IProgressMonitor monitor, boolean recalc) {
		cloud.layoutCloud(monitor, recalc);
	}

	/**
	 * Returns the {@link TagCloud} managed by this viewer.
	 * @return
	 */
	public TagCloud getCloud() {
		return cloud;
	}
	
	/**
	 * Sets the label provider of this viewer, which must be an
	 * {@link ICloudLabelProvider}.
	 */
	@Override
	public void setLabelProvider(IBaseLabelProvider labelProvider) {
		super.setLabelProvider(labelProvider);
		Assert.isTrue(labelProvider instanceof ICloudLabelProvider);
	}
	
	/**
	 * Sets the content provider of this viewer, which must be
	 * an {@link IStructuredContentProvider}.
	 */
	@Override
	public void setContentProvider(IContentProvider contentProvider) {
		super.setContentProvider(contentProvider);
		Assert.isTrue(contentProvider instanceof IStructuredContentProvider);
	}
		
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.viewers.Viewer#inputChanged(java.lang.Object, java.lang.Object)
	 */
	@Override
	protected void inputChanged(Object input, Object oldInput) {
		selection.clear();
		objectMap.clear();
		IStructuredContentProvider contentProvider = (IStructuredContentProvider) getContentProvider();
		Object[] elements = contentProvider.getElements(input);
		List<Word> words = new ArrayList<Word>();
		ICloudLabelProvider labelProvider = (ICloudLabelProvider) getLabelProvider();
		short i = 0;
		for (Object element : elements) {
			Word word = new Word(labelProvider.getLabel(element));
			word.setColor(labelProvider.getColor(element));
			word.weight = labelProvider.getWeight(element);
			word.setFontData(labelProvider.getFontData(element));
			word.angle = labelProvider.getAngle(element);
			word.data = element;
			Assert.isNotNull(word.string, "Labelprovider must return a String for each element");
			Assert.isNotNull(word.getColor(), "Labelprovider must return a Color for each element");
			Assert.isNotNull(word.getFontData(), "Labelprovider must return a FontData for each element");
			Assert.isLegal(word.weight >= 0, "Labelprovider must return a weight between 0 and 1 (inclusive), but value was " + word.weight);
			Assert.isLegal(word.weight <= 1, "Labelprovider must return a weight between 0 and 1 (inclusive), but value was " + word.weight);
			Assert.isLegal(word.angle >= -90, "Angle of an element must be between -90 and +90 (inclusive), but was " + word.angle);
			Assert.isLegal(word.angle <= 90, "Angle of an element must be between -90 and +90 (inclusive), but was " + word.angle);
			words.add(word);
			i++;
			word.id = i;
			objectMap.put(element, word);
			if(i == maxWords) break;
		}
		selection.clear();
		if(monitor != null) {
			monitor.subTask("Layouting...");
		}
		cloud.setWords(words, monitor);
	}

	/**
	 * Sets the maximum number of elements which will be
	 * displayed by the cloud. Note that there is no guarantee
	 * that this amount of elements will actually be displayed,
	 * as this depends on additional factors.
	 * @return
	 */
	public void setMaxWords(int words) {
		this.maxWords = words;
	}
	
	/**
	 * Calls {@link TagCloud#zoomFit()} to scale the cloud such
	 * that it fits the current visible area.
	 */
	public void zoomFit() {
		cloud.zoomFit();
	}

	/**
	 * Zooms in
	 */
	public void zoomIn() {
		cloud.zoomIn();
	}

	/**
	 * Zooms out
	 */
	public void zoomOut() {
		cloud.zoomOut();
	}
	
	/**
	 * Resets the zoom to 100%
	 */
	public void zoomReset() {
		cloud.zoomReset();
	}

	public void setBoost(int boost) {
		cloud.setBoost(boost);
	}
	
	/**
	 * Returns the maximum number of elements which will be
	 * displayed by the cloud. Note that there is no guarantee
	 * that this amount of elements will actually be displayed,
	 * as this depends on additional factors.
	 * @return
	 */
	public int getMaxWords() {
		return maxWords;
	}

	public void setInput(Object input, IProgressMonitor progressMonitor) {
		this.monitor = progressMonitor;
		super.setInput(input);
		this.monitor = null;
	}

	public void setBoostFactor(float boostFactor) {
		cloud.setBoostFactor(boostFactor);
	}

	public void setLayouter(ILayouter layouter) {
		cloud.setLayouter(layouter);
	}
	
}
