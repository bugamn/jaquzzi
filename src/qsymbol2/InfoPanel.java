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
import javax.swing.*;

import mathlib.Easy;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;

/**
 * A class providing a clickable info panel. It can display up to two info lines at a
 * time. A single click to the panel reveals the next two info lines. The info lines
 * can be thought of being in a queue. This class is used in the toolbar of the
 * jaQuzzi applet to display information like the current step, stepCount, time
 * estimates etc.
 * @see edu.buffalo.fs7.jaQuzzi.jaQuzzi
 */
public class InfoPanel extends JPanel {
    /** holds the labels */
    protected Vector keys;
    protected Vector values;
    protected int index;

    /**
     * creates an empty infoPanel
     */
    public InfoPanel() {
	keys = new Vector();
	values = new Vector();
	index = 0;

	setBorder(BorderFactory.createEtchedBorder());
	addMouseListener(new ClickListener(this));
    }

    /**
     * adds a new info label - info value pair to the panel
     */
    public void put(String key, Object value){
	if (key != null && value != null) {
	    keys.add(key);
	    values.add(value);
	}
    }

    /**
     * overwrites an existing info label - info value pair with a new info value
     */
    public void set(String key, Object value) {
	if (key != null && value != null) {
	    int pos = keys.indexOf(key);
	    if (pos >= 0) values.setElementAt(value, pos);
	}
    }

    /**
     * brings up the next two info lines.
     */
    protected void nextStringSet() {
	int mod = (keys.size() % 2 != 0) ? keys.size()+1 : keys.size();
	if (keys.size() == 0) index = -1;
	else index = (index+2) % mod;
    }

    public Dimension getPreferredSize(){
	return new Dimension(130, 30);
    }

    public Dimension getMinimumSize() {
	return getPreferredSize();
    }
    
    /**
     * paints a two line info panel
     */
    public void paint(Graphics g) {
	super.paint(g);
	Graphics2D g2 = (Graphics2D) g;
	Dimension d = getSize();
	g2.setPaint(Color.black);

	g2.setFont(g2.getFont().deriveFont(new Double(d.height/3.0).floatValue()));
	FontMetrics fm = g2.getFontMetrics();

	int h = fm.getHeight();

	String display;
	Object o;
	if (keys.size() > index && values.size() > index) {
	    o = values.elementAt(index);
	    if (o instanceof Double) display = Easy.format(((Double)o).doubleValue(),4);
	    else display = o.toString();

	    g2.drawString((String)keys.elementAt(index)+": "+display,4, h-2);
	}
	if (keys.size() > index+1 && values.size() > index+1) {
	    o = values.elementAt(index+1);
	    if (o instanceof Double) display = Easy.format(((Double)o).doubleValue(),4);
	    else display = o.toString();

	    g2.drawString((String)keys.elementAt(index+1)+": "+display, 4, d.height-5);
	}
    }
    /**
     * implements the clickability of the panel
     */    
    protected class ClickListener extends MouseAdapter {
	JPanel parent;
	public ClickListener(JPanel parent) {
	    this.parent = parent;
	}
	public void mouseClicked(MouseEvent e) {
	    if (e.getComponent().equals(parent)) {
		nextStringSet();
		parent.repaint();
	    }
	}
    }

}
