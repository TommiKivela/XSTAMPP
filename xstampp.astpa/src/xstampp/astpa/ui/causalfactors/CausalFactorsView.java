/*******************************************************************************
 * 
 * Copyright (c) 2013-2017 A-STPA Stupro Team Uni Stuttgart (Lukas Balzer, Adam Grahovac, Jarkko
 * Heidenwag, Benedikt Markt, Jaqueline Patzek, Sebastian Sieber, Fabian Toth, Patrick
 * Wickenhäuser, Aliaksei Babkovich, Aleksander Zotov).
 * 
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 *******************************************************************************/

package xstampp.astpa.ui.causalfactors;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Optional;
import java.util.UUID;

import org.eclipse.swt.widgets.Composite;

import messages.Messages;
import xstampp.astpa.model.causalfactor.interfaces.ICausalFactor;
import xstampp.astpa.model.controlaction.interfaces.IUnsafeControlAction;
import xstampp.astpa.model.controlaction.safetyconstraint.ICorrespondingUnsafeControlAction;
import xstampp.astpa.model.controlstructure.components.ComponentType;
import xstampp.astpa.model.controlstructure.interfaces.IRectangleComponent;
import xstampp.astpa.model.interfaces.ICausalFactorDataModel;
import xstampp.astpa.model.interfaces.IExtendedDataModel.ScenarioType;
import xstampp.astpa.model.interfaces.ITableModel;
import xstampp.astpa.model.linking.Link;
import xstampp.astpa.model.linking.LinkingType;
import xstampp.astpa.ui.CommonGridView;
import xstampp.model.IDataModel;
import xstampp.model.ObserverValue;
import xstampp.ui.common.ProjectManager;
import xstampp.ui.common.grid.CellButtonLinking;
import xstampp.ui.common.grid.DeleteGridEntryAction;
import xstampp.ui.common.grid.GridCellText;
import xstampp.ui.common.grid.GridCellTextEditor;
import xstampp.ui.common.grid.GridRow;
import xstampp.ui.common.grid.IGridCell;
import xstampp.usermanagement.api.AccessRights;

/**
 * The view to add causal factors to control structure components, edit them and add links to the
 * related hazards.
 * 
 * @author Benedikt Markt, Patrick Wickenhaeuser, Lukas Balzer
 */
public class CausalFactorsView extends CommonGridView<ICausalFactorDataModel> {

  private static final String CAUSALFACTORS = "Text filter for Causal Factors";
  private static List<String> _withScenarioColumns = Arrays.asList(Messages.Component,
      Messages.CausalFactors, "Unsafe Control Action", Messages.HazardLinks, "Causal Scenarios",
      Messages.SafetyConstraint, Messages.NotesSlashRationale);
  private static List<String> _withoutScenarioColumns = Arrays.asList(Messages.Component,
      Messages.CausalFactors, "Unsafe Control Action", Messages.HazardLinks,
      Messages.SafetyConstraint, "Design Hint", Messages.NotesSlashRationale);
  /**
   * ViewPart ID.
   */
  public static final String ID = "astpa.steps.step3_2";
  private boolean includeFirstChildRow;

  /**
   * Ctor.
   * 
   * @author Patrick Wickenhaeuser
   * 
   */
  public CausalFactorsView() {
    setUseFilter(true);
    this.includeFirstChildRow = false;
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
    super.createPartControl(parent, _withoutScenarioColumns.toArray(new String[0]));
    List<ICorrespondingUnsafeControlAction> ucaList = this.getDataModel().getUCAList(null);
    String[] names = new String[ucaList.size()];
    UUID[] values = new UUID[ucaList.size()];
    for (int i = 0; i < ucaList.size(); i++) {
      names[i] = ucaList.get(i).getTitle();
      values[i] = ucaList.get(i).getId();
    }
    addChoices("UCA", names);
    addChoiceValues("UCA", values);
  }

  @Override
  public DeleteGridEntryAction<ICausalFactorDataModel> getDeleteAction() {
    return new DeleteCFAction(getGridWrapper(), getDataModel(), Messages.CausalFactors, null);
  }

