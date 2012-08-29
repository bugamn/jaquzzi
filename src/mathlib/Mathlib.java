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
 * The Mathlib class needs to be instantiated in order to use all mathlib package
 * features as variables, functions etc. The class initializes mathematical
 * constants and registers functions coming along in the mathpackage.
 *
 * It implements a couple of static functions for the access to those variables
 * and functions, which are used by the <tt>Parse</tt> object.
 *
 */
public class Mathlib implements MathlibEventListener {

    /**
     * This variable controls the verboseness of the mathlib package.
     * @see LOG
     */
    static int DEBUGLEVEL = 0;

    /**
     * This variable controls the amount of digits shown.
     * @see Easy
     */
    static int DIGITS = 3;

    /**
     * This variable controls the output of the class <tt>Complex</tt>. If it is set
     * to 0, complex numbers are printed as x + yi even if x = 0 or y = 0. If it is
     * set to a value >= 1, numbers are printed in a short format, i.e. 0+1i = i.
     * The variable has precedence over the <tt> DIGITS</tt> variable
     * @see Mathlib#DIGITS
     * @see Complex
     */
    static int SIMPLE = 1;


    /**
     * This variable holds the defined variables
     */
    protected static Hashtable vars = null;

    /**
     * this hashtable provides categories for variables
     */
    protected static Hashtable categories = null;

    /**
     * this vector holds computation event listeners
     */
    protected static Vector listeners = null;

    /**
     * This variable holds the defined functions
     */
    protected static Hashtable functions = null;
    private Parse parser = null;

    protected static Random random;

    /**
     * This constructor initializes the hashtables for variables and functions
     * that might be used in algebraic expressions. It defines certain variables
     * and registers available functions. It as well instatiates a parser object.
     * If this constructor is not called, the parsing part of the mathlib 
     * package cannot be used.
     */
    public Mathlib() {
	random = new Random();
	vars = new Hashtable();
	categories = new Hashtable();
	functions = new Hashtable();
	parser = new Parse(this);
	MathObject.mathlib = this;

	// system
	parser.fireMathlibEvent("DEBUGLEVEL", null, "system", new Complex(DEBUGLEVEL), MathlibEvent.ADD);
	parser.fireMathlibEvent("DIGITS", null, "system", new Complex(DIGITS), MathlibEvent.ADD);
	parser.fireMathlibEvent("SIMPLE", null, "system", new Complex(SIMPLE), MathlibEvent.ADD);
	//	try{
	    // presets
	    putVar("pi", new Complex(Math.PI));
	    putVar("sqrt2", new Complex(Math.sqrt(2)));
	    putVar("NOT", Matrix.parseMatrix("[0 1, 1 0]"));
	    putVar("sigma_x", Matrix.parseMatrix("[0 1, 1 0]"));
	    putVar("sigma_y", Matrix.parseMatrix("[0 -i, i 0]"));
	    putVar("sigma_z", Matrix.parseMatrix("[1 0, 0 -1]"));
	    putVar("H", Parse.parseExpression("1/sqrt2*[1 1, 1 -1]"));

	    // functions
	    functions.put("one", new OneMap());
	    functions.put("exp", new ExpMap());
	    functions.put("sin", new SinMap());
	    functions.put("cos", new CosMap());
	    functions.put("sqrt", new SqrtMap());
	    functions.put("norm", new Norm());
	    functions.put("Rx", new RxMap());
	    functions.put("Ry", new RyMap());
	    functions.put("Rz", new RzMap());
	    functions.put("Ph", new PhMap());
	    //	    functions.put("QFT", new QFTMap());
	    //	    functions.put("trace", new TraceMap());
	    //	    functions.put("traceB", new TraceBMap());
	    //	    functions.put("schmidtNo", new SchmidtNoMap());
	    //	    functions.put("schmidt", new SchmidtDecomp());
	    functions.put("measure", new Measurement());
	    functions.put("dump", new DumpMap());

	    //commands
	    functions.put("register", new Command("register"));
	    functions.put("unregister", new Command("unregister"));
	    functions.put("delete", new Command("delete"));
	    functions.put("gateproperty", new Command("gateproperty"));
	    /*
	} Acatch (Exception e) {
	    LOG.LOG(0, "serious error while initializing preset variables!");
	}
	    */
    }

