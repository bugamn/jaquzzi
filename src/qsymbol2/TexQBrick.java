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
 * class extending the QBrick class in order to generate code for the Latex figure
 * environment for each cell. It uses the identification routine of the QBrick class,
 * but overwrites the paint method and adds the method getTexCode(). The drawing of
 * the whole table is done by the GateTable class (the drawing model is much easier
 * than the one implemented by the JTable class).
 * @see QBrick
 * @see GateTable */
public class TexQBrick extends QBrick {

    public static int WIDTH = 50;  // 50
    private static int HALFWIDTH = WIDTH/2;
    public static int HEIGHT = 20; // 20
    private static int HALFHEIGHT = HEIGHT/2; // 10

    public static int FRAMEHEIGHT = 12; // 14
    public static int FRAMEWIDTH = 27; // 16

    public static int VSPACER = (HEIGHT-FRAMEHEIGHT)/2; // 3
    public static int HSPACER = (WIDTH-FRAMEWIDTH)/2; // 17
    private static int HSPACER2 = (WIDTH-FRAMEWIDTH)/2+FRAMEWIDTH; // 17

    private static final int PREP_UP = 200;
    private static final int PREP_DOWN = 500;

    /** 
     * create a brick from a given gate a given row (= qubit) position
     */
    public TexQBrick(JTable table, Gate g, int qubit, boolean selected, int currentStep){
	super(table, g, qubit, selected, currentStep);
	if (state == NOTHING) {
	    if (g.gate_descr.charAt(qubit) == 'u') {
		state = PREP_UP;
	    }
	    else if (g.gate_descr.charAt(qubit) == 'd') {
		state = PREP_DOWN;
	    }
	}
    }

    /**
     * does nothing, simply overwrites the QBrick method
     */
    public void paint(Graphics g) {
    }

    /** 
     * sets the width of each brick
     */
    public static void setBrickWidth(int width) {
	WIDTH = width;  
	HALFWIDTH = WIDTH/2;
	FRAMEWIDTH = (WIDTH+4)/2; 
	HSPACER = (WIDTH-FRAMEWIDTH)/2;
	HSPACER2 = (WIDTH-FRAMEWIDTH)/2+FRAMEWIDTH;
    }

