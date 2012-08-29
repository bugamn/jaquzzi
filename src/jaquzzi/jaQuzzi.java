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

package jaquzzi;

import java.io.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.print.*;
import java.beans.*;
import java.net.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;

import mathlib.Argument;
import mathlib.BinaryOp;
import mathlib.Braket;
import mathlib.Decoherence;
import mathlib.Gate;
import mathlib.GateProperty;
import mathlib.MathObject;
import mathlib.Mathlib;
import mathlib.MathlibEvent;
import mathlib.Matrix;
import mathlib.Parse;
import mathlib.StringArgument;

import qsymbol2.ChartWindow;
import qsymbol2.DataWindow;
import qsymbol2.GateContainer;
import qsymbol2.GatePanel;
import qsymbol2.GateTableModel;
import qsymbol2.InfoPanel;
import qsymbol2.QVarTree;

/**
 * <p> This class is the central class of the jaQuzzi program. It is derived from the 
 * JApplet class. The program can be run as an applet (with reduced functionality) as 
 * well as 
 * an application. In the case of given command line parameters the program is executed 
 * without any graphical user interface. The two modes are called interactive mode and
 * batch mode. For documentation of the batch mode please refer to the information
 * given on the following website: http://www.physics.buffalo.edu/~phygons/jaQuzzi
 * </p>
 * The jaQuzzi program is based on 3 packages: 
 * <ul>
 * <li>The jaQuzzi package provides general parts of the GUI such as dialogs and 
 * action such as open, new, save.
 * <li>The qsymbol package provides the visalization of the mathlib package and 
 * implements all the quantum computation specififc details of the GUI.
 * <li>The mathlib package provides the math engine. It communicates with the GUI
 * through an event model similar to the one used by JAVA > 1.1.
 * </ul>
 * @see edu.buffalo.fs7.mathlib
 * @see edu.buffalo.fs7.qsymbol2
 * @see Dialogs
 */
public class jaQuzzi extends JApplet  {

    /** version string */
    public static final String version = "jaQuzzi 0.099RC";

    /** images are a little bit tricky if a program can be run as an applet as well
     * as an application. Therefore all the icons used in the jaQuzzi program are
     * public static.
     * @see jaQuzzi#initIcons()
     */
    public static ImageIcon newIcon;
    public static ImageIcon openIcon;
    public static ImageIcon saveIcon;
    public static ImageIcon saveasIcon;
    public static ImageIcon enlargeIcon;
    public static ImageIcon shrinkIcon;
    public static ImageIcon consoleIcon;
    public static ImageIcon chartIcon;
    public static ImageIcon dataIcon;
    public static ImageIcon propertyIcon;
    public static ImageIcon resetIcon;
    public static ImageIcon runIcon;
    public static ImageIcon stepbackwardIcon;
    public static ImageIcon stepforwardIcon;
    public static ImageIcon stopIcon;
    public static ImageIcon measureIcon;
    public static ImageIcon qubitupIcon;
    public static ImageIcon qubitdownIcon;
    public static ImageIcon expIcon;
    public static ImageIcon equalsIcon;

    /** frame that can hold the applet in case of a stand alone execution */
    private static JFrame f;

    /** hold the URL in case of applet mode*/
    public URL codeBase;

    /** popup menues */
    private JPopupMenu popupHeader;
    private JPopupMenu popupQubits;
    private JPopupMenu popupTable;

    private JComboBox qubitSelector;
    private JPanel centerPanel;
    private GatePanel gatePanel;

    private JFileChooser fileChooser;

    /** the currently opened file*/
    public static File file;
    private static boolean fileChanged = false;
    private ChangeListener changeListener;

    private InfoPanel infoPanel;

    /** holds a reference to the mathlib package*/
    private Mathlib mathlib;   
    /** the console window */ 
    private QVarTree varTree;

    // holds console window
    private JFrame console;

    /** holds chart window */
    public static JFrame chartWindowFrame;
    /** chart window */
    public static ChartWindow chartWindow;

    /** holds data window */
    public static JFrame dataWindowFrame;
    /** data window */
    public static DataWindow dataWindow;

    /**
     * init method of the jaQuzzi applet. It initializes a Mathlib object and 
     * instantiates the GatePanel class which is reseponsible for the graphical
     * representation of the mathlib package. It initializes the toolbar
     * as well as the popup menues and the dataWindow and chartWindow.
     * @see edu.buffalo.fs7.qsymbol2.GatePanel
     * @see edu.buffalo.fs7.qsymbol2.ChartWindow
     * @see edu.buffalo.fs7.qsymbol2.DataWindow
     */
    public void init(){
	initIcons();

	mathlib = new Mathlib();

        getContentPane().setLayout(new BorderLayout());
	
	centerPanel = new JPanel();

	centerPanel.setLayout(new BorderLayout());	
	centerPanel.add("North",createToolbar());
	//	centerPanel.add("East",createGateToolbar());

	file = null;
	gatePanel = new GatePanel(mathlib);
	Parse.fireMathlibEvent("gate0", null, "current circuit", new Gate(0), MathlibEvent.ADD);

	changeListener = new ChangeListener();
	gatePanel.addPropertyChangeListener(changeListener);
	gatePanel.gates.addPropertyChangeListener(changeListener);

	// add qubits
	gatePanel.addQubit();
	gatePanel.addQubit();
	gatePanel.addQubit();


	centerPanel.add("Center", gatePanel);
        getContentPane().add("Center", centerPanel);

        //Create the popup menu.
        popupHeader = new JPopupMenu();
	popupHeader.add(new AddGateAction());
	popupHeader.add(new InsertGateAction());
	popupHeader.add(new RemoveGateAction());
	popupHeader.addSeparator();
	popupHeader.add(new GroupGatesAction());
	popupHeader.add(new UnGroupGatesAction());
	popupHeader.addSeparator();
	popupHeader.add(new GatePropertyAction());

	popupTable = new JPopupMenu();
	popupTable.add(new NOTGateAction());
	popupTable.add(new PHASEGateAction());
	popupTable.add(new SIMPLEGateAction());
	popupTable.add(new CUSTOMGateAction());
	popupTable.add(new EXPERTGateAction());
	popupTable.addSeparator();
	popupTable.add(new RANDGateAction());
	popupTable.add(new MeasureGateAction());
	popupTable.add(new PrepareGateAction());

	JMenu qftMenu = new JMenu("QFT");
	qftMenu.add(new QFTGateAction());
	qftMenu.add(new InverseQFTGateAction());
	popupTable.add(qftMenu);
	popupTable.addSeparator();

	JMenu fixedSizeMenu = new JMenu("fixed size gates");
	fixedSizeMenu.add(new XChangeGateAction());
	fixedSizeMenu.add(new CompareGateAction());
	popupTable.add(fixedSizeMenu);


	popupQubits = new JPopupMenu();
	popupQubits.add(new AddQubitAction());
	popupQubits.add(new InsertQubitAction());
	popupQubits.add(new RemoveQubitAction());
	popupQubits.add(new RenameQubitAction());

        //Add listener to components that can bring up popup menus.
        MouseListener popupListener = new PopupListener();
	gatePanel.gates.addMouseListener(popupListener);
	gatePanel.gates.getTableHeader().addMouseListener(popupListener);
	gatePanel.inQubits.addMouseListener(popupListener);

	// mathlib tree
	varTree = new QVarTree(mathlib);
        console = new JFrame("math engine");
	console.getContentPane().add(varTree, BorderLayout.CENTER);
	console.pack();
        console.setSize(new Dimension(160,400));
        console.setVisible(false);

	// chart window
	int[] a = {0, 1, 2};
	chartWindow = new ChartWindow(gatePanel.gates, a);
	chartWindowFrame = new JFrame("chart center") {
		public void setVisible(boolean visible) {
		    super.setVisible(visible);
		    chartWindow.setVisible(visible);
		}
	    };
	chartWindowFrame.getContentPane().add(chartWindow);

	chartWindowFrame.pack();
	chartWindowFrame.setSize(new Dimension(400, 300));
	chartWindowFrame.setVisible(false);

	dataWindow = new DataWindow(gatePanel.gates, GatePanel.qubits, a);
	dataWindowFrame = new JFrame("data center") {
		public void setVisible(boolean visible) {
		    super.setVisible(visible);
		    dataWindow.setVisible(visible);
		}
	    };
	dataWindowFrame.getContentPane().add(dataWindow);
	dataWindowFrame.pack();
        dataWindowFrame.setSize(new Dimension(400,200));
        dataWindowFrame.setVisible(false);

	updateTitle();

    }

