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

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.*;
import javax.swing.event.*;

import mathlib.Gate;

import java.util.EventObject;

/**
 * class to provide the preparation gate within the GateTable. This class uses the
 * BrickCheckBox in order to render the qubit preparation.
 * @see BrickCheckBox
 * @see QubitRenderer
 * @see GateTable
 */
public class QubitEditor implements TableCellEditor {
    BrickCheckBox box;
    DefaultCellEditor cellEditor;

    public QubitEditor() {
	box = new BrickCheckBox();
	cellEditor = new DefaultCellEditor(box);
    }

    /**
     * standard method for returning the cell editor. The data transfer is done by
     * the JTable class and the GateTableModel.
     */
    public Component getTableCellEditorComponent(JTable table, Object value,
              boolean isSelected, int row, int column) {
	Boolean flipState;

	if (((GateContainer)value).isLeafContainer()) {
	    GateContainer gateContainer = ((GateContainer)value);
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
		box = new BrickCheckBox(false, table, isSelected, stepKind);
		box.setIsSelected(table, isSelected);
		flipState = new Boolean(false);
		return cellEditor.getTableCellEditorComponent(table, flipState, isSelected, 
							  row, column);
	    }
	    else if (gate.gate_descr.charAt(row) == 'd') {
		box = new BrickCheckBox(true, table, isSelected, stepKind);
		box.setIsSelected(table, isSelected);
		flipState = new Boolean(true);
		return cellEditor.getTableCellEditorComponent(table, flipState, isSelected, 
							  row, column);
	    }
	}
	return null;
    }

    public Object getCellEditorValue() {
	return cellEditor.getCellEditorValue();
    }

    public Component getComponent() {
	return cellEditor.getComponent();
    }
    public boolean stopCellEditing() {
	return cellEditor.stopCellEditing();
    }
    public void cancelCellEditing() {
	cellEditor.cancelCellEditing();
    }
    public boolean isCellEditable(EventObject anEvent) {
	return cellEditor.isCellEditable(anEvent);
    }
    public boolean shouldSelectCell(EventObject anEvent) {
	return cellEditor.shouldSelectCell(anEvent);
    }
    public void addCellEditorListener(CellEditorListener l) {
	cellEditor.addCellEditorListener(l);
    }
    public void removeCellEditorListener(CellEditorListener l) {
	cellEditor.removeCellEditorListener(l);
    }
    public void setClickCountToStart(int n) {
	cellEditor.setClickCountToStart(n);
    }
    public int getClickCountToStart() {
	return cellEditor.getClickCountToStart();
    }

}

