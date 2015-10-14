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

package acast.model.controlstructure;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import org.eclipse.draw2d.geometry.Rectangle;

import acast.controlstructure.CSAbstractEditor;
import acast.model.causalfactor.ICausalComponent;
import acast.model.controlstructure.components.Anchor;
import acast.model.controlstructure.components.CSConnection;
import acast.model.controlstructure.components.Component;
import acast.model.controlstructure.components.ComponentType;
import acast.model.controlstructure.components.ConnectionType;
import acast.model.controlstructure.interfaces.IConnection;
import acast.model.controlstructure.interfaces.IRectangleComponent;
import acast.ui.accidentDescription.Recommendation;

/**
 * Controller-class for working with the control structure diagram
 * 
 * @author Fabian Toth
 * 
 */
public class ControlStructureController {

	@XmlElement(name = "component")
	private Component root;

	@XmlElementWrapper(name = "connections")
	@XmlElement(name = "connection")
	private List<CSConnection> connections;
	private final Map<UUID, IRectangleComponent> componentTrash;
	private final Map<UUID, Integer> componentIndexTrash;
	private final Map<UUID, IConnection> connectionTrash;
	private final Map<UUID, List<UUID>> removedLinks;
	private boolean initiateStep1;
	private boolean initiateStep2;

	/**
	 * Constructor of the control structure controller
	 * 
	 * @author Fabian Toth
	 */
	public ControlStructureController() {
		connections = new ArrayList<>();
		componentIndexTrash = new HashMap<>();
		componentTrash = new HashMap<>();
		connectionTrash = new HashMap<>();
		removedLinks = new HashMap<>();
		setRoot(new Rectangle(), new String());
		initiateStep1 = false;
		initiateStep2 = false;
	}

	/**
	 * Adds a new component to a parent with the given values.
	 * 
	 * @param parentId
	 *            the id of the parent
	 * @param layout
	 *            the layout of the new component
	 * @param text
	 *            the text of the new component
	 * @param type
	 *            the type of the new component
	 * @param index
	 *            TODO
	 * @return the id of the created component. Null if the component could not
	 *         be added
	 * 
	 * @author Fabian Toth
	 */
	public UUID addComponent(UUID parentId, Rectangle layout, String text, ComponentType type, Integer index) {
		Component newComp = new Component(text, layout, type);
		Component parent = getInternalComponent(parentId);
		parent.addChild(newComp, index);
		return newComp.getId();
	}

	/**
	 * Adds a new component to a parent with the given values.
	 * 
	 * @param controlActionId
	 *            an id of a ControlAction
	 * @param parentId
	 *            the id of the parent
	 * @param layout
	 *            the layout of the new component
	 * @param text
	 *            the text of the new component
	 * @param type
	 *            the type of the new component
	 * @param index
	 *            TODO
	 * 
	 * @return the id of the created component. Null if the component could not
	 *         be added
	 * @author Fabian Toth,Lukas Balzer
	 */
	public UUID addComponent(UUID controlActionId, UUID parentId, Rectangle layout, String text, ComponentType type,
			Integer index) {
		Component newComp = new Component(controlActionId, text, layout, type);
		Component parent = getInternalComponent(parentId);
		parent.addChild(newComp, index);
		if (TableView.visible) {
			TableView.update(text);
		}
		return newComp.getId();
	}

	/**
	 * Creates a new root with the given values.
	 * 
	 * @param layout
	 *            the layout of the new component
	 * @param text
	 *            the text of the new component
	 * @return the id of the created component. Null if the component could not
	 *         be added
	 * 
	 * @author Fabian Toth
	 */
	public UUID setRoot(Rectangle layout, String text) {
		Component newComp = new Component(text, layout, ComponentType.ROOT);
		root = newComp;
		return newComp.getId();
	}