    /**
     * load all the icons used by the jaQuzzi program. The procedure is different for
     * the applet case and the application case.
     */
    protected void initIcons() {
	if (f == null) {
	    newIcon = new ImageIcon(getURL("resources/new.gif"));
	    openIcon= new ImageIcon(getURL("resources/open.gif"));
	    saveIcon = new ImageIcon(getURL("resources/save.gif"));
	    saveasIcon = new ImageIcon(getURL("resources/msave.gif"));
	    enlargeIcon = new ImageIcon(getURL("resources/fit_+.gif"));
	    shrinkIcon = new ImageIcon(getURL("resources/fit_-.gif"));
	    consoleIcon = new ImageIcon(getURL("resources/q.gif"));
	    chartIcon = new ImageIcon(getURL("resources/chart.gif"));
	    dataIcon = new ImageIcon(getURL("resources/enlarge.gif"));
	    propertyIcon = new ImageIcon(getURL("resources/properties.gif"));
	    resetIcon = new ImageIcon(getURL("resources/reset.gif"));
	    runIcon = new ImageIcon(getURL("resources/run.gif"));
	    stepbackwardIcon = new ImageIcon(getURL("resources/step_back.gif"));
	    stepforwardIcon = new ImageIcon(getURL("resources/step_forward.gif"));
	    stopIcon = new ImageIcon(getURL("resources/stop.gif"));
	    measureIcon = new ImageIcon(getURL("resources/measure.gif"));
	    qubitupIcon = new ImageIcon(getURL("resources/qubit_up.gif"));
	    qubitdownIcon = new ImageIcon(getURL("resources/qubit_down.gif"));
	    expIcon = new ImageIcon(getURL("resources/exp.gif"));
	    equalsIcon = new ImageIcon(getURL("resources/equals.gif"));
	}
	else {
	// applet is running standalone!
	    newIcon = new ImageIcon(this.getClass().getResource("resources/new.gif"));
	    openIcon= new ImageIcon(this.getClass().getResource("resources/open.gif"));
	    saveIcon = new ImageIcon(this.getClass().getResource("resources/save.gif"));
	    saveasIcon = new ImageIcon(this.getClass().getResource("resources/msave.gif"));
	    enlargeIcon = new ImageIcon(this.getClass().getResource("resources/fit_+.gif"));
	    shrinkIcon = new ImageIcon(this.getClass().getResource("resources/fit_-.gif"));
	    consoleIcon = new ImageIcon(this.getClass().getResource("resources/q.gif"));
	    chartIcon = new ImageIcon(this.getClass().getResource("resources/chart.gif"));
	    dataIcon = new ImageIcon(this.getClass().getResource("resources/enlarge.gif"));
	    propertyIcon = new ImageIcon(this.getClass().getResource("resources/properties.gif"));
	    resetIcon = new ImageIcon(this.getClass().getResource("resources/reset.gif"));
	    runIcon = new ImageIcon(this.getClass().getResource("resources/run.gif"));
	    stepbackwardIcon = new ImageIcon(this.getClass().getResource("resources/step_back.gif"));
	    stepforwardIcon = new ImageIcon(this.getClass().getResource("resources/step_forward.gif"));
	    stopIcon = new ImageIcon(this.getClass().getResource("resources/stop.gif"));
	    measureIcon = new ImageIcon(this.getClass().getResource("resources/measure.gif"));
	    qubitupIcon = new ImageIcon(this.getClass().getResource("resources/qubit_up.gif"));
	    qubitdownIcon = new ImageIcon(this.getClass().getResource("resources/qubit_down.gif"));
	    expIcon = new ImageIcon(this.getClass().getResource("resources/exp.gif"));
	    equalsIcon = new ImageIcon(this.getClass().getResource("resources/equals.gif"));
	}
    }

    /**
     * method to parse paramters from a command line parameter. The given String must
     * be of the form a:b:c:d, where a,b are double numbers and c,d integers.
     * (a = initial value, b = end value, c = steps inbetween, d = repetition per step).
     * @return the parsed parameters as a Parameter object.
     */
    protected static Parameter getParameterSet(String s) {
	StringTokenizer st = new StringTokenizer(s, ":", true);
	double start = -1; double end = -1; double steps = -1; int rep = -1;
	String token;
	try {
	    // start
	    if (st.hasMoreTokens()) {
		start = Double.parseDouble(st.nextToken());
		if (st.hasMoreTokens()) st.nextToken(); // dispose ':'
	    }
	    if (st.hasMoreTokens()) {
		token = st.nextToken();
		if (token.equals(":")) end = start;
		else end = Double.parseDouble(st.nextToken());
		if (st.hasMoreTokens()) st.nextToken(); // dispose ':'
	    } else { end = start; steps = 1;rep = 1; return new Parameter(start, end, steps, rep);}
	    if (st.hasMoreTokens()) {
		if (token.equals(":")) steps = (start == end) ? 1 : 2;
		else steps = Double.parseDouble(st.nextToken());
		if (st.hasMoreTokens()) st.nextToken(); // dispose ':'
	    } else { steps = 2; rep = 1; return new Parameter(start, end, steps, rep);}
	    if (st.hasMoreTokens()) {
		rep = Integer.parseInt(st.nextToken());
	    } else { rep = 1; }
	    return new Parameter(start, end, steps, rep);
	}
	catch (Exception e) {
	    return null;
	}
    }

