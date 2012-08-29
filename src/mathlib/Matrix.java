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
 * Class representing a mathematical matrix object.
 * @see BinaryOp
 */
public class Matrix extends Argument {
    /**
     * holds the complex components
     */
    protected Complex[][] data;

    /**
     *  holds dimension information
     */
    protected int n, m;

    /**
     * holds transpose information
     */
    protected boolean transpose = false;

    /**
     * holds conjugate information
     */
    protected boolean conjugate = false;

    /**
     * constructs a zero (n x m) matrix (n rows, m columns)
     */
    public Matrix(int n, int m) {
	this.n = n;
	this.m = m;
	data = new Complex[n][m];
	for (int i = 0; i < n; i++) {
	    for (int j = 0; j < m; j++) {
		data[i][j] = new Complex(0);
	    }
	}
    }

    /**
     * constructs a zero (n x n) matrix
     */
    public Matrix(int n) {
	this(n, n);
	for (int i = 0; i < n; i++) {
	    data[i][i] = new Complex(1);
	}
    }

    /**
     * clone constructor
     */
    public Matrix(MathObject o) {
	if (o instanceof Matrix) {
	    Matrix m1 = (Matrix)o;
	    this.n = m1.n();
	    this.m = m1.m();
	    data = new Complex[n][m];
	    for (int i = 0; i < n; i++) {
		for (int j = 0; j < m; j++) {
		    data[i][j] = new Complex(m1.getElement(i,j));
		}
	    }
	}
    }

    /**
     * clone method
     */
    public Object clone() {
	return new Matrix(this);
    }

    /**
     * <p> parses a matrix from a string. The matrix given by [a b c, d e f, g h i] 
     * is parsed into </p>
     * <tt><p> a  b  c </p>
     * <p> d e f </p>
     * <p> g h i </p>
     * The method throws an IllegalArgumentException of a NumberFormatException if
     * the parse process fails.
     * @return matrix
     */
    public static Matrix parseMatrix(String str) 
	throws IllegalArgumentException, NumberFormatException {
	Matrix matrix = null;

	int pos1 = 0, pos2 = 0; 
	int k = 0;
	int n = 0, m = 0;

	try {
	    // check & remove brackets
	    if ((str.charAt(0) == '[') && (str.charAt(str.length()-1) == ']')) 
		str = str.substring(1, str.length()-1);
	    else 
		throw new IllegalArgumentException("proper brakets expected: "+ str);
	}
	catch (Exception e) {
	    throw new IllegalArgumentException("proper brakets expected: "+ str);
	}

	str = str.trim();
	str = str.concat(",");

	for (int i = 0; i < str.length(); i++) {
	    if (str.charAt(i) == ',') n++;
	}
	matrix = new Matrix(n,1);

	for (int i = 0; i < str.length(); i++) {
	    if (str.charAt(i) == ',') {
		pos2 = i;

		// exceptions thrown by parseVector!
		Vect temp = Vect.parseVector("["+str.substring(pos1, pos2)+"]");

		m = temp.dimension;
		matrix.data[k] = new Complex[m];
		if (k == 0) matrix.m = m;
		else {
		    if (m != matrix.m) throw new IllegalArgumentException("matrix rows have different dimensions!");
		}

		for (int j = 0; j < matrix.m; j++) {
		    matrix.data[k][j] = new Complex(temp.data[j]);
		}
		pos1 = pos2+1;
		k++;
	    }
	}
	return matrix;
    }

    /**
     * transposes the matrix
     */
    public void transpose() {
	transpose = transpose ^ true;
    }

    /**
     * complex conjugates the matrix
     */
    public void conjugate() {
	conjugate = conjugate ^ true;
    }

    /**
     * negates the matrix
     */ 
    public void negative() {
	for (int i = 0; i < n(); i++) {
	    for (int j = 0; j < m(); j++) {
		setElement(i,j, getElement(i,j).negative());
	    }
	}
    }

    /**
     * equality predicate
     */ 
    public boolean equals(MathObject m1) {
	boolean veto = false;
	if (m1 instanceof Matrix) {
	    Matrix m2 = (Matrix)m1;
	    veto = (m() != m2.m()) || (n() != m2.n());
	    veto = veto | (m2.transpose != transpose);

	    if (veto) return false;

	    for (int i = 0; i < n(); i++) {
		for (int j = 0; j < m(); j++) {
		    if (getElement(i, j).equals(m2.getElement(i, j)) == false) 
			return false;
		}
	    }
	    return true;
	}
	else return false;
    }

    /**
     * returns the element in row n, col m. This method should always be used to 
     * access components, since for example the transpose method not really transposes
     * the matrix, but rater sets a flag.
     * @param n row
     * @param m col
     * @return component
     */ 
    public Complex getElement(int n, int m) {
	if (conjugate) {
	    if (transpose) return data[m][n].conjugate();
	    else return data[n][m].conjugate();
	}
	else {
	    if (transpose) return data[m][n];
	    else return data[n][m];
	}
    }

    /**
     * sets the element in row n, col m to a certain value. This method should 
     * always be used to 
     * access components, since for example the transpose method not really transposes
     * the matrix, but rater sets a flag.
     * @param n row
     * @param m col
     * @param value new component value
     */ 
    public void setElement(int n, int m, Complex value) {
	if (conjugate) {
	    if (transpose) data[m][n].set(value.re(), -value.im());
	    else data[n][m].set(value.re(), -value.im());
	}
	else {
	    if (transpose) data[m][n].set(value.re(), value.im());
	    else data[n][m].set(value.re(), value.im());
	}
    }

    /**
     * returns the number of rows.
     */
    public int n() {
	return (transpose)? m : n;
    }

    /**
     * returns the number of columns
     */ 
    public int m() {
	return (transpose)? n : m;
    }

    /**
     * returns a string representation of the object
     */
    public String toString() {
	String str = new String("\n");

	for (int i = 0; i < n(); i++) {
	    for (int j = 0; j < m(); j++) {
		str = str.concat(getElement(i,j).toString() + "\t");
	    }
	    str = str.concat("\n");
	}
	return str;
    }

    /**
     * returns a parsable string representation of the matrix (complex numbers in
     * machine precision).
     */
    public String toParseableString() {
	String str = new String("[");
	for (int i = 0; i < n(); i++) {
	    for (int j = 0; j < m(); j++) {
		str = str.concat(getElement(i,j).toParseableString() + " ");
	    }
	    if (i < n()-1) str = str.concat(", ");
	}
	str = str.concat("]");
	return str;
    }

}