	/**
	 * Searches for the component with the given id and changes the layout of it
	 * 
	 * @param componentId
	 *            the id of the component
	 * @param layout
	 *            the new text
	 * @param step1
	 *            if the layout of step 1 should be changed
	 * @return true if the text could be changed
	 * 
	 * @author Fabian Toth
	 * 
	 */
	public boolean changeComponentLayout(UUID componentId, Rectangle layout, boolean step1) {
		Component component = getInternalComponent(componentId);
		if (component != null) {
			// every time the layout is changed the controller checks if both
			// steps have been initialized and if not synchronizes the two
			// layouts
			if (step1 && initiateStep1) {
				initiateStep2 = true;
				component.setLayout(layout, false);
			} else if (step1 && !initiateStep1) {
				initiateStep2 = false;
			} else if (!step1 && initiateStep2) {
				initiateStep1 = false;
			}
			component.setLayout(layout, step1);
			return true;
		}
		return false;
	}

	/**
	 * Searches for the component with the given id and changes the text of it
	 * 
	 * @param componentId
	 *            the id of the component
	 * @param text
	 *            the new text
	 * @return true if the text could be changed
	 * 
	 * @author Fabian Toth
	 */
	public boolean changeComponentText(UUID componentId, String text) {
		Component component = getInternalComponent(componentId);
		if (component != null) {
			TableView.changeNameOfComboItem(text);
			CSAbstractEditor.identifiers.remove(componentId);
			CSAbstractEditor.identifiers.put(text, componentId);
			component.setText(text);
			return true;
		}
		return false;
	}

	/**
	 * Searches recursively for the component with the given id and removes it
	 * 
	 * @param componentId
	 *            the id of the component to delete
	 * @return true if this controller contained the specified element
	 * 
	 * @author Fabian Toth
	 */
	public boolean removeComponent(UUID componentId) {
		Component component = getInternalComponent(componentId);
		removeAllLinks(componentId);
		componentTrash.put(componentId, component);
		componentIndexTrash.put(componentId, root.getChildren().indexOf(component));
		CSAbstractEditor.identifiers.remove(component.getText());

		if (TableView.visible) {
			TableView.refresh(component.getText(), true);
			TableView.viewer.getTable().removeAll();
		}
		return root.removeChild(componentId);
	}

	/**
	 * This methode recovers a Component which was deleted before, from the
	 * componentTrash
	 * 
	 * @author Lukas Balzer
	 * 
	 * @param parentId
	 *            the id of the parent
	 * @param componentId
	 *            the id of the component to recover
	 * @return whether the component could be recoverd or not
	 */
	public boolean recoverComponent(UUID parentId, UUID componentId) {
		if (componentTrash.containsKey(componentId)) {
			Component parent = getInternalComponent(parentId);
			boolean success = parent.addChild((Component) componentTrash.get(componentId),
					componentIndexTrash.get(componentId));
			componentTrash.remove(componentId);
			if (removedLinks.containsKey(componentId)) {
				for (UUID connectionId : removedLinks.get(componentId)) {
					recoverConnection(connectionId);
				}
			}
			return success;
		}

		return false;

	}

	/**
	 * Searches recursively for the component with the given id
	 * 
	 * @param componentId
	 *            the id of the child
	 * @return the component with the given id
	 * 
	 * @author Fabian Toth
	 */
	public IRectangleComponent getComponent(UUID componentId) {
		if (root == null) {
			return null;
		}
		return root.getChild(componentId);
	}

	/**
	 * Gets all components of the root level
	 * 
	 * @return the the components
	 * 
	 * @author Fabian Toth
	 */
	public IRectangleComponent getRoot() {
		return root;
	}

	/**
	 * Adds a new connection with the given values
	 * 
	 * @param sourceAnchor
	 *            the anchor at the source component
	 * @param targetAnchor
	 *            the anchor at the target component
	 * @param connectionType
	 *            the type of the connection
	 * @return the id of the new connection
	 * 
	 * @author Fabian Toth
	 */
	public UUID addConnection(Anchor sourceAnchor, Anchor targetAnchor, ConnectionType connectionType) {
		CSConnection newConn = new CSConnection(sourceAnchor, targetAnchor, connectionType);
		connections.add(newConn);
		return newConn.getId();
	}