    /**
     * this method is called when the jaQuzzi class is started from the command line.
     * It creates a frame in the case of no parameters given and put the applet into
     * the frame, or runs the jaQuzzi class in batch mode if parameters are given. 
     */
    public static void main(String s[]) {
	// okay quiet mode?!
	if (s.length != 0) {
	    StringTokenizer st;
	    File inputFile = null;
	    int mode = -1;
	    Parameter sigma = null;
	    Parameter rate = null;
	    Parameter decay = null;
	    int[] plotQubits = null;
	    int index;
	    int qubitState = 0;
	    int n = 0;

	    int decayQubit = -1;

	    double val, dec, noise;

	    // parse arguments
	    int token = 0;

	    System.out.println("jaQuzzi " + version + " (C) 2000 Felix Schuermann under GNU Public License");
	    // at least two more tokens have to be there!
	    while (token < s.length-2) {
		// option
		if (s[token].startsWith("-")) {
		    if (s[token].endsWith("mode")){
			token++;
			try {
			    mode = Integer.parseInt(s[token]);
			} catch (Exception e) {
			    System.out.println("error in parameter -mode");
			    System.exit(-1);
			}
			token++;
		    }
		    else if (s[token].endsWith("init")){
			token++;
			for (int i = 0; i < s[token].length(); i++) {
			    if (s[token].charAt(i)== '1') qubitState += BinaryOp.pow(2,s[token].length()-i-1);
			}
			token++;
		    }
		    else if (s[token].endsWith("plot")){
			token++;
			// count 1s
			index = 0;
			for (int i = 0; i < s[token].length(); i++) 
			    if (s[token].charAt(i) == '1') index++;

			plotQubits = new int[index];
			index = 0;
			for (int i = 0; i < s[token].length(); i++) {
			    if (s[token].charAt(i)== '1') plotQubits[index++] = i;
			}
			token++;
		    }
		    else if (s[token].endsWith("sigma")){
			token++;
			sigma = getParameterSet(s[token++]);
			if (sigma == null) {
			    System.out.println("error in parameter -sigma");
			    System.exit(-1);
			}
		    }
		    else if (s[token].endsWith("rate")){
			token++;
			rate = getParameterSet(s[token++]);
			if (rate == null) {
			    System.out.println("error in parameter -rate");
			    System.exit(-1);
			}
		    }
		    else if (s[token].endsWith("decay")){
			token++;
			decay = getParameterSet(s[token++]);
			if (decay == null) {
			    System.out.println("error in parameter -decay");
			    System.exit(-1);
			}
		    }
		    else if (s[token].endsWith("decbit")){
			token++;
			try {
			    decayQubit = Integer.parseInt(s[token++]);
			}
			catch (Exception e) {
			    System.out.println("error in parameter -decbit");
			    System.exit(-1);
			}
		    }
		    else {
			System.out.println("invalid parameter: " + s[token]);
			System.exit(-1);
		    }
		} 
		else {
		    System.out.println("invalid parameter: " + s[token]);
		    System.exit(-1);
		}
	    }
	    if (s.length-token == 1) inputFile = new File(s[s.length-1]);
	    else {
		System.out.println("missing file argument!");
		System.exit(-1);
	    }

	    if (inputFile.exists()) {
		Mathlib mathlib = new Mathlib();
		GateTableModel gateTableModel = new GateTableModel(mathlib);
		gateTableModel.primitiveLoadFromFile(inputFile);
		// initalization value given
		Braket qubits = (Braket) Mathlib.getVar("qubits");
		n = qubits.n;
		if (qubitState != 0) {
		    Parse.fireMathlibEvent("qubits", new Braket(qubitState, n), MathlibEvent.CHANGE);
		    Parse.fireMathlibEvent("refQubits", new Braket(qubitState, n), MathlibEvent.CHANGE);
		}

		// get implicit mode
		if (mode == -1) {
		    if (sigma != null && rate == null && decay == null) mode = 1;
		    else if (sigma == null && (rate != null || decay != null)) mode = 2;
		    else if (sigma != null && (rate != null || decay != null)) mode = 3;
		}

		// adjust parameters
		double tempVal;
		if (sigma == null) {
		    tempVal = gateTableModel.getCircuitProperty("sigma");
		    if (tempVal == -1) tempVal = 0;
		    sigma = new Parameter(tempVal, tempVal, 1, 1);
		}
		if (rate == null) {
		    tempVal = gateTableModel.getCircuitProperty("rate");
		    if (tempVal == -1) tempVal = 0.0001;
		    rate = new Parameter(tempVal, tempVal, 1, 1);
		}
		if (decay == null) {
		    tempVal = gateTableModel.getCircuitProperty("decay");
		    if (tempVal == -1) tempVal = 0.5;
		    decay = new Parameter(tempVal, tempVal, 1, 1);
		}

		gateTableModel.setCalcFidelity(true);
		// circuit presets
		if (mode == -1) {
		    System.out.println("mode: "+mode+" sigma: "+sigma.start+" rate: "+rate.start+" decay: "+decay.start);
		    gateTableModel.run();
		    gateTableModel.dumpData(inputFile, plotQubits);
		}
		// ideal mode
		else if (mode == 0) {
		    gateTableModel.setCircuitProperty("mode", 0);
		    System.out.println("mode: "+mode+" sigma: "+sigma.start+" rate: "+rate.start+" decay: "+decay.start);
		    gateTableModel.run();
		    gateTableModel.dumpData(inputFile, plotQubits);
		}
		// noise errors
		else if (mode == 1) {
		    gateTableModel.setCircuitProperty("mode", 1);
		    for (int c = 0; c < sigma.steps; c++) {
			if (sigma.steps == 1) val = sigma.start;
			else val = sigma.start+(sigma.end-sigma.start)/(sigma.steps-1)*c;
			gateTableModel.setCircuitProperty("sigma", val);
			for (int i = 0; i < sigma.rep; i++) {
			    System.out.println("mode: "+mode+" sigma: "+val+" rate: "+rate.start+" decay: "+decay.start +" rep: "+i);
			    gateTableModel.reset();
			    Parse.fireMathlibEvent("qubits", new Braket(qubitState, n), MathlibEvent.CHANGE);
			    Parse.fireMathlibEvent("refQubits", new Braket(qubitState, n), MathlibEvent.CHANGE);
			    gateTableModel.run();
			    gateTableModel.dumpData(inputFile, plotQubits);
			}
		    }
		}
		// decoherence errors
		else if (mode == 2) {
		    Decoherence.presetQubit = decayQubit;
		    gateTableModel.setCircuitProperty("mode", 2);
		    for (int c = 0; c < rate.steps; c++) {
			if (rate.steps == 1) val = rate.start;
			else val = rate.start+(rate.end-rate.start)/(rate.steps-1)*c;
			gateTableModel.setCircuitProperty("rate", val);
			for (int i = 0; i < rate.rep; i++) {
			    for (int d = 0; d < decay.steps; d++) {
				if (decay.steps == 1) tempVal = decay.start;
				else tempVal = decay.start+(decay.end-decay.start)/(decay.steps-1)*d;
				gateTableModel.setCircuitProperty("decay", tempVal);
				for (int k = 0; k < decay.rep; k++) {
				    System.out.println("mode: "+mode+" sigma: "+sigma.start+" rate: "+val+" decay: "+tempVal +" rep: "+i+"-"+k);
				    gateTableModel.reset();
				    Parse.fireMathlibEvent("qubits", new Braket(qubitState, n), MathlibEvent.CHANGE);
				    Parse.fireMathlibEvent("refQubits", new Braket(qubitState, n), MathlibEvent.CHANGE);
				    gateTableModel.run();
				    gateTableModel.dumpData(inputFile, plotQubits);
				}
			    }
			}
		    }
		}
		// both errors
		else if (mode == 3) {
		    Decoherence.presetQubit = decayQubit;
		    gateTableModel.setCircuitProperty("mode", 3);
		    for (int b = 0; b < sigma.steps; b++) {
			if (sigma.steps == 1) noise = sigma.start;
			else noise = sigma.start + (sigma.end-sigma.start)/(sigma.steps-1)*b;
			gateTableModel.setCircuitProperty("sigma", noise);
			for (int j = 0; j < sigma.rep; j++) {
			    for (int c = 0; c < rate.steps; c++) {
				if (rate.steps == 1) val = rate.start;
				else val = rate.start+(rate.end-rate.start)/(rate.steps-1)*c;
				gateTableModel.setCircuitProperty("rate", val);
				for (int i = 0; i < rate.rep; i++) {
				    for (int d = 0; d < decay.steps; d++) {
					if (decay.steps == 1) dec = decay.start;
					else dec = decay.start+(decay.end-decay.start)/(decay.steps-1)*d;
					gateTableModel.setCircuitProperty("decay", dec);
					for (int k = 0; k < decay.rep; k++) {
					    System.out.println("mode: "+mode+" sigma: "+noise+" rate: "+val+" decay: "+dec +" rep: "+j+"-"+i+"-"+k);
					    gateTableModel.reset();
					    Parse.fireMathlibEvent("qubits", new Braket(qubitState, n), MathlibEvent.CHANGE);
					    Parse.fireMathlibEvent("refQubits", new Braket(qubitState, n), MathlibEvent.CHANGE);
					    gateTableModel.run();
					    gateTableModel.dumpData(inputFile, plotQubits);
					}
				    }
				}
			    }
			}
		    }
		}
		System.exit(0);
	    }
	    else {
		System.out.println("file not existing!");
		System.exit(-1);
	    }
	}
	else {
	    f = new JFrame(version);
	    f.addWindowListener(new WindowAdapter() {
		    public void windowClosing(WindowEvent e) {
			/*
			  if (fileChanged) {
			  if (JOptionPane.showConfirmDialog(f, "Save before closing?", "jaQUzzi", YES_NO_OPTION) == JOptionPane.YES_OPTION) {
			  
			  }
			*/
			System.exit(0);
		    }
		});
	    JApplet applet = new jaQuzzi();

	    f.getContentPane().add("Center", applet);
	    applet.init();
	    f.pack();
	    f.setSize(new Dimension(600,450));
	    f.show();
	}
    }

    /**
     * Create the toolbar.  
     */
    private Component createToolbar() {
	JToolBar toolbar = new JToolBar();
	AbstractAction action;
	// new button
	action = new NewAction();
	toolbar.add(action);

	// open button
	action = new OpenAction();
	if (f== null) action.setEnabled(false);
	toolbar.add(action);

	// save button
	action = new SaveAction();
	if (f == null) action.setEnabled(false);
	toolbar.add(action);

	// save button
	action = new SaveAsAction();
	if (f == null) action.setEnabled(false);
	toolbar.add(action);

	// separator
	toolbar.add(Box.createHorizontalStrut(5));

	// copy button
	//	toolbar.add(new CopyAction());
	// cut button 
	//	toolbar.add(new CutAction());
	// paste button
	//	toolbar.add(new PasteAction());

	// separator
	toolbar.add(Box.createHorizontalStrut(5));

	// reset button
	toolbar.add(new EnlargeAction());
	// reset button
	toolbar.add(new ShrinkAction());

	// separator
	toolbar.add(Box.createHorizontalStrut(5));

	// console button
	toolbar.add(new ConsoleAction());

	// chart button
	toolbar.add(new ChartAction());

	JPanel p = new JPanel();
	p.setLayout(new BorderLayout());

	JToolBar tb = new JToolBar();
	tb.setFloatable(false);
	infoPanel = new InfoPanel();
	infoPanel.put(GatePanel.step, new Integer(0));
	infoPanel.put(GatePanel.stepCount, new Integer(1));
	infoPanel.put(GatePanel.elapsedTime, new Double(0));
	infoPanel.put(GatePanel.remainingTimeEstimate, new Double(0));
	infoPanel.put(GatePanel.avgTimePerGate, new Double(0));
	infoPanel.put(GatePanel.fidelity, new Double(1));
	infoPanel.put("Author", "Felix Schuermann");
	infoPanel.put("(C) 2000", "GNU PL");
	tb.add(new PropertyAction());
	tb.add(infoPanel);
	p.add(tb, "East");
	toolbar.add(p);

	toolbar.add(Box.createHorizontalGlue());

	return toolbar;
    }

    /**
     * in case of jaQuzzi being executed standalone, this method provides title
     * information such as the file name.
     */
    public static void updateTitle() {
	if (f != null) {
	    f.setTitle(version + "[" + ((file == null)? "new_circuit": file.getName()) + "]" 
		       + ((fileChanged)? "*":""));
	}
    }

    /**
     * One day, JApplet will make this method obsolete. 
     */
    protected URL getURL(String filename) {
        URL url = null;
        if (codeBase == null) {
            codeBase = getCodeBase();
        }

        try {
            url = new URL(codeBase, filename);
        } catch (java.net.MalformedURLException e) {
            System.out.println("Couldn't create image: badly specified URL");
            return null;
        }

        return url;
    }
    
    /**
     * class that listens to changes in the gatePanel. E.g., when a gate was added,
     * this class is invoced. It updates the InfoPanel as well as the title.
     * @see edu.buffalo.fs7.qsymbol2.InfoPanel
     */
    class ChangeListener implements java.beans.PropertyChangeListener {
	public void propertyChange(PropertyChangeEvent e) {
	    infoPanel.set(e.getPropertyName(), e.getNewValue());
	    infoPanel.repaint();
	    // table modified! exception: step change...
	    if (e.getPropertyName().equals(GatePanel.qubitUpdate)) {
		gatePanel.qubitUpdate();
	    }
	    else if (e.getPropertyName().equals(GatePanel.stepCount) || 
		     e.getPropertyName().equals(GatePanel.table)) {
		if (fileChanged == false) {
		    fileChanged = true; updateTitle();
		}
	    }
	}
    }

