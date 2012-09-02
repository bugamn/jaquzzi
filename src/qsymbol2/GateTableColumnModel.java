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
import java.awt.event.*;
import javax.swing.table.*;
import javax.swing.*;

import mathlib.GateProperty;
import mathlib.MathObject;

/**
 * class replacing the DefaultTableColumnModel for the GateTable class. It implements
 * methods to add and remove gateContainers to the ColumnModel and sets the correct
 * renderer classes. It as well implements
 * methods to increase and decrease the width of the table columns.
 * @see GateTable
 * @see GateTableModel
 * @see QubitRenderer
 * @see QubitHeaderRenderer
 */
public class GateTableColumnModel extends DefaultTableColumnModel {
    protected TableCellRenderer renderer;
    protected TableCellRenderer headerRenderer;
    protected int columnWidth = 60;

    /**
     * creates the GateTableColumnModel
     */
    public GateTableColumnModel() {
	super();
	renderer = new QubitRenderer();
	headerRenderer = new QubitHeaderRenderer();
    }

    /**
     * creates a TableColumn object for the given GateContainer object. Sets the cell
     * renderer to QubitRenderer and the HeaderRenderer to QubitHeaderRenderer. Sets
     * the column header to the top level name of the gateContainer.
     * @see GateContainer
     */
    public void addGateContainer(GateContainer gateContainer) {
	TableColumn aColumn = new TableColumn(getColumnCount(), 75, renderer, null);
	aColumn.setHeaderRenderer(headerRenderer);

	GateProperty gp = gateContainer.getPropertyObject();
	if (gp != null) {
	    MathObject name = gp.getProperty("name");
	    if (name != null) 
		aColumn.setHeaderValue(name);
	    else 
		aColumn.setHeaderValue(gateContainer.getTopLevelName());
	} 
	else
	    aColumn.setHeaderValue(gateContainer.getTopLevelName());
	aColumn.setResizable(false);
	//	System.out.println(gateContainer.getLeafCount());
	aColumn.setPreferredWidth(columnWidth*gateContainer.getLeafCount());
	addColumn(aColumn);
    }

    /**
     * removes the correspondig TableColumn object from the TableColumn model.
     */
    public void removeGateContainer(GateContainer gateContainer) {
	//	try {
	    TableColumn aColumn = getColumn(getColumnIndex(gateContainer.getTopLevelName()));
	    TableColumn bColumn;
	    int modelIndex = aColumn.getModelIndex();
	    int bModelIndex;
	    Enumeration columns = getColumns();
	    while(columns.hasMoreElements()) {
		bColumn = (TableColumn)columns.nextElement();
		bModelIndex = bColumn.getModelIndex();
		bColumn.setModelIndex((bModelIndex > modelIndex)? --bModelIndex : bModelIndex);
	    }
	    removeColumn(aColumn);
	    getSelectionModel().clearSelection();
	    /*		
	} catch (Exception e) {
	    System.out.println("unexpected exception: " + e.getMessage());
}
	    */
    }

    /**
     * returns the preferred width of a column
     */
    public int getPreferredColumnWidth() {
	return columnWidth;
    }

    /**
     * changes the column width of each column. This method takes care that grouped
     * columns do have the correct width (n*single column).
     */  
    public void changeColumnWidth(int delta) {
	TableColumn bColumn;
	int columns;
	int newColumnWidth = (((columnWidth + delta) > 15) ? (columnWidth+delta):columnWidth);
	Enumeration enumeration = getColumns();
	while(enumeration.hasMoreElements()) {
	    bColumn = (TableColumn)enumeration.nextElement();
	    columns = bColumn.getPreferredWidth()/columnWidth;
	    bColumn.setPreferredWidth(newColumnWidth*columns);
	}
	columnWidth = newColumnWidth;
    }
  
}
