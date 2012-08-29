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
 * Class representing the complex exp() function. It can operate on complex numbers and
 * on quadratic matrices.
 * @see MathMap
 * @see Parse
 */
public class ExpMap extends MathMap {
    private String data;

    /**
     * creates the exp function
     */
    public ExpMap() {
	data = new String("exp");
    }

    /**
     * applies the exp function to either a complex number or a quadratic matrix. The
     * exp for quadratic matrices is calculated in a power series which terminates if
     * the norm of the k-th term is smaller eps.
     * @see Complex
     * @see Norm
     */
    public MathObject apply(MathObject o) {
	if (o instanceof Complex) {
	    return new Complex(Math.cos(((Complex)o).im()),Math.sin(((Complex)o).im())).times(Math.exp(((Complex)o).re()));
	}
	else if (o instanceof Matrix){
	    if (((Matrix)o).n() == ((Matrix)o).m()) {
		// one matrix
		Matrix expTerm = new Matrix(((Matrix)o).n());

		Matrix result = new Matrix(((Matrix)o).n(), ((Matrix)o).m());
		Norm norm = new Norm();
		BinaryOp op = new BinaryOp('*');
		BinaryOp div = new BinaryOp('/');
		BinaryOp plus = new BinaryOp('+');
		Complex fac = new Complex(1);
		long n = 0; 
		String cont = null;

		while (Math.abs(((Complex)norm.apply(expTerm)).magnitude()) > Complex.eps && n < 1000){
		    result = (Matrix) plus.apply(result, expTerm);
		    n++;
		    expTerm = (Matrix)div.apply(expTerm, new Complex(n));
		    expTerm = (Matrix)op.apply(expTerm, o);
		}
		System.out.println("steps: "+ n);
		if (n < 1000) return result;
		else {
		    LOG.LOG(0, "argument did not converge");
		    return null;
		}
	    }
	}
	LOG.LOG(0, "exp not defined for argument");
	return null;
    }

    /**
     * returns string representation of the exp map
     */
    public String toString() {
	return data;
    }

}
