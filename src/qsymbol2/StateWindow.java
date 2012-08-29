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
import javax.swing.*;

import mathlib.Braket;
import mathlib.ComputationEvent;
import mathlib.ComputationEventListener;
import mathlib.Easy;
import mathlib.Mathlib;
import mathlib.MathlibEvent;
import mathlib.MathlibEventListener;
import mathlib.Measurement;
import mathlib.Parse;

import java.util.*;

public class StateWindow extends JPanel implements MathlibEventListener, ComputationEventListener {
    protected Mathlib mathlib;
    protected GateTable table;
    protected String var;

    protected JTextArea textArea;
    protected JScrollPane scrollPane;

    protected boolean listen;
    protected int[] qubits;

    public StateWindow(GateTable table, String var, int[] qubits) {
	Parse.addMathlibEventListener(this);
	Mathlib.addComputationEventListener(this);
	this.table = table;
	this.mathlib = table.getMathlib();
	this.var = var;
	this.qubits = qubits;
	this.listen = false;

	textArea = new JTextArea(0,0);
	textArea.setEditable(false);
	textArea.setLineWrap(true);

	scrollPane = new JScrollPane(textArea);

	setLayout(new BorderLayout());
	add(scrollPane, BorderLayout.CENTER);
    }

    public void setListen(boolean listen) {
	this.listen = listen;
    }
    public void setQubits(int[] qubits) {
	this.qubits = qubits;

	// update data vector
	updateText();
    }

    public void setVar(String var) {
	this.var = var;
	updateText();
    }

    public void refresh() {
	updateText();
    }

    protected void updateText() {
	if ("fidelity".equals(var)) {
	    String str = new String("");
	    str = str.concat("Fidelity:\n");
	    Vector v = table.getFidelityVector();
	    for (int i = 0; i< v.size(); i++) {
		str = str.concat(Easy.format(((Double)v.elementAt(i)).doubleValue(),5) + " ");
	    }
	    textArea.setText(str);
	}
	else if ("prob".equals(var)) {
	    String str = new String("");
	    str = str.concat("probability distribution:\n");
	    Vector v = Measurement.getProbDistribution((Braket)Mathlib.getVar(GatePanel.qubits), qubits);
	    for (int i = 0; i< v.size(); i++) {
		str = str.concat(Easy.format(((Double)v.elementAt(i)).doubleValue(),5) + " ");
	    }
	    textArea.setText(str);
	}
	else if ("probIdeal".equals(var)) {
	    String str = new String("");
	    str = str.concat("ideal probability distribution:\n");
	    Braket q = ((Mathlib.getVar(GatePanel.refQubits) == null)? (Braket)Mathlib.getVar(GatePanel.qubits): (Braket)Mathlib.getVar(GatePanel.refQubits));
	    Vector v = Measurement.getProbDistribution(q, qubits);
	    for (int i = 0; i< v.size(); i++) {
		str = str.concat(Easy.format(((Double)v.elementAt(i)).doubleValue(),5) + " ");
	    }
	    textArea.setText(str);
	}
	else textArea.setText(var+"\n"+mathlib.getVar(var).toString());
    }

    public void setVisible(boolean visible) {
	super.setVisible(visible);
	updateText();
    }


    // MathlibEventListener methods

    public void addVariable(MathlibEvent e) {
	if (e.getObjectName().equals(var) && isVisible() && listen) {
	    updateText();
	}
    }

    public void changeVariable(MathlibEvent e) {
	if (e.getObjectName().equals(var) && isVisible() && listen) {
	    updateText();
	}
    }

    public void removeVariable(MathlibEvent e) {}

    // ComputationEventListener methods

    public void computationEvent(ComputationEvent e) {
	if (isVisible() && e.getAction() == ComputationEvent.DONE && listen) {
	    updateText();
	}
    }

}

