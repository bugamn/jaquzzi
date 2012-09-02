//  jaQuzzi 0.1 - Interactive Quantum Computer Simulator    
//  Copyright (C) 2000  Felix Schuermann
//
//  This program is free software; you can redistribute it and/or modify
//  it under the terms of the GNU General Public License as published by
//  the Free Software Foundation; either version 2 of the License, or
//  any later version.
//
//  This program is distributed in the hope that it will be useful,
//  but WITHOUT ANY WARRANTY; without even the implied warranty of
//  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//  GNU General Public License for more details.
//
//  You should have received a copy of the GNU General Public License
//  along with this program; if not, write to the Free Software
//  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
//
//  The author can be reached under: fschuermann@usa.net
//  
//  A full copy of the source can be obtained from: 
//  www.physics.buffalo.edu/~phygons/jaQuzzi

package qsymbol2;

import java.io.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.print.*;
import java.beans.*;
import javax.swing.*;
import javax.swing.table.*;

import mathlib.Complex;
import mathlib.GateProperty;
import mathlib.MathObject;
import mathlib.MathObjectConstraints;
import mathlib.Mathlib;
import mathlib.MathlibEvent;
import mathlib.MathlibEventListener;
import mathlib.Parse;
import mathlib.Timing;
import mathlib.VarAuthority;

/**
 * This class is the most important class for organizing a quantum circuit. Since for
 * a quantum circuit the number of output qubits matches the number of
 * input qubits (ciruits are reversible), an adequate class to represent a quantum
 * circuit is a table. The columns are the quantum gates and the rows represent the
 * qubit lines.
 * <p>The class GateTable is derived from a JTable class and is organized in a
 * similar fashion. That is, it has a TableModel (provided by the GateTableModel
 * class) and a ColumnModel (provided by the GateTableColumnModel). The
 * GateTableModel provides the data for each column, whereas the GateTableColumnModel
 * holds a TableColumn object for each column, which is responsible for the graphical
 * representation of the column. The GateTableModel does not hold objects of the Gate
 * class, but rather objects of the GateContainer class, which wraps the mathlib Gate
 * class. This is done to provide grouping of several gates into one gate group. A
 * gate group is treated as a single column of the GateTableModel and gates within a
 * group therefore cannot be changed unless ungrouped.</p>
 * <p>The separation between the GateTableModel and the GateTableColumnModel is
 * necessary because in a JTable columns can be interactively moved. For such an
 * action the data in the GateTableModel is not changed, only the order in the
 * GateTableColumnModel changes. In effect, only the GateTable knows
 * the correct order of the gates and therefore has to implement the StepHandler
 * interface to provide a stepping mechanism for the quantum circuit. Since the program
 * can run in batch mode (with no graphic output) and JAVA requires a console for 
 * instantiating a SWING object, the GateTableModel implements a stepHandler as well.
 * The GateTableModel can run a circuit autonomously.</p>
 * <p>The GateTable class implements the MathlibEventListener interface and reacts to
 * MathlibEvents. E.g., if the event is to add a Gate with subcategory "current
 * circuit", the GateTable class informs the GateTableColumnModel as well as the
 * GateTableModel to provide the corresponding column information.
 * </p>
 * <p>The rendering of the columns is done by the QubitRenderer class.
 * The column headers are rendered by the
 * QubitHeaderRenderer class.</p>
 * <p>Right now columns and rows are treated a little bit differently in the GateTable. Whereas
 * the names of the columns are generated from the variable name of the contained gates, qubits
 * have a StringArgument variable that holds the name. Therefore it is possible to use special 
 * characters for the naming of qubits, which are not allowed for the naming of gates. Finally,
 * this restriction should be taken away with introducing a stringArgument variable for each
 * gate.</p>
 * @see GateTableModel
 * @see GateTableColumnModel
 * @see QubitRenderer
 * @see QubitHeaderRenderer
 * @see QubitEditor
 */
public class GateTable extends JTable implements StepHandler, MathlibEventListener {
    /** hold the mathlib */
    protected Mathlib mathlib = null;
    /** this is to keep track of the properties of the GateTable class */
    protected PropertyChangeSupport properties;
    /** the GateTable class times the computation */
    protected Timing timing;
    /** holds the names of the qubits */
    protected Vector qubits;
    /** true if the table should be repainted every computational step */
    protected boolean updateTable;

    /** depth of the deepest GateContainer in the GateTable */
    protected int depth;

    /**
     * creates the GateTable which holds the quantum circuit. Required arguments are
     * a mathlib object, a timing object and whether the graphical output shall be
     * supressed. The Timing class is necessary since the GatePanel class does timing
     * as well and they share a timing object. 
     */
    public GateTable(Mathlib mathlib, Timing timing) {
	super();
	this.mathlib = mathlib;
	this.timing = timing;
	properties = new PropertyChangeSupport(this);
	Mathlib.addMathlibEventListener(this);
	qubits = new Vector();
	setUpdateTable(true);
    }

