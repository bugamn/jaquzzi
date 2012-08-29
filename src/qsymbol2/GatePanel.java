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

import jaquzzi.jaQuzzi;

import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;

import mathlib.Argument;
import mathlib.Braket;
import mathlib.Complex;
import mathlib.Gate;
import mathlib.GateProperty;
import mathlib.MathObject;
import mathlib.Mathlib;
import mathlib.MathlibEvent;
import mathlib.MathlibEventListener;
import mathlib.Measurement;
import mathlib.Parse;
import mathlib.StringArgument;
import mathlib.Timing;

import java.beans.*;

/**
 * The GatePanel class is the central class in the qsymbol2 package. It is
 * responsible to bring up a quantum circuit (provided by the GateTable class) with
 * qubit controls, step gauge and computation controls. Basically, everything you see
 * besides the toolbar in the jaQuzzi program is provided by this class.
 * <p>This class is responsible to add and remove qubits (since it has to update the
 * GateTable as well as the QubitPanel) and therefore implements the
 * MathlibEventListener interface.</p>
 * It as well implements other interfaces to react to changes in the GateTable or
 * QubitPanel.
 * @see GateTable
 * @see QubitPanel
 */
public class GatePanel extends JPanel implements ComponentListener, ItemListener, ActionListener, MathlibEventListener{

    /** string constant for qubits variable */
    public static final String qubits = new String("qubits");
    /** string constant for second qubits variable in case of calculating fidelity */
    public static final String refQubits = new String("refQubits");
    /** string constant for fidelity property */
    public static final String fidelity = new String("fidelity");
    /** string constant for elapsed time property */
    public static final String elapsedTime = "elaps. time[s]";
    /** string constant for step property */
    public static final String step = "step";
    /** string constant for stepCount property */
    public static final String stepCount = "stepCount";
    /** string constant for averageTimerperGate property */
    public static final String avgTimePerGate = "avg. time/gate[ms]";
    /** string constant for remainingTimeEstimate property */
    public static final String remainingTimeEstimate  = "remain. time[s]";
    /** string constant for table property */
    public static final String table = "table";
    /** string constant for qubitUpdate property */
    public static final String qubitUpdate = "qubitUpdate";

    protected GridBagLayout gridbag;
    protected GridBagConstraints c;

    /** holds the qubitPanel for setting init values to the qubits */
    public QubitPanel inQubits;

    protected JLabel statusBarLabel;
    protected JLabel statusBar;
    protected JButton runButton;
    /** true if a computation thread is running */
    protected boolean run = true;
    /** true if the fidelity is to be calculated */
    protected boolean calcFidelity;

    protected JProgressBar progressBar;

    protected JScrollPane viewPort;
    /** the GateTable */
    public GateTable gates;
    /** the mathlib */
    protected Mathlib mathlib;

    private PropertyChangeSupport properties;
    private Timing timing;

