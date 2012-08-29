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

import java.io.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import mathlib.Braket;
import mathlib.Mathlib;

import java.util.*;

public class ChartWindow extends JPanel {

    protected QubitChart chart;
    protected JToolBar toolbar;
    protected JLabel statusBar;
    protected GateTable table;
    protected int[] qubits;

    public ChartWindow(GateTable table, int[] qubits) {
	setLayout(new BorderLayout());
	this.table = table;
	this.qubits = qubits;
	toolbar = new JToolBar(JToolBar.HORIZONTAL);
	toolbar.setFloatable(false);


	JCheckBox track = new JCheckBox("auto", false);
	track.setToolTipText("automatically refresh");
	track.addItemListener(new ItemListener() {
		public void itemStateChanged(ItemEvent e) {
		    if (((JCheckBox)e.getItem()).isSelected()) 
			chart.setListen(true);
		    else chart.setListen(false);
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

	chart = new QubitChart(table, QubitChart.PROBABILITY_CHART, qubits);

	toolbar.add(b);

	toolbar.addSeparator();

	b = new JButton("prob");
	b.setToolTipText("probability chart");
	b.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    setChartType(QubitChart.PROBABILITY_CHART);
		}
	    });

	toolbar.add(b);

	b = new JButton("re()");
	b.setToolTipText("real amplitude chart");
	b.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    setChartType(QubitChart.REAMPLITUDE_CHART);
		}
	    });
	toolbar.add(b);
	b = new JButton("im()");
	b.setToolTipText("imaginary amplitude chart");
	b.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    setChartType(QubitChart.IMAMPLITUDE_CHART);
		}
	    });

	toolbar.add(b);

	toolbar.addSeparator();
	b = new JButton("fidelity");
	b.setToolTipText("|<Psi|Psi_error>|^2");
	b.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    setChartType(QubitChart.FIDELITY_CHART);
		}
	    });
	toolbar.add(b);

	toolbar.addSeparator();
	b = new JButton("data");
	b.setToolTipText("show the numerical values");
	b.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    jaQuzzi.dataWindow.setQubits(getQubits());
		    switch(chart.getType()) {
		    case QubitChart.PROBABILITY_CHART: jaQuzzi.dataWindow.setVar("prob"); break;
		    case QubitChart.REAMPLITUDE_CHART: jaQuzzi.dataWindow.setVar("qubits"); break;
		    case QubitChart.IMAMPLITUDE_CHART: jaQuzzi.dataWindow.setVar("qubits"); break;
		    case QubitChart.FIDELITY_CHART: jaQuzzi.dataWindow.setVar("fidelity"); break;
		    }
		    
		    if (jaQuzzi.dataWindowFrame.isVisible() == false)
			jaQuzzi.dataWindowFrame.setVisible(true);
		    else jaQuzzi.dataWindow.refresh();
		}
	    });
	toolbar.add(b);

	final GateTable localTable = table;
	b = new JButton("dump");
	b.setToolTipText("writes the data to a file");
	b.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    if (jaQuzzi.file == null)
			((GateTableModel)localTable.getModel()).dumpData(new File("untitled.jaq"),getQubits());
		    else
			((GateTableModel)localTable.getModel()).dumpData(jaQuzzi.file, getQubits());
		}
	    });
	toolbar.add(b);

	statusBar = new JLabel("");
	String status = "";
	for (int i = 0; i < qubits.length; i++)
	    status = status.concat(table.getQubitName(qubits[i])+" ");
	statusBar.setBorder(BorderFactory.createLoweredBevelBorder());
	statusBar.setText("qubits: "+status);

	add(toolbar, "North");
	add(chart, "Center");
	add(statusBar, "South");
    }

    public void refresh() {
	chart.refresh();
    }

    public void setVisible(boolean visible) {
	super.setVisible(visible);
	chart.setVisible(visible);
    }

    public void setQubits(int[] qubits) {
	if (qubits == null) {
	    int n = ((Braket)Mathlib.getVar(GatePanel.qubits)).n;
	    int[] q = new int[n];
	    for (int i = 0; i < n; i++) q[i] = i;
	    chart.setQubits(q);
	    statusBar.setText("qubits: all");
	    this.qubits = q;
	}
	else {
	    String status = "";
	    for (int i = 0; i < qubits.length; i++)
		status = status.concat(table.getQubitName(qubits[i])+" ");
	    chart.setQubits(qubits);
	    statusBar.setText("qubits: "+status);
	    this.qubits = qubits;
	}
    }

    public int[] getQubits() {
	return qubits;
    }

    public void setChartType(int type) {
	chart.setType(type);
    }


}
