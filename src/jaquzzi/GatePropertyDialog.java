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
import mathlib.StringArgument;

import java.awt.*;
import java.awt.event.*;
import java.util.*;


/**
 * class representing a dialog to set the properties of a GateContainer.
 */
public class GatePropertyDialog extends JDialog {
    boolean OK = false;

    JLabel name;
    JTextField nameInput;

    JLabel iteration;
    JTextField iterationInput;

    JLabel sigma;
    JTextField sigmaInput;

    JLabel errorRate;
    JTextField errorRateInput;

    JLabel decayProb;
    JTextField decayProbInput;

    GateProperty properties;
    final JFrame f;
    final String gate;
	
    public GatePropertyDialog(JFrame parent, String gateName) {
	super(parent, "Gate Properties", true);

	MathObject o = Mathlib.getVar(gateName+"_properties");
	if (o == null) properties = new GateProperty(gateName);
	else properties = (GateProperty)o;

	f = parent;
	gate = gateName;

	GridBagLayout gridBag = new GridBagLayout();
	GridBagConstraints c = new GridBagConstraints();

	JPanel pane = new JPanel();

	pane.setLayout(gridBag);
	pane.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

	JPanel basicPane = new JPanel();
	GridBagLayout bpGridLayout = new GridBagLayout();
	GridBagConstraints bpConstraints = new GridBagConstraints();
	basicPane.setLayout(bpGridLayout);
	basicPane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Basic Properties"));


	//--- name property 

	Argument nameProperty = properties.getProperty("name");
	if (nameProperty == null)
	    nameInput = new JTextField(gate, 20);
	else 
	    nameInput = new JTextField(nameProperty.toString(),20);

	name = new JLabel("gate name");
	name.setLabelFor(nameInput);
	name.setDisplayedMnemonic('n');

	bpConstraints.fill = GridBagConstraints.VERTICAL;
	bpConstraints.gridwidth = GridBagConstraints.RELATIVE;
	bpConstraints.weightx = 1;
	basicPane.add(name, bpConstraints);

	bpConstraints.fill = GridBagConstraints.BOTH;
	bpConstraints.gridwidth = GridBagConstraints.REMAINDER;
	basicPane.add(nameInput, bpConstraints);

	//--- iteration property 

	Argument repProperty = properties.getProperty("reps");
	if (repProperty == null)
	    iterationInput = new JTextField("1");
	else 
	    iterationInput = new JTextField(""+(new Double(((Complex)repProperty).re())).intValue());

	iteration = new JLabel("gate iteration");
	iteration.setLabelFor(iterationInput);
	iteration.setDisplayedMnemonic('i');

	bpConstraints.fill = GridBagConstraints.VERTICAL;
	bpConstraints.gridwidth = GridBagConstraints.RELATIVE;
	basicPane.add(iteration, bpConstraints);

	bpConstraints.fill = GridBagConstraints.BOTH;
	bpConstraints.gridwidth = GridBagConstraints.REMAINDER;
	basicPane.add(iterationInput, bpConstraints);


	//------ avanced pane

	JPanel advancedPane = new JPanel();
	GridBagLayout apGridLayout = new GridBagLayout();
	GridBagConstraints apConstraints = new GridBagConstraints();
	advancedPane.setLayout(apGridLayout);
	advancedPane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Advanced Properties"));

	//--- iteration property 

	Argument sigmaProperty = properties.getProperty("sigma");
	if (sigmaProperty == null)
	    sigmaInput = new JTextField("global");
	else 
	    sigmaInput = new JTextField(sigmaProperty.toString());

	sigma = new JLabel("deviation sigma");
	sigma.setLabelFor(sigmaInput);
	sigma.setDisplayedMnemonic('e');

	apConstraints.fill = GridBagConstraints.VERTICAL;
	apConstraints.gridwidth = GridBagConstraints.RELATIVE;
	apConstraints.weightx = 1;
	advancedPane.add(sigma, apConstraints);

	apConstraints.fill = GridBagConstraints.BOTH;
	apConstraints.gridwidth = GridBagConstraints.REMAINDER;
	advancedPane.add(sigmaInput, apConstraints);

	//--- error Rate property 

	Argument errorRateProperty = properties.getProperty("rate");
	if (errorRateProperty == null)
	    errorRateInput = new JTextField("global",10);
	else 
	    errorRateInput = new JTextField(errorRateProperty.toString(),10);

	errorRate = new JLabel("error rate per gate");
	errorRate.setLabelFor(errorRateInput);
	errorRate.setDisplayedMnemonic('r');

	apConstraints.fill = GridBagConstraints.VERTICAL;
	apConstraints.gridwidth = GridBagConstraints.RELATIVE;
	apConstraints.weightx = 1;
	advancedPane.add(errorRate, apConstraints);

	apConstraints.fill = GridBagConstraints.BOTH;
	apConstraints.gridwidth = GridBagConstraints.REMAINDER;
	advancedPane.add(errorRateInput, apConstraints);

	//--- decayProb property 

	Argument decayProbProperty = properties.getProperty("decay");
	if (decayProbProperty == null)
	    decayProbInput = new JTextField("global",10);
	else 
	    decayProbInput = new JTextField(decayProbProperty.toString(),10);

	decayProb = new JLabel("decay probability");
	decayProb.setLabelFor(decayProbInput);
	decayProb.setDisplayedMnemonic('p');

	apConstraints.fill = GridBagConstraints.VERTICAL;
	apConstraints.gridwidth = GridBagConstraints.RELATIVE;
	apConstraints.weightx = 1;
	advancedPane.add(decayProb, apConstraints);

	apConstraints.fill = GridBagConstraints.BOTH;
	apConstraints.gridwidth = GridBagConstraints.REMAINDER;
	advancedPane.add(decayProbInput, apConstraints);

	// --------------------------
	// -- putting it together

	c.gridwidth = GridBagConstraints.REMAINDER;
	c.fill = GridBagConstraints.BOTH;
	c.weightx = 1;
	pane.add(basicPane, c);
	pane.add(advancedPane, c);


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
		    if ("".equals(nameInput.getText()) == true) {
			JOptionPane.showMessageDialog(f, "Name cannot be empty!");
			return; 
		    }
		    boolean veto = false;
		    char faultyChar = ' ';
		    for (int i = 0; i < nameInput.getText().length(); i++) {
			if (",.!&|=()+'/-*#; \"".indexOf(nameInput.getText().charAt(i)) != -1) {
			    veto = true;
			    faultyChar = nameInput.getText().charAt(i);
			    break;
			}
		    }
		    if (veto) {
			JOptionPane.showMessageDialog(f, "Invalid character in name: '" + faultyChar +"'");
			return;
		    }

		    int reps;
		    try {
			reps = Integer.parseInt(iterationInput.getText());
		    } catch (Exception exc) {
			JOptionPane.showMessageDialog(f, "Error parsing int value from iteration field!");
			return;
		    }
		    double sigmaVal;
		    if ("global".equals(sigmaInput.getText()) == false) {
			try{
			    sigmaVal = Double.parseDouble(sigmaInput.getText());
			} catch (Exception exc) {
			    JOptionPane.showMessageDialog(f, "Error parsing double value from sigma error field!");
			    return;
			}
			properties.addProperty("sigma", new Complex(sigmaVal));
		    }
		    else properties.removeProperty("sigma");

		    double rateVal;
		    if ("global".equals(errorRateInput.getText()) == false) {
			try{
			    rateVal = Double.parseDouble(errorRateInput.getText());
			} catch (Exception exc) {
			    JOptionPane.showMessageDialog(f, "Error parsing double value from decoherence rate field!");
			    return;
			}
			properties.addProperty("rate", new Complex(rateVal));
		    }
		    else properties.removeProperty("rate");

		    double decayVal;
		    if ("global".equals(decayProbInput.getText()) == false) {
			try{
			    decayVal = Double.parseDouble(decayProbInput.getText());
			} catch (Exception exc) {
			    JOptionPane.showMessageDialog(f, "Error parsing double value from decay probability field!");
			    return;
			}
			properties.addProperty("decay", new Complex(decayVal));
		    }
		    else properties.removeProperty("decay");

		    if (gate.equals(nameInput.getText()) == false)
			properties.addProperty("name", 
					       new StringArgument(nameInput.getText()));
		    properties.addProperty("reps", new Complex(reps));
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

    public boolean wasCanceled() {
	return OK ^ true;
    }

    public GateProperty getPropertyObject() {
	return properties;
    }

}
