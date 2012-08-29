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
 * <p>This class represents a binary operator of the form '+', '-', '*' or '/'.
 * It has all the specialized routines for different arguments build in. For fast
 * quantum simulations it has static functions specialized in applying gates to
 * qubits given as ket vectors. In contrast to the normal operations, the
 * specialized gate methods do not copy the argument, but the result
 * is written into the argument object.</p>
 * The binary operator is identified by the <tt>Tokenizer</tt> class and executed
 * by the <tt>Parse</tt> class.
 * 
 * @see Tokenizer
 * @see Parse
 * @see Braket
 */
public class BinaryOp extends Operator {
    /**
     *  identifies the operator
     */
    protected char data;

    /**
     * creates a binary operator according to the char given. Valid input values:
     * +,-,*,\
     */
    public BinaryOp(char c) {
	data = c;
    }

    /**
     * this is the generic apply method. It dispatches the computation request according
     * to the type of the given arguments. Usually, the compiler is able to bind
     * type specific methods at compile time, but since at compile time it is not
     * clear, which argument type <tt>Parse</tt> might parse, this cannot be done.
     * @param s1 left hand side argument
     * @param s2 right hand side argument
     * @return resulting MathObject
     */
    public MathObject apply(MathObject s1, MathObject s2) {
	MathObject result = null;

	if ((s1 instanceof Complex) && (s2 instanceof Complex)) {
	    result = apply((Complex)s1, (Complex)s2);
	}
	else if ((s1 instanceof Complex) && (s2 instanceof Vect)) {
	    result = apply((Complex)s1, (Vect)s2);
	}
	else if ((s2 instanceof Complex) && (s1 instanceof Vect)) {
	    result = apply((Complex)s2, (Vect)s1);
	}
	else if ((s1 instanceof Vect) && (s2 instanceof Vect)) {
	    result = apply((Vect)s1, (Vect)s2);
	}
	else if ((s1 instanceof Complex) && (s2 instanceof Matrix)) {
	    result = apply((Complex)s1, (Matrix)s2);
	}
	else if ((s1 instanceof Matrix) && (s2 instanceof Complex)) {
	    result = apply((Matrix)s1, (Complex)s2);
	}
	else if ((s1 instanceof Matrix) && (s2 instanceof Matrix)) {
	    result = apply((Matrix)s1, (Matrix)s2);
	}
	else if ((s1 instanceof Matrix) && (s2 instanceof Vect)) {
	    result = apply((Matrix)s1, (Vect)s2);
	}
	else if ((s1 instanceof Gate) && (s2 instanceof Vect)) {
	    result = apply((Gate)s1, (Vect)s2);
	}
	if (result == null) {
	    System.out.println("Error in operation '"+data+"' with arg1 = " + s1.toString() + " and arg2 = " + s2.toString());
	}
	return result;
    }

    /**
     * Complex-Complex operations (+,-,*,/)
     */
    public MathObject apply(Complex s1, Complex s2) {
	switch (data) {
	case '+': return s1.plus(s2);
	case '-': return s1.minus(s2);
	case '*': return s1.times(s2);
	case '/': return s1.divided(s2);
	}
	return null;
    }

    /**
     * Complex-Vector operations (*)
     */
    public MathObject apply(Complex s1, Vect v2) {
	Vect result;
	switch (data) {
	case '*': {
	    if (s1.magnitude() == 0) return new Complex(0);
	    if (v2 instanceof Braket) result = new Braket(((Braket)v2));
	    else result = new Vect((Vect)v2);

	    if (v2 instanceof Braket && ((Braket)v2).isBra()) s1.conjugate();
	    for (int i = 0; i < v2.dimension; i++) {
		result.data[i] = v2.getElement(i).times(s1);
	    }
	    if (v2 instanceof Braket && ((Braket)v2).isBra()) s1.conjugate();
	    return result;
	}
	case '+':
	case '-': {
	    if (s1.magnitude() == 0) return v2;
	    else return null;
	}
	default: return null;
	}
    }

