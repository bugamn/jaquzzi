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
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.tree.*;

import mathlib.Complex;
import mathlib.Gate;
import mathlib.LOG;
import mathlib.MathObject;
import mathlib.MathObjectConstraints;
import mathlib.Mathlib;
import mathlib.MathlibEvent;
import mathlib.MathlibEventListener;
import mathlib.Matrix;
import mathlib.VarAuthority;

/**
 * class implementing a panel which lists the variables of a Mathlib object and
 * allows to access the math engine directly.
 */
public class QVarTree extends JPanel implements ActionListener, MathlibEventListener {

    JTextField inputLine;
    JTextArea outputArea;
    JTree tree;
    DefaultTreeModel model;
    JSplitPane splitPane;

    protected Mathlib math;

    /** is responsible for grouping of variables */
    protected VarAuthority varAuthority;

    DefaultMutableTreeNode mathlib;
    DefaultMutableTreeNode functions;
    /** references to the nodes for easy adding & removing */
    private Hashtable nodes;

    /**
     * create the panel for a given mathlib object
     */
    public QVarTree(Mathlib math) {
	this.math = math;
	math.addMathlibEventListener(this);
	varAuthority = new VarAuthority(math, true);

	nodes = new Hashtable();

	// root 
	mathlib = new DefaultMutableTreeNode("mathlib");
	functions = new DefaultMutableTreeNode("available functions");

	String nodeName;
	DefaultMutableTreeNode node;


	// categories
	MathObjectConstraints moc = new MathObjectConstraints();
	moc.subCategory = new String("current circuit");
	moc.mathClass = Gate.class;
	varAuthority.registerCategory("current circuit", moc);

	moc.subCategory = null;
	moc.mathClass = Matrix.class;
	moc.m = moc.n = 2;
	moc.expression = new String("self*self'\"==one(2)");
	varAuthority.registerCategory("simple gates", moc);

	moc.subCategory = new String("system");
	moc.m = moc.n = -1; moc.expression = null; moc.mathClass = null;
	varAuthority.registerCategory("system", moc);

	moc.subCategory = null;
	moc.mathClass = Complex.class;
	varAuthority.registerCategory("scalars", moc);

	moc.mathClass = null;
	varAuthority.registerCategory("others", moc);

	// add nodes
	tree = new JTree(mathlib);

	mathlib.add(buildCategory("current circuit"));
	mathlib.add(buildCategory("simple gates"));
	mathlib.add(buildCategory("system"));
	mathlib.add(buildCategory("scalars"));
	mathlib.add(buildCategory("others"));

	mathlib.add(functions);


	for (Enumeration e = math.getFunctions(); e.hasMoreElements();) {
	    nodeName = (String)e.nextElement();
	    node = new DefaultMutableTreeNode(nodeName);
	    functions.add(node);
	    nodes.put(nodeName, node);
	}

	GridBagLayout gridBag = new GridBagLayout();
	setLayout(gridBag);
	GridBagConstraints c = new GridBagConstraints();
	c.gridwidth = GridBagConstraints.REMAINDER;

	MouseListener ml = new MouseAdapter() {
		public void mouseClicked(MouseEvent e) {
		    TreePath selPath = tree.getPathForLocation(e.getX(), e.getY());
		    if(selPath != null) {
			if(e.getClickCount() == 1) {
			    singleClick(selPath);
			}
			else if(e.getClickCount() == 2) {
			    doubleClick(selPath);
			}
		    }
		}
	    };
	tree.addMouseListener(ml);

	tree.expandRow(0);
	tree.setVisibleRowCount(10);
	// geometrics

	JScrollPane sp = new JScrollPane(tree);
        c.fill = GridBagConstraints.BOTH;
	c.weightx = 1;
	c.weighty = 1;

 	outputArea = new JTextArea(6,0);
	outputArea.setEditable(false);
	outputArea.setLineWrap(true);
	JScrollPane scrollPane = new JScrollPane(outputArea);
	LOG.setOutputArea(outputArea);

	splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, sp, scrollPane);
	splitPane.setDividerSize(6);

	gridBag.setConstraints(splitPane, c);
	add(splitPane);

 
	JLabel label = new JLabel(">>");
	c.gridwidth = GridBagConstraints.RELATIVE;
	c.weightx = 0;
	c.weighty = 0;
	gridBag.setConstraints(label, c);
	add(label);

