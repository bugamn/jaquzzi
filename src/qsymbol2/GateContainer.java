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

import mathlib.Argument;
import mathlib.Complex;
import mathlib.Gate;
import mathlib.GateProperty;
import mathlib.MathObject;
import mathlib.Mathlib;
import mathlib.MathlibEvent;
import mathlib.Matrix;
import mathlib.NoiseError;
import mathlib.Parse;

/**
 * <p>class wrapping a Mathlib Gate object. This class provides the ability of grouping
 * gates together and hierarchical arrange them. It uses the addtional information
 * provided by optional GateProperty objects to provide multiple iterations of
 * gates and gate groups, gates with errors and as well handles the simulatin of
 * decoherence.</p>
 * <p>Gates are grouped with the help of their name qualifier. For example the gates
 * group1.gate0 and group1.gate1 would belong to the same group. Since this class is
 * responsible for holding groups of gates it implements the Steppable interface with
 * which it administers the sequential operation of gates.</p>
 * @see Steppable
 * @see edu.buffalo.fs7.mathlib.Gate
 *
 */
public class GateContainer implements Steppable{
    private static final String GATE_GROUP = "gate group";

    /** holds child containers */
    protected Vector childs;
    /** holds the name of the gate in case this container is a leaf */
    protected String gateName;
    /** holds the child which "has" the current step*/
    protected int stepChild;
    /** holds the current iteration of the gate container */
    protected int currentIteration;
    /** holds the step focus. Only one gate container at a time is allowed to hold
     * the step focus. */
    protected boolean stepFocus;

    /** name of the gate container */
    protected String name;
    /** description of the gate container */
    protected String description;
    /** parent container */
    protected GateContainer parent = null;

    /**
     * constructs a gate container
     * @param parent the parent gate container or null
     * @param level the level of the gate container
     * @param name the full name of the gate container
     * @param gateName the name of the gate
     */
    public GateContainer(GateContainer parent, String name, String gateName) {
	this.parent = parent;
	this.currentIteration = 0;
	this.stepChild = 0;
	this.stepFocus = false;
	NameStripper domains = new NameStripper(name);
	if (domains.getGroupCount() > 0) {
	    this.name = domains.getGroup(0);
	    this.gateName = null;
	    this.description = generateGateDescription();
	    childs = new Vector();
	    domains.stripLeadingGroup();
	    childs.add(new GateContainer(this, domains.getFullName(), gateName));
	}
	else {
	    this.name = domains.getName();
	    this.gateName = gateName;
	    this.description = generateGateDescription();
	}
    }


    //----------------------------------------------
    // child management

    /**
     * determines whether a gate container qualified by the given string parameter is
     * a child of this gatecontainer.
     * @param name name of variable for gatecontainer
     * @return true if child
     */
    public boolean containsGateContainer(String name) {
	Enumeration enum = getLeaves();
	boolean cont = false;
	while (enum.hasMoreElements() && cont == false){
	    if (name.equals(((GateContainer)enum.nextElement()).getFullName())) 
		cont = true;
	}
	return cont;
    }

    /**
     * adds a given gate container as a child to this gate container.
     */
    public void addChildContainer(GateContainer child) {
	//increase level of child
	child.levelDown(getFullName());
	child.setParent(this);
	if (childs == null) {
	    childs = new Vector();
	}
	childs.add(child);
	//	System.out.println("container: "+child.toString() + " has been added to container: "+ this.toString());
    }

    /**
     * inserts a given gate container to a certain position of the child containers.
     */
    public void insertChildContainer(GateContainer child, int pos) {
	if (pos >= 0 && pos <= getChildCount()) {
	    child.levelDown(getFullName());
	    child.setParent(this);

	    if (childs == null) {
		childs = new Vector();
	    }
	    childs.insertElementAt(child, pos);
	    //	    System.out.println("container: "+child.toString() + " has been added to container: "+ this.toString());

	}
    }

    /**
     * removes a child container specified by child.
     * @param child the child container to be removed
     */
    public void removeChildContainer(GateContainer child) {
	if (childs != null) {
	    childs.remove(child);
	}
    }

    /**
     * returns the number of direct childs
     */
    public int getChildCount() {
	if (childs == null) return 0;
	else return childs.size();
    }

    /**
     * returns a specified child container. null if index out of range.
     * @param number of child starting with 0
     */
    public GateContainer getChildContainer(int i) {
	if (isLeafContainer() == false && i >= 0 && i < getChildCount()){
	    return (GateContainer)childs.elementAt(i);
	}
	return null;
    }

