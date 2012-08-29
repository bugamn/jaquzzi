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

import javax.swing.*;
import javax.swing.event.*;

import mathlib.Argument;
import mathlib.Complex;
import mathlib.GateProperty;
import mathlib.MathObject;
import mathlib.Mathlib;

import java.awt.*;
import java.awt.event.*;
import java.util.*;


/**
 * class representing a dialog to set the simulation properties of a circuit.
 */
public class CircuitPropertyDialog extends JDialog {
    boolean OK = false;

    JPanel simulationModePanel;

    JLabel sigma;
    JTextField sigmaInput;

    JLabel errorRate;
    JTextField errorRateInput;

    JLabel decayProb;
    JTextField decayProbInput;

    final JCheckBox fidelity;

    GateProperty properties;
    final JFrame f;

    /** 0 = ideal, 1 = operational errors, 2 = decoherence errors, 3 = both errors */
    protected int simMode; 
	
    public CircuitPropertyDialog(JFrame parent, int simMode) {
	super(parent, "Simulation Properties", true);

	MathObject o = Mathlib.getVar("circuit_properties");
	if (o == null) properties = new GateProperty("circuit");
	else properties = (GateProperty)o;

	f = parent;

	GridBagLayout gridBag = new GridBagLayout();
	GridBagConstraints c = new GridBagConstraints();

	JPanel pane = new JPanel();

	pane.setLayout(gridBag);
	pane.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));


	Box box = Box.createVerticalBox();
	//------ operror pane

	JPanel operrorPane = new JPanel();
	GridBagLayout apGridLayout = new GridBagLayout();
	GridBagConstraints apConstraints = new GridBagConstraints();
	operrorPane.setLayout(apGridLayout);
	operrorPane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Operational Error"));

	//--- sigma property 

	Argument sigmaProperty = properties.getProperty("sigma");
	if (sigmaProperty == null)
	    sigmaInput = new JTextField("0",10);
	else 
	    sigmaInput = new JTextField(sigmaProperty.toParseableString(),10);

	sigma = new JLabel("sigma error");
	sigma.setLabelFor(sigmaInput);
	sigma.setDisplayedMnemonic('e');

	apConstraints.fill = GridBagConstraints.VERTICAL;
	apConstraints.gridwidth = GridBagConstraints.RELATIVE;
	apConstraints.weightx = 1;
	operrorPane.add(sigma, apConstraints);

	apConstraints.fill = GridBagConstraints.BOTH;
	apConstraints.gridwidth = GridBagConstraints.REMAINDER;
	operrorPane.add(sigmaInput, apConstraints);


	//------ decerror pane

	JPanel decerrorPane = new JPanel();
	GridBagLayout dpGridLayout = new GridBagLayout();
	GridBagConstraints dpConstraints = new GridBagConstraints();
	decerrorPane.setLayout(dpGridLayout);
	decerrorPane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Decoherence Error"));

	//--- error Rate property 

	Argument errorRateProperty = properties.getProperty("rate");
	if (errorRateProperty == null)
	    errorRateInput = new JTextField("0",10);
	else 
	    errorRateInput = new JTextField(errorRateProperty.toParseableString(),10);

	errorRate = new JLabel("error rate per gate");
	errorRate.setLabelFor(errorRateInput);
	errorRate.setDisplayedMnemonic('r');

	dpConstraints.fill = GridBagConstraints.VERTICAL;
	dpConstraints.gridwidth = GridBagConstraints.RELATIVE;
	dpConstraints.weightx = 1;
	decerrorPane.add(errorRate, dpConstraints);

	dpConstraints.fill = GridBagConstraints.BOTH;
	dpConstraints.gridwidth = GridBagConstraints.REMAINDER;
	decerrorPane.add(errorRateInput, dpConstraints);

	//--- decayProb property 

	Argument decayProbProperty = properties.getProperty("decay");
	if (decayProbProperty == null)
	    decayProbInput = new JTextField("0",10);
	else 
	    decayProbInput = new JTextField(decayProbProperty.toParseableString(),10);

	decayProb = new JLabel("decay probability");
	decayProb.setLabelFor(decayProbInput);
	decayProb.setDisplayedMnemonic('p');

	dpConstraints.fill = GridBagConstraints.VERTICAL;
	dpConstraints.gridwidth = GridBagConstraints.RELATIVE;
	dpConstraints.weightx = 1;
	decerrorPane.add(decayProb, dpConstraints);

	dpConstraints.fill = GridBagConstraints.BOTH;
	dpConstraints.gridwidth = GridBagConstraints.REMAINDER;
	decerrorPane.add(decayProbInput, dpConstraints);


	box.add(operrorPane);
	box.add(decerrorPane);

	// simulation mode pane

	JPanel basicPane = new JPanel();
	GridBagLayout bpGridLayout = new GridBagLayout();
	GridBagConstraints bpConstraints = new GridBagConstraints();
	basicPane.setLayout(bpGridLayout);
	basicPane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Simulation Mode"));

	this.simMode = simMode;

	//--- simulation mode property
	JRadioButton ideal = new JRadioButton("ideal");
	ideal.setMnemonic('i');
	ideal.setActionCommand("ideal");
	if (simMode == 0) {
	    ideal.setSelected(true);
	    sigmaInput.setEnabled(false);
	    errorRateInput.setEnabled(false);
	    decayProbInput.setEnabled(false);
	} 

	JRadioButton operror = new JRadioButton("operational errors");
	operror.setMnemonic('o');
	operror.setActionCommand("operror");
	if (simMode == 1) {
	    operror.setSelected(true);
	    sigmaInput.setEnabled(true);
	    errorRateInput.setEnabled(false);
	    decayProbInput.setEnabled(false);
	} 

	JRadioButton decerror = new JRadioButton("decoherence errors");
	decerror.setMnemonic('d');
	decerror.setActionCommand("decerror");
	if (simMode == 2) {
	    decerror.setSelected(true);
	    sigmaInput.setEnabled(false);
	    errorRateInput.setEnabled(true);
	    decayProbInput.setEnabled(true);
	} 

	JRadioButton both = new JRadioButton("both error types");
	both.setMnemonic('b');
	both.setActionCommand("both");
	if (simMode == 3) {
	    both.setSelected(true);
	    sigmaInput.setEnabled(true);
	    errorRateInput.setEnabled(true);
	    decayProbInput.setEnabled(true);
	} 

	ButtonGroup simulationMode = new ButtonGroup();
	simulationMode.add(ideal);
	simulationMode.add(operror);
	simulationMode.add(decerror);
	simulationMode.add(both);

	RadioListener listener = new RadioListener();
	ideal.addActionListener(listener);
	operror.addActionListener(listener);
	decerror.addActionListener(listener);
	both.addActionListener(listener);

	simulationModePanel = new JPanel();
	simulationModePanel.setLayout(new GridLayout(0,1));
	simulationModePanel.add(ideal);
	simulationModePanel.add(operror);
	simulationModePanel.add(decerror);
	simulationModePanel.add(both);

	bpConstraints.fill = GridBagConstraints.BOTH;
	bpConstraints.gridwidth = GridBagConstraints.REMAINDER;
	bpConstraints.weightx = 1;
	basicPane.add(simulationModePanel, bpConstraints);

	fidelity = new JCheckBox("calculate fidelity (double memory + double computation required)");
	Argument fidelityProperty = properties.getProperty("fidelity");
	if (fidelityProperty == null)
	    fidelity.setSelected(false);
	else 
	    fidelity.setSelected(((Complex)fidelityProperty).re() != 0);

	fidelity.setMnemonic('f');


	// --------------------------
	// -- putting it together

	c.gridwidth = GridBagConstraints.RELATIVE;
	c.fill = GridBagConstraints.BOTH;
	pane.add(basicPane, c);
	c.gridwidth = GridBagConstraints.REMAINDER;
	c.weightx = 1;
	pane.add(box, c);
	pane.add(fidelity, c);

	c.fill = GridBagConstraints.NONE;
	c.anchor = GridBagConstraints.CENTER;
	c.weighty = 0;

	JPanel buttonPanel = new JPanel();
	buttonPanel.setLayout(new GridLayout(1,2,10,10));
	JButton ok = new JButton("Ok"); 
	ok.setMnemonic('k');
	ok.setDefaultCapable(true);
	ok.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    double sigmaVal;
		    try{
			sigmaVal = Double.parseDouble(sigmaInput.getText());
		    } catch (Exception exc) {
			JOptionPane.showMessageDialog(f, "Error parsing double value from sigma error field!");
			return;
		    }
		    double rateVal;
		    try{
			rateVal = Double.parseDouble(errorRateInput.getText());
		    } catch (Exception exc) {
			JOptionPane.showMessageDialog(f, "Error parsing double value from error rate field!");
			return;
		    }
		    double decayVal;
		    try{
			decayVal = Double.parseDouble(decayProbInput.getText());
		    } catch (Exception exc) {
			JOptionPane.showMessageDialog(f, "Error parsing double value from decay probability field!");
			return;
		    }
		    properties.addProperty("rate", new Complex(rateVal));
		    properties.addProperty("decay", new Complex(decayVal));
		    properties.addProperty("sigma", new Complex(sigmaVal));
		    properties.addProperty("mode", new Complex(getSimulationMode()));
		    properties.addProperty("fidelity", 
					   new Complex(((fidelity.isSelected())? 1:0)));

		    OK = true;
		    hide();
		}
	    });

	JButton cancel = new JButton("Cancel"); 
	cancel.setMnemonic('c');
	cancel.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    hide();
		}
	    });
	buttonPanel.add(ok); 
	buttonPanel.add(cancel);

	JPanel centerPanel = new JPanel();
	centerPanel.setLayout(new FlowLayout());
	centerPanel.add(Box.createHorizontalStrut(20));
	centerPanel.add(buttonPanel);
	centerPanel.add(Box.createHorizontalStrut(20));

	c.insets = new Insets(20, 0, 0, 0);
	pane.add(centerPanel,c);

	getRootPane().setDefaultButton(ok);
	getContentPane().add(pane);
    }

    private int getSimulationMode() {
	return simMode;
    }

    public boolean wasCanceled() {
	return OK ^ true;
    }

    public GateProperty getPropertyObject() {
	return properties;
    }

    class RadioListener implements ActionListener {
	public void actionPerformed(ActionEvent e) {
	    if ("ideal".equals(e.getActionCommand())) {
		sigmaInput.setEnabled(false);
		errorRateInput.setEnabled(false);
		decayProbInput.setEnabled(false);
		simMode = 0;
	    }
	    else if ("operror".equals(e.getActionCommand())) {
		sigmaInput.setEnabled(true);
		errorRateInput.setEnabled(false);
		decayProbInput.setEnabled(false);
		simMode = 1;
	    }
	    else if ("decerror".equals(e.getActionCommand())) {
		sigmaInput.setEnabled(false);
		errorRateInput.setEnabled(true);
		decayProbInput.setEnabled(true);
		simMode = 2;
	    }
	    else if ("both".equals(e.getActionCommand())) {
		sigmaInput.setEnabled(true);
		errorRateInput.setEnabled(true);
		decayProbInput.setEnabled(true);
		simMode = 3;
	    }
	}
    }

}