	inputLine = new JTextField();
	inputLine.addActionListener(this);
	c.weightx = 1;
	c.weighty = 0;
	c.gridwidth = GridBagConstraints.REMAINDER;
        c.fill = GridBagConstraints.HORIZONTAL;
        gridBag.setConstraints(inputLine, c);
        add(inputLine);
    }

    /**
     * implements the functionality for a single click to a tree element
     */
    public void singleClick(TreePath path) {
	String var = ((String)((DefaultMutableTreeNode)path.getLastPathComponent()).getUserObject());
	MathObject m = math.getVar(var);
	if (m != null) outputArea.append(var+" = \n"+m.toString()+"\n");
    }

    /**
     * implements the functionality for a double click to a tree element. right now
     * it does nothing
     */
    public void doubleClick(TreePath path) {
    }

    /**
     * method called when the enter button was pressed in the command line
     */
    public void actionPerformed(ActionEvent evt) {
        String expr = inputLine.getText();
	try {
	    String var = math.evaluateExpression(expr);
	    outputArea.append(var+" = \n"+math.getVar(var).toString()+"\n");
	} catch (Exception e) {
	    //	    outputArea.append("exception: " + e.getMessage() + "\n");
	}
        inputLine.selectAll();
    }

    /**
     * add a node to the tree in case of a MathlibEvent.
     */
    public void addVariable(MathlibEvent e) {
	if (e.getMathObject() != null) {
	    String category = varAuthority.whichCategory(e.getObjectName(), e.getCategory());
	    DefaultMutableTreeNode cat = (DefaultMutableTreeNode)nodes.get(category);
	    model = (DefaultTreeModel)tree.getModel();

	    DefaultMutableTreeNode node = new DefaultMutableTreeNode(e.getObjectName());
	    nodes.put(e.getObjectName(), node);	

	    model.insertNodeInto(node, cat, model.getChildCount(cat));
	}
    }

    /**
     * removes a node from the tree in case of a MathlibEvent.
     */
    public void removeVariable(MathlibEvent e) {
	DefaultMutableTreeNode node=(DefaultMutableTreeNode)nodes.get(e.getObjectName());
	model = (DefaultTreeModel)tree.getModel();
	if (node != null) model.removeNodeFromParent(node);
	nodes.remove(e.getObjectName());
    }

    /**
     * changes the tree in case of a MathlibEvent
     */
    public void changeVariable(MathlibEvent e) {
	if (e.getAction() == MathlibEvent.CHANGE_NAME) {
	    DefaultMutableTreeNode node=(DefaultMutableTreeNode)nodes.get(e.getObjectName());
	    node.setUserObject(e.getNewName());
	    nodes.remove(e.getObjectName());
	    nodes.put(e.getNewName(), node);
	    
	    model = (DefaultTreeModel)tree.getModel();
	    model.nodeChanged(node);

	}
	else if (e.getAction() == MathlibEvent.CHANGE_CATEGORY) {
	    removeVariable(new MathlibEvent(e.getObjectName(), null, MathlibEvent.REMOVE));
	    addVariable(new MathlibEvent(e.getObjectName(), null, e.getNewName(), e.getMathObject(), MathlibEvent.ADD));
	}
	else if (e.getAction() == MathlibEvent.CHANGE) {
	    removeVariable(new MathlibEvent(e.getObjectName(), null, MathlibEvent.REMOVE));
	    addVariable(new MathlibEvent(e.getObjectName(), null, e.getCategory(), e.getMathObject(), MathlibEvent.ADD));
	}

    }

    /**
     * builds the children of a category node and returns the category node.
     * @return category node
     */
    public DefaultMutableTreeNode buildCategory(String category){
	// build category node
	DefaultMutableTreeNode cat = new DefaultMutableTreeNode(category);
	nodes.put(category, cat);

	// build childs
	Enumeration e = varAuthority.getElementsInCategory(category);
	DefaultMutableTreeNode node;
	String name;

	model = (DefaultTreeModel)tree.getModel();
	while (e.hasMoreElements() ) {
	    name = (String)e.nextElement();
	    node = new DefaultMutableTreeNode(name);
	    nodes.put(name, node);

	    // insert to tree
	    model.insertNodeInto(node, cat, model.getChildCount(cat));
	}
	return cat;
    }



}
