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
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;

import mathlib.Argument;
import mathlib.BinaryOp;
import mathlib.Braket;
import mathlib.Complex;
import mathlib.Decoherence;
import mathlib.Gate;
import mathlib.GateProperty;
import mathlib.LOG;
import mathlib.MathObject;
import mathlib.Mathlib;
import mathlib.MathlibEvent;
import mathlib.Matrix;
import mathlib.Measurement;
import mathlib.Parse;
import mathlib.Timing;

import java.util.*;

/**
 * This class holds the actual quantum circuit. Gates are represented by
 * GateContainer objects to allow gate groups. The actual gate information is stored
 * in a mathlib variable of the type Gate and each GateContainer has a reference to a
 * Gate variable. The GateTableModel does not autonomously react to MathlibEvents,
 * but rather the GateTable class is taking care of this.
 * @see GateTable
 * @see GateTableColumnModel
 * @see GateContainer
 * @see edu.buffalo.fs7.mathlib.Gate
 */
public class GateTableModel extends AbstractTableModel {

    /** holds names of gate variables */
    protected Vector gateContainers;

    /** hold the decoherence class */
    protected Decoherence decoherence;
    /** has one element for each computational step in case of the*/
    protected Vector fidelity;
    /** keeps track of the decayed qubits */
    protected Vector decayedQubits;
    /** keeps track of the steps where the decay occurred */
    protected Vector decaySteps;

    /** determines whether fidelity is calculated (calculating the fidelity requires the double
     * amount of time and memory) */
    protected boolean calcFidelity;

    /** keeps track of the current computation step*/
    protected DefaultBoundedRangeModel stepModel;

    protected Mathlib mathlib;

    /** variable holds the step at which the last action was performed that prevents 
     *  reversible execution of the circuit beyond that step, e.g., a measurement
     */
    protected int reverseVetoStep;

    /**
     * creates a GateTableModel. A gate is represented by a GateContainer. The
     * GateContainers are saved in an internal vector, but the gates themselves are
     * Gate objects represented by a variable in the Mathlib class.
     */
    public GateTableModel(Mathlib mathlib) {
	this.mathlib = mathlib;
	gateContainers = new Vector();
	fidelity = new Vector();
	fidelity.addElement(new Double(1));
	calcFidelity = false;
	decayedQubits = new Vector();
	decaySteps = new Vector();
	decoherence = new Decoherence();
	stepModel = new DefaultBoundedRangeModel(0,0,0,0);
	reverseVetoStep = -1;
    }

    /**
     * adds a GateContainer to the GateTableModel 
     * @param gateContainer the gateContainer object to add
     */
    public void addGateContainer(GateContainer gateContainer) {
	gateContainers.add(gateContainer);
	updateStepModel();
    }

    /**
     * gets the number of top level gateContainers (= column count)
     */
    public int getColumnCount() { 
	return gateContainers.size(); 
    }
    
    /**
     * returns the dimension of the gates (= qubit number)
     */
    public int getRowCount() { 
	return ((gateContainers.size() == 0) ? 0 : ((GateContainer)gateContainers.get(0)).getDimension());
    }

    /**
     * returns always the GateContainer class
     */
    public Class getColumnClass(int c) {
	return GateContainer.class;
    }

    /**
     * returns the top level name of the gateContainer in column column
     */
    public String getColumnName(int column) {
	return ((GateContainer)gateContainers.get(column)).getTopLevelName();
    }
       
    /**
     * returns the gateContainer in the model which corresponds to the column
     * the paramter row is ignored.
     */
    public Object getValueAt(int row, int col) {
	if (col < gateContainers.size()) 
	    return gateContainers.get(col);
	else return null;
    }

    /**
     * returns the gateContainer with the given name, null otherwise
     */
    public GateContainer getGateContainer(String name) {
	GateContainer gateContainer;
	for (int i= 0; i < getColumnCount(); i++) {
	    gateContainer = (GateContainer)getValueAt(0,i);
	    if (gateContainer.getFullName().equals(name)) return gateContainer;
	}
	return null;
    }

    /**
     * returns the gateContainer in the given column, null otherwise
     */
    public GateContainer getGateContainer(int column) {
	GateContainer gateContainer;
	if (column >= 0 && column < getColumnCount()) {
	    return (GateContainer)getValueAt(0,column);
	}
	return null;
    }

