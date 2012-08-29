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

public class InputFile {

    String fileName;
    BufferedReader br;
    StringBuffer buf = new StringBuffer();
    StringBuffer buf2 = new StringBuffer();
    boolean endOfFile_;
    int lineNumber_;

    public InputFile (String name) {
	fileName = new String(name);
	try {
	    br = new BufferedReader(new FileReader(fileName));
	} catch (IOException ioe) {
	    error("cannot open file: " + fileName);
	}
	endOfFile_ = false;
	lineNumber_ = 0;
    }

    public String currentLine () {
	return buf.toString();
    }

    public void getLine () {
	String line = null;
	try {
	    line = br.readLine();
	    int len = line.length();
	    buf.setLength(len);
	    for (int i = 0; i < len; i++)
		buf.setCharAt(i, line.charAt(i));
	    buf.setLength(len);
	} catch (IOException e) {
	    error("getLine() error reading line from" + fileName);
	    buf.setLength(0);
	} catch (Exception e) {
	    buf.setLength(0);
	}
	if (line == null)
	    endOfFile_ = true;
	else
	    ++lineNumber_;
    }

    public boolean endOfFile () {
	return endOfFile_;
    }

    public int lineNumber () {
	return lineNumber_;
    }

    public double doubleAtColumn (int column) {
	double d = 0;
	int i = column - 1;
	int j = 0;
	buf2.setLength(buf.length());
	try {
	    int len = buf.length();
	    while (true) {
		char c = ' ';
		while (c == ' ' || c == '\t' && i <  len)
		    c = buf.charAt(i++);
		while (c != ' ' && c != '\t') {
		    buf2.setCharAt(j++, c);
		    if (i >= len)
			break;
		    c = buf.charAt(i++);
		}
		buf2.setLength(j);
		break;
	    }
	    d = new Double(buf2.toString()).doubleValue();
	} catch (NumberFormatException nfe) {
	    error("doubleAtColumn(" + column + ") error converting "
		  + buf2.toString());
	} catch (IndexOutOfBoundsException ioobe) {
	    error("bad index i = " + i + " j = " + j);
	}
	return d;
    }

    public String stringNumber (int number) {
	if (number < 1) {
	    error("bad item number = " + number + " must be > 0");
	    return null;
	}
	buf2.setLength(buf.length());
	int i = 0;
	int j = 0;
	try {
	    int n = 1;
	    int len = buf.length();
	    char c = buf.charAt(i++);
	    while (n < number) {
		while (c == ' ' || c == '\t')
		    c = buf.charAt(i++);
		while (c != ' ' && c != '\t')
		    c = buf.charAt(i++);
		++n;
	    }
	    while (c == ' ' || c == '\t' && i <  len)
		c = buf.charAt(i++);
	    while (c != ' ' && c != '\t' && i <  len) {
		buf2.setCharAt(j++, c);
		c = buf.charAt(i++);
	    }
	    buf2.setLength(j);
	    return buf2.toString();
	} catch (IndexOutOfBoundsException ioobe) {
	    error("end of line at column " + i + " searching for item " +
		  number);
	}
	return null;
    }
    
    public void close () {
	try {
	    br.close();
	} catch (IOException ioe) { }
    }

    private void error (String message) {
	System.err.println("comphys.InputFile: " + message);
    }

}

