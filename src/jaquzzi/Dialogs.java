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

import javax.swing.*;
import javax.swing.event.*;

import mathlib.GateProperty;
import mathlib.Matrix;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

/**
 * class providing static methods to bring up dialog windows used by the jaQuzzi class.
 * @see PhaseInputDialog
 * @see ChooseMatrixDialog
 * @see MatrixInputDialog
 * @see GatePropertyDialog
 * @see CircuiPropertyDialog
 */
public class Dialogs {

    /**
     * brings up the PhaseInputDialog. Parameters are the parent frame and a two element
     * integer array with the nominator and the denominator.
     */
    public static boolean phaseInputDialog(JFrame parent, int[] ratio) {
	PhaseDialog dialog = new PhaseDialog(parent, "Ph(delta)", true, ratio[0], ratio[1]);

	dialog.setSize(dialog.getPreferredSize());
	dialog.setLocation(200, 200);
	dialog.show();

	if (dialog.wasCanceled() == true) {
	    return false;
	}
	else {
	    ratio[0] = dialog.getNominator();
	    ratio[1] = dialog.getDenominator();
	    return true;
	}
    }

    //----------------------------------------------------------------------------------


    /**
     * show a dialog for the input of an abritrary matrix. The matrix object must be
     * given as a parameter. Optionally, another matrix with boolean values can be
     * given to be used as an enabled-mask.
     * @return name of the created variable
     * @see MatrixInputDialog
     */
    public static String mathInputDialog(JFrame parent, String title, String matrixName, Matrix matrix, boolean[][] enabled, boolean unitary) {
	MatrixInputDialog dialog = new MatrixInputDialog(parent, title, true, matrixName, matrix, enabled, unitary);
	dialog.setSize(dialog.getPreferredSize());
	dialog.setLocation(200, 200);
	dialog.show();

	if (dialog.wasCanceled() ==  true) {
	    return null;
	}
	else {
	    return dialog.getMatrixName();
	}
    }


    //----------------------------------------------------------------------------------



    /**
     * brings up the ChooseMatrixDialog. Parameters are the parent frame and the number 
     * of qubits selected.
     * @see ChooseMatrixDialog
     */
    public static String chooseMatrixDialog(JFrame parent, int qubitsSel) {
	ChooseMatrixDialog dialog = new ChooseMatrixDialog(parent, qubitsSel);

	dialog.setSize(dialog.getPreferredSize());
	dialog.setLocation(200, 200);
	dialog.show();

	if(dialog.wasCanceled() == true) {
	    return null;
	}
	else {
	    return dialog.getMatrixName();
	}
    }

    //----------------------------------------------------------------------------------



    /**
     * brings up the GatePropertyDialog. The parameters are parent frame and the 
     * gateName. 
     * @see GatePropertyDialog
     */
    public static GateProperty gatePropertyDialog(JFrame parent, String gateName) {
	GatePropertyDialog dialog = new GatePropertyDialog(parent, gateName);

	dialog.setSize(dialog.getPreferredSize());
	dialog.setLocation(200, 200);
	dialog.show();

	if(dialog.wasCanceled() == true) {
	    return null;
	}
	else {
	    return dialog.getPropertyObject();
	}
    }

    /**
     * brings up the CircuitPropertyDialog. The parameters are parent frame and the 
     * simulation mode. 
     * @see CircuitPropertyDialog
     */
    public static GateProperty circuitPropertyDialog(JFrame parent, int mode) {
	CircuitPropertyDialog dialog = new CircuitPropertyDialog(parent, mode);

	dialog.setSize(dialog.getPreferredSize());
	dialog.setLocation(200, 200);
	dialog.show();

	if(dialog.wasCanceled() == true) {
	    return null;
	}
	else {
	    return dialog.getPropertyObject();
	}
    }

}