    /**
     * this class listens to mouse events in order to bring up the appropriate popup
     * menu.
     */
    class PopupListener extends MouseAdapter {
	public void mouseClicked(MouseEvent e) {
	    if(e.getClickCount() == 2) {
		if (gatePanel.gates.getSelectedColumnCount() == 1) {
		    int columns[] = gatePanel.gates.getSelectedColumns();
		    GateContainer gateContainer = gatePanel.gates.getGateContainer(columns[0]);
		    if (gateContainer.isLeafContainer()) {
			String gateName = gateContainer.getFullName();
			Matrix m = gateContainer.getGate().getMatrix();
			String matrixName = gateContainer.getGate().getMatrixName();
			boolean expression = !(Parse.checkForValidVarName(matrixName));
			if (m != null) {
			    String name = (expression)? "NO_VARIABLE":matrixName;
			    boolean[][] mask = new boolean[m.n()][m.m()];
			    for (int i = 0; i < m.n(); i++) {
				for (int j = 0; j < m.m(); j++) {
				    mask[i][j] = false;
				}
			    }
			    String newName = Dialogs.mathInputDialog(f, "gate", name,m, mask, true);
			}
		    }
		    else JOptionPane.showMessageDialog(f, "gate group!");
		}
	    }
	}

        public void mousePressed(MouseEvent e) {
            maybeShowPopup(e);
        }

	/**
	 * this method is overwritten to bring up a popup menu in the case of a right
	 * mouse click, but not in case of a left mouse click. The selection of the
	 * column is done in the standars fashion (SHIFT, CTRL, etc)
	 */
        public void mouseReleased(MouseEvent e) {
	    if (e.isPopupTrigger() == false && e.getComponent() instanceof JTableHeader){
		int clickCol = gatePanel.gates.columnAtPoint(e.getPoint());
		ListSelectionModel selModel = gatePanel.gates.getColumnModel().getSelectionModel();
		if (e.isShiftDown()) {
		    int minCol = selModel.getMinSelectionIndex();
		    int maxCol = selModel.getMaxSelectionIndex();
		    gatePanel.gates.setRowSelectionInterval(0, gatePanel.gates.getRowCount()-1);
		    if (clickCol >= minCol) {
			selModel.setSelectionInterval(minCol, clickCol);

		    }
		    else if (clickCol <= maxCol) {
			selModel.setSelectionInterval(clickCol, maxCol);
		    }
		}
		else if (e.isControlDown()) {
		    if (selModel.isSelectedIndex(clickCol)) {
			selModel.removeSelectionInterval(clickCol, clickCol);
		    }
		    else {
			gatePanel.gates.setRowSelectionInterval(0, gatePanel.gates.getRowCount()-1);
			selModel.addSelectionInterval(clickCol, clickCol);
		    }
		}
		else {
		    gatePanel.gates.setRowSelectionInterval(0, gatePanel.gates.getRowCount()-1);
		    selModel.setSelectionInterval(clickCol, clickCol);
		}
	    }
            else maybeShowPopup(e);
        }

        private void maybeShowPopup(MouseEvent e) {
            if (e.isPopupTrigger()) {
		ListSelectionModel selModel = gatePanel.gates.getColumnModel().getSelectionModel();

		// TableHeader selected
		if (e.getComponent() instanceof JTableHeader) {
		    gatePanel.gates.setRowSelectionInterval(0, gatePanel.gates.getRowCount()-1);
		    int clickCol = gatePanel.gates.columnAtPoint(e.getPoint());
		    int minCol = selModel.getMinSelectionIndex();
		    int maxCol = selModel.getMaxSelectionIndex();
		    if (e.isShiftDown()) {
			if (clickCol > minCol) {
			    selModel.setSelectionInterval(minCol, clickCol);
			}
			else if (clickCol < maxCol) {
			    selModel.setSelectionInterval(clickCol, maxCol);
			}
		    }
		    else if (e.isControlDown()) {
			selModel.addSelectionInterval(clickCol, clickCol);
		    }
		    else if (gatePanel.gates.isColumnSelected(clickCol) == false 
			     || minCol == -1){
			selModel.setSelectionInterval(clickCol, clickCol);
		    }
		    popupHeader.show(e.getComponent(), e.getX(), e.getY());
		}
		// qubit selected
		else if (e.getComponent() instanceof JPanel) {
		    int row = gatePanel.inQubits.qubitAtPoint(e.getPoint());
		    gatePanel.gates.setRowSelectionInterval(row, row);

		    selModel.setSelectionInterval(0, gatePanel.gates.getColumnCount()-1);

		    popupQubits.show(e.getComponent(), e.getX(), e.getY());
		}
		else {
		    if (gatePanel.gates.getSelectedRowCount() == 0 && gatePanel.gates.getSelectedColumnCount() == 0) {
			gatePanel.gates.setRowSelectionInterval(gatePanel.gates.rowAtPoint(e.getPoint()), gatePanel.gates.rowAtPoint(e.getPoint()));
			selModel.setSelectionInterval(gatePanel.gates.columnAtPoint(e.getPoint()), gatePanel.gates.columnAtPoint(e.getPoint()));
		    }
		    int[] columns = gatePanel.gates.getSelectedColumns();
		    boolean isGroup = false;
		    for (int i = 0; i < columns.length; i++) {
			if (((GateContainer)gatePanel.gates.getValueAt(0,columns[i])).isLeafContainer() == false)
			    isGroup = true;
		    }

		    if (isGroup == false) {
 			MenuElement[] entries = popupTable.getSubElements();
			if (entries != null) {
			    for (int i = 0; i < entries.length; i++) {
				((JMenuItem) entries[i]).setEnabled(true);
			    }
			}
		    }
		    else {
 			MenuElement[] entries = popupTable.getSubElements();
			if (entries != null) {
			    for (int i = 0; i < entries.length; i++) {
				((JMenuItem) entries[i]).setEnabled(false);
			    }
			}
		    }
		    popupTable.show(e.getComponent(), e.getX(), e.getY());
		}
            }
        }
    }


    // actions

    /**
     * This class represents the "new circuit" command. It is derived from the 
     * AbstractAction class.
     */
    class NewAction extends AbstractAction {
	NewAction() {
 	    super("", newIcon);
	    putValue(Action.SHORT_DESCRIPTION, "create a new circuit");
	}

	public void actionPerformed(ActionEvent actionEvent) {
	    if (gatePanel.gates.isCalculationRunning() == true) return;
	    // get rid of all the gates
	    gatePanel.setFidelity(false);
	    gatePanel.gates.flushCurrentCircuit();

	    // add gate
	    Parse.fireMathlibEvent("gate0", null, "current circuit", new Gate(0), MathlibEvent.ADD);
	    gatePanel.addQubit();
	    gatePanel.addQubit();
	    gatePanel.addQubit();

	    gatePanel.resize();
	    fileChanged = false;
	    file = null;
	    updateTitle();
	}
    }

    /**
     * This class represents the "load circuit" command. It is derived from the 
     * AbstractAction class.
     */
    class OpenAction extends AbstractAction {
	OpenAction() {
 	    super("", openIcon);
	    putValue(Action.SHORT_DESCRIPTION, "load a circuit");
	}

	public void actionPerformed(ActionEvent actionEvent) {
	    if (gatePanel.gates.isCalculationRunning() == true) return;
	    JaqFileFilter ff = new JaqFileFilter();
	    fileChooser = new JFileChooser(".");
	    fileChooser.setDialogType(JFileChooser.OPEN_DIALOG);
	    fileChooser.addChoosableFileFilter(ff);
	    fileChooser.addChoosableFileFilter(fileChooser.getAcceptAllFileFilter());
	    fileChooser.setFileFilter(ff);
	    fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		
	    int retval = fileChooser.showDialog(f, null);
	    if(retval == JFileChooser.APPROVE_OPTION) {
		file = fileChooser.getSelectedFile();
		if (file != null) {
		    gatePanel.setFidelity(false);
		    gatePanel.gates.loadModelFromFile(file);
		    
		    gatePanel.adjustRowHeight();
		    gatePanel.resize();
			
		    fileChanged = false;
		    updateTitle();
		    }
	    }
	}
    }

    /**
     * This class represents the "save circuit" command. It is derived from the 
     * AbstractAction class.
     */
    class SaveAction extends AbstractAction {
	boolean ask;
	SaveAction() {
 	    super("", saveIcon);
	    putValue(Action.SHORT_DESCRIPTION, "save the circuit");
	    ask = false;
	}

	public void actionPerformed(ActionEvent actionEvent) {
	    if (gatePanel.gates.isCalculationRunning() == true) return;
	    if (ask || file == null) {
		JaqFileFilter ff = new JaqFileFilter();
		JaqFileFilter texff = new JaqFileFilter("tex", "LaTeX files (*.tex)");
		fileChooser = new JFileChooser(".");
		fileChooser.setDialogType(JFileChooser.SAVE_DIALOG);
		fileChooser.addChoosableFileFilter(ff);
		fileChooser.addChoosableFileFilter(texff);
		fileChooser.setFileFilter(ff);
		fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

		int retval = fileChooser.showDialog(f, null);
		if(retval == JFileChooser.APPROVE_OPTION) {
		    file = fileChooser.getSelectedFile();
		    if (file != null) {
			if (fileChooser.getFileFilter() == ff) {
			    if (ff.getExtension(file) == null) 
				file = new File(file.getPath()+".jaq");
			    gatePanel.gates.saveModelToFile(file);
			    fileChanged = false; updateTitle();
			}
			else {
			    if (ff.getExtension(file) == null) 
				file = new File(file.getPath()+".tex");
			    gatePanel.gates.saveTableToTex(file);
			}
		    }
		}
	    }
	    else {
		gatePanel.gates.saveModelToFile(file);
		fileChanged = false; updateTitle();
	    }
	}
    }

