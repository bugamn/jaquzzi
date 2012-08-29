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
import mathlib.BinaryOp;
import mathlib.Complex;
import mathlib.Matrix;
import mathlib.Parse;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

/**
 * class representing a dialog to enter a variable sized matrix. A mask can be given to
 * select the entries editable. The flag unitary sets whether the matrix is tested for 
 * unitarity.
 */
public class MatrixInputDialog extends JDialog {
    Matrix m1;
    boolean isUnitary;
    JTextField[][] input;
    JFrame f;
    boolean OK = false;
    JTextField nameInput;

    public MatrixInputDialog(JFrame parent, String title, boolean modal, String matrixName,
			     Matrix matrix, boolean[][] mask, boolean unitary) {

	super(parent, title, true);

	m1 = matrix;
	isUnitary = unitary;
	input = new JTextField[matrix.n()][matrix.m()];
	f = parent;

	Box box;

	GridBagLayout gridBag = new GridBagLayout();
	GridBagConstraints c = new GridBagConstraints();

	JPanel pane = new JPanel();

	pane.setLayout(gridBag);
	pane.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));


	c.gridwidth = GridBagConstraints.RELATIVE;
	c.fill = GridBagConstraints.HORIZONTAL;
	c.weightx = 0.0;
	c.weighty = 0;
	c.insets = new Insets(0, 0, 10, 0);

	JLabel name = new JLabel("Name");
	if (matrixName == null) 
	    nameInput = new JTextField();
	else
	    nameInput = new JTextField(matrixName);
	name.setLabelFor(nameInput);
	name.setDisplayedMnemonic('N');
	pane.add(name, c);

	c.gridwidth = GridBagConstraints.REMAINDER;
	c.weightx = 1.0;

	pane.add(nameInput, c);

	c.insets = new Insets(0, 0, 0, 0);

	JPanel inputPane = new JPanel();
	inputPane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black), "matrix"));
	inputPane.setLayout(new GridLayout(matrix.n(), matrix.m(), 5, 5));
	for (int i = 0; i < matrix.n(); i++) {
	    for (int j = 0; j < matrix.m(); j++) {
		input[i][j] = new JTextField(matrix.getElement(i, j).toString(), 6);
		if (mask != null) input[i][j].setEnabled(mask[i][j]);
		inputPane.add(input[i][j]);
	    }
	}

	c.insets = new Insets(0, 0, 0, 0);
	pane.add(inputPane, c);

	JPanel buttonPanel = new JPanel();
	buttonPanel.setLayout(new GridLayout(1,2,10,10));
	JButton ok = new JButton("Ok"); 
	ok.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    if (nameInput.getText().equals("")) {
			JOptionPane.showMessageDialog(f, "Must enter a name!");
			return;
		    }

		    try {
			for (int i = 0; i < m1.n(); i++) {
			    for (int j = 0; j < m1.m(); j++) {
				m1.setElement(i, j, (Complex)Parse.parseExpression(input[i][j].getText()));
			    }
			}
		    } catch (Exception ex) {
			JOptionPane.showMessageDialog(f, "Invalid matrix component!");
			return;
		    }

		    // check constraint
		    if (isUnitary) {
			Matrix m2 = new Matrix(m1);
			BinaryOp op = new BinaryOp('*');
			m2.conjugate(); m2.transpose();
			if (((Argument)op.apply(m1, m2)).equals((new OneMap()).apply(new Complex(m2.m()))) == false) {
			    JOptionPane.showMessageDialog(f, "Matrix is not unitary!");
			    return;
			}
		    }
		    OK = true;
		    hide();
		}
	    });

	JButton cancel = new JButton("Cancel"); 
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

	getContentPane().add(pane);

    }

    public boolean wasCanceled() {
	return OK ^ true;
    }

    public String getMatrixName() {
	return nameInput.getText();
    }

}

