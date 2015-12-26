// part of Taclet
// author: Ulrike Hager

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.text.DecimalFormat;
import java.io.*;

public class OneDHistogramFixed {
	private static final boolean DEBUG = false;
	private boolean externalLimits;
	private int[] count;
	private double[] lowBinLimit;
	private int numberOfBins;	
	Color sectorColour;
	DecimalFormat oneDecimal;
	

	public OneDHistogramFixed(Color aColour, int binNumber, double lowLimit, double highLimit){
		numberOfBins = binNumber;
		count= new int[numberOfBins];
		lowBinLimit = new double[numberOfBins+1];
		lowBinLimit[0]=lowLimit;
		for (int i =1; i<numberOfBins+1; i++){
			lowBinLimit[i]=lowBinLimit[i-1]+(highLimit-lowLimit)/((double)binNumber);
			if (DEBUG) System.out.println("bin: " + i + " lowBinLimit: " + lowBinLimit[i] );
		}
		sectorColour=aColour;
		oneDecimal = new DecimalFormat("0.0");
	}

	public void updateHistogram(double xValue) {
		if (DEBUG) System.out.println("updateHistogram: " + xValue);
		if (Double.isNaN(xValue)) return;
		if ((xValue < lowBinLimit[0]) || (xValue >= lowBinLimit[numberOfBins])) return;
		int j = 0;
		while ((xValue>=lowBinLimit[j+1]) && (j < numberOfBins)){
			j++;
		}
		count[j]++; 
		
	}


	private void updateCounts(double xValue){
		if (DEBUG) System.out.println("update counts "+ xValue);
		int j = 0;
		while ((xValue>=lowBinLimit[j+1]) && (j < numberOfBins)){
			j++;
		}
		count[j]++; 
	}		
	
	void writeData(String fileName){
		try{
		BufferedWriter outFile = new BufferedWriter(new FileWriter(fileName));
		for (int i =0; i<numberOfBins; i++){
			if (count[i]>0) outFile.write(lowBinLimit[i] + "\t" + count[i] + "\n");
		}
		outFile.close();
		}catch (IOException e){
			System.out.println("could not open file: " + fileName + "to write");
	}
	}
	
	void paint(Graphics g, int hWidth, int hHeight, int xOffset, int yOffset, double lowLimit, double highLimit) {
		
//		if (DEBUG) System.out.println("paint: lowest bin: " + lowBinLimit[0] + " binSize: " + binSize + "highest bin: " + lowBinLimit[numberOfBins]);
		if (DEBUG) System.out.println("paint");
		g.translate(xOffset,yOffset);
		// Find the panel size and bar width and interval dynamically
		int width = hWidth;
		int height = hHeight;
		g.setColor(Color.BLACK);
		g.drawLine(0, height, width, height);
		g.drawLine(0, height , 0, 0);
		int firstPoint = 0;
		int lastPoint = numberOfBins;

		if (!Double.isNaN(lowLimit))
		{
			int j = 0;
			while ((lowLimit>=lowBinLimit[j+1]) && (j < numberOfBins)){
				j++;
			}
			firstPoint = j;
			if (DEBUG) System.out.println("firstPoint determined: " + firstPoint);
		}
			
		if (!Double.isNaN(highLimit))
		{
			int j = numberOfBins;
			while ((highLimit<lowBinLimit[j-1]) && (j > 0)){
				j--;
			}
			lastPoint = j;
			if (DEBUG) System.out.println("lastPoint determined: " + lastPoint);
		}

		int numberOfPaintedBins = lastPoint -firstPoint;
		int intervalCheck=0;
		double interval = ((double)width ) / ((double)numberOfPaintedBins);
		int individualWidth = (int)interval;
		if (individualWidth<1){intervalCheck=1; individualWidth *= 2;}
		if (individualWidth<1) individualWidth = 1;

		int maxCount = 0;
		// x is the start position for the first bar in the histogram
		double x = 0;

		g.setFont(new Font("sansserif", Font.PLAIN, 8));

		double xticsCount =  ((double)numberOfPaintedBins/10.0);
//		int xtics2 = xticsCount;
		double xtics = (double)firstPoint;
		while (xtics <= lastPoint){
			int xPos = (int)((xtics-(double)firstPoint) * interval);
			g.drawString("" + oneDecimal.format(lowBinLimit[(int)xtics]) + "",xPos,(height+10));
			xtics += xticsCount;
		}
		switch(intervalCheck)
		{
		case 0:
			for (int i = firstPoint; i < lastPoint; i++) {
				if (maxCount < count[i])
					maxCount = count[i];
			}
			if (maxCount == 0) return;
			for (int i = firstPoint; i < lastPoint; i++) {
				// Find the bar height
				int barHeight =
					(int)(((double)count[i] / (double)maxCount) * (double)(height));

				double ytics = ((double)(maxCount)/10.0);
				double yticsCount = ytics;
				g.setColor(Color.BLACK);
				while (yticsCount <= maxCount){
					int ticHeight = (int)((yticsCount/(double)maxCount)*((double)height));
					g.drawString(String.valueOf((int)yticsCount),-20,(height - ticHeight));
					yticsCount+=ytics;
				}
				g.setColor(sectorColour);
				g.drawRect((int)x, height - barHeight, individualWidth, barHeight);
				g.fillRect((int)x, height- barHeight, individualWidth, barHeight);

				x += interval;
			}
			break;
		case 1:
			for (int i = firstPoint; i < lastPoint-1; i+=2) {
				if (maxCount < count[i]+count[i+1])
					maxCount = count[i]+count[i+1];
			}
			if (maxCount == 0) return;
			double ytics = ((double)(maxCount)/10.0);
			double yticsCount = ytics;
			g.setColor(Color.BLACK);
			while (yticsCount <= maxCount){
				int ticHeight = (int)((yticsCount/(double)maxCount)*((double)height));
				g.drawString(String.valueOf((int)yticsCount),-20,(height - ticHeight));
				yticsCount+=ytics;
			}

			for (int i = firstPoint; i < lastPoint-1; i+=2) {
				// Find the bar height
				int barHeight =
					(int)(((double)(count[i]+count[i+1]) / (double)maxCount) * (double)(height));

				// Display a bar (i.e. rectangle)
				g.setColor(sectorColour);
				g.drawRect((int)x, height - barHeight, individualWidth, barHeight);
				g.fillRect((int)x, height- barHeight, individualWidth, barHeight);

				x += 2*interval;

//			xtics2++;
			}
			break;
		}
	}
}
