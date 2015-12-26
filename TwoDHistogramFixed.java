// part of Taclet
// author: Ulrike Hager

import java.io.*;
import java.net.*;
import java.awt.*;
import java.awt.geom.*;
import javax.swing.*;
import java.lang.*;
//import javax.swing.*;
//import java.awt.*;
//import java.awt.geom.*;
import java.util.*;
import javax.swing.JPanel;
import java.text.DecimalFormat;

public class TwoDHistogramFixed{
	private final static boolean DEBUG = false;
	private int[][] count;
	int maxCount;
	private double[] xLowBinLimit;
	private int xNumberOfBins, xPaintedBins;
	private int hWidth, hHeight, xOffset, yOffset, xFirst, xLast, yFirst, yLast;
	private double[] yLowBinLimit;
	double xInterval, yInterval;
	int individualWidth, individualHeight;
	private int yNumberOfBins,yPaintedBins;
	double xLow, xHigh, yLow, yHigh;
	private Color[] cPalette;
//	private int intervalCheck;     // if bins too narrow, use half the number of bins
	DecimalFormat oneDecimal,zeroDecimal;
	String ticMark;
	Dimension offDimension;
    Image offImage;
    Graphics offGraphics;
	boolean updated;
	
	public TwoDHistogramFixed(int HWidth, int HHeight, int xOffset, int yOffset, int xBins, double xLow, double xHigh, int yBins, double yLow, double yHigh){
		hWidth = HWidth;
		hHeight =HHeight;
		this.xOffset = xOffset;
		this.yOffset = yOffset;
		this.xLow = xLow;
		this.yLow = yLow;
		this.xHigh = xHigh;
		this.yHigh = yHigh;
		
		xNumberOfBins = xBins; xPaintedBins = xBins;
		xLowBinLimit = new double[xNumberOfBins+1];
		xLowBinLimit[0]=xLow;
		for (int i =1; i<xNumberOfBins+1; i++){
			xLowBinLimit[i]=xLowBinLimit[i-1]+(xHigh-xLow)/((double)xNumberOfBins);
			if (DEBUG) System.err.println("bin: " + i + " xLowBinLimit: " + xLowBinLimit[i] );
		}
		yNumberOfBins = yBins;  yPaintedBins = yBins;
		yLowBinLimit = new double[yNumberOfBins+1];
		yLowBinLimit[0]=yLow;
		for (int i =1; i<yNumberOfBins+1; i++){
			yLowBinLimit[i]=yLowBinLimit[i-1]+(yHigh-yLow)/((double)yNumberOfBins);
			if (DEBUG) System.err.println("bin: " + i + " yLowBinLimit: " + yLowBinLimit[i] );
		}
		count= new int[xNumberOfBins][yNumberOfBins];
		for (int i =0; i < xNumberOfBins; i++){
			for (int j = 0; j < yNumberOfBins; j++){
				count[i][j]=0;
			}
		}
		oneDecimal = new DecimalFormat("0.0");
		zeroDecimal = new DecimalFormat("0");
		cPalette = new Color[50];
		for (int i = 0; i < 25 ; i++){
			cPalette[i] = new Color(0,10*i,250-10*i);
		}
		for (int i = 25; i < 50 ; i++){
			cPalette[i] = new Color(5*i,250-5*i,0);
		}
		maxCount = 0;
			xFirst = 0;
			yFirst = 0;
			xLast = xNumberOfBins;
			yLast = yNumberOfBins;
		xPaintedBins = xLast - xFirst;
		yPaintedBins = yLast - yFirst;
		xInterval = ((double)hWidth ) / ((double)(xPaintedBins));
		individualWidth = (int)xInterval;
		yInterval = ((double)hHeight ) / ((double)(yPaintedBins));
		individualHeight = (int)yInterval;
		if (individualWidth < 1) individualWidth = 1; 
		if (individualHeight < 1) individualHeight = 1; 
		if (DEBUG) System.err.println("init: xFirst: " + xFirst + " xLast: " + xLast + " yFirst: " + yFirst + " yLast: " + yLast);
		updated = false;
		
	}

		
	
	public void updateHistogram(double xValue, double yValue, double xLow, double xHigh, double yLow, double yHigh){ //updateHistogram
//		if (DEBUG) System.err.println("updating: " + xValue + " " + yValue);
		if (Double.isNaN(xValue) || Double.isNaN(yValue)) return;
		if ((xValue < xLowBinLimit[0]) || (xValue >= xLowBinLimit[xNumberOfBins])) return;  
		if ((yValue < yLowBinLimit[0]) || (yValue >= yLowBinLimit[yNumberOfBins])) return;  
		int j = 0;
		while ((xValue>=xLowBinLimit[j+1]) && (j < xNumberOfBins)){
			j++;
		}
		int k = 0;
		while ((yValue>=yLowBinLimit[k+1]) && (k < yNumberOfBins)){
			k++;
		}
		count[j][k]++;
		this.xLow = xLow;
		this.yLow = yLow;
		this.xHigh = xHigh;
		this.yHigh = yHigh;
		updated = true;
	}
	