    /** 
     * create a GatePanel class. A mathlib object needs to be provided
     */
    public GatePanel(Mathlib mathlib) {
	this.mathlib = mathlib;
	Mathlib.addMathlibEventListener(this);
	calcFidelity = false;
	Parse.fireMathlibEvent(qubits, new Braket(4), MathlibEvent.ADD);

	// holds the statistics
	timing = new Timing();
	properties = new PropertyChangeSupport(this);

	inQubits = new QubitPanel(this, 0, QubitPanel.EXTENDED);

	statusBarLabel = new JLabel("state:");
	statusBar = new JLabel("");
	statusBar.setBorder(BorderFactory.createLoweredBevelBorder());

	// create gate table
	gates = new GateTable(mathlib, timing);
	gates.setDefaultRenderer(GateContainer.class, new QubitRenderer());
	gates.setDefaultEditor(GateContainer.class, new QubitEditor());
	gates.setSelectionModel(new QubitSelectionModel());
      	gates.setCellSelectionEnabled(true);
	gates.setAutoCreateColumnsFromModel(false);
	gates.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
	gates.setModel(new GateTableModel(mathlib));

	gates.setShowHorizontalLines(false);
	gates.setShowVerticalLines(false);
	gates.setIntercellSpacing(new Dimension(0, 0));

	gates.addMouseListener(new MouseAdapter() {
		public void mouseClicked(MouseEvent e) {
		    if (e.isControlDown()) {
			if (gates.getSelectedRowCount() > 0) {
			    int clickedColumn = gates.columnAtPoint(e.getPoint());
			    int clickedRow = gates.rowAtPoint(e.getPoint());
			    if (gates.isColumnSelected(clickedColumn) == false) {
				// okay overwrite the selection behavior
				gates.setColumnSelectionInterval(clickedColumn, clickedColumn);
			    }
			}
		    }
		}
	    });


	viewPort = new JScrollPane(gates);
	viewPort.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
	viewPort.addComponentListener(this);

	progressBar = new JProgressBar(gates.getStepModel());
	progressBar.setBorderPainted(false);




	// arrange the bits & pieces....

	setBorder(BorderFactory.createEtchedBorder());

	// arranging the components....
	gridbag = new GridBagLayout();
	c = new GridBagConstraints();
	setLayout(gridbag);

	
	//inQubits
	c.fill = GridBagConstraints.VERTICAL;
	c.weightx = 0;
	c.weighty = 1.0;
	c.gridwidth = GridBagConstraints.RELATIVE;
	c.insets = new Insets(30, 10, 17, 0);
	gridbag.setConstraints(inQubits, c);
	add(inQubits);
	
	//gates
	c.fill = GridBagConstraints.BOTH;
	c.weightx = 1.0;
	c.weighty = 1.0;
	c.gridwidth = GridBagConstraints.REMAINDER;
	c.insets = new Insets(10, 0, 0, 10);
	gridbag.setConstraints(viewPort, c);
	add(viewPort);

	//statusBar label
	c.fill = GridBagConstraints.NONE;
	c.weightx = 0;
	c.weighty = 0;
	c.gridwidth = GridBagConstraints.RELATIVE;
	c.insets = new Insets(10, 10, 10, 10);
       	gridbag.setConstraints(statusBarLabel, c);
       	add(statusBarLabel);


	JPanel panel = new JPanel();
	GridBagConstraints g = new GridBagConstraints();
	GridBagLayout gbl = new GridBagLayout();
	panel.setLayout(gbl);

	//statusbar
	g.fill = GridBagConstraints.HORIZONTAL;
	g.weightx = 1.0;
	g.weighty = 0;
	g.gridwidth = GridBagConstraints.RELATIVE;
	g.insets = new Insets(10, 10, 10, 0);
	gbl.setConstraints(statusBar, g);
	panel.add(statusBar);

	//action bar
	g.fill = GridBagConstraints.NONE;
	g.weightx = 0;
	g.weighty = 0;
	g.gridwidth = GridBagConstraints.REMAINDER;
	g.insets = new Insets(0, 0, 0, 10);
	JToolBar actionBar = createToolbar2();
	gbl.setConstraints(actionBar, g);
	panel.add(actionBar);

	c.fill = GridBagConstraints.HORIZONTAL;
	c.weightx = 1.0;
	c.weighty = 0;
	c.gridwidth = GridBagConstraints.REMAINDER;
	c.insets = new Insets(0, 0, 0, 0);
	gridbag.setConstraints(panel, c);
	add(panel);


	JPanel progressPanel = new JPanel();
	progressPanel.setBorder(BorderFactory.createLoweredBevelBorder());
	gbl = new GridBagLayout();
	progressPanel.setLayout(gbl);
	g.fill = GridBagConstraints.HORIZONTAL;
	g.weightx = 1.0;
	g.insets = new Insets(0,0,0,0);
	gbl.setConstraints(progressBar, g);
	progressPanel.add(progressBar);
	c.insets = new Insets(0, 0, 0, 0);
	gridbag.setConstraints(progressPanel,c);
	add(progressPanel);

    }