    /**
     * Vector-Vector operation (+,-,*,#)
     */
    public MathObject apply(Vect v1, Vect v2) {
	switch (data) {
	case '+': {
	    Vect result = null;
	    if ((v1.dimension == v2.dimension) && (v1.transpose == v2.transpose)) {
		if ((v2 instanceof Braket) || (v1 instanceof Braket)) 
		    result = new Braket(((Braket)v2).n);
		else result = new Vect(v1.dimension);
		for (int i = 0; i < v1.dimension; i++) {
		    result.data[i] = v1.getElement(i).plus(v2.getElement(i));
		}
		if (v1.transpose) 
		    if (!(result instanceof Braket)) result.transpose();
		return result;
	    } else return null; }
	case '-': {
	    Vect result = null;
	    if ((v1.dimension == v2.dimension) && (v1.transpose == v2.transpose)) {
		if ((v2 instanceof Braket) || (v1 instanceof Braket)) 
		    result = new Braket(((Braket)v2).n);
		else result = new Vect(v1.dimension);

		for (int i = 0; i < v1.dimension; i++) {
		    result.data[i] = v1.getElement(i).minus(v2.getElement(i));
		}
		if (v1.transpose) 
		    if (!(result instanceof Braket)) result.transpose();

		return result;
	    } else return null; }

	// inner product
	case '*': {
	    if (v1.dimension == v2.dimension) {
		Complex result = new Complex();
		for (int i = 0; i < v2.dimension; i++) {
		    result = result.plus(v1.getElement(i).times(v2.getElement(i)));
		}
		return new Complex(result);
	    } else return null;
	}
	// outer product
	case '#': {
	    if ((v1.transpose == true) && (v2.transpose == false)) {
		Matrix result = new Matrix(v1.dimension, v2.dimension);
		for (int i = 0; i < v1.dimension; i++) {
		    for(int j = 0; j < v2.dimension; j++) {
			result.data[i][j] = v1.getElement(i).times(v2.getElement(j));
		    }
		}
		return result;
	    }
	    else if ((v1.transpose == true) && (v2.transpose == true)) {
		Vect result = new Vect(v1.dimension*v2.dimension);
		result.transpose();
		try{
		    if ((v2 instanceof Braket) && (v1 instanceof Braket)) 
			result = Braket.parseBraket(result);
		} catch (Exception e) {
		    LOG.LOG(0, e.getMessage());
		    return null;
		}

		for (int i = 0; i < v1.dimension; i++) {
		    for(int j = 0; j < v2.dimension; j++) {
			result.data[i*v2.dimension+j] = v1.getElement(i).times(v2.getElement(j));
		    }
		}
		return result;
	    }
	    else if ((v1.transpose == false) && (v2.transpose == true)) {
		Matrix result = new Matrix(v2.dimension,v1.dimension);
		for (int i = 0; i < v1.dimension; i++) {
		    for(int j = 0; j < v2.dimension; j++) {
			result.data[j][i] = v1.getElement(i).times(v2.getElement(j));
		    }
		}
		return result;
	    }
	    else if ((v1.transpose == false) && (v2.transpose == false)) {
		Vect result = new Vect(v1.dimension*v2.dimension);

		try{
		    if ((v2 instanceof Braket) && (v1 instanceof Braket)) 
			result = Braket.parseBraket(result);
		} catch (Exception e) {
		    LOG.LOG(0, e.getMessage());
		    return null;
		}

		for (int i = 0; i < v1.dimension; i++) {
		    for(int j = 0; j < v2.dimension; j++) {
			result.data[i*v2.dimension+j] = v1.getElement(i).times(v2.getElement(j));
		    }
		}
		return result;

	    }
	    return null;
	}
	}
	return null;
    }

    /**
     * matrix-matrix operations (+,-,*,#)
     */
    public MathObject apply(Matrix m1, Matrix m2) {
	Matrix result = null;
	switch (data) {
	case '+': {
	    if ((m1.n() == m2.n()) && (m1.m() == m2.m())) {
		result = new Matrix(m1.n(), m1.m());
		for (int i = 0; i < m1.n(); i++) {
		    for ( int j = 0; j < m1.m(); j++) {
			result.setElement(i,j, m1.getElement(i,j).plus(m2.getElement(i,j)));
		    }
		}
	    }
	    return result;
	}
	case '-':  {
	    if ((m1.n() == m2.n()) && (m1.m() == m2.m())) {
		result = new Matrix(m1.n(), m1.m());
		for (int i = 0; i < m1.n(); i++) {
		    for ( int j = 0; j < m1.m(); j++) {
			result.setElement(i,j, m1.getElement(i,j).minus(m2.getElement(i,j)));
		    }
		}
	    }
	    return result;
	}
	case '*':  {
	    if ((m1.m() == m2.n()) && (m1.n() == m2.m())) {
		result = new Matrix(m1.n(), m2.m());
		for (int i = 0; i < m1.n(); i++) {
		    for ( int j = 0; j < m2.m(); j++) {
			for (int u = 0; u < m1.m(); u++) {
			    result.setElement(i,j, result.getElement(i,j).plus(m1.getElement(i,u).times(m2.getElement(u,j))));
			}
		    }
		}
	    }
	    return result;
	}
	// outer product
	case '#': {
	    result = new Matrix(m1.n()*m2.n(), m1.m()*m2.m());
	    for (int i1 = 0; i1 < m1.n(); i1++) {
		for (int j1 = 0; j1 < m1.m(); j1++) {
		    for (int i2 = 0; i2 < m2.n(); i2++) {
			for (int j2 = 0; j2 < m2.m(); j2++) {
			    result.setElement(i1*m2.n()+i2, j1*m2.m()+j2, 
				   m1.getElement(i1,j1).times(m2.getElement(i2,j2))  );
			}
		    }
		}
	    }
	    return result;
	}
	}
	return null;
    }

