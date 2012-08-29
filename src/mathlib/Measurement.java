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

package mathlib;

import java.util.*;


/**
 * class representing the process of measurement. It implements a full measurement
 * as well as a partial measurement of one qubit. The full measurement is implemented 
 * as a MathMap since and the partial measurement can be accessed with a gate of the 
 * notation {-:!:-:-:-:-}, which would measure the second qubit and renormalize the
 * state. This class also provides a method to give the probability distribution
 * of the state vector.
 * @see BinaryOp
 */
public class Measurement extends MathMap {
    double prob;

    /**
     * create a measurement map for a full measurement
     */
    public Measurement() {
	prob = 0;
    }

    /**
     * applying a full measurement to a ket vector
     */
    public MathObject apply(MathObject o) {
	if (o instanceof Braket){
	    Braket qubits = (Braket) o;
	    Random random = new Random();
	    double randDouble = random.nextDouble();

	    double probBin = 0.0;

	    for (int i = 0; i < qubits.dimension; i++) {
		if (qubits.data[i] != null)
		    probBin += qubits.data[i].magnitudeSquared();
		if (probBin > randDouble) {
		    prob = qubits.getElement(i).magnitudeSquared();
		    return new Braket(i, qubits.n);
		}
	    }

	}
	LOG.LOG(0, "measure() not defined for argument");
	return null;
    }

    /**
     * performing a partial measurement to the qubit given. It renormalizes the ket
     * vector afterwards.
     * @see BinaryOp#implicitApply(Gate, Braket, Matrix)
     */
    public static void partialMeasurement(Braket q, int qubit) {
	int n = q.n;
	int step = 0;

	double zeroProb = 0.0;

	// counter
	int[] jokers = new int[n]; int j = 0;
	for (int i = 0; i < n; i++) if (i!= qubit) jokers[j++] = n-i-1;

	// initialize binary counter
	boolean[] binary_counter = new boolean[n-1];

	for (int i = 0; i < BinaryOp.pow(2,n-1); i++) {
	    step = 0;
	    if (n-1 > 0) {
		// calculate stepsize
		for (int l = 0; l < n-1; l++) {
		    step += (binary_counter[l]? BinaryOp.pow(2,jokers[n-1-l-1]) : 0);
		} 

		// increase binary counter
		for(j=0;(j<n-1)&&!(binary_counter[j]=!binary_counter[j++]););
	    }

	    if (q.data[step] != null)
		zeroProb += q.data[step].magnitudeSquared();
	}
	Random random = new Random();
	double randDouble = random.nextDouble();

	int event = (Mathlib.getVar("projector") == null)? MathlibEvent.ADD:MathlibEvent.CHANGE;
	// ok...we measured the qubit state |0>
	if (zeroProb > randDouble) {
	    zeroProb = Math.sqrt(zeroProb);
	    Parse.fireMathlibEvent("projector", Matrix.parseMatrix("[1/"+zeroProb+ " 0, 0 0]"), event);
	}
	else {
	    zeroProb = Math.sqrt(1-zeroProb);
	    Parse.fireMathlibEvent("projector", Matrix.parseMatrix("[0 0, 0 1/"+(zeroProb)+ "]"), event);
	} 
	// okay...modifying state vector!!!
	BinaryOp.implicitApply2x2(new Gate(n, qubit, "projector"), q, null);
    }

    /**
     * returns the probability distribution for the outcome of a particular basis ket
     * when performing a measurement without actually performing a measurement.
     * The array qubits specifies which qubits are considered. 
     */
    public static Vector getProbDistribution(Braket q, int[] qubits) {
	int k = qubits.length;
	int n = q.n;
	Vector probDistrib = new Vector(BinaryOp.pow(2, k));

	// joker digits
	int[] jokers = new int[n-k];
	boolean[] binary_counter1 = new boolean[n-k];
	int step;

	double prob = 0;

	// counter for possible states
	boolean[] binary_counter2 = new boolean[k];
	int offset = 0;

	// fill the joker array with remaining qubit positions
	int pos = 0;
	boolean found = false;
	for (int i = 0; i < n; i++) {
	    found = false;
	    for (int j = 0; j < k; j++)
		if (qubits[j] == i) found = true;
	    if (found == false && n-k > pos) 
		jokers[pos++] = i;
	}


	// for each possible state
	for (int i = 0; i < BinaryOp.pow(2, k); i++) {

	    offset = 0;
	    // calculate stepsize
	    for (int l = 0; l < k; l++) {
		offset += (binary_counter2[l]? BinaryOp.pow(2,n-qubits[k-l-1]-1) : 0);
	    } 

	    // increase binary counter
	    for(int l=0;(l < k)&&!(binary_counter2[l]=!binary_counter2[l++]););

	    prob = 0;

	    // sum probabilities
	    for (int j = 0; j < BinaryOp.pow(2, n-k); j++) {
		step = 0;

		// calculate stepsize
		for (int l = 0; l < n-k; l++) {
		    step += (binary_counter1[l]? BinaryOp.pow(2,n-jokers[n-k-l-1]-1) : 0);
		} 

		// increase binary counter
		for(int l=0;(l < n-k)&&!(binary_counter1[l]=!binary_counter1[l++]););

		if (q.data[offset + step] != null)
		    prob += q.data[offset + step].magnitudeSquared();
	    }

	    probDistrib.add(new Double(prob));

	}
	return probDistrib;
    }

    /**
     * returns the real and imaginary parts of a register of qubits
     */
    public static Vector getPhaseDistribution(Braket q, int[] qubits) {
	int k = qubits.length;
	int n = q.n;
	Vector phaseDistrib = new Vector(BinaryOp.pow(2, k));

	// joker digits
	int[] jokers = new int[n-k];
	boolean[] binary_counter1 = new boolean[n-k];
	int step;

	Complex phase;

	// counter for possible states
	boolean[] binary_counter2 = new boolean[k];
	int offset = 0;

	// fill the joker array with remaining qubit positions
	int pos = 0;
	boolean found = false;
	for (int i = 0; i < n; i++) {
	    found = false;
	    for (int j = 0; j < k; j++)
		if (qubits[j] == i) found = true;
	    if (found == false && n-k > pos) 
		jokers[pos++] = i;
	}


	// for each possible state
	for (int i = 0; i < BinaryOp.pow(2, k); i++) {

	    offset = 0;
	    // calculate stepsize
	    for (int l = 0; l < k; l++) {
		offset += (binary_counter2[l]? BinaryOp.pow(2,n-qubits[k-l-1]-1) : 0);
	    } 

	    // increase binary counter
	    for(int l=0;(l < k)&&!(binary_counter2[l]=!binary_counter2[l++]););

	    phase = new Complex(0);

	    // sum probabilities
	    for (int j = 0; j < BinaryOp.pow(2, n-k); j++) {
		step = 0;

		// calculate stepsize
		for (int l = 0; l < n-k; l++) {
		    step += (binary_counter1[l]? BinaryOp.pow(2,n-jokers[n-k-l-1]-1) : 0);
		} 

		// increase binary counter
		for(int l=0;(l < n-k)&&!(binary_counter1[l]=!binary_counter1[l++]););

		if (q.data[offset + step] != null)
		    phase = phase.plus(q.data[offset+step]);
	    }

	    phaseDistrib.add(phase);

	}
	return phaseDistrib;
    }

    /**
     * saves the probability of the latest measurement outcome
     */
    public double getLastProbability() {
	return prob;
    }

    /**
     * string representation of the measurement map
     */
    public String toString() {
	return "measurement";
    }

}
