package xstpa.ui;

import java.util.Collections;
import java.util.List;
import java.util.Observable;

import messages.Messages;

import org.eclipse.jface.viewers.ColumnLabelProvider;

import xstampp.astpa.ui.sds.CSCView;
import xstampp.model.ILTLProvider;
import xstampp.model.ObserverValue;
import xstpa.model.XSTPADataController;

public class RefinedSafetyConstraintsView extends CSCView{

	XSTPADataController dataController;
	
	public RefinedSafetyConstraintsView() {
		this.filter = new RefinedEntryFilter();
		this.headers[0] = Messages.ID;
		this.headers[1] = "Refined Unsafe Control Actions";
		this.headers[2] = Messages.ID;
		this.headers[3] = getTitle();
	}
	@Override
	protected Object getInput() {
		if(dataController == null){
			return null;
		}
		List<ILTLProvider> allRUCA = dataController.getModel().getAllRefinedRules();
  	    Collections.sort(allRUCA);
  	    
  	    return allRUCA;
	}
	
	@Override
	public String getTitle() {
		return "Refined Safety Constraints";
	}
	
	/**
	 * @return the dataController
	 */
	public XSTPADataController getDataController() {
		return this.dataController;
	}

	/**
	 * @param dataController the dataController to set
	 */
	public void setDataController(XSTPADataController dataController) {
		if(this.dataController != dataController){
			this.dataController = dataController;
			
			this.update(dataController, ObserverValue.CONTROL_ACTION);
			this.dataController.addObserver(this);
		}
	}
	
	@Override
	public void update(Observable dataModelController, Object updatedValue) {
		ObserverValue type = (ObserverValue) updatedValue;
		switch (type) {
			case CONTROL_ACTION:
					refresh();
		default:
			break;
		}
	}
	
	@Override
	protected ColumnLabelProvider getColumnProvider(int columnIndex) {

		switch(columnIndex){
		case 0: 
			return new ColumnLabelProvider(){
				@Override
				public String getText(Object element) {
					return "RSR1."+((ILTLProvider)element).getNumber();
				}
			};
		case 1:
			return new ColumnLabelProvider(){
				@Override
				public String getText(Object element) {
					return ((ILTLProvider)element).getRefinedUCA();
				}
			};
		case 2:
			return new ColumnLabelProvider(){
				@Override
				public String getText(Object element) {
					return "RSC2."+((ILTLProvider)element).getNumber();
				}
			};
		case 3:
			return new ColumnLabelProvider(){
				@Override
				public String getText(Object element) {
					return ((ILTLProvider)element).getSafetyRule();
				}
			};
			
			
		}
		return null;
	
	}
	
	
	
}
