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
 * Class representing a complex number. It is the most widely used
 * object in the mathlib package as it provides the elementary arithmetic 
 * operations of complex numbers and controls the precision.
 * @see MathObject
 * @see Mathlib
 */
public class Complex extends Argument {
    private double re, im;
    private static int digits = 2;
    private static int internalDigits = 14;
    private static boolean simple = true;

    /**
     * precision of output
     */
    protected static double eps = 0.5e-14;

    /**
     * create the number 0.
     * @see Complex#Complex(double, double)
     */
    public Complex() {
	this(0, 0);
    }

    /**
     * create a real number
     * @see Complex#Complex(double, double)
     */
    public Complex(double re) {
	this(re, 0);
    }

    /**
     * create a complex number
     */
    public Complex(double re, double im) {
	this.re = re;
	this.im = im;
    }

    /**
     * clones a complex number
     */
    public Complex(MathObject o) {
	if (o instanceof Complex) {
	    this.re = ((Complex)o).re();
	    this.im = ((Complex)o).im();
	}
    }

    /**
     * clone method
     */
    public Object clone() {
	return new Complex(this);
    }

    /**
     * returns the real part of the complex number
     * @return real part
     */
    public double re() {
	return (Math.abs(re) < eps)? 0 : re;
    }

    /**
     * returns the complex part of the complex number
     * @return complex part
     */
    public double im() {
	return (Math.abs(im) < eps)? 0: im;
    }

    /**
     * set the complex number to a given value
     * @param re real part
     * @param im imaginary part
     */
    public void set(double re, double im) {
	this.re = re;
	this.im = im;
    }

    // polar representation

    /**
     * returns the squared magnitude of the complex number
     * @return squared magnitude
     */
    public double magnitudeSquared() {
	return re*re + im*im;
    }

    /**
     * returns the magnitude of the complex number. For details
     * see Numerical Recipes
     * @return magnitude
     */
    public double magnitude() {
	double absA = Math.abs(re);
	double absB = Math.abs(im);
	if (absA >= absB) {
	    double b_over_a = im/re;
	    return absA*Math.sqrt(1+(b_over_a)*(b_over_a));
	}
	else {
	    double a_over_b = re/im;
	    return absB*Math.sqrt(1+(a_over_b)*(a_over_b));
	} 
    }

    /**
     * returns the angle of the complex number in the complex plane
     * @return angle in rad
     */
    public double angle() {
	return Math.atan2(im,re);
    }

    // operations

    /**
     * add a a given complex number with this object and returns
     * a new complex number
     * @return resulting complex number
     */
    public Complex plus(Complex rhs) {
	return new Complex(re+rhs.re,im+rhs.im);
    }

    /**
     * specialized addition routine for large Braket objects where the complex
     * numbers are not instantiated before actually participating in an operation.
     * @see Braket
     */
    public Complex plus_(Complex rhs) {
	if (rhs == null) return new Complex(re, im);
	else return plus(rhs);
    }

    /**
     * add a given complex number with this object and returns
     * a new complex number
     * @return resulting complex number
     */
    public Complex minus(Complex rhs) {
	return new Complex(re-rhs.re,im-rhs.im);
    }

    /**
     * multiplies a given complex number with this object and returns
     * a new complex number
     * @return resulting complex number
     */
    public Complex times(Complex rhs) {
	return new Complex(re*rhs.re-im*rhs.im,re*rhs.im+im*rhs.re);
    }

    /**
     * specialized multiplication routine for large Braket objects where the complex
     * numbers are not instantiated before actually participating in an operation.
     * @see Braket
     */
    public Complex times_(Complex rhs) {
	if (rhs == null) return null;
	else return times(rhs);
    }

    /**
     * multilies a given double number with this object and returns
     * a new complex number
     * @return resulting complex number
     */
    public Complex times(double scalar) {
	return new Complex(re*scalar,im*scalar);
    }

    /**
     * divides this object by the given complex number. See Numerical Recipes.
     * @return resulting comlex number
     */
    public Complex divided(Complex rhs) {
	//  c1 = a + ib     c2 = c + id 
	//  see Numerical Recipes
	if (Math.abs(rhs.re) >= Math.abs(rhs.im)) {
	    double d_over_c = rhs.im/rhs.re;
	    double denom = rhs.re+rhs.im*d_over_c;
	    return new Complex((re+im*d_over_c)/denom, (im-re*d_over_c)/denom);
	}
	else {
	    double c_over_d = rhs.re/rhs.im;
	    double denom = rhs.re*c_over_d + rhs.im;
	    return new Complex((re*c_over_d+im)/denom, (im*(c_over_d)-re)/denom);
	}
    }

    /**
     * @return the complex conjugate of the complex number
     */
    public Complex conjugate() {
	return new Complex(re,(im == 0)? 0 : -im);
    }

    /**
     * @return the negative of the complex number
     */
    public Complex negative() {
	return new Complex( (re == 0) ? 0: -re,(im == 0)? 0 : -im);
    }

    // equality predicate

