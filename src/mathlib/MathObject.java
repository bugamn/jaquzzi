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
 * Generic class for mathematical entities in the mathlib package.
 * Arguments like complex numbers as well as operators are derived from this 
 * object. It is intensely used by the <tt>Parse</tt> and the <tt>Tokenizer</tt>
 * objects.
 * @see Parse
 * @see Tokenizer
 * @see Complex
 */
public class MathObject implements Cloneable {

    /**
     * holds a reference to an instantiated <tt>Mathlib</tt> object.
     */
    public static Mathlib mathlib = null;

    /**
     * generic constructor
     */
    public MathObject() {
    }

    /**
     * clone instructor
     */
    public MathObject(MathObject o) {
    }

    /**
     * clone method
     */
    public Object clone() {
	return new MathObject(this);
    }

    /**
     * returns a string representation of the object (not necessarily parseable).
     * @see MathObject#toParseableString
     */
    public String toString() {
	return new String("");
    }

    /**
     * returns a parseable representation of the object
     * @see MathObject#toString
     */
    public String toParseableString() {
	return toString();
    }
}