    /**
     * returns the mathlib object. 
     */
    public Mathlib getMathlib() {
	return mathlib;
    }

    /**
     * overwrites the JTable method to automatically create a GateTableColumnModel
     */
    public TableColumnModel createDefaultColumnModel() {
	return new GateTableColumnModel();
    }

    /**
     * overwrites the JTable method to provide the correct tooltip
     */
    public String getToolTipText(MouseEvent event) {
	GateContainer g = null;
	try {
	    g = (GateContainer)getModel().getValueAt(0, convertColumnIndexToModel(columnAtPoint(event.getPoint())));
	} catch (Exception e) { return ""; }

	return g.getDescription();
    }


    // gate specific methods
    /**
     * returns the gateContainer in column (the column index of the GateTableColumnModel)
     */
    public GateContainer getGateContainer(int column) {
	try {
	    return (GateContainer)getModel().getValueAt(0, convertColumnIndexToModel(column));
	} catch (Exception e) { return null; }
    }

    /**
     * returns a gateContainer of with the name specified. Names of gateContainers
     * must be unique.
     */
    public GateContainer getGateContainer(String name) {
	return ((GateTableModel)getModel()).getGateContainer(name);
    }

    /**
     * returns the name of a gateContainer speciefied by a column index
     * (GateTableColumnModel index)
     */
    public String getGateContainerName(int column) {
	return (String)getModel().getColumnName(convertColumnIndexToModel(column));
    }

    /**
     * returns the maximum depth of the GateContainers hold by this table
     */
    public int getMaxContainerDepth() {
	return depth;
    }

    /**
     * translates the current selection in the table into a gate_descr that can be
     * used to create a Gate object. This method is very important to provide the
     * interactive gate construction in the jaQuzzi program. Depending on the order
     * of the clicks, the selection is evaluated. For this purpose a different
     * SelectionModel is utilized.
     * @see QubitSelectionModel
     * @param matrixDim the dimension of the matrix to encode given as 2^matrixDim
     */
    public String translateSelectionTogate_descr(int matrixDim) {
	QubitSelectionModel qsm = (QubitSelectionModel)getSelectionModel();
	Vector v = qsm.getIndicesByPriority();
	int maxC = v.size()-matrixDim;
	int cCount = 0;
	Enumeration e = v.elements();
	char[] gate_descr = new char[getRowCount()];
	for (int i = 0; i < getRowCount(); i++) gate_descr[i] = '-';
	while(e.hasMoreElements()) {
	    gate_descr[((Integer)e.nextElement()).intValue()] = (cCount < maxC) ? '1' : 'm';
	    cCount++;
	}
	return new String(gate_descr);
    }

    /**
     * inserts a GateContainer into a given position (GateTableColumnModel index). If the naming
     * of the gateContainer to be inserted matches the naming of the container preceding the
     * position, the containers are grouped.
     * @see GatTable#registerGateContainer(GateContainer, int)
     */
    public void insertGateContainer(GateContainer gateContainer, int pos) {
	if (pos == 0) {
	    ((GateTableModel)getModel()).addGateContainer(gateContainer);
	    // add corresponding column
	    ((GateTableColumnModel)getColumnModel()).addGateContainer(gateContainer);
	    // move it
	    ((GateTableColumnModel)getColumnModel()).moveColumn(getColumnCount()-1, pos);
	}
	else {
	    GateContainer preceedingContainer = (GateContainer)getValueAt(0, pos-1);

	    // merge insuccessful ==> add
	    if (preceedingContainer.addContainerIfPossible(gateContainer) != 
		preceedingContainer) {

		((GateTableModel)getModel()).addGateContainer(gateContainer);
		// add corresponding column
		((GateTableColumnModel)getColumnModel()).addGateContainer(gateContainer);
		// move it
		((GateTableColumnModel)getColumnModel()).moveColumn(getColumnCount()-1, pos);
	    }
	}
	((GateTableModel)getModel()).tableUpdated();
    }

    /**
     * removes a GateContainer from the GateTableModel as well the GateTableColumnModel.
     * The index specified is the GateTableColumnModel index. It basically fires the 
     * required mathlib events.
     * @see GateTable#removeVariable(MathlibEvent)
     * @see GateTable#unregisterGateContainer
     */
    protected void removeGateContainer(int col) {
	GateContainer gateContainer = getGateContainer(col);
	if (gateContainer.isLeafContainer()) {
	    if (gateContainer.getPropertyObject() != null)
		Parse.fireMathlibEvent(gateContainer.getFullName()+"_properties", null, 
				       "current circuit", 
				       gateContainer.getPropertyObject(), 
				       MathlibEvent.REMOVE);
	    Parse.fireMathlibEvent(gateContainer.getFullName(), null, "current circuit", 
				   gateContainer.getGate(), MathlibEvent.REMOVE);
	}
	else {
	    unregisterGateContainer(col);
	    Enumeration leaves = gateContainer.getLeaves();
	    GateContainer leaf;
	    while (leaves.hasMoreElements()) {
		leaf = (GateContainer)leaves.nextElement();

		if (leaf.getPropertyObject() != null)
		    Parse.fireMathlibEvent(leaf.getFullName()+"_properties", 
					   null, 
					   "current circuit", 
					   leaf.getPropertyObject(), 
					   MathlibEvent.REMOVE);
		Parse.fireMathlibEvent(leaf.getFullName(), null, 
				       "current circuit", 
				       leaf.getGate(), MathlibEvent.REMOVE);
	    }
	}
    }

