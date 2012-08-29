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
 * function representing z-rotation operator in 2 dimensions. The matrix is
 * of the form Rz(a) = [exp(-i*a/2) 0, 0 exp(i*a/2)]
 */
public class RzMap extends MathMap {
    private String data;

    /**
     * constructs the one-map
     */
    public RzMap() {
	data = new String("Rz");
    }

    /**
     * the parameter given is the angle about which shall be rotated. Only the real part
     * of the number is used.
     * @param o complex number c = a + i*0
     * @return rotation matrix
     */ 
    public MathObject apply(MathObject o) {
	if (o instanceof Complex) {
	    String strRz= new String("[exp(-i*("+((Complex)o).re()+")/2) 0, 0 exp(i*("+((Complex)o).re()+")/2)]");
	    Matrix Rz = Matrix.parseMatrix(strRz);
	    return Rz;
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
