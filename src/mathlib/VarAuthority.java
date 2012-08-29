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

import java.util.*;


/**
 * Class providing a filter and grouping mechanism for variables kept by a Mathlib
 * object. It can for example be used to obtain all variables of a certain type, e.g.
 * gates. But it also provides more complex filter rules, such as dimension constraints,
 * or boolean expressions. For details see MathObjectConstraints.
 *
 * @see MathObjectConstraints
 * @see Mathlib
 */
public class VarAuthority {

    /** holds a reference to the Mathlib object */
    protected Mathlib mathlib;
    /** holds registered categories */
    protected Hashtable categories;
    /** holds references to variables already sorted into a special group (exclusive mode)*/
    protected Vector varsAssigned;
    /** exclusive mode */
    protected boolean exclusiveMode;
    /** keeps record on priority of groups (first come first serve) */
    protected Vector priority;

    /**
     * creates a VarAuthority object for a certain Mathlib object. The exclusive mode allows
     * variables to be in only one category (membership decided by priority of groups).
     * @param mathlib Mathlib object which keeps the variables
     * @param exclusiveMode multiple group membership
     */
    public VarAuthority(Mathlib mathlib, boolean exclusiveMode) {
	this.mathlib = mathlib;
	this.exclusiveMode = exclusiveMode;
	categories = new Hashtable();
	priority = new Vector();
	varsAssigned = new Vector();
    }	

    /**
     * returns a enumeration of variable names which match the given category.
     * @param category category name
     */
    public Enumeration getElementsInCategory(String category) throws IllegalArgumentException {
	if (categories.containsKey(category)) {
	    String variable;
	    String subCategory;
	    Enumeration vars = mathlib.getVars();
	    final Vector elements = new Vector();

	    while (vars.hasMoreElements()) {
		variable = (String)vars.nextElement();
		subCategory = mathlib.getCategory(variable);
		if (!(exclusiveMode && varsAssigned.contains(variable)) && isObjectInCategory(variable, subCategory, category)) {
		    elements.add(variable);
		    if (exclusiveMode) varsAssigned.add(variable);
		} 

	    }
	    return new Enumeration() {
		    int index = 0;
		    public boolean hasMoreElements() { return index < elements.size();}
		    public Object nextElement() { return elements.elementAt(index++);}
		};

	}
	else throw new IllegalArgumentException("no such category!");
    }


    /**
     * checks whether the given object (given by varibale & subCategory) matches
     * the constraints of category
     * @param variable name of the MathObject
     * @param subCategory subcategory of the MathObject
     * @param category category to check against
     * @return true if object fits into the category
     */
    public boolean isObjectInCategory(String var, String subCategory, String category) {
	MathObjectConstraints c;
	boolean veto = false;
	String expression = null;
	MathObject variable = Mathlib.getVar(var);

	c = (MathObjectConstraints)categories.get(category);
	veto = false; // veto by constraints

	// check for class
	if (c.mathClass != null && c.mathClass.isInstance(variable) == false)  veto = true;
	else {
	    expression = c.expression;
	    if (expression != null)
		expression = Parse.replaceSubstring(expression, "self", var);

	    if (c.subCategory != null && c.subCategory.equals(subCategory) == false)
		veto = true;

	    if (variable instanceof Vect) {
		veto = veto | (c.dimension != -1 && c.dimension != ((Vect)variable).dimension);
		try {
		    veto = veto | (expression != null &&(((Argument)Parse.parseExpression(expression)).equals(new Complex(1)) == false));
		} catch(Exception e) { veto = true; }
	    }
	    else if (variable instanceof Braket) {
		veto = (c.n != -1 && c.n != ((Braket)variable).n);
		veto = veto | (c.dimension != -1 && c.dimension != ((Braket)variable).dimension);
		try {
		    veto = veto | (expression != null &&(((Argument)Parse.parseExpression(expression)).equals(new Complex(1)) == false));
		} catch(Exception e) { veto = true; }
	    }
	    else if (variable instanceof Matrix) {
		veto = veto | (c.n != -1 && c.n != ((Matrix)variable).n());
		veto = veto | (c.m != -1 && c.m != ((Matrix)variable).m());
		try {
		    veto = veto | (expression != null &&(((Argument)Parse.parseExpression(expression)).equals(new Complex(1)) == false));
		} catch(Exception e) { veto = true; }
	    }
	    else if (variable instanceof Gate) {
		veto = veto | (c.n != -1 && c.n != ((Gate)variable).n);
		try {
		    veto = veto | (expression != null &&(((Argument)Parse.parseExpression(expression)).equals(new Complex(1)) == false));
		} catch(Exception e) { veto = true; }
		    
	    }
	}
	if (veto) return false;
	else return true;
    }


    /**
     * returns the first matching category for the given variable.
     * @return category name or null if no match
     */
    public String whichCategory(String variable, String subCategory) {
	Enumeration enum = priority.elements();
	String categoryName = null;

	while (enum.hasMoreElements()) {
	    categoryName = (String) enum.nextElement();

	    // ok...insert!
	    if (isObjectInCategory(variable, subCategory, categoryName)) {
		return categoryName;
	    }
	}
	return null;
    }



    /**
     * returns the registered categories
     */
    public Enumeration getCategories() { 
	return categories.elements();
    }

    /**
     * registers a category
     */
    public void registerCategory(String category, MathObjectConstraints constraints) {
	priority.add(category);
	categories.put(category, new MathObjectConstraints(constraints));
    }

    /**
     * removes a category
     */
    public void unregisterCategory(String category) {
	priority.remove(category);
	categories.remove(category);
    }
}
