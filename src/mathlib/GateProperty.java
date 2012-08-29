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
 * <p>Class representing properties of a gate object, like repetitions, parameters for
 * error behavior of gates, parameters for decoherence simulation. The idea is to 
 * provide optional additional information for the processing of gates. This class
 * is used to encode gate specific information of circuits into files and their 
 * reconstruction.</p>
 * Properties are added with the command 'gateproperty("gatename", a=123)' and the 
 * property object can be handled within the MathObject structure (e.g. save like
 * a variable). The gateproperty object will be called "gatename_property".
 * @see Gate
 */
public class GateProperty extends Argument{
    protected Hashtable properties;
    private String gate;

    /**
     * creates a new property object
     */
    public GateProperty(String gate) {
	this.gate = gate;
	properties = new Hashtable();
    }

    /**
     * clone constructor
     */
    public GateProperty(MathObject o) {
	if (o instanceof GateProperty) {
	    this.gate = ((GateProperty)o).getGate();
	    this.properties = new Hashtable();
	    Enumeration e = ((GateProperty)o).getPropertyKeys();
	    String key;
	    while (e.hasMoreElements()) {
		key = (String)e.nextElement();
		properties.put(key, ((GateProperty)o).getProperty(key));
	    }
	}
    }

    /**
     * clones the GateProperty object
     */
    public Object clone() {
	return new GateProperty(this);
    }

    /**
     * adds a property to the property object. If the property is already assigned, the
     * old value is overwritten.
     * @param property the name of the property
     * @param value the complex value of the property
     */
    public void addProperty(String property, Argument value) {
	properties.put(property, value);
    }

    /**
     * removes a property of the property object. If the property is existing, the 
     * property is returned otherwise null.
     */
    public Argument removeProperty(String property) {
	Object ret = properties.remove(property);
	return (ret == null)? null : (Argument) ret;
    }

    /**
     * returns the complex value of a property specified by the string. If the property
     * is not available, null is returned.
     * @return value of property or null
     */ 
    public Argument getProperty(String property) {
	return (properties.containsKey(property))? (Argument)properties.get(property):null;
    }

    /**
     * equality predicate
     */
    public boolean equals(MathObject o) {
	if (o instanceof GateProperty) {
	    boolean veto = false;
	    Enumeration e = getPropertyKeys();
	    Argument property;
	    String propName;
	    while (e.hasMoreElements() && veto == false) {
		propName = (String)e.nextElement();
		property = getProperty(propName);
		if (!(((GateProperty)o).properties.contains(propName) && ((GateProperty)o).getProperty(propName).equals(property))) {
		    veto = true;
		}
	    }
	    e = ((GateProperty)o).getPropertyKeys();
	    while (e.hasMoreElements() && veto == false) {
		propName = (String)e.nextElement();
		property = ((GateProperty)o).getProperty(propName);
		if (!(properties.contains(propName) && getProperty(propName).equals(property))) {
		    veto = true;
		}
	    }
	    return !veto;
	}
	return false;
    }

    /**
     * returns the contained properties
     */
    public String toString() {
	String str = new String("property object:\n");
	Enumeration e = properties.keys();
	String propertyName;
	Argument property;
	while (e.hasMoreElements()) {
	    propertyName = (String)e.nextElement();
	    property = getProperty(propertyName);
	    str = str.concat(propertyName +":\t" + property.toString()+"\n");
	}
	str = str.concat("gate: " + getGate()+"\n");
	return str;
    }

    /**
     * returns a parsable sequence of commands representing this property object
     */
    public String toParseableString() {
	String str = new String("");
	Enumeration e = properties.keys();
	String propertyName;
	Argument property;
	while (e.hasMoreElements()) {
	    propertyName = (String)e.nextElement();
	    property = getProperty(propertyName);
	    str = str.concat("gateproperty(\""+getGate()+"\","+propertyName +"=" + property.toParseableString()+")\n");
	}
	return str;
    }

    /**
     * sets the gate to which this property object belongs
     */
    public void setGate(String gate) {
	this.gate = gate;
    }

    /**
     * returns the gate to which this property object belongs.
     */
    public String getGate() {
	return gate;
    }

    /**
     * returns the name of all properties as an enumeration
     */
    public Enumeration getPropertyKeys() {
	return properties.keys();
    }

}