    /**
     * This class represents the "save circuit as" command. It is derived from the 
     * AbstractAction class.
     */
    class SaveAsAction extends SaveAction {
	SaveAsAction() {
	    super();
	    ask = true;
	    putValue(Action.SHORT_DESCRIPTION, "save as");
	    putValue(Action.SMALL_ICON, saveasIcon);
	}
    }

    /**
     * This class represents the "make columns wider" command. It is derived from the 
     * AbstractAction class.
     */
    class EnlargeAction extends AbstractAction {
	EnlargeAction(){
	    super("", enlargeIcon);
	    putValue(Action.SHORT_DESCRIPTION, "make gates wider");
	}

	public void actionPerformed(ActionEvent e) {
	    gatePanel.increaseColumnWidth();
	}
    }

    /**
     * This class represents the "make columns narrower" command. It is derived from the 
     * AbstractAction class.
     */
    class ShrinkAction extends AbstractAction {
	ShrinkAction(){
	    super("", shrinkIcon);
	    putValue(Action.SHORT_DESCRIPTION, "make gates narrower");
	}

	public void actionPerformed(ActionEvent e) {
	    gatePanel.decreaseColumnWidth();
	}
    }



    /**
     * This class represents the "console" command. It is derived from the 
     * AbstractAction class.
     */
    class ConsoleAction extends AbstractAction {
	ConsoleAction(){
	    super("", consoleIcon);
	    putValue(Action.SHORT_DESCRIPTION, "show the math engine console");
	}

	public void actionPerformed(ActionEvent e) {
	    if (console.isVisible()) console.setVisible(false);
	    else console.setVisible(true);
	}
    }

    /**
     * This class represents the "chartWindow" command. It is derived from the 
     * AbstractAction class.
     */
    class ChartAction extends AbstractAction {
	ChartAction() {
	    super("", chartIcon);
	    putValue(Action.SHORT_DESCRIPTION, "plot probalility chart");
	}

	public void actionPerformed(ActionEvent e) {
	    if (gatePanel.gates.getSelectedRowCount() > 0 ) {
		int[] rows = gatePanel.gates.getSelectedRows();
		chartWindow.setQubits(rows);
	    }
	    else chartWindow.setQubits(null);
	    chartWindowFrame.setVisible(true);
	    chartWindowFrame.repaint();
	}
    }

    /**
     * This class represents the "circuit property" command. It is derived from the 
     * AbstractAction class.
     */
    class PropertyAction extends AbstractAction {
	PropertyAction() {
	    super("", propertyIcon);
	    putValue(Action.SHORT_DESCRIPTION, "circuit properties");
	}

	public void actionPerformed(ActionEvent actionEvent) {
	    GateProperty circuitProperties = Dialogs.circuitPropertyDialog(f,
						  gatePanel.gates.getSimulationMode());
	    if (circuitProperties != null) {
		int action;
		if (Mathlib.getVar("circuit_properties") == null)
		    action = MathlibEvent.ADD;
		else action = MathlibEvent.CHANGE;
		Parse.fireMathlibEvent("circuit_properties", null, "current circuit", 
				       circuitProperties, action);
	    }
	}
    }


    // qubit actions
    /**
     * This class represents the "add qubit" command. It is derived from the 
     * AbstractAction class.
     */
    class AddQubitAction extends AbstractAction {
	AddQubitAction() {
	    super("add qubit");
	    putValue(Action.SHORT_DESCRIPTION, "add a qubit to the circuit");
	}

	public void actionPerformed(ActionEvent actionEvent) {
	    gatePanel.addQubit();
	}
    }

    /**
     * This class represents the "insert qubit" command. It is derived from the 
     * AbstractAction class.
     */
    class InsertQubitAction extends AbstractAction {
	InsertQubitAction() {
	    super("insert qubit");
	    putValue(Action.SHORT_DESCRIPTION, "insert a qubit into the circuit");
	}

	public void actionPerformed(ActionEvent actionEvent) {
	    if (gatePanel.gates.getSelectedRowCount() == 1 ) {
		int[] rows = gatePanel.gates.getSelectedRows();

		gatePanel.insertQubit(rows[0]);
	    }
	    else {
		JOptionPane.showMessageDialog(f, "Please select only one row (before which the qubit can be inserted)!");
	    }
	}
    }

    /**
     * This class represents the "rename qubit" command. It is derived from the 
     * AbstractAction class.
     */
    class RenameQubitAction extends AbstractAction {
	RenameQubitAction() {
	    super("rename qubit...");
	    putValue(Action.SHORT_DESCRIPTION, "rename the selected qubit");
	}
	public void actionPerformed(ActionEvent e) {
	    int[] rows = gatePanel.gates.getSelectedRows();
	    if (rows.length == 1 ) {
		String var = JOptionPane.showInputDialog(f, "Rename \"" + gatePanel.gates.getQubitName(rows[0]) + "\" to: ");
		if (var != null) {
		    Parse.fireMathlibEvent(gatePanel.gates.getQubitVar(rows[0]), null, 
					   "current circuit", new StringArgument(var), 
					   MathlibEvent.CHANGE);
		}

	    }
	    else {
		JOptionPane.showMessageDialog(f, "Please select only one qubit!");
	    }
	}
    }

    /**
     * This class represents the "remove qubit" command. It is derived from the 
     * AbstractAction class.
     */
    class RemoveQubitAction extends AbstractAction {
	RemoveQubitAction() {
	    super("remove qubit");
	    putValue(Action.SHORT_DESCRIPTION, "remove the selected qubits");
	}
	
	public void actionPerformed(ActionEvent actionEvent) {
	    if (gatePanel.gates.getSelectedRowCount() > 0 ) {
		if (gatePanel.gates.getRowCount() == 1) {
		    JOptionPane.showMessageDialog(f, "Sorry, can't delete last qubit! There must be at least one qubit left!");
		    return;
		}

		int[] rows = gatePanel.gates.getSelectedRows();
		for (int k = rows.length-1; k >= 0; k--) {	
		    gatePanel.removeQubit(rows[k]);
		}
	    }
	    else {
		JOptionPane.showMessageDialog(f, "Please select a qubit!");
	    }
	}
    }


    // ------------------------------------
    // gate actions

    /**
     * This class represents the "group gates" command. It is derived from the 
     * AbstractAction class. 
     */
    class GroupGatesAction extends AbstractAction {
	GroupGatesAction() {
	    super("group");
	    putValue(Action.SHORT_DESCRIPTION, "group gates");
	}

	public void actionPerformed(ActionEvent actionEvent) {
	    if (gatePanel.gates.getSelectedColumnCount() <= 1) {
		JOptionPane.showMessageDialog(f, "need to select more than one gate!");
	    }
	    else { 
		gatePanel.gates.reset();
		int[] columns = gatePanel.gates.getSelectedColumns();
		int min = Integer.MAX_VALUE;
		int max = 0;

		for (int i = 0; i < columns.length; i++) {
		    min = (columns[i] < min)? columns[i]:min;
		    max = (columns[i] > max)? columns[i]:max;
		}

		gatePanel.gates.getColumnModel().getSelectionModel().
		    setSelectionInterval(min, max);
			
		String gates = new String("group_");
		for (int i = 0; i < columns.length; i++) 
		    gates = gates.concat(gatePanel.gates.getGateContainerName(columns[i]));
		// continuous block!!
		GateContainer gc;
		if ((max-min) == columns.length-1) {
		    GateContainer newGroup = new GateContainer(null, gates, null);
		    for (int i = columns.length-1; i>= 0; i--) {
			gc = gatePanel.gates.getGateContainer(columns[i]);
			gatePanel.gates.unregisterGateContainer(columns[i]);
			newGroup.insertChildContainer(gc, 0);
		    }
		    gatePanel.gates.registerGateContainer(newGroup, columns[0]);

		    gatePanel.gates.reset();
		    gatePanel.adjustRowHeight();
		    gatePanel.revalidate();
		    gatePanel.repaint();
		}
		else JOptionPane.showMessageDialog(f,"Only neighboring gates can be grouped!");
	    }
	}
    }

    /**
     * This class represents the "ungroup gates" command. It is derived from the 
     * AbstractAction class. In order to prevent name conflicts of gateContainers
     * this class renames gateContainers that become ungroup in case of an already
     * existing name.
     */
    class UnGroupGatesAction extends AbstractAction {
	UnGroupGatesAction() {
	    super("ungroup");
	    putValue(Action.SHORT_DESCRIPTION, "ungroup gates");
	}

	public void actionPerformed(ActionEvent actionEvent) {
	    if (gatePanel.gates.getSelectedColumnCount() == 1) {
		int[] columns = gatePanel.gates.getSelectedColumns();
		GateContainer groupCont = gatePanel.gates.getGateContainer(columns[0]);
		if (groupCont.isLeafContainer()== false) {

		    gatePanel.gates.reset();
		    gatePanel.gates.unregisterGateContainer(columns[0]);
		    Enumeration childs = groupCont.ungroup();
		    GateContainer child;
		    int j = 0;
		    while (childs.hasMoreElements()) {
			child = (GateContainer)childs.nextElement();
			gatePanel.gates.registerGateContainer(child, columns[0]+j);
			j++;
		    }

		    gatePanel.adjustRowHeight();
		    gatePanel.revalidate();
		    gatePanel.repaint();
		} 
		else {
		    JOptionPane.showMessageDialog(f, "Can't ungroup single gate");
		}
	    }
	    else {
		JOptionPane.showMessageDialog(f, "Please select only one group!");
	    }
	}
    }