  @Override
  protected Map<String, Boolean> getCategories() {
    Map<String, Boolean> categories = new HashMap<>();
    categories.put("ALL", false);
    categories.put(ComponentType.ACTUATOR.name(), true);
    categories.put(ComponentType.CONTROLLER.name(), true);
    categories.put(ComponentType.CONTROLLED_PROCESS.name(), true);
    categories.put(ComponentType.SENSOR.name(), true);
    categories.put(CAUSALFACTORS, false);
    categories.put("UCA", false);
    return categories;
  }

  @Override
  protected String[] getCategoryArray() {
    return new String[] { "ALL", ComponentType.ACTUATOR.name(), ComponentType.CONTROLLER.name(),
        ComponentType.CONTROLLED_PROCESS.name(), ComponentType.SENSOR.name(), CAUSALFACTORS, "UCA" };
  }

  public String[] getScenarioColumns() {
    return _withScenarioColumns.toArray(new String[0]);
  }

  public String[] getColumns() {
    return _withoutScenarioColumns.toArray(new String[0]);
  }

  /**
   * @return true if the component is filtered out and should not be used
   */
  private boolean isCFFiltered(IRectangleComponent comp) {
    boolean isTypeFiltered = false;
    if (!getActiveCategory().isEmpty()) {
      isTypeFiltered = getActiveCategory().equals("ALL");
      if (!isTypeFiltered && (getActiveCategory().equals(ComponentType.ACTUATOR.name())
          || getActiveCategory().equals(ComponentType.CONTROLLER.name())
          || getActiveCategory().equals(ComponentType.CONTROLLED_PROCESS.name())
          || getActiveCategory().equals(ComponentType.SENSOR.name()))) {
        isTypeFiltered = true;
        if (!getActiveCategory().equals(comp.getComponentType().name())) {
          return true;
        }
      }

    }
    if (!isTypeFiltered) {
      return false;
    }
    return isFiltered(comp.getText());
  }

  @Override
  protected void fillTable() {

    if (this.getDataModel().isUseScenarios()) {
      this.getGridWrapper().setColumnLabels(getScenarioColumns());
    } else {
      this.getGridWrapper().setColumnLabels(getColumns());
    }
    List<IRectangleComponent> components = this.getDataModel().getCausalComponents();
    for (IRectangleComponent component : components) {
      if (isCFFiltered(component)) {
        continue;
      }
      GridRow componentRow = new GridRow(this.getGridWrapper().getColumnLabels().length);
      GridCellText cell = new GridCellText(component.getText());
      cell.setToolTip(component.getText());
      componentRow.addCell(0, cell);
      getGridWrapper().addRow(componentRow);
      boolean first = this.includeFirstChildRow;

      Map<ICausalFactor, List<Link>> ucaCfLink_Component_ToCFmap = getDataModel()
          .getCausalFactorController()
          .getCausalFactorBasedMap(component, getDataModel().getLinkController());
      for (ICausalFactor factor : ucaCfLink_Component_ToCFmap.keySet()) {
        if (factor != null && !isFiltered(factor.getText(), CAUSALFACTORS)) {
          GridRow causalFactorRow;
          causalFactorRow = (first) ? componentRow
              : new GridRow(this.getGridWrapper().getColumnLabels().length, 1, new int[] { 1 });
          createCausalFactorRow(causalFactorRow, ucaCfLink_Component_ToCFmap.get(factor), component,
              factor);
          if (!first) {
            componentRow.addChildRow(causalFactorRow);
          }
          first = false;
        }
      }
      GridRow buttonRow = new GridRow(this.getGridWrapper().getColumnLabels().length);
      buttonRow.addCell(1, new GridCellButtonAddCausalFactor(component, getDataModel()));
      buttonRow.setColumnSpan(1, getGridWrapper().getColumnLabels().length - 2);
      componentRow.addChildRow(buttonRow);

    }
  }

