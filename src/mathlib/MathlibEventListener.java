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
 * this interface is implemented by classes who would like to be notified about
 * variable changes in the Mathlib class.
 * @see Mathlib
 * @see MathlibEvent
 * @see ComputationEventListener
 */
public interface MathlibEventListener {

    /**
     * method called when a variable is added
     */
    public void addVariable(MathlibEvent e);
    /**
     * method called when a variable is removed
     */
    public void removeVariable(MathlibEvent e);
    /**
     * method called when a variable changes it's value,name or category
     */
    public void changeVariable(MathlibEvent e);

}
