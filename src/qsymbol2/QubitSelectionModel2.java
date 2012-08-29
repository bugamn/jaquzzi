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

/*
 * @(#)DefaultListSelectionModel.java	1.55 00/02/02
 *
 * Copyright 1997-2000 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Sun Microsystems, Inc.  
 * Use is subject to license terms.
 * 
 */

package qsymbol2;

import java.util.EventListener;
import java.util.BitSet;
import java.util.Vector;
import java.io.Serializable;

import javax.swing.event.*;
import javax.swing.*;

/**
 * This class copies a lot of the code of the original DefaultSelectionModel. It is
 * not necessarily the fine art of object oriented programming, but the changes
 * @version 1.55 02/02/00
 * @author Philip Milne
 * @author Hans Muller
 * @see ListSelectionModel */

public class QubitSelectionModel2 extends DefaultListSelectionModel
{
    protected PriorityQueue priorityQueue = new PriorityQueue();


    // Sets the state at this index and update all relevant state.
    private void set(int r) {
	System.out.println("alrighty..");
	if (value.get(r)) {
	    return;
	}
	priorityQueue.add(r);
	value.set(r);
        markAsDirty(r);

        // Update minimum and maximum indices
        minIndex = Math.min(minIndex, r);
        maxIndex = Math.max(maxIndex, r);
    }

    // Clears the state at this index and update all relevant state.
    private void clear(int r) {
	System.out.println("alrighty..2");
	if (!value.get(r)) {
	    return;
	}
	priorityQueue.remove(r);
	value.clear(r);
        markAsDirty(r);

        // Update minimum and maximum indices
        /*
           If (r > minIndex) the minimum has not changed.
           The case (r < minIndex) is not possible because r'th value was set.
           We only need to check for the case when lowest entry has been cleared,
           and in this case we need to search for the first value set above it.
	*/
	if (r == minIndex) {
	    for(minIndex = minIndex + 1; minIndex <= maxIndex; minIndex++) {
	        if (value.get(minIndex)) {
	            break;
	        }
	    }
	}
        /*
           If (r < maxIndex) the maximum has not changed.
           The case (r > maxIndex) is not possible because r'th value was set.
           We only need to check for the case when highest entry has been cleared,
           and in this case we need to search for the first value set below it.
	*/
	if (r == maxIndex) {
	    for(maxIndex = maxIndex - 1; minIndex <= maxIndex; maxIndex--) {
	        if (value.get(maxIndex)) {
	            break;
	        }
	    }
	}
	/* Performance note: This method is called from inside a loop in
	   changeSelection() but we will only iterate in the loops
	   above on the basis of one iteration per deselected cell - in total.
	   Ie. the next time this method is called the work of the previous
	   deselection will not be repeated.

	   We also don't need to worry about the case when the min and max
	   values are in their unassigned states. This cannot happen because
	   this method's initial check ensures that the selection was not empty
	   and therefore that the minIndex and maxIndex had 'real' values.

	   If we have cleared the whole selection, set the minIndex and maxIndex
	   to their cannonical values so that the next set command always works
	   just by using Math.min and Math.max.
	*/
        if (isSelectionEmpty()) {
            minIndex = MAX;
            maxIndex = MIN;
        }
    }


    public Vector getIndicesByPriority() {
	return priorityQueue.getQueue();
    }


}