	/**
	 * Searches for the connection with the given id and changes the connection
	 * type to the new value
	 * 
	 * @param connectionId
	 *            the id of the connection to change
	 * @param connectionType
	 *            the new connection type
	 * @return true if the connection type could be changed
	 * 
	 * @author Fabian Toth
	 */
	public boolean changeConnectionType(UUID connectionId, ConnectionType connectionType) {
		IConnection connection = getConnection(connectionId);
		if (connection != null) {
			((CSConnection) connection).setConnectionType(connectionType);
			return true;
		}
		return false;
	}

	/**
	 * Searches for the connection with the given id and changes the targetId to
	 * the new value
	 * 
	 * @param connectionId
	 *            the id of the connection to change
	 * @param targetAnchor
	 *            the new source anchor
	 * @return true if the targetId could be changed
	 * 
	 * @author Fabian Toth
	 */
	public boolean changeConnectionTarget(UUID connectionId, Anchor targetAnchor) {
		IConnection connection = getConnection(connectionId);
		if (connection != null) {
			((CSConnection) connection).setTargetAnchor(targetAnchor);
			return true;
		}
		return false;
	}

	/**
	 * Searches for the connection with the given id and changes the sourceId to
	 * the new value
	 * 
	 * @param connectionId
	 *            the id of the connection to change
	 * @param sourceAnchor
	 *            the new source anchor
	 * @return true if the sourceId could be changed
	 * 
	 * @author Fabian Toth
	 */
	public boolean changeConnectionSource(UUID connectionId, Anchor sourceAnchor) {
		IConnection connection = getConnection(connectionId);
		if (connection != null) {
			((CSConnection) connection).setSourceAnchor(sourceAnchor);
			return true;
		}
		return false;
	}

	/**
	 * Deletes the connection with the given id
	 * 
	 * @param connectionId
	 *            the id of the connection
	 * @return true if this component contained the specified element
	 * 
	 * @author Fabian Toth
	 */
	public boolean removeConnection(UUID connectionId) {
		IConnection connection = getConnection(connectionId);
		if (connections.remove(connection)) {
			connectionTrash.put(connectionId, connection);
			return true;
		}
		return false;
	}

	/**
	 * This methode recovers a Connection which was deleted before, from the
	 * connectionTrash
	 * 
	 * @author Lukas Balzer
	 * 
	 * @param connectionId
	 *            the id of the component to recover
	 * @return whether the Connection could be recovered or not
	 */
	public boolean recoverConnection(UUID connectionId) {
		if (connectionTrash.containsKey(connectionId)) {
			boolean success = connections.add((CSConnection) connectionTrash.get(connectionId));
			connectionTrash.remove(connectionId);
			return success;
		}
		return false;

	}

	/**
	 * Gets the connection with the given id
	 * 
	 * @param connectionId
	 *            the id of the connection
	 * @return the connection with the given id
	 * 
	 * @author Fabian Toth
	 */
	public IConnection getConnection(UUID connectionId) {
		for (IConnection connection : connections) {
			if (connection.getId().equals(connectionId)) {
				return connection;
			}
		}
		return null;
	}

	/**
	 * Searches recursively for the internal component with the given id
	 * 
	 * @param componentId
	 *            the id of the child
	 * @return the component with the given id
	 * 
	 * @author Fabian Toth
	 */
	private Component getInternalComponent(UUID componentId) {
		if (root == null) {
			return null;
		}
		return root.getChild(componentId);
	}