    /**
     * creates the computation controll toolbar
     */
    protected JToolBar createToolbar2() {
	JToolBar toolbar;
	JButton b;

	toolbar = new JToolBar();
	toolbar.setFloatable(false);

	b = new JButton(jaQuzzi.dataIcon);
	b.setActionCommand("enlarge");
	b.setToolTipText("display the whole state");
	b.addActionListener(this);
	toolbar.add(b);

	b = new JButton(jaQuzzi.resetIcon);
	b.setActionCommand("reset");
	b.setToolTipText("reset the circuit");
	b.addActionListener(this);
	toolbar.add(b);

	b =new JButton(jaQuzzi.stepbackwardIcon);
	b.setActionCommand("back");
	b.setToolTipText("step backward");
	b.addActionListener(this);
	toolbar.add(b);

	runButton = new JButton(jaQuzzi.runIcon);
	runButton.setActionCommand("run");
	runButton.setToolTipText("run!");
	runButton.addActionListener(this);
	toolbar.add(runButton);

	b = new JButton(jaQuzzi.stepforwardIcon);
	b.setActionCommand("forward");
	b.setToolTipText("step forward");
	b.addActionListener(this);
	toolbar.add(b);

	b = new JButton(jaQuzzi.measureIcon);
	b.setActionCommand("measure");
	b.setToolTipText("measure!");
	b.addActionListener(this);
	toolbar.add(b);


	return toolbar;
    }


    // ------------------------------------
    // methods of ComponentListener

    /** implementing ComponentListener interface. nothing is done here. */
    public void componentHidden(ComponentEvent e) { }
    /** implementing ComponentListener interface. nothing is done here. */
    public void componentMoved(ComponentEvent e) { }
    /** implementing ComponentListener interface. nothing is done here. */
    public void componentShown(ComponentEvent e) { }

    /**
     * implementing ComponentListener interface. The row height is adjusted to fit
     * the qubits into the window.
     */
    public void componentResized(ComponentEvent e) {
	if (e.getComponent().equals(viewPort)) {
	    if (gates != null) {
		gates.setRowHeight(calculateRowHeight());
	    }
	}
    }

    // ------------------------------------
    // methods of ItemListener
    /**
     * method of the ItemListener interface. This is implemented to react to changes
     * of the qubit controls.
     * @see QubitPanel
     */
    public void itemStateChanged(ItemEvent e) {
	if (gates.isCalculationRunning() == false) {
	    int oldStep = gates.getStep();
	    int oldStepCount = gates.getStepCount();
	    Braket q = inQubits.getQubitValue();
	    if (q != null) {
		Parse.fireMathlibEvent(qubits, q, MathlibEvent.CHANGE);
		statusBar.setText(q.toString(200));
	    }
	    if (calcFidelity) {
		Braket refQ = inQubits.getQubitValue();
		if (refQ != null) {
		    Parse.fireMathlibEvent(refQubits, refQ, MathlibEvent.CHANGE);
		}
	    }
	    
	    gates.reset();
	    properties.firePropertyChange(step, -1, gates.getStep());
      	    properties.firePropertyChange(stepCount, oldStepCount, gates.getStepCount());
	    properties.firePropertyChange(elapsedTime, new Double(timing.getElapsedTimeSec()), new Double(0)); 
	    timing.reset();
	    properties.firePropertyChange(fidelity, new Double(0), new Double(gates.getFidelity()));
	}
    }

