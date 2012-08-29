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
 * this class represents a data structure to provide the QubitSelectionModel with the
 * capability of tracking the order of how the rows got selected.
 * <p>The structure works as follows: in principle it is a queue, that is the element
 * first put into it is the first element which gets out. But if an element is added
 * which is already in the queue, it is removed from the higher priority position
 * (earlier in the queue) and added to the end</p>
 */
public class PriorityQueue {
    protected Hashtable keys;
    protected Vector elements;

    /**
     * create an empty queue
     */
    public PriorityQueue() {
	keys = new Hashtable();
	elements = new Vector();
    }

    /**
     * add an element
     */
    public void add(int index){
	Object o = keys.get(""+index);
	if (o != null) {
	    elements.remove(o);
	    elements.add(o);
	}
	else {
	    o = new Integer(index);
	    keys.put(""+index, o);
	    elements.add(o);
	}
    }

    /**
     * remove an element
     */
    public boolean remove(int index) {
	Object o = keys.get(""+index);
	if (o != null) 
	    return elements.remove(o);
	return false;
    }

    /**
     * returns the queue in the order of highest priority (earliest selection)
     */
    public Enumeration elements() {
	return elements.elements();
    }

    public Vector getQueue() {
	return elements;
    }

}
