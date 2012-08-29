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
import java.awt.*;

public class Histogram extends Chart {
    protected int valPerBin;

    public Histogram() {
	valPerBin = 0;
	setPlotStyle(BAR_STYLE);
    }

    public void setValuesPerBin(int valPerBin) {
	this.valPerBin = valPerBin;
    }

    public int getValuesPerBin() {
	return valPerBin;
    }

    public double getPlotValue(int index) {
	if (getValuesPerBin() > 1) {
	    double yValue = 0;

	    // bin the values
	    for (int j = 0; j < getValuesPerBin(); j++) {
		if (index*getValuesPerBin()+j < yValues.size())
		    yValue += ((Double)yValues.elementAt(index*getValuesPerBin()+j)).doubleValue();
	    }
	    return yValue;

	}
	else return ((Double)yValues.elementAt(index)).doubleValue();
    }

    protected void resizeChart(Dimension d, Insets insets) {
	int width = d.width-insets.left-insets.right;
	int number = (yValues.size() == 0)? width: width/yValues.size();

	if (number <= 0) {
	    int oldValuesPerBin = getValuesPerBin();
	    setPixelPerTick(1);
	    setValuesPerBin(new Double(Math.ceil(1.0*yValues.size()/width)).intValue());
	    setXAxisRange(0, new Double(Math.ceil(1.0*yValues.size()/getValuesPerBin())).intValue()-1);

	}
	else {
	    setValuesPerBin(1);
	    setPixelPerTick(number);
	    setXAxisRange(0, yValues.size()-1);
	}
    }

    public void plotLegend(Graphics2D g2, Point origin) {
	g2.setFont(g2.getFont().deriveFont(10));
	if (getValuesPerBin() > 1)
	g2.drawString("bin size: "+getValuesPerBin() , origin.x, origin.y);
	
    }

}

