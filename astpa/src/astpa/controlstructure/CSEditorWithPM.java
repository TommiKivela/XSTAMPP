/*******************************************************************************
 * Copyright (c) 2013 A-STPA Stupro Team Uni Stuttgart (Lukas Balzer, Adam
 * Grahovac, Jarkko Heidenwag, Benedikt Markt, Jaqueline Patzek, Sebastian
 * Sieber, Fabian Toth, Patrick Wickenhäuser, Aliaksei Babkovich, Aleksander
 * Zotov).
 * 
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 *******************************************************************************/

package astpa.controlstructure;

import messages.Messages;

import org.eclipse.draw2d.Viewport;
import org.eclipse.gef.palette.CombinedTemplateCreationEntry;
import org.eclipse.gef.palette.MarqueeToolEntry;
import org.eclipse.gef.palette.PaletteDrawer;
import org.eclipse.gef.palette.PaletteRoot;
import org.eclipse.gef.palette.PaletteSeparator;
import org.eclipse.gef.palette.PanningSelectionToolEntry;
import org.eclipse.jface.resource.ImageDescriptor;

import astpa.Activator;
import astpa.controlstructure.controller.factorys.CSModelCreationFactory;
import astpa.model.controlstructure.components.ComponentType;

/**
 * 
 * @author Aliaksei Babkovich, Lukas Balzer
 * 
 */
public class CSEditorWithPM extends CSAbstractEditor {
	
	/**
	 * The ID is used to reference the {@link CSEditorWithPM}
	 */
	public static final String ID = "CSDiagrammPM"; //$NON-NLS-1$
	private double zoomLevel;
	private Viewport viewLocation;
	
	
	/**
	 * this sets the zoom initially to 100%
	 * 
	 * @author Lukas Balzer
	 * 
	 */
	public CSEditorWithPM() {
		this.zoomLevel = 1.0;
		this.viewLocation = null;
	}
	
	@Override
	public PaletteRoot getPaletteRoot() {
		PaletteRoot root = new PaletteRoot();
		
		PaletteDrawer manipGroup = new PaletteDrawer(Messages.ManipulationObjects);
		root.add(manipGroup);
		
		PanningSelectionToolEntry selectionToolEntry = new PanningSelectionToolEntry();
		selectionToolEntry.setDescription(Messages.SpacePlusMouseTo);
		manipGroup.add(selectionToolEntry);
		manipGroup.add(new MarqueeToolEntry());
		
		root.setDefaultEntry(selectionToolEntry);
		
		PaletteSeparator separator = new PaletteSeparator();
		root.add(separator);
		PaletteDrawer componentElements = new PaletteDrawer(Messages.ComponentElements);
		root.add(componentElements);
		
		ImageDescriptor imgDesc = Activator.getImageDescriptor("/icons/buttons/controlstructure/process_model_40.png"); //$NON-NLS-1$
		componentElements.add(new CombinedTemplateCreationEntry(Messages.ProcessModel, Messages.CreateProcessModel,
			ComponentType.PROCESS_MODEL, new CSModelCreationFactory(ComponentType.PROCESS_MODEL), imgDesc, imgDesc));
		
		PaletteDrawer additionalElements = new PaletteDrawer(Messages.Others);
		root.add(additionalElements);
		
		imgDesc = Activator.getImageDescriptor("/icons/buttons/controlstructure/process_variable.png"); //$NON-NLS-1$
		additionalElements.add(new CombinedTemplateCreationEntry(Messages.ProcessVariable,
			Messages.CreateProcessVariable, ComponentType.PROCESS_VARIABLE, new CSModelCreationFactory(
				ComponentType.PROCESS_VARIABLE), imgDesc, imgDesc));
		
		imgDesc = Activator.getImageDescriptor("/icons/buttons/controlstructure/process_value.png"); //$NON-NLS-1$
		additionalElements.add(new CombinedTemplateCreationEntry(Messages.ProcessValue, Messages.CreateProcessValue,
			ComponentType.PROCESS_VALUE, new CSModelCreationFactory(ComponentType.PROCESS_VALUE), imgDesc, imgDesc));
		
		return root;
	}
	
	@Override
	public String getTitle() {
		return Messages.ControlStructureDiagramWithProcessModel;
	}
	
	@Override
	public String getId() {
		return CSEditorWithPM.ID;
	}
	
	@Override
	public double getZoomLevel() {
		return this.zoomLevel;
	}
	
	@Override
	public void setZoomLevel(double zoom) {
		this.zoomLevel = zoom;
		
	}
	
	@Override
	public Viewport getViewport() {
		return this.viewLocation;
	}
	
	@Override
	public void setViewport(Viewport view) {
		this.viewLocation = view;
	}
	
	@Override
	public boolean triggerExport(String path) {
		return this.printStructure(path,Messages.ExportCSwithPM, Messages.ExportingCSwithPM);
	}
}
