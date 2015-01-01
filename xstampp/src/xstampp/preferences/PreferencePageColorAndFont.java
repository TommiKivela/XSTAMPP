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

package xstampp.preferences;

import messages.Messages;

import org.eclipse.jface.preference.ColorFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.FontFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import xstampp.Activator;

/**
 * Generate the color preference page.
 * 
 * @author Sebastian Sieber
 * 
 */
public class PreferencePageColorAndFont extends FieldEditorPreferencePage
		implements IWorkbenchPreferencePage {

	private ColorFieldEditor navigationSelectedColor,
			navigationUnselectedColor, hoverColor, splitterForegroundColor,
			splitterBackgroundColor, splitterFontColor;

	private FontFieldEditor navigationTitel, defaultFont;

	/**
	 * Constructor using grid layout.
	 */
	public PreferencePageColorAndFont() {
		super(FieldEditorPreferencePage.GRID);
	}

	@Override
	public void init(IWorkbench workbench) {
		this.setPreferenceStore(Activator.getDefault().getPreferenceStore());
		this.setDescription(Messages.ChangeColorsAnd);
	}

	@Override
	protected void createFieldEditors() {
		this.navigationSelectedColor = new ColorFieldEditor(
				IPreferenceConstants.NAVIGATION_ITEM_SELECTED,
				Messages.SelectedNavItem, this.getFieldEditorParent());
		this.addField(this.navigationSelectedColor);

		this.navigationUnselectedColor = new ColorFieldEditor(
				IPreferenceConstants.NAVIGATION_ITEM_UNSELECTED,
				Messages.UnselectedNavItem, this.getFieldEditorParent());
		this.addField(this.navigationUnselectedColor);

		this.hoverColor = new ColorFieldEditor(IPreferenceConstants.HOVER_ITEM,
				Messages.Hover, this.getFieldEditorParent());
		this.addField(this.hoverColor);

		this.splitterForegroundColor = new ColorFieldEditor(
				IPreferenceConstants.SPLITTER_FOREGROUND,
				Messages.SplitterForegColor, this.getFieldEditorParent());
		this.addField(this.splitterForegroundColor);

		this.splitterBackgroundColor = new ColorFieldEditor(
				IPreferenceConstants.SPLITTER_BACKGROUND,
				Messages.SplitterBackgColor, this.getFieldEditorParent());
		this.addField(this.splitterBackgroundColor);

		this.splitterFontColor = new ColorFieldEditor(
				IPreferenceConstants.SPLITTER_FONT, Messages.SplitterFontColor,
				this.getFieldEditorParent());
		this.addField(this.splitterFontColor);

		// Fonts
		this.navigationTitel = new FontFieldEditor(
				IPreferenceConstants.NAVIGATION_TITLE_FONT, Messages.TitleFont,
				this.getFieldEditorParent());
		this.addField(this.navigationTitel);

		this.defaultFont = new FontFieldEditor(
				IPreferenceConstants.DEFAULT_FONT, Messages.DefaultFont,
				this.getFieldEditorParent());
		this.addField(this.defaultFont);
	}

	@Override
	protected void checkState() {
		super.checkState();
	}

	@Override
	protected void performDefaults() {
		this.navigationSelectedColor.loadDefault();
		this.navigationUnselectedColor.loadDefault();
		this.hoverColor.loadDefault();
		this.splitterForegroundColor.loadDefault();
		this.splitterBackgroundColor.loadDefault();
		this.splitterFontColor.loadDefault();
		this.navigationTitel.loadDefault();
		this.defaultFont.loadDefault();

	}

	@Override
	public boolean performOk() {
		this.navigationSelectedColor.store();
		this.navigationUnselectedColor.store();
		this.hoverColor.store();
		this.splitterForegroundColor.store();
		this.splitterBackgroundColor.store();
		this.splitterFontColor.store();
		this.navigationTitel.store();
		this.defaultFont.store();

		return super.performOk();
	}
}