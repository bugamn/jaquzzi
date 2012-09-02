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
import javax.swing.*;
import javax.swing.table.*;

/**
 * class for drawing the column header of the GateTable table. The standard
 * HeaderRenderer needs to be overwritten in order to provide the correct headers for
 * the groupable gatecontainers.
 * @see QubitRenderer
 * @see GateTable
 */
public class QubitHeaderRenderer extends JPanel implements TableCellRenderer {

    GridBagLayout gridbag;
    GridBagConstraints c;

    QubitHeaderRenderer() {
	gridbag = new GridBagLayout();
	c = new GridBagConstraints();
	c.fill = GridBagConstraints.BOTH;
	c.weightx = 1; c.weighty = 1;
	setLayout(gridbag);
    }

    /**
     * in order to render the header correctly, the method analyses the GateContainer
     * for possible children. It uses a GridBagLayout to arrange single JLabels in
     * the JPanel implemented by the class itself.
     */
    public Component getTableCellRendererComponent(JTable table,
						   Object value,
						   boolean isSelected,
						   boolean hasFocus,
						   int row,
						   int column) {
	removeAll();
	if (table != null) {
	    JTableHeader header = table.getTableHeader();
	    if (header != null) {
		String columnName = (String)value;
	
		GateContainer rootContainer = 
		    ((GateTableModel)table.getModel()).getGateContainer(columnName);
		Enumeration enumeration;
		JLabel label;
		GateContainer container;
		int j = 0;
		int x = 0;
		int y = 0;
		String headerText = new String("");
		Object o;
		int depth = ((GateTable)table).getMaxContainerDepth();
		for (int i = 0; i <= depth; i++) {
		    enumeration = rootContainer.getChildsOnLevel(i);
		    j = 0;
		    while(enumeration.hasMoreElements()) {
			j++;
			o = enumeration.nextElement();
			if (o instanceof String)
			    x++;
			else {
			    container = (GateContainer)o;
			    headerText = new String("");
			    if (container.getMaxIteration() > 1) {
				headerText = ("["+container.getCurrentIteration()+"/"+container.getMaxIteration()+"] ");
			    }
			    headerText = headerText.concat(container.getTopLevelName());
			    label = new HeaderLabel(headerText);
			    label.setForeground(UIManager.getColor("TableHeader.foreground"));
			    if (container.getMaxIteration() > 1) 
				label.setBackground(Color.blue);
			    else
				label.setBackground(UIManager.getColor("TableHeader.background"));
			    label.setBorder(UIManager.getBorder("TableHeader.cellBorder"));
			    label.setHorizontalAlignment(JLabel.CENTER);
			    label.setFont(header.getFont());
			    c.gridwidth = container.getLeafCount();
			    c.gridx = x;
			    x += c.gridwidth;
			    c.gridy = y;
			    if (container.isLeafContainer()) c.gridheight = 0;
			    else c.gridheight = 1;

			    gridbag.setConstraints(label, c);
			    add(label);
			}
		    }
		    if (j > 0) { x= 0; y++; }
		    else break;
		}
	    }
	}
	return this;
    }

    /**
     * special Label class with predefined dimensions.
     */
    class HeaderLabel extends JLabel {
	HeaderLabel(String text) {
	    super(text);
	}

	public Dimension getMinimumSize() {
	    return new Dimension(20, super.getMinimumSize().height);
	}

	public Dimension getPreferredSize() {
	    return new Dimension(50, super.getPreferredSize().height);
	}
    }

}


