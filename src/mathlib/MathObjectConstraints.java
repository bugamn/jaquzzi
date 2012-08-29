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
 * this class provides constraints for a category similar to GridBagConstraints
 * in the GridBagLayout. It is used in connection with the VarAuthority class to
 * group variables into specialized groups.
 * @see VarAuthority
 * @see Mathlib
 */
public class MathObjectConstraints {
    /** class type constraint */
    public Class mathClass;
    /** category constraint */
    public String subCategory;
    /** n extension constraint */
    public int n;
    /** m extension constraint (might not apply everywhere */
    public int m;
    /** dimension constraint */
    public int dimension;
    /** expression constraint */
    public String expression;
    
    /**
     * creates an empty constraints object
     */
    public MathObjectConstraints() {
	this(null, null, -1, -1, -1, null);
    }

    /**
     * creates a MathObjectConstraints object
     * @param mathClass class to filter for or null
     * @param subCategory category to filter for or null
     * @param n n extension to filter for or -1
     * @param m m extension to filter for or -1
     * @param dimension dimension filter or -1
     * @param expression conditional expression, like self*self'"==one(2)
     */
    public MathObjectConstraints (Class mathClass, String subCategory, int n, int m, int dimension, String expression) {
	this.mathClass = mathClass;
	this.subCategory = subCategory;
	this.n = n;
	this.m = m;
	this.dimension = dimension;
	this.expression = expression;
    }

    /**
     * clones a MathObjectConstraints object
     */
    public MathObjectConstraints(MathObjectConstraints c) {
	this.mathClass = c.mathClass;
	this.subCategory = c.subCategory;
	this.n = c.n;
	this.m = c.m;
	this.dimension = c.dimension;
	this.expression = c.expression;
    }
}
