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

/**
 * interface implemented by objects which are operated in a sequential manner. It is 
 * implemented by the GateContainer class to maintain the handling of gates in the
 * presence of grouped GateContainers.
 * @see StepHandler
 * @see GateContainer
 * @see GateTable
 */
public interface Steppable {

    /** needs to return the total number of steps */
    public int getStepCount();
    /** needs to return the current step number */
    public int getStep();
    /** needs to increase the step by one and return false if the step cannot be handled
     * by this object. 
     */
    public boolean stepForward();
    /** needs to decrease the step by one and return false if the step cannot be handled
     * by this object. 
     */
    public boolean stepBackward();
    /** resets the step handling to an initial state */
    public void reset();

}