    // ------------------------------------
    // methods of ActionListener
    /**
     * method of the ActionListener interface. Here, the computation control center
     * events are processed.
     */
    public void actionPerformed(ActionEvent e) {
	if ("enlarge".equals(e.getActionCommand())) {
	    if (jaQuzzi.dataWindow != null) {
		jaQuzzi.dataWindow.setQubits(null);
		if (jaQuzzi.dataWindowFrame.isVisible())
		    jaQuzzi.dataWindow.refresh();
		else jaQuzzi.dataWindowFrame.setVisible(true);
	    }
	}
	else if ("measure".equals(e.getActionCommand())) {
	    if (gates.isCalculationRunning() == false) {
		Measurement m = new Measurement();
		Braket newq = (Braket)m.apply(Mathlib.getVar(qubits));
		Parse.fireMathlibEvent(qubits, newq, MathlibEvent.CHANGE);
		if (calcFidelity) {
		    Braket newRefQ = (Braket)m.apply(Mathlib.getVar(refQubits));
		    Parse.fireMathlibEvent(refQubits, new Braket(newRefQ), MathlibEvent.CHANGE);
		}
		gates.setReverseVetoStep(gates.getStep());
		qubitUpdate();
	    }
	}
	else if ("back".equals(e.getActionCommand())) {
	    stepBackward();
	}
	else if ("forward".equals(e.getActionCommand())) {
	    stepForward();
	}
	else if ("run".equals(e.getActionCommand())) {
	    run();
	}
	else if ("reset".equals(e.getActionCommand())) {
	    if (gates.isCalculationRunning() == false) {
		reset();
		qubitUpdate();
	    }
	}
    }

    /**
     * runs a computation. Depending on the operation mode, a thread is started or
     * the comutation is done linearly.
     * @see GateTable#gotoStep(Braket, Braket, int)
     */
    public void run() {
	if (gates.isCalculationRunning() == false) {
	    gates.getTableHeader().setReorderingAllowed(false);
	    runButton.setIcon(jaQuzzi.stopIcon);
	    runButton.setToolTipText("running...press to stop!");
	    run = false;
	    int oldStep = gates.getStep();
	    gates.gotoStep(gates.getStepCount());

	}
	else {
	    gates.getTableHeader().setReorderingAllowed(true);
	    runButton.setIcon(jaQuzzi.runIcon);
	    runButton.setToolTipText("run!");
	    run = true;
	    gates.stopCalculation();
	}
    }

    private double timeForStep;

    /**
     * wraps the stepForward method of the GateTable class. This is done to obtain
     * more precise timing information in case of interactive stepping (graphics
     * require more time).
     * @see GateTable#stepForward(Braket, Braket)
     */
    public void stepForward() {
	if (gates.isCalculationRunning() == false && gates.getEndOfTable() == false) {
	    int oldStep = gates.getStep();
	    timing.start();
	    gates.setUpdateTable(true);
	    gates.stepForward();
	    
	    statusBar.setText(((Braket)Mathlib.getVar(qubits)).toString(200));
	    properties.firePropertyChange(step, new Integer(oldStep), new Integer(gates.getStep()));
	    properties.firePropertyChange(fidelity, new Double(0), new Double(gates.getFidelity()));
	    timing.stop(1);
	    fireTimingProperties();
	}
    }

    /**
     * wraps the stepBackward method of the GateTable class. This is done to obtain
     * more precise timing information in case of interactive stepping (graphics
     * require more time).
     * @see GateTable#stepBackward(Braket, Braket)
     */
    public void stepBackward() {
	if (gates.isCalculationRunning() == false && gates.getStep() > 0) {
	    int oldStep = gates.getStep();
	    timing.start();
	    gates.setUpdateTable(true);
	    gates.stepBackward();
	    
	    statusBar.setText(((Braket)Mathlib.getVar(qubits)).toString(200));
	    properties.firePropertyChange(step, new Integer(oldStep), new Integer(gates.getStep()));
	    properties.firePropertyChange(fidelity, new Double(0), new Double(gates.getFidelity()));
	    timing.stop(0);
	    fireTimingProperties();
	}
    }

