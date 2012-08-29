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

package jaquzzi;

import java.io.File;
import javax.swing.filechooser.*;

/**
 * class implementing a file filter. Right now it implements a filter for *.jaq files as
 * well arbitray extensions.
 */
public class JaqFileFilter extends FileFilter {

    String ext;
    String description;

    /**
     * create a filter for *.jaq files
     */
    public JaqFileFilter() {
	super();
	ext = "jaq";
	description = "jaQuzzi gate circuits (*.jaq)";
    }

    /**
     * create a filter for a given extension with a given description
     */
    public JaqFileFilter(String ext, String description) {
	super();
	this.ext = ext;
	this.description = description;
    }

    /**
     * main method to accept a file
     */
    public boolean accept(File f) {
	if(f != null) {
	    if(f.isDirectory()) {
		return true;
	    }
	    if (ext.equals(getExtension(f)))
		return true;
	}
	return false;
    }

    /**
     * returns the description of the filter
     */
    public String getDescription() {
	return description;
    }

    /**
     * returns the extension of a given file
     */
    public String getExtension(File f) {
	if(f != null) {
	    String filename = f.getName();
	    int i = filename.lastIndexOf('.');
	    if(i>0 && i<filename.length()-1) {
		return filename.substring(i+1).toLowerCase();
	    };
	}
	return null;
    }

}
