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
 * Class which allows to dump the components of a braket.
 *
 */
public class DumpMap extends MathMap {
    private String data;

    /**
     * creates a new dump map
     */
    public DumpMap() {
	data = new String("dump");
    }

    /**
     * returns the components of a braket as a StringArgument object
     */
    public MathObject apply(MathObject o) {
	if (o instanceof Braket) {
	    String dump = new String("");
	    Braket b = (Braket)o;
	    for (int i = 0; i < b.dimension-1; i++) {
		dump = dump.concat( ((b.data[i]== null)?"0":b.data[i].toString()) + " | ");
	    }
	    dump = dump.concat(((b.data[b.dimension-1]== null)?"0":b.data[b.dimension-1].toString())+"\n");
	    dump = dump.concat("transpose: " + b.transpose);
	    return new StringArgument(dump);
	}

	LOG.LOG(0, toString() + " not defined for argument");
	return null;
    }

    /**
     * returns the string representation of the dump map
     */
    public String toString() {
	return data;
    }

}