    /**
     * returns true if the cell is a preparation operation
     */
    public boolean isCellEditable(int row, int col) {
	GateContainer gateContainer = (GateContainer)getValueAt(row, col);
	if (gateContainer.isLeafContainer()) {
	    Gate gate = gateContainer.getGate();
	    if (gate.gate_descr.charAt(row) == 'u' || 
		gate.gate_descr.charAt(row) == 'd') {
		return true;
	    }
	    else return false;
	}
	else return false;
    }

    /**
     * method is called in the process of editing a cell. It only is invoked for the
     * preparation operation
     */
    public void setValueAt(Object value, int row, int col) {
	GateContainer gateContainer = (GateContainer)getValueAt(row, col);
	if (gateContainer.isLeafContainer()) {
	    Gate gate = gateContainer.getGate();
	    char[] gate_descr = new char[gate.n];

	    // convert to array
	    for (int i = 0; i < gate.n; i++) gate_descr[i] = gate.gate_descr.charAt(i);

	    // must be a set qubit event
	    if (value instanceof Boolean) {
		gate_descr[row] = (((Boolean)value).booleanValue()) ? 'd':'u';
		gate.gate_descr = new String(gate_descr);
		Parse.fireMathlibEvent(getColumnName(col), null, "current circuit", gate, MathlibEvent.CHANGE);
	    }
	}
    }


    /**
     * checks whether a given gateContainer is contained by the GateTableModel
     */
    public boolean contains(GateContainer gateContainer) {
	if (gateContainer ==null) return false;
	return gateContainers.contains(gateContainer);
    }

    /**
     * removes a given gateContainer
     */
    public void removeGateContainer(GateContainer gateContainer) {
	gateContainers.remove(gateContainer);
	updateStepModel();
    }

    /**
     * removes a given gateContainer
     */
    public void removeGateContainer(int column) {
	if (column < gateContainers.size()) {
	    gateContainers.remove(column);
	    updateStepModel();
	}
    }

    /**
     * remove a qubit, i.e. for each gateContainer in the model the gate_descr is
     * modiefied. A plausibility check is run afterwards.
     */
    public void removeQubit(int row) {
	if (row >= 0 && row < getRowCount()) {
	    GateContainer gateContainer;
	    Gate gate;
	    String gate_descr;
	    Enumeration leaves;
	    int ms = 0;
	    for (int i = 0; i < getColumnCount(); i++) {
		gateContainer = (GateContainer)getValueAt(0, i);
		leaves = gateContainer.getLeaves();
		while (leaves.hasMoreElements()) {
		    gate = ((GateContainer)leaves.nextElement()).getGate();
		    gate.n--;
		    gate_descr = gate.gate_descr.substring(0, row);
		    if (row < gate.gate_descr.length())
			gate_descr = gate_descr.concat(gate.gate_descr.substring(row+1));
		    gate.gate_descr = gate_descr;

		    //plausibility 
		    if (gate.gate_descr.indexOf("1") != -1 
			&& gate.gate_descr.indexOf("m") == -1) {
			gate.gate_descr = gate.gate_descr.replace('1', '-');
		    }
		    ms = 0;
		    for (int j = 0; j < gate.gate_descr.length(); j ++) {
			if (gate.gate_descr.charAt(j) == 'm') ms++;
		    }
		    if (gate.matrixDimension != BinaryOp.pow(2,ms)) {
			gate.gate_descr = gate.gate_descr.replace('m', '-');
		    }

		    Parse.fireMathlibEvent(getColumnName(i), null, "current circuit", gate, MathlibEvent.CHANGE);
		}
	    }
	}
    }

