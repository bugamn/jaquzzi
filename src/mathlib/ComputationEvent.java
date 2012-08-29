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
 * Class representing a computation event. This event type is generated when
 * the implicitApply method of the BinaryOp class is executed.
 * @see ComputationEventListener
 * @see Mathlib
 */
public class ComputationEvent {
    /** computation started */
    public static int STARTED = 1;
    /** computation in progress */
    public static int IN_PROGRESS = 2;
    /** computation done */
    public static int DONE = 3;

    private MathObject variable;
    private MathObject operator;
    private int action;
    private int currentStep;
    private int maxStep;

    /**
     * creates a ComputationEvent
     * @param operator originating operator
     * @param variable affected variable
     * @param currentStep current step in computation
     * @param maxStep maximum steps in computation
     * @param action action code
     */
    public ComputationEvent(MathObject operator, MathObject variable, int currentStep, int maxStep, int action) {
	this.operator = operator;
	this.variable = variable;
	this.action = action;
	this.currentStep = currentStep;
	this.maxStep = maxStep;
    }

    /**
     * returns action code
     */
    public int getAction() {
	return action;
    }

    /**
     * returns affected variable
     */
    public MathObject getVariable() { 
	return variable;
    }

    /**
     * returns originating operator
     */
    public MathObject getOperator() {
	return operator;
    }

    /**
     * returns max steps in computation
     */
    public int getMaxStep() {
	return maxStep;
    }

    /**
     * returns current step in computation
     */
    public int getCurrentStep() {
	return currentStep;
    }
}