    /**
     * complex-matrix operations (*)
     */
    public MathObject apply(Complex s1, Matrix m2) {
	Matrix result = null;
	switch (data) {
	case '*': {
	    result = new Matrix(m2.n(), m2.m());
	    for (int i = 0; i < m2.n(); i++) {
		for (int j = 0; j < m2.m(); j++) {
		    result.setElement(i,j,m2.getElement(i,j).times(s1));
		}
	    }
	    return result;
	}
	case '+':
	case '-': {
	    if (s1.magnitude() == 0) return m2;
	    else return null;
	}
	}
	return null;
    }

    /**
     * matrix-complex operations (*,/)
     */
    public MathObject apply(Matrix m1, Complex s2) {
	Matrix result = null;
	switch (data) {
	case '*': {
	    result = new Matrix(m1.n(), m1.m());
	    for (int i = 0; i < m1.n(); i++) {
		for (int j = 0; j < m1.m(); j++) {
		    result.setElement(i,j,m1.getElement(i,j).times(s2));
		}
	    }
	    return result;
	}
	case '/': {
	    result = new Matrix(m1.n(), m1.m());
	    for (int i = 0; i < m1.n(); i++) {
		for (int j = 0; j < m1.m(); j++) {
		    result.setElement(i,j,m1.getElement(i,j).divided(s2));
		}
	    }
	    return result;
	}
	case '+':
	case '-': {
	    if (s2.magnitude() == 0) return m1;
	    else return null;
	}
	}
	return null;
    }

    /**
     * matrix-vector operations (*)
     */
    public MathObject apply(Matrix m1, Vect v2) {
	Vect result = null;
	switch (data) {
	case '*': {
	    if ((m1.m() == v2.dimension) && (v2.transpose)) {
		if ((v2 instanceof Braket) && (m1.n() == m1.m()))
		    result = new Braket(((Braket)v2).n);
		else result = new Vect(m1.n());
		
		for (int i = 0; i < m1.n(); i++) {
		    for (int u = 0; u < m1.m(); u++) {
			result.data[i] = 
			    result.getElement(i).plus(m1.getElement(i,u).times(v2.getElement(u)));
		    }
		}
		if (!(result instanceof Braket)) result.transpose();
		return result;
	    }
	}
	}
	return null;
    }

    /**
     * integer power funtion base^exp
     * @param base
     * @param exp
     * @return value as int
     */
    public static int pow(int base, int exp) {
	int result = 1;
	for (int j = 0; j < exp; j++) result *= base;
	return result;
    }