    /**
     * adds a gateContainer to the GateTableModel as well as the GateTableColumnModel, but does
     * not merge containers into groups
     * @see GateTable#insertGateContainer(GateContainer, int)
     */
    public void registerGateContainer(GateContainer gateContainer, int pos) {
	int gcDepth = gateContainer.getContainerDepth();
	if (gcDepth > depth) depth = gcDepth;
	((GateTableModel)getModel()).addGateContainer(gateContainer);
	// add corresponding column
	((GateTableColumnModel)getColumnModel()).addGateContainer(gateContainer);
	// move it
	((GateTableColumnModel)getColumnModel()).moveColumn(getColumnCount()-1, pos);
    }

    /**
     * remove gateContainer from model and columnModel without deleting the variables
     * @see GateTable#removeGateContainer(int)
     */
    public void unregisterGateContainer(int col) {
	GateContainer gateContainer = getGateContainer(col);
	((GateTableModel)getModel()).removeGateContainer(gateContainer);
	((GateTableColumnModel)getColumnModel()).removeGateContainer(gateContainer);

	depth = 0;
	int gcDepth = 0;
	for (int i = 0; i < getColumnCount(); i++) {
	    gcDepth = getGateContainer(i).getContainerDepth();
	    if (gcDepth > depth) depth = gcDepth;
	}
	((GateTableModel)getModel()).tableUpdated();
    }

    public int locateGateContainer(String gateContainer) {
	GateContainer col;
	int colNumber = 0;
	for (int i = 0; i < getColumnCount(); i++){
	    col = (GateContainer)getValueAt(0,i);
	    if (col.containsGateContainer(gateContainer)) return colNumber;
	    colNumber++;
	}
	return -1;
    }

    /**
     * this method allows to set the step beyond a reversible computation is not allowed
     * anymore, e.g. measurement
     */
    public void setReverseVetoStep(int step) {
	((GateTableModel)getModel()).setReverseVetoStep(step);
    }

    /**
     * returns the step beyond which no reversible computation is allowed
     */
    public int getReverseVetoStep() {
	return ((GateTableModel)getModel()).getReverseVetoStep();
    }

    // ---------------------------------
    // MathlibEventListner methods
    /**
     * method of the MathlibEventListener interface. This method reacts to events of the
     * category "current circuit" when objects of the class Gate are involved.
     */
    public void addVariable(MathlibEvent e) {
	if ("current circuit".equals(e.getCategory()) && e.getMathObject() instanceof Gate) {
	    // add gate to Model
	    GateContainer gateContainer = 
		new GateContainer(null, e.getObjectName(), e.getObjectName());
	    GateTableModel tableModel = ((GateTableModel)getModel());

	    if (getColumnCount() == 0) {
		tableModel.addGateContainer(gateContainer);

		// add corresponding column
		((GateTableColumnModel)getColumnModel()).addGateContainer(gateContainer);
	    }
	    // try to merge new container with the last one...
	    else {
		GateContainer lastContainer = (GateContainer)getValueAt(0, convertColumnIndexToModel(getColumnCount()-1));
		// merge insuccessful ==> add
		if (lastContainer.addContainerIfPossible(gateContainer)!=lastContainer) {
		    tableModel.addGateContainer(gateContainer);

		    // add corresponding column
		    ((GateTableColumnModel)getColumnModel()).addGateContainer(gateContainer);
		}
		// only adjust size...
		else {
		    GateTableColumnModel columnModel = (GateTableColumnModel)getColumnModel();
		    columnModel.getColumn(getColumnCount()-1).setPreferredWidth(lastContainer.getLeafCount()*columnModel.getPreferredColumnWidth());
		    int gcDepth = lastContainer.getContainerDepth();
		    if (gcDepth > depth) depth = gcDepth;
		}
	    }
	    tableModel.tableUpdated();
	    properties.firePropertyChange(GatePanel.stepCount, 0, getStepCount());
	    properties.firePropertyChange(GatePanel.remainingTimeEstimate, new Double(0),
					  new Double(timing.getTimeEstimateForStepsSec(getStepCount()-getStep())));
	}
	else if ("current circuit".equals(e.getCategory()) 
		 && e.getMathObject() instanceof GateProperty) {
	    GateTableModel tableModel = ((GateTableModel)getModel());

	    tableModel.tableUpdated();
	    updateStepModel();
	    properties.firePropertyChange(GatePanel.stepCount, 0, getStepCount());
	    properties.firePropertyChange(GatePanel.remainingTimeEstimate, new Double(0),
					  new Double(timing.getTimeEstimateForStepsSec(getStepCount()-getStep())));
	}
    }

