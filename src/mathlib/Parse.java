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
 * class providing methods to parse an arithmetic expression. A string with an arithmetic expression
 * is first preprocessed to allow certain short notations such like |01><01| for an outer product,
 * then then string is divided into MathObject tokens by the <tt>Tokenizer</tt> class. The parse class
 * finally evaluates the expression and returns the result of the expression or throws an exception
 * in case of an error.
 * <p>Since this class is the central class for evalutating an expression it also provides the mathlib
 * event handling. Everytime there is for example an assignment in an expression and the expression
 * was completely evaluated, a MathlibEvent is fired to notify listeners about the newly assigned
 * variable. The MathlibEvent mechnism is very similar to the event model implemented in Java itself.
 * Potential listener classes need to implement the MathlibEventListener interface and register themselves
 * here in the Parse object.</p>
 * <p>For this class to have all features, a Mathlib object must be instantiated. Although this
 * class provides static expression evalutation methods, it is strongly recommended to provide a mathlib
 * object in order to use the full functinality (the Mathlib class provides the variable management). </p>
 * <p>MathlibEvent listeners are for example the classes Mathlib, QVarTree, GateTable.</p>
 * @see Tokenizer
 * @see Mathlib
 * @see MathlibEvent
 * @see MathlibEventListener
 */
public class Parse {
    /** still in use ? */
    public static boolean echoAnswer = true;
    /** holds the MathlibEventListeners */
    protected static Vector listeners = null;
    /** holds the Mathlib object */
    protected Mathlib mathlib;
    protected static String category = null;

    /** 
     * creates a Parse object associated with a certain Mathlib object */
    public Parse(Mathlib mathlib) {
	this.mathlib = mathlib;

	listeners = new Vector();
	addMathlibEventListener(mathlib);
    }

    /**
     * registers a MathlibEventListener
     * @param m listener object
     */
    public static synchronized void addMathlibEventListener(MathlibEventListener m) {
	if (m == null) return;
        listeners.add(m);
    }

    /**
     * removes a MathlibEventListener
     */
    public static synchronized void removeMathlibEventListener(MathlibEventListener m){
	if (m == null) return;
	listeners.remove(m);
    }

    /**
     * is called internally by fireMathlibEvent in order to dispatch the event
     * @param e MathlibEvent to dispatch
     */
    protected static synchronized void processEvent(MathlibEvent event) {
	if (event.getAction() == MathlibEvent.ADD && Mathlib.getVar(event.getObjectName())!= null) {
	    LOG.LOG(0, "variable '"+event.getObjectName()+"' already existing. No action performed."); 
	    return;
	}

	// add category to the variable
	MathlibEvent e;
	if (event.getCategory() == null) 
	    e = new MathlibEvent(event.getSource(), event.getObjectName(), event.getNewName(), Mathlib.getCategory(event.getObjectName()), event.getMathObject(), event.getAction());
	else e = event;

	// debug
	LOG.LOG(1, "\nMathlibEvent: " + e.getObjectName());
	LOG.LOG(1, "action: " + e.getAction());
	if (e.getNewName() != null) LOG.LOG(1, "newName: " + e.getNewName());
	if (e.getCategory() != null) LOG.LOG(1, "category: " + e.getCategory());
	LOG.LOG(1, "mathObject: " + ((e.getMathObject() == null) ? "null": e.getMathObject().toString()));

	
	if (listeners != null) {
	    if (e.getAction() == MathlibEvent.ADD) {
		for (int i = 0; i < listeners.size(); i++) {
		    ((MathlibEventListener)listeners.get(i)).addVariable(e);
		}
	    }
	    else if (e.getAction() == MathlibEvent.REMOVE) {
		for (int i = 0; i < listeners.size(); i++) {
		    ((MathlibEventListener)listeners.get(i)).removeVariable(e);
		}
	    }
	    else if (e.getAction() == MathlibEvent.CHANGE || 
		     e.getAction() == MathlibEvent.CHANGE_NAME ||
		     e.getAction() == MathlibEvent.CHANGE_CATEGORY) {
		for (int i = 0; i < listeners.size(); i++) {
		    ((MathlibEventListener)listeners.get(i)).changeVariable(e);
		}
	    }
	}
    }