    /**
     * gate-vector operations (*)
     */
    public MathObject apply(Gate g1, Vect v2) {
	Vect result = null;
	int dim = 0, n = 0, cs = 0;
	int first, offset, step;
	boolean exchange = false;
	int[] jokers = new int[g1.n];
	/*
	switch (data) {
	case '*': {
	    if (pow(2, g1.n) == v2.dimension) {
		
		if (v2 instanceof Braket) result = new Braket((Braket)v2);
		else result = new Vect(v2);


		n = g1.n;
		dim = pow(2, n);

		first = 0;
		step = 0;
		offset = 0;

		int j = 0;
		for (int k = 0; k < n; k++) {
		    switch (g1.gate_descr.charAt(k)) {
		    case '1': cs++; first += pow(2, n-k-1); break;
		    case 'm': offset += pow(2, n-k-1); break;
		    case '+': {
			if (exchange == false) offset = pow(2, n-k-1); 
			else first = pow(2,n-k-1);
			exchange = true; 
		    }
		    }

		    if (g1.gate_descr.charAt(k) == '-') { 
			jokers[j] = n-k-1;
			j++;
		    }
		}

		if (exchange) {
		    boolean[] binary_counter = new boolean[n-2];

		    offset = offset - first;

		    for (int i = 0; i < pow(2,n-2); i++) {
			step = 0;
			if (n-2 > 0) {
			    // calculate stepsize
			    for (int l = 0; l < n-2; l++) {
				step += (binary_counter[l]? pow(2,jokers[n-2-l-1]) : 0);
			    } 

			    // increase binary counter
			    for(j=0;(j<n-2)&&!(binary_counter[j]=!binary_counter[j++]););
			}


			result.data[first+step] = v2.data[first + offset + step];
			result.data[first + offset + step] = v2.data[first+step];
		    }
		
		}
		else if (offset != 0 || cs != 0) {

		    boolean[] binary_counter = new boolean[n-cs-1];

		    //		for (int l = 0; l < n-cs-1; l++) binary_counter[l] = false;


		    for (int i = 0; i < pow(2,n-cs-1); i++) {
			step = 0;
			if (n-cs-1 > 0) {

			    // calculate stepsize
			    for (int l = 0; l < n-cs-1; l++) {
				step += (binary_counter[l]? pow(2,jokers[n-cs-1-l-1]) : 0);
			    } 

			    // increase binary counter
			    for(j=0;(j<n-cs-1)&&!(binary_counter[j]=!binary_counter[j++]););


			    result.data[first+step] = 
				g1.data[0][0].times(v2.data[first+step]).plus(
			        g1.data[0][1].times(v2.data[first+offset+step]));
			    result.data[first + offset + step] = 
				g1.data[1][0].times(v2.data[first+step]).plus(
			        g1.data[1][1].times(v2.data[first+offset+step]));
			}


		    }
		
		}
	    }

	}
	}
	*/
	return result;
    }


    /**
     * this method dispatches gate operation to specialized methods. It is a specialized
     * method used in the quantum simulation and is not used during a normal parse
     * process. The parameter error can be a 2x2 matrix with which the gate is modified
     * in oder to simulate operational errors.
     * @param g1 gate to be applied
     * @param v2 ket vector
     * @param error 2x2 matrix or null
     * @see Measurement#partialMeasurement(Braket, int)
     * @see BinaryOp#implicitApply2x2(Gate, Braket, Matrix)
     * @see BinaryOp#implicitApply2x2a(Gate, Braket)
     * @see BinaryOp#implicitApply2x2b(Gate, Braket)
     * @see BinaryOp#implicitApplyNxN(Gate, Braket)
     * @see BinaryOp#implicitApplyNxNo(Gate, Braket)
     */
    public static void implicitApply(Gate g1, Braket v2, Matrix error) {
	if (pow(2, g1.n) == v2.dimension) {

	    Mathlib.fireComputationEvent(g1, v2, 0, 0, ComputationEvent.STARTED);

	    int first_m = g1.gate_descr.indexOf('m');
	    int last_m = g1.gate_descr.lastIndexOf('m');
	    int u = g1.gate_descr.indexOf('u');
	    int d = g1.gate_descr.indexOf('d');
	    int measurement = g1.gate_descr.indexOf('!');
	    if (measurement != -1) Measurement.partialMeasurement(v2, measurement);
	    else {
		// 2x2 matrix
		if (u != -1 || d != -1)
		    implicitApply2x2(g1, v2, null);
		else if (first_m != -1 && last_m == first_m) {
		    Complex comp = new Complex(0, 0);
		    Matrix m = g1.getMatrix();

		    if (error == null 
			&& m.getElement(0,0).equals(comp) 
			&& m.getElement(1,1).equals(comp))
			implicitApply2x2b(g1, v2);
		    else if (error == null 
			     && m.getElement(1,0).equals(comp) 
			     && m.getElement(0,1).equals(comp))
			implicitApply2x2a(g1, v2);
		    else
			implicitApply2x2(g1, v2, error);
		}
		// NxN matrix
		else if (first_m != last_m) implicitApplyNxN(g1, v2);
	    }

	    // notify listeners
	    Mathlib.fireComputationEvent(g1, v2, 0, 0, ComputationEvent.DONE);
	}
    }


