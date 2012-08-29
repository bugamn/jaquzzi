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

package qsymbol2;

import java.util.*;

/**
 * this class is a helper class to translate the naming convention of the Gate
 * variables into a nested structure of GateContainers. For example a gate named
 * topgroup.group.gate will translate into three gate containers named gate, nested
 * into the one named group, nested into the one called topgroup. In order to
 * facilitate grouping and ungrouping this class tokenizes the name of a variable and
 * strips away for example the leading (toplevel) part.
 * @see GateContainer
 * @see edu.buffalo.fs7.mathlib.Gate
 */
public class NameStripper {

    /** holds the parts of the name */
    protected Vector tokens;

    /** 
     * create a NameStripper object for a given string. The separator is the
     * charakter '.'
     */
    public NameStripper(String name) {
	StringTokenizer st = new StringTokenizer(name, ".\n");
	tokens = new Vector(st.countTokens());

	// copy into vector
	while (st.hasMoreTokens()) tokens.add(st.nextToken());
    }

    /**
     * removes the top level group name
     */
    public void stripLeadingGroup() {
	removeGroup(0);
    }

    /**
     * returns the full name
     */
    public String getFullName() {
	String str = new String("");
	for (int i = 0; i < getGroupCount(); i++) {
	    str = str.concat(getGroup(i) + ".");
	}
	str = str.concat(getName());
	return str;
    }

    /**
     * returns the lowest order name
     */
    public String getName() {
	if (tokens.isEmpty() == true) return null;
	else return ((String)tokens.elementAt(tokens.size()-1));
    }

    /**
     * counts the number of group levels.
     * @return if the name is containing of only one part it returns 0
     */
    public int getGroupCount() {
	return (tokens.isEmpty())? 0:(tokens.size()-1);
    }

    /**
     * returns a specified part of the name. 0 is the top level.
     */
    public String getGroup(int i) {
	return (i < 0 || i >= tokens.size())? null: ((String)tokens.elementAt(i));
    }

    /**
     * returns the parts of the name in order from top level to lowest level
     */
    public Enumeration getGroups() {
	return new Enumeration() {
		int group = 0;
		public boolean hasMoreElements() {
		    return (group < getGroupCount());
		}
		public Object nextElement() {
		    return getGroup(group++);
		}
	    };
    }

    /**
     * removes a certain part of the name. 0 is the top level.
     */
    public void removeGroup(int i) {
	if (i >=0 && i < getGroupCount()) {
	    tokens.remove(i);
	}
    }

    /**
     * outputs all groups separated by tabs
     */
    public String toString() {
	String str = new String("");
	Enumeration e = tokens.elements();
	while (e.hasMoreElements()) {
	    str = str.concat((String)e.nextElement() + "\t");
	}
	return str;
    }

}