    /**
     * returns the direct childs in an Enumeration object.
     */
    public Enumeration getChilds() {
	return new Enumeration() {
		int child = 0;
		public boolean hasMoreElements() {
		    return (child < getChildCount());
		}
		public Object nextElement() {
		    return getChildContainer(child++);
		}
	    };
    }

    /**
     * returns childs on a specified level in an Enumeration object.
     * @param level child level
     */
    public Enumeration getChildsOnLevel(int level) {
	Vector containers = new Vector();
	addChildsToVector(level, containers);
	return containers.elements();
    }

    /**
     * adds childs of a certain level to a given vector
     */
    private void addChildsToVector(int level, Vector containers) {
	if (getContainerLevel() == level) containers.add(this);
	else if (getContainerLevel() < level && isLeafContainer()) containers.add("empty");
	else if (isLeafContainer() == false) {
	    Enumeration e = getChilds();
	    GateContainer child;
	    while (e.hasMoreElements()) {
		child = (GateContainer) e.nextElement();
		child.addChildsToVector(level, containers);
	    }
	}
    }

    //-------------------------------------------
    // leaf methods

    /**
     * removes a leaf container specified by name
     */
    public void removeLeafGateContainer(String name) {
	Enumeration enum = getLeaves();
	boolean cont = false;
	GateContainer gateContainer = null;
	while (enum.hasMoreElements() && cont == false){
	    gateContainer = (GateContainer)enum.nextElement();
	    if (name.equals(gateContainer.getFullName())) cont = true;
	}
	if (cont) {
	    removeContainerIfNecessary(gateContainer);
	}
    }

    /**
     *
     */
    private void removeContainerIfNecessary(GateContainer gateContainer) {
	GateContainer parent = gateContainer.getParent();
	if (parent != null) {
	    parent.removeChildContainer(gateContainer);
	    if (parent.getChildCount() == 0) 
		removeContainerIfNecessary(this);
	}
    }

    /**
     * returns an enumeration of the leaves parented by this container and its
     * childs.
     */
    public Enumeration getLeaves() {
	Vector leaves = new Vector();
	addLeavesToVector(leaves);
	return leaves.elements();
    }

    /**
     * determines the number of leaves parented by this container and its child 
     * containers
     * @return number of leafs
     */
    public int getLeafCount() {
	if (isLeafContainer()) return 1;
	else {
	    Enumeration e = getChilds();
	    int count = 0;
	    while (e.hasMoreElements()) {
		count += ((GateContainer)e.nextElement()).getLeafCount();
	    }
	    return count;
	}
    }

    /**
     * method to add all subsidary leaf containers to a given vector
     */
    private void addLeavesToVector(Vector leaves) {
	if (isLeafContainer()) leaves.add(this);
	else {
	    Enumeration e = getChilds();
	    GateContainer child;
	    while (e.hasMoreElements()) {
		child = (GateContainer) e.nextElement();
		child.addLeavesToVector(leaves);
	    }
	}
    }

    //------------------------------------------
    // other methods


    /**
     * ungroups a parent container into its child components. The child container are
     * updated according to the new structure and returned as an enumeration.
     *
     */
    public Enumeration ungroup() {
	// get rid of group properties
	GateProperty gp = getPropertyObject();
	if (gp != null) {
	    Parse.fireMathlibEvent(getFullName()+"_properties", getFullName()+"_properties", "current circuit", gp, MathlibEvent.REMOVE);
	}

	// take care of childs
	return new Enumeration() {
		int child = 0;
		public boolean hasMoreElements() {
		    return child < getChildCount();
		}

		public Object nextElement() {
		    GateContainer childContainer = getChildContainer(child++);
		    String oldTopLevel = getTopLevelName();
		    childContainer.setParent(null);
		    childContainer.levelUp(oldTopLevel);
		    return childContainer;
		}
	    };
    }

