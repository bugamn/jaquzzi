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
 * A MathlibEvent is fired whenever a variable is created, deleted or changes it's
 * value. With this mechanism all listeners are notified about a change in the
 * Mathlib scope. The event is usually fired by the <tt>Parse</tt> class, but exceptionally
 * as well in the jaQuzzi program.
 * @see MathlibEventListener
 */
public class MathlibEvent {
    /** variable added */
    public static int ADD = 1;
    /** variable removed */
    public static int REMOVE = 2;
    /** variable value change */
    public static int CHANGE = 3;
    /** variable name change */
    public static int CHANGE_NAME = 4;
    /** variable category change */
    public static int CHANGE_CATEGORY = 5;

    private Object source;
    private String objectName;
    private String newName;
    private MathObject mathObject;
    private int action;
    private String category;

    /**
     * creates a MathlibEvent
     * @param source the source object causing the event
     * @param objectName the name of the affected MathObject
     * @param newName a possible new name for the MathObject
     * @param category the category of the mathObject
     * @param mathObject the value of the MathObject
     * @param action the action code
     */
    public MathlibEvent(Object source, String objectName, String newName, String category, MathObject mathObject, int action) {
	this.source = source;
	this.mathObject = mathObject;
	this.objectName = objectName;
	this.action = action;
	this.newName = newName;
	this.category = category;
    }

    /**
     * creates a MathlibEvent
     * @param objectName the name of the affected MathObject
     * @param newName a possible new name for the MathObject
     * @param category the category of the mathObject
     * @param mathObject the value of the MathObject
     * @param action the action code
     */
    public MathlibEvent(String objectName, String newName, String category, MathObject mathObject, int action) {
	this(null, objectName, newName, category, mathObject, action);
    }

    /**
     * creates a MathlibEvent
     * @param objectName the name of the affected MathObject
     * @param mathObject the value of the MathObject
     * @param action the action code
     */
    public MathlibEvent(String objectName, MathObject mathObject, int action) {
	this(null, objectName, null, null, mathObject, action);
    }

    /**
     * creates a rename MathlibEvent
     * @param objectName the name of the affected MathObject
     * @param newName a possible new name for the MathObject
     */
    public MathlibEvent(String oldName, String newName) {
	this(null, oldName, newName, null, null, CHANGE_NAME);
    }

    /**
     * returns the origin of the MathlibEvent
     */
    public Object getSource() {
	return source;
    }

    /**
     * returns the action code of the mathlib event
     */
    public int getAction() {
	return action;
    }

    /**
     * return the newName argument
     */
    public String getNewName() {
	return newName;
    }

    /**
     * returns the name of the affected MathObject
     */
    public String getObjectName() { 
	return objectName;
    }

    /**
     * returns the value of the affected MathObject
     */
    public MathObject getMathObject() {
	return mathObject;
    }

    /**
     * returns the category of the MathObject
     */
    public String getCategory() {
	return category;
    }
}
