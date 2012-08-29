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
 * Class representing a norm map. For vectors it is the L_2 norm, for matrices it is
 * the maximums norm.
 * @see ExpMap
 */
public class Norm extends MathMap {
    private String data = "";

    /**
     * creates a norm map
     */
    public Norm() {
	data = new String("norm");
    }

    /**
     * calculates the norm of a MathObeject. Valid arguments are: vectors, brakets,
     * matrices.
     * @return real number in complex form
     */
    public MathObject apply(MathObject o) {
	Complex norm;
	BinaryOp op = new BinaryOp('*');

	if (o instanceof Vect) {
	    Vect o_dagger = new Vect((Vect)o);
	    o_dagger.transpose(); o_dagger.conjugate();
	    norm= new Complex(Math.sqrt((((Complex)op.apply(o, o_dagger)).magnitude())));
	    return norm;
	}
	else if (o instanceof Braket) {
	    Braket o_dagger = new Braket((Braket)o);
	    o_dagger.transpose(); o_dagger.conjugate();
	    norm= new Complex(Math.sqrt((((Complex)op.apply(o, o_dagger)).magnitude())));
	    return norm;
	}
	else if (o instanceof Matrix) {
	    double max = 0, val = 0;
	    for (int i = 0; i < ((Matrix)o).n(); i++) {
		for (int j = 0; j < ((Matrix)o).m(); j++) {
		    val = Math.abs(((Matrix)o).getElement(i,j).magnitude());
		    if (val > max) max = val;
		}
	    }
	    return new Complex(max);
	}

	LOG.LOG(0, "norm() not defined for specified argument");
	return null;
    }

    /**
     * returns string representation
     */
    public String toString() {
	return data;
    }

}
