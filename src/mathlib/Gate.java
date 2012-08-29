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
 * Class representing a quantum gate. Since it is not feasable to save gates as
 * unitary matrices and usually a gate only affects a small number of qubits, it
 * is useful to introduce a more abstract description of a model. The syntax is
 * the following:
 * <p><tt>{-:-:1:NOT:-}</tt> - this would represent a controlled NOT gate 
 * operating on the 4th of 5 qubits and beeing controlled by the 3rd. The NOT entry
 * can be replaced by any other unitary (2x2) matrix.</p>
 * The object specifying the matrix can be a variable, a MathMap producing a matrix or
 * a matrix definition.
 * <p>For higher dimensional gates the notation is as follows:</p>
 * <tt>{1:-:U:U}</tt> this represents a controlled U gate operating on the 3rd and 4th
 * qubit of 4 qubits, it is controlled by the first qubit.
 * @see BinaryOp
 * @see Braket
 */
public class Gate extends Argument {
    /** holds the description of the gate in string form */
    public String gate_descr; // allowed characters = 1, -, m

    /** holds the matrix dimension*/
    public int matrixDimension = 0;

    /** holds the matrix name */
    public String matrixName;

    protected boolean transpose = false;
    protected boolean conjugate = false;
    protected boolean negative = false;

    /** holds the number of qubits */
    public int n;


    /**
     * here n is the number of qubits ==> the dimension of the corresponding
     * matrix is 2^n. This constructor simply generates a gate with is unity.
     */
    public Gate(int n) {
	this.n = n;
	gate_descr = new String("");
	matrixName = new String("");
	matrixDimension = 0;
	for (int i = 0; i < n; i++) {
	    gate_descr = gate_descr.concat("-");
	}
    }

    /**
     * clone constructor
     */
    public Gate(MathObject o) {
	if (o instanceof Gate) {
	    Gate g1 = (Gate) o;
	    this.n = g1.n;
	    this.transpose = g1.transpose;
	    this.conjugate = g1.conjugate;
	    this.negative = g1.negative;
	    this.matrixDimension = g1.matrixDimension;

	    gate_descr = g1.gate_descr;
	    matrixName = g1.matrixName;
	}
    }

    /**
     * creates a gate for a given gate_descr string and matrix name
     */
    public Gate(String gate_descr, String matrixName) {
	this.n = gate_descr.length();
	this.gate_descr = gate_descr;
	this.matrixDimension = ((matrixName == null)? 0:((Matrix)Parse.parseExpression(matrixName)).n());
	this.matrixName = ((matrixName == null) ? "" : matrixName);
    }

    /**
     * creates a gate for n qubits having a 2x2 matrix matrixName operating at position matrixPos
     */
    public Gate(int n, int matrixPos, String matrixName) {
	this.n = n;
	char[] descr = new char[n];
	for (int i = 0; i < n; i++) {
	    descr[i] = ((matrixPos == i)? 'm':'-');
	}
	this.gate_descr = new String(descr);
	this.matrixDimension = ((matrixName == null)? 0:((Matrix)Parse.parseExpression(matrixName)).n());
	this.matrixName = ((matrixName == null) ? "" : matrixName);
    }

    /**
     * clone method
     */
    public Object clone() {
	return new Gate(this);
    }