    /**
     * returns the Latex code for the brick.
     */
    public String getTexCode() {
	switch(state) {
	case NOTHING: return "\\begin{picture}("+WIDTH+","+HEIGHT+")\\put(0,"+HALFHEIGHT+"){\\line(1,0){"+WIDTH+"}}\\end{picture}";
	case BOTH: return "\\begin{picture}("+WIDTH+","+HEIGHT+")\\put(0,"+HALFHEIGHT+"){\\line(1,0){"+WIDTH+"}}\\put("+HALFWIDTH+",0){\\line(0,1){"+HEIGHT+"}}\\end{picture}";
	case CONTROL+DOWN: return "\\begin{picture}("+WIDTH+","+HEIGHT+")\\put(0,"+HALFHEIGHT+"){\\line(1,0){"+WIDTH+"}}\\put("+HALFWIDTH+","+HALFHEIGHT+"){\\circle*{2}}\\put("+HALFWIDTH+","+HALFHEIGHT+"){\\line(0,-1){"+HALFHEIGHT+"}}\\end{picture}";
	case CONTROL+UP: return "\\begin{picture}("+WIDTH+","+HEIGHT+")\\put(0,"+HALFHEIGHT+"){\\line(1,0){"+WIDTH+"}}\\put("+HALFWIDTH+","+HALFHEIGHT+"){\\circle*{2}}\\put("+HALFWIDTH+","+HALFHEIGHT+"){\\line(0,1){"+HALFHEIGHT+"}}\\end{picture}";
	case CONTROL+BOTH: return "\\begin{picture}("+WIDTH+","+HEIGHT+")\\put(0,"+HALFHEIGHT+"){\\line(1,0){"+WIDTH+"}}\\put("+HALFWIDTH+","+HALFHEIGHT+"){\\circle*{2}}\\put("+HALFWIDTH+",0){\\line(0,1){"+HEIGHT+"}}\\end{picture}";

	case CONTROLLED:{
	    if (gate.matrixName.equals("NOT"))
		return "\\begin{picture}("+WIDTH+","+HEIGHT+")\\put(0,"+HALFHEIGHT+"){\\line(1,0){"+WIDTH+"}}\\put("+HSPACER+","+VSPACER+"){\\makebox("+FRAMEWIDTH+","+FRAMEHEIGHT+"){x}}\\end{picture}";//[t] before {x} removed.
	    else {
		return "\\begin{picture}("+WIDTH+","+HEIGHT+")\\put(0,"+HALFHEIGHT+"){\\line(1,0){"+HSPACER+"}}\\put("+HSPACER+","+VSPACER+"){\\framebox("+FRAMEWIDTH+","+FRAMEHEIGHT+"){$"+gate.matrixName+"$}}\\put("+HSPACER2+","+HALFHEIGHT+"){\\line(1,0){"+HSPACER+"}}\\end{picture}";
	    }
	   
	}
	case CONTROLLED+UP:{
	    if (gate.matrixName.equals("NOT"))
		return "\\begin{picture}("+WIDTH+","+HEIGHT+")\\put(0,"+HALFHEIGHT+"){\\line(1,0){"+WIDTH+"}}\\put("+HALFWIDTH+","+HALFHEIGHT+"){\\circle{"+FRAMEHEIGHT+"}}\\put("+HALFWIDTH+","+VSPACER+"){\\line(0,1){"+(HEIGHT-VSPACER)+"}}\\end{picture}";
	    else {
		return "\\begin{picture}("+WIDTH+","+HEIGHT+")\\put(0,"+HALFHEIGHT+"){\\line(1,0){"+HSPACER+"}}\\put("+HSPACER+","+VSPACER+"){\\framebox("+FRAMEWIDTH+","+FRAMEHEIGHT+"){$"+gate.matrixName+"$}}\\put("+HSPACER2+","+HALFHEIGHT+"){\\line(1,0){"+HSPACER+"}}\\put("+HALFWIDTH+","+(VSPACER+FRAMEHEIGHT)+"){\\line(0,1){"+VSPACER+"}}\\end{picture}";
	    }
	}
	case CONTROLLED+DOWN:{
	    if (gate.matrixName.equals("NOT"))
		return "\\begin{picture}("+WIDTH+","+HEIGHT+")\\put(0,"+HALFHEIGHT+"){\\line(1,0){"+WIDTH+"}}\\put("+HALFWIDTH+","+HALFHEIGHT+"){\\circle{"+FRAMEHEIGHT+"}}\\put("+HALFWIDTH+",0){\\line(0,1){"+(HEIGHT-VSPACER)+"}}\\end{picture}";
	    else {
		return "\\begin{picture}("+WIDTH+","+HEIGHT+")\\put(0,"+HALFHEIGHT+"){\\line(1,0){"+HSPACER+"}}\\put("+HSPACER+","+VSPACER+"){\\framebox("+FRAMEWIDTH+","+FRAMEHEIGHT+"){$"+gate.matrixName+"$}}\\put("+HSPACER2+","+HALFHEIGHT+"){\\line(1,0){"+HSPACER+"}}\\put("+HALFWIDTH+","+VSPACER+"){\\line(0,-1){"+VSPACER+"}}\\end{picture}";
	    }
	}
	case CONTROLLED+BOTH:{
	    if (gate.matrixName.equals("NOT"))
		return "\\begin{picture}("+WIDTH+","+HEIGHT+")\\put(0,"+HALFHEIGHT+"){\\line(1,0){"+WIDTH+"}}\\put("+HALFWIDTH+","+HALFHEIGHT+"){\\circle{"+FRAMEHEIGHT+"}}\\put("+HALFWIDTH+",0){\\line(0,1){"+HEIGHT+"}}\\end{picture}";
	    else {
		return "\\begin{picture}("+WIDTH+","+HEIGHT+")\\put(0,"+HALFHEIGHT+"){\\line(1,0){"+HSPACER+"}}\\put("+HSPACER+","+VSPACER+"){\\framebox("+FRAMEWIDTH+","+FRAMEHEIGHT+"){$"+gate.matrixName+"$}}\\put("+HSPACER2+","+HALFHEIGHT+"){\\line(1,0){"+HSPACER+"}}\\put("+HALFWIDTH+","+VSPACER+"){\\line(0,-1){"+VSPACER+"}}\\put("+HALFWIDTH+","+(VSPACER+FRAMEHEIGHT)+"){\\line(0,1){"+VSPACER+"}}\\end{picture}";
	    }
	}

	case XCHANGE+UP: return "\\begin{picture}("+WIDTH+","+HEIGHT+")\\put(0,"+HALFHEIGHT+"){\\line(1,0){"+WIDTH+"}}\\put("+HALFWIDTH+","+HEIGHT+"){\\vector(0,-1){"+HALFHEIGHT+"}}\\end{picture}";
	case XCHANGE+DOWN: return "\\begin{picture}("+WIDTH+","+HEIGHT+")\\put(0,"+HALFHEIGHT+"){\\line(1,0){"+WIDTH+"}}\\put("+HALFWIDTH+",0){\\vector(0,1){"+HALFHEIGHT+"}}\\end{picture}";

	case MEASUREMENT: return "\\begin{picture}("+WIDTH+","+HEIGHT+")\\put("+HSPACER+","+VSPACER+"){\\makebox("+FRAMEWIDTH+","+FRAMEHEIGHT+"){!}}\\end{picture}";
	case PREP_UP: return "\\begin{picture}("+WIDTH+","+HEIGHT+")\\put("+HSPACER+","+VSPACER+"){\\makebox("+FRAMEWIDTH+","+FRAMEHEIGHT+"){$\\ket{0}$}}\\end{picture}";
	case PREP_DOWN: return "\\begin{picture}("+WIDTH+","+HEIGHT+")\\put("+HSPACER+","+VSPACER+"){\\makebox("+FRAMEWIDTH+","+FRAMEHEIGHT+"){$\\ket{1}$}}\\end{picture}";

	}
	return "";
    }

}

