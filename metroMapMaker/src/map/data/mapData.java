package map.data;

import java.util.ArrayList;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Effect;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Shape;
import map.gui.mapWorkspace;
import djf.components.AppDataComponent;
import djf.AppTemplate;
import javafx.collections.FXCollections;
import static javafx.scene.paint.Color.rgb;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import static map.data.mapState.*;
import javafx.scene.text.Text;
import jtps.jTPS;
import static map.data.Draggable.LINE;
import map.transactions.AddNode_Transaction;

/**
 * This class serves as the data management component for this application.
 *
 * @author Richard McKenna
 * @author ?
 * @version 1.0
 */
public class mapData implements AppDataComponent {
    // FIRST THE THINGS THAT HAVE TO BE SAVED TO FILES
    
    // THESE ARE THE NODES IN THE MAP
    ObservableList<Node> mapNodes;
    ObservableList<Line> gridLines;
    Boolean hasGridLines = false;
    Boolean showingGridlines = false;
        
    // THIS IS THE SHAPE CURRENTLY BEING SIZED BUT NOT YET ADDED
    Node newShape;

    // THIS IS THE NODE CURRENTLY SELECTED
    Node selectedNode;

    // CURRENT STATE OF THE APP
    mapState state;

    // THIS IS A SHARED REFERENCE TO THE APPLICATION
    AppTemplate app;
    
    mapWorkspace mapWorkspace;
    
    // USE THIS WHEN THE NODE IS SELECTED
    Effect highlightedEffect;

    public static final String WHITE_HEX = "#FFFFFF";
    public static final String BLACK_HEX = "#000000";
    public static final Paint DEFAULT_BACKGROUND_COLOR = Paint.valueOf(WHITE_HEX);
    public static final Color HIGHLIGHTED_COLOR = Color.valueOf("#93CEFF");
    public static final int HIGHLIGHTED_STROKE_THICKNESS = 10;

    /**
     * THis constructor creates the data manager and sets up the
     *
     *
     * @param initApp The application within which this data manager is serving.
     */
    public mapData(AppTemplate initApp) {
	// KEEP THE APP FOR LATER
	app = initApp;
        
	// NO SHAPE STARTS OUT AS SELECTED
	newShape = null;
	selectedNode = null;

	// THIS IS FOR THE SELECTED SHAPE
	DropShadow dropShadowEffect = new DropShadow();
	dropShadowEffect.setSpread(0.9);
	dropShadowEffect.setColor(HIGHLIGHTED_COLOR);
	dropShadowEffect.setBlurType(BlurType.GAUSSIAN);
	dropShadowEffect.setRadius(HIGHLIGHTED_STROKE_THICKNESS);
	highlightedEffect = dropShadowEffect;
    }
    
    public ObservableList<Node> getMapNodes() {
	return mapNodes;
    }
    
    public void setMapNodes(ObservableList<Node> initLogoNodes) {
	mapNodes = initLogoNodes;
    }
    
    public void removeSelectedNode() {
	if (selectedNode != null) {
	    mapNodes.remove(selectedNode);
	    selectedNode = null;
	}
    }
 
    /**
     * This function clears out the HTML tree and reloads it with the minimal
     * tags, like html, head, and body such that the user can begin editing a
     * page.
     */
    @Override
    public void resetData() {
	setState(SELECTING_NODE);
	newShape = null;
	selectedNode = null;
	
	mapNodes.clear();
	((mapWorkspace)app.getWorkspaceComponent()).getCanvas().getChildren().clear();
        ((mapWorkspace)app.getWorkspaceComponent()).initDebugText();
        ((mapWorkspace)app.getWorkspaceComponent()).getListOfLines().clear();
        ((mapWorkspace)app.getWorkspaceComponent()).getListOfStations().clear();
        ((mapWorkspace)app.getWorkspaceComponent()).getListOfLineNames().clear();
        ((mapWorkspace)app.getWorkspaceComponent()).getListOfStationNames().clear();
    }
    
    public Color getBackgroundColor() {
        return (Color)((mapWorkspace)app.getWorkspaceComponent()).getCanvas().getBackground().getFills().get(0).getFill();
    }
    
    public void setBackgroundColor(Color color) {
        Pane canvas = ((mapWorkspace)app.getWorkspaceComponent()).getCanvas();        
        BackgroundFill fill = new BackgroundFill(color, null, null);
	Background background = new Background(fill);
        canvas.setBackground(background);
    }

    public void selectSizedShape() {
	if (selectedNode != null)
	    unhighlightNode(selectedNode);
	selectedNode = newShape;
	highlightNode(selectedNode);
	newShape = null;
    }
    
    public void unhighlightNode(Node node) {
	node.setEffect(null);
    }
    
    public void highlightNode(Node node) {
	node.setEffect(highlightedEffect);
    }
    
    public void startNewLabel(int x, int y, String text){
        DraggableText label = new DraggableText(text);
        Font font = ((mapWorkspace) app.getWorkspaceComponent()).getCurrentFontSettings();
        label.start(x, y);
        newShape = label;
        label.setFont(font);
        initNewShape();
    }

    public void startNewStation(int x, int y, String name) {
        DraggableText label = new DraggableText(name);
        label.start(x, y);
        label.setIsForStation(true);
        newShape = label;
        initNewShape();
        
	MetroStation newStation = new MetroStation(label.getX(), label.getY(), 10);
        label.setX(label.getX() + 15);
        label.setY(label.getY() + label.getHeight() / 2);
        newStation.setFill(rgb(255,255,255));
        newStation.setStroke(rgb(0,0,0));
        newStation.setAssociatedLabel(label);
        label.setAssociatedStation(newStation);
	newShape = newStation;
	initNewShape();
        
        mapWorkspace = (mapWorkspace)app.getWorkspaceComponent();
        mapWorkspace.addStationToList(newStation);
    }
    
