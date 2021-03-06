/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.anchorage.docks.node;

import com.anchorage.docks.containers.SingleDockContainer;
import com.anchorage.docks.containers.StageFloatable;
import static com.anchorage.docks.containers.common.AnchorageSettings.FLOATING_NODE_DROPSHADOW_RADIUS;
import com.anchorage.docks.node.ui.DockUIPanel;
import javafx.scene.layout.StackPane;

import com.anchorage.docks.containers.interfaces.DockContainableComponent;
import com.anchorage.docks.containers.interfaces.DockContainer;
import com.anchorage.docks.containers.subcontainers.DockTabberContainer;
import com.anchorage.docks.stations.DockStation;
import com.anchorage.docks.stations.DockSubStation;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;
import javafx.stage.Window;

/**
 *
 * @author Alessio
 */
public class DockNode extends StackPane implements DockContainableComponent {

    public enum DOCK_POSITION {

        LEFT,
        RIGHT,
        TOP,
        BOTTOM,
        CENTER
    }

    private DockUIPanel content;

    private DockContainer container;

    private DockStation station;

    private BooleanProperty floatableProperty;
    private BooleanProperty closeableProperty;
    private BooleanProperty resizableProperty;
    private BooleanProperty maximizableProperty;

    private ReadOnlyBooleanWrapper floatingProperty;
    private ReadOnlyBooleanWrapper draggingProperty;
    private ReadOnlyBooleanWrapper resizingProperty;
    private ReadOnlyBooleanWrapper maximizingProperty;

    private ReadOnlyObjectWrapper<DockContainer> containerProperty;

    private StageFloatable stageFloatable;

    private DockNode() {

        floatableProperty = new SimpleBooleanProperty(true);
        closeableProperty = new SimpleBooleanProperty(true);
        resizableProperty = new SimpleBooleanProperty(true);
        maximizableProperty = new SimpleBooleanProperty(true);

        floatingProperty = new ReadOnlyBooleanWrapper(false);
        draggingProperty = new ReadOnlyBooleanWrapper(false);
        resizingProperty = new ReadOnlyBooleanWrapper(false);
        maximizingProperty = new ReadOnlyBooleanWrapper(false);

        containerProperty = new ReadOnlyObjectWrapper<>(null);
    }

    public DockNode(DockUIPanel node) {

        this();

        this.content = node;

        buildUI(node);
    }

    public BooleanProperty floatableProperty() {
        return floatableProperty;
    }

    public BooleanProperty closeableProperty() {
        return closeableProperty;
    }

    public BooleanProperty resizableProperty() {
        return closeableProperty;
    }

    public BooleanProperty maximizableProperty() {
        return maximizableProperty;
    }

    public ReadOnlyBooleanProperty floatingProperty() {
        return floatingProperty.getReadOnlyProperty();
    }

    public ReadOnlyBooleanProperty draggingProperty() {
        return draggingProperty.getReadOnlyProperty();
    }

    public ReadOnlyBooleanProperty maximizingProperty() {
        return maximizingProperty.getReadOnlyProperty();
    }

    public ReadOnlyObjectProperty containerProperty() {
        return containerProperty.getReadOnlyProperty();
    }

    public void restore() {
        if (draggingProperty.get()) {
            closeFloatingStage();
        }
        disableDragging();
    }

    public boolean isDockVisible() {
        if (container == null || floatingProperty.get()) {
            return true;
        }
        else {
            return container.isDockVisible(this);
        }
    }

    private void closeFloatingStage() {

        if (stageFloatable != null) {
            stageFloatable.closeStage();
            stageFloatable = null;
        }

        floatingProperty.set(false);
    }

    public StageFloatable getFloatableStage() {
        return stageFloatable;
    }

    private void buildUI(DockUIPanel node) {
        getChildren().add(node);
        node.setDockNode(this);
    }

    public void moveFloatable(double x, double y) {
        if (!maximizingProperty.get()) {
            stageFloatable.move(x, y);
        }
    }

