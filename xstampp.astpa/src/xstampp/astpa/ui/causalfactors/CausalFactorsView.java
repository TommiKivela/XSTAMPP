/*******************************************************************************
 * 
 * Copyright (c) 2013-2016 A-STPA Stupro Team Uni Stuttgart (Lukas Balzer, Adam
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

package xstampp.astpa.ui.causalfactors;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPart;

import messages.Messages;
import xstampp.astpa.haz.ITableModel;
import xstampp.astpa.model.causalfactor.CausalFactor;
import xstampp.astpa.model.causalfactor.interfaces.CausalFactorEntryData;
import xstampp.astpa.model.causalfactor.interfaces.ICausalComponent;
import xstampp.astpa.model.causalfactor.interfaces.ICausalFactor;
import xstampp.astpa.model.causalfactor.interfaces.ICausalFactorEntry;
import xstampp.astpa.model.controlaction.safetyconstraint.ICorrespondingUnsafeControlAction;
import xstampp.astpa.model.controlstructure.components.ComponentType;
import xstampp.astpa.model.interfaces.ICausalFactorDataModel;
import xstampp.model.IDataModel;
import xstampp.model.ObserverValue;
import xstampp.ui.common.ProjectManager;
import xstampp.ui.common.grid.CellButton;
import xstampp.ui.common.grid.GridCellColored;
import xstampp.ui.common.grid.GridCellLinking;
import xstampp.ui.common.grid.GridCellText;
import xstampp.ui.common.grid.GridRow;
import xstampp.ui.common.grid.GridWrapper;
import xstampp.ui.editors.AbstractFilteredEditor;

/**
 * The view to add causal factors to control structure components, edit them and
 * add links to the related hazards.
 * 
 * @author Benedikt Markt, Patrick Wickenhaeuser
 */
public class CausalFactorsView extends AbstractFilteredEditor{

	private static final RGB PARENT_BACKGROUND_COLOR = new RGB(215, 240, 255);
	private int internalUpdates;
	private static final String CAUSALFACTORS= "Text filter for Causal Factors";
	private Map<UUID,CausalFactor> factorsToUUIDs;
	private DeleteCFAction deleteAction;
	 /**
   * ViewPart ID.
   */
  public static final String ID = "astpa.steps.step3_2";

  private ICausalFactorDataModel dataInterface = null;

  /**
   * The log4j logger.
   */
  private static final Logger LOGGER = Logger.getRootLogger();


  private GridWrapper grid;

  private boolean lockreload;

	private class NewConstraintButton extends CellButton {

    private UUID componentId;
    private UUID factorId;
    private UUID entryId;

		public NewConstraintButton(UUID componentId, UUID factorId,UUID entryId) {
			super(new Rectangle(
          4, 1,
          GridWrapper.getAddButton16().getBounds().width,
          GridWrapper.getAddButton16().getBounds().height),
          GridWrapper.getAddButton16());
      this.componentId = componentId;
      this.factorId = factorId;
      this.entryId = entryId;
		}

		@Override
		public void onButtonDown(Point relativeMouse, Rectangle cellBounds) {
	    CausalFactorEntryData data = new CausalFactorEntryData(entryId);
	    data.setConstraint(new String());
	    dataInterface.changeCausalEntry(componentId, factorId, data);
		}
	}


	/**
	 * Ctor.
	 * 
	 * @author Patrick Wickenhaeuser
	 * 
	 */
	public CausalFactorsView() {
		this.factorsToUUIDs = new HashMap<>();
		this.dataInterface = null;
		this.grid = null;
		setUseFilter(true);
		setGlobalCategory("ALL");
	}

	@Override
	protected void updateFilter() {

		this.reloadTable();
	}
	@Override
	public String getId() {
		return CausalFactorsView.ID;
	}

