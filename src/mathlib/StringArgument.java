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
 * Class representing a string in the MathObject structure. It can be parsed
 * with the syntax "abc".
 * @see Tokenizer
 * @see Parse
 */
public class StringArgument extends Argument {
    /** holds teh string */
    protected String string;

    /**
     * creates a string argument from a given string
     */
    public StringArgument(String string) {
	this.string = string;
    }

    /**
     * clone constructor
     */
    public StringArgument(MathObject o) {
	if (o instanceof StringArgument) {
	    this.string = ((StringArgument)o).toString();
	}
    }

    /**
     * clone method
     */
    public Object clone() {
	return new StringArgument(this);
    }


    /**
     * returns the StringArgument as a string, i.e. abc
     */
    public String toString() {
	return string;
    }

    /**
     * returns the StringArgument as a parsable string, i.e. "abc"
     */
    public String toParseableString() {
	return "\""+ toString() + "\"";
    }

    /**
     * equality predicate
     */
    public boolean equals(MathObject o1) {
	try {
	    return (string.equals(((StringArgument)o1).string) ? true: false);
	} catch (Exception e) { return false; }
    }
}
