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
 * Class representing the real sin() function. It can operate on complex numbers, but only takes
 * the real part.
 * @see MathMap
 * @see Parse
 */
public class SinMap extends MathMap {
    private String data;

    /**
     * creates the exp function
     */
    public SinMap() {
	data = new String("sin");
    }

    /**
     * applies the real sin function to the real part of the complex number given as an argument
     * @see Complex
     * @see Norm
     */
    public MathObject apply(MathObject o) {
	if (o instanceof Complex) {
	    return new Complex(Math.sin(((Complex)o).re()));
	}
	LOG.LOG(0, toString()+"() not defined for argument");
	return null;
    }

    /**
     * returns string representation of the sin map
     */
    public String toString() {
	return data;
    }

}