    /**
     * brings a child container one level up. The property objects and the gate variables
     * are adjusted accordingly.
     * @param oldTopLevel the old top level name of the gate container
     */
    protected void levelUp(String oldTopLevel) {
	String fullName = getFullName();
	String oldFullName = oldTopLevel.concat("."+fullName);
	updateDescription();
	if (isLeafContainer()) {
	    // change gate container
	    String oldName = name;
	    while(Mathlib.getVar(getFullName()) != null) {
		name = oldName + "_"+Mathlib.getRandom().nextInt(10000);
	    }
	    fullName = getFullName();

	    Parse.fireMathlibEvent(oldFullName, fullName, "current circuit", getGate(), MathlibEvent.CHANGE_NAME);
	    setGate(fullName);

	    // change property object
	    MathObject gp = Mathlib.getVar(oldFullName+"_properties");
	    if (gp != null) {
		((GateProperty)gp).setGate(fullName);
		Parse.fireMathlibEvent(oldFullName+"_properties", fullName+"_properties", "current circuit", gp, MathlibEvent.CHANGE_NAME);
	    }

	}
	else {
	    // change property object
	    MathObject gp = Mathlib.getVar(oldFullName+"_properties");
	    if (gp != null) {
		((GateProperty)gp).setGate(fullName);
		Parse.fireMathlibEvent(oldFullName+"_properties", fullName+"_properties", "current circuit", gp, MathlibEvent.CHANGE_NAME);
	    }
	    for (int i = 0; i < getChildCount(); i++) {
		getChildContainer(i).levelUp(oldTopLevel);
	    }
	}
    }


    /**
     * brings a child container one level down. The property objects and the gate 
     * variables are adjusted accordingly.
     * @param oldTopLevel the old top level name of the gate container
     */
    protected void levelDown(String newTopLevel) {
	String oldFullName = getFullName();
	String fullName = newTopLevel+"."+oldFullName;
	updateDescription();
	if (isLeafContainer()) {
	    // change gate container
	    Parse.fireMathlibEvent(oldFullName, fullName, "current circuit", getGate(), MathlibEvent.CHANGE_NAME);
	    setGate(fullName);

	    // change property object
	    MathObject gp = Mathlib.getVar(oldFullName+"_properties");
	    if (gp != null) {
		((GateProperty)gp).setGate(fullName);
		Parse.fireMathlibEvent(oldFullName+"_properties", fullName+"_properties", "current circuit", gp, MathlibEvent.CHANGE_NAME);
	    }
	}
	else {
	    // change property object
	    MathObject gp = Mathlib.getVar(oldFullName+"_properties");
	    if (gp != null) {
		((GateProperty)gp).setGate(fullName);
		Parse.fireMathlibEvent(oldFullName+"_properties", fullName+"_properties", "current circuit", gp, MathlibEvent.CHANGE_NAME);
	    }
	    for (int i = 0; i < getChildCount(); i++) {
		getChildContainer(i).levelDown(newTopLevel);
	    }

	}
    }

    /**
     * this is a special method to add a single container column to an existing
     * container and merge matching groups.
      */
    public GateContainer addContainerIfPossible(GateContainer nextContainer) {
	// can't add to a leaf container...
	if (isLeafContainer()) {
	    return nextContainer;
	}

	//okay, proceed...
	if (getTopLevelName().equals(nextContainer.getTopLevelName())) {
	    // last child
	    GateContainer lastChild = getChildContainer(getChildCount()-1);
	    GateContainer targetContainer = lastChild.addContainerIfPossible(nextContainer.getChildContainer(0));
	    // container not added!
	    if (targetContainer != lastChild) {
		if (childs == null)
		    childs = new Vector();
		targetContainer.setParent(this);
		targetContainer.updateDescription();
		childs.add(targetContainer);
		return this;
	    }
	    return this;
	}
	else {
	    return nextContainer;
	}
    }

    /**
     * returns the parent of the gate container
     */
    public GateContainer getParent() {
	return parent;
    }

    /**
     * sets the parent of the gate container
     */
    public void setParent(GateContainer parent) {
	this.parent = parent;
    }

    /**
     * adjusts the child container if the top level container was renamed.
     *
     */
    public void adjustGateContainer(String oldTopLevel) {
	if (isLeafContainer()) {
	    String fullName = getFullName();
	    if (fullName.equals(gateName) == false) {
		// property object
		MathObject gp = Mathlib.getVar(gateName+"_properties");
		if (gp != null) {
		    ((GateProperty)gp).setGate(fullName);
		    Parse.fireMathlibEvent(gateName+"_properties", fullName+"_properties", "current circuit", gp, MathlibEvent.CHANGE_NAME);
		}

		Parse.fireMathlibEvent(gateName, fullName, "current circuit", getGate(), MathlibEvent.CHANGE_NAME);
		setGate(fullName);
	    }

	}
	else {
	    String fullName = getFullName();
	    NameStripper ns = new NameStripper(fullName);
	    ns.stripLeadingGroup();
	    String oldFullName = oldTopLevel + "." + ns.getFullName();
	    if (fullName.equals(oldFullName) == false) {
		// property object
		MathObject gp = Mathlib.getVar(oldFullName+ "_properties");
		if (gp != null) {
		    ((GateProperty)gp).setGate(fullName);
		    Parse.fireMathlibEvent(oldFullName+"_properties", fullName+"_properties", "current circuit", gp, MathlibEvent.CHANGE_NAME);
		}

		for (int i = 0; i < getChildCount(); i++) {
		    getChildContainer(i).adjustGateContainer(oldTopLevel);
		}
	    }

	}
    }