    public void changeVariable(MathlibEvent e ) {
	GateTableModel model = (GateTableModel)getModel();
	GateTableColumnModel columnModel = (GateTableColumnModel)getColumnModel();

	if (e.getAction() == MathlibEvent.CHANGE_NAME) {
	    if ("current circuit".equals(e.getCategory()) 
		&& e.getMathObject() instanceof Gate) {

		GateContainer gateContainer = getGateContainer(e.getObjectName());

		if (gateContainer != null && gateContainer.isLeafContainer()) {
		    /*
		    GateProperty gp = gateContainer.getPropertyObject();
		    if (gp != null) {
			Parse.fireMathlibEvent(e.getObjectName()+"_properties", 
					       e.getNewName()+"_properties", 
					       "current circuit", gp, 
					       MathlibEvent.CHANGE_NAME);
		    }
		    
		    gateContainer.setTopLevelName(e.getNewName());
		    getColumnModel().getColumn(getColumnModel().getColumnIndex(e.getObjectName())).setHeaderValue(e.getNewName());
		    gateContainer.setGate(e.getNewName());
		    updateStepModel();
		    */
		    ((GateTableModel)getModel()).tableUpdated();
		    properties.firePropertyChange(GatePanel.table, 0, -1);
		}


		/*
		int pos = locateGateContainer(e.getObjectName());
		removeVariable(new MathlibEvent(e.getObjectName(), null, "current circuit", e.getMathObject(), MathlibEvent.REMOVE));
		GateContainer gateContainer = new GateContainer(null, 0, e.getNewName(), e.getNewName());
		insertGateContainer(gateContainer, pos);
		*/
	    }
	    /*
	    else if ("current circuit".equals(e.getCategory()) 
		     && e.getMathObject() instanceof GateProperty) {
		int pos = e.getNewName().lastIndexOf("_properties");
		((GateProperty)e.getMathObject()).setGate(e.getNewName().substring(0, pos));
		System.out.println("set gate to: " + e.getNewName().substring(0, pos));
	    }
	    */
	}
	else if (e.getAction() == MathlibEvent.CHANGE_CATEGORY) {
	    // ok, variable is currently checked in.
	    if ("current circuit".equals(e.getCategory())
		&& e.getMathObject() instanceof Gate) {
		GateContainer gateContainer = getGateContainer(e.getObjectName());
		if (("current circuit".equals(e.getNewName()) == false) && model.contains(gateContainer)) {
		    if (gateContainer != null) {
			model.removeGateContainer(gateContainer);
			columnModel.removeGateContainer(gateContainer);
			((GateTableModel)getModel()).tableUpdated();
			properties.firePropertyChange(GatePanel.stepCount, 
						      0, getStepCount());
			properties.firePropertyChange(GatePanel.remainingTimeEstimate, 
						      new Double(0),
						      new Double(timing.getTimeEstimateForStepsSec(getStepCount()-getStep())));

		    }
		}
	    }
	    // check in!
	    else if ("current circuit".equals(e.getNewName()) 
		     && e.getMathObject() instanceof Gate) {
		GateContainer gateContainer = getGateContainer(e.getObjectName());
		if (model.contains(gateContainer) == false && 
		    e.getMathObject() instanceof Gate ) {
		    
		    addVariable(new MathlibEvent(e.getObjectName(), null, "current circuit", e.getMathObject(), MathlibEvent.ADD));
		}
	    }
	}
	else if (e.getAction() == MathlibEvent.CHANGE && e.getMathObject() instanceof Gate) {
	    GateContainer gateContainer = getGateContainer(e.getObjectName());
	    if ("current circuit".equals(e.getCategory()) && 
		model.contains(gateContainer) && e.getMathObject() instanceof Gate) {
		gateContainer.updateDescription();
		model.tableUpdated();
	    }
	}
    }