    /**
     * moves a qubit by exchanging the charakters in the gate_descr for each
     * gateContainer 
     */
    public void moveQubit(int fromRow, int toRow) {
	if (fromRow >= 0 && fromRow < getRowCount() && 
	    toRow >= 0 && toRow < getRowCount() && 
	    fromRow != toRow) {

	    GateContainer gateContainer;
	    Enumeration leaves;
	    Gate gate;
	    char[] gate_descr;
	    int index1 = (fromRow < toRow)? fromRow : toRow;
	    int index2 = (fromRow < toRow)? toRow : fromRow;

	    for (int i = 0; i < getColumnCount(); i++) {
		gateContainer = (GateContainer)getValueAt(0,i);
		leaves = gateContainer.getLeaves();
		while (leaves.hasMoreElements()) {
		    gate = ((GateContainer)leaves.nextElement()).getGate();
		    gate_descr = gate.gate_descr.toCharArray();
		    char temp = gate.gate_descr.charAt(index2);
		    for (int k = index2; k > index1; k--) {
			gate_descr[k] = gate_descr[k-1];
		    }
		    gate_descr[index1] = temp;

		    gate.gate_descr = new String(gate_descr);

		    Parse.fireMathlibEvent(getColumnName(i), null, "current circuit", gate, MathlibEvent.CHANGE);
		}
	    }
	}
    }

    /**
     * adds a qubit by adding a '-' to the gate_descr of each gate contained by the
     * gateContainers in this model.
     */
    public void addQubit() {
	GateContainer gateContainer;
	Enumeration leaves;
	Gate gate;
	for (int i = 0; i < getColumnCount(); i++) {
	    gateContainer = (GateContainer)getValueAt(0,i);
	    leaves = gateContainer.getLeaves();
	    while (leaves.hasMoreElements()) {
		gate = ((GateContainer)leaves.nextElement()).getGate();
		gate.n = gate.n + 1;
	    
		gate.gate_descr = gate.gate_descr.concat("-");

		Parse.fireMathlibEvent(getColumnName(i), null, "current circuit", gate, MathlibEvent.CHANGE);
	    }
	}
    }

    /**
     * wraps the fireTableDataChanged() method....
     */
    public void tableUpdated() {
	fireTableDataChanged();
    }

    private int stepColumn;
    private boolean endOfTable;
    private boolean forward;
    private Matrix errorMatrix;
    private double rate;
    private double decayProb;

    /**
     * returns the error modifier for the current gate
     */
    private Matrix getCurrentErrorMatrix() {
	return errorMatrix;
    }

    private double getCurrentDecoherenceRate() {
	return rate;
    }

    private double getCurrentDecayProbability() {
	return decayProb;
    }

    public double getFidelity() {
	return ((Double)fidelity.elementAt(fidelity.size()-1)).doubleValue();
    }

    /**
     * method sets whether the fidelity of during a calculation should be calculated
     * or not. If the boolean parameter is set to true, the calculation methods will look
     * for two Braket objects with the names "qubits" and "refQubits". The variable "qubits"
     * holds the calculation with errors, whereas the variable "refQubits" holds the ideal
     * calculation results.
     */
    public void setCalcFidelity(boolean calcFidelity) {
	this.calcFidelity = calcFidelity;
    }

    public Vector getFidelityVector() {
	return fidelity;
    }

    public Vector getDecayedQubits() {
	return decayedQubits;
    }

    public Vector getDecaySteps() {
	return decaySteps;
    }

    /**
     * returns the direction of the comutation as +1 (forward) -1 (backward)
     */
    public int getDirection() {
	return (forward)? 1:-1;
    }

    /**
     * true if step is in the last column and last step
     */
    public boolean getEndOfTable() {
	return endOfTable;
    }

    /**
     * calculates the fidelity of the quantum computation
     */
    protected double calculateFidelity(Braket q, Braket refQ) {
	if (refQ != null) {
	    refQ.transpose();
	    refQ.conjugate();
	    BinaryOp times = new BinaryOp('*');
	    Complex fid = (Complex)times.apply(refQ, q);
	    refQ.conjugate();
	    refQ.transpose();
	    return fid.magnitudeSquared();
	} else return 1;
    }

    /**
     * returns the current step of the computation
     */
    public int getStep() { 
	return stepModel.getValue();
    }

    /**
     * returns the maximum number of computational steps
     */
    public int getStepCount() {
	return stepModel.getMaximum();
    }

    /**
     * returns the column that has the step
     */
    public int getStepColumn() {
	return stepColumn;
    }

    /**
     * sets the stepColumn
     */
    public void setStepColumn(int stepColumn) {
	this.stepColumn = stepColumn;
    }