    /**
     * parses a gate from a given string. Throws an IllegalArgumentException when the
     * operation fails.
     */
    public static Gate parseGate(String str) throws IllegalArgumentException {
	Gate g = null;

	int pos1 = 0, pos2 = 0; 
	int n = 0;
	int j = 0;
	int c = 0; // number of controls 

	Matrix matrix = null;
	boolean matrixAssigned = false;
	String tmpName;

	try {
	    if ((str.charAt(0) == '{') && (str.charAt(str.length()-1) == '}')) 
		str = str.substring(1, str.length()-1);
	    else 
		throw new IllegalArgumentException("proper brakets expected: "+ str);
	}
	catch (Exception e) {
	    throw new IllegalArgumentException("proper brakets expected: "+ str);
	}

	str = str.trim();
	str = str.concat(":");

	for (int i = 0; i < str.length(); i++) {
	    if (str.charAt(i) == ':') n++;
	}
	
	g = new Gate(n);
	g.gate_descr = new String("");

	for (int i = 0; i < str.length(); i++) {
	    if (str.charAt(i) == ':') {
		pos2 = i;
		if (str.substring(pos1, pos2).trim().equals("-")) {
		    g.gate_descr = g.gate_descr.concat("-");
		}
		else if (str.substring(pos1, pos2).trim().equals("u")) {
		    g.gate_descr = g.gate_descr.concat("u");
		}
		else if (str.substring(pos1, pos2).trim().equals("d")) {
		    g.gate_descr = g.gate_descr.concat("d");
		}
		else if (str.substring(pos1, pos2).trim().equals("!")) {
		    g.gate_descr = g.gate_descr.concat("!");
		}
		else if (str.substring(pos1, pos2).trim().equals("1")) {
		    g.gate_descr = g.gate_descr.concat("1");
		    c++;
		}
		else {
		    try {
			tmpName = mathlib.evaluateExpression(str.substring(pos1,pos2).trim());

			if (matrixAssigned && g.matrixName.equals(str.substring(pos1,pos2).trim()) == false)
			    throw new IllegalArgumentException("matrix mismatch");
			else if (matrixAssigned == false) {
			    matrix = (Matrix)mathlib.getVar(tmpName);
			}
		    } catch (IllegalArgumentException iae) {
			throw new IllegalArgumentException("invalid object in gate definition: " + str.substring(pos1, pos2));
		    }
		    // here is everything ok!
		    if (matrixAssigned == false && matrix.n()%2==0 && matrix.m()%2 ==0 ){
			matrixAssigned = true;
			g.matrixName = str.substring(pos1,pos2).trim();
			g.gate_descr = g.gate_descr.concat("m");
		    }
		    //XXX  dimension!!!
		    else if (matrixAssigned == true && matrix.n()%2==0 && matrix.m()%2 ==0 ) g.gate_descr = g.gate_descr.concat("m");
		    else throw new IllegalArgumentException("invalid dimension of mtrix / double matrix definition: " + str.substring(pos1, pos2));
		}
		// get rid of ":"
		pos1 = pos2+1;
		j++;
	    }
	}

	// plausibility
	if (c > 0 && matrixAssigned == false) 
	    throw new IllegalArgumentException("control without matrix!");

	return g;
    }

    /**
     * transposes the gate
     */ 
    public void transpose() {
	transpose = transpose ^ true;
    }

    /**
     * complex conjugates the gate
     */
    public void conjugate() {
	conjugate = conjugate ^ true;
    }

    /**
     * negates the gate
     */
    public void negative() {
	negative = negative ^ true;
    }

    /**
     * returns the matrix applied to the subspace
     */
    public Matrix getMatrix() {
	if (matrixName == null || matrixName.trim().equals("")) return null;
	Matrix m = new Matrix((Matrix)Parse.parseExpression(matrixName));
	if (transpose) m.transpose();
	if (conjugate) m.conjugate();
	if (negative) m.negative();
	return m;
    }

    /**
     * returns the expression identifying the matrix. Can be a variable name or regular 
     * expression
     */
    public String getMatrixName() {
	return matrixName;
    }

    /**
     * sets the matrix name (can be a variable name of a matrix or an regular expression.
     * If the dimension do not fit, the matrixName is not set and false is returned.
     */
    public boolean setMatrixName(String matrixName) {
	MathObject m = Parse.parseExpression(matrixName);
	if (m != null && m instanceof Matrix) {
	    if (((Matrix)m).m() == ((Matrix)m).n() && ((Matrix)m).m() == matrixDimension) {
		this.matrixName = matrixName;
		return true;
	    }
	}
	return false;
    }

    /**
     * equality predicate
     */
    public boolean equals(MathObject m1) {
	boolean veto = false;
	if (m1 instanceof Gate) {
	    Gate g = (Gate)m1;
	    veto = (n != g.n);
	    veto = veto | (gate_descr.equals(g.gate_descr) == false);
	    veto = veto | (matrixName.equals(g.matrixName) == false);
	    veto = veto | (transpose != g.transpose);
	    veto = veto | (conjugate != g.conjugate);
	    veto = veto | (negative != g.negative);
	    return (!veto);
	}
	else return false;
    }

    /**
     * return string representation of the gate
     */
    public String toString() {
	String str = new String("{");

	for (int i = 0; i < n; i++) {
	    if (gate_descr.charAt(i) != 'm')
		str = str.concat(gate_descr.charAt(i) + ":");
	    else 
		str = str.concat(matrixName + ":");
	}
	if (str.equals("{") == false) str = str.substring(0, str.length()-1);
	str = str.concat("}");

	return str;
    }
    /*
    public int getComplexity() {
	int cs = 0;
	for (int i = 0; i < n; i++) {
	    if (gate_descr.charAt(i) == '1' && gate_descr.charAt(i) == 'm') cs++;
	}
	return cs;
    }
    */
}

