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

import java.io.*;
import javax.swing.*;

public class LOG {
    public static boolean errorFlag = false;
    private static int debuglevel = 0;
    protected static JTextArea output;


    public static void LOG(int level, String str) {
	if (output == null) {
	    if (level <= debuglevel) System.out.println(str);
	}
	else {
	    if (level <= debuglevel) output.append(str+"\n");
	}
    }

    public static void ERROR(String expression, String msg, int pos) {
	if ((errorFlag == false) || (debuglevel > 0)) {
	    errorFlag = true;
	    String str = "";
	    str = str.concat(msg+"\n");
	    str = str.concat(expression+"\n");
	    for (int i = 1; i < pos; i++) {
		    str = str.concat(" ");
	    }
	    if (pos != 0) str = str.concat("^\n");

	    if (output == null)  System.out.println(str);
	    else output.append(str);
	}
    }

    public static void reset() { errorFlag = false; }

    public static void setDebuglevel(int newDebuglevel) {
	debuglevel = newDebuglevel;
    }

    public static int getDebuglevel() {
	return debuglevel;
    }

    public static void setOutputArea(JTextArea newOutput) {
	output = newOutput;
    }

}
