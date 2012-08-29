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
 * <p>
 * Class representing a "command" in the Mathlib language. This class is a special child
 * of the <tt>MathMap</tt> object since is it not a mathematical map, but rather
 * a system function like delete or register/unregister.</p>
 * The Command class allows in addition multiple arguments, but in this implementation
 * only up to two MathObjects. In addition to the value (the MathObject), the name
 * of a possibly involved variable usually should be given as well. 
 * The command class is parsed
 * by the <tt>Tokenizer</tt> class and executed by the <tt>Parse</tt> class.
 * @see Parse
 * @see Tokenizer
 * @see MathMap
 */
public class Command extends MathMap {
    private String command;

    /**
     * creates a new command. The commands are actually hard coded. Implemented commads
     * so far: register, unregister, delete
     * @see Command#apply(String, MathObject, String, MathObject)
     */
    public Command(String command) {
	this.command = command;
    }

    /**
     * apply implementation in accordance to the MathMap parent
     */
    public MathObject apply(MathObject o) {
	return apply(null, o, null, null);
    }

    /**
     * apply implementation for a one-argument command. Commands implemented so far:
     * <ul>
     * <li> delete(variable) - deletes a variable
     * <li> unregister(variable) - removes a variable from a category
     * </ul>
     * @param s1 name of the variable
     * @param o1 value of the variable
     */
    public MathObject apply(String s1, MathObject o1) {
	return apply(s1, o1, null, null);
    }

    /**
     * apply implementation for a two-argument command. Commands implemented so far:
     * <ul>
     * <li> register(category, variable) - registers a variable in a certain catgory
     * <li> gateproperty(gatename, variable) - adds/creates properties of a gate
     * </itemize>
     * @param s1 name of the first variale
     * @param o1 value of the first variable
     * @param s2 name of the second variable
     * @param o2 value of the second variable
     * @see Command#apply(String, MathObject)
     */
    public MathObject apply(String s1, MathObject o1, String s2, MathObject o2) {

	// register command: registers a variable in a subcategory
	if ("register".equals(command)) {
	    return register(s1, o1, s2);
	}
	// unregister command: unregisters a variable in a subcategory
	else if ("unregister".equals(command)) {
	    if (s2 == null && o2 == null) return unregister(s1);
	    else {
		LOG.LOG(0, command+ "() too many parameters!");
		return null;
	    }
	}
	// gateproperty command:
	else if ("gateproperty".equals(command)) {
	    return gateproperty(s1, o1, s2);
	}
	// delete command: deletes a variable
	else if ("delete".equals(command)) {
	    if (s2 == null && o2 == null) return delete(s1);
	    else { LOG.LOG(0, command + "() too many parameters!"); return null; }
	}
	LOG.LOG(0, command + ": no such function!");
	return null;

    }

    /**
     * registers a variable in a certain category as specified by s1. The category
     * is saved in the <tt>Mathlib</tt> object and need not exist beforehand. Categories
     * are used to group variabled together. One preset group is the category "system".
     * The jaQuzzi program uses a category "current circuit" in order to identify
     * the variables belonging to a certain circuit. The VarAuthority class can be
     * used to filter variables kept by a Mathlib object.
     * @see Command#unregister(String)
     * @see Mathlib
     * @see VarAuthority
     * @param s1 name of category variable
     * @param o1 StringArgument with category
     * @param s2 name of the variable to be registered
     */
    public MathObject register(String s1, MathObject o1, String s2) {
	if (!(o1 instanceof StringArgument)) {
	    LOG.LOG(0, command + " requires category as string argument! e.g. register(\"system\", ...)");
	    return null;
	}
	if ((s2 == null)) {
	    LOG.LOG(0, command + " requires variable name! e.g. register(..., s_x)");
	    return null;
	}

	MathObject o = Mathlib.getVar(s2);
	if (o == null) { 
	    LOG.LOG(0, command + " no variable "+ s2 + " found!"); 
	    return null;
	}

	// everything ok
	Parse.fireMathlibEvent(s2, o1.toString(), null, o, MathlibEvent.CHANGE_CATEGORY);
	return new StringArgument(s2 + " added to category "+o1.toString());
    }

    /**
     * creates/adds a <tt>GateProperty</tt> object which specifies certain optional 
     * parameters of a <tt>Gate</tt> object.
     * @see GateProperty
     * @param s1 name of category variable
     * @param o1 StringArgument with category
     * @param s2 name of the variable to be registered
     */
    public MathObject gateproperty(String s1, MathObject o1, String s2) {
	if (!(o1 instanceof StringArgument)) {
	    LOG.LOG(0, command + " requires gatename as string argument! e.g. gateproperty(\"gate0\", ...)");
	    return null;
	}
	if ((s2 == null)) {
	    LOG.LOG(0, command + " requires property name! e.g. gateproperty(..., sigma=123)");
	    return null;
	}

	MathObject o = Mathlib.getVar(s2);
	if (o == null) { 
	    LOG.LOG(0, command + " no variable "+ s2 + " found!"); 
	    return null;
	}

	// everything ok
	MathObject gp = Mathlib.getVar(o1.toString()+"_properties");

	if (gp == null) {
	    gp = new GateProperty(o1.toString());
	    String cat = (Command.mathlib == null)? null : Command.mathlib.getCategory(o1.toString());
	    ((GateProperty)gp).addProperty(s2, (Complex)o);
	    Parse.fireMathlibEvent(o1.toString()+"_properties", null, cat, gp, MathlibEvent.ADD);
	} else {
	    ((GateProperty)gp).addProperty(s2, (Complex)o);
	    Parse.fireMathlibEvent(o1.toString()+"_properties", null, null, gp, MathlibEvent.CHANGE);
	}
	return gp;
    }

    /**
     * unregisters a variable from a category. For the description of the category 
     * concept see Command->register.
     * @param s1 name of the variable to be unregistered.
     */
    public MathObject unregister(String s1) {
	if ((s1 == null)) {
	    LOG.LOG(0, command + " requires variable name! e.g. unregister(s_x)");
	    return null;
	}

	MathObject o = Mathlib.getVar(s1);
	if (o == null) { 
	    LOG.LOG(0, command + " no variable "+ s1 + " found!"); 
	    return null;
	}

	// everything ok
	Parse.fireMathlibEvent(s1, null, null, o, MathlibEvent.CHANGE_CATEGORY);
	return new StringArgument(s1 + " removed from category.");
    }

    /**
     * deletes a variable from the list of the Mathlib object.
     * @param s1 name of the variable
     */ 
    public MathObject delete(String s1) {
	if ((s1 == null)) {
	    LOG.LOG(0, command + " requires variable name! e.g. delete(s_x)");
	    return null;
	}

	MathObject o = Mathlib.getVar(s1);
	if (o == null) { 
	    LOG.LOG(0, command + " no variable "+ s1 + " found!"); 
	    return null;
	}

	// everything ok
	Parse.fireMathlibEvent(s1, null, null, o, MathlibEvent.REMOVE);
	return new StringArgument(s1 + " deleted.");
    }

    /**
     * returns the name of the command
     */
    public String toString() {
	return command;
    }

}