    /**
     * updates the step model according to the gate containers.
     */
    protected void updateStepModel() {
	int count = 0;
	for (int i = 0; i < getColumnCount(); i++) {
	    count += getGateContainer(i).getStepCount();
	}
	stepModel.setMaximum(count);
    }

    /**
     * returns the step model used by the step handler
     */
    public DefaultBoundedRangeModel getStepModel() {
	return stepModel;
    }

    /**
     * returns the next gate. not error proof.
     */
    private Gate nextGate(int currentColumn, int nextColumn) {
	stepColumn = currentColumn;
	GateContainer gc = getGateContainer(currentColumn);
	Gate gate = gc.getCurrentGate();
	int simulationMode = getSimulationMode();
	if (simulationMode == 1 || simulationMode == 3)
	    errorMatrix = gc.getCurrentErrorMatrix();
	if (simulationMode == 2 || simulationMode == 3) {
	    rate = gc.getCurrentDecoherenceRate();
	    decayProb = gc.getCurrentDecayProbability();
	}

	boolean result = gc.stepForward();
	if (result == false && currentColumn == nextColumn) {
	    endOfTable = true;
	}
	else if (result == false) {
	    gc.setStepFocus(false);
	    gc = getGateContainer(nextColumn);
	    stepColumn = nextColumn;
	    gc.setStepFocus(true);
	}
	return gate;
    }

    /**
     * returns the next gate in backward direction. not error proof.
     */
    private Gate previousGate(int currentColumn, int previousColumn) {
	stepColumn = currentColumn;
	GateContainer gc = getGateContainer(currentColumn);
	boolean result = gc.stepBackward();
	int simulationMode = getSimulationMode();
	if (endOfTable) {
	    endOfTable = false;
	    if (simulationMode == 1 || simulationMode == 3)
		errorMatrix = gc.getCurrentErrorMatrix();
	    if (simulationMode == 2 || simulationMode == 3) {
		rate = gc.getCurrentDecoherenceRate();
		decayProb = gc.getCurrentDecayProbability();
	    }
	    return gc.getCurrentGate();
	}
	if (result == false && currentColumn != previousColumn) {
	    gc.setStepFocus(false);
	    gc = getGateContainer(previousColumn);
	    stepColumn = previousColumn;
	    gc.stepBackward();
	    gc.setStepFocus(true);
	}
	if (simulationMode == 1 || simulationMode == 3)
	    errorMatrix = gc.getCurrentErrorMatrix();
	if (simulationMode == 2 || simulationMode == 3) {
	    rate = gc.getCurrentDecoherenceRate();
	    decayProb = gc.getCurrentDecayProbability();
	}
	return gc.getCurrentGate();
    }

    /**
     * steps one step forward in the order of the model.
     */
    public void stepForward() {
	stepForward(stepColumn, (stepColumn < getColumnCount()-1) ? stepColumn+1:stepColumn);
    }


    /**
     * performs the next calculation step in forward direction. As a parameter the current step
     * column is required as well as the next column in forward direction. This last parameter
     * is necessary because if this model is used within the GateTable class, the order of the
     * step columns can be different from the order in the model. The given indices are 
     * GateTableModel indices.
     */
    public void stepForward(int currentColumn, int nextColumn) {
	forward = true;
	if (stepModel.getValue() < getStepCount()) {
	    Gate g = nextGate(currentColumn, nextColumn);
	    if (g.gate_descr.indexOf("!") != -1 || g.gate_descr.indexOf("u") != -1 ||
		g.gate_descr.indexOf("d") != -1) setReverseVetoStep(getStep());
	    int simulationMode = getSimulationMode();
	    Braket q = (Braket) Mathlib.getVar("qubits");
	    MathObject refQ = Mathlib.getVar("refQubits");
	    // operational error
	    if (simulationMode == 1 || simulationMode ==3)
		BinaryOp.implicitApply(g, q, getCurrentErrorMatrix());
	    else 
		BinaryOp.implicitApply(g, q, null);

	    // decoherence error
	    if (simulationMode == 2 || simulationMode == 3) {
		decoherence.apply(getCurrentDecoherenceRate(), 
				  getCurrentDecayProbability(), q);
		if (decoherence.decayOccurred()) {
		    decayedQubits.add(new Integer(decoherence.getLastDecoheredQubit()));
		    decaySteps.add(new Integer(getStep()));
		    LOG.LOG(0, "decoherence at step: " + getStep());
		}

	    }
	    // reference qubit
	    if (calcFidelity && simulationMode != 0) {
		BinaryOp.implicitApply(g, (Braket)refQ, null);
		fidelity.addElement(new Double(calculateFidelity(q, (Braket)refQ)));
	    }
	    else fidelity.addElement(new Double(calculateFidelity(q, null)));

	    stepModel.setValue(stepModel.getValue()+1);
	}
    }

