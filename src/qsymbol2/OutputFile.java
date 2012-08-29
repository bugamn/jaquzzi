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

import java.io.*;

public class OutputFile {

    String fileName;
    PrintWriter pw;

    public OutputFile (String name) {
	fileName = new String(name);
	try {
	    pw = new PrintWriter(new FileWriter(fileName));
	} catch (IOException ioe) {
	    System.err.println("Cannot open file: " + fileName);
	}
    }

    public void print (String str) {
	if (pw != null)
	    pw.print(str);
    }

    public void println (String str) {
	if (pw != null)
	    pw.println(str);
    }

    public void println (int i) {
	if (pw != null)
	    pw.println(i);
    }

    public void close () {
	if (pw != null)
	    pw.close();
    }

    public static void main (String[] args) {
	OutputFile of = new OutputFile("test.data");
	of.println(10 + "this" + " is only a test");
	of.println(10);
	of.close();
    }

}