  /**
   * 
   * @param componentRow
   * @param causalEntryLinks
   *          a List with Links of type {@link LinkingType#UcaCfLink_Component_LINK}
   * @param component
   * @param factor
   * @return
   */
  private boolean createCausalFactorRow(GridRow factorRow, List<Link> causalEntryLinks,
      IRectangleComponent component, ICausalFactor factor) {
    CellEditorCausalFactor cell = new CellEditorCausalFactor(getGridWrapper(), getDataModel(),
        factor.getText(), factor.getId());
    if (!checkAccess(AccessRights.ADMIN)) {
      cell.setReadOnly(true);
      cell.setShowDelete(false);
    }
    factorRow.addCell(1, cell);

    // the causal factor contains multiple child rows for each causal factor entry
    boolean first = this.includeFirstChildRow;

    List<IUnsafeControlAction> ucaList = new ArrayList<>();
    for (Link link : causalEntryLinks) {
      GridRow entryRow = (first) ? factorRow
          : new GridRow(this.getGridWrapper().getColumnLabels().length, 1, new int[] { 2 });
      /*
       * Depending on whether the entry is linked to a uca or not the uca column is filled and the
       * hazards are either based on the uca or linkable
       */
      Link ucaCFLink = getDataModel().getLinkController()
          .getLinkObjectFor(LinkingType.UCA_CausalFactor_LINK, link.getLinkA());
      IUnsafeControlAction uca = getDataModel().getControlActionController()
          .getUnsafeControlAction(ucaCFLink.getLinkA());
      ucaList.add(uca);
      if (!(uca == null || isFiltered(uca.getId(), "UCA"))) {
        createUCAEntry(entryRow, link, ucaCFLink, uca);
        if (!first) {
          factorRow.addChildRow(entryRow);
        }
        first = false;
      }
    }
    ;

    // A new row is added to the factorRow for adding additional entries
    GridRow addEntriesRow = new GridRow(this.getGridWrapper().getColumnLabels().length);
    if (checkAccess(AccessRights.WRITE)) {
      addEntriesRow.addCell(2, new GridCellButtonAddUCAEntry(component, factor.getId(),
          getDataModel(), getGrid(), ucaList));
    }

    addEntriesRow.setColumnSpan(2, getGridWrapper().getColumnLabels().length - 3);
    factorRow.addChildRow(addEntriesRow);
    return true;
  }

  /**
   * 
   * @param causalEntryLink
   *          a Link of type {@link LinkingType#UcaCfLink_Component_LINK}
   * @param ucaCFLink
   *          a Link of type {@link LinkingType#UCA_CausalFactor_LINK}
   * @param uca
   *          a model of type {@link IUnsafeControlAction}
   */
  private void createUCAEntry(GridRow entryRow, Link causalEntryLink, Link ucaCFLink,
      IUnsafeControlAction uca) {

    CellEditorCausalEntry cell = new CellEditorCausalEntry(getGridWrapper(), getDataModel(),
        ucaCFLink, uca, causalEntryLink.getId());
    cell.setToolTip(uca.getIdString());
    UUID controlAction = getDataModel().getControlActionForUca(uca.getId()).getId();
    if (!checkAccess(controlAction, AccessRights.WRITE)) {
      cell.setReadOnly(true);
      cell.setShowDelete(false);
    }
    entryRow.addCell(2, cell);

    boolean first = this.includeFirstChildRow;
    LinkingType entryType = getDataModel().isUseScenarios()
        ? LinkingType.CausalEntryLink_Scenario_LINK
        : LinkingType.CausalEntryLink_HAZ_LINK;
    // If a CausalEntryLink_SC2_LINK exists for the given UcaCfLink_Component_LINK than and
    // scenarios are not used than a single row containing only that safety constraint is created
    if (getDataModel().getLinkController().isLinked(LinkingType.CausalEntryLink_SC2_LINK,
        causalEntryLink.getId())) {
      entryType = getDataModel().isUseScenarios() ? entryType
          : LinkingType.CausalEntryLink_SC2_LINK;
    }
    for (UUID hazId : getDataModel().getLinkController().getLinksFor(LinkingType.UCA_HAZ_LINK,
        uca.getId())) {
      ITableModel hazard = getDataModel().getHazard(hazId);
      if (hazard != null) {
        getDataModel().getLinkController().addLink(LinkingType.CausalEntryLink_HAZ_LINK,
            causalEntryLink.getId(), hazId, false);
      }
    }
    // depending on the choice of entryType a set of links is iterated to create the scenarios or
    // dafetyConstaint row/s
    for (Link link : getDataModel().getLinkController().getRawLinksFor(entryType,
        causalEntryLink.getId())) {
      GridRow row = (first) ? entryRow
          : new GridRow(this.getGridWrapper().getColumnLabels().length, 1);
      switch (entryType) {
      case CausalEntryLink_Scenario_LINK: {
        if (getDataModel().getExtendedDataController().getRefinedScenario(link.getLinkB()) == null) {
          ProjectManager.getLOGGER().error("Causal Factor Scenario link with illegal Scenario id!");
        } else {
          createScenarioRow(row, link, uca);
        }
        break;
      }
      case CausalEntryLink_HAZ_LINK: {
        createHazardRow(row, link, uca);
        break;
      }
      case CausalEntryLink_SC2_LINK: {
        createSingleConstraintRow(row, link, uca);
        // In column 6 the Note/Rational cell for the UCACfLink_Component_LINK is created
        row.addCell(6,
            new CellEditorFactorNote(getGridWrapper(), getDataModel(), causalEntryLink));
        break;
      }
      default:
        ProjectManager.getLOGGER().debug(
            "Constant " + entryType.name() + " is not a valid enum for a causal factor entry");
        break;
      }
      if (!first) {
        entryRow.addChildRow(row);
      }
      first = false;
    }
    if (getDataModel().isUseScenarios()) {
      GridRow scenarioRow = (first) ? entryRow
          : new GridRow(this.getGridWrapper().getColumnLabels().length);
      GridCellText scenarioCell = new GridCellText("Add a new scenario");
      scenarioCell.addCellButton(new CellButtonAddScenario(getDataModel(), causalEntryLink, uca));
      scenarioCell.addCellButton(new CellButtonLinking<ContentProviderScenarios>(getGridWrapper(),
          new ContentProviderScenarios(getDataModel(), causalEntryLink, uca), ucaCFLink.getId()));
      scenarioRow.setColumnSpan(4, 1);
      scenarioRow.addCell(4, scenarioCell);
      if (!first) {
        entryRow.addChildRow(scenarioRow);
      }
    }
  }

