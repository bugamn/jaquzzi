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
 * <p>Class representing a mathematical bra or ket object as introduced by the Dirac
 * notation. The basis chosen for this object is the canonical z-axis basis.</p>
 * <p>It is derived from the class
 * <tt>Vect</tt> and is the most important object for quantum computations.</p>
 * The components of the braket are not created before they are actually needed in a
 * computation.
 * @see Complex#times_(Complex).
 * @see BinaryOp
 */
public class Braket extends Vect {
    /**
     * holds the number of qubits. The dimension is given by 2^n.
     */
    public int n; // number of bits != dimension

    /**
     * holds whether the braket is a bra or a ket.
     */
    protected boolean bra;

    /**
     * clone constructor
     * @see Vect#Vect(MathObject)
     */
    public Braket(MathObject o) {
	super((Vect)o);
	if (o instanceof Braket) {
	    Braket b = (Braket)o;
	    this.bra = b.bra;
	    this.n = b.n;
	}
    }

    /**
     * constructs an empty ket vector with n qubits and dimension 2^n.
     * @param n number of qubits
     */
    public Braket(int n) {
	super(new Double(Math.pow(2,n)).intValue());
	bra = true;
	this.n = n;
	transpose();
    }

    /**
     * constructs a basis ket.
     * @param state 0<= state < 2^n
     * @param n number of qubits
     */
    public Braket(int state, int n) {
	super(new Double(Math.pow(2,n)).intValue());
	data[state] = new Complex(1);
	bra = true;
	this.n = n;
	transpose();
    }

    /**
     * clone method
     */
    public Object clone() {
	return new Braket(this);
    }

    public boolean isBra() {
	return bra;
    }

    /**
     * creates a braket object from a <tt>Vect</tt> object. The transpose property of
     * the vector determines whether the parsed object becomes a bra (transpose = false)
     * or a ket (transpose = true). Throws an IllegalArgumentException if the operation
     * fails.
     * @param v a vect object
     * @return a braket object
     */
    public static Braket parseBraket(Vect v) throws IllegalArgumentException {
	int n = 0; 
	double dim = 0;
	Braket b = null;
	Complex c;

	while ((dim = Math.pow(2, n)) < v.dimension) n++;
	if (dim == v.dimension) {
	    b = new Braket(n);
	    if (v.transpose == true) b.bra = false;
	    for (int i = 0; i < v.dimension; i++) {
		c = v.data[i];
		if (c == null)
		    b.data[i] = null;
		else
		    b.data[i] = new Complex(c);
	    }
	    return b;
	}
	else throw new IllegalArgumentException("invalid vector dimension");
    }

    /**
     * parses a braket object from a given string. Possible notations are:
     * <itemize>
     * <item> |001001> or <101000| (binary)
     * <item> |+> or <-| (only for one qubit brakets!)
     * </itemize>
     * Throws NumberFormatException or IllegalArgumentException if the operation fails.
     * @param str string to parse
     * @return the braket object
     */
    public static Braket parseBraket(String str) 
	throws NumberFormatException, IllegalArgumentException {

	String old = new String(str);
	Braket b = null;
	boolean bra = true;
	int n, dimension;
	int state = 0;

	try {
	    if ((str.charAt(0) == '<') 
		&& (str.charAt(str.length()-1) == '|')) bra = true;
	    else if ((str.charAt(0) == '|') 
		     && (str.charAt(str.length()-1) == '>')) bra = false;
	    str = str.substring(1, str.length()-1);
	}
	catch (Exception e) {
	    throw new IllegalArgumentException("invalid braket: "+ old);
	}

	// proceed with binary number
	str = str.trim();
	n = str.length();

	dimension = new Double(Math.pow(2,n)).intValue();
	b = new Braket(n);

	try {
	    state = Integer.parseInt(str, 2);
	} catch (Exception e) {
	    if (dimension == 2)  {
		if (str.equals("+")) state = 0;
		else if (str.equals("-")) state = 1;
		else {
		    throw new IllegalArgumentException("+,-,0 or 1 in between " + ((bra == false) ? "|>": "<|") + " expected!");
		}
	    }
	    else {
		throw new NumberFormatException("binary number in between " + ((bra == false) ? "|>": "<|") + " expected!");
	    }
	}
	for (int i = 0; i < dimension; i++) {
	    b.data[i] = (i == state) ? new Complex(1) : null;
	}
	if (bra == true) b.transpose();
	return b;
    }