    /**
     * returns the container level
     */
    public int getContainerLevel() {
	int i = 0;
	GateContainer gc = this;
	while (gc.getParent() != null) {
	    i++;
	    gc = gc.getParent();
	}
	return i;
    }

    /**
     * returns the depth of this container 
     */
    public int getContainerDepth() {
	if (isLeafContainer()) {
	    return 0;
	}
	else {
	    Enumeration leaves = getLeaves();
	    GateContainer gc;
	    int level = 0;
	    int childLevel;
	    while (leaves.hasMoreElements()) {
		gc = (GateContainer)leaves.nextElement();
		childLevel = gc.getContainerLevel();
		if (childLevel > level) level = childLevel;
	    }
	    return level;
	}
    }

    /**
     * returns the most important information of the gate containe as a string.
     */
    public String toString() {
	String str = new String("");
	str = str.concat("TopLevelName: "+ getTopLevelName()+"\n");
	str = str.concat("containerLevel: " + getContainerLevel()+"\n");
	str = str.concat("childCount: " + getChildCount()+"\n");
	str = str.concat("stepChild: " + stepChild+"\n");
	str = str.concat("currentIteration: " + getCurrentIteration()+"\n");
	str = str.concat("maxIteration: " + getMaxIteration()+"\n");
	str = str.concat("LeafContainer: " + isLeafContainer()+"\n");
	return str;
    }

    //---------------------------------------------
    // GateContainer properties

    /**
     * returns the property object
     */
    public GateProperty getPropertyObject() {
	MathObject o = Mathlib.getVar(getFullName()+"_properties");
	return (o == null) ? null: (GateProperty)o;
    }

    /**
     * registers the given gateProperty object as the property object of this gate 
     * containers.
     */
    public void setPropertyObject(GateProperty gateProperty) {
	int action;
	if (getPropertyObject() == null) action = MathlibEvent.ADD;
	else action = MathlibEvent.CHANGE;
	gateProperty.setGate(getFullName());
	Parse.fireMathlibEvent(getFullName()+"_properties", null, "current circuit", gateProperty, action);
    }

    /**
     * determines whether container is a leaf container and therefore holds a gate
     */
    public boolean isLeafContainer() {
	return (gateName != null && getChildCount() == 0);
    }

    /**
     * returns the dimension of the first gate contained by this container
     */
    public int getDimension() {
	GateContainer container = this;
	while (container.isLeafContainer() == false) {
	    container = container.getChildContainer(0);
	}
	if (container.isLeafContainer()) return container.getGate().n;
	else return 0;
    }

    /**
     * returns the top level name of the gate container. E.g. group.gate0 --> group
     */
    public String getTopLevelName() {
	return name;
    }

    /**
     * sets the top level name of the gate container.
     */
    public void setTopLevelName(String name) {
	this.name = name;
    }

    /**
     * returns the full name of the gate container
     */
    public String getFullName() {
	String fullName = getTopLevelName();
	GateContainer pointer = getParent();
	while (pointer != null) {
	    fullName = pointer.getTopLevelName()+"."+fullName;
	    pointer = pointer.getParent();
	}
	return fullName;
    }

    /**
     * returns the current iteration of this gate container. Used by the graphics 
     * routines to plot the header.
     */
    public int getCurrentIteration() {
	return currentIteration;
    }

    /**
     * sets the value of the current iteration
     */
    public void setCurrentIteration(int currentIteration) {
	this.currentIteration = currentIteration;
    }

    /**
     * returns the maximum number of iterations for this gate container.
     */
    public int getMaxIteration() {
	MathObject properties = Mathlib.getVar(getFullName()+"_properties");
	if (properties != null) {
	    Complex c = (Complex)((GateProperty)properties).getProperty("reps");
	    if (c != null) return new Double(c.re()).intValue();
	}
	return 1;
    }

