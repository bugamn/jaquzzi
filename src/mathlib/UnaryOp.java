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
 * Class representing an unary operator. Implemented are +,-,'(transpose), " (complex
 * conjugate).
 * @see Tokenizer
 * @see BinaryOp
 * @see Parse
 */
public class UnaryOp extends Operator {
    private char data;

    /**
     * creates a unary operator of the given type
     */
    public UnaryOp(char c) {
	data = c;
    }

    /**
     * dispatch method. Since at compile time the bindings are not known, the Parse class
     * needs to have this generic method.
     * @see BinaryOp#apply(MathObject, MathObject)
     */
    public MathObject apply(MathObject o1) {
	MathObject result = null;
	if (o1 instanceof Vect) result = apply((Vect) o1);
	else if (o1 instanceof Matrix) result = apply((Matrix) o1);
	else if (o1 instanceof Complex) result = apply((Complex) o1);
	else if (o1 instanceof Gate) result = apply((Gate) o1);
	return result;
    }

    /**
     * applies the unary operator to a complex number
     */
    public MathObject apply(Complex c1) {
	switch (data) {
	case '-' : { return c1.negative();}
	case '+' : { return c1; }
	case '\'': { return c1; }
	case '"' : { return c1.conjugate();}
	}
	return null;
    }

    /**
     * applies the unary operator to a vect object
     */
    public MathObject apply(Vect v1) {
	Vect result;
	if (v1 instanceof Braket) result = new Braket((Braket)v1);
	else result = new Vect(v1);

	switch (data) {
	case '-' : { result.negative(); break; }
	case '\'': { result.transpose(); break; }
	case '"' : { result.conjugate(); break; }
	}
	return result;
    }

    /**
     * applies the unary operator to a matrix object
     */
    public MathObject apply(Matrix m1) {
	Matrix result = new Matrix(m1);
	switch (data) {
	case '-' : { result.negative(); break; }
	case '\'': { result.transpose(); break; }
	case '"' : { result.conjugate(); break;   }
	}
	return result;
    }

    /**
     * applies the unary operator to a gate object
     */
    public MathObject apply(Gate g1) {
	Gate result = new Gate(g1);
	switch (data) {
	case '-' : { result.negative(); break; }
	case '\'': { result.transpose(); break; }
	case '"' : { result.conjugate(); break;   }
	}
	return result;
    }

    /**
     * returns whether the unary operator this object represents is a leading
     * unary operator, i.e. +,-
     */
    public boolean leadingUnaryOp() {
	if ((data == '+') || (data == '-')) return true;
	else return false;
    }

    /**
     * returns string representation of the operator
     */
    public String toString() {
	return new Character(data).toString();
    }
}