    public void startNewLine(int x1, int y1, int x2, int y2, String name, Color color){
        DraggableText start = new DraggableText(name);
        start.setIsForLine(true);
        start.setIsStart(true);
        start.start(x1, y1);
        newShape = start;
        initNewShape();
        DraggableText end = new DraggableText(name);
        end.setIsForLine(true);
        end.start(x2, y2);
        newShape = end;
        initNewShape();
        
        MetroLine line = new MetroLine(5, color);
        line.getPoints().addAll(new Double[]{start.getX(), start.getY(), end.getX(), end.getY()});
        start.setAssociatedLine(line);
        end.setAssociatedLine(line);
        line.setAssociatedStartLabel(start);
        line.setAssociatedEndLabel(end);
        newShape = line;
        initNewShape();
        
        mapWorkspace = (mapWorkspace)app.getWorkspaceComponent();
        mapWorkspace.addLineToList(line);
    }

    public void initNewShape() {
	// DESELECT THE SELECTED SHAPE IF THERE IS ONE
	if (selectedNode != null) {
	    unhighlightNode(selectedNode);
	    selectedNode = null;
	}

	// USE THE CURRENT SETTINGS FOR THIS NEW SHAPE
	mapWorkspace workspace = (mapWorkspace)app.getWorkspaceComponent();
//	newShape.setFill(workspace.getFillColorPicker().getValue());
//	newShape.setStroke(workspace.getOutlineColorPicker().getValue());
//	newShape.setStrokeWidth(workspace.getOutlineThicknessSlider().getValue());
	
	// GO INTO SHAPE SIZING MODE
	state = mapState.SELECTING_NODE;
	
	// FINALLY, ADD A TRANSACTION FOR ADDING THE NEW SHAPE
        jTPS tps = app.getTPS();
        mapData data = (mapData)app.getDataComponent();
        AddNode_Transaction newTransaction = new AddNode_Transaction(data, newShape);
        tps.addTransaction(newTransaction);
    }

    public Node getNewShape() {
	return newShape;
    }

    public Node getSelectedNode() {
	return selectedNode;
    }

    public void setSelectedNode(Node initSelectedNode) {
	selectedNode = initSelectedNode;
    }

    public Node selectTopNode(int x, int y) {
	Node node = getTopNode(x, y);
	if (node == selectedNode)
	    return node;
	
	if (selectedNode != null) {
	    unhighlightNode(selectedNode);
	}
        if (node != null) {
            highlightNode(node);
            mapWorkspace workspace = (mapWorkspace) app.getWorkspaceComponent();
            workspace.loadSelectedNodeSettings(node);
        }
        selectedNode = node;
        if (node != null && !(node instanceof MetroLine)) {
            ((Draggable) node).setStart(x, y);
        }
	return node;
    }
    
    public boolean isShape(Draggable node) {
        return ((node.getNodeType() == Draggable.LINE) 
                || (node.getNodeType() == Draggable.STATION)
                || (node.getNodeType() == Draggable.TEXT));
    }
    
    public Draggable getSelectedDraggableNode() {
        if (selectedNode == null)
            return null;
        else
            return (Draggable)selectedNode;
    }

    public Node getTopNode(int x, int y) {
	for (int i = mapNodes.size() - 1; i >= 0; i--) {
	    Node node = (Node)mapNodes.get(i);
	    if (node.contains(x, y)) {
		return node;
	    }
	}
	return null;
    }

    public mapState getState() {
	return state;
    }

    public void setState(mapState initState) {
	state = initState;
    }

    public boolean isInState(mapState testState) {
	return state == testState;
    }
    
    public void removeNode(Node nodeToRemove) {
        int currentIndex = mapNodes.indexOf(nodeToRemove);
        if (currentIndex >= 0) {
	    mapNodes.remove(currentIndex);
        }
    }    
    
    public void addNode(Node nodeToAdd) {
        int currentIndex = mapNodes.indexOf(nodeToAdd);
        if (currentIndex < 0) {
	    mapNodes.add(nodeToAdd);
        }
        for (int i = 0; i < mapNodes.size(); i++){
            if (mapNodes.get(i) instanceof MetroStation){
                mapNodes.add(mapNodes.remove(i));
            }
        }
    }

    public int getIndexOfNode(Node node) {
        return mapNodes.indexOf(node);
    }

    public void addNodeAtIndex(Node node, int nodeIndex) {
        mapNodes.add(nodeIndex, node);    
    }

    public boolean isTextSelected() {
        if (selectedNode == null)
            return false;
        else
            return (selectedNode instanceof DraggableText);
    }
    
    public void showGridLines(){
        gridLines = FXCollections.observableArrayList();
        Pane canvas = ((mapWorkspace) app.getWorkspaceComponent()).getCanvas();
        for (int i = 30; i < canvas.getMaxHeight(); i = i + 30){
            Line line = new Line(0, i, canvas.getMaxWidth(), i);
            gridLines.add(line);
        }
        for (int i = 30; i < canvas.getMaxWidth(); i = i + 30){
            Line line = new Line(i, 0, i, canvas.getMaxHeight());
            gridLines.add(line);
        }
        mapNodes.addAll(0, gridLines);
        hasGridLines = true;
        showingGridlines = true;
    }
    
    public void hideGridLines(){
        if (hasGridLines){
            mapNodes.removeAll(gridLines);
            showingGridlines = false;
        }
    }
    
    public void updateGridLines(){
        if (hasGridLines && showingGridlines){
            hideGridLines();
            showGridLines();
        }
    }
    
}