    /**
     * steps one step backward in the order of the model.
     */
    public void stepBackward() {
	stepBackward(stepColumn, (stepColumn > 0) ? stepColumn-1 : stepColumn);
    }

    /**
     * performs the next calculation step in backward direction. As a parameter the current step
     * column is required as well as the next column in backward direction. This last parameter
     * is necessary because if this model is used within the GateTable class, the order of the
     * step columns can be different from the order in the model. The given indices are 
     * GateTableModel indices.
     */
    public void stepBackward(int currentColumn, int previousColumn) {
	forward = false;
	if (stepModel.getValue() > 0) {
	    Gate g = previousGate(currentColumn, previousColumn);
	    g.transpose();
	    g.conjugate();

	    Braket q = (Braket)Mathlib.getVar("qubits");
	    MathObject refQ = Mathlib.getVar("refQubits");

	    int simulationMode = getSimulationMode();
	    if (simulationMode == 1 || simulationMode ==3)
		BinaryOp.implicitApply(g, q, getCurrentErrorMatrix());
	    else
		BinaryOp.implicitApply(g, q, null);

	    // decoherence error
	    if (simulationMode == 2 || simulationMode == 3) {
		decoherence.apply(getCurrentDecoherenceRate(), 
				  getCurrentDecayProbability(), q);
		if (decoherence.decayOccurred()) {
		    decayedQubits.add(new Integer(decoherence.getLastDecoheredQubit()));
		    decaySteps.add(new Integer(getStep()));
		    LOG.LOG(0, "decoherence at step: " + getStep());
		}
	    }

	    // reference qubit
	    if (calcFidelity && simulationMode != 0) {
		BinaryOp.implicitApply(g, (Braket)refQ, null);
		fidelity.addElement(new Double(calculateFidelity(q, (Braket)refQ)));
	    }
	    else fidelity.addElement(new Double(calculateFidelity(q, null)));

	    stepModel.setValue(stepModel.getValue()-1);
	    g.conjugate();
	    g.transpose();
	}
    }

    public void run() {
	int toStep = getStepCount();
	int oldStep = getStep();
	int infoStep = ((toStep > oldStep)? toStep-oldStep : oldStep-toStep)/10;
	if (infoStep == 0) infoStep = 1;
	else if (infoStep > 10 && infoStep < 100) infoStep = 10;
	else if (infoStep >= 100) infoStep = 100;
	Timing timing = new Timing();
	timing.start();
	System.out.println("computation started");
	while (toStep > stepModel.getValue()) {
	    stepForward();
	    timing.stepsAccomplished(1);
	    if (getStep() % infoStep == 0) {
		System.out.println("step: "+ getStep() + 
			    "\t"+GatePanel.elapsedTime+": "+timing.getElapsedTimeSec() +
				   "\t"+GatePanel.avgTimePerGate+": "+timing.getAvgTimePerStepMillis());
		
	    }
	}
	while (toStep < stepModel.getValue()) {
	    stepBackward();
	    timing.stepsAccomplished(1);
	    if (getStep() % infoStep == 0) {
		System.out.println("step: "+ getStep() + 
				   "\t"+GatePanel.elapsedTime+": "+timing.getElapsedTimeSec() +
				   "\t"+GatePanel.avgTimePerGate+": "+timing.getAvgTimePerStepMillis());
	    }
	}
	timing.stop();
	System.out.println("computation done in " + 
			   "\t"+GatePanel.elapsedTime+": "+timing.getElapsedTimeSec());
	
    }