    public void makeGhostFloatable(Window owner, double x, double y) {

        if (!floatingProperty.get()) {

            stageFloatable = new StageFloatable(this, owner, x, y);
            stageFloatable.show();

            makeTransparent();
        }

    }

    public void enableDraggingOnPosition(double x, double y) {
        draggingProperty.set(true);

        makeGhostFloatable(station.getScene().getWindow(), x, y);

        if (!maximizingProperty().get()) {
            getStation().prepareZones(this);
        }
    }

    public void disableDragging() {
        draggingProperty.set(false);
        makeOpaque();
    }

    private void makeTransparent() {
        content.setOpacity(0.4);
    }

    private void makeOpaque() {
        content.setOpacity(1);
    }

    public void makeNodeActiveOnFloatableStage(Window owner, double x, double y) {

        disableDragging();

        if (!floatingProperty.get()) {

            if (floatableProperty.get()) {
                if (stageFloatable == null) {
                    makeGhostFloatable(owner, x, y);
                }
                stageFloatable.makeNodeActiveOnFloatableStage();

                floatingProperty.set(true);

            }
            else {
                restore();
            }
        }

    }

    /**
     * Get the value of station
     *
     * @return the value of station
     */
    public DockStation getStation() {
        return station;
    }

    /**
     * Set the value of station
     *
     * @param station new value of station
     */
    public void setStation(DockStation station) {
        this.station = station;
    }

    @Override
    public void setParentContainer(DockContainer container) {
        this.container = container;
        containerProperty.set(container);
    }

    @Override
    public DockContainer getParentContainer() {
        return container;
    }

    public void dockAsFloating(Window owner, DockStation station, double x, double y, double width, double height) {
        station.add(this);
        makeNodeActiveOnFloatableStage(owner, x, y);
        stageFloatable.setWidth(width);
        stageFloatable.setHeight(height);
        setStation((DockStation) station);
    }

    public void dock(DockStation station, DockNode.DOCK_POSITION position) {
        station.add(this);
        station.putDock(this, position);
        setStation((DockStation) station);
    }

    public void dock(DockNode nodeTarget, DockNode.DOCK_POSITION position) {

        nodeTarget.getStation().add(this);
        nodeTarget.getParentContainer().putDock(this, nodeTarget, position);
        setStation(nodeTarget.getStation());
    }

    public void dock(DockSubStation subStation, DockNode.DOCK_POSITION position) {

        subStation.putDock(this, position);
    }

    public void undock() {

        restore();

        if (getParentContainer() != null && !(getParentContainer() instanceof SingleDockContainer)) {
            getParentContainer().undock(this);
            station.remove(this);
        }
        else if (floatingProperty.get()) {
            closeFloatingStage();
            station.remove(this);
        }
    }

    public DockUIPanel getContent() {
        return content;
    }

    @Override
    public String toString() {
        return content.titleProperty().get();
    }

    public Bounds getSceneBounds() {
        return localToScene(getBoundsInLocal());
    }

    public Bounds getScreenBounds() {
        return localToScreen(getBoundsInLocal());
    }

    public boolean checkForTarget(double x, double y) {

        Point2D screenToScenePoint = getScene().getRoot().screenToLocal(x, y);
        Bounds sceneBounds = getSceneBounds();
        return sceneBounds.contains(screenToScenePoint.getX(), screenToScenePoint.getY());

    }

    public boolean insideTabContainer() {
        return container instanceof DockTabberContainer;
    }

    public void maximizeOrRestore() {
        if (maximizingProperty.get()) {
            restoreLayout();
        }
        else {
            maximizeLayout();
        }

    }

    public void restoreLayout() {
        if (floatingProperty.get()) {
            stageFloatable.setX(0);
            stageFloatable.setY(0);
            stageFloatable.setMaximized(false);
        }
        else {
            station.restore(this);
        }
        maximizingProperty.set(false);
    }

    public void maximizeLayout() {
        if (floatingProperty.get()) {

            stageFloatable.setMaximized(true);
        }
        else {
            station.maximize(this);
        }
        maximizingProperty.set(true);
    }
}