    /**
     * this method can determine whether a given MathObject m is equal to
     * the complex number.
     * @param m MathObject to be compared with
     * @return equal or not
     */
    public boolean equals(MathObject m) {
	if (m instanceof Complex) {
	    Complex rhs = (Complex)m;
	    if ((Math.abs(re-rhs.re) < eps) && (Math.abs(im - rhs.im) < eps))
		return true;
	}
	return false;
    }

    /**
     * sets the number of output digits
     * @see Complex#toString()
     */
    public static void setDigits(int newdigits) {
	digits = newdigits;
    }

    /**
     * @return the number of output digits
     * @see Complex#toString()
     */
    public static int getDigits() {
	return digits;
    }

    /**
     * sets the internal number of digits. Although the internal operations still
     * are done with machine precision, the number of internal digits affects the
     * output and the equality predicate.
     * @see Complex#equals(MathObject)
     */
    public static void setInternalDigits(int newInternalDigits) {
	internalDigits = newInternalDigits;
	eps = 0.5/Math.pow(10, newInternalDigits);
    }

    /**
     * @return the number of internal digits
     */
    public static int getInternalDigits() {
	return internalDigits;
    }

    /**
     * sets the output mode for complex numbers.
     * e.g. c = 0 + 1*i
     *     simple = true :  c = i
     *     simple = false:  c = 0 + 1i
     * @param newSimple true for simple mode
     */
    public static void setSimple(boolean newSimple) {
	simple = newSimple;
    }

    /**
     * parses a complex number from a given string. Returns the complex
     * number if possible or throws a NumberFormatException.
     * @param str string to parse
     * @return complex number
     */
    public static Complex parseComplex(String str) throws NumberFormatException {
	double re, im;
	int i, i_pos = 0;

	try {
	    str = str.trim();
	    i = str.indexOf("+",1);
	    if (i == -1) {
		i = str.indexOf("-",1);
		// new for numbers like 1E-7
		if (i > 0) {
		    if (str.charAt(i-1) == 'E') i = str.indexOf("-",i+1);
		}
	    }


	    // only real or imaginary part
	    if (i == -1) { 
		i_pos = str.indexOf("i");
		if (i_pos == -1) {
		    re = new Double(str).doubleValue(); 
		    im = 0; 
		}
		// special handling for "i" instead of "1i"
		else if (i_pos == 0) {
		    re = 0;
		    im = 1;
		    if (str.length() > 1) throw new NumberFormatException(str + "is not a complex number");
		}
		else if ((i_pos == 1) && (str.charAt(0) == '-')) { re = 0; im = -1; }
		else {
		    re = 0;
		    im = new Double(str.substring(0, i_pos)).doubleValue();
		}
	    }
	    // both parts
	    else {
		re = new Double(str.substring(0, i)).doubleValue();

		i_pos = str.indexOf("i");
		if (i == i_pos-1) im = new Double(str.substring(i,i_pos)+"1").doubleValue();
		else im = new Double(str.substring(i, i_pos)).doubleValue();
	    }
	    return new Complex(re, im);
	}
	catch (Exception e) { 
	    throw new NumberFormatException(str + " is not a complex number!");
	}

    }

    /**
     * returns the string representation of the complex number.
     * The string is parsable by the <tt> Parse</tt> object. 
     * @return string representation
     */
    public String toString() {
	String str = new String("");
	double reC = (Math.abs(re) < eps)? 0:re;
	double imC = (Math.abs(im) < eps)? 0:im;

	if (simple) {
	    if ((reC == 0) && (imC == 0))
		str = str.concat("0");
	    else if ((reC != 0) && (imC == 0)) 
		str = str.concat(Easy.format(reC, digits));
	    else if ((reC == 0) && (imC != 0)) {
		if (imC == 1) str = str.concat("i");
		else if (imC == -1) str = str.concat("-i");
		else str = str.concat(Easy.format(imC, digits)+"i");
	    }
	    else {
		str = str.concat(Easy.format(reC, digits));
		str = (imC >= 0) ? str.concat("+") : str;
		str = str.concat(Easy.format(imC, digits) + "i");
	    }

	}
	else {
	    str = str.concat(Easy.format(reC, digits));
	    str = (imC >= 0) ? str.concat("+") : str;
	    str = str.concat(Easy.format(imC, digits) + "i");
	}
	return str;
    }

    /**
     * returns the string representation of the complex number. The complex number are
     * printed in machine precision. The string is parsable by 
     * the <tt> Parse</tt> object. 
     * @return string representation
     */
    public String toParseableString() {
	String str = new String("");

	if (simple) {
	    if ((re == 0) && (im == 0))
		str = str.concat("0");
	    else if ((re != 0) && (im == 0)) 
		str = str.concat(""+re);
	    else if ((re == 0) && (im != 0)) {
		if (im == 1) str = str.concat("i");
		else if (im == -1) str = str.concat("-i");
		else str = str.concat(""+im+"i");
	    }
	    else {
		str = str.concat(""+re);
		str = (im >= 0) ? str.concat("+") : str;
		str = str.concat(""+im + "i");
	    }

	}
	else {
	    str = str.concat(""+re);
	    str = (im >= 0) ? str.concat("+") : str;
	    str = str.concat(""+im+ "i");
	}
	return str;
    }


}

