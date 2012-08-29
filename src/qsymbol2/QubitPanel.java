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

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import mathlib.Braket;

import java.util.*;

/**
 * class providing a panel with CheckBoxes to set the initial value of a braket. This
 * class is used by the GatePanel class in order to initialize the qubits. The class
 * can provide an extended and simple version of the panel (extended text + icon,
 * simple just icon). The class provides methods to retrieve a Braket from the
 * settings done by the user as well as to initialize the panel. The panel can grow
 * (checkboxes added) and shrink (checkboxes removed).
 * @see GatePanel
 * @see edu.buffalo.fs7.mathlib.Braket
 */
public class QubitPanel extends JPanel {

    /** mode constant */
    public static final int EXTENDED = 1;
    /** mode constant */
    public static final int SIMPLE = 2;

    /** number of qubits = number of checkboxes*/
    protected int qubits;
    /** vector holding the name of the qubit for each checkbox */
    protected Vector labels;
    /** vector holding the checkbox for each qubit */
    protected Vector qubitBoxes;

    /** refernence to the parent who needs to be an ItemListener */
    protected ItemListener parent;

    /** mode */
    protected boolean extended;

    /**
     * creates a qubit panel in extended mode
     */
    public QubitPanel(ItemListener parent, int qubits) {
	this(parent, qubits, EXTENDED);
    }

    /**
     * creates a qubit panel in the specified mode
     */
    public QubitPanel(ItemListener parent, int qubits, int complexityMode) {
	this.parent = parent;
	this.qubits = 0;
	this.extended = (complexityMode == EXTENDED);

	labels = new Vector(qubits);
	qubitBoxes = new Vector(qubits);

	setLayout(new GridLayout(0,2));

	construct(qubits);
    }

    /**
     * sets the name of a qubit in the panel
     */
    public void setQubitName(int number, String name) {
	if (number >= 0 && number < qubits) {
	    ((JLabel)labels.get(number)).setText(name);
	}
    }

    /**
     * returns the name of a qubit in the panel
     */
    public String getQubitName(int number) {
	if (number >= 0 && number < qubits) {
	    return ((JLabel)labels.get(number)).getText();
	}
	else return null;
    }

    /**
     * sets the qubit number to a given value. If the given number is larger, more
     * checkboxes are added, if the given number is smaller, checkboxes are removed.
     */
    public void setQubitNumber(int qubits) {
	construct(qubits);
    }

    /**
     * returns the number of checkboxes in the panel
     */
    public int getQubitNumber() {
	return qubits;
    }

    /**
     * moves a checkbox with its label to a given position
     */
    public void moveQubit(int fromRow, int toRow) {
	int index1 = (fromRow < toRow)? fromRow : toRow;
	int index2 = (fromRow < toRow)? toRow : fromRow;
	String temp = getQubitName(index2);
	for (int i = index2; i > index1; i--) {
	    setQubitName(i, getQubitName(i-1));
	}
	setQubitName(index1, temp);
    }

    /**
     * removes a given checkbox with label
     */
    public void removeQubit(int number) {
	JLabel label = (JLabel)labels.get(number);
	JCheckBox qubitBox = (JCheckBox)qubitBoxes.get(number);

	// remove from panel
	remove(label);
	remove(qubitBox);

	    // remove from vector
	labels.remove(label);
	qubitBoxes.remove(qubitBox);
	qubits--;
    }

    /**
     * add a checkbox with a label specified by labelText
     */
    public void addQubit(String labelText) {
	JLabel label;
	JCheckBox qubitBox;

	if (extended) {
	    qubitBox = new JCheckBox("|0>", jaQuzzi.qubitupIcon);
	}
	else {
	    qubitBox = new JCheckBox("", jaQuzzi.qubitupIcon);
	}
	qubitBox.setSelectedIcon(jaQuzzi.qubitdownIcon);
	qubitBox.addItemListener(new ItemListener() {
		public void itemStateChanged(ItemEvent e) {
		    JCheckBox checkbox = (JCheckBox)e.getItem();
		    checkbox.setText((checkbox.isSelected() ? "|1>":"|0>"));
		}
	    });
	qubitBox.addItemListener(parent);
	qubitBox.setHorizontalTextPosition(JCheckBox.RIGHT);

	label = new JLabel(labelText);

	    // add to vector
	labels.addElement(label);
	qubitBoxes.addElement(qubitBox);

	    // add to panel
	add(label);
	add(qubitBox);

	qubits++;
    }

    /**
     * inserts a checkbox with a label specified by labelName at position row.
     */
    public void insertQubit(String labelName, int row) {
	if (row >= 0 && row < qubits-1) {
            int k = qubits-1;

	    addQubit("");
	    while (k > row) {
		((JLabel)labels.get(k)).setText(((JLabel)labels.get(k-1)).getText());
		k--;
	    }
	    ((JLabel)labels.get(row)).setText(labelName);
	}
    }

    private void construct(int newQubits) {
	while (newQubits > qubits) {
	    addQubit("");
	}
	while (newQubits < qubits) {
	    removeQubit(qubits-1);
	}
    }


    /**
     * retrieves the braket from the positions of the qubit checkboxes
     */
    public Braket getQubitValue() {
	String str = new String("|");
	for (Enumeration e = qubitBoxes.elements(); e.hasMoreElements();) {
	    str = str.concat((((JCheckBox)e.nextElement()).isSelected() ? "1":"0"));
	}
	str = str.concat(">");

	try {
	    return Braket.parseBraket(str);
	}catch (Exception e) {
	    return null;
	}
    }

    /**
     * does nothing right now
     */
    public void reset() {
    }

    /**
     * initializes the checkboxes to the pattern given by the BitSet. If a bit in the
     * BitSet is set (==1) than the corresponding checkbox is set to |1>. The lowest
     * order bit in the BitSet sets the lowest order qubit.
     */
    public void setQubitValue(BitSet bits) {
	for (int i = 0; i < qubits; i++) {
	    ((JCheckBox)qubitBoxes.elementAt(i)).setSelected(bits.get(qubits-i-1));
	}
    }

    /**
     * Returns the index of the row that <I>point</I> lies in, or -1 if is
     * not in the range [0, getQubitNumber()-1].
     *
     * @return  the index of the row that <I>point</I> lies in, or -1 if it
     *          is not in the range [0, getQubitNumber()-1]
     */
    public int qubitAtPoint(Point point) {
        int y = point.y;
	if (labels.size() == 0) return -1;
        int totalRowHeight = ((JLabel)labels.get(0)).getSize().height;
        int result = y/totalRowHeight;
        if (result < 0) {
            return -1;
        }
        else if (result >= getQubitNumber()) {
            return -1;
        }
        else {
            return result;
        }
    }
}