    /**
     * This function is called for example by the class <tt> Parse</tt> in order
     * to register a new variable after recognizing an assignment expression.
     * It throws an exception, if the <tt>Mathlib</tt> class has not been instantiated.
     * @param key name of the variable
     * @param m name of the MathObject
     * @see Parse
     * @see MathObject
     */
    public static void putVar(String key, MathObject m) throws MissingResourceException {
	if (vars == null) 
	    throw new MissingResourceException("mathlib not initialized!", "Mathlib","");
	vars.remove(key);
	vars.put(key, m);
    }

    /**
     * This function is removes a variable from the variable list.
     * It throws an exception, if the <tt>Mathlib</tt> class has not been instantiated.
     * @param key name of the variable
     * @see Mathlib#removeVariable(MathlibEvent)
     * @see MathObject
     */
    public static void removeVar(String key) throws MissingResourceException {
	if (vars == null) 
	    throw new MissingResourceException("mathlib not initialized!", "Mathlib","");
	vars.remove(key);
    }

    /**
     * This function is called for example by the class <tt> Parse</tt> in order
     * to check expressions for beeing a variable and obtaining its value.
     * It throws an exception, if the <tt>Mathlib</tt> class has not been instantiated.
     * @param key name of the variable
     * @see Parse
     * @return MathObject 
     */
    public static MathObject getVar(String key) throws MissingResourceException {
	if (vars == null) 
	    throw new MissingResourceException("mathlib not initialized!", "Mathlib","");
	return (MathObject) vars.get(key);
    }

    /**
     * returns the category of a variable.
     * It throws an exception, if the <tt>Mathlib</tt> class has not been instantiated.
     * @param key name of the variable
     * @see Parse
     * @return MathObject 
     */
    public static String getCategory(String key) throws MissingResourceException {
	if (categories == null) 
	    throw new MissingResourceException("mathlib not initialized!", "Mathlib","");
	Object o = categories.get(key);
	return ((o == null) ? null : (String) categories.get(key));
    }


    /**
     * This function is called for example by the class <tt> Parse</tt> in order
     * to check expressions for beeing a function and obtaining its representation.
     * It throws an exception, if the <tt>Mathlib</tt> class has not been instantiated.
     * @param key name of the variable
     * @see Parse
     * @see MathMap
     * @return MathMap
     */
    public static MathMap getFunction(String key) throws MissingResourceException {
	if (functions == null) 
	    throw new MissingResourceException("mathlib not initialized!", "Mathlib","");
	return (MathMap) functions.get(key);
    }

    /**
     * This function wraps the evaluateExpression function of the <tt>Parse</tt> class.
     * It takes care of system variables and updates them.
     * @see Parse#evaluateExpression
     * @param str String to parse and evaluate
     * @return returns the name (key) of the variable to which the expression was assigned.
     */
    public String evaluateExpression(String str) throws IllegalArgumentException {
        String resultkey = parser.evaluateExpression(str);
	updateSystem();
	return resultkey;
    }


    /**
     * returns a Enumeration object of all variable names
     */
    public Enumeration getVars() {
	return vars.keys();
    }

    /**
     * returns a Enumeration object of all function names
     */
    public Enumeration getFunctions() {
	return functions.keys();
    }

    /**
     * returns the random object of the mathlib class
     */
    public static Random getRandom() {
	return random;
    }

    /**
     * This function lists all registered variables of the Mathlib object.
     */

    public void listVars() {
	Enumeration variables = vars.keys();
	System.out.println("List of variables:");
	while (variables.hasMoreElements()) {
	    System.out.print(variables.nextElement()+ ", ");
	}
	System.out.println(";");
    }

    /**
     * This function lists the system variables with their corresponding values.
     */
    public void listSystem() {
	System.out.println("System properties:");
	System.out.println("DEBUGLEVEL = " + DEBUGLEVEL);
	System.out.println("DIGITS     = " + DIGITS);
	System.out.println("SIMPLE     = " + SIMPLE);
    }

