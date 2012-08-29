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

import java.awt.*;
import java.awt.geom.*;
import java.util.*;
import javax.swing.*;

import mathlib.Easy;

public class Chart extends JPanel {
    protected DoubleRange yAxisRange;
    protected IntRange xAxisRange;

    protected Vector yValues;

    protected int plotStyle = 2;
    protected int pixelPerTick=10;
    protected int minorTick = 2;
    protected int majorTick = 4; 

    public Chart() {
	yValues = new Vector();
	yAxisRange = new DoubleRange();
	xAxisRange = new IntRange();
    }

    public Chart(Vector yValues, DoubleRange yRange) {
	this.yValues = yValues;
	this.yAxisRange = new DoubleRange(yRange);
	this.xAxisRange = new IntRange(0, yValues.size()-1);
    }


    // range methods

    public void setYAxisRange(double yMin, double yMax) {
	yAxisRange.setRange(yMin, yMax);
    }

    public void setXAxisRange(int xMin, int xMax) {
	xAxisRange.setRange(xMin, xMax);
    }

    public DoubleRange getYAxisRange() {
	return yAxisRange;
    }

    public IntRange getXAxisRange() {
	return xAxisRange;
    }


    // data methods

    public int getYDataCount() {
	return yValues.size();
    }

    protected int getYPlotCount() {
	return getYDataCount();
    }

    /**
     * returns the minimum x value in the current range
     */
    public double getMinXValue() {
	return 0;
    }

    /**
     * returns the maximum x value in the current range
     */
    public double getMaxXValue() {
	return 0;
    }

    /**
     * returns the minimum y value in the current range
     */
    public double getMinYValue() {
	return 0;
    }

    /**
     * returns the maximum y value in the current range
     */
    public double getMaxYValue() {
	return 0;
    }

    public void setYVector(Vector y) {
	yValues = y;
    }

    public double getPlotValue(int index) {
	return ((Double)yValues.elementAt(getXAxisRange().getLowerBound()+index)).doubleValue();
    }

    public Dimension getPreferredDimension() {
	return new Dimension(40+getPixelPerTick()*getYDataCount(),200);
    }


    // paint methods

    public final static int POINT_STYLE = 0;
    public final static int LINE_STYLE = 1;
    public final static int BAR_STYLE = 2;

    /**
     * sets the plot style, can be one of the three POINT_STYLE, LINE_STYLE, BAR_STYLE
     */
    public void setPlotStyle(int style) {
	this.plotStyle = style;
    }

    public void setPixelPerTick(int pixelPerTick) {
	this.pixelPerTick = pixelPerTick;
    }

    public int getPixelPerTick() {
	return pixelPerTick;
    }

    private Insets getDrawInsets() {
	return new Insets(10, 40, 30, 20);
    }

    public String getAxisTick(int pos) {
	return ""+(xAxisRange.getLowerBound()+pos);
    }

    protected void resizeChart(Dimension d, Insets insets) {
	int width = d.width-insets.left-insets.right;
	int number = (xAxisRange.getWidth() == 0)? width: width/(xAxisRange.getWidth());
	setPixelPerTick((number <= 0)? 1: number);
    }

    protected void plotXAxis(Graphics2D g2, int zero) {
	Dimension d = getSize();
	Insets insets = getDrawInsets();
	g2.draw(new Line2D.Double(insets.left, zero,
				  d.width-insets.right, zero));

	g2.setFont(g2.getFont().deriveFont(10));
	FontMetrics fm = g2.getFontMetrics();

	int w = fm.stringWidth(getAxisTick(xAxisRange.getWidth()));
	String display;
	int dist = getPixelPerTick();

	int majorTickAt = (dist >= w) ? 1: new Double(Math.ceil(1.0*w/dist)).intValue();

	for (int i = xAxisRange.getLowerBound(); i <= xAxisRange.getUpperBound(); i++) {
	    if (dist >= 5)
		g2.draw(new Line2D.Double(insets.left+i*dist, 
					  zero-minorTick, 
					  insets.left+i*dist, 
					  zero+minorTick));

	    // major tick
	    if ((i+1) % majorTickAt == 0) {
		display = getAxisTick(i);
		w = fm.stringWidth(display);
		g2.drawString(display, insets.left + i*dist - w/2, zero+18);
	    
		// draw intersections
		g2.draw(new Line2D.Double(insets.left+i*dist, 
					  zero-majorTick, 
					  insets.left+i*dist, 
					  zero+majorTick));
	    }
	}
    }