    /**
     * method sets whether the fidelity of during a calculation should be calculated
     * or not. If the boolean parameter is set to true, an additional Braket object
     * is created in order to provide the possibility to calculate the ideal state
     * and the non-ideal state at the same time.
     */
    public void setFidelity(boolean calcFidelity) {
	if (this.calcFidelity != calcFidelity) {
	    reset();
	    this.calcFidelity = calcFidelity;
	    gates.setCalcFidelity(calcFidelity);
	    if (calcFidelity == true) {
		Parse.fireMathlibEvent(refQubits, inQubits.getQubitValue(), 
				       MathlibEvent.ADD);
	    }else {
		Parse.fireMathlibEvent(refQubits, inQubits.getQubitValue(), 
				       MathlibEvent.REMOVE);
	    }
	}
    }

    /**
     * generates an add qubit event which causes the GatePanel class to modify the
     * QubitPanel and the GateTable. This method is the preferred method to add a qubit
     */
    public void addQubit() {
	int number = 0;

	// find free name
	while (Mathlib.getVar("qubit_"+number) != null) number++;

	Parse.fireMathlibEvent("qubit_"+number, null, "current circuit", new StringArgument("qubit_"+number), MathlibEvent.ADD);

    }

    /**
     * generates an insert qubit event which causes the GatePanel class to modify the
     * QubitPanel and the GateTable. This method is the preferred method to inesert a 
     * qubit.
     */
    public void insertQubit(int row) {
	addQubit();
	gates.moveQubit(gates.getRowCount()-1, row);
	inQubits.moveQubit(gates.getRowCount()-1, row);

	inQubits.revalidate();
	inQubits.repaint();

	// call method directly....
	itemStateChanged(null);
    }

    /**
     * generates an remove qubit event which causes the GatePanel class to modify the
     * QubitPanel and the GateTable. This method is the preferred method to remove  a 
     * qubit.
     */
    public void removeQubit(int row) {
	Parse.fireMathlibEvent(gates.getQubitVar(row), null, "current circuit", 
			 Mathlib.getVar(gates.getQubitVar(row)), MathlibEvent.REMOVE); 
    }


    /**
     * generates an insert gateContainer event which causes the GatePanel class to
     * modify the GateTable. This method is the preferred method
     * to insert a gateContainer.  */
    public void insertGateContainer(int col) {
	addGateContainer();
	gates.getColumnModel().moveColumn(gates.getModel().getColumnCount()-1, col);
    }

    /**
     * generates an add gateContainer event which causes the GatePanel class to
     * modify the GateTable. This method is the preferred method
     * to add a gateContainer.  */
    public void addGateContainer() {
	int oldStepCount = gates.getStepCount();
	int number = gates.getModel().getColumnCount();

	// this should end!!
	while (Mathlib.getVar("gate"+number) != null) number++;

	String no = new Integer(number).toString();

	Parse.fireMathlibEvent("gate"+no, null, "current circuit", 
			       new Gate(gates.getModel().getRowCount()), 
			       MathlibEvent.ADD);
    }

    /**
     * generates a remove gateContainer event which causes the GatePanel class to
     * modify the GateTable. This method is the preferred method
     * to remove a gateContainer.  */
    public void removeGateContainer(int col) {
	gates.removeGateContainer(col);
    }


    /**
     * generates a rename gateContainer event which causes the GatePanel class to
     * modify the GateTable. This method is the preferred method
     * to rename a gateContainer. Renaming is quite complex since the renaming of a
     * gate group requires the renaming of all the contained gate variables. */
    public void renameGateContainer(int col, String newName) {
	GateContainer gateContainer = gates.getGateContainer(col);
	String oldTopLevel = gateContainer.getTopLevelName();
	gateContainer.setTopLevelName(newName);
	gates.getColumnModel().getColumn(col).setHeaderValue(newName);
	//fire mathlib events
	gateContainer.adjustGateContainer(oldTopLevel);

	properties.firePropertyChange(table, 0, 1);
    }


