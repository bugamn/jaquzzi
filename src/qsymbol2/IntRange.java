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

/**
 * simple class providing a range of int numbers. This class is used by the Chart 
 * class. 
 * @see DoubleRange
 * @see Chart
 */
public class IntRange {
    protected int lowerBound;
    protected int upperBound;

    public IntRange() {
	this(0,0);
    }

    /**
     * creates a range object with the specified range.
     */
    public IntRange(int lowerBound, int upperBound) {
	this.lowerBound = lowerBound;
	this.upperBound = upperBound;
    }

    /**
     * clones a range object
     */
    public IntRange(IntRange range) {
	this.lowerBound = range.getLowerBound();
	this.upperBound = range.getUpperBound();
    }

    /**
     * returns the lower bound
     */
    public int getLowerBound() {
	return lowerBound;
    }

    /**
     * returns the upper bound
     */
    public int getUpperBound() {
	return upperBound;
    }

    public void setLowerBound(int lowerBound) {
	this.lowerBound = lowerBound;
    }

    public void setUpperBound(int upperBound) {
	this.upperBound = upperBound;
    }

    public void setRange(int lowerBound, int upperBound) {
	this.lowerBound = lowerBound;
	this.upperBound = upperBound;
    }

    public int getWidth() {
	return upperBound-lowerBound;
    }

}