    /**
     * this method applies a general unitary 2x2 matrix onto the corresponding 
     * subspace as specified by the Gate argument to the ket given by v2.
     * The parameter error is a 2x2 matrix with which the gate gets modified.
     * @see BinaryOp#implicitApply2x2a(Gate, Braket)
     */
    public static void implicitApply2x2(Gate g1, Braket v2, Matrix error){
	int dim = 0, n = 0, cs = 0;
	int first, offset, step;
	int u = 0, d = 0;
	Complex c1, c2;
	int[] jokers = new int[g1.n];
	Matrix m;	
	Complex m11, m12, m21, m22;

	//	System.out.println("implicitApply2x2");

	if (pow(2, g1.n) == v2.dimension) {
		
	    n = g1.n;
	    dim = pow(2, n);

	    first = 0;
	    step = 0;
	    offset = 0;

	    int j = 0;
	    for (int k = 0; k < n; k++) {
		switch (g1.gate_descr.charAt(k)) {
		case '1': cs++; first += pow(2, n-k-1); break;
		case 'm': offset += pow(2, n-k-1); break;
		case 'u': offset += pow(2, n-k-1); u = 1; break;
		case 'd': offset += pow(2, n-k-1); d = 1; break;
		}

		if (g1.gate_descr.charAt(k) == '-') { 
		    jokers[j] = n-k-1;
		    j++;
		}
	    }

	    if (u != 0 && d == 0)
		m = Matrix.parseMatrix("[1 1, 0 0]");
	    else if (u == 0 && d != 0)
		m = Matrix.parseMatrix("[0 0, 1 1]");
	    else {
		m = g1.getMatrix();

		if (error != null) {
		    BinaryOp times = new BinaryOp('*');
		    m = (Matrix)(times.apply(m, error));
		}
	    }

	    Complex comp = new Complex(0,0);
	    m11 = m.getElement(0, 0);
	    m12 = m.getElement(0, 1);
	    m21 = m.getElement(1, 0);
	    m22 = m.getElement(1, 1);

	    if (offset != 0 || cs != 0) {
		boolean[] binary_counter = new boolean[n-cs-1];

		for (int i = 0; i < pow(2,n-cs-1); i++) {
		    step = 0;
		    if (n-cs-1 > 0) {
			// calculate stepsize
			for (int l = 0; l < n-cs-1; l++) {
			    step += (binary_counter[l]? pow(2,jokers[n-cs-1-l-1]) : 0);
			} 

			// increase binary counter
			for(j=0;(j<n-cs-1)&&!(binary_counter[j]=!binary_counter[j++]););
		    }

		    //
		    // |m11 m12|   | c1 |
		    // |       | * |    |
		    // |m21 m22|   | c2 |
		    // 

		    // c1 == 0
		    if (v2.data[first+step] == null) {
			//c2 == 0
			if (v2.data[first+offset+step] == null) {
			    // do nothing!
			}
			// c2 != 0
			else {
			    v2.data[first+step] = m12.times(v2.data[first+offset+step]);
			    v2.data[first+offset+step] = 
				m22.times(v2.data[first+offset+step]);
			}
		    }
		    // c1 != 0
		    else {
			if (v2.data[first+offset+step] == null) {
			    v2.data[first+offset+step] = m21.times(v2.data[first+step]);
			    v2.data[first+step] = m11.times(v2.data[first+step]);
			}
			else {
			    c1 = new Complex(v2.data[first+step]);
			    c2 = new Complex(v2.data[first+offset+step]);

			    v2.data[first+step] = m11.times(c1).plus(m12.times(c2));
			    v2.data[first+offset+step] = m21.times(c1).plus(m22.times(c2));
			}
		    }
		}
	    }
	}
    }

