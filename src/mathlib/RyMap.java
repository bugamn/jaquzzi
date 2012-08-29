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
 * function representing y-rotation operator in 2 dimensions. The matrix is
 * of the form Ry(b) = [cos(b/2) sin(b/2), -sin(b/2) cos(b/2)]
 */
public class RyMap extends MathMap {
    private String data;

    /**
     * constructs the one-map
     */
    public RyMap() {
	data = new String("Ry");
    }

    /**
     * the parameter given is the angle about which shall be rotated. Only the real part
     * of the number is used.
     * @param o complex number c = b + i*0
     * @return rotation matrix
     */ 
    public MathObject apply(MathObject o) {
	if (o instanceof Complex) {
	    double b = ((Complex)o).re();
	    String strRy= new String("[cos("+b+"/2) sin("+b+"/2), -sin("+b+ "/2) cos("+b+"/2)]");
	    Matrix Ry = Matrix.parseMatrix(strRy);
	    return Ry;
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