    /**
     * This function looks for system variables in the vars hashtable and assigns
     * their value to the corresponding internal system variables.
     */
    public void updateSystem() {
	try {
	    DEBUGLEVEL= new Double(((Complex)getVar("DEBUGLEVEL")).re()).intValue();
	    DIGITS = new Double(((Complex)getVar("DIGITS")).re()).intValue();
	    SIMPLE = new Double(((Complex)getVar("SIMPLE")).re()).intValue();
	} 
	catch(Exception e) { 
	    DEBUGLEVEL = 100; 
	    DIGITS = 3;
	    SIMPLE = 1;
	    LOG.LOG(0, "[updateSystem] variable not found!");
	}
	LOG.setDebuglevel(DEBUGLEVEL);
	Complex.setDigits(DIGITS);
	Complex.setSimple((SIMPLE > 0) ? true : false);
    }

    /**
     * required by the MathlibEventListener interface. It reacts to a MathlibEvent.
     * @param MathlibEvent event
     */
    public void removeVariable(MathlibEvent e) {
	// only remove if not system!
	if ("system".equals(categories.get(e.getObjectName())) == false) {
	    removeVar(e.getObjectName());
	    categories.remove(e.getObjectName());
	}
    }

    /**
     * required by the MathlibEventListener interface. It reacts to a MathlibEvent.
     * @param MathlibEvent event
     */
    public void addVariable(MathlibEvent e) {
	if (e.getMathObject() != null) {
	    putVar(e.getObjectName(), e.getMathObject());
	    if (e.getCategory() != null) categories.put(e.getObjectName(), e.getCategory());
	}
    }

    /**
     * required by the MathlibEventListener interface. It reacts to a MathlibEvent.
     * @param MathlibEvent event
     */
    public void changeVariable(MathlibEvent e) {
	if (e.getAction() == MathlibEvent.CHANGE) {
	    putVar(e.getObjectName(), e.getMathObject());
	}
	else if (e.getAction() == MathlibEvent.CHANGE_CATEGORY) {
	    if ("system".equals(e.getCategory()) == false) {
		categories.remove(e.getObjectName());
		if (e.getNewName() != null) categories.put(e.getObjectName(),e.getNewName());
	    }
	}
	else if (e.getAction() == MathlibEvent.CHANGE_NAME) {
	    if ("system".equals(e.getCategory()) == false) {
		MathObject m = (MathObject)vars.remove(e.getObjectName());
		if (m != null && e.getNewName() != null) {
		    putVar(e.getNewName(), m); 

		    categories.remove(e.getObjectName());
		    if (e.getCategory() != null) categories.put(e.getNewName(), e.getCategory());
		}
	    }
	}
    }

    /**
     * wrapper method...
     * @see Parse#addMathlibEventListener(MathlibEventListener)
     */
    public static void addMathlibEventListener(MathlibEventListener listener) {
	Parse.addMathlibEventListener(listener);
    }

    /**
     * wrapper method...
     * @see Parse#addMathlibEventListener(MathlibEventListener)
     */
    public static void removeMathlibEventListener(MathlibEventListener listener) {
	Parse.removeMathlibEventListener(listener);
    }

    public void setCategory(String category) {
	parser.setCategory(category);
    }


    /**
     * registers a ComputationEventListener
     * @param m listener object
     */
    public static synchronized void addComputationEventListener(ComputationEventListener m) {
	if (m == null) return;

	if (listeners == null) listeners = new Vector();
        listeners.add(m);
    }

    /**
     * removes a ComputationEventListener
     */
    public static synchronized void removeComputationEventListener(ComputationEventListener m){
	if (m == null) return;
	listeners.remove(m);
    }

    /**
     * is called internally by fireMathlibEvent in order to dispatch the event
     * @param e ComputationEvent to dispatch
     */
    protected static synchronized void processEvent(ComputationEvent event) {
	if (listeners != null) {
	    for (int i = 0; i < listeners.size(); i++) {
		((ComputationEventListener)listeners.get(i)).computationEvent(event);
	    }
	}
    }

    /**
     * creates a ComputationEvent and pumps it to processEvent
     * @param operator the operator causing the event
     * @param objectName name of the variable computed
     * @param currentStep current step of computation
     * @param maxStep maximum step of computation
     * @param the action to perform
     */
    public static void fireComputationEvent(MathObject operator, MathObject var, int currentStep, int maxStep, int action) {
	processEvent(new ComputationEvent(operator, var, currentStep, maxStep, action));
    }


}
