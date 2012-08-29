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

import mathlib.Braket;
import mathlib.Mathlib;

import java.util.*;


public class DataWindow extends JPanel {

    protected StateWindow stateWindow;
    protected JToolBar toolbar;
    protected JLabel statusBar;
    protected GateTable table;
    protected Mathlib mathlib;

    protected JButton qubitsB;
    protected JButton refQubits;
    protected JButton fidelity;
    protected String var;

    public DataWindow(GateTable table, String var, int[] qubits) {
	setLayout(new BorderLayout());
	this.table = table;
	toolbar = new JToolBar(JToolBar.HORIZONTAL);
	toolbar.setFloatable(false);
	this.mathlib = table.getMathlib();
	this.var = var;

	JCheckBox track = new JCheckBox("auto", false);
	track.setToolTipText("automatically refresh");
	track.addItemListener(new ItemListener() {
		public void itemStateChanged(ItemEvent e) {
		    if (((JCheckBox)e.getItem()).isSelected()) 
			stateWindow.setListen(true);
		    else stateWindow.setListen(false);
		}
	    });

	toolbar.add(track);

	JButton b = new JButton("refresh");
	b.setToolTipText("causes the graph to reload the data");
	b.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    refresh();
		}
	    });

	stateWindow = new StateWindow(table, var, qubits);

	toolbar.add(b);

	toolbar.addSeparator();

	qubitsB = new JButton("state");
	qubitsB.setToolTipText("state vector (with errors if applicable)");
	qubitsB.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    setVar(GatePanel.qubits);
		}
	    });

	toolbar.add(qubitsB);

	b = new JButton("prob");
	b.setToolTipText("probability distribution (with errors if applicable)");
	b.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    setVar("prob");
		}
	    });

	toolbar.add(b);
	toolbar.addSeparator();

	refQubits = new JButton("state (ideal)");
	refQubits.setToolTipText("ideal state vector");
	refQubits.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    if (Mathlib.getVar(GatePanel.refQubits) != null) {
			setVar(GatePanel.refQubits);
		    }
		    else {
			setVar(GatePanel.qubits);
		    }
		}
	    });
	toolbar.add(refQubits);

	b = new JButton("prob (ideal)");
	b.setToolTipText("ideal probability distribution");
	b.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    setVar("probIdeal");
		}
	    });

	toolbar.add(b);
	toolbar.addSeparator();


	fidelity = new JButton("fidelity");
	fidelity.setToolTipText("shows the fidelity");
	fidelity.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    setVar("fidelity");
		}
	    });

	toolbar.add(fidelity);

	statusBar = new JLabel("");
	String status = "";
	for (int i = 0; i < qubits.length; i++)
	    status = status.concat(table.getQubitName(qubits[i])+" ");
	statusBar.setBorder(BorderFactory.createLoweredBevelBorder());
	statusBar.setText("qubits: "+status);

	add(toolbar, "North");
	add(stateWindow, "Center");
	add(statusBar, "South");
    }

    public void setVisible(boolean visible) {
	super.setVisible(visible);
	stateWindow.setVisible(visible);
    }

    public void setQubits(int[] qubits) {
	if (qubits == null) {
	    int n = ((Braket)Mathlib.getVar(GatePanel.qubits)).n;
	    int[] q = new int[n];
	    for (int i = 0; i < n; i++) q[i] = i;
	    stateWindow.setQubits(q);
	    statusBar.setText("qubits: all");
	}
	else {
	    String status = "";
	    for (int i = 0; i < qubits.length; i++)
		status = status.concat(table.getQubitName(qubits[i])+" ");
	    stateWindow.setQubits(qubits);
	    statusBar.setText("qubits: "+status);
	}
    }

    public void setVar(String var) {
	stateWindow.setVar(var);
    }

    public void refresh() {
	stateWindow.refresh();
    }


}