    /**
     * creates a MathlibEvent and pumps it to processEvent
     * @param objectName name of the mathobject
     * @param mathObject the actual mathobject
     * @param the action to perform
     */
    public static void fireMathlibEvent(String objectName, MathObject mathObject, int action) {
	processEvent(new MathlibEvent(objectName, mathObject, action));
    }

    /**
     * creates a MathlibEvent and pumps it to processEvent
     * @param objectName name of the mathobject
     * @param newName  new name of the mathobject in case of a CHANGE_NAM event
     * @param category logical category of the mathobject
     * @param mathObject the actual mathobject
     * @param the action to perform
     */
    public static void fireMathlibEvent(String objectName, String newName, String category, MathObject mathObject, int action) {
	processEvent(new MathlibEvent(objectName, newName, category, mathObject, action));
    }

    /**
     * creates a MathlibEvent and pumps it to processEvent
     * @param objectName name of the mathobject
     * @param newName new name of the mathobject
     * @param the action to perform
     */
    public static void fireMathlibEvent(String objectName, String newName) {
	processEvent(new MathlibEvent(objectName, newName));
    }

    /**
     * set a preset category in order to let all MathlibEvents generated by the parse methods belong to this
     * preset category (not used anymore)
     */
    public void setCategory(String newCategory) {
	category = newCategory;
    }


    /**
     * static method which can parse an expression without using variable and without
     * any assignments.
     * @param str string to parse
     * @return the mathobject which was identified
     */
    public static MathObject parseExpression(String str) 
	throws IllegalArgumentException {
	
	str = preprocess(str);
	if (str == null) 
	    throw new IllegalArgumentException("illegal assignment or emty statement");
	else {
	    MathObject result = parse(0, new Tokenizer(str, false), false, false);
	    if (result == null)
		throw new IllegalArgumentException("illegal expression on r.h.s.");
	    else return result;
	}
    }

    /**
     * this method fully evaluates an expression and assignes the result variable.
     * If you just want to evaluate an algebraic expression without assigning the
     * result variable use parseExpression
     * @see Parse#parseExpression .
     * @param str string to be evaluated
     */
    public String evaluateExpression(String str) throws IllegalArgumentException {
	String ans;
	LOG.reset();

	str = preprocess(str);
	if (str != null) {
	    Tokenizer t = new Tokenizer(str, true);
	    MathObject result = parse(0, t, false, true);
	    if (result != null) {
		ans = ((t.getAnswerVar() == null) ? "ans" : t.getAnswerVar());
		LOG.LOG(3, "evaluateExpression: name of answer variable = " + ans);
		if (echoAnswer || t.getAnswerVar() == null) {
		    if (mathlib.getVar(ans) == null) 
			fireMathlibEvent("ans", result, MathlibEvent.ADD);
		    else 
			fireMathlibEvent("ans", result, MathlibEvent.CHANGE);
		}
		LOG.LOG(3, "evaluateExpression: value of answer variable = " + mathlib.getVar(ans).toString());
		return ans;
	    } else throw new IllegalArgumentException("illegal expression on r.h.s.");
	}
	else throw new IllegalArgumentException("illegal assignment or emty statement");
    }

    /**
     * preprocesses a given string. It resolves certain task which right now cannot
     * be performed by the parse method.
     * @see Parse#parse
     * @param str string to preprocess
     * @return the preprocessed string which now can be parsed
     */
    private static String preprocess(String str) {
	str = str.trim();

	// empty string!
	if (str.length() == 0) return null;


	// check expression terminator
	if (!(str.charAt(str.length()-1) == ';')) str = str.concat(";"); 

	// commands
	//	if (str.equals("vars;")) { listVars(); return null; }

	// check for implicit operations
	str = replaceSubstring(str, "><", ">#<");
	//	str = replaceSubstring(str, "||", "|*|");
	str = replaceSubstring(str, "0|0", "0|*|0");
	str = replaceSubstring(str, "0|1", "0|*|1");
	str = replaceSubstring(str, "1|0", "1|*|0");
	str = replaceSubstring(str, "1|1", "1|*|1");

	//str = str.trim();
	LOG.LOG(1, "[preprocessor] returns "+ str);
	return str;
    }


