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

import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;

/**
 * class derived from the JCheckBox class. It is modified in a way that it represents
 * a qubit preparation control within the GateTable framework. E.g., it was modiefied
 * to draw the step marker.
 * @see QBrick
 */
public class BrickCheckBox extends JCheckBox {
    /** hold the information whether the step marker should be painted to the left or
     * right */
    protected int stepKind;

    public BrickCheckBox() {
	super(".");
	stepKind = 0;
	setIcon(jaQuzzi.qubitupIcon);
	setText("");
	setSelectedIcon(jaQuzzi.qubitdownIcon);
	setHorizontalAlignment(JLabel.RIGHT);
	setForeground(UIManager.getColor("Table.foreground"));
	setBackground(UIManager.getColor("Table.background"));
    }

    public BrickCheckBox(boolean up, JTable table, boolean isSelected,
			 int stepKind) {
	super(".");
	this.stepKind = stepKind;
	setIcon(jaQuzzi.qubitupIcon);
	setText("");
	setSelectedIcon(jaQuzzi.qubitdownIcon);
	setHorizontalAlignment(JLabel.RIGHT);
	//	setHorizontalTextPosition(JCheckBox.RIGHT);

	setSelected(!up);
	//	setText("|0>");

	if (isSelected) {
	    setForeground(table.getSelectionForeground());
	    setBackground(table.getSelectionBackground());
	}
	else {
	    setForeground(UIManager.getColor("Table.foreground"));
	    setBackground(UIManager.getColor("Table.background"));
	}
    }

    public Dimension getMinimumSize() {
	return new Dimension(20, 15);
    }

    public Dimension getPreferredSize() {
	return new Dimension(50, 20);
    }

    public void paint(Graphics g) {
	super.paint(g);
	Graphics2D g2 = (Graphics2D)g;
	Dimension d = getSize();

	if (stepKind == 1) {
	    g2.setPaint(Color.red);
	    g2.draw(new Line2D.Double(d.width-1, 0, d.width-1, d.height));
	} else if (stepKind == -1) {
	    g2.setPaint(Color.red);
	    g2.draw(new Line2D.Double(0, 0, 0, d.height));
	}

    }

    /**
     * set the selection status. The colors are changed accordingly.
     */
    public void setIsSelected(JTable table, boolean isSelected) {
	if (isSelected) {
	    setForeground(table.getSelectionForeground());
	    setBackground(table.getSelectionBackground());
	}
	else {
	    setForeground(UIManager.getColor("Table.foreground"));
	    setBackground(UIManager.getColor("Table.background"));
	}
    }
}

