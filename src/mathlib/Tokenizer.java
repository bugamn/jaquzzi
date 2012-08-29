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
 * <p>This class is one of the central classes for the parsing process. It analyzes a
 * Java String object and extracts tokens from the string for which corresponding
 * MathObject are created. The Tokenizer class is working in close connection to the
 * <tt>Parse</tt> class, in which the identified MathObjects are further
 * processed. </p> <p>The parsing process is usually done after the preprocessing
 * stage of the <tt>Parse</tt> class and identifies all objects in the string. It
 * therefore is responsible for the semantics of the Mathlib parsing method.</p>
 * <ul>
 * <li>complex numbers: a+bi, a+b*i</li>
 * <li>vectors: [a b c]</li>
 * <li>brakets: |0010>, <1001| </li>
 * <li>matrices (by row): [a b c, d e f, g h i]</li>
 * <li>gates: {-:-:1:VAR)</li>
 * <li>Strings: "abc"</li>
 * <li>binary operators: +, -, *, /, # (tensor product)</li>
 * <li>unary operators: +, -, ' (transpose), " (complex conjugate)</li>
 * <li>conditional operators: ==, !=, &&, ||</li>
 * </ul>
 * <p>There are two general modi, in which the Tokenizer class can be run: (a)
 * assignments within the expression are allowed (b) assignments are not allowed.
 * @see Parse
 * @see Mathlib
 * 
 */
public class Tokenizer {
    private String str;
    private int pos = 0;
    private MathObject token = null;

    private boolean leadingUnaryOpExpected = true;
    private boolean assignmentAllowed = false;

    private Vector tokens;

    private Vector tokenNames;
    private String tokenName;
    private int tokenNo;

    /**
     * creates a Tokenizer object for the given string. The string is parsed and the
     * tokens can be accessed with nextToken()
     * @param str string to work on
     * @param assignmentAllowed switch whether or not assignments are allowed
     */
    public Tokenizer(String str, boolean assignmentAllowed) {
	this.str = str;
	this.assignmentAllowed = assignmentAllowed;
	tokens = new Vector();
	tokenNames = new Vector();

	tokenName = "";
	tokenNo = -1;
	token = identifyToken();

	while (token != null) {
	    tokens.add(token);
	    tokenNo++;
	    tokenNames.add(tokenName);
	    tokenName = ""; // reset
	    token = identifyToken();
	}
	tokenNo = 0;
    }

    /**
     * returns the name of the answer variable in case of an assignment
     */
    public String getAnswerVar() {
	try {
	    if ((tokens.size() == 1 || tokens.elementAt(1) instanceof Assignment)
&& !(((String)tokenNames.elementAt(0)).equals("")))
		return (String) tokenNames.elementAt(0);
	} catch (Exception e) {}
	return null;
    }

    /**
     * returns the name of the current token if available. Since the <tt>Parse</tt> class
     * executes expressions in the moment they can resolved, names of variables get
     * lost. For some operations though the name of the variable is important,
     * e.g. Commands
     * @see Command
     */
    public String getTokenName() {
	try {
	    if (((String) tokenNames.elementAt(tokenNo-1)).equals("")) return null;
	    else return (String) tokenNames.elementAt(tokenNo-1);
	} catch (Exception e ) {}
	return null;
    }

    /**
     * returns the name of the next token if available. Since the <tt>Parse</tt> class
     * executes expressions in the moment they can resolved, names of variables get
     * lost. For some operations though the name of the variable is important,
     * e.g. Commands
     * @see Command
     */
    public String getNextTokenName() {
	try {
	    if (((String) tokenNames.elementAt(tokenNo)).equals("")) return null;
	    else return (String) tokenNames.elementAt(tokenNo);
	} catch (Exception e ) {}
	return null;
    }

    /**
     * returns whether or not there are more tokens.
     */
    public boolean hasMoreTokens() {
	return (tokenNo < tokens.size());
    }
    