    public void removeVariable(MathlibEvent e) {
	if ("current circuit".equals(e.getCategory()) && e.getMathObject() instanceof Gate) {
	    GateContainer gateContainer = getGateContainer(e.getObjectName());

	    // is a column
	    if (gateContainer != null) {
		// remove from model
		((GateTableModel)getModel()).removeGateContainer(gateContainer);
		// remove corresponding column
		((GateTableColumnModel)getColumnModel()).removeGateContainer(gateContainer);

		((GateTableModel)getModel()).tableUpdated();
		properties.firePropertyChange(GatePanel.stepCount, 
					      0, getStepCount());
		properties.firePropertyChange(GatePanel.remainingTimeEstimate, 
					      new Double(0),
					      new Double(timing.getTimeEstimateForStepsSec(getStepCount()-getStep())));
	    }
	    else {
		for (int i = 0; i < getColumnCount();i++) {
		    gateContainer = (GateContainer)getValueAt(0, i);
		    if (gateContainer.containsGateContainer(e.getObjectName())) {
			gateContainer.removeLeafGateContainer(e.getObjectName());
			break;
		    }
		}
		updateStepModel();
		properties.firePropertyChange(GatePanel.stepCount, 
					      0, getStepCount());
		properties.firePropertyChange(GatePanel.remainingTimeEstimate, 
					      new Double(0),
					      new Double(timing.getTimeEstimateForStepsSec(getStepCount()-getStep())));
	    }
	}
	else if ("current circuit".equals(e.getCategory()) 
		 && e.getMathObject() instanceof GateProperty) {
	    GateTableModel tableModel = ((GateTableModel)getModel());
	    tableModel.tableUpdated();
	    updateStepModel();
	    properties.firePropertyChange(GatePanel.stepCount, 0, getStepCount());
	    properties.firePropertyChange(GatePanel.remainingTimeEstimate, new Double(0),
					  new Double(timing.getTimeEstimateForStepsSec(getStepCount()-getStep())));
	}
    }

    /**
     * adds a qubit with a given name
     */
    public void addQubit(String name) {
	((GateTableModel)getModel()).addQubit();
	((GateTableModel)getModel()).tableUpdated();
	
	qubits.add(name);
    }

    public void moveQubit(int fromRow, int toRow) {
	((GateTableModel)getModel()).moveQubit(fromRow, toRow);
	((GateTableModel)getModel()).tableUpdated();

	if (fromRow < toRow) toRow--;
	String  name = (String)qubits.remove(fromRow);
	qubits.insertElementAt(name, toRow);
    }

    public void removeQubit(int row) {
	if (row >= 0 && row < getRowCount()) {
	    qubits.remove(row);
	    ((GateTableModel)getModel()).removeQubit(row);
	    ((GateTableModel)getModel()).tableUpdated();
	}
    }

    public String getQubitName(int row) {
	return Mathlib.getVar((String)qubits.get(row)).toString();
    }

    public String getQubitVar(int row) {
	return (String)qubits.get(row);
    }

    public int convertVariableToView(String var) {
	return qubits.indexOf(var);
    }

    /**
     * set whether the table should be updated during a calculation or not.
     * @param updateTable true to set update
     */
    public void setUpdateTable(boolean updateTable) {
	this.updateTable = updateTable;
    }

    /**
     * returns the flag whether the table is updated during a calculation
     */
    public boolean getUpdateTable() {
	return updateTable;
    }

    public void addPropertyChangeListener(PropertyChangeListener l)
    {
	properties.addPropertyChangeListener(l);
    }
    public void removePropertyChangeListener(PropertyChangeListener l)
    {
	properties.removePropertyChangeListener(l);
    }

    protected void fireTimingProperties() {
	properties.firePropertyChange(GatePanel.elapsedTime, new Double(-1), 
				      new Double(timing.getElapsedTimeSec()));
	properties.firePropertyChange(GatePanel.avgTimePerGate, new Double(-1),
				      new Double(timing.getAvgTimePerStepMillis()));
	properties.firePropertyChange(GatePanel.remainingTimeEstimate, new Double(-1),
				      new Double(timing.getTimeEstimateForStepsSec(getStepCount()-getStep())));
    }

    // --------------------------------------
    // StepHandler methods

    private int stepColumn;

    /**
     * wraps the GateTableModel method
     * @see GateTableModel#getDirection()
     */
    public int getDirection() {
	return ((GateTableModel)getModel()).getDirection();
    }

    /**
     * wraps the GateTableModel method
     * @see GateTableModel#getEndOfTable()
     */
    public boolean getEndOfTable() {
	return ((GateTableModel)getModel()).getEndOfTable();
    }

    /**
     * wraps the GateTableModel method
     * @see GateTableModel#getFidelity()
     */
    public double getFidelity() {
	return ((GateTableModel)getModel()).getFidelity();
    }

    /**
     * wraps the GateTableModel method
     * @see GateTableModel#setCalcFidelity(boolean)
     */
    public void setCalcFidelity(boolean calcFidelity) {
	((GateTableModel)getModel()).setCalcFidelity(calcFidelity);
    }

    /**
     * wraps the GateTableModel method
     * @see GateTableModel#getFidelityVector()
     */
    public Vector getFidelityVector() {
	return ((GateTableModel)getModel()).getFidelityVector();
    }

    /**
     * wraps the GateTableModel method
     * @see GateTableModel#getDecayedQubits()
     */
    public Vector getDecayedQubits() {
	return ((GateTableModel)getModel()).getDecayedQubits();
    }