    /**
     * returns gate if container is leaf container otherwise null
     */
    public Gate getGate() {
	if (isLeafContainer()) {
	    return (Gate)Mathlib.getVar(gateName);
	}
	else return null;
    }
    /*
    public Gate getSimulationGate() {
	if (isLeafContainer()) {
	    return (Gate)Mathlib.getVar(gateName);
	}
	else return null;
    }
    */
    /**
     * sets the gate hold by this gatecontainer. only applicable to leaf containers.
     * Name of the variable that represents the gate.
     */
    public void setGate(String gateName) {
	this.gateName = gateName;
    }

    /**
     * returns the gate which corresponds to the current step. It returns null if the
     * step is larger than the gates hold by this gate container. 
     * @return the current gate
     */
    public Gate getCurrentGate() {
	if (isLeafContainer()) {
	    return getGate();
	}
	else {
	    if (stepChild < getChildCount()) {
		return getChildContainer(stepChild).getCurrentGate();
	    } 
	    return null;
	}
    }

    /**
     * returns the error modifier (2x2 matrix) for the current gate or null if none is specified
     */
    public Matrix getCurrentErrorMatrix() {
	if (isLeafContainer()) {
	    double sigma = getProperty("sigma");
	    NoiseError noiseError = new NoiseError();
	    if (sigma == 0) return null;
	    else return (Matrix)noiseError.apply(new Complex(sigma));
	}
	else {
	    if (stepChild < getChildCount()) {
		return getChildContainer(stepChild).getCurrentErrorMatrix();
	    } 
	    return null;
	}
    }

    public double getCurrentDecoherenceRate() {
	if (isLeafContainer()) {
	    return getProperty("rate");
	}
	else {
	    if (stepChild < getChildCount()) {
		return getChildContainer(stepChild).getCurrentDecoherenceRate();
	    } 
	    return 0;
	}
    }

    public double getCurrentDecayProbability() {
	if (isLeafContainer()) {
	    return getProperty("decay");
	}
	else {
	    if (stepChild < getChildCount()) {
		return getChildContainer(stepChild).getCurrentDecayProbability();
	    } 
	    return 0;
	}
    }

    /**
     * returns the description of this gate container as displayed by the tooltip
     * @return string of description
     */
    public String getDescription() {
	return description;
    }

    /**
     * updates gate container description
     */
    public void updateDescription() {
	description = generateGateDescription();
    }

    /**
     * generates gate container description
     */
    private String generateGateDescription() {
	if (isLeafContainer()) {
	    Gate g = getGate();
	    String str = new String("");
	    String matrix = new String("");
	    int pos = 0;

	    pos = g.gate_descr.indexOf("1");
	    while (pos != -1) {
		str = str.concat("c-");
		pos = g.gate_descr.indexOf("1",pos+1);
	    }
	    if (g.gate_descr.indexOf("m") != -1) {

		String display;
		String s = g.matrixName;

		str = str.concat(g.matrixName);

		str = str.concat(" gate");
	    }
	    else if (g.gate_descr.indexOf("!") != -1) {
		str = str.concat("partial measurement");
	    }
	    else if (g.gate_descr.indexOf("u") != -1 || g.gate_descr.indexOf("d") != -1) {
		str = str.concat("preparation gate");
	    }
	    if (str.equals("")) str = str.concat("unity gate");
	    return str;
	}
	else return GATE_GROUP;
    }


    //----------------------------------------------
    // Stepable interface

    /**
     * sets the step focus to this gate container. This property is used to determine
     * whether the current step is in this container
     */
    public void setStepFocus(boolean stepFocus) {
	if (isLeafContainer()) 
	    this.stepFocus = stepFocus;
	else {
	    this.stepFocus = stepFocus;
	    for (int i = 0; i < getChildCount(); i++) 
		getChildContainer(i).setStepFocus(stepFocus);
	}
    }

    /**
     * returns wheter the gate container has the step focus
     */
    public boolean hasStepFocus() {
	return stepFocus;
    }

    /**
     * returns the number of steps contained in this gatecontainer. It is the number
     * calculated by adding up all childs and their repetition.
     */
    public int getStepCount() { 
	if (isLeafContainer()) {
	    return getMaxIteration();
	}
	else {
	    int childCount = 0;
	    for (int i = 0; i < getChildCount(); i++) {
		childCount += getChildContainer(i).getStepCount();
	    }
	    return childCount*getMaxIteration(); 
	}
    }

    /**
     * returns the step child index
     */
    protected int getStepChild() {
	return stepChild;
    }