	/**
	 * Removes all links that are connected to the component with the given id
	 * 
	 * @author Fabian Toth,Lukas Balzer
	 * 
	 * @param componentId
	 *            the id of the component
	 * @return true if the connections have been deleted
	 */
	private boolean removeAllLinks(UUID componentId) {
		List<IConnection> connectionList = new ArrayList<>();
		removedLinks.put(componentId, new ArrayList<UUID>());
		for (CSConnection connection : connections) {
			if (connection.connectsComponent(componentId)) {
				UUID tmpID = connection.getId();
				connectionList.add(connection);
				connectionTrash.put(tmpID, connection);
				removedLinks.get(componentId).add(tmpID);
			}
		}
		return connections.removeAll(connectionList);
	}

	/**
	 * Gets all connections of the control structure diagram
	 * 
	 * @author Fabian Toth
	 * 
	 * @return all connections
	 */
	public List<IConnection> getConnections() {
		List<IConnection> result = new ArrayList<>();
		for (CSConnection connection : connections) {
			result.add(connection);
		}
		return result;
	}

	/**
	 * Get all causal components
	 * 
	 * @author Fabian Toth
	 * 
	 * @return all causal components
	 */
	public List<ICausalComponent> getCausalComponents() {
		List<ICausalComponent> result = new ArrayList<>();
		if (root == null) {
			return result;
		}

		for (Component component : root.getInternalChildren()) {
				result.add(component);
		}
		return result;
	}

	/**
	 * Gets all components of an internal type. Do not use outside the data
	 * model.
	 * 
	 * @author Fabian Toth
	 * 
	 * @return all components
	 */
	public List<Component> getInternalComponents() {
		if (root == null) {
			return new ArrayList<Component>();
		}
		return root.getInternalChildren();
	}

	/**
	 * Overwrites the layout of step3 with the layout of step1
	 * 
	 * @author Lukas Balzer
	 * 
	 * @param id
	 *            the id of the component
	 * @return true, if the layout has been synchronized
	 */
	public boolean sychronizeLayout(UUID id) {
		return root.getChild(id).sychronizeLayout();
	}

	/**
	 * 
	 * @author Lukas Balzer
	 * 
	 * @return the amount of components currently in the trash
	 */
	public int getComponentTrashSize() {
		return componentTrash.size();
	}

	/**
	 * 
	 * @author Lukas Balzer
	 * 
	 * @return the amount of components currently in the trash
	 */
	public int getConnectionTrashSize() {
		return connectionTrash.size();
	}

	/**
	 * is called the first time the cs is opened sets a boolean which indicates
	 * that the 1. step must be initialized
	 *
	 * @author Lukas Balzer
	 *
	 */
	public void initializeCSS() {
		initiateStep1 = true;
	}

	public void addResponsibility(UUID ident, String id, String description) {
		if (root.getChild(ident) != null) {
			root.getChild(ident).addResponsibility(id, description);
		}
	}

	public void removeResponsibility(UUID ident, String id) {
		if (root.getChild(ident) != null) {
			root.getChild(ident).removeResponsibility(id);
		}
	}

	public void updateResponsibility(UUID ident, String id, String newId) {
		if (root.getChild(ident) != null) {
			root.getChild(ident).updateResponsibility(id, newId);
		}
	}

	public void changeResponsibility(UUID ident, String id, String description) {
		if (root.getChild(ident) != null) {
			root.getChild(ident).changeResponsibility(id, description);
		}
	}

	public List<Responsibility> getResponsibilitiesList(UUID ident) {
		if (root.getChild(ident) == (null) || root.getChild(ident).getResponsibilitiesList() == null) {
			return new ArrayList<Responsibility>();
		} else {
			return root.getChild(ident).getResponsibilitiesList();
		}
	}

	public Responsibility getResponsibility(UUID ident, String id) {
		if (root.getChild(ident) != null) {
			return root.getChild(ident).getResponsibility(id);
		}
		return null;
	}

	public void addFlaw(UUID ident, String id, String description) {
		if (root.getChild(ident) != null) {
			root.getChild(ident).addFlaw(id, description);
		}
	}

