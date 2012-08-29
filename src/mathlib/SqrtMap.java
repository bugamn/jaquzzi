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
 * sqrt map for complex arguments
 */
public class SqrtMap extends MathMap {
    private String data;

    /**
     * constructs the sqrt map
     */
    public SqrtMap() {
	data = new String("sqrt");
    }

    /**
     * applies the sqrt map to a complex number
     * @param o complex number
     * @return sqrt in complex form
     */
    public MathObject apply(MathObject o) {
	if (o instanceof Complex) {
	    double re = ((Complex)o).re();
	    double im = ((Complex)o).im();

	    if (im == 0) {
		if (re >= 0) return new Complex(Math.sqrt(re));
		else return new Complex(0, Math.sqrt(-re));
	    }
	}

	LOG.LOG(0, data + "() not defined for argument");
	return null;
    }

    /**
     * string representation of map
     */
    public String toString() {
	return data;
    }

}
