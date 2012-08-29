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
 * Class representing conditional operators like "==", "!=","&&", "||"
 */
public class ConditionalOp extends Operator {
    private String data;

    /**
     * creates a conditional operator specified by data. Allowed 
     * parameters are "==", "!=", "&&", "||"
     */
    public ConditionalOp(String data) {
	this.data = data;
    }

    /**
     * applies the contitional operator between the two arguments given by a1, a2
     * @param a1 left hand side argument
     * @param a2 right hand side argument
     * @return boolean as complex number (re() = 0 for false)
     */
    public MathObject apply(Argument a1, Argument a2) {
	if (data.equals("==")) return new Complex((a1.equals(a2) ? 1 : 0));
	else if (data.equals("!=")) return new Complex((a1.equals(a2) ? 0: 1));
	else if (data.equals("||")) {
	    if (a1 instanceof Complex && a2 instanceof Complex) 
		return new Complex( ((((Complex)a1).re() != 0 || ((Complex)a1).im() != 0 || ((Complex)a2).re() != 0 || ((Complex)a2).im() != 0) ? 1 : 0));
	}
	else if (data.equals("&&")) {
	    if (a1 instanceof Complex && a2 instanceof Complex) 
		return new Complex( ((
(((Complex)a1).re() != 0 || ((Complex)a1).im() != 0) && (((Complex)a2).re() != 0 || ((Complex)a2).im() != 0)) ? 1 : 0));

	}
	return null;
    }

    /**
     * string representation of the conditional operator
     */
    public String toString() {
	return data;
    }

}