	public void removeFlaw(UUID ident, String id) {
		if (root.getChild(ident) != null) {
			root.getChild(ident).removeFlaw(id);
		}
	}

	public void updateFlaw(UUID ident, String id, String newId) {
		if (root.getChild(ident) != null) {
			root.getChild(ident).updateFlaw(id, newId);
		}
	}

	public void changeFlaw(UUID ident, String id, String description) {
		if (root.getChild(ident) != null) {
			root.getChild(ident).changeFlaw(id, description);
		}
	}

	public List<Responsibility> getFlawsList(UUID ident) {

		if (root.getChild(ident) == (null) || root.getChild(ident).getFlawsList() == null) {
			return new ArrayList<Responsibility>();
		} else {
			return root.getChild(ident).getFlawsList();
		}
	}

	public Responsibility getFlaw(UUID ident, String id) {
		if (root.getChild(ident) != null) {
			return root.getChild(ident).getFlaw(id);
		}
		return null;
	}

	public void addContext(UUID ident, String id, String description) {
		if (root.getChild(ident) != null) {
			root.getChild(ident).addContext(id, description);
		}
	}

	public void updateContext(UUID ident, String id, String newId) {
		if (root.getChild(ident) != null) {
			root.getChild(ident).updateContext(id, newId);
		}
	}

	public void removeContext(UUID ident, String id) {
		if (root.getChild(ident) != null) {
			root.getChild(ident).removeContext(id);
		}
	}

	public void changeContext(UUID ident, String id, String description) {
		if (root.getChild(ident) != null) {
			root.getChild(ident).changeContext(id, description);
		}
	}

	public List<Responsibility> getContextList(UUID ident) {
		if (root.getChild(ident) == (null) || root.getChild(ident).getContextList() == null) {
			return new ArrayList<Responsibility>();
		} else {
			return root.getChild(ident).getContextList();
		}
	}

	public Responsibility getContext(UUID ident, String id) {
		if (root.getChild(ident) != null) {
			return root.getChild(ident).getContext(id);
		}
		return null;
	}

	public void addUnsafeAction(UUID ident, String id, String description) {
		if (root.getChild(ident) != null) {
			root.getChild(ident).addUnsafeAction(id, description);
		}
	}

	public void updateUnsafeAction(UUID ident, String id, String newId) {
		if (root.getChild(ident) != null) {
			root.getChild(ident).updateUnsafeAction(id, newId);
		}
	}

	public void removeUnsafeAction(UUID ident, String id) {
		if (root.getChild(ident) != null) {
			root.getChild(ident).removeUnsafeAction(id);
		}
	}

	public void changeUnsafeAction(UUID ident, String id, String description) {
		if (root.getChild(ident) != null) {
			root.getChild(ident).changeUnsafeAction(id, description);
		}
	}

	public List<Responsibility> getUnsafeActionsList(UUID ident) {
		if (root.getChild(ident) == (null) || root.getChild(ident).getUnsafeActionsList() == null) {
			return new ArrayList<Responsibility>();
		} else {
			return root.getChild(ident).getUnsafeActionsList();
		}
	}

	public Responsibility getUnsafeActions(UUID ident, String id) {
		if (root.getChild(ident) != null) {
			return root.getChild(ident).getUnsafeActions(id);
		}
		return null;
	}

	public void addRecommendation(UUID ident, String id, String description) {
		if (root.getChild(ident) != null) {
			root.getChild(ident).addRecommendation(id, description);
		}
	}

	public void removeRecommendation(UUID ident, String id) {
		if (root.getChild(ident) != null) {
			root.getChild(ident).removeRecommendation(id);
		}
	}

	public void updateRecommendation(UUID ident, String id, String newId) {
		if (root.getChild(ident) != null) {
			root.getChild(ident).updateRecommendation(id, newId);
		}
	}

	public void changeRecommendation(UUID ident, String id, String description) {
		if (root.getChild(ident) != null) {
			root.getChild(ident).changeRecommendation(id, description);
		}
	}