    private static MathObject parse(int n, Tokenizer tokens, boolean assignmentPending,
				    boolean assignmentAllowed) {
	MathObject tok1 = null;
	MathObject arg1 = null, arg2 = null, arg3 = null, op = null, op2 = null;
	MathObject leadingUn = null;

	LOG.LOG(2, "[parse("+n+")] entering with assignmentPending = "+((assignmentPending) ? "true" : "false"));

	tok1 = tokens.nextToken(); 
	if (tok1 != null) LOG.LOG(2, "[parse("+n+"):tok1] "+tok1.toString());
	else LOG.LOG(0, "");
	//--------------------------------------
	// identify first node

	// look for leading unary op
	if ((tok1 instanceof UnaryOp) && (((UnaryOp)tok1).leadingUnaryOp())) {
	    LOG.LOG(1, "[parse("+n+")] leading unary op detected: " + tok1.toString());
	    leadingUn = tok1;
	    LOG.LOG(1, "[parse("+n+"):leadingUn] "+leadingUn.toString());

	    tok1 = tokens.nextToken("parse error: expression incomplete"); 
	    if (tok1 != null) LOG.LOG(2, "[parse("+n+"):tok1] "+tok1.toString());
	}

mainloop:
	while (tok1 != null) {

	    // process first argument

	    // here arg1 can be != null if the expression is of the form a+b+c
	    if (arg1 == null) arg1 = processArg(n, tokens, assignmentPending, assignmentAllowed);
	    // here arg1 == null is an error!
	    if (arg1 == null) return null; // should be already an error message!

	    LOG.LOG(2, "[parse("+n+"):leadingUnOp] " + ((leadingUn == null) ? "null" : leadingUn.toString()));
	    LOG.LOG(2, "[parse("+n+"):arg1] "+arg1.toString());

	    // apply leading unary op
	    if (leadingUn != null) {
		arg1 = ((UnaryOp)leadingUn).apply(arg1);
		LOG.LOG(1, "intermediate result: " + arg1.toString());
		leadingUn = null;
		LOG.LOG(1, "[parse("+n+"):arg1] "+arg1.toString());
	    }


	    // read last token
	    tokens.pushBack();
	    tok1 = tokens.nextToken();

	    //---------------------------------------
	    // identify first operator (assignments are handled in processArg!)

	    if ((tok1 == null) || (tok1 instanceof CloseDelimiter) || tok1 instanceof Separator) return arg1;
	    else if (tok1 instanceof BinaryOp || tok1 instanceof ConditionalOp) {
		op = tok1;
	    }
	    else {
		LOG.LOG(0, "logical error: operator expected!");
		return null;
	    } 


	    //---------------------------------------
	    // identify second argument

	    tok1 = tokens.nextToken("parse error: argument expected");
	    if (tok1 == null) return null;

	    arg2 = processArg(n, tokens, assignmentPending, assignmentAllowed);
	    if (arg2 == null) return null; // should be already an error message!

	    //---------------------------------------
	    // check for next operator (precedence problem)

	    tokens.pushBack();
	    tok1 = tokens.nextToken();
	    if (tok1 instanceof ConditionalOp) {
		arg1 = ((BinaryOp)op).apply((Argument)arg1, (Argument)arg2);
		LOG.LOG(1, "intermediate result: " + arg1.toString());
		continue mainloop; 
	    }
	    while (tok1 instanceof BinaryOp) {
		// well, this is the easy case ;-)
		if ( ((BinaryOp)op).precedence((BinaryOp)tok1) ) { 
		    arg1 = ((BinaryOp)op).apply(arg1, arg2);
		    LOG.LOG(1, "intermediate result: " + arg1.toString());
		    continue mainloop; // just leave the inner while loop !!
		}
		// now it's getting messy because we can't process the first two args
		else {
		    op2 = tok1;

		    //---------------------------------------
		    // identify third argument

		    tok1 = tokens.nextToken("parse error: argument expected");
		    if (tok1 == null) return null;

		    arg3 = processArg(n, tokens, assignmentPending, assignmentAllowed);
		    if (arg3 == null) return null; // should be already an error message!

		    // now we don't care anymore, we evaluate arg2 op2 arg3
		    arg2 = ((BinaryOp)op2).apply(arg2, arg3);
		    LOG.LOG(1, "intermediate result: " + arg2.toString());

		    tokens.pushBack();
		    tok1 = tokens.nextToken();
		}
	    }

	    // XXX not sure about assignmentPending....
	    if (tok1 == null || tok1 instanceof CloseDelimiter || assignmentPending) {
		if (op instanceof BinaryOp) return ((BinaryOp)op).apply(arg1, arg2);
		else return ((ConditionalOp)op).apply((Argument)arg1, (Argument)arg2);
	    }
	    else {
		LOG.LOG(0, "parse error: incomplete statement!");
		return null;
	    }
	}
	return null;

    }