    /**
     * this method is a optimized version to apply a unitary 2x2 matrix with zeros 
     * of the counter diagonal onto the corresponding 
     * subspace as specified by the Gate argument to the ket given by v2.
     * @see BinaryOp#implicitApply2x2(Gate, Braket, Matrix)
     * @see BinaryOp#implicitApply2x2b(Gate, Braket)
     */
    public static void implicitApply2x2a(Gate g1, Braket v2){
	int dim = 0, n = 0, cs = 0;
	int first, offset, step;
	Complex c1, c2;
	int[] jokers = new int[g1.n];
	Matrix m;	
	Complex m11, m22;

	//	System.out.println("implicitApply2x2a");

	if (pow(2, g1.n) == v2.dimension) {
		
	    n = g1.n;
	    dim = pow(2, n);

	    first = 0;
	    step = 0;
	    offset = 0;

	    int j = 0;
	    for (int k = 0; k < n; k++) {
		switch (g1.gate_descr.charAt(k)) {
		case '1': cs++; first += pow(2, n-k-1); break;
		case 'm': offset += pow(2, n-k-1); break;
		}

		if (g1.gate_descr.charAt(k) == '-') { 
		    jokers[j] = n-k-1;
		    j++;
		}
	    }
	    m = g1.getMatrix();
	    m11 = m.getElement(0, 0);
	    m22 = m.getElement(1, 1);

	    if (offset != 0 || cs != 0) {
		boolean[] binary_counter = new boolean[n-cs-1];

		for (int i = 0; i < pow(2,n-cs-1); i++) {
		    step = 0;
		    if (n-cs-1 > 0) {

			// calculate stepsize
			for (int l = 0; l < n-cs-1; l++) {
			    step += (binary_counter[l]? pow(2,jokers[n-cs-1-l-1]) : 0);
			} 

			// increase binary counter
			for(j=0;(j<n-cs-1)&&!(binary_counter[j]=!binary_counter[j++]););


		    }

		    //
		    // |m11  0 |   | c1 |
		    // |       | * |    |
		    // | 0  m22|   | c2 |
		    // 

		    // c1 == 0
		    if (v2.data[first+step] == null) {
			//c2 == 0
			if (v2.data[first+offset+step] == null) {
			    // do nothing!
			}
			// c2 != 0
			else {
			    v2.data[first+step] = null;
			    v2.data[first+offset+step] = 
				m22.times(v2.data[first+offset+step]);
			}
		    }
		    // c1 != 0
		    else {
			if (v2.data[first+offset+step] == null) {
			    v2.data[first+offset+step] = null;
			    v2.data[first+step] = m11.times(v2.data[first+step]);
			}
			else {
			    v2.data[first+step] = m11.times(v2.data[first+step]);
			    v2.data[first+offset+step] = m22.times(v2.data[first+offset+step]);
			}
		    }
		}
	    }
	}
    }


    /**
     * this method is a optimized version to apply a unitary 2x2 matrix with zero 
     * diagonal onto the corresponding 
     * subspace as specified by the Gate argument to the ket given by v2.
     * @see BinaryOp#implicitApply2x2(Gate, Braket, Matrix)
     * @see BinaryOp#implicitApply2x2a(Gate, Braket)
     */
    public static void implicitApply2x2b(Gate g1, Braket v2){
	int dim = 0, n = 0, cs = 0;
	int first, offset, step;
	Complex c1, c2;
	int[] jokers = new int[g1.n];
	Matrix m;	
	Complex m12, m21;

	//	System.out.println("implicitApply2x2b");

	if (pow(2, g1.n) == v2.dimension) {
		
	    n = g1.n;
	    dim = pow(2, n);

	    first = 0;
	    step = 0;
	    offset = 0;

	    int j = 0;
	    for (int k = 0; k < n; k++) {
		switch (g1.gate_descr.charAt(k)) {
		case '1': cs++; first += pow(2, n-k-1); break;
		case 'm': offset += pow(2, n-k-1); break;
		}

		if (g1.gate_descr.charAt(k) == '-') { 
		    jokers[j] = n-k-1;
		    j++;
		}
	    }

	    m = g1.getMatrix();
	    m12 = m.getElement(0, 1);
	    m21 = m.getElement(1, 0);

	    if (offset != 0 || cs != 0) {
		boolean[] binary_counter = new boolean[n-cs-1];

		for (int i = 0; i < pow(2,n-cs-1); i++) {
		    step = 0;
		    if (n-cs-1 > 0) {

			// calculate stepsize
			for (int l = 0; l < n-cs-1; l++) {
			    step += (binary_counter[l]? pow(2,jokers[n-cs-1-l-1]) : 0);
			} 

			// increase binary counter
			for(j=0;(j<n-cs-1)&&!(binary_counter[j]=!binary_counter[j++]););


		    }

		    //
		    // | 0  m12|   | c1 |
		    // |       | * |    |
		    // |m21  0 |   | c2 |
		    // 

		    // c1 == 0
		    if (v2.data[first+step] == null) {
			//c2 == 0
			if (v2.data[first+offset+step] == null) {
			    // do nothing!
			}
			// c2 != 0
			else {
			    v2.data[first+step] = m12.times(v2.data[first+offset+step]);
			    v2.data[first+offset+step] = null;
				
			}
		    }
		    // c1 != 0
		    else {
			if (v2.data[first+offset+step] == null) {
			    v2.data[first+offset+step] = m21.times(v2.data[first+step]);
			    v2.data[first+step] = null;
			}
			else {
			    c1 = new Complex(v2.data[first+step]);

			    v2.data[first+step] = m12.times(v2.data[first+offset+step]);
			    v2.data[first+offset+step] = m21.times(c1);
			}
		    }
		}
	    }
	}
    }