    // ------------------------------------
    // methods of MathlibEventListener
    /**
     * method of the MathlibEventListener interface. This method reacts to an add
     * qubit event.
     */
    public void addVariable(MathlibEvent e) {
	// add a qubit
	if ("current circuit".equals(e.getCategory()) && 
		 e.getObjectName().startsWith("qubit_") &&
		 e.getMathObject() instanceof StringArgument) {

	    int oldQubits = gates.getRowCount();
	    gates.addQubit(e.getObjectName());

	    gates.setRowHeight(calculateRowHeight(gates.getRowCount()));

	    inQubits.addQubit(e.getMathObject().toString()); 
	    inQubits.revalidate();
	    inQubits.repaint();

	    // call method directly....
	    itemStateChanged(null);
	    
	    properties.firePropertyChange(qubits, new Integer(oldQubits), new Integer(gates.getRowCount()));
	    timing.reset();
	    fireTimingProperties();
	}
	else if ("current circuit".equals(e.getCategory()) && 
		 e.getObjectName().equals("circuit_properties") &&
		 e.getMathObject() instanceof GateProperty) {
	    Argument fid = ((GateProperty)e.getMathObject()).getProperty("fidelity");
	    if (fid != null) {
		if (((Complex)fid).re() == 0) setFidelity(false);
		else setFidelity(true);
	    }
	}
    }

    /**
     * method of the MathlibEventListener interface. This method reacts to an change
     * qubit event.
     */
    public void changeVariable(MathlibEvent e) {
	if ("current circuit".equals(e.getCategory()) && 
	    e.getObjectName().startsWith("qubit_") &&
	    e.getMathObject() instanceof StringArgument) {

	    if (e.getAction() == MathlibEvent.CHANGE) {
		inQubits.setQubitName(gates.convertVariableToView(e.getObjectName()),
				      e.getMathObject().toString());
	    }
	    else if (e.getAction() == MathlibEvent.CHANGE_CATEGORY) {
		if ("current circuit".equals(e.getNewName()) == false){
		    int row = gates.convertVariableToView(e.getObjectName());
		    if (row != -1) {

			int oldQubits = gates.getRowCount();
			gates.removeQubit(row);
			gates.setRowHeight(calculateRowHeight(gates.getRowCount()));

			inQubits.removeQubit(row);
			inQubits.revalidate();
			inQubits.repaint();

			// call method directly....
			itemStateChanged(null);

			properties.firePropertyChange(qubits, new Integer(oldQubits), new Integer(gates.getRowCount()));
			timing.reset();
			fireTimingProperties();
		    }
		}
	    }
	}
	else if ("current circuit".equals(e.getNewName()) && 
		 e.getAction() == MathlibEvent.CHANGE_CATEGORY &&
		 e.getObjectName().startsWith("qubit_") &&
		 e.getMathObject() instanceof StringArgument) {

	    int oldQubits = gates.getRowCount();
	    gates.addQubit(e.getObjectName());

	    gates.setRowHeight(calculateRowHeight(gates.getRowCount()));

	    inQubits.addQubit(e.getMathObject().toString()); 
	    inQubits.revalidate();
	    inQubits.repaint();

	    // call method directly....
	    itemStateChanged(null);

	    properties.firePropertyChange(qubits, new Integer(oldQubits), new Integer(gates.getRowCount()));
	    timing.reset();
	    fireTimingProperties();
	}
	else if ("current circuit".equals(e.getCategory()) && 
		 e.getObjectName().equals("circuit_properties") &&
		 e.getMathObject() instanceof GateProperty) {
	    Argument fid = ((GateProperty)e.getMathObject()).getProperty("fidelity");
	    if (fid != null) {
		if (((Complex)fid).re() == 0) setFidelity(false);
		else setFidelity(true);
	    }
	}
    }

    /**
     * method of the MathlibEventListener interface. This method reacts to an remove
     * qubit event.
     */
    public void removeVariable(MathlibEvent e) {
	if ("current circuit".equals(e.getCategory()) && 
	    e.getObjectName().startsWith("qubit_") &&
	    e.getMathObject() instanceof StringArgument) {

	    int number = -1;

	    number = gates.convertVariableToView(e.getObjectName());
	    if (number != -1) {
		int oldQubits = gates.getRowCount();
		gates.removeQubit(number);
		gates.setRowHeight(calculateRowHeight(gates.getRowCount()));

		inQubits.removeQubit(number);
		inQubits.revalidate();
		inQubits.repaint();

		// call method directly....
		itemStateChanged(null);
		properties.firePropertyChange(qubits, new Integer(oldQubits), new Integer(gates.getRowCount()));
		timing.reset();
		fireTimingProperties();
	    }
	}
    }