  /**
   * 
   * @param entryRow
   *          the row in which the cells will be added
   * @param ucaHazLink
   *          a Link of type {@link LinkingType#CausalEntryLink_HAZ_LINK}
   * @param uca
   *          the {@link IUnsafeControlAction} to which this is linked
   * @return
   */
  private GridRow createHazardRow(GridRow entryRow, Link ucaHazLink, IUnsafeControlAction uca) {
    ITableModel hazard = getDataModel().getHazard(ucaHazLink.getLinkB());
    String hazText = hazard != null ? hazard.getIdString() + " - " + hazard.getTitle() : "no hazard";
    GridCellText hazCell = new GridCellText(hazText);
    if (hazard != null && hazard.getDescription() != null) {
      String toolTip = hazard.getDescription();
      int maxLength = 70;
      String wrappedToolTip = "";
      int nextIndex = 0;
      while (nextIndex < toolTip.length()) {
        int indexOf = toolTip.indexOf(' ', nextIndex + maxLength);
        int endIndex = indexOf < 0 ? toolTip.length() : indexOf + 1;
        wrappedToolTip += toolTip.substring(nextIndex, endIndex) + "\n";
        nextIndex = endIndex;
      }
      hazCell.setToolTip(wrappedToolTip);
    }
    entryRow.addCell(3, hazCell);

    CellEditorSafetyConstraint cell = new CellEditorSafetyConstraint(getGridWrapper(),
        getDataModel(), ucaHazLink);
    ITableModel actionForUca = getDataModel().getControlActionForUca(uca.getId());
    if (actionForUca != null && !checkAccess(actionForUca.getId(), AccessRights.WRITE)) {
      cell.setReadOnly(true);
      cell.setShowDelete(false);
    }
    entryRow.addCell(4, cell);

    Optional<Link> safetyOption = getDataModel().getLinkController()
        .getRawLinksFor(LinkingType.CausalHazLink_SC2_LINK, ucaHazLink.getId()).stream()
        .findFirst();

    IGridCell hintCell = new GridCellText("");
    if (safetyOption.isPresent()) {
      hintCell = new CellEditorFactorNote(getGridWrapper(), getDataModel(), safetyOption.get());
      ((GridCellTextEditor) hintCell).setDefaultText("Design hint...");
    }
    // in column 5 the Design Hint cell for the CausalHazLink_SC2_LINK is added
    entryRow.addCell(5, hintCell);
    // In column 6 the Note/Rational cell for the CausalEntryLink_HAZ_LINK is created
    entryRow.addCell(6, new CellEditorFactorNote(getGridWrapper(), getDataModel(), ucaHazLink));
    return null;

  }