    /**
     * resets the step handler and the gate containers
     */
    public void reset() { 
	GateContainer gc;
	for (int i = 0; i < getColumnCount(); i++) {
	    gc = getGateContainer(i);
	    gc.reset();
	    gc.setStepFocus(false);
	}
	forward = true;
	fidelity.removeAllElements();
	fidelity.addElement(new Double(1));
	decayedQubits.removeAllElements();
	decaySteps.removeAllElements();
	endOfTable = false;
	if (getColumnCount() > 0) getGateContainer(0).setStepFocus(true);
	stepColumn = 0;
	stepModel.setValue(0);
	updateStepModel(); 
	reverseVetoStep = -1;
    } 

    /**
     * this method allows to set the step beyond a reversible computation is not allowed
     * anymore, e.g. measurement
     */
    public void setReverseVetoStep(int step) {
	reverseVetoStep = step;
    }

    /**
     * returns the step beyond which no reversible computation is allowed
     */
    public int getReverseVetoStep() {
	return reverseVetoStep;
    }


    /**
     * returns the simulation mode currently set (0 = ideal, 1 = operational errors, 
     * 2 = decoherence errors, 3 = both errors
     */
    public int getSimulationMode() {
	MathObject cp = Mathlib.getVar("circuit_properties");
	if (cp != null) {
	    Argument simMode = ((GateProperty)cp).getProperty("mode");
	    if (simMode != null) {
		return new Double(((Complex)simMode).re()).intValue();
	    }
	    else return 0;
	}
	return 0;
    }

    /**
     * This method provides functionality to load just a table model from a file. In the 
     * interactive mode this method is not used, but in the batch mode. This method is much
     * simpler than the loadModelFromFile method implemented by the GateTable class. The reason
     * for this is, that no columns need to be created as well as all the other mathlibEvent 
     * listener need to be informed.
     * @see GateTable#loadModelFromFile(File)
     */
    public void primitiveLoadFromFile(File file) {
	String ans;
	InputFile inf = new InputFile(file.getAbsolutePath());
	int rep;
	setCalcFidelity(true);
	gateContainers.removeAllElements();

	Parse.echoAnswer = false;
	MathObject ansObject;
	int qubits = 0;
	while (inf.endOfFile() == false) {
	    inf.getLine();

	    try {
		ans = mathlib.evaluateExpression(inf.currentLine());
		ansObject = Mathlib.getVar(ans);
		if (ansObject instanceof Gate) {
		    qubits = ((Gate)ansObject).n;
		    // add gate to Model
		    GateContainer gateContainer = new GateContainer(null, ans, ans);

		    if (getColumnCount() == 0) {
			addGateContainer(gateContainer);
		    }
		    // try to merge new container with the last one...
		    else {
			GateContainer lastContainer = (GateContainer)getValueAt(0, getColumnCount()-1);
			// merge insuccessful ==> add
			if (lastContainer.addContainerIfPossible(gateContainer)!=lastContainer) {
			    addGateContainer(gateContainer);
			}
		    }
		}
	    } catch (Exception e) {
		System.out.println("ignored line: "+inf.currentLine());
	    }		
	}
	Parse.fireMathlibEvent("qubits", new Braket(0,qubits), MathlibEvent.ADD);
	Parse.fireMathlibEvent("refQubits", new Braket(0,qubits), MathlibEvent.ADD);
	Parse.echoAnswer = true;
	reset();
	inf.close();
    }