	@Override
	public String getTitle() {
		return Messages.CausalFactorsTable;
	}
	@Override
	public void createPartControl(Composite parent) {
		this.internalUpdates = 0;
		this.setDataModelInterface(ProjectManager.getContainerInstance()
				.getDataModel(this.getProjectID()));
		parent.setLayout(new GridLayout(1, false));
		super.createPartControl(parent);
		this.grid = new GridWrapper(parent, new String[] { Messages.Component,
				Messages.CausalFactors,"UCA", Messages.HazardLinks,"Scenarios",
				Messages.SafetyConstraint, Messages.NotesSlashRationale });
		this.grid.getGrid().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		this.deleteAction = new DeleteCFAction(grid, dataInterface, Messages.CausalFactors, null);
		this.reloadTable();
	}
	@Override
	protected Map<String, Boolean> getCategories() {
		Map<String, Boolean> categories= new HashMap<>();
		categories.put("ALL", false);
		categories.put(ComponentType.ACTUATOR.name(), true);
		categories.put(ComponentType.CONTROLLER.name(), true);
		categories.put(ComponentType.CONTROLLED_PROCESS.name(), true);
		categories.put(ComponentType.SENSOR.name(), true);
		categories.put(CAUSALFACTORS, false);
		return categories;
	}
	
	@Override
	protected String[] getCategoryArray() {
		return new String[]{"ALL",ComponentType.ACTUATOR.name(), 
				ComponentType.CONTROLLER.name(),
				ComponentType.CONTROLLED_PROCESS.name(),
				ComponentType.SENSOR.name(),
				CAUSALFACTORS};
	}
	
	private boolean isFiltered(ICausalComponent component) {
		//filters for the text filter 
		if(getActiveCategory().equals("ALL")){
			return isFiltered(component.getText());
		}
		else if(getActiveCategory().equals(CAUSALFACTORS)){
			for(ICausalFactor factor: component.getCausalFactors()){
				if(!isFiltered(factor.getText())){
					return false;
				}
			}
			return true;
		}
		return isFiltered(component.getText(),component.getComponentType().name());
	}
	/**
	 * Fill the table.
	 * 
	 * @author Patrick Wickenhaeuser
	 * 
	 * @param components
	 *            the list of components.
	 */
	private void fillTable() {
	  List<ICausalComponent> components = this.dataInterface.getCausalComponents(null);
		for (ICausalComponent component : components) {
			if(isFiltered(component)){
				continue;
			}
			GridRow csRow = new GridRow(1);
			GridCellText causalComp = new GridCellText(component.getText());
			csRow.addCell(causalComp);
			for(int i=0;i<6; i++){
			  csRow.addCell(new GridCellColored(this.grid,CausalFactorsView.PARENT_BACKGROUND_COLOR));
			}
			this.grid.addRow(csRow);
			Map<UUID,ICorrespondingUnsafeControlAction> ucaMap = new HashMap<>();
      for(ICorrespondingUnsafeControlAction uca : dataInterface.getUCAList(null)){
        ucaMap.put(uca.getId(), uca); 
      }
			//each causal factor is displayed as child row of the causal component
			for (ICausalFactor factor : component.getCausalFactors()) {
				if(isFiltered(factor.getText(),CAUSALFACTORS)){
					continue;
				}
				GridRow childRow = new GridRow(1);
				childRow.addCell(new CellEditorCausalFactor(this.grid, dataInterface, factor
						.getText(), component.getId(),factor.getId()));
				
		   //A new row is added to the factorRow for adding additional entries
//        for(int i=1; i<5;i++){
//          childRow.addCell(new GridCellColored(this.grid,CausalFactorsView.PARENT_BACKGROUND_COLOR));
//        }
				//the causal factor contains multiple child rows for each causal factor entry
        for(ICausalFactorEntry entry : factor.getAllEntries()){
  				GridRow entryRow = new GridRow(1);
          CausalFactorsView.LOGGER.info("Adding new GridCellLinking"); //$NON-NLS-1$
          
          /*
           * Depending on whether the entry is linked to a uca or not
           * the uca column is filled and the hazards are either based on the uca
           * or linkable
           */
          if(entry.getUcaLink() != null && ucaMap.containsKey(entry.getUcaLink())){
            String ucaDescription = ucaMap.get(entry.getUcaLink()).getDescription();
            entryRow.addCell(new CellEditorCausalEntry(grid, dataInterface, ucaDescription,
                                              component.getId(), factor.getId(), entry.getId()));
            List<UUID> hazIds = dataInterface.getLinksOfUCA(entry.getUcaLink());
            String linkingString = new String();
            for(ITableModel hazard : dataInterface.getHazards(hazIds)){
              linkingString += "H-" +hazard.getNumber() + ",";
            }
            entryRow.addCell(new GridCellText(linkingString.substring(0, linkingString.length()-2)));
            entryRow.addCell(new GridCellLinking<ContentProviderScenarios>(
                factor.getId(), new ContentProviderScenarios(dataInterface, component.getId(), factor.getId(), entry),
                this.grid));
            
          }else{
            entryRow.addCell(new CellEditorCausalEntry(grid, dataInterface, new String(),
                                              component.getId(), factor.getId(), entry.getId()));
            entryRow.addCell(new GridCellLinking<ContentProviderHazards>(
    						factor.getId(), new ContentProviderHazards(dataInterface, component.getId(), factor.getId(), entry),
    						this.grid));

            entryRow.addCell(new GridCellText());
          }
          
          
          /*
           * The Safety Constraint is dispayed if available, if the text is
           * null than a new entry can be added or one of the existing constraints 
           * can be imported
           */
  				if (entry.getConstraintText() == null) {
  					GridCellText constraintsCell = new GridCellText(new String());
  					constraintsCell.addCellButton(new NewConstraintButton(component.getId(), factor.getId(),entry.getId()));
  					constraintsCell.addCellButton(new CellButtonImportConstraint(grid.getGrid(),entry,component.getId(), factor.getId(),dataInterface));
  					entryRow.addCell(constraintsCell);
  				} else {
  				  entryRow.addCell(new CellEditorSafetyConstraint(grid, dataInterface, component.getId(), factor.getId(),entry));
  				}
  
  				entryRow.addCell(new CellEditorFactorNote(this.grid,dataInterface,component.getId(), factor.getId(),entry));
          childRow.addChildRow(entryRow);
        }
        //A new row is added to the factorRow for adding additional entries
        GridRow addEntriesRow = new GridRow(1);
        addEntriesRow.addCell(new CellButtonAddUCAEntry(component, factor.getId(), dataInterface,grid.getGrid()));
        for(int i=1; i<5;i++){
          addEntriesRow.addCell(new GridCellColored(this.grid,CausalFactorsView.PARENT_BACKGROUND_COLOR));
        }
        childRow.addChildRow(addEntriesRow);
				csRow.addChildRow(childRow);
			}
      
			
			GridRow buttonRow = new GridRow(1);
			buttonRow.addCell(new CellButtonAddCausalFactor(component,dataInterface));

			csRow.addChildRow(buttonRow);
		}
	}

	
	/**
	 * Reload the whole table.
	 * 
	 * @author Patrick Wickenhaeuser
	 * 
	 */
	private void reloadTable() {
		if(!this.lockreload){
			this.lockreload = true;
			int tmp= this.grid.getGrid().getVerticalBar().getSelection();
			
	
			this.factorsToUUIDs = new HashMap<>();
			this.grid.clearRows();
			this.fillTable();
			this.grid.reloadTable();
			this.lockreload = false;
			this.grid.getGrid().setTopIndex(tmp);
		}
	}