  /**
   * 
   * @param entryRow
   *          the row in which the cells will be added
   * @param causalEntryLink
   *          a Link of type {@link ObserverValue#CausalEntryLink_SC2_LINK}
   * @param uca
   *          the {@link IUnsafeControlAction} to which this is linked
   * @return
   */
  private GridRow createSingleConstraintRow(GridRow entryRow, Link causalEntryLink,
      IUnsafeControlAction uca) {
    addHazardCell(entryRow, uca);

    CellEditorSingleSafetyConstraint cell = new CellEditorSingleSafetyConstraint(getGridWrapper(),
        getDataModel(), causalEntryLink);
    ITableModel actionForUca = getDataModel().getControlActionForUca(uca.getId());
    if (actionForUca != null && !checkAccess(actionForUca.getId(), AccessRights.WRITE)) {
      cell.setReadOnly(true);
      cell.setShowDelete(false);
    }
    entryRow.addCell(4, cell);

    Optional<Link> safetyOption = Optional.ofNullable(getDataModel().getLinkController()
        .getLinkObjectFor(LinkingType.CausalEntryLink_SC2_LINK, causalEntryLink.getId()));

    IGridCell hintCell = new GridCellText("-");
    if (safetyOption.isPresent()) {
      hintCell = new CellEditorFactorNote(getGridWrapper(), getDataModel(), safetyOption.get());
      ((GridCellTextEditor) hintCell).setDefaultText("Design hint...");
    }
    // in column 5 the Design Hint cell for the CausalEntryLink_SC2_LINK is added
    entryRow.addCell(5, hintCell);
    return null;

  }

  private void addHazardCell(GridRow entryRow, ITableModel uca) {
    String hazString = "";
    for (UUID hazId : getDataModel().getLinkController().getLinksFor(LinkingType.UCA_HAZ_LINK,
        uca.getId())) {
      hazString += hazString.isEmpty() ? "" : ", ";
      hazString += getDataModel().getHazard(hazId).getIdString();
    }
    entryRow.setRowSpanningCells(new int[] { 2, 3 });
    entryRow.addCell(3, new GridCellText(hazString));
  }

  /**
   * 
   * @param entryRow
   *          the row in which the cells will be added
   * @param scenarioLink
   *          a Link of type {@link ObserverValue#CausalEntryLink_Scenario_LINK}
   * @param uca
   *          the {@link IUnsafeControlAction} to which this is linked
   * @return
   */
  private void createScenarioRow(GridRow entryRow, Link scenarioLink, IUnsafeControlAction uca) {
    addHazardCell(entryRow, uca);
    ScenarioType type = getDataModel().getExtendedDataController()
        .getScenarioType(scenarioLink.getLinkB());
    entryRow.addCell(4, new CellEditorCausalScenario(getGridWrapper(), getDataModel(), scenarioLink,
        scenarioLink.getLinkB(), type));
    entryRow.addCell(5, new CellEditorCausalScenarioConstraint(getGridWrapper(), getDataModel(),
        scenarioLink.getLinkB(), type));

    entryRow.addCell(6, new CellEditorFactorNote(getGridWrapper(), getDataModel(), scenarioLink));

  }

  @Override
  public void update(Observable dataModelController, Object updatedValue) {
    IDataModel controller = (IDataModel) dataModelController;
    if (controller.getProjectName().equals(getDataModel().getProjectName())) {
      super.update(dataModelController, updatedValue);
      switch ((ObserverValue) updatedValue) {
      case UNSAFE_CONTROL_ACTION: {
        List<ICorrespondingUnsafeControlAction> ucaList = this.getDataModel().getUCAList(null);
        String[] names = new String[ucaList.size()];
        UUID[] values = new UUID[ucaList.size()];
        for (int i = 0; i < ucaList.size(); i++) {
          names[i] = ucaList.get(i).getTitle();
          values[i] = ucaList.get(i).getId();
        }
        addChoices("UCA", names);
        addChoiceValues("UCA", values);
      }
      case CONTROL_STRUCTURE:
      case LINKING:
      case HAZARD:
      case Extended_DATA:
      case CAUSAL_FACTOR:
        reloadTable();
        break;
      default:
        break;
      }
    }
  }

}