    /**
     * dumps the simultation results into a file. The format can be interpreted by
     * the GNUPlot prorgam. The naming convention is the following. The name of the
     * circuit +number +".prob" for probability distribution and +".fid" for fidelity.
     */
    public void dumpData(File file, int[] qubits) {
	String fileName = file.getName();

	if (fileName.endsWith(".jaq")) 
	    fileName = fileName.substring(0, fileName.length()-4);

	int counter = 0;
	File f = new File(fileName+counter+".prob");
	while (f.exists()) {
	    counter++;
	    f = new File(fileName+counter+".prob");
	}

	OutputFile of = new OutputFile(f.getAbsolutePath());

	String qubStr = new String("");
	if (qubits == null) qubStr = "all";
	else if (qubits.length > 0) {
	    qubStr = qubStr.concat(""+qubits[0]);
	    for (int i = 1; i < qubits.length; i++)
		qubStr = qubStr.concat(", "+qubits[i]);
	}

	// write probability distribution
	writeHeader(of);
	of.print("set title \"Probability Distribution ("+f.getName()+")\"\n");
	of.print("set xlabel \"state (qubits: "+qubStr+")\"\n");
	of.print("plot [*:*] [0:1] '-' with impulses \n");
	Braket q = (Braket)Mathlib.getVar(GatePanel.qubits);

	if (qubits== null) {
	    qubits = new int[q.n];
	    for (int i = 0; i < q.n; i++) qubits[i] = i;
	}
	Enumeration e = Measurement.getProbDistribution(q, qubits).elements();
	while (e.hasMoreElements()) {
	    of.print(((Double)e.nextElement()).doubleValue()+"\n");
	}
	of.close();

	f = new File(fileName+counter+".fid");
	of = new OutputFile(f.getAbsolutePath());

	// write fidelity
	writeHeader(of);
	of.print("set title \"Fidelity ("+f.getName()+")\"\n");
	of.print("set xlabel \"step\"\n");
	of.print("plot [*:*] [0:1] '-' with dots \n");
	e = getFidelityVector().elements();
	while (e.hasMoreElements()) {
	    of.print(((Double)e.nextElement()).doubleValue()+"\n");
	}
	of.close();
    }

    /**
     * writes some header data for gnu readable file
     */
    private void writeHeader(OutputFile of) {
	of.print("# steps in circuit = "+ getStepCount()+"\n");

	int simMode = getSimulationMode();

	MathObject cp = Mathlib.getVar("circuit_properties");
	if (cp != null) {
	    Argument a = ((GateProperty)cp).getProperty("sigma");
	    of.print("set label 2 \"sigma = "+((a == null || simMode == 0 || simMode == 2)? "0":a.toString())+"\" at 1,0.9\n");

	    a = ((GateProperty)cp).getProperty("rate");
	    of.print("set label 3 \"rate = "+((a == null || simMode == 0 || simMode == 1)? "0":a.toString())+"\" at 1,0.8\n");

	    a = ((GateProperty)cp).getProperty("decay");
	    of.print("set label 4 \"decay = "+((a == null || simMode == 0 || simMode == 1)? "0":a.toString())+"\" at 1,0.7\n");
	}

	Enumeration e = getDecayedQubits().elements();
	String str = new String("dec bits = ");
	while (e.hasMoreElements()) {
	    str = str.concat(((Integer)e.nextElement()).intValue()+",");
	}
	of.print("set label 5 \""+str+"\" at 1,0.6\n");

	e = getDecaySteps().elements();
	str = new String("dec steps = ");
	while (e.hasMoreElements()) {
	    str = str.concat(((Integer)e.nextElement()).intValue()+",");
	}
	of.print("set label 6 \""+str+"\" at 1,0.5\n");
	of.print("set nokey\n");
    }



    /**
     * this method retrieves information from a GateProperty object named
     * "circuit_properties" kept as a variable in the mathlib class. It returns -1
     * if the desired property is not specified. This method applies only to
     * properties of numerical value.
     * @param property name of the property, e.g. "mode"
     * @return double value
     */
    public double getCircuitProperty(String property) {
	MathObject o = Mathlib.getVar("circuit_properties");
	if (o != null) {
	    Argument a = ((GateProperty)o).getProperty(property);
	    if (a != null) {
		return ((Complex)a).re();
	    }
	    else return -1;
	}
	else return -1;
    }

    /**
     * sets a property to a GateProperty object "circuit_properties" kept as a
     * variable in the mathlib class. It creates the variable if necessary. This
     * method only applies to numerical properties.
     */
    public void setCircuitProperty(String property, double value) {
	int action;
	MathObject o = Mathlib.getVar("circuit_properties");
	GateProperty cp;
	if (o == null) {
	    action = MathlibEvent.ADD;
	    cp = new GateProperty("circuit");
	}
	else {
	    action = MathlibEvent.CHANGE;
	    cp = (GateProperty)o;
	}

	cp.addProperty(property, new Complex(value));

	Parse.fireMathlibEvent("circuit_properties", null, "current circuit", 
			       cp, action);
    }

}


