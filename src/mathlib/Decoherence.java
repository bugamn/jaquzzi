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
 * class representing the effect of decoherence. The decoherence model implemented is
 * assuming that at every time a decoherence effect is about to occur a random qubit
 * interacts with an environmental qubit through the following process:
 * <p> |0>|0> --> |0>|0>   and |1>|0> --> sqrt(p)|1>|0> + sqrt(1-p)|0>|1> </p>
 * where p is the probability that the qubit stays in the state |1> and 1-p the
 * probability that it decays to |0>. After a decay the state is renormalized.
 * The underlying assumption is that the state |1> is higher energetic than |0> and 
 * therefore a decay is possible.
 */
public class Decoherence extends MathMap {
    static int decQubit = -1;
    static boolean decayed;

    public static int presetQubit=-1;

    /**
     * create a measurement map for a full measurement
     */
    public Decoherence() {
	decayed = false;
    }

    public MathObject apply(double rate, double decay, MathObject o) {
	if (o instanceof Braket){
	    int qubit;
	    if (presetQubit == -1) 
		qubit = Mathlib.getRandom().nextInt(((Braket)o).n);
	    else
		qubit = presetQubit;

	    // decohere!!
	    if (Mathlib.getRandom().nextDouble() <= rate) {
		decayed = decohere((Braket)o, 1-decay, qubit);
		if (decayed) {
		    LOG.LOG(0,"qubit "+qubit+" decayed.");
		}
		else {
		    LOG.LOG(0,"qubit " + qubit + " remained.");
		}
		return o;
	    }
	    else {
		decayed = false;
		decQubit = -1;
	    }
	}
	return o;
    }

    /**
     * applies a decoherence step to a ket vector. In this method a random number is
     * generated and compared to the error rate per gate (this value right now is 
     * obtained from the circuit_properties variable). If the random number is smaller
     * the method <tt>decohere</tt> is called with a random qubit and the decay
     * probability specified by the ciruit_properties variable.
     * @param o Braket
     */
    public MathObject apply(MathObject o) {
	if (o instanceof Braket){
	    MathObject cp = Mathlib.getVar("circuit_properties");
	    if (cp != null) {
		Argument rate = ((GateProperty)cp).getProperty("rate");
		if (rate != null) {
		    // check for decay probability
		    Argument decayProb = ((GateProperty)cp).getProperty("decay");
		    double decayVal;
		    if (decayProb != null) 
			decayVal = ((Complex)decayProb).re();
		    else 
			decayVal = 0.5;

		    double rateVal = ((Complex)rate).re();
		    int qubit = Mathlib.getRandom().nextInt(((Braket)o).n);
		    // decohere!!
		    if (Mathlib.getRandom().nextDouble() <= rateVal) {
			boolean yes = decohere((Braket)o, 1-decayVal, qubit);
			System.out.println("decoherence step on qubit " + qubit+ " with decay probability p = " + decayVal);
			if (yes) 
			    System.out.println("qubit decayed.");
			else
			    System.out.println("qubit remained unchanged.");
			return o;
		    }
		    else {
			decayed = false;
			decQubit = -1;
		    }
		}
	    }
	}
	return o;
    }

    /**
     * this method decoheres a given qubit in the ket given as well. With probablity
     * p the qubit remains in state |1>. The state is renormailzed afterwards.
     * 
     * @param q ket vector
     * @param p probability for the state |1> remaining in |1>
     * @param qubit the qubit to decohere
     * @see BinaryOp#implicitApply(Gate, Braket, Matrix)
     * @return true if a decay occured
     */
    public static boolean decohere(Braket q, double p, int qubit) {
	if (Mathlib.getRandom().nextDouble() > p) {
	    int dim = 0, n = 0;
	    int offset, step;
	    double norm = 0;
	    boolean zeroCheck = true;

	    n = q.n;
	    dim = q.dimension;

	    offset = BinaryOp.pow(2, n-qubit-1);

	    // counter
	    int[] jokers = new int[n]; int j = 0;
	    for (int i = 0; i < n; i++) if (i!= qubit) jokers[j++] = n-i-1;

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

		if (q.data[step+offset] != null) {
		    zeroCheck = false;		
		    
		    q.data[step] = q.data[step+offset];
		    q.data[step+offset] = null;
		    norm += q.data[step].magnitudeSquared();
		}
	    }
	    // was already in |0>
	    if (zeroCheck) return false;

	    norm = Math.sqrt(1/norm);	
	    if (norm == 1) {
		decQubit = qubit;
		decayed = true;
		return true;
	    }

	    // renormalize
	    for (int i = 0; i < dim; i++) {
		if (q.data[i] != null) q.data[i] = q.data[i].times(norm);
	    }
	    decQubit = qubit;
	    decayed = true;
	    return true;
	}
	else {
	    decQubit = -1;
	    decayed = false;
	    return false;
	}
    }

    /**
     * returns the last decohered qubit. -1 if nothing happend during last decoherence
     * step
     */
    public static int getLastDecoheredQubit() {
	return decQubit;
    }

    /**
     * returns true if at the last operation the qubit decayed
     */
    public static boolean decayOccurred() {
	return decayed;
    }


    /**
     * string representation of the decoherence map
     */
    public String toString() {
	return "decoherence";
    }

}
