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

/**
 * Class providing timing functionality for gate operations.
 */
public class Timing {
    private double elapsedTime;
    private long steps;

    private long startT;
    private long endT;

    private boolean timing;

    /**
     * creates a timing object and initializes it
     */
    public Timing() {
	reset();
    }

    /**
     * reset the internal values
     */
    public void reset() {
	steps = 0;
	elapsedTime = 0;
	startT = 0;
	endT = 0;
	timing = false;
    }

    /**
     * starts the timing process
     */
    public void start() {
	startT = System.currentTimeMillis();
	timing = true;
    }

    /**
     * stops the timing process
     */
    public void stop() {
	endT = System.currentTimeMillis();
	timing = false;
	elapsedTime += (endT-startT);
	startT = 0;
	endT = 0;
    }

    /**
     * sets the number of steps accomplished in the last timing interval
     */
    public void stepsAccomplished(int stepsAcc) {
	steps += stepsAcc;
    }

    /**
     * combines the stop() and stepsAccomplished(int) methods
     */ 
    public void stop(int stepsAcc) {
	stop();
	stepsAccomplished(stepsAcc);
    }

    /**
     * returns whether the object is currently timing
     */
    public boolean isTiming() {
	return timing;
    }

    /**
     * returns the total elapsed time in seconds
     */
    public double getElapsedTimeSec() {
	if (timing) {
	    long tmpT = System.currentTimeMillis();
	    return (elapsedTime+(tmpT-startT))/1000;
	}
	else return elapsedTime/1000;
    }

    /**
     * returns the average time per gate in milliseconds
     */
    public double getAvgTimePerStepMillis() {
	if (timing) {
	    long tmpT = System.currentTimeMillis();
	    return (elapsedTime+(tmpT-startT))/steps;
	}
	return elapsedTime/steps;
    }

    /**
     * returns a time estimate for the given number of steps based on the
     * average time per step.
     */
    public double getTimeEstimateForStepsSec(int steps) {
	return steps*getAvgTimePerStepMillis()/1000;
    }

}