    /**
     * wraps the GateTableModel method
     * @see GateTableModel#getDecaySteps()
     */
    public Vector getDecaySteps() {
	return ((GateTableModel)getModel()).getDecaySteps();
    }

    /**
     * performs one calculation step forward. This method translates the order of the gates
     * determined by the GateTableColumnModel into a model index and then calls the 
     * GateTableModel methods to perform the step forward.
     * @see GateTableModel#stepForward(int, int)
     */
    public void stepForward(){
	int oldStepColumn = stepColumn;
	int nextColumn = (stepColumn == getColumnCount() -1)? stepColumn : stepColumn +1;
	((GateTableModel)getModel()).stepForward(convertColumnIndexToModel(stepColumn),
						 convertColumnIndexToModel(nextColumn));
	stepColumn = convertColumnIndexToView(((GateTableModel)getModel()).getStepColumn());
	// paint?
	if (getUpdateTable()) {
	    paintColumn(oldStepColumn);
	    if (oldStepColumn != stepColumn) paintColumn(stepColumn);
	}
    }

    /**
     * performs one calculation step backward. This method translates the order of the gates
     * determined by the GateTableColumnModel into a model index and then calls the 
     * GateTableModel methods to perform the step backward.
     * @see GateTableModel#stepBackward(int, int)
     */
    public void stepBackward() {
	if (getStep() <= getReverseVetoStep()+1) {
	    JOptionPane.showMessageDialog(null, "Irreversible step! Sorry!");
	    return;
	}

	int oldStepColumn = stepColumn;
	int previousColumn = (stepColumn == 0) ? stepColumn : stepColumn - 1;
	((GateTableModel)getModel()).stepBackward(convertColumnIndexToModel(stepColumn),
						 convertColumnIndexToModel(previousColumn));
	stepColumn = convertColumnIndexToView(((GateTableModel)getModel()).getStepColumn());
	// paint?
	if (getUpdateTable()) {
	    paintColumn(oldStepColumn);
	    if (oldStepColumn != stepColumn) paintColumn(stepColumn);
	}
    }


    /**
     * performs a calculation up to the given step
     * @return the step where the computation ended
     */
    public int gotoStep(int toStep) {
	setUpdateTable(false);
	if (calculationRunning == false) {
	    stopPending = false;
	    calculation = new Calculation(toStep);
	    calculation.start();
	}
	return getStep();
    }

    /**
     * resets the step handler and the gate containers
     */
    public void reset() { 
	int oldStepColumn = stepColumn;
	((GateTableModel)getModel()).reset();
	stepColumn = 0;
	if (getColumnCount() > 0) {
	    int modelStepColumn = ((GateTableModel)getModel()).getStepColumn();
	    getGateContainer(convertColumnIndexToView(modelStepColumn)).setStepFocus(false);
	    ((GateTableModel)getModel()).setStepColumn(convertColumnIndexToModel(stepColumn));
	    getGateContainer(stepColumn).setStepFocus(true);
	}
	paintColumn(oldStepColumn);
	paintColumn(0);
    } 

    /**
     * repaints a given column
     */
    private void paintColumn(int col) {
	if (col >= 0 && col < getColumnCount()) {
	    // update header in case of iteration column
	    if (((GateContainer)getValueAt(0, col)).getMaxIteration() != 1)
		getTableHeader().resizeAndRepaint();

	    // update rest of column
	    Rectangle r1,r2;
	    // clear column
	    r1 = getCellRect(0, col, false);
	    r2 = getCellRect(getRowCount()-1, col, false);
	    repaint(r1.x, r1.y, r1.width, r2.y + r2.height);
	}
    }

    /**
     * returns the current step of the computation
     */
    public int getStep() { 
	return ((GateTableModel)getModel()).getStepModel().getValue();
    }

    /**
     * returns the column in which the computation is. Attention: if grouped gates are
     * present and iteration for gates is set, the stepColumn is not equal to the step.
     */
    public int getStepColumn() {
	return stepColumn;
    }

    /**
     * wraps the GateTableModel method
     * @see GateTableModel#getStepCount()
     */
    public int getStepCount() {
	return ((GateTableModel)getModel()).getStepModel().getMaximum();
    }

    /**
     * updates the step model according to the gate containers.
     */
    private void updateStepModel() {
	((GateTableModel)getModel()).updateStepModel();
    }

    /**
     * wraps the GateTableModel method
     * @see GateTableModel#getStepModel()
     */
    public DefaultBoundedRangeModel getStepModel() {
	return ((GateTableModel)getModel()).getStepModel();
    }

    /**
     * wraps the GateTableModel method
     * @see GateTableModel#getSimulationMode()
     */
    public int getSimulationMode() {
	return ((GateTableModel)getModel()).getSimulationMode();
    }

    //----------------------------------------------
    // load & save methods