    /**
     * this method applies a general unitary NxN matrix onto the corresponding 
     * subspace as specified by the Gate argument to the ket given by v2.
     * @see BinaryOp#implicitApply2x2(Gate, Braket, Matrix)
     * @see BinaryOp#implicitApplyNxNo(Gate, Braket)
     */
    public static void implicitApplyNxN(Gate g1, Braket v2){
	int dim = 0, n = 0, cs = 0, ms = 0;
	int first, step;
	int[] jokers = new int[g1.n];
	int[] m = new int[g1.n];

	Complex[] temp = null;

	if (pow(2, g1.n) == v2.dimension) {
		
	    n = g1.n;
	    dim = pow(2, n);

	    first = 0;
	    step = 0;

	    int j = 0;
	    for (int k = 0; k < n; k++) {
		switch (g1.gate_descr.charAt(k)) {
		case '1': cs++; first += pow(2, n-k-1); break;
		case 'm': m[ms] = n-k-1; ms++; break;
		case '-': jokers[j] = n-k-1; j++; break;
		}
	    }

	    if (ms != 0 || cs != 0) {
		boolean[] binary_counter = new boolean[ms];

		int[] offsets = new int[pow(2,ms)];
		for (int i = 0; i < pow(2, ms); i++) {
		    // calculate steps
		    for (int l = 0; l < ms; l++) {
			offsets[i] += (binary_counter[l]? pow(2,m[ms-l-1]) : 0);
		    } 
		    // increase binary counter
		    for(j=0;(j<ms)&&!(binary_counter[j]=!binary_counter[j++]););
		}

		// fetch matrix
		Matrix matrix = null;
		if (ms != 0) matrix = g1.getMatrix();

		// reinitialize binary counter
		binary_counter = new boolean[n-cs-ms];

		for (int i = 0; i < pow(2,n-cs-ms); i++) {
		    step = 0;
		    if (n-cs-ms > 0) {
			// calculate stepsize
			for (int l = 0; l < n-cs-ms; l++) {
			    step += (binary_counter[l]? pow(2,jokers[n-cs-ms-l-1]) : 0);
			} 

			// increase binary counter
			for(j=0;(j<n-cs-ms)&&!(binary_counter[j]=!binary_counter[j++]););
		    }

		    // get values from ket
		    temp = new Complex[ms*2];
		    for (int l = 0; l < ms; l++) {
			if (v2.data[first+offsets[2*l]+step] == null)
			    temp[2*l] = null;
			else 
			    temp[2*l] = new Complex(v2.data[first+offsets[2*l]+step]);

			if (v2.data[first+offsets[2*l+1]+step] == null)
			    temp[2*l+1] = null;
			else
			    temp[2*l+1] = new Complex(v2.data[first+offsets[2*l+1]+step]);
		    }

		    // do calculation
		    for (int l = 0; l < ms; l++) {
			v2.data[first+offsets[2*l]+step] = 
			    matrix.getElement(2*l,0).times_(temp[0]);
			v2.data[first+offsets[2*l+1]+step] = 
			    matrix.getElement(2*l+1,0).times_(temp[0]);

			for (int k = 1; k < 2*ms; k++) {
			    if (v2.data[first+offsets[2*l]+step] == null)
				v2.data[first+offsets[2*l]+step] = 
				    matrix.getElement(2*l,k).times_(temp[k]);
			    else
				v2.data[first+offsets[2*l]+step] = 
				    v2.data[first+offsets[2*l]+step].
				    plus_(matrix.getElement(2*l,k).times_(temp[k]));

			    if (v2.data[first+offsets[2*l+1]+step] == null)
				v2.data[first+offsets[2*l+1]+step] = 
				    matrix.getElement(2*l+1,k).times_(temp[k]);
			    else
				v2.data[first+offsets[2*l+1]+step] = 
				    v2.data[first+offsets[2*l+1]+step].
				    plus_(matrix.getElement(2*l+1,k).times_(temp[k]));
			}
		    }
		}
		
	    }
	}
    }