    /**
     * method to increase the column width of the columns in the GateTable. It also
     * adjusts the width of the TexQBrick class.
     */
    public void increaseColumnWidth() {
	((GateTableColumnModel)gates.getColumnModel()).changeColumnWidth(+10);
	TexQBrick.setBrickWidth(TexQBrick.WIDTH+10);
    }

    /**
     * method to decrease the column width of the columns in the GateTable. It also
     * adjusts the width of the TexQBrick class.
     */
    public void decreaseColumnWidth() {
	((GateTableColumnModel)gates.getColumnModel()).changeColumnWidth(-10);
	if (TexQBrick.WIDTH-10 >= 10) TexQBrick.setBrickWidth(TexQBrick.WIDTH-10);
    }

    /**
     * calculates the row height so that all qubits fit into the panel.
     */
    private int calculateRowHeight(int rows) {
	if (rows == 0) rows++;
	int availHeight = (viewPort.getSize().height-gates.getTableHeader().getPreferredSize().height);
	if (gates.getAutoResizeMode() == JTable.AUTO_RESIZE_OFF)
	    availHeight -= viewPort.getHorizontalScrollBar().getSize().height;

	int rowHeight = Math.max(new Double(Math.ceil(availHeight/rows)).intValue(),1);
	int tableHeight = rows*rowHeight;
	while (tableHeight > (availHeight-3)) {
	    rowHeight--;
	    tableHeight = rows*rowHeight;
	}
	return ((rowHeight < 1) ? 1: rowHeight);
    }


    /**
     * calculates the row height so that all qubits fit into the panel.
     */
    private int calculateRowHeight() {
	return calculateRowHeight(gates.getModel().getRowCount());
    }

    /**
     * adjusts the row height so that all qubits fit into the panel.
     */
    public void adjustRowHeight() {
	gates.setRowHeight(calculateRowHeight());
    }

    /**
     * is a workaround for JAVA bug 4226181, but I think it is not used anymore in
     * the application.
     */
    public void resize() {
	// workaround hack due to BUG 4226181
	gates.setSize(viewPort.getSize()) ;
	// end hack 
    }

    /**
     * resets the gatePanel and the computation
     */
    public void reset() {
	itemStateChanged(null);	
    }

    /**
     * wrapping method
     */
    public void addPropertyChangeListener(PropertyChangeListener l)
    {
	properties.addPropertyChangeListener(l);
    }

    /**
     * wrapping method
     */
    public void removePropertyChangeListener(PropertyChangeListener l)
    {
	properties.removePropertyChangeListener(l);
    }

    /**
     * update the timing properties.
     */
    protected void fireTimingProperties() {
	properties.firePropertyChange(elapsedTime, new Double(-1), 
				      new Double(timing.getElapsedTimeSec()));
	properties.firePropertyChange(avgTimePerGate, new Double(-1),
				      new Double(timing.getAvgTimePerStepMillis()));
	properties.firePropertyChange(remainingTimeEstimate, new Double(-1),
				      new Double(timing.getTimeEstimateForStepsSec(gates.getStepCount()-gates.getStep())));
    }

    /**
     * updates the statusbar and sets the icon for the computation control panel
     * correctly. 
     */
    public void qubitUpdate() {
	MathObject q = Mathlib.getVar(qubits);
	if (q != null) 
	    statusBar.setText(((Braket)q).toString(200));
	if (run == false) {
	    gates.getTableHeader().setReorderingAllowed(true);
	    runButton.setIcon(jaQuzzi.runIcon);
	    runButton.setToolTipText("run!");
	    run = true;
	}

    }

}