    /**
     * This class represents the "add gate" command. It is derived from the 
     * AbstractAction class.
     */
    class AddGateAction extends AbstractAction {
	AddGateAction() {
	    super("Add gate");
	    putValue(Action.SHORT_DESCRIPTION, "add a gate to the circuit");
	}

	public void actionPerformed(ActionEvent actionEvent) {
	    gatePanel.addGateContainer();
	}
    }
    
    /**
     * This class represents the "insert gate" command. It is derived from the 
     * AbstractAction class.
     */
    class InsertGateAction extends AbstractAction {
	InsertGateAction() {
	    super("Insert gate");
	    putValue(Action.SHORT_DESCRIPTION, "insert a gate to the circuit");
	}

	public void actionPerformed(ActionEvent actionEvent) {
	    if (gatePanel.gates.getSelectedColumnCount() == 1) {
		int[] columns = gatePanel.gates.getSelectedColumns();
		gatePanel.insertGateContainer(columns[0]);
	    }

	}
    }

    /**
     * This class represents the "remove gate" command. It is derived from the 
     * AbstractAction class.
     */
    class RemoveGateAction extends AbstractAction {
	RemoveGateAction() {
	    super("Remove gate");
	    putValue(Action.SHORT_DESCRIPTION, "remove the selected gate");
	}

	public void actionPerformed(ActionEvent actionEvent) {
	    int[] columns = gatePanel.gates.getSelectedColumns();
	    String gates = new String("");

	    if (gatePanel.gates.getSelectedColumnCount() > 0) {
		if (gatePanel.gates.getColumnCount() == 1) {
		    JOptionPane.showMessageDialog(f, "Sorry, last gate cannot be removed!");
		    return;
		}
		else if (gatePanel.gates.getSelectedColumnCount() == gatePanel.gates.getColumnCount() ) {
		    JOptionPane.showMessageDialog(f, "Sorry, can't remove all gates!");
		    return;
		}

		for (int i = 0; i < columns.length; i++) {
		    gates = gates.concat(gatePanel.gates.getGateContainerName(columns[i]) + " ");
		}
		if (JOptionPane.showConfirmDialog(f, "Really delete gates: \n" 
				     + gates + "?") == JOptionPane.YES_OPTION) {
		    if (columns[0] <= gatePanel.gates.getStepColumn())
			gatePanel.reset();
										   
		    for (int i = columns.length-1; i >= 0; i--) {
			gatePanel.removeGateContainer(columns[i]);
		    }

		    gatePanel.gates.clearSelection();
		}
	    }

	}
    }

    /**
     * This class represents the "gate property" command. It is derived from the 
     * AbstractAction class.
     */
    class GatePropertyAction extends AbstractAction {
	GatePropertyAction() {
	    super("properties...");
	    putValue(Action.SHORT_DESCRIPTION, "set gate properties");
	}
	public void actionPerformed(ActionEvent actionEvent) {
	    if (gatePanel.gates.getSelectedColumnCount() == 1) {
		int[] columns = gatePanel.gates.getSelectedColumns();
		GateContainer gc = gatePanel.gates.getGateContainer(columns[0]);

		GateProperty gp = Dialogs.gatePropertyDialog(f, gc.getFullName());
		if (gp != null) {
		    Argument a = gp.removeProperty("name");
		    if (a != null) 
			gatePanel.renameGateContainer(columns[0], a.toString());
		    gc.setPropertyObject(gp);
		    gatePanel.reset();
		    gatePanel.revalidate();
		    gatePanel.repaint();
		}
	    }
	    else {
		JOptionPane.showMessageDialog(f, "Please select only one column!");
	    }
	}
    }


    // gate actions

    /**
     * 
     */
    class NOTGateAction extends AbstractAction {
	NOTGateAction() {
	    super("not/c-not");
	    putValue(Action.SHORT_DESCRIPTION, "create NOT/cNOT gate");
	}

	public void actionPerformed(ActionEvent actionEvent) {
	    int[] columns = gatePanel.gates.getSelectedColumns();
	    if (gatePanel.gates.getSelectedColumnCount() <= 0)
		JOptionPane.showMessageDialog(f, "Please select a gate first!");
	    else {
		GateContainer gateContainer;
		String gate_descr = gatePanel.gates.translateSelectionTogate_descr(1);
		for (int i = 0; i < columns.length; i++) {
		    gateContainer = gatePanel.gates.getGateContainer(columns[i]);

		    if (gateContainer.isLeafContainer())
			Parse.fireMathlibEvent(gateContainer.getFullName(),null, "current circuit", new Gate(gate_descr, "NOT"), MathlibEvent.CHANGE);
		    else JOptionPane.showMessageDialog(f, "Can't modify grouped gates. Skipping group \""+ gateContainer.getTopLevelName()+ "\"!");
	
		}
	    }
	}
    }

    class PrepareGateAction extends AbstractAction {
	PrepareGateAction() {
	    super("prepare");
	    putValue(Action.SHORT_DESCRIPTION, " preparation of single qubit");
	}

	public void actionPerformed(ActionEvent actionEvent) {
	    int[] columns = gatePanel.gates.getSelectedColumns();
	    if (gatePanel.gates.getSelectedColumnCount() <= 0)
		JOptionPane.showMessageDialog(f, "Please select a gate first!");
	    else {
		int[] rows = gatePanel.gates.getSelectedRows();
		if (rows.length > columns.length) {
		    if (JOptionPane.showConfirmDialog(f, "Insufficient number of gates selected\nfor the number of qubits chosen to be measured! \nAdd required gates and proceed?") == JOptionPane.YES_OPTION) {

			// insert required gates
			for (int i = 0; i < rows.length-columns.length; i++) {
			    gatePanel.insertGateContainer(columns[columns.length-1]+i+1);

			}

			//restore selection (necessary since jdk1.3)
			for (int i = 0; i < columns.length; i++) {
			    gatePanel.gates.getColumnModel().getSelectionModel().
				addSelectionInterval(columns[i],
						     columns[i]);
			}
			gatePanel.gates.getColumnModel().getSelectionModel().
			    addSelectionInterval(columns[columns.length-1]+1,
				columns[columns.length-1]+rows.length-columns.length);

			// re-read columns
			columns = gatePanel.gates.getSelectedColumns();
		    }
		    else return;
		}

		int n = gatePanel.gates.getRowCount();
		char[] gate_descr = new char[n];
		// for each column
		for (int i = 0; i < rows.length; i++) {
		    for (int k = 0; k < n; k++) gate_descr[k] = '-';
		    gate_descr[rows[i]] = 'u';

		    // Attention!! gatecontainer must be single gate container!!!
		    Parse.fireMathlibEvent(
		        gatePanel.gates.getGateContainerName(columns[i]), null, "current circuit",
			new Gate(new String(gate_descr), null), 
			MathlibEvent.CHANGE);
		}
	    }
	}
    }

    class MeasureGateAction extends AbstractAction {
	MeasureGateAction() {
	    super("partial measurement");
	    putValue(Action.SHORT_DESCRIPTION, "create partial measurement gate");
	}

	public void actionPerformed(ActionEvent actionEvent) {
	    int[] columns = gatePanel.gates.getSelectedColumns();
	    if (gatePanel.gates.getSelectedColumnCount() <= 0)
		JOptionPane.showMessageDialog(f, "Please select a gate first!");
	    else {
		int[] rows = gatePanel.gates.getSelectedRows();
		if (rows.length > columns.length) {
		    if (JOptionPane.showConfirmDialog(f, "Insufficient number of gates selected\nfor the number of qubits chosen to be measured! \nAdd required gates and proceed?") == JOptionPane.YES_OPTION) {

			// insert required gates
			for (int i = 0; i < rows.length-columns.length; i++) {
			    gatePanel.insertGateContainer(columns[columns.length-1]+i+1);

			}

			//restore selection (necessary since jdk1.3)
			for (int i = 0; i < columns.length; i++) {
			    gatePanel.gates.getColumnModel().getSelectionModel().
				addSelectionInterval(columns[i],
						     columns[i]);
			}
			gatePanel.gates.getColumnModel().getSelectionModel().
			    addSelectionInterval(columns[columns.length-1]+1,
				columns[columns.length-1]+rows.length-columns.length);

			// re-read columns
			columns = gatePanel.gates.getSelectedColumns();
		    }
		    else return;
		}

		int n = gatePanel.gates.getRowCount();
		char[] gate_descr = new char[n];
		// for each column
		for (int i = 0; i < rows.length; i++) {
		    for (int k = 0; k < n; k++) gate_descr[k] = '-';
		    gate_descr[rows[i]] = '!';

		    // Attention!! gatecontainer must be single gate container!!!
		    Parse.fireMathlibEvent(
		        gatePanel.gates.getGateContainerName(columns[i]), null, "current circuit",
			new Gate(new String(gate_descr), null), 
			MathlibEvent.CHANGE);
		}
	    }
	}
    }

    class PHASEGateAction extends AbstractAction {
	PHASEGateAction() {
	    super("phase/c-phase...");
	    putValue(Action.SHORT_DESCRIPTION, "create PHASE/cPHASE gate");
	}