	void writeData(String fileName){
		try{
			BufferedWriter outFile = new BufferedWriter(new FileWriter(fileName));
		for (int i =0; i<xNumberOfBins; i++){
			for (int j = 0; j < yNumberOfBins; j++){
				if (count[i][j] > 0) outFile.write(xLowBinLimit[i] + "\t" + yLowBinLimit[j] + "\t"+ count[i][j] + "\n");
			}
		}
			outFile.close();
		}catch (IOException e){
			System.out.println("could not open file: " + fileName + "to write");
		}
	}


	public void drawHistogram(Graphics g){
		if (DEBUG) System.out.println("drawHistogram");

		g.translate(xOffset, yOffset);

		if ((!updated) && (maxCount == 0)) return;
		if (updated){
		if (DEBUG) System.err.println("drawing: was updated");
			
			xFirst = 0;
			yFirst = 0;
			xLast = xNumberOfBins;
			yLast = yNumberOfBins;
			if (!Double.isNaN(xLow))
			{
				int j = 0;
				while ((xLow>=xLowBinLimit[j+1]) && (j < xNumberOfBins)){
					j++;
				}
				xFirst = j;
				if (DEBUG) System.out.println("firstPoint determined: " + xFirst);
			}
			
			if (!Double.isNaN(xHigh))
			{
				int j = xNumberOfBins;
				while ((xHigh<xLowBinLimit[j-1]) && (j > 0)){
					j--;
				}
				xLast = j;
				if (DEBUG) System.err.println("lastPoint determined: " + xLast);
			}
			if (!Double.isNaN(yLow))
			{
				int j = 0;
				while ((yLow>=yLowBinLimit[j+1]) && (j < yNumberOfBins)){
					j++;
				}
				yFirst = j;
				if (DEBUG) System.out.println("firstPoint determined: " + yFirst);
			}
			
			if (!Double.isNaN(yHigh))
			{
				int j = yNumberOfBins;
				while ((yHigh<yLowBinLimit[j-1]) && (j > 0)){
					j--;
				}
				yLast = j;
				if (DEBUG) System.out.println("lastPoint determined: " + yLast);
			}

			maxCount = 0;
			for (int i =xFirst; i<xLast; i++){
				for (int j = yFirst; j < yLast; j++){
					if (maxCount < count[i][j]) maxCount = count[i][j]; 
				}
			}
			if (maxCount == 0) return;

			xPaintedBins = xLast - xFirst;
			yPaintedBins = yLast - yFirst;
			xInterval = ((double)hWidth ) / ((double)(xPaintedBins));
			individualWidth = (int)xInterval;
			yInterval = ((double)hHeight ) / ((double)(yPaintedBins));
			individualHeight = (int)yInterval;
			if (individualWidth < 1) individualWidth = 1; 
			if (individualHeight < 1) individualHeight = 1; 
		if (DEBUG) System.err.println("updated: xFirst: " + xFirst + " xLast: " + xLast + " yFirst: " + yFirst + " yLast: " + yLast);
		}
		
		g.setColor(Color.BLACK);
		g.drawLine(0, hHeight, hWidth, hHeight);
		g.drawLine(0, hHeight, 0, 0);

		g.setFont(new Font("sansserif", Font.PLAIN, 8));

		double xticsCount =  ((double)xPaintedBins/10.0);
		double xtics = (double)xFirst;
		while (xtics <= xLast){
			int xPos = (int)((xtics-(double)xFirst) * xInterval);
			g.drawString("" + oneDecimal.format(xLowBinLimit[(int)xtics]) + "",xPos,(hHeight+10));
			xtics += xticsCount;
		}
		
		double yticsCount =  ((double)yPaintedBins/10.0);
		double ytics = (double)yFirst;
		while (ytics <= yLast){
			int yPos = (int)((ytics-(double)yFirst) * yInterval);
			g.drawString("" + oneDecimal.format(yLowBinLimit[(int)ytics]) + "",-20,hHeight-yPos);
			ytics += yticsCount;
		}

		for (int i =xFirst; i<xLast; i++){
			for (int j = yFirst; j < yLast; j++){
				if (count[i][j] > 0){
					int colour = (int)(((double)count[i][j])/((double)maxCount) * (double)(cPalette.length-1));
					if (colour>cPalette.length-1) colour = cPalette.length-1;
					g.setColor(cPalette[colour]);
					g.fillRect((int)((i+1-xFirst)*xInterval),(int)(hHeight-(j+1-yFirst)*yInterval),individualWidth, individualHeight);
				}
			}
		}

		g.setColor(Color.WHITE);
		g.fillRect((int)(hWidth-20),(int)(hHeight-10), 30, 10);
		g.setColor(Color.BLACK);
		g.drawRect((int)(hWidth-20),(int)(hHeight-10), 30, 10);
		g.setColor(cPalette[49]);
		g.fillRect((int)(hWidth-19), (int)(hHeight-6), individualWidth,individualHeight);
		g.setColor(Color.BLACK);
		g.drawString(String.valueOf(maxCount),(int)(hWidth-18+individualWidth), hHeight-2);
		updated = false;

	}

		
}