	/**
	 * sets the data model object for this editor
	 *
	 * @author Lukas
	 *
	 * @param dataInterface the data model object
	 */
	public void setDataModelInterface(IDataModel dataInterface) {
		this.dataInterface = (ICausalFactorDataModel) dataInterface;
		this.dataInterface.addObserver(this);
	}

	@Override
	public void update(Observable dataModelController, Object updatedValue) {
		super.update(dataModelController, updatedValue);
		if(internalUpdates > 0){
			internalUpdates--;
		}else{
			switch ((ObserverValue) updatedValue) {
			case CONTROL_STRUCTURE:
			case CAUSAL_FACTOR:
				Display.getDefault().syncExec(new Runnable() {
	
					@Override
					public void run() {
						CausalFactorsView.this.reloadTable();
					}
				});
				break;
			default:
				break;
			}
		}
	}


	@Override
	public void dispose() {
		this.dataInterface.deleteObserver(this);
		super.dispose();
	}
	
	@Override
	public void partBroughtToTop(IWorkbenchPart arg0) {
		if(!this.lockreload && this.factorsToUUIDs.size() > 0){
			this.lockreload = true;
			
			this.lockreload = false;
			this.factorsToUUIDs = new HashMap<>();
		}
		super.partBroughtToTop(arg0);
	}
}
