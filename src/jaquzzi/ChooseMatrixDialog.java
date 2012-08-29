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

import qsymbol2.SortedListModel;

import mathlib.BinaryOp;
import mathlib.MathObject;
import mathlib.MathObjectConstraints;
import mathlib.Matrix;
import mathlib.VarAuthority;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

/**
 * class representing a dialog to choose a matrix from the list of variables stored
 * in the mathlib class. Depending on the number of qubits selected (given to this
 * class as a parameter), it is first determined what dimensin the matrix should have.
 * @see Dialogs
 */
public class ChooseMatrixDialog extends JDialog {
    JFrame f;
    JList vars;
    VarAuthority varAuthority;
    SortedListModel listModel;
    String retStr = null;
    boolean OK = false;
	
    public ChooseMatrixDialog(JFrame parent, int qubitsSel) {
	super(parent, "choose untary matrix", true);

	f = parent;
	varAuthority = new VarAuthority(MathObject.mathlib, false);
	listModel = new SortedListModel();
	Box box;

	GridBagLayout gridBag = new GridBagLayout();
	GridBagConstraints c = new GridBagConstraints();

	JPanel pane = new JPanel();

	pane.setLayout(gridBag);
	pane.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));


	varAuthority.registerCategory("unitary", new MathObjectConstraints(
									   Matrix.class, null, 2, 2, -1, "self*self'\"==one(2)"));
	Enumeration e = varAuthority.getElementsInCategory("unitary");
	while (e.hasMoreElements()) 
	    listModel.add(e.nextElement());

	vars = new JList(listModel);
	vars.setSelectionMode(DefaultListSelectionModel.SINGLE_SELECTION);
	JScrollPane scrollPane = new JScrollPane(vars);


	JLabel dimension = new JLabel("(2^n x 2^n)-matrix  n:");
	dimension.setDisplayedMnemonic('n');
	Vector nChoices = new Vector(qubitsSel);
	for (int i = 0; i < qubitsSel; i++) nChoices.add(new Integer(i+1));
	final JComboBox dimensionInput = new JComboBox(nChoices);
	dimensionInput.setSelectedItem(nChoices.elementAt(0));
	dimensionInput.addItemListener(new ItemListener() {
		public void itemStateChanged(ItemEvent e) {
		    try {
			int n = ((Integer)e.getItem()).intValue();
			int dim = BinaryOp.pow(2,n);
			varAuthority.unregisterCategory("unitary");
			varAuthority.registerCategory("unitary", 
						      new MathObjectConstraints(
										Matrix.class, null, dim, dim, -1, 
										"self*self'\"==one("+dim+")"));
			listModel.removeAll();
			Enumeration enum = varAuthority.getElementsInCategory("unitary");
			while (enum.hasMoreElements()) {
			    listModel.add(enum.nextElement());
			}
		    }
		    catch (Exception excep) {
		    }
		}
	    });

	JCheckBox adjoint = new JCheckBox("adjoint");
	adjoint.setMnemonic('a');

	// arrange bits & pieces

	c.gridwidth = GridBagConstraints.RELATIVE;
	c.fill = GridBagConstraints.HORIZONTAL;
	c.weightx = 0;
	c.weighty = 0;
	c.insets = new Insets(0, 0, 0, 0);
	pane.add(dimension, c);

	c.gridwidth = GridBagConstraints.REMAINDER;
	c.weightx = 1;
	c.insets = new Insets(0, 10, 0, 0);
	pane.add(dimensionInput, c);

	c.insets = new Insets(10, 0, 0, 0);
	c.fill = GridBagConstraints.BOTH;
	c.weightx = 1;
	c.weighty = 1;
	pane.add(scrollPane, c);

	c.gridwidth = GridBagConstraints.REMAINDER;
	c.fill = GridBagConstraints.NONE;
	c.weightx = 1;
	c.weighty = 0;
	c.anchor = GridBagConstraints.WEST;
	pane.add(adjoint, c);


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
		    if (vars.getSelectedValue() != null) {
			OK = true;
			hide();
		    }
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

    public String getMatrixName() {
	return (String)vars.getSelectedValue();
    }

}