	public List<Recommendation> getRecommendationList(UUID ident) {
		if (root.getChild(ident) == (null) || root.getChild(ident).getRecommendationList() == null) {
			return new ArrayList<Recommendation>();
		} else {
			return root.getChild(ident).getRecommendationList();
		}
	}

	public Recommendation getRecommendation(String id, UUID ident) {
		if (root.getChild(ident) != null) {
			return root.getChild(ident).getRecommendation(id);
		}
		return null;
	}

	/**
	 * this funktion
	 * 
	 * @param componentId
	 *            the id of the component
	 * @return the relative of the component which belongs to the given id
	 */
	public UUID getRelativeOfComponent(UUID componentId) {
		return getInternalComponent(componentId).getRelative();
	}

	/**
	 * @param componentId
	 *            the id of the component
	 * @param relativeId
	 *            the relative to set
	 */
	public void setRelativeOfComponent(UUID componentId, UUID relativeId) {
		Component comp = getInternalComponent(componentId);
		if (comp != null) {
			comp.setRelative(relativeId);
		}
	}

	/**
	 * @param componentId
	 *            the id of the component
	 * @param isSafetyCritical
	 *            the isSafetyCritical to set
	 */
	public void setSafetyCritical(UUID componentId, boolean isSafetyCritical) {
		Component comp = getInternalComponent(componentId);
		if (comp != null) {
			comp.setSafetyCritical(isSafetyCritical);
		}
	}

	/**
	 * @param componentId
	 *            the id of the component
	 * @param comment
	 *            the comment to set
	 */
	public void setComment(UUID componentId, String comment) {
		Component comp = getInternalComponent(componentId);
		if (comp != null) {
			comp.setComment(comment);
		}
	}

	/**
	 *
	 * @author Lukas
	 *
	 * @param componentId
	 *            the id of the component
	 * @param variableID
	 *            the variable which should be rmoved
	 * @return whether or not the add was successful, it also returns false if
	 *         the given uuid belongs to no component
	 */
	public boolean addUnsafeProcessVariable(UUID componentId, UUID variableID) {
		Component comp = getInternalComponent(componentId);
		if (comp != null) {
			return comp.addUnsafeProcessVariable(variableID);
		}
		return false;
	}

	/**
	 *
	 * @author Lukas
	 *
	 * @param componentId
	 *            the id of the component
	 * @param variableID
	 *            the variable which should be rmoved
	 * @return whether or not the remove was successful, it also returns false
	 *         if the given uuid belongs to no component
	 */
	public boolean removeUnsafeProcessVariable(UUID componentId, UUID variableID) {
		Component comp = getInternalComponent(componentId);
		if (comp != null) {
			comp.removeUnsafeProcessVariable(variableID);
		}
		return false;
	}

	/**
	 *
	 * @author Lukas
	 *
	 * @param componentId
	 *            the id of the component
	 * @return a map cointaining all process variables provided as keys to a
	 *         safe/unsafe boolean
	 */
	public Map<IRectangleComponent, Boolean> getRelatedProcessVariables(UUID componentId) {
		Component comp = getInternalComponent(componentId);
		Map<IRectangleComponent, Boolean> values = new HashMap<>();
		if (comp != null) {
			List<UUID> upv = comp.getUnsafeProcessVariables();
			IConnection conn = getConnection(comp.getRelative());
			Component target = getInternalComponent(conn.getTargetAnchor().getOwnerId());
			if (target == null || target.getComponentType() != ComponentType.CONTROLLER) {
				return values;
			}
			for (IRectangleComponent child : target.getChildren()) {
				if (child.getComponentType() == ComponentType.PROCESS_MODEL) {
					for (IRectangleComponent variable : child.getChildren()) {
						if (variable.getComponentType() == ComponentType.PROCESS_VARIABLE) {
							values.put(variable, upv.contains(variable.getId()));
						}
					}
				}
			}
		}
		return values;
	}

}