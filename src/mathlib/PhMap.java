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
 * class representing a 2x2 phase matrix. The argument given to this MathMap specifies the
 * phase angle as follows: [exp(i*delta) 0, 0 exp(i*delta)]
 * @see RxMap
 * @see MathMap
 */
public class PhMap extends MathMap {
    private String data;

    /**
     * constructs the one-map
     */
    public PhMap() {
	data = new String("Ph");
    }

    /**
     * the parameter given is the phase angle delta. Only the real part
     * of the number is used.
     * @param o complex number c = b + i*0
     * @return rotation matrix
     */ 
    public MathObject apply(MathObject o) {
	if (o instanceof Complex) {
	    double delta = ((Complex)o).re();
	    String strPh= new String("[exp("+delta+"*i) 0, 0 exp("+delta+"*i)]");
	    Matrix Ph = Matrix.parseMatrix(strPh);
	    return Ph;
	}
	LOG.LOG(0, toString() + " not defined for argument");
	return null;
    }

    /**
     * returns the string representation of the map
     */
    public String toString() {
	return data;
    }

}