	public void actionPerformed(ActionEvent actionEvent) {
	    int[] columns = gatePanel.gates.getSelectedColumns();
	    if (gatePanel.gates.getSelectedColumnCount() <= 0)
		JOptionPane.showMessageDialog(f, "Please select a gate first!");
	    else {
		int[] rational = {2, 1};;
		String gate_descr = gatePanel.gates.translateSelectionTogate_descr(1);

		if (Dialogs.phaseInputDialog(f, rational)) {
		    // construct name
		    String name = "Ph(pi*("+rational[0]+")/"+rational[1]+")";

		    for (int i = 0; i < columns.length; i++) {
			Parse.fireMathlibEvent(gatePanel.gates.getGateContainerName(columns[i]),null, "current circuit", new Gate(gate_descr, name), MathlibEvent.CHANGE);
		    }
		}
	    }

	}
    }

    class CUSTOMGateAction extends AbstractAction {
	CUSTOMGateAction() {
	    super("custom/c-custom...");
	    putValue(Action.SHORT_DESCRIPTION, "create CUSTOM/cCUSTOM gate");
	}

	public void actionPerformed(ActionEvent actionEvent) {
	    int[] columns = gatePanel.gates.getSelectedColumns();
	    if (gatePanel.gates.getSelectedColumnCount() <= 0)
		JOptionPane.showMessageDialog(f, "Please select a gate first!");
	    else {

		int selRowCount = gatePanel.gates.getSelectedRowCount();
		Object selectedValue;
		if (selRowCount > 1) {
		    Object[] possibleNValues = new Integer[selRowCount]; 
		    for (int i = 0; i < selRowCount; i++) possibleNValues[i] = new Integer(i+1);

		    selectedValue = JOptionPane.showInputDialog(f, 
					       "please specify n (2^n x 2^n matrix):", "matrix dimension", 
					       JOptionPane.PLAIN_MESSAGE, null, possibleNValues, possibleNValues[0]);
		}
		else selectedValue = new Integer(1);
		if (selectedValue != null) {
		    int n = ((Integer)selectedValue).intValue();

		    Matrix m1 = new Matrix(BinaryOp.pow(2,n), BinaryOp.pow(2,n));
		    String var = Dialogs.mathInputDialog(f, "Enter matrix", null, m1, null, true);
		    if (var != null) {
			int event = (Mathlib.getVar(var) == null)? MathlibEvent.ADD : MathlibEvent.CHANGE;
			Parse.fireMathlibEvent(var, null, "current circuit",m1,event);
			
			String gate_descr = gatePanel.gates.translateSelectionTogate_descr(n);
			for (int i = 0; i < columns.length; i++) {

			    Parse.fireMathlibEvent(
				    gatePanel.gates.getGateContainerName(columns[i]),
				    null, "current circuit", 
				    new Gate(gate_descr, var), 
				    MathlibEvent.CHANGE);
			}
		    }
		    
		}
		
	    }
	}
    }

    class EXPERTGateAction extends AbstractAction {
	EXPERTGateAction() {
	    super("expert/c-expert...");
	    putValue(Action.SHORT_DESCRIPTION, "expert mode");
	}

	public void actionPerformed(ActionEvent actionEvent) {
	    int[] columns = gatePanel.gates.getSelectedColumns();
	    if (gatePanel.gates.getSelectedColumnCount() <= 0)
		JOptionPane.showMessageDialog(f, "Please select a gate first!");
	    else {

		String expression = JOptionPane.showInputDialog(f, "enter expression for 2x2 matrix");
		MathObject o;
		if (expression != null) {
		    try {
			o = Parse.parseExpression(expression);
		    }
		    catch (Exception e) {
			JOptionPane.showMessageDialog(f, "invalid expression!");
			return;
		    }
		    if (o != null && o instanceof Matrix && ((Matrix)o).n()==2 && ((Matrix)o).m() == 2) {
			String gate_descr = gatePanel.gates.translateSelectionTogate_descr(1);
			for (int i = 0; i < columns.length; i++) {

			    Parse.fireMathlibEvent(
				    gatePanel.gates.getGateContainerName(columns[i]),
				    null, "current circuit", 
				    new Gate(gate_descr, expression), MathlibEvent.CHANGE);
			}
		    }
		    else {
			JOptionPane.showMessageDialog(f, "invalid expression!");
			return;
		    }					    
		}
		
	    }
	}
    }

    class SIMPLEGateAction extends AbstractAction {
	SIMPLEGateAction() {
	    super("var/c-var...");
	    putValue(Action.SHORT_DESCRIPTION, "choose from registered operators");
	}

	public void actionPerformed(ActionEvent actionEvent) {
	    int[] columns = gatePanel.gates.getSelectedColumns();
	    if (gatePanel.gates.getSelectedColumnCount() <= 0)
		JOptionPane.showMessageDialog(f, "Please select a gate first!");
	    else {
		String var = Dialogs.chooseMatrixDialog(f, gatePanel.gates.getSelectedRowCount());
		if (var != null) {
		    int dim = ((Matrix)Mathlib.getVar(var)).n();
		    int n = 1;
		    while(BinaryOp.pow(2,n)<dim)n++;

		    for (int i = 0; i < columns.length; i++) {
			Parse.fireMathlibEvent(
				    gatePanel.gates.getGateContainerName(columns[i]),
				    null, "current circuit", 
				    new Gate(gatePanel.gates.translateSelectionTogate_descr(n), var), 
				    MathlibEvent.CHANGE);
		    }
		}
		/*
		VarAuthority varAuthority = new VarAuthority(mathlib, false);
		varAuthority.registerCategory("unitary", new MathObjectConstraints(
				Matrix.class, null, 2, 2, -1, "self*self'\"==one(2)"));
		Enumeration e = varAuthority.getElementsInCategory("unitary");
		Vector v = new Vector();
		while (e.hasMoreElements()) v.add(e.nextElement());
		Object[] possibleValues = v.toArray(); 
		Object selectedValue = JOptionPane.showInputDialog(f, 
		       "Choose one of the following unitary 2x2 matrices", "SIMPLE gates", JOptionPane.PLAIN_MESSAGE, null, possibleValues, possibleValues[0]);

		if (selectedValue != null) {
		    for (int i = 0; i < columns.length; i++) {
			Parse.fireMathlibEvent(gatePanel.gates.getGateContainerName(columns[i]),null, "current circuit", new Gate(gatePanel.gates.getGate_descrForSelection(), (String)selectedValue), MathlibEvent.CHANGE);
		    }
		}
		*/
		
	    }
	}
    }

    class XChangeGateAction extends AbstractAction {
	XChangeGateAction() {
	    super("qubit exchange [2]");
	    putValue(Action.SHORT_DESCRIPTION, "exchang two qubits");
	}

	public void actionPerformed(ActionEvent actionEvent) {
	    int[] columns = gatePanel.gates.getSelectedColumns();
	    if (gatePanel.gates.getSelectedColumnCount() <= 0)
		JOptionPane.showMessageDialog(f, "Please select a gate first!");
	    else {
		int[] rows = gatePanel.gates.getSelectedRows();
		if (rows.length != 2) {
		    JOptionPane.showMessageDialog(f, "Please select two qubits!");
		}
		// ok, let's work
		else {
		    int n = gatePanel.gates.getGateContainer(columns[0]).getDimension();
		    // construct gate_descr
		    char[] gate_descr = new char[n];
		    for (int l = 0; l < n; l++) {
			gate_descr[l] = ((l== rows[0] || l == rows[1]) ? 'm' : '-');
		    }

		    // add xChange matrix
		    String ans = mathlib.evaluateExpression("xChange=[1 0 0 0, 0 0 1 0, 0 1 0 0, 0 0 0 1]");
		    Parse.fireMathlibEvent(ans, "current circuit", null, Mathlib.getVar(ans), MathlibEvent.CHANGE_CATEGORY);

		    for (int i = 0; i < columns.length; i++) {
			Parse.fireMathlibEvent(gatePanel.gates.getGateContainerName(columns[i]),null, "current circuit", new Gate(new String(gate_descr), "xChange"), MathlibEvent.CHANGE);
		    }
		}
	    }

	}
    }


    class RANDGateAction extends AbstractAction {
	RANDGateAction() {
	    super("Hadamard");
	    putValue(Action.SHORT_DESCRIPTION, "create randomization gates");
	}

	public void actionPerformed(ActionEvent actionEvent) {
	    int[] columns = gatePanel.gates.getSelectedColumns();
	    if (gatePanel.gates.getSelectedColumnCount() <= 0)
		JOptionPane.showMessageDialog(f, "Please select a gate first!");
	    else {
		int[] rows = gatePanel.gates.getSelectedRows();
		if (rows.length > columns.length) {
		    if (JOptionPane.showConfirmDialog(f, "Insufficient number of gates selected\nfor the number of qubits chosen to be randomized! \nAdd required gates and proceed?") == JOptionPane.YES_OPTION) {

			// insert required gates
			for (int i = 0; i < rows.length-columns.length; i++) {
			    gatePanel.insertGateContainer(columns[columns.length-1]+i+1);

			}

			//restore selection (necessary since jdk1.3)
			for (int i = 0; i < columns.length; i++) {
			    gatePanel.gates.getColumnModel().getSelectionModel().
				addSelectionInterval(columns[i],
						     columns[i]);
			}
			gatePanel.gates.getColumnModel().getSelectionModel().
			    addSelectionInterval(columns[columns.length-1]+1,
				columns[columns.length-1]+rows.length-columns.length);

			// re-read columns
			columns = gatePanel.gates.getSelectedColumns();
		    }
		    else return;
		}

		int n = gatePanel.gates.getRowCount();

		// for each column
		for (int i = 0; i < rows.length; i++) {
		    Parse.fireMathlibEvent(
		        gatePanel.gates.getGateContainerName(columns[i]), null, "current circuit",
			new Gate(n, rows[i], "H"), 
			MathlibEvent.CHANGE);
		}
	    }
	}
    }

