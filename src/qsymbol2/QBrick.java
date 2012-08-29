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
import java.awt.geom.*;
import javax.swing.*;

import mathlib.Gate;

/**
 * this class is used by the GateTable to render one cell. The standard renderer had to
 * be overwritten in order to represent a quantum circuit. The GateTable has one
 * object for each column - a GateContainer. In case of a redraw it calls its
 * CellRenderer, in case of the GateTable the QubitRenderer class. The renderer is
 * responsible for drawing a certain cell. In case of the QubitRenderer, this task is
 * delegated to the QBrick because cells with multiple gates can occur. It is the
 * task of the QBrick class to determine how to draw itself. This is done by the
 * constructor which needs information provided by the JTable class.
 * @see QubitRenderer
 * @see GateTable
 * @see TexQBrick
 */
public class QBrick extends JPanel{
    /** state constant */
    public static final int NOTHING = 0;
    /** state constant */
    public static final int UP = 1;
    /** state constant */
    public static final int DOWN = 2;
    /** state constant */
    public static final int BOTH = 3;

    /** state constant */
    public static final int CONTROL = 10;
    /** state constant */
    public static final int CONTROLLED = 20;

    /** state constant */
    public static final int MEASUREMENT = 50;
    /** state constant */
    public static final int XCHANGE = 100;

    /** the drawing table (needs to be GateTable) */
    protected JTable table;

    /** the integer state of this QBrick*/
    protected int state;
    /** true if the cell is selected */
    protected boolean selected;
    /** allowed values -1 (draw step on the left), 0 (draw no step), 1 (draw step on
     * the right)
     */
    protected int currentStep;
    /** stores the gate */
    protected Gate gate;

    /**
     * creates a brick with a line in the middle 
     */
    public QBrick() {
	this.state = NOTHING;
	selected = false;
    }

    /** 
     * creates a brick with a given state (see the constants of this class)
     */
    public QBrick(int state) {
	this.state = state;
    }

    /**
     * identifies the brick from the information provided. This information is
     * usually provided by the JTable to a CellRenderer.
     * @param table the drawing table (needs to be GateTable)
     * @param g the gate to draw a cell of
     * @param qubit the qubit to draw
     * @param selected cell is selected
     * @param currentStep information about the step
     * @see QBrick#currentStep
     */
    public QBrick(JTable table, Gate g, int qubit, boolean selected, 
		  int currentStep) {
	this.table = table;
	this.selected = selected;
	this.currentStep = currentStep;
	this.gate = g;
	if (qubit < g.gate_descr.length()) {
	    state = 0;

	    int m_pos = g.gate_descr.indexOf('m');
	    int first_1 = g.gate_descr.indexOf('1');
	    int last_1 = g.gate_descr.lastIndexOf('1');
	    int first_m = g.gate_descr.indexOf('m');
	    int last_m = g.gate_descr.lastIndexOf('m');
	    int measurement = g.gate_descr.indexOf('!');

	    if (measurement == qubit) state = MEASUREMENT;
	    else if (m_pos != last_m && "xChange".equals(g.matrixName)) {
		if (first_m == qubit || last_m== qubit) state = XCHANGE;
	    }
	    else if (g.gate_descr.charAt(qubit) == '1') state += CONTROL;
	    else if (g.gate_descr.charAt(qubit) == 'm') state += CONTROLLED;

	    if (state != XCHANGE && m_pos >= 0 && first_1 >= 0 && last_1 >= 0) {

		if (qubit == first_1 && first_1 < m_pos) state += DOWN;
		else if (qubit == last_1 && first_1 > m_pos) state += UP;
		else if (qubit == last_1 && last_1 > m_pos && first_1 < m_pos) state += UP;
		else if (qubit == m_pos && first_1 > m_pos) state += DOWN;
		else if (qubit == m_pos && last_1 < m_pos) state += UP;
		else if (qubit < first_1 && qubit < m_pos) state = state;
		else if (qubit > last_1 && qubit > m_pos) state = state;
		else state += BOTH;
	    }
	    // xChange gate
	    else if (first_m != last_m && first_m >= 0 && last_m >= 0) {
		if (qubit == first_m) state += DOWN;
		else if (qubit == last_m) state += UP;
		else if (qubit > first_m && qubit < last_m) state += (DOWN + UP);
	    }
	    //	    System.out.println(m_pos + " first_1: " + first_1 + " last_1:" + last_1 + " state: " + state + " first_m: " + first_m+ " last_m: "+last_m);
	}
	else state = 0;
    }

    public Dimension getMinimumSize() {
	return new Dimension(20, 15);
    }

    public Dimension getPreferredSize() {
	return new Dimension(50, 20);
    }

