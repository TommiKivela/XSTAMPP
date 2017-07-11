/*******************************************************************************
 * Copyright (c) 2013, 2017 Lukas Balzer, Asim Abdulkhaleq, Stefan Wagner
 * Institute of Software Technology, Software Engineering Group
 * University of Stuttgart, Germany
 *  
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
/**
 * 
 * @author Lukas Balzer
 */
package xstampp.stpapriv.wizards.stepData;

import java.io.IOException;

import messages.Messages;

import org.eclipse.jface.dialogs.MessageDialog;

import xstampp.model.IDataModel;
import xstampp.stpapriv.Activator;
import xstampp.stpapriv.messages.SecMessages;
import xstampp.stpapriv.util.jobs.XCSVExportJob;
import xstampp.stpapriv.wizards.AbstractExportWizard;
import xstampp.ui.common.ProjectManager;
import xstampp.ui.wizards.CSVExportPage;

/**
 * 
 * @author Lukas Balzer
 * 
 */
public class RefinedSCCSVWizard extends AbstractExportWizard {

	/**
	 * 
	 * @author Lukas Balzer
	 * 
	 */
	public RefinedSCCSVWizard() {
		super("");
		String[] filters = new String[] { "*.csv" }; //$NON-NLS-1$
		this.setExportPage(new CSVExportPage(filters, SecMessages.RefinedSecurityConstraintsTable + Messages.AsDataSet, Activator.PLUGIN_ID));
	}

	@Override
	public boolean performFinish() {
		String filePath = getExportPage().getExportPath();
		try {
			if (this.checkError(this.checkPath(filePath))) {
				IDataModel model = ProjectManager.getContainerInstance()
						.getDataModel(this.getExportPage().getProjectID());
				XCSVExportJob export = new XCSVExportJob("Export CSV",	filePath,
						((CSVExportPage) this.getExportPage()).getSeperator(),
						model, XCSVExportJob.REFINED_CONSTRAINTS);
				export.schedule();
			} else {
				return false;
			}
		} catch (IOException e) {
			MessageDialog.openWarning(this.getShell(), Messages.Warning,
					Messages.ChooseTheDestination);
			return false;
		}
		return true;
	}

}
