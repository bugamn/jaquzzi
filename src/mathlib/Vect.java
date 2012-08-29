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
 * Class representing a mathematical vector. The string representation of the default
 * vector is given by <tt>[a b c]</tt>. The default vector therefore is a dual vector.
 * A normal vector is created by parsing <tt>[a b c]'</tt> or calling the constructor
 * followed by a transpose() operation.
 * @see Braket
 */
public class Vect extends Argument {
    /**
     * holds the components. Should not be accessed directly, but via the 
     * <tt>getElement()</tt> method.
     * @see Vect#getElement(int)
     */
    protected Complex[] data;

    /**
     * holds the information whether or not the vector is transposed.
     */
    protected boolean transpose = false;

    /**
     * holds the dimension
     */
    public int dimension;

    /**
     * generates a vector of dimension 0.
     */
    public Vect() {
	dimension = 0;
    }

    /**
     * generates a 0-vector of dimension <tt> dimension </tt>
     * @param dimension the dimension of the vector
     */
    public Vect(int dimension) {
	data = new Complex[dimension];
	this.dimension = dimension;
    }

    /**
     * clone constructor. If the object given as a parameter is another <tt>vect</tt>
     * object, a new vector with the same properties is generated.
     */
    public Vect(MathObject o) {
	if (o instanceof Vect) {
	    Complex zero = new Complex(0,0);
	    Vect v = (Vect) o;
	    data = new Complex[v.dimension];
	    this.dimension = v.dimension;
	    this.transpose = v.transpose;
	    for (int i = 0; i < dimension; i++) {
		if (v.data[i] == null || zero.equals(v.data[i]))
		    data[i] = null;
		else 
		    data[i] = new Complex(v.data[i]);
	    }
	}
    }

    /**
     * clones the vector.
     * @see Vect#Vect(MathObject)
     */
    public Object clone() {
	return new Vect(this);
    }

    /**
     * parses the <tt>vect</tt> object from a string. Throws NumberFormatException or
     * IllegalArgumentException if parsing failed.
     * @param str string of the form <tt>[a b c]</tt>
     * @return a Vect object corresponding to the vector described by the string
     */
    public static Vect parseVector(String str) 
	throws NumberFormatException, IllegalArgumentException {

	Vect v = null;
	int pos1 = 0, pos2 = 0; 
	int n = 0;
	int dimension = 0;
	MathObject component = null;

	try {
	    if ((str.charAt(0) == '[') && (str.charAt(str.length()-1) == ']')) 
		str = str.substring(1, str.length()-1);
	    else 
		throw new IllegalArgumentException("proper brakets expected: "+ str);
	}
	catch (Exception e) {
	    throw new IllegalArgumentException("proper brakets expected: "+ str);
	}

	str = str.trim();
	str = str.concat(" ");

	for (int i = 0; i < str.length(); i++) {
	    if (str.charAt(i) == ' ') dimension++;
	}
	
	v = new Vect(dimension);

	for (int i = 0; i < str.length(); i++) {
	    if (str.charAt(i) == ' ') {
		pos2 = i;
		try {
		    v.data[n] = Complex.parseComplex(str.substring(pos1, pos2));
		}
		// well, now we have to parse!
		catch (NumberFormatException e) {
		    try {
			component = Parse.parseExpression(str.substring(pos1,pos2));
		    } catch (IllegalArgumentException iae) {
			throw new IllegalArgumentException("invalid object in vector definition: " + str.substring(pos1, pos2));
		    }
		    if (component == null) throw new IllegalArgumentException("invalid object in vector definition: " + str.substring(pos1, pos2));
		    // here is everything ok!
		    else if (component instanceof Complex) v.data[n]= (Complex)component;
		    // here, it's not a complex number :-(
		    else throw new NumberFormatException("complex number in vector definition expected: " + str.substring(pos1, pos2));

		}
		pos1 = pos2;
		n++;
	    }
	}
	return v;
    }

    /**
     * transposes the vector
     */
    public void transpose() {
	transpose = transpose ^ true;
    }

    /**
     * conjugates the vector
     */
    public void conjugate() {
	for (int i = 0; i < dimension; i++) {
	    if (data[i] != null) data[i] = data[i].conjugate();
	}
    }

    /**
     * negates the vector
     */
    public void negative() {
	for (int i = 0; i < dimension; i++) {
	    if (data[i] != null) data[i] = data[i].negative();
	}
    }

    /**
     * equality predicate
     * @return true if the MathObject o is equal to this vector
     */
    public boolean equals(MathObject o) {
	boolean equal = true;

	if (!(o instanceof Vect)) return false;

	if (dimension != ((Vect)o).dimension) equal = false;
	else {
	    for (int i = 0; i < dimension; i ++) {
		if (getElement(i).equals(((Vect)o).getElement(i))) equal = false;
	    }
	    if (transpose != ((Vect)o).transpose) equal = false;
	}
	return equal;
    }

    /**
     * returns the component of the vector specified by <tt>x</tt>.
     * @param x number of component starting with 0
     * @return complex component
     */
    public Complex getElement(int x) {
	if (x >= 0 && x < dimension) {
	    Complex val = data[x];
	    return (val == null) ? new Complex(0,0): val;
	}
	else return null;
    }

    /**
     * @return string representation of the vector object
     */
    public String toString() {
	String str = new String("");
	str = (transpose) ? "\n" : "";
	str = str.concat("[");
	for (int i = 0; i < dimension; i++) {
	    str = str.concat(getElement(i).toString());
	    if (i < dimension-1) str = (transpose) ? str.concat("\n") : str.concat("\t");
	}
	str = str.concat("]\n");
	return str;
    }

    /**
     * @return parsable string representation of ths vector object.
     */
    public String toParseableString() {
	String str = new String("");
	str = str.concat("[");
	for (int i = 0; i < dimension; i++) {
	    str = str.concat(getElement(i).toParseableString());
	    if (i < dimension-1) str = str.concat(" ");
	}
	str = str.concat("]");
	return str;
    }
}

