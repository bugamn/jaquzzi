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

import mathlib.Parse;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

/**
 * class representing a dialog to enter a 2x2 phase matrix
 */
public class PhaseDialog extends JDialog {
    JComboBox nominator;
    JComboBox denominator;
    JLabel phase;
    int nom; 
    int denom;
    boolean OK;

    public PhaseDialog(JFrame parent, String title, boolean modal, int nom, int denom) {
	super(parent, title, modal);

	phase = new JLabel("");
	this.nom = nom;
	this.denom = denom;
	OK = false;

	Box box;

	GridBagLayout gridBag = new GridBagLayout();
	GridBagConstraints c = new GridBagConstraints();

	JPanel pane = new JPanel();

	pane.setLayout(gridBag);
	pane.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));


	c.fill = GridBagConstraints.HORIZONTAL;
	c.weightx = 0.0;
	c.weighty = 0;
	c.gridwidth = GridBagConstraints.REMAINDER;
	c.gridheight = 1;
	c.gridx = 0;
	c.gridy = 0;
	c.insets = new Insets(0, 0, 20, 0);

	JLabel name = new JLabel("Please specify phase:");
	pane.add(name, c);

	c.gridwidth = 3;
	c.gridheight = 4;
	c.gridy = 1;
	c.insets = new Insets(0, 0, 0, 0);
	JLabel i = new JLabel(jaQuzzi.expIcon);
	pane.add(i, c);

	Vector nominatorNumbers = new Vector(21);
	for (int k = -10; k <= 10; k++) {
	    nominatorNumbers.add(new Integer(k).toString());
	}
	nominator = new JComboBox(nominatorNumbers);
	nominator.setSelectedIndex(nom+10);

	String[] denominatorNumbers = {"1", "2", "3", "4", "5"};
	denominator = new JComboBox(denominatorNumbers);
	denominator.setSelectedIndex(denom-1);


	nominator.addItemListener(new ItemListener() {
		public void itemStateChanged(ItemEvent e) {
		    setNominator(Integer.parseInt((String)e.getItem()));
		    phase.setText((Parse.parseExpression("exp(i*pi*(" + ((String)e.getItem()) + ")/" + ((String)denominator.getSelectedItem()) + ")")).toString());
		}
	    });
	c.gridwidth = 1;
	c.gridheight = 1;
	c.gridx = 3;
	pane.add(nominator, c);

	c.gridy = 2;
	JLabel dash = new JLabel(new ImageIcon("resources/dash.gif"));
	pane.add(dash, c);

	denominator.addItemListener(new ItemListener() {
		public void itemStateChanged(ItemEvent e) {
		    setDenominator(Integer.parseInt((String)e.getItem()));
		    phase.setText((Parse.parseExpression("exp(i*pi*(" + ((String)nominator.getSelectedItem()) + ")/" + ((String)e.getItem()) + ")")).toString());
		}
	    });
	c.gridy = 3;
	pane.add(denominator, c);

	JLabel equals = new JLabel(jaQuzzi.equalsIcon);
	c.gridx = 4;
	c.gridy = 1;
	c.gridheight = 4;
	pane.add(equals, c);

	phase.setText(Parse.parseExpression("exp(i*pi*" + ((String)nominator.getSelectedItem()) + "/" + ((String)denominator.getSelectedItem()) + ")").toString());
	c.gridx = 5;
	c.gridy = 3;
	c.gridheight = 1;
	pane.add(phase, c);

	c.gridy = 1;
	pane.add(Box.createHorizontalStrut(60), c);

	JPanel buttonPanel = new JPanel();
	buttonPanel.setLayout(new GridLayout(1,4,10,10));
	JButton ok = new JButton("Ok"); 
	ok.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    hide();
		    OK = true;
		}
	    });

	JButton cancel = new JButton("Cancel"); 
	cancel.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    hide();
		}
	    });
	buttonPanel.add(Box.createHorizontalStrut(50));
	buttonPanel.add(ok); buttonPanel.add(cancel);
	buttonPanel.add(Box.createHorizontalStrut(50));

	c.gridx = 0;
	c.gridwidth = GridBagConstraints.REMAINDER;
	c.gridy = 5;
	c.gridheight = 1;
	c.insets = new Insets(30, 0, 0, 0);
	pane.add(buttonPanel,c);

	getRootPane().setDefaultButton(ok);

	getContentPane().add(pane);
    }

    public void setNominator(int nom) {
	this.nom = nom;
    }

    public void setDenominator(int denom) {
	this.denom = denom;
    }


    public int getNominator() {
	return nom;
    }

    public int getDenominator() {
	return denom;
    }

    public boolean wasCanceled() {
	return !OK;
    }
}