    private static MathObject processArg(int n, Tokenizer tokens, boolean assignmentPending, boolean assignmentAllowed) {
	MathObject tok = null, arg = null;
	MathMap func = null;

	// recover last read token
	tokens.pushBack();
	tok = tokens.nextToken();
	LOG.LOG(3, "[processArg("+n+")] started with token: "+ tok.toString());

	// dig in...
	if (tok instanceof OpenDelimiter)
	    arg = parse(n+1, tokens, false, assignmentAllowed);
	// command
	else if (tok instanceof Command) {
	    func = (Command)tok;

	    // next token needs to be '(' !!
	    tok = tokens.nextToken("parse error: opening delimiter expected");
	    if (tok instanceof OpenDelimiter) {
		String argName = tokens.getNextTokenName();
		arg = parse(n+1, tokens, false, assignmentAllowed);
		LOG.LOG(2, "[processArg("+n+"):arg] first argument "+ arg.toString());

		tokens.pushBack();
		tok = tokens.nextToken();

		// second argument!
		if (tok instanceof Separator) {
		    String arg2Name = tokens.getNextTokenName();
		    MathObject arg2 = parse(n+1, tokens, false, assignmentAllowed);
		    if (arg2 == null) { 
			LOG.LOG(0, "second argument expected!"); return null; 
		    }
		    LOG.LOG(2, "[processArg("+n+"):arg] second argument "+ arg2.toString());
		    arg = ((Command)func).apply(argName, arg, arg2Name, arg2);
		}
		else {
		    arg = ((Command)func).apply(argName, arg);
		}
	    }
	    else {
		LOG.ERROR(func.toString(), "opening delimiter expected!", func.toString().length()+1);
		return null;
	    }
	}
	// function
	else if (tok instanceof MathMap) {
	    func = (MathMap)tok;

	    // next token needs to be '(' !!
	    tok = tokens.nextToken("parse error: opening delimiter expected");
	    if (tok instanceof OpenDelimiter) {
		arg = parse(n+1, tokens, false, assignmentAllowed);
		LOG.LOG(2, "[processArg("+n+"):arg] apply function "+ tok.toString());
		arg = func.apply(arg);
	    }
	    else {
		LOG.ERROR(func.toString(), "opening delimiter expected!", func.toString().length()+1);
		return null;
	    }
	}
	else if (tok instanceof Variable) {
	    Variable var = (Variable) tok;

	    // next token needs to be '='
	    tok = tokens.nextToken("parse error: assignment for unknown variable expected");
	    if (tok instanceof Assignment) {
		arg = parse(n + 1, tokens, true, assignmentAllowed);
		if (arg == null) {
		    LOG.LOG(0, "parse error: incomplete assignment"); return null;
		}
		LOG.LOG(2, "[processArg("+n+"):arg] apply assignment: " + var.toString() + tok.toString() + arg.toString());
		// arg is already the result...now fire MathlibEvent
		if (assignmentAllowed) {
		    LOG.LOG(2, "[processArg("+n+"):arg] fireMathlibEvent()");
		    // assign answer variable
		    fireMathlibEvent(var.toString(), null, category, arg, (Mathlib.getVar(var.toString()) == null) ? MathlibEvent.ADD: MathlibEvent.CHANGE);
		}
		return arg;
	    } 
	    else {
		LOG.LOG(0, "parse error: unknown variable!");
		return null;
	    }
	}
	// proceed with argument 
	else if (tok instanceof Argument) arg = tok;
	else {
	    LOG.LOG(0, "parse error: argument expected!");
	    return null;
	}

	// in this case there must already be an error message
	if (arg == null) return null;
	LOG.LOG(2, "[processArg("+n+"):arg] "+ arg.toString());

	// this is necessary for omittable outer brakets!
	if (n > 0 && assignmentPending == false)
	    tok = tokens.nextToken("parse error: ')' expected");
	else tok = tokens.nextToken();

	// check for unary ops...
	if (tok instanceof UnaryOp && (arg instanceof Variable || arg instanceof StringArgument)) {
	    LOG.LOG(0,"unary operators cannot be applied to strings or during an assignment");
	    return null;
	}

	while (tok instanceof UnaryOp) {
	    arg = ((UnaryOp)tok).apply(arg);
	    LOG.LOG(1, "[processArg("+n+"):arg] "+arg.toString());
	    if (n > 0 && assignmentPending == false) { 
		tok = tokens.nextToken("parse error: ')' expected");
		if (tok == null) return null;
	    }
	    else tok = tokens.nextToken();
	    LOG.LOG(2, "[processArg("+n+"):tok] "+ ((tok == null) ? "null":tok.toString()));
	}

	// if opening delimiter n  > 0 !!
	if (((n > 0) && (tok instanceof CloseDelimiter)) ||
	    (n > 0 && tok instanceof Separator) ||
	    ((n ==0) && (tok == null)) || 
	    //	    (tok instanceof CloseDelimiter && assignmentPending) ||
	    (tok == null && assignmentPending) ||
	    (tok instanceof BinaryOp) || (tok instanceof ConditionalOp) ||
	    (tok instanceof Variable))
	    {
	    
	    LOG.LOG(1, "[processArg("+n+")] returns "+ arg.toString());
	    return arg;
	}
	else {
	    LOG.LOG(0, "parse error: incomplete statement");
	    return null;
	}

    }

    /**
     * function that checks whether a given name is a valid variable name
     * @return true if the name can be used as a variable name
     */
    public static boolean checkForValidVarName(String name) {
	if (name == null) return false;

	for (int i = 0; i < name.length(); i++) {
	    if (",.!&|=()+'/-*#; \"".indexOf(name.charAt(i)) != -1) {
		return false;
	    }
	}
	return true;
    }

    /**
     * static method replacing a substring by another substring used by the preprocessor
     * @param workStr string to work in
     * @param oldStr substring to replace
     * @param newStr string to replace with
     */
    public static String replaceSubstring(String workStr, String oldStr, String newStr){
	String temp = new String("");
	int pos1 = 0, pos2 = 0;

	pos2 = workStr.indexOf(oldStr);
	if (pos2 == -1) return workStr;

	while ((pos2 != -1) && (pos2 < workStr.length())) {
	    temp = temp.concat(workStr.substring(pos1, pos2));
	    temp = temp.concat(newStr);
	    pos1 = pos2 + oldStr.length();
	    pos2 = workStr.indexOf(oldStr, pos1);
	}
	temp = temp.concat(workStr.substring(pos1));
	return temp;
    }


}
