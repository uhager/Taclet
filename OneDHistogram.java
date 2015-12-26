// part of Taclet
// author: Ulrike Hager

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.text.DecimalFormat;

public class OneDHistogram {
	private static final boolean DEBUG = false;
	private boolean externalLimits;
	private int[] count;
	private double[] lowBinLimit;
	ArrayList<Double> xArray;
	private double xMin, xMax, binSize;
	private int numberOfBins;	
	Color sectorColour;
	DecimalFormat oneDecimal;
	int notPaintedSince;

	public OneDHistogram(Color aColour, int binNumber){
		externalLimits=false;
		numberOfBins = binNumber;
		count= new int[numberOfBins];
		lowBinLimit = new double[numberOfBins+1];
		for (int i = 0; i<lowBinLimit.length; i++){
			lowBinLimit[i]=0;
		}
		xMin=-100;
		xMax=-100;
		xArray= new ArrayList<Double>();
		sectorColour=aColour;
		oneDecimal = new DecimalFormat("0.0");
	}

	private void switchToSet(double lowLimit, double highLimit){
		externalLimits = true;
		lowBinLimit[0]=lowLimit;
		lowBinLimit[numberOfBins]=highLimit;
		binSize = (highLimit-lowLimit)/numberOfBins ;
		fillBins();
	}

	
	public void updateHistogram(double xValue) {
		if (Double.isNaN(xValue)) return;
		xArray.add(xValue);
		notPaintedSince++;
	}

	private void determineBinning(double lowLimit, double highLimit){
		if (DEBUG) System.out.println("determine binning");
		double xValue = ((Double)xArray.get(xArray.size()-1)).doubleValue();
		int check = 0;
		if (Double.isNaN(lowLimit) || Double.isNaN(highLimit)) check +=1;
		if ((xValue > lowBinLimit[numberOfBins]) || (xValue < lowBinLimit[0])) check += 10;
		if (externalLimits) check += 100;
		switch(check){
		case 0:
		case 10:
 			switchToSet(lowLimit,highLimit);
			break;
		case 1:
		case 100:
			updateCounts(xValue);
			break;
		case 11:
		case 101:
		case 111:
			externalLimits = false;
			sortBins();
			break;
		case 110:
			break;

		}
	}

	private void updateCounts(double xValue){
		if (DEBUG) System.out.println("update counts");
		int j = 0;
		while ((xValue>=lowBinLimit[j+1]) && (j < numberOfBins)){
			j++;
		}
		count[j]++; 
	}
		
	
		
	private void sortBins(){
		if (DEBUG) System.out.println("rebinning");
		count= new int[numberOfBins];
		lowBinLimit = new double[numberOfBins+1];
		xMin=((Double)xArray.get(0)).doubleValue();
		xMax=xMin;
		if (xArray.size()>1){
			for (int i =1 ; i<xArray.size(); i++){
				double xValue=((Double)xArray.get(i)).doubleValue();
				if (DEBUG) System.out.println("arrayPos: " + i + " xValue: " + xValue );
				if (xValue < xMin) xMin = xValue;
				if (xValue > xMax) xMax = xValue;
			}
		}
		if (xMax == xMin){
			binSize = 10.0/((double)numberOfBins);
			lowBinLimit[0]=xMin-5.0;
		}
		else {
			binSize = (xMax-xMin+((xMax-xMin)/4))/((double)numberOfBins);
			lowBinLimit[0]=xMin-((xMax-xMin)/8);
		}
		fillBins();
	}

	public void fillBins(){
		if (DEBUG) System.out.println("fillBins, lowest bin: " + lowBinLimit[0] + " binSize: " + binSize);
		for (int i =1; i<numberOfBins+1; i++){
			lowBinLimit[i]=lowBinLimit[i-1]+binSize;
			if (DEBUG) System.out.println("bin: " + i + " xValue: " + lowBinLimit[i] );
		}
		for (int i =0 ; i<xArray.size(); i++){
			double xValue=xArray.get(i);
			int j = 0;
			if ((xValue < lowBinLimit[0]) || (xValue > lowBinLimit[numberOfBins])) continue;
			while ((xValue>=lowBinLimit[j+1])
				   && (j < numberOfBins)){
				j++;
			}
			count[j]++; 
		}
		if (DEBUG){
			for (int i =0; i<numberOfBins; i++){
				System.out.println("bin: " + i + " count: " + count[i] );
			}
		}

	}
	
	
	void paint(Graphics g, int hWidth, int hHeight, int xOffset, int yOffset, double lowLimit, double highLimit) {
		
//		if (DEBUG) System.out.println("paint: lowest bin: " + lowBinLimit[0] + " binSize: " + binSize + "highest bin: " + lowBinLimit[numberOfBins]);
		if (xArray.size() != 0){ // No display if count is null
			if (DEBUG) System.out.println("paint");
			if (notPaintedSince == 1) determineBinning(lowLimit,highLimit);
			else
			{
				if (DEBUG) System.out.println("not painted for a while, re-sort everything");
				if (Double.isNaN(lowLimit) || Double.isNaN(highLimit))
				{
					externalLimits = false;
					sortBins();
				}
				else switchToSet(lowLimit,highLimit);
			}
	
			g.translate(xOffset,yOffset);
			// Find the panel size and bar width and interval dynamically
			int width = hWidth;
			int height = hHeight;
			g.drawLine(0, height, width, height);
			g.drawLine(0, height , 0, 0);

			if (xArray.size() != 0){ // No display if count is null

				double interval = ((double)width ) / ((double)count.length);
				int individualWidth = (int)(((width ) / numberOfBins));

				int maxCount = 0;
				for (int i = 0; i < count.length; i++) {
					if (maxCount < count[i])
						maxCount = count[i];
				}

				// x is the start position for the first bar in the histogram
				int x = 0;

				// Draw base lines and ytics
				g.setFont(new Font("sansserif", Font.PLAIN, 8));
				double ytics = ((double)(maxCount)/10);
				double yticsCount = ytics;
				while (yticsCount <= maxCount){
					double ticHeight = ((yticsCount/(double)maxCount)*(height));
					g.drawString(String.valueOf((int)yticsCount),-20,(int)(height - ticHeight));
					yticsCount+=ytics;
				}

				int xticsCount =  (int)((double)numberOfBins/10.0);
				int xtics2 = xticsCount;
				for (int i = 0; i < count.length; i++) {
					// Find the bar height
					int barHeight =
						(int)(((double)count[i] / (double)maxCount) * (height));

					// Display a bar (i.e. rectangle)
					g.setColor(sectorColour);
					g.drawRect(x, height - barHeight, individualWidth, barHeight);
					g.fillRect(x, height- barHeight, individualWidth, barHeight);

					g.setColor(Color.BLACK);
					if (xtics2 == xticsCount){
						String ticMark = "" + oneDecimal.format(lowBinLimit[i]) + "";
						g.drawString(ticMark, x, height + 10);
						xtics2 =0;
					}
					x += interval;
					xtics2++;
				}
			}
			notPaintedSince = 0;
		}
	}
}
