// part of Taclet
// author: Ulrike Hager

import java.net.*;
import java.io.*;
import java.util.ArrayList;


public class GlobalVars{
	public static boolean gotContact;
	public static boolean showTracks;
	public static boolean freezeTrack;
	public static boolean stop;
	public static boolean correctForShielding;
	public static boolean selectCoincidences;
	boolean drawLine, drawTrack, loopFile, singlePopup, oneTrackPerSector, logScale;
	int page, display;
	int energyFactor;
	double stepSize;
	public double energyLow, energyHigh, zLow, zHigh, angleLow, angleHigh, zEndLow, zEndHigh, radiusLow, radiusHigh, lengthLow, lengthHigh, totalEnergyLow, totalEnergyHigh;
	public double[] sliceLimit;
	BufferedReader incoming;
	public GlobalVars(){
		gotContact = false;
		stepSize=1;
		energyFactor = 100;
		showTracks = true;
		page = display = 0;
		stop = false;
		freezeTrack = false;
		correctForShielding = false;
		selectCoincidences = false;
		drawLine=false;
		drawTrack = true;
		loopFile = false;
		singlePopup = true;
		oneTrackPerSector = true;
		logScale = false;
		energyLow = energyHigh = zLow = zHigh = angleLow = angleHigh = zEndLow = zEndHigh = radiusLow = radiusHigh = lengthLow = lengthHigh = totalEnergyLow = totalEnergyHigh = Double.NaN;
		sliceLimit = new double[8];
		for (int i = 0; i<4; i++){
			sliceLimit[i] = i*60-120;
			sliceLimit[i+4] = (i+1)*60-120;
		}
	}
}
