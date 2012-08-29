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

import mathlib.Braket;
import mathlib.Complex;
import mathlib.ComputationEvent;
import mathlib.ComputationEventListener;
import mathlib.Mathlib;
import mathlib.MathlibEvent;
import mathlib.MathlibEventListener;
import mathlib.Measurement;
import mathlib.Parse;

public class QubitChart extends Histogram implements MathlibEventListener, ComputationEventListener {
    public static final int PROBABILITY_CHART = 0;
    public static final int REAMPLITUDE_CHART = 1;
    public static final int IMAMPLITUDE_CHART = 2;
    public static final int FIDELITY_CHART = 3;

    protected int[] qubits;
    protected int type;
    protected boolean listen;
    protected GateTable table;

    public QubitChart(GateTable table, int type, int[] qubits) {
	super();
	this.type = type;
	this.table = table;
	listen = false;
	setYAxisRange(0,1);
	setQubits(qubits);
	Parse.addMathlibEventListener(this);
	Mathlib.addComputationEventListener(this);
    }

    public void setType(int type) {
	this.type = type;
	updateDataVector();
	repaint();
    }

    public int getType() {
	return type;
    }

    public void setListen(boolean listen) {
	this.listen = listen;
    }

    public void setQubits(int[] qubits) {
	this.qubits = qubits;

	// update data vector
	updateDataVector();
    }

    public void refresh() {
	updateDataVector();
	repaint();
    }

    public double getPlotValue(int index) {
	if (getValuesPerBin() > 1) {
	    double yValue = 0;

	    if (type == FIDELITY_CHART) {
		int i = 0;
		// bin the values
		for (int j = 0; j < getValuesPerBin(); j++) {
		    if (index*getValuesPerBin()+j < yValues.size()) {
			i++;
			yValue += ((Double)yValues.elementAt(index*getValuesPerBin()+j)).doubleValue();
		    }
		}
		return yValue/i;
	    }
	    else {
		// bin the values
		for (int j = 0; j < getValuesPerBin(); j++) {
		    if (index*getValuesPerBin()+j < yValues.size())
			yValue += ((Double)yValues.elementAt(index*getValuesPerBin()+j)).doubleValue();
		}
		return yValue;
	    }

	}
	else return ((Double)yValues.elementAt(index)).doubleValue();
    }

    private void updateDataVector() {
	double prob;

	yValues.removeAllElements();

	// fetch qubits
	Braket q = (Braket)Mathlib.getVar(GatePanel.qubits);

	if (q != null) {
	    if (qubits.length <= q.n) { 
		if (type == PROBABILITY_CHART) {
		    setPlotStyle(BAR_STYLE);
		    yValues = Measurement.getProbDistribution(q, qubits);
		    yAxisRange.setRange(0,1);
		}
		else if (type == REAMPLITUDE_CHART) {
		    setPlotStyle(BAR_STYLE);
		    Enumeration e = Measurement.getPhaseDistribution(q, qubits).elements();
		    while (e.hasMoreElements()) {
			yValues.addElement(new Double(((Complex)e.nextElement()).re()));
		    }
		    yAxisRange.setRange(-1,1);
		}
		else if (type == IMAMPLITUDE_CHART) {
		    setPlotStyle(BAR_STYLE);
		    Enumeration e = Measurement.getPhaseDistribution(q, qubits).elements();
		    while (e.hasMoreElements()) {
			yValues.addElement(new Double(((Complex)e.nextElement()).im()));
		    }
		    yAxisRange.setRange(-1,1);
		}
		else if (type == FIDELITY_CHART) {
		    setPlotStyle(POINT_STYLE);
		    Enumeration e = table.getFidelityVector().elements();
		    while (e.hasMoreElements()) {
			yValues.addElement(e.nextElement());
		    }
		    xAxisRange.setRange(0, yValues.size()-1);
		    yAxisRange.setRange(0, 1);
		}
		//		xAxisRange.setRange(0, BinaryOp.pow(2, qubits.length)-1);
	    }
	}

    }

    public String getAxisTick(int pos) {
	if (getYDataCount() < 16 && type != FIDELITY_CHART) return Braket.getBasisString(xAxisRange.getLowerBound()+pos, qubits.length, false);
	else return ""+(xAxisRange.getLowerBound()+pos);

    }

    public void addVariable(MathlibEvent e) {
	if (e.getObjectName().equals("qubits") && isVisible() && listen) {
	    updateDataVector();
	    repaint();
	}
    }

    public void changeVariable(MathlibEvent e) {
	if (e.getObjectName().equals("qubits") && isVisible() && listen) {
	    updateDataVector();
	    repaint();
	}
    }

    public void removeVariable(MathlibEvent e) {}


    public void computationEvent(ComputationEvent e) {
	if (e.getAction()== ComputationEvent.DONE && isVisible() && listen) {
	    updateDataVector();
	    repaint();
	}
    }

}
