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

import java.util.*;
import java.awt.*;
import java.awt.geom.*;
import javax.swing.*;
import javax.swing.table.*;

import mathlib.Gate;
/**
 * this class is derived from the DefaulTableCellRenderer in order to be able to
 * render the GateContainers correctly. Since one column of the GateTable can
 * contain a group of gateContainers, it is necessary to render multiple cells.
 * The component used to draw one cell is the QBrick class, for multiple rendering,
 * the QBricks are added to a Panel. The BrickCheckBox is responsible for rendering a
 * preparation gate.
 * @see GateTable
 * @see QubitHeaderRenderer
 */
public class QubitRenderer extends DefaultTableCellRenderer {

    /**
     * this method of the mother object is overwritten
     */
    public Component getTableCellRendererComponent(JTable table,
						   Object value,
						   boolean isSelected,
						   boolean hasFocus,
						   int row,
						   int column) {
	GateContainer gateContainer = (GateContainer)value;
	if (gateContainer.isLeafContainer()) {
	    Gate gate = gateContainer.getGate();

	    // determine whether is step
	    int stepKind = 0;
	    int direction = ((GateTable)table).getDirection();
	    if (gateContainer.hasStepFocus()) {
		if (((GateTable)table).getEndOfTable()) stepKind = 1;
		else {
		    if (gateContainer.getCurrentIteration() > 0 && direction == -1) 
			stepKind = 1;
		    else stepKind = -1;
		}
	    }

	    if (gate.gate_descr.charAt(row) == 'u') {
		return new BrickCheckBox(true, table, isSelected, stepKind);
	    }
	    else if (gate.gate_descr.charAt(row) == 'd') {
		return new BrickCheckBox(false, table, isSelected, stepKind);
	    }
	    else {
		return new QBrick(table, gate, row, isSelected, stepKind);
	    }
	}
	// multiple gates..
	else {
	    JLabel qbricks = new JLabel();
	    qbricks.setLayout(new GridLayout(1,0));
	    Enumeration leaves = ((GateContainer)value).getLeaves();
	    GateContainer gc;
	    Gate gate;
	    int j = 0;
	    int stepKind = 0;
	    int direction = ((GateTable)table).getDirection();
	    while (leaves.hasMoreElements()) {
		gc = ((GateContainer)leaves.nextElement());
		gate = gc.getGate();

		// determine step
		if (gc.isStep()) {
		    if (((GateTable)table).getEndOfTable()) 
			stepKind = 1;
		    else {
			if (gc.getCurrentIteration() > 0 && direction == -1)
			    stepKind = 1;
			else stepKind = -1;
		    }
		} else stepKind = 0;

		if (gate.gate_descr.charAt(row) == 'u') {
		    qbricks.add(new BrickCheckBox(true, table, isSelected, stepKind));
		}
		else if (gate.gate_descr.charAt(row) == 'd') {
		    qbricks.add(new BrickCheckBox(false, table, isSelected, stepKind));
		}
		else {
		    qbricks.add(new QBrick(table, gate, row, isSelected, stepKind));
		}
		j++;
	    }
	    return qbricks;
	}
    }


}