    /**
     * this method is an optimized method for applying a general unitary NxN matrix 
     * onto the corresponding subspace as specified by the Gate argument to the ket 
     * given by v2. It should perform better for N > 4 than the general version.
     * @see BinaryOp#implicitApply2x2(Gate, Braket, Matrix)
     * @see BinaryOp#implicitApplyNxN(Gate, Braket)
     */
    public static void implicitApplyNxNo(Gate g1, Braket v2){
	int dim = 0, n = 0, cs = 0, ms = 0;
	int first, step;
	int[] jokers = new int[g1.n];
	int[] m = new int[g1.n];

	// make working copy...
	Braket temp = new Braket(v2);

	if (pow(2, g1.n) == v2.dimension) {
		
	    n = g1.n;
	    dim = pow(2, n);

	    first = 0;
	    step = 0;

	    int j = 0;
	    for (int k = 0; k < n; k++) {
		switch (g1.gate_descr.charAt(k)) {
		case '1': cs++; first += pow(2, n-k-1); break;
		case 'm': m[ms] = n-k-1; ms++; break;
		case '-': jokers[j] = n-k-1; j++; break;
		}
	    }

	    if (ms != 0 || cs != 0) {
		boolean[] binary_counter = new boolean[ms];

		int[] offsets = new int[pow(2,ms)];
		for (int i = 0; i < pow(2, ms); i++) {
		    // calculate steps
		    for (int l = 0; l < ms; l++) {
			offsets[i] += (binary_counter[l]? pow(2,m[ms-l-1]) : 0);
		    } 
		    // increase binary counter
		    for(j=0;(j<ms)&&!(binary_counter[j]=!binary_counter[j++]););
		}

		// fetch matrix
		Matrix matrix = null;
		if (ms != 0) matrix = g1.getMatrix();

		// reinitialize binary counter
		binary_counter = new boolean[n-cs-ms];

		for (int i = 0; i < pow(2,n-cs-ms); i++) {
		    step = 0;
		    if (n-cs-ms > 0) {
			// calculate stepsize
			for (int l = 0; l < n-cs-ms; l++) {
			    step += (binary_counter[l]? pow(2,jokers[n-cs-ms-l-1]) : 0);
			} 

			// increase binary counter
			for(j=0;(j<n-cs-ms)&&!(binary_counter[j]=!binary_counter[j++]););
		    }

		    // do calculation
		    for (int l = 0; l < ms; l++) {
			v2.data[first+offsets[2*l]+step] = 
			    matrix.getElement(2*l,0).times_(temp.data[0]);
			v2.data[first+offsets[2*l+1]+step] = 
			    matrix.getElement(2*l+1,0).times_(temp.data[0]);

			for (int k = 1; k < 2*ms; k++) {
			    if (v2.data[first+offsets[2*l]+step] == null)
				v2.data[first+offsets[2*l]+step] = 
				    matrix.getElement(2*l,k).times_(temp.data[k]);
			    else
				v2.data[first+offsets[2*l]+step] = 
				    v2.data[first+offsets[2*l]+step].
				    plus_(matrix.getElement(2*l,k).times_(temp.data[k]));

			    if (v2.data[first+offsets[2*l+1]+step] == null)
				v2.data[first+offsets[2*l+1]+step] = 
				    matrix.getElement(2*l+1,k).times_(temp.data[k]);
			    else
				v2.data[first+offsets[2*l+1]+step] = 
				    v2.data[first+offsets[2*l+1]+step].
				    plus_(matrix.getElement(2*l+1,k).times_(temp.data[k]));
			}
		    }
		}
		
	    }
	}
    }

    /**
     * decides whether the current operator precedes the operator given as
     * a parameter.
     * @param b a binary operator to compare with
     */
    public boolean precedence(BinaryOp b) {
	if (("+-".indexOf(this.data) != -1) && ("*#/".indexOf(b.data) != -1))
	    return false;
	else return true;
    }


    /**
     * returns the string representation of the binary operator
     */
    public String toString() {
	return new Character(data).toString();
    }
}