    /**
     * returns the next token, returns null if there are no more tokens
     * @return MathObject
     */
    public MathObject nextToken() {
	MathObject token;
	if (hasMoreTokens()) token = (MathObject)tokens.get(tokenNo);
	else token = null;
	LOG.LOG(2, "[Tokenizer] returns: " +((token == null) ? "null":token.toString()));
	tokenNo++;
	return token;
    }

    /**
     * returns the next token and generates an error message in case there are no
     * more tokens. This method is used by the <tt>Parse</tt> class to generate
     * appropriate error messages.
     * @param errorMsg string to display if there are no more tokens
     * @return MathObject
     */
    public MathObject nextToken(String errorMsg) {
	MathObject m = nextToken();
	if (m == null) LOG.ERROR(this.str, errorMsg, pos);
	return m;
    }

    /**
     * goes back one token. A nextToken() followed by pushBack() followed by
     * nextToken() will give the same object twice.
     */
    public void pushBack() {
	if (tokenNo > 0) tokenNo--;
    }

    /**
     * the central identification method. It defines the semantics and identifies the mathobjects.
     */
    private MathObject identifyToken() {
	int oldpos = 0;
	char c;
	
	try{
	    c = str.charAt(pos);
	    while ( c == ' ') { 
		pos++; //omit spaces
		c = str.charAt(pos);
	    }
	} catch (Exception e) { 
	    return null;
	}

	oldpos = pos;
	switch (c) {

	// nan-argument

	//vector, matrix
	case '[': { 
	    pos = str.indexOf(']',oldpos);
	    if (pos == -1) { 
		LOG.ERROR(str,"identification error: incomplete vector/matrix",oldpos+1);
		return null;
	    }
	    LOG.LOG(5,"[identifyToken] vector/matrix identified: "
		    +str.substring(oldpos,pos+1));
	    pos++;
	    leadingUnaryOpExpected = false;
	    try {
		if (str.indexOf(',',oldpos) != -1) 
		    return Matrix.parseMatrix(str.substring(oldpos,pos));
		else return Vect.parseVector(str.substring(oldpos,pos));
	    } catch (Exception e) {
		LOG.ERROR(str, e.getMessage(), oldpos);
		return null;
	    }
	}

	case '{': {
	    pos = str.indexOf('}',oldpos);
	    if (pos == -1) { 
		LOG.ERROR(str,"identification error: incomplete gate",oldpos+1);
		return null;
	    }
	    LOG.LOG(5,"[identifyToken] gate identified: "
		    +str.substring(oldpos,pos+1));
	    pos++;
	    leadingUnaryOpExpected = false;
	    try {
		return Gate.parseGate(str.substring(oldpos,pos));

	    } catch (Exception e) {
		LOG.ERROR(str, e.getMessage(), oldpos);
		return null;
	    }
	}

	// ket
	case '|': { 
	    pos++;
	    try { c = str.charAt(pos); }
	    catch (Exception e) { 
		LOG.ERROR(str, "[identifyToken] identification error: incomplete conditional operator", pos+1);
		return null;
	    }
	    if (c == '|') {
		pos++;
		return new ConditionalOp("||");
	    }
	    else {
		pos = str.indexOf('>',oldpos); 
		if (pos == -1) { 
		    LOG.ERROR(str,"identification error: incomplete ket",oldpos+1); 
		    return null;
		}
		pos++;
		leadingUnaryOpExpected = false;
		LOG.LOG(5,"[identifyToken] ket identified: "+str.substring(oldpos,pos));
		try {
		    return Braket.parseBraket(str.substring(oldpos,pos));
		} catch (Exception e) {
		    LOG.ERROR(str, e.getMessage(), oldpos);
		    return null;		
		}
	    }
	}
	// bra
	case '<': { 
	    pos = str.indexOf('|',oldpos); 
	    if (pos == -1) { 
		LOG.ERROR(str, "identification error: incomplete bra", oldpos+1); 
		return null;
	    }
	    pos++;
	    leadingUnaryOpExpected = false;
	    LOG.LOG(5,"[identifyToken] bra identified: "+str.substring(oldpos,pos));
	    try {
		return Braket.parseBraket(str.substring(oldpos,pos));
	    } catch (Exception e) {
		LOG.ERROR(str, e.getMessage(), oldpos);
		return null;		
	    }
	}

	// simple unary operator
	// transpose
	case '\'': { 
	    pos++;
	    LOG.LOG(5,"[identifyToken] unary operator identified: "+c);
	    return new UnaryOp(c); 
	}

	// adjoint
	// this symbol has a double meaning
	case '"' :{ 
	    // is unary operator if leading token = Argument or UnaryOp
	    pos++;
	    try {
		Argument a = (Argument)tokens.get(tokenNo);
		LOG.LOG(5,"[identifyToken] unary operator identified: "+c);
		return new UnaryOp(c); 
	    } catch (Exception e) {
		try {
		    if (tokens.get(tokenNo) instanceof UnaryOp) {
			LOG.LOG(5,"[identifyToken] unary operator identified: "+c);
			return new UnaryOp(c); 
		    }
		} catch (Exception e2) {
		}
		// ok, let's try whether it's a StringArgument
		pos = str.indexOf('"',oldpos+1); 
		if (pos == -1) { 
		    LOG.ERROR(str, "identification error: incomplete string", oldpos+1); 
		    return null;
		}
		pos++;
		leadingUnaryOpExpected = false;
		LOG.LOG(5,"[identifyToken] string identified: "+str.substring(oldpos,pos));
		try {
		    return new StringArgument(str.substring(oldpos+1,pos-1));
		} catch (Exception excep) {
		    LOG.ERROR(str, excep.getMessage(), oldpos);
		    return null;		
		}

	    }
	}

	// simple binary operator

	case '+':
	case '-': if (leadingUnaryOpExpected) {
	    pos++;
	    leadingUnaryOpExpected = false;
	    LOG.LOG(5,"[identifyToken] unary operator identified: "+c);
	    return new UnaryOp(c); 
	}
	case '*':
	case '/': 
	case '#': { 
	    pos++;
	    leadingUnaryOpExpected = false;
	    LOG.LOG(5,"[identifyToken] binary operator identified: "+c);
	    return new BinaryOp(c); }


	case '(': {
	    pos++;
	    leadingUnaryOpExpected = true;
	    LOG.LOG(5,"[identifyToken] delimiter identified: "+c);
	    return new OpenDelimiter();}
	case ')': { 
	    pos++;
	    leadingUnaryOpExpected = false;
	    LOG.LOG(5,"[identifyToken] delimiter identified: "+c);
	    return new CloseDelimiter();
	}
	// conditional operators
	case '!': {
	    pos++;
	    try { c = str.charAt(pos); }
	    catch (Exception e) { 
		LOG.ERROR(str, "[identifyToken] identification error: incomplete conditional operator", pos+1);
		return null;
	    }
	    if (c == '=') {
		pos++;
		return new ConditionalOp("!=");
	    }
	    else {
		LOG.ERROR(str, "[identifyToken] identification error: invalid conditional operator", pos+1);
		return null;
	    }
	}
	case '&': {
	    pos++;
	    try { c = str.charAt(pos); }
	    catch (Exception e) { 
		LOG.ERROR(str, "[identifyToken] identification error: incomplete conditional operator", pos+1);
		return null;
	    }
	    if (c == '&') {
		pos++;
		return new ConditionalOp("&&");
	    }
	    else {
		LOG.ERROR(str, "[identifyToken] identification error: invalid conditional operator", pos+1);
		return null;
	    }
	}
	case '=': {
	    pos++;
	    try { c = str.charAt(pos); }
	    catch (Exception e) { 
		LOG.ERROR(str, "[identifyToken] identification error: incomplete conditional operator/assignment", pos+1);
		return null;
	    }
	    if (c == '=')  {
		pos++;
		return new ConditionalOp("==");
	    }
	    else {
		if (assignmentAllowed == true) { 
		    leadingUnaryOpExpected = true;
		    LOG.LOG(5, "[identifyToken] assignment identified: "+ c);
		    return new Assignment();
		}
	    }
	    // attention: steps to next case....
	}
	case ';': {
	    pos++;
	    leadingUnaryOpExpected = true;
	    LOG.LOG(5,"[identifyToken] terminator identified: "+c);
	    return null;
	}

	case ',': {
	    pos++;
	    leadingUnaryOpExpected = true;
	    LOG.LOG(5, "[identifyToken] separator identified: "+ c);
	    return new Separator();
	}

	// number or variable
	default: {
	    MathObject result = null;
	    leadingUnaryOpExpected = false;
	    while (",!&|=()+'/-*#; \"".indexOf(c) == -1) {
		pos++;
		try { c = str.charAt(pos); }
		catch (Exception e) { 
		    LOG.ERROR(str, "[identifyToken] identification error: argument finalizer missing", pos+1);
		    return null;
		}
		LOG.LOG(6, "[identifyToken:number] pos: "+pos + " char: "+c);
	    }
	    // NEW! handling of numbers in the format 1E-7
	    if (c == '-') {
		try { 
		    c = str.charAt(pos-1); 
		    if (c == 'E') {
			pos++;
			while (",!&|=()+'/-*#; \"".indexOf(c) == -1) {
			    pos++;
			    try { c = str.charAt(pos); }
			    catch (Exception e) { 
				LOG.ERROR(str, "[identifyToken] identification error: argument finalizer missing", pos+1);
				return null;
			    }
			    LOG.LOG(6, "[identifyToken:number] pos: "+pos + " char: "+c);
			}
		    }
		}
		catch (Exception e) { 
		}		
	    }

	    try { return Complex.parseComplex(str.substring(oldpos, pos)); }
	    // okay... identified object is not a complex number -> check vars
	    catch (NumberFormatException e) {
		try { 
		    LOG.LOG(5, "[Tokenizer] not a complex number: " + str.substring(oldpos, pos));
		    result = Mathlib.getFunction(str.substring(oldpos, pos));
		    if (result == null) {
			LOG.LOG(5, "[Tokenizer] not a function: " + str.substring(oldpos, pos));
			result = Mathlib.getVar(str.substring(oldpos, pos)); 
			if (result == null) {
			    if (assignmentAllowed == false) {
				LOG.ERROR(str,"parse error: unknown variable!", pos+1);
				return null;
			    }
			    else {
				// assign answer variable
				tokenName = str.substring(oldpos, pos);

				result = new Variable(str.substring(oldpos, pos));
				LOG.LOG(5, "[Tokenizer] returns new variable "+ result.toString());
				return result;
			    }
			} else {
			    result = (MathObject) result.clone();
			    // assign answer variable
			    tokenName = str.substring(oldpos, pos);

			    // check for assignment
			    int temp = pos;
			    try {
				while (str.charAt(temp) == ' ') temp++;
			    } catch (Exception ee) { return result;}
			    try {
				// assignment!! ignore value!
				if (str.charAt(temp) == '=' && str.charAt(++temp) != '=') {
				    result = new Variable(str.substring(oldpos,pos));
				    LOG.LOG(5, "[Tokenizer] returns new variable " + result.toString());
				    return result;
				}
			    } catch (Exception ee ) { return result;}
			    LOG.LOG(5, "[Tokenizer] returns " + result.toString());
			    return result;
			}
		    } else {
			LOG.LOG(5, "[Tokenizer] returns " + result.toString());
			return result;
		    }
		}
		catch (MissingResourceException mre) {
		    LOG.ERROR(str,"parse error: unknown object!", pos+1);
		    return null;
		}
		    
	    }
	    catch(Exception e) { LOG.LOG(0, "mhh..serious error"); return null; }

	}
	}

    }

}

