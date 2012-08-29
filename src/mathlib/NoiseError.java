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

import java.util.*;


/**
 * class representing a map that generates an infinitesimal 2x2 complex rotation matrix
 * in dependence of the standard deviation of a Gaussian distribution given as a parameter.
 * This class is used to model the effect of noise during gate applications.
 * @see Gate
 * @see Decoherence
 */
public class NoiseError extends MathMap {

    /**
     * creates a map that generates an infinitesimal 2x2 rotation matrix.
     */
    public NoiseError() {
    }

    /**
     * returns an infinitesimal 2x2 rotation matrix where the three parameters eps_1, 
     * eps_2, eps_3 are drawn from a Gaussian distribution of standard deviation sigma
     * as specified in the real part of the complex number. The return value is the 2x2
     * matrix.
     */
    public MathObject apply(MathObject o) {
	if (o instanceof Complex) {
	    double sigma = ((Complex)o).re();

	    double eps1_2 = sigma * Mathlib.getRandom().nextGaussian()/2.0;
	    double eps2_2 = sigma * Mathlib.getRandom().nextGaussian()/2.0;
	    double eps3_2 = sigma * Mathlib.getRandom().nextGaussian()/2.0;
	    double eps4 = sigma * Mathlib.getRandom().nextGaussian();
	    //	    System.out.println(" error: eps1 = "+eps1_2*2 +"\t eps2 = "+eps2_2*2+ "\t eps3 = " + eps3_2*2);

	    double cos = Math.cos(eps2_2);
	    double sin = Math.sin(eps2_2);
	    double cos1plus3 = Math.cos(eps1_2+eps3_2);
	    double cos1minus3 = Math.cos(eps1_2-eps3_2);
	    double sin1plus3 = Math.sin(eps1_2+eps3_2);
	    double sin1minus3 = Math.sin(eps1_2-eps3_2);

	    double phasecos = Math.cos(eps4);
	    double phasesin = Math.sin(eps4);

	    Complex m11 = new Complex(phasecos*cos1plus3*cos,-phasesin*sin1plus3*cos);
	    Complex m12 = new Complex(cos1minus3*sin,-sin1minus3*sin);
	    Complex m21 = new Complex(-phasecos*cos1minus3*sin,-phasesin*sin1minus3*sin);
	    Complex m22 = new Complex(cos1plus3*cos,sin1plus3*cos);

	    Matrix m = new Matrix(2);
	    m.setElement(0,0,m11);
	    m.setElement(0,1,m12);
	    m.setElement(1,0,m21);
	    m.setElement(1,1,m22);
	    return m;
	}
	LOG.LOG(0, "invalid parameter for noiseError()");
	return null;
    }


    /**
     * string representation of the decoherence map
     */
    public String toString() {
	return "noiseError";
    }

}