    /**
     * increases the step by one.
     * @return true if step was successful, false if this step leaves the gatecontainer
     */
    public boolean stepForward() {
	if (isLeafContainer()) {
	    currentIteration++;
	    if (getMaxIteration()-getCurrentIteration() <= 0) {
		currentIteration = getMaxIteration();
		return false;
	    } else {
		return true;
	    }
	}
	else {
	    if (getChildContainer(stepChild).stepForward() == false) {
		stepChild++;
		if (stepChild == getChildCount()) {
		    currentIteration++;
		    if (getMaxIteration()-getCurrentIteration() <= 0) {
			currentIteration = getMaxIteration();
			stepChild--; // correct the value 
			return false;
		    }else {
			int curIt = currentIteration;
			reset();
			currentIteration = curIt;
			stepChild = 0;
			return true;
		    }
		} else return true;
	    } 
	    return true;
	}
    }

    /**
     * decreases the step by one.
     * @return true if step was successful, false if this step leaves the gatecontainer
     */
    public boolean stepBackward() {
	if (isLeafContainer()) {
	    if (getCurrentIteration() <= 0) {
		currentIteration = 0;
		return false;
	    } else {
		GateContainer par = getParent();
		while (par != null) {
		    if (par.getCurrentIteration() == par.getMaxIteration())
			par.setCurrentIteration(par.getCurrentIteration()-1);
		    par = par.getParent();
		}
		currentIteration--;
		return true;
	    }
	}
	else {
	    if (getChildContainer(stepChild).stepBackward() == false) {
		stepChild--;
		if (stepChild < 0) {
		    if (getCurrentIteration() <= 0) {
			currentIteration = 0;
			stepChild = 0;
			return false;
		    } else {
			int curIt = currentIteration-1;
			complete();
			currentIteration = curIt;
			getChildContainer(stepChild).stepBackward();
			return true;
		    }
		} else {
		    getChildContainer(stepChild).stepBackward();
		    return true;
		}
	    } 
	    return true;
	}
    }

    /**
     * returns the current step within this gate container
     * @return -1 if the gate container does not have the step focus
     */
    public int getStep() {
	if (hasStepFocus()) {
	    if (isLeafContainer()) return getCurrentIteration();
	    else {
		int count = 0;
		for (int i = 0; i < stepChild; i++) {
		    count += getChildContainer(i).getStepCount();
		}
		count += getChildContainer(stepChild).getStep();
		return count;
	    }
	}
	else return -1;
    }

    /**
     * returns true if the current gate container holds the current step
     */
    public boolean isStep() {
	if (hasStepFocus()) {
	    GateContainer parentContainer = getParent();
	    // top container
	    if (parentContainer == null) {
		return true;
	    }
	    else {
		if (parentContainer.isStep() && parentContainer.getChildContainer(parentContainer.stepChild).getFullName().equals(getFullName())) {
		    return true;
		}
		else return false;
	    }
	}
	else return false;
    }

    /**
     * resets the step handling of the gate container
     */
    public void reset() {
	if (isLeafContainer()) {
	    currentIteration = 0;
	    stepChild = 0;
	} else {
	    currentIteration = 0;
	    stepChild = 0;
	    for (int i = 0; i < getChildCount(); i++) {
		getChildContainer(i).reset();
	    }
	}
    }

    /**
     * sets the step handling of the gate container to a completed state
     */
    public void complete() {
	if (isLeafContainer()) {
	    currentIteration = getMaxIteration();
	    stepChild = 0;
	} else {
	    currentIteration = getMaxIteration();
	    stepChild = getChildCount()-1;
	    for (int i = 0; i < getChildCount(); i++) {
		getChildContainer(i).complete();
	    }
	}
    }

    // -----------------------------------------------------
    /**
     * returns a numerical property from the gate property object or the circuit
     * property object (gate preferred over circuit). returns 0 if no property assigned.
     * @param property the string identification of the property
     */
    private double getProperty(String property) {
	double value = 0;
	boolean assigned = false;
	GateProperty gp;
	Argument a;
	// getting property
	GateContainer gc = this;

	while (gc != null && assigned == false) {
	    gp = gc.getPropertyObject();
	    if (gp != null) {
		a = gp.getProperty(property);
		if (a != null) {
		    value = ((Complex)a).re();
		    assigned = true;
		}
	    }
	    gc = gc.getParent();
	}
	if (assigned == false) {
	    MathObject cp = Mathlib.getVar("circuit_properties");
	    if (cp != null) {
		a = ((GateProperty)cp).getProperty(property);
		if (a != null) {
		    value = ((Complex)a).re();
		}
	    }
	}
	return value;
    }

}