    class CompareGateAction extends AbstractAction {
	CompareGateAction() {
	    super("compare [4]");
	    putValue(Action.SHORT_DESCRIPTION, "gate comparing to states");
	}

	public void actionPerformed(ActionEvent actionEvent) {
	    int[] columns = gatePanel.gates.getSelectedColumns();
	    if (gatePanel.gates.getSelectedColumnCount() <= 0)
		JOptionPane.showMessageDialog(f, "Please select a gate first!");
	    else {
		int[] rows = gatePanel.gates.getSelectedRows();
		if (rows.length != 4) {
		    JOptionPane.showMessageDialog(f, "This operation is a four-qubit gate.\nPlease select the four qubits corresponding to |i_k>, |j_k>, |a_k>, |a_k+1>.");
		    return;
		}
		if (columns.length < 9) {
		    if (JOptionPane.showConfirmDialog(f, "Insufficient number of gates needed\nfor the comparison! \nAdd required gates and proceed?") == JOptionPane.YES_OPTION) {

			// insert required gates
			for (int i = 0; i < 9-columns.length; i++) {
			    gatePanel.insertGateContainer(columns[columns.length-1]+i+1);

			}

			//restore selection (necessary since jdk1.3)
			for (int i = 0; i < columns.length; i++) {
			    gatePanel.gates.getColumnModel().getSelectionModel().
				addSelectionInterval(columns[i],
						     columns[i]);
			}
			gatePanel.gates.getColumnModel().getSelectionModel().
			    addSelectionInterval(columns[columns.length-1]+1,
				columns[columns.length-1]+9-columns.length);

			// re-read columns
			columns = gatePanel.gates.getSelectedColumns();
		    }
		    else return;
		}

		int n = gatePanel.gates.getRowCount();
		char[] descr = new char[n];
		int col = 0;

		// clear
		for (int m = 0; m < n; descr[m++] = '-');
		descr[rows[2]] = '1';
		descr[rows[3]] = 'm';

		Parse.fireMathlibEvent(
		    gatePanel.gates.getGateContainerName(columns[col++]), null, "current circuit",
		    new Gate(new String(descr), "NOT"), 
		    MathlibEvent.CHANGE);

		Parse.fireMathlibEvent(
		    gatePanel.gates.getGateContainerName(columns[col++]), null, "current circuit",
		    new Gate(n, rows[1], "NOT"), 
		    MathlibEvent.CHANGE);

		Parse.fireMathlibEvent(
		    gatePanel.gates.getGateContainerName(columns[col++]), null, "current circuit",
		    new Gate(n, rows[2], "NOT"), 
		    MathlibEvent.CHANGE);

		// clear
		for (int m = 0; m < n; descr[m++] = '-');
		descr[rows[0]] = '1';
		descr[rows[1]] = '1';
		descr[rows[2]] = '1';
		descr[rows[3]] = 'm';

		Parse.fireMathlibEvent(
		    gatePanel.gates.getGateContainerName(columns[col++]), null, "current circuit",
		    new Gate(new String(descr), "NOT"), 
		    MathlibEvent.CHANGE);


		Parse.fireMathlibEvent(
		    gatePanel.gates.getGateContainerName(columns[col++]), null, "current circuit",
		    new Gate(n, rows[2], "NOT"), 
		    MathlibEvent.CHANGE);

		Parse.fireMathlibEvent(
		    gatePanel.gates.getGateContainerName(columns[col++]), null, "current circuit",
		    new Gate(n, rows[1], "NOT"), 
		    MathlibEvent.CHANGE);

		Parse.fireMathlibEvent(
		    gatePanel.gates.getGateContainerName(columns[col++]), null, "current circuit",
		    new Gate(n, rows[0], "NOT"), 
		    MathlibEvent.CHANGE);

		// clear
		for (int m = 0; m < n; descr[m++] = '-');
		descr[rows[0]] = '1';
		descr[rows[1]] = '1';
		descr[rows[2]] = '1';
		descr[rows[3]] = 'm';

		Parse.fireMathlibEvent(
		    gatePanel.gates.getGateContainerName(columns[col++]), null, "current circuit",
		    new Gate(new String(descr), "NOT"), 
		    MathlibEvent.CHANGE);

		Parse.fireMathlibEvent(
		    gatePanel.gates.getGateContainerName(columns[col++]), null, "current circuit",
		    new Gate(n, rows[0], "NOT"), 
		    MathlibEvent.CHANGE);
	    }
	}
    }


    class GenericQFTGateAction extends AbstractAction {
	String factor = "";
	String identifier = "R";
	GenericQFTGateAction() {
	    super("generic QFT");
	    putValue(Action.SHORT_DESCRIPTION, "create quantum fourier transform gates");
	}
	
	public void actionPerformed(ActionEvent actionEvent) {
	    int[] columns = gatePanel.gates.getSelectedColumns();
	    if (gatePanel.gates.getSelectedColumnCount() <= 0)
		JOptionPane.showMessageDialog(f, "Please select a gate first!");
	    else {
		int[] rows = gatePanel.gates.getSelectedRows();

		// attention! the last division is a integer number division!!
		int reqGates = (rows.length*(rows.length+1))/2+rows.length/2;

		if (columns.length < reqGates) {
		    if (JOptionPane.showConfirmDialog(f, "Insufficient number of gates selected\nfor the number of qubits chosen to be Fourier transformed! \nAdd required gates and proceed?") == JOptionPane.YES_OPTION) {

			// insert required gates
			for (int i = 0; i < reqGates-columns.length; i++) {
			    gatePanel.insertGateContainer(columns[columns.length-1]+i+1);

			}

			//restore selection (necessary since jdk1.3)
			for (int i = 0; i < columns.length; i++) {
			    gatePanel.gates.getColumnModel().getSelectionModel().
				addSelectionInterval(columns[i],
						     columns[i]);
			}
			gatePanel.gates.getColumnModel().getSelectionModel().
			    addSelectionInterval(columns[columns.length-1]+1,
				columns[columns.length-1]+reqGates-columns.length);

			// re-read columns
			columns = gatePanel.gates.getSelectedColumns();
		    }
		    else return;
		}

		// this is not necassarily the number of the QFT!
		int n = gatePanel.gates.getRowCount();

		// construct
		char[] descr = new char[n];


		// register phase gates's
		int event;
		for (int k = 0; k < rows.length-1; k++) {
		    if (Mathlib.getVar(identifier+"_"+(k+2)) == null) event = MathlibEvent.ADD;
		    else event = MathlibEvent.CHANGE;

		    Parse.fireMathlibEvent(identifier+"_"+(k+2), null, "current circuit", 
			 Matrix.parseMatrix("[1 0, 0 exp("+factor+"i*pi/"+BinaryOp.pow(2, k+1)+")]"),
			 event);
		}

		// construct H and phase_1...phase_i-1
		int col = 0;
		for (int i = 0; i < rows.length; i++) {
		    // one H per qubit
		    Parse.fireMathlibEvent(
		        gatePanel.gates.getGateContainerName(columns[col]), null, 
			"current circuit",
			new Gate(n, rows[i], "H"), 
			MathlibEvent.CHANGE);


		    col++;
		    for (int k = i+1; k < rows.length; k++) {
			// clear descr
			for (int m = 0; m < n; descr[m++] = '-');
			descr[rows[k]] = '1';
			descr[rows[i]] = 'm';

			Parse.fireMathlibEvent(
			     gatePanel.gates.getGateContainerName(columns[col]), null, 
			     "current circuit", 
			     new Gate(new String(descr), identifier+"_"+(k-i+1)),
			     MathlibEvent.CHANGE);
			col++;
		    }
		}

		// add xChange matrix
		String ans = mathlib.evaluateExpression("xChange=[1 0 0 0, 0 0 1 0, 0 1 0 0, 0 0 0 1]");
		Parse.fireMathlibEvent(ans, "current circuit", null, Mathlib.getVar(ans), MathlibEvent.CHANGE_CATEGORY);

		// construct swap
		for (int i = 0; i < rows.length/2; i++) {
		    // clear descr
		    for (int m = 0; m < n; descr[m++] = '-');
		    descr[rows[i]] = 'm';
		    descr[rows[rows.length-i-1]] = 'm';

		    Parse.fireMathlibEvent(
			gatePanel.gates.getGateContainerName(columns[col]), null, 
			"current circuit", 
			new Gate(new String(descr), "xChange"),
			MathlibEvent.CHANGE);

			col++;
		}
	    }
	}
    }


    class QFTGateAction extends GenericQFTGateAction {
	QFTGateAction() {
	    super();
	    putValue(Action.NAME, "QFT");
	    putValue(Action.SHORT_DESCRIPTION, "create quantum fourier transform gates");
	}
    }

    class InverseQFTGateAction extends GenericQFTGateAction {
	InverseQFTGateAction() {
	    super();
	    putValue(Action.NAME, "inverse QFT");
	    putValue(Action.SHORT_DESCRIPTION, "create inverse quantum fourier transform gates");
	    identifier = "S";
	    factor = "-";
	}
    }


}