    /**
     * clears the circuit. It removes all variables in the category "current circuit"
     * from the mathlib and disposes all gate containers.
     */
    public void flushCurrentCircuit() {
	reset();

	// remove grouped gateContainer
	for (int i = getColumnCount()-1; i >= 0; i--) {
	    removeGateContainer(i);
	}

	// remove qubits
	for (int i = getRowCount()-1; i >= 0; i--) {
	    Parse.fireMathlibEvent(getQubitVar(i), null, "current circuit", Mathlib.getVar(getQubitVar(i)), MathlibEvent.REMOVE);
	}

	// remove the rest
	VarAuthority varAuthority = new VarAuthority(mathlib, false);
	varAuthority.registerCategory("current circuit", new MathObjectConstraints(
		       null, "current circuit", -1, -1, -1, null));
	Enumeration e = varAuthority.getElementsInCategory("current circuit");
	String var;
	while (e.hasMoreElements()) {
	    var = (String)e.nextElement();
	    Parse.fireMathlibEvent(var, null, "current circuit", Mathlib.getVar(var), MathlibEvent.REMOVE);
	}
	qubits.removeAllElements();

	updateStepModel();
	((GateTableModel)getModel()).tableUpdated();
    }

    /**
     * loads a circuit from a given file. It constructs all the gate containers and
     * registers all variables belonging to the circuit in the category "current circuit"
     */
    public void loadModelFromFile(File file) {
	String ans;
	InputFile inf = new InputFile(file.getAbsolutePath());
	int rep;

	flushCurrentCircuit();
	timing.reset();

	Parse.echoAnswer = false;
	MathObject ansObject;
	while (inf.endOfFile() == false) {
	    inf.getLine();

	    try {
		ans = mathlib.evaluateExpression(inf.currentLine());
		ansObject = Mathlib.getVar(ans);
		if (ansObject instanceof GateProperty) {
		    Parse.fireMathlibEvent(((GateProperty)ansObject).getGate()+"_properties", "current circuit", null, ansObject, MathlibEvent.CHANGE_CATEGORY);
		}
		else {
		    Parse.fireMathlibEvent(ans, "current circuit", null, Mathlib.getVar(ans), MathlibEvent.CHANGE_CATEGORY);
		}
		//		gateTableModel.addGate(ans);
	    } catch (Exception e) {
		if (inf.currentLine().trim().length() > 0)
		    System.out.println("ignored line: "+inf.currentLine());
	    }		
	}
	Parse.echoAnswer = true;
	reset();
	inf.close();
    }

    /**
     * saves a circuit to a file. It writes all variables of the category "current 
     * circuit" into a file.
     */
    public void saveModelToFile(File file) {
	OutputFile of = new OutputFile(file.getAbsolutePath());

	VarAuthority varAuthority = new VarAuthority(mathlib, false);
	varAuthority.registerCategory("current circuit", new MathObjectConstraints(
		       null, "current circuit", -1, -1, -1, null));
	Enumeration e = varAuthority.getElementsInCategory("current circuit");
	String varName;
	MathObject variable;

	int digits = Complex.getDigits();
	Complex.setDigits(Complex.getInternalDigits());

	// write variables
	while (e.hasMoreElements()) {

	    varName = (String)e.nextElement();
	    variable = mathlib.getVar(varName);
	    if (variable instanceof Gate == false && 
		varName.startsWith("qubit_") == false && 
		variable instanceof GateProperty == false) {
		of.print(varName + "=" + variable.toParseableString() + "\n");
	    }
	}

	// write qubit names
	e = qubits.elements();
	int no = 0;
	while (e.hasMoreElements() ){
	    varName = (String)e.nextElement();
	    variable = mathlib.getVar(varName);
	    of.print("qubit_"+no + "=" + variable.toParseableString() + "\n");
	    no++;
	}

	// write gate properties
	varAuthority.registerCategory("gateproperties", new MathObjectConstraints(
		       GateProperty.class, "current circuit", -1, -1, -1, null));
	e = varAuthority.getElementsInCategory("gateproperties");
	while (e.hasMoreElements() ){
	    variable = mathlib.getVar((String)e.nextElement());
	    of.print(variable.toParseableString());
	}

	Complex.setDigits(digits);

	GateContainer g;
	// write gates
	for (int i = 0; i < getColumnCount(); i++) {
	    g = getGateContainer(i);
	    if (g.isLeafContainer()) {
		of.print(g.getFullName() + "=" +g.getGate().toString() + "\n");
	    }
	    else {
		Enumeration enumeration = g.getLeaves();
		GateContainer gateContainer;
		while (enumeration.hasMoreElements()) {
		    gateContainer = (GateContainer)enumeration.nextElement();
		    of.print(gateContainer.getFullName() + "=" +gateContainer.getGate().toString() + "\n");
		}
	    }
	}
	of.close();
    }

    boolean printQubits = true;