    /**
     * responsible to draw the state specified by this class
     */
    public void paint(Graphics g) {
	Graphics2D g2 = (Graphics2D) g;
	Dimension d = getSize();
	int temp_state = state;
	boolean controlled = false;

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

	//	if (selected) g2.setPaint(table.getSelectionBackground());
       	if (selected) g2.setPaint(Color.red);
      	else g2.setPaint(Color.white);
	g2.fill(new Rectangle2D.Double(0, 0, d.width, d.height));

	if (currentStep == 1) {
	    g2.setPaint(Color.red);
	    g2.draw(new Line2D.Double(d.width-1, 0, d.width-1, d.height));
	}
	else if (currentStep == -1) {
	    g2.setPaint(Color.red);
	    g2.draw(new Line2D.Double(0, 0, 0, d.height));
	}


	g2.setPaint(Color.black);
	g2.draw(new Line2D.Double(0, d.height/2.0, d.width, d.height/2.0));

	if (temp_state - XCHANGE >= 0) {
	    temp_state -= XCHANGE;
	    double r = Math.min(d.width, d.height)/6.0;
	    double tip_h = r;

	    // flip
	    if (temp_state-UP == 0) tip_h = - tip_h;

	    g2.draw(new Line2D.Double(d.width/2.0-r, d.height/2.0+tip_h, d.width/2.0, d.height/2.0));
	    g2.draw(new Line2D.Double(d.width/2.0, d.height/2.0, d.width/2.0+r, d.height/2.0+tip_h));
	}
	else if (temp_state - MEASUREMENT >= 0) {
	    temp_state -= MEASUREMENT;
	    int h_1_4 = d.height/8;
	    int h_1_8 = d.height/16;
	    int w_1_4 = d.width/16;
	    int w_1_8 = d.width/32;

	    Polygon lightning = new Polygon();
	    lightning.addPoint(2*w_1_4, h_1_4);
	    lightning.addPoint(w_1_4-w_1_8, 2*h_1_4);
	    lightning.addPoint(w_1_4+w_1_8, 2*h_1_4);
	    lightning.addPoint(w_1_8, 3*h_1_4);
	    lightning.addPoint(4*w_1_4-w_1_8, h_1_4+h_1_8);
	    lightning.addPoint(3*w_1_4, h_1_4+h_1_8);
	    lightning.addPoint(4*w_1_4, h_1_4);
	    lightning.translate(3*d.width/8, 0);

	    g2.setPaint(Color.yellow);
	    g2.fill(lightning);
	    g2.setPaint(Color.red);
	    g2.draw(lightning);

	}
	// single NOT gate
	else if (temp_state - CONTROLLED == 0 && gate.matrixName.equals("NOT")) {
	    temp_state -= CONTROLLED;
	    g2.setFont(g2.getFont().deriveFont(new Double(Math.min(d.width, d.height)/3.0).floatValue()));
	    g2.drawString("x", new Double(d.width/2.0).floatValue(), new Double(d.height/2.0 -d.height/10.0).floatValue());
	}
	// controlled NOT gate or single/controlled operator
	else if (temp_state - CONTROLLED >= 0) {
	    temp_state -= CONTROLLED;

	    // NOT gate
	    if (gate.matrixName.equals("NOT")) {
		double r = Math.min(d.width, d.height)/4.0;
		g2.draw(new Arc2D.Double(d.width/2.0-r, d.height/2.0-r, 2*r, 2*r, 0, 360, Arc2D.CHORD));
		g2.draw(new Line2D.Double(d.width/2.0, d.height/2.0-r, d.width/2.0, d.height/2.0+r));
	    }
	    else {
		controlled = true;
		double r = Math.min(d.width, d.height)/3.0;

		if (selected) g2.setPaint(table.getSelectionBackground());
		else g2.setPaint(Color.white);
		g2.fill(new Rectangle2D.Double(d.width/2.0-r, d.height/2.0-r, 2*r, 2*r));

		g2.setPaint(Color.black);
		g2.draw(new Rectangle2D.Double(d.width/2.0-r, d.height/2.0-r, 2*r, 2*r));

		g2.setFont(g2.getFont().deriveFont(new Double(Math.min(d.width, d.height)/3.0).floatValue()));
		FontMetrics fm = g2.getFontMetrics();

		String display = gate.matrixName;

		int w = fm.stringWidth(display);
		int h = fm.getHeight();

		// font too big
		if (w > 2*r) {
		    g2.setFont(g2.getFont().deriveFont(new Double(Math.min(d.width, d.height)/5.0).floatValue()));
		    fm = g2.getFontMetrics();
		    w = fm.stringWidth(display);
		    h = fm.getHeight();
		}


		g2.drawString(display, new Double((d.width-w)/2.0).floatValue(), new Double(d.height/2.0+h/4.0).floatValue());
	    }
	}
	if (temp_state - CONTROL >= 0) {
	    temp_state -= CONTROL;
	    double r = Math.min(d.width, d.height)/8.0;
	    g2.fill(new Arc2D.Double(d.width/2.0-r, d.height/2.0-r, 2*r, 2*r, 0, 360, Arc2D.CHORD));
	}

	if (temp_state - DOWN >= 0) {
	    temp_state -= DOWN;
	    double r = Math.min(d.width, d.height)/3.0;
	    if (controlled) 
		g2.draw(new Line2D.Double(d.width/2.0, d.height/2.0+r, d.width/2.0, d.height));
	    else 
		g2.draw(new Line2D.Double(d.width/2.0, d.height/2.0, d.width/2.0, d.height));
	}

	if (temp_state - UP >= 0) {
	    temp_state -= UP;
	    double r = Math.min(d.width, d.height)/3.0;
	    if (controlled)
		g2.draw(new Line2D.Double(d.width/2.0, 0, d.width/2.0, d.height/2.0-r));
	    else
		g2.draw(new Line2D.Double(d.width/2.0, 0, d.width/2.0, d.height/2.0));
	}

    }

}