    /**
     * transposes the braket. bra -> ket", ket -> bra". Where " denotes the complex
     * conjugate. In order to transform a bra to a ket, a transpose() operation followed
     * by a conjugate() operation must be performed.
     * @see Vect#conjugate()
     */
    public void transpose() {
	super.transpose();
	if (bra) bra = false;
	else bra = true; 
    }

    /**
     * equality predicate.
     * @return true if o equals this braket
     */
    public boolean equals(MathObject o) {
	if (o instanceof Braket) {
	    if (((Braket)o).bra == bra 
		&& ((Braket)o).n == n) return super.equals((Vect) o);
	    else return false;
	}
	else return false;
    }

    /**
     * static method to obtain the string representation of a basis braket.
     * @return the string representation of a certain basis state in binary braket 
     * notation.
     * @param basisState the number of the basisState (0 <= basisState < 2^n)
     * @param n the number of qubits (the length of the binary representation)
     * @param bra determines whether the state is given as bra or ket
     * @see Braket#toString()
     */
    public static String getBasisString(int basisState, int n, boolean bra) {
	String str = new String("");
	String binaryStr = Integer.toBinaryString(basisState);
	str = (bra) ? "<" : "|";
	for(int i = 1; i <= n-binaryStr.length(); i++) {
	    str = str.concat("0");
	}	    
	str = str.concat(binaryStr);
	str = (bra) ? str.concat("|") : str.concat(">");
	return str;

    }

    /**
     * This method expands the braket in its basis states. The chosen basis is the
     * canonical z-axis basis.
     * @return the string representation of this braket object
     * @see Braket#toString(int)
     * @see Braket#getBasisString(int,int,boolean)
     */
    public String toString() {
	String str = new String("");
	Complex factor;

	for (int i = 0; i < dimension; i++) {
	    factor = data[i];
	    if (factor != null) {
		if ((factor.im() == 0) && (factor.re() != 0)) {
		    str = str + ((factor.re() > 0) ? " + " : " ");
		    str = str.concat(factor.toString());
		    str = str.concat("*");
		    str = str.concat(getBasisString(i, n, bra));
		}
		else if ((factor.re() == 0) && (factor.im() != 0)) {
		    str = str + ((factor.im() > 0) ? " + " : " ");
		    str = str.concat(factor.toString());
		    str = str.concat("*");
		    str = str.concat(getBasisString(i, n, bra));
		}
		else if ((factor.im() != 0) && (factor.re() != 0)) {
		    str = str.concat(" + (");
		    str = str.concat(factor.toString());
		    str = str.concat(")*");
		    str = str.concat(getBasisString(i, n, bra));
		}
	    }

	}
	if (str.equals("")) str = str.concat("0");

	return str;
    }

    /**
     * This method expands the braket in its basis states. The chosen basis is the
     * canonical z-axis basis. In contrast to the normal toString() method, this method
     * limits its output to the number given as an argument. This is useful since
     * the string representation grows exponentially with the number of qubits!
     * @param length length of the string after which it is truncated
     * @return the string representation of this braket object
     * @see Braket#toString()
     */
    public String toString(int length) {
	String str = new String("");
	Complex factor;

	for (int i = 0; i < dimension; i++) {
	    if (str.length() >= length) return str;
	    factor = data[i];
	    if (factor != null) {
		if ((factor.im() == 0) && (factor.re() != 0)) {
		    str = str + ((factor.re() > 0) ? " + " : " ");
		    str = str.concat(factor.toString());
		    str = str.concat("*");
		    str = str.concat(getBasisString(i, n, bra));
		}
		else if ((factor.re() == 0) && (factor.im() != 0)) {
		    str = str + ((factor.im() > 0) ? " + " : " ");
		    str = str.concat(factor.toString());
		    str = str.concat("*");
		    str = str.concat(getBasisString(i, n, bra));
		}
		else if ((factor.im() != 0) && (factor.re() != 0)) {
		    str = str.concat(" + (");
		    str = str.concat(factor.toString());
		    str = str.concat(")*");
		    str = str.concat(getBasisString(i, n, bra));
		}
	    }

	}
	if (str.equals("")) str = str.concat("0");

	return str;
    }

    /**
     * returns a parsable string representation of the braket.
     */
    public String toParseableString() {
	return toString();
    }


}