    protected int plotYAxis(Graphics2D g2) {
	Insets insets = getDrawInsets();
	Dimension d = getSize();
	g2.setPaint(Color.black);
	g2.draw(new Line2D.Double(insets.left, insets.top, insets.left, d.height-insets.bottom));

	double max = getYAxisRange().getUpperBound();
	double min = getYAxisRange().getLowerBound();
	double range = Math.abs(max)+Math.abs(min);

	int zero;
	zero = -(new Double(Math.ceil((d.height-insets.top-insets.bottom)/range*Math.abs(min))).intValue())+(d.height-insets.bottom);


	// intersections!
	g2.setFont(g2.getFont().deriveFont(10));
	double yDist = (d.height-(insets.top+insets.bottom))/5.0;
	double gap = range/5;

	// zero tick
	g2.draw(new Line2D.Double(insets.left-majorTick, zero, 
				  insets.left+majorTick, zero));
	g2.drawString("0", 2, zero);

	// upwards
	int y = 1;
	while ((zero-y*yDist) >= insets.top) {
	    g2.draw(new Line2D.Double(insets.left-majorTick, zero-yDist*y, 
				      insets.left+majorTick, zero-yDist*y));
	    g2.drawString(""+Easy.format(y*gap,3), 2, 
			  new Double(Math.round(zero-y*yDist)).intValue());
	    y++;
	}

	// downwards
	y = 1;
	while ((zero+y*yDist) <= (d.height-insets.bottom)) {
	    g2.draw(new Line2D.Double(insets.left-majorTick, zero+yDist*y, 
				      insets.left+majorTick, zero+yDist*y));
	    g2.drawString(""+Easy.format(-y*gap,3), 2, 
			  new Double(Math.round(zero+y*yDist)).intValue());
	    y++;
	}
	return zero;
    }

    public void plotLegend(Graphics2D g2, Point origin) {

    }

    public void paint(Graphics g) {
	Graphics2D g2 = (Graphics2D)g;
	Dimension d = getSize();
	Insets insets = getDrawInsets();
	resizeChart(d, insets);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
			    RenderingHints.VALUE_ANTIALIAS_ON);

	// clear background
      	g2.setPaint(Color.white);
	g2.fill(new Rectangle2D.Double(0, 0, d.width, d.height));

	// axes
	int zero = plotYAxis(g2);
	plotXAxis(g2, zero);

	// data
	g2.setPaint(Color.red);
	int barWidth = getPixelPerTick();
	barWidth = ((barWidth >= 5) ? barWidth/2 : 1);

	int drawHeight = d.height-(insets.bottom+insets.top);
	int relY = 0;
	int dist = getPixelPerTick();

	g2.setPaint(Color.blue);
	plotLegend(g2, new Point(d.width-insets.right-100, insets.top));
	g2.setPaint(Color.red);

	for (int i = 0; i <= xAxisRange.getWidth(); i++) {
	    relY = scale(zero-insets.top, drawHeight, getPlotValue(i)); 
	    switch (plotStyle) {
	    case POINT_STYLE: {
		    g2.draw(new Line2D.Double(insets.left+i*dist,zero+relY,insets.left+i*dist,zero+relY));
		    break;
	    }
	    case LINE_STYLE: 
	    case BAR_STYLE: {
		if (relY <= 0)
		    g2.fill(new Rectangle2D.Double(insets.left+i*dist-barWidth/2, 
						   zero+relY, barWidth, -relY));
		else 
		    g2.fill(new Rectangle2D.Double(insets.left+i*dist-barWidth/2, 
						   zero, barWidth, relY));
					       
	    }
	    }
	}

    }

    protected int scale(int relZero, int drawHeight, double value) {
	DoubleRange range = getYAxisRange();
	if (value >= range.getUpperBound()) return -relZero;
	else if (value <= range.getLowerBound()) return drawHeight-relZero;

	return new Double(-Math.ceil(drawHeight/getYAxisRange().getWidth()*value)).intValue();
    }



}