    /**
     * writes the circuit in a format which can be interpreted by the LaTeX
     * picture environment
     * @see TexQBrick
     */
    public void saveTableToTex(File file) {
	OutputFile of = new OutputFile(file.getAbsolutePath());
	int height = getRowCount()*TexQBrick.HEIGHT;
	int width = getColumnCount()*TexQBrick.WIDTH;
	TexQBrick tex;
	int offset = (printQubits)? TexQBrick.WIDTH:0;
	GateContainer gc;
	//	int yOffset = getMaxContainerDepth()*TexQBrick.HEIGHT;
	int yOffset = 0;
	int yCount = getMaxContainerDepth();
	int childOffset = 0;
	int z = 0;
	Enumeration childs;
	GateContainer child;

	of.print("\\begin{picture}("+(offset+width)+","+height+")\n");
	if (printQubits) {
	    for (int i = 0; i < getRowCount(); i++) {
		of.print("\\put(0,"+(yOffset+(getRowCount()-i-1)*TexQBrick.HEIGHT)+"){\\put("+TexQBrick.HSPACER+","+TexQBrick.VSPACER+"){\\makebox("+TexQBrick.FRAMEWIDTH+","+TexQBrick.FRAMEHEIGHT+"){$"+getQubitName(i)+"$}}}\n");
	    }
	}
	for(int j = 0; j < getColumnCount(); j++) {
	    gc = getGateContainer(j);
	    if (gc.isLeafContainer()) {
		for (int i = 0; i < getRowCount(); i++) {
		    tex = new TexQBrick(this, gc.getGate(), i, false, 0);
		    of.print("\\put("+(offset+(childOffset+j)*TexQBrick.WIDTH)+","+(yOffset+(getRowCount()-i-1)*TexQBrick.HEIGHT)+"){"+tex.getTexCode()+"}\n");
		}
	    }
	    else {
		childs = gc.getLeaves();
		z = 0;
		while (childs.hasMoreElements()) {
		    child = (GateContainer)childs.nextElement();
		    for (int i = 0; i < getRowCount(); i++) {
			tex = new TexQBrick(this, child.getGate(), i, false, 0);
			of.print("\\put("+(offset+(childOffset+z+j)*TexQBrick.WIDTH)+","+(yOffset+(getRowCount()-i-1)*TexQBrick.HEIGHT)+"){"+tex.getTexCode()+"}\n");
		    }
		    z++;
		}
		childOffset += gc.getLeafCount()-1;
	    }
	}

	of.print("\\end{picture}\n");
	of.close();
    }



    //------------------------------------------------------
    // calculation methods

    protected boolean calculationRunning = false;
    private boolean stopPending = false;
    Thread calculation;

    /**
     * returns true if a calculation thread is running
     */
    public boolean isCalculationRunning() {
	return calculationRunning;
    }

    /**
     * causes the calculation to stop
     */
    public void stopCalculation() {
	if (isCalculationRunning()) {
	    stopPending = true;
	}
    }

    /**
     * a thread to perform a computation in the quantum circuit.
     */
    protected class Calculation extends Thread {
	protected int toStep;
	public Calculation(int toStep) {
	    super("gateCalculation");
	    //	    setPriority(Thread.MIN_PRIORITY);
	    this.toStep = toStep;
	}

	public void run() {
	    int oldStep = getStep();
	    int oldStepColumn = getStepColumn();
	    int infoStep = ((toStep > oldStep)? toStep-oldStep : oldStep-toStep)/10;
	    if (infoStep == 0) infoStep = 1;
	    else if (infoStep > 10 && infoStep < 100) infoStep = 10;
	    else if (infoStep >= 100) infoStep = 100;
	    calculationRunning = true;
	    timing.start();
	    while (this.toStep > getStepModel().getValue() && stopPending == false) {
		stepForward();
		timing.stepsAccomplished(1);
		if (getStep() % infoStep == 0) {
		    fireTimingProperties();
		    properties.firePropertyChange(GatePanel.step, new Integer(oldStep), new Integer(getStep()));
		    properties.firePropertyChange(GatePanel.fidelity, new Double(0), new Double(getFidelity()));
		}
	    }
	    while (this.toStep < getStepModel().getValue() && stopPending == false) {
		stepBackward();
		timing.stepsAccomplished(1);
		if (getStep() % infoStep == 0) {
		    fireTimingProperties();
		    properties.firePropertyChange(GatePanel.step, new Integer(oldStep), new Integer(getStep()));
		    properties.firePropertyChange(GatePanel.fidelity, new Double(0), new Double(getFidelity()));
		}
	    }
	    timing.stop();
	    calculationRunning = false;
	    stopPending = false;
	    paintColumn(oldStepColumn);
	    paintColumn(getStepColumn());
	    properties.firePropertyChange(GatePanel.step, new Integer(oldStep), new Integer(getStep()));
	    fireTimingProperties();
	    properties.firePropertyChange(GatePanel.qubitUpdate, 0, 1);
	    properties.firePropertyChange(GatePanel.fidelity, new Double(0), new Double(getFidelity()));
	}
    }


}
