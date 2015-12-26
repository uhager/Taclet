// part of Taclet
// author: Ulrike Hager

import java.io.*;
import java.net.*;
import java.awt.*;
import java.awt.geom.*;
import javax.swing.*;
import java.lang.*;
import java.util.*;
import javax.swing.JPanel;
import java.awt.event.*;
import java.text.DecimalFormat;
import java.text.DateFormat;
import java.text.SimpleDateFormat;


public class OutputPanel extends JPanel implements Runnable, MouseListener { //class OutputPanel, draws histograms and tracks
	private static final boolean DEBUG = false;
	boolean energyCut, angleCut, originCut, totalEnergyCut, deltaECut;
	boolean eventInfo;
 	int CurrentSector=8;
	double currentEnergy, totalEnergy;
	String currentEvent;
	String Input, unknownString;
	int unknownStrings;
	int[] totalHits, currentHits, sliceOffset;
	int sumEvents, sumTracks;
	String voltage, pressure, runNumber, presentedEvents, acceptedEvents, flow;
 	HistBar[] histBar;
 	HistCircle histCircle;
	HistCircle[] sliceCircle;
	
	OneDHistogramFixed zHistAll, angleHistAll, radiusHistAll, energyHistAll, totalEnergyHist;
	OneDHistogramFixed[] energyHist;
	OneDHistogramFixed[] zHist;
	OneDHistogramFixed[] angleHist;
	OneDHistogramFixed[] radiusHist;
	OneDHistogramFixed[] coincidenceHist;
//	OneDHistogram energyHistAll;
//	TwoDHistogram  energyVsTotalLengthAll;
	TwoDHistogramFixed[] energyVsZ,energyVsZSmall;
	TwoDHistogramFixed[] deltaEvsE, deltaEvsESmall;
	TwoDHistogramFixed[] energyVsOrigin,energyVsOriginSmall;
	TwoDHistogramFixed[] energyVsAngle,energyVsAngleSmall;
	TwoDHistogramFixed[] energyVsTotalLength, energyVsTotalLengthSmall;
	TwoDHistogramFixed energyVsZAll, energyVsAngleAll, originVsAngleAll, radiusVsZAll, energyVsOriginAll,energyVsTotalLengthAll,deltaEvsEAll;
	TwoDHistogramFixed[] radiusVsZ, radiusVsZSmall;
	TwoDHistogramFixed[] originVsAngle, originVsAngleSmall;
	double energyLow, energyHigh,totalEnergyLow, totalEnergyHigh, zLow, zHigh, angleLow, angleHigh, zEndLow, zEndHigh, radiusLow, radiusHigh, lengthLow, lengthHigh;
	public double energyLowCut, energyHighCut, totalEnergyLowCut, totalEnergyHighCut, zLowCut, zHighCut, angleLowCut, angleHighCut, zEndLowCut, zEndHighCut, radiusLowCut, radiusHighCut, lengthLowCut, lengthHighCut, deltaELowCut,deltaEHighCut;
 	Sector[] sectors;
	ArrayList<Sector> collectSectors, paintSectors; 
	ArrayList<Integer> coincidenceList;
//	ArrayList<Integer> coincidenceArray;
//	int[] coincidenceList;
	Dimension offDimension;
    Image offImage;
    Graphics offGraphics;
	Color[] tacColour;
	GlobalVars globals;
	private boolean showTracks;
	JFrame popupFrame;
	JTextArea popupText;
	DecimalFormat twoDecimal;
	
	public OutputPanel(GlobalVars globals){ //constructor
		this.globals=globals;
		showTracks=globals.showTracks;
		totalHits= new int[8];
		voltage=pressure=acceptedEvents=presentedEvents=runNumber=flow="0";
		energyLow = 0; energyHigh = 20; globals.energyLow = energyLow; globals.energyHigh = energyHigh; energyLowCut = energyLow; energyHighCut = energyHigh;
		deltaELowCut = energyLow; deltaEHighCut = energyHigh;
		totalEnergyLow = 0; totalEnergyHigh = 2* energyHigh; globals.totalEnergyLow = totalEnergyLow; globals.totalEnergyHigh = totalEnergyHigh; totalEnergyLowCut = totalEnergyLow; totalEnergyHighCut = totalEnergyHigh;
		radiusLow = 0; radiusHigh = 60; globals.radiusLow = radiusLow; globals.radiusHigh = radiusHigh;
		angleLow = 0; angleHigh = 180.5; globals.angleLow = angleLow; globals.angleHigh = angleHigh; angleLowCut = angleLow; angleHighCut = angleHigh;
		zLow = -120; zHigh = 122; globals.zLow = zLow; globals.zHigh = zHigh; globals.zEndLow = zLow; globals.zEndHigh = zHigh; zLowCut = zLow; zHighCut = zHigh; zEndLowCut = zLow; zEndHighCut = zHigh;
//		zEndLow = -1; zEndHigh = 62; 
		lengthLow= 0; lengthHigh = 300; globals.lengthLow= lengthLow; globals.lengthHigh = lengthHigh; lengthLowCut = lengthLow; lengthHighCut = lengthHigh;
		int[] anOffset = {150,430,150,430,150,150,400,400};
		sliceOffset = anOffset;
		histBar =new HistBar[8];
//		histCircle =new HistCircle(400,150,globals.);
		coincidenceList= new ArrayList<Integer>();
		collectSectors = new ArrayList<Sector>();
		paintSectors = new ArrayList<Sector>();
//		coincidenceList = new int[8];
// 		sectors = new Sector[8];
// 		coincidenceArray= new ArrayList<Integer>();
// 		coincidenceList = new int[coincidenceArray.size()];
		tacColour = new Color[8];
		for(int i=0; i<tacColour.length; i++){
			tacColour[0]= new Color(160,16,23);
			tacColour[1]= new Color(5,94,154);
			tacColour[2]= new Color(0,153,77);
			tacColour[3]= new Color(192,204,59);
			tacColour[4]= new Color(86,33,122);
			tacColour[5]= new Color(43,162,148);
			tacColour[6]= new Color(222,141,42);
			tacColour[7]= new Color(10,34,69);
		}
		addMouseListener(this);
		energyCut = false;
		angleCut = false;
		originCut = false;
		eventInfo = false;
		twoDecimal = new DecimalFormat("0.00");
		popupFrame = new JFrame("Event Info");
		reInitAll();
// 		sectorIni();
// 		initEnergyHistos();
// 		initZHistos();
	} //end constructor

	public void mouseClicked(MouseEvent event) {
		if (globals.page==0){
			int xPos=event.getX();
			int yPos=event.getY();
			if ((xPos>360) && (yPos>630)){
						popupText = new JTextArea(5, 20);
						popupText.setEditable(false);
						popupText.append(unknownString);
			if (globals.singlePopup) popupFrame.dispose();
						popupFrame = new JFrame("Unknown string");
						popupFrame.add(popupText);
						popupFrame.pack();
						popupFrame.setVisible(true);

			}
			else if ((paintSectors.size()>0) && (!eventInfo)){
			DecimalFormat oneDecimal = new DecimalFormat("0.0");
			popupText = new JTextArea(5, 20);
//			JScrollPane scrollPane = new JScrollPane(textArea); 
			popupText.setEditable(false);
			popupText.append("Event: " + currentEvent + "\n");
			for (int i =0; i < paintSectors.size(); i++){
				int s = paintSectors.get(i).getNumber();
				popupText.append("sector: " + s + " \n Energy: " + twoDecimal.format(paintSectors.get(i).getTotalEnergy()) + " \n Endpoint Z: " + paintSectors.get(i).getLastZ() +"\t R: " + oneDecimal.format(paintSectors.get(i).getLastRadius()) +" \n Origin: " + oneDecimal.format(paintSectors.get(i).getOrigin()) + "\t Angle: " +  oneDecimal.format(paintSectors.get(i).getAngle()) + "\n ") ;
			}
			
			if (globals.singlePopup) popupFrame.dispose();
			popupFrame = new JFrame("Event " + currentEvent);
			popupFrame.add(popupText);
			popupFrame.pack();
			popupFrame.setVisible(true);
			
			eventInfo = true;
		}
	}
	}
	public void mouseExited(MouseEvent e) {}
	public void mousePressed(MouseEvent event) {}
	public void mouseReleased(MouseEvent event){}
	public void mouseEntered(MouseEvent e) {}
	public void mouseMoved(MouseEvent e) {}
	public void mouseDragged(MouseEvent e) {}



	public void sectorIni(){
		totalHits = new int[8];
		currentHits = new int[8];
		histBar =new HistBar[8];
		histCircle =new HistCircle(400,150,globals.stepSize, tacColour);
		sliceCircle = new HistCircle[4];
		for (int i = 0; i<sliceCircle.length; i++){
			sliceCircle[i] = new HistCircle(sliceOffset[i],sliceOffset[i+4],globals.stepSize*2,tacColour);
		}
		for(int i=0; i<histBar.length; i++){
			histBar[i] = new HistBar(i,totalHits[i],globals.stepSize);
			if (globals.correctForShielding){
				if (i == 1 || i == 2) histBar[i].setStepSize(1.2*globals.stepSize);
				if (i == 4 || i == 7) histBar[i].setStepSize(1.5*globals.stepSize);
			}
		}
			if (globals.correctForShielding){
				for(int i=0; i<8; i++){
					if (i == 1 || i == 2){
						histCircle.setStepSize(i,1.2*globals.stepSize);
						for (int j = 0; j<4; j++){
							sliceCircle[j].setStepSize(i,1.2*globals.stepSize);
						}
					}
					if (i == 4 || i == 7){
						histCircle.setStepSize(i,1.5*globals.stepSize);
						for (int j = 0; j<4; j++){
							sliceCircle[j].setStepSize(i,1.5*globals.stepSize);
						}
					}
			}
		}

	}
	
	public void initHistos(){
		totalEnergyHist = new OneDHistogramFixed(Color.BLUE,800,totalEnergyLow,totalEnergyHigh);
		radiusVsZAll = new TwoDHistogramFixed(500,500,30,40,63,zLow,zHigh,240,radiusLow,radiusHigh);
		radiusVsZ = new TwoDHistogramFixed[8];
		for(int i=0; i<radiusVsZ.length; i++){
			int[] offset = offsetHalf(i);
			radiusVsZ[i] = new TwoDHistogramFixed(300,265,offset[0],offset[1],242,zLow,zHigh,240,radiusLow,radiusHigh);
		}
		radiusVsZSmall = new TwoDHistogramFixed[8];
		for(int i=0; i<radiusVsZSmall.length; i++){
			int[] offset = offsetOverview(i);
			radiusVsZSmall[i] = new TwoDHistogramFixed(180,160,offset[0],offset[1],121,zLow,zHigh,140,radiusLow,radiusHigh);
		}
		energyVsZAll = new TwoDHistogramFixed(500,500,30,40,242,zLow,zHigh,800,energyLow,energyHigh);
		energyVsZ = new TwoDHistogramFixed[8];
		for(int i=0; i<energyVsZ.length; i++){
			int[] offset = offsetHalf(i);
			energyVsZ[i] = new TwoDHistogramFixed(300,265,offset[0],offset[1],242,zLow,zHigh,800,energyLow,energyHigh);
		}
		energyVsZSmall = new TwoDHistogramFixed[8];
		for(int i=0; i<energyVsZSmall.length; i++){
			int[] offset = offsetOverview(i);
			energyVsZSmall[i] = new TwoDHistogramFixed(180,160,offset[0],offset[1],121,zLow,zHigh,200,energyLow,energyHigh);
		}
		energyVsAngleAll = new TwoDHistogramFixed(500,500,30,40,361,angleLow,angleHigh,800,energyLow,energyHigh);
		energyVsAngle = new TwoDHistogramFixed[8];
		for(int i=0; i<energyVsAngle.length; i++){
			int[] offset = offsetHalf(i);
			energyVsAngle[i] = new TwoDHistogramFixed(300,265,offset[0],offset[1],361,angleLow,angleHigh,800,energyLow,energyHigh);
		}
		energyVsAngleSmall = new TwoDHistogramFixed[8];
		for(int i=0; i<energyVsAngleSmall.length; i++){
			int[] offset = offsetOverview(i);
			energyVsAngleSmall[i] = new TwoDHistogramFixed(180,160,offset[0],offset[1],90,0,180,100,energyLow,energyHigh);
		}
		deltaEvsEAll = new TwoDHistogramFixed(500, 500, 30, 40,800,energyLow,energyHigh,800,energyLow,energyHigh);
		deltaEvsE = new TwoDHistogramFixed[8];
		for(int i=0; i<deltaEvsE.length; i++){
			int[] offset = offsetHalf(i);
			deltaEvsE[i] = new TwoDHistogramFixed(300,265,offset[0],offset[1],800,energyLow,energyHigh,800,energyLow,energyHigh);
		}
		deltaEvsESmall = new TwoDHistogramFixed[8];
		for(int i=0; i<deltaEvsESmall.length; i++){
			int[] offset = offsetOverview(i);
			deltaEvsESmall[i] = new TwoDHistogramFixed(180,160,offset[0],offset[1],200,energyLow,energyHigh,200,energyLow,energyHigh);
		}
		originVsAngleAll = new TwoDHistogramFixed(500,500,30,40,361,angleLow,angleHigh,242,zLow,zHigh);
		originVsAngle = new TwoDHistogramFixed[8];
		for(int i=0; i<originVsAngle.length; i++){
			int[] offset = offsetHalf(i);
			originVsAngle[i] = new TwoDHistogramFixed(300,265,offset[0],offset[1],361,angleLow,angleHigh,242,zLow,zHigh);
		}
		originVsAngleSmall = new TwoDHistogramFixed[8];
		for(int i=0; i<originVsAngleSmall.length; i++){
			int[] offset = offsetOverview(i);
			originVsAngleSmall[i] = new TwoDHistogramFixed(180,160,offset[0],offset[1],90,angleLow,angleHigh,121,zLow,zHigh);
		}
		energyVsOriginAll = new TwoDHistogramFixed(500,500,30,40,242,zLow,zHigh,800,energyLow,energyHigh);
		energyVsOrigin = new TwoDHistogramFixed[8];
		for(int i=0; i<energyVsOrigin.length; i++){
			int[] offset = offsetHalf(i);
			energyVsOrigin[i] = new TwoDHistogramFixed(300,265,offset[0],offset[1],242,zLow,zHigh,600,energyLow,energyHigh);
		}
		energyVsOriginSmall = new TwoDHistogramFixed[8];
		for(int i=0; i<energyVsOriginSmall.length; i++){
			int[] offset = offsetOverview(i);
			energyVsOriginSmall[i] = new TwoDHistogramFixed(180,160,offset[0],offset[1],242,zLow,zHigh,100,energyLow,energyHigh);
		}
		energyHist = new OneDHistogramFixed[8];
		energyHistAll = new OneDHistogramFixed(Color.BLUE,800,energyLow,energyHigh);
		for(int i=0; i<energyHist.length; i++){
			energyHist[i] = new OneDHistogramFixed(tacColour[i] ,800,energyLow,energyHigh);
		}
		angleHistAll = new OneDHistogramFixed(Color.BLUE, 361,angleLow,angleHigh);
		angleHist = new OneDHistogramFixed[8];
		for(int i=0; i<angleHist.length; i++){
			angleHist[i] = new OneDHistogramFixed(tacColour[i], 361,angleLow,angleHigh);
		}
		energyVsTotalLengthAll = new TwoDHistogramFixed(500,500,30,40,300,lengthLow,lengthHigh,800,energyLow,energyHigh);
		energyVsTotalLength = new TwoDHistogramFixed[8];
		for(int i=0; i<energyVsTotalLength.length; i++){
			int[] offset = offsetHalf(i);
			energyVsTotalLength[i] = new TwoDHistogramFixed(300,265,offset[0],offset[1],150,lengthLow,lengthHigh,800,energyLow,energyHigh);
		}
		energyVsTotalLengthSmall = new TwoDHistogramFixed[8];
		for(int i=0; i<energyVsTotalLengthSmall.length; i++){
			int[] offset = offsetOverview(i);
			energyVsTotalLengthSmall[i] = new TwoDHistogramFixed(180,160,offset[0],offset[1],100,lengthLow,lengthHigh,200,energyLow,energyHigh);
		}
		radiusHist = new OneDHistogramFixed[8];
		radiusHistAll = new OneDHistogramFixed(Color.BLUE,240,radiusLow,radiusHigh);
		for(int i=0; i<radiusHist.length; i++){
			radiusHist[i] = new OneDHistogramFixed(tacColour[i],240,radiusLow,radiusHigh);
		}
		coincidenceHist = new OneDHistogramFixed[8];
		for(int i=0; i<coincidenceHist.length; i++){
			coincidenceHist[i] = new OneDHistogramFixed(tacColour[i], 8,0,8);
		}
		zHist = new OneDHistogramFixed[8];
		zHistAll = new OneDHistogramFixed(Color.BLUE,242,zLow,zHigh);
		for(int i=0; i<zHist.length; i++){
			zHist[i] = new OneDHistogramFixed(tacColour[i],242,zLow,zHigh);
		}

	}
	public void reInitAll(){
		sectorIni();
		initHistos();
		sumTracks = sumEvents = 0;
		totalEnergy = 0;
		unknownStrings=0;
	}

	
	public void drawLines(Graphics g){
 		g.setColor(Color.BLACK);
		g.drawLine(20,300,650,300);
		g.drawLine(20,450,650,450);
		g.drawLine(20,600,650,600);
	}

	public Dimension getPreferredSize() {
        return new Dimension(700,655);
    }

	public void changeSliceStep(int i){
		if (i > 3) i = i-4;
		sliceCircle[i] = new HistCircle(sliceOffset[i],sliceOffset[i+4],globals.stepSize*2,tacColour);
	}
	
	public int[] offsetOverview(int i){
		int[] offset = new int[2];
		offset[0] = 5;
		offset[1] = 0;
		if (i == 0) offset[1] = 15;
		if (i==1 || i ==2 || i==4 || i == 5 || i == 7) offset[0] = 220;
		if (i==3 || i == 6) {
			offset[0]=-440;
			offset[1]=195;
		}
		return offset;
	}

	public int[] offsetHalf(int i){
		int[] offset = new int[2];
		offset[0] = 5;
		offset[1] = 0;
		if (i == 0) offset[1] = 15;
		if (i==1 || i ==3 || i==5 || i ==7) offset[0] = 320;
		if (i==2 || i==6) {
			offset[0]=-320;
			offset[1]=285;
		}
		return offset;
	}

	public void paintOneDOverview(OneDHistogram[] histo, Graphics g, double lowLimit, double highLimit){
		for(int i=0; i<histo.length; i++){
			int[] offset = offsetOverview(i);
			histo[i].paint(g, 180, 160, offset[0], offset[1], lowLimit, highLimit);
		}
	}
	
	public void paintOneDFirstHalf(OneDHistogram[] histo, Graphics g, double lowLimit, double highLimit){
		for(int i=0; i<histo.length/2; i++){
			int[] offset = offsetHalf(i);
			histo[i].paint(g, 300, 265, offset[0], offset[1], lowLimit, highLimit);
		}
	}

	public void paintOneDSecondHalf(OneDHistogram[] histo, Graphics g, double lowLimit, double highLimit){
		for(int i=histo.length/2; i<histo.length; i++){
			int[] offset = offsetHalf(i);
			histo[i].paint(g, 300, 265, offset[0], offset[1], lowLimit, highLimit);
		}
	}


	public void paintOneDFixedOverview(OneDHistogramFixed[] histo, Graphics g, double lowLimit, double highLimit){
		for(int i=0; i<histo.length; i++){
			int[] offset = offsetOverview(i);
			histo[i].paint(g, 180, 160, offset[0], offset[1], lowLimit, highLimit);
		}
	}
	
	public void paintOneDFixedFirstHalf(OneDHistogramFixed[] histo, Graphics g, double lowLimit, double highLimit){
		for(int i=0; i<histo.length/2; i++){
			int[] offset = offsetHalf(i);
			histo[i].paint(g, 300, 265, offset[0], offset[1], lowLimit, highLimit);
		}
	}

	public void paintOneDFixedSecondHalf(OneDHistogramFixed[] histo, Graphics g, double lowLimit, double highLimit){
		for(int i=histo.length/2; i<histo.length; i++){
			int[] offset = offsetHalf(i);
			histo[i].paint(g, 300, 265, offset[0], offset[1], lowLimit, highLimit);
		}
	}


	public void drawPlots(Graphics g){
		Graphics2D g2D = (Graphics2D) g;
		if (globals.page == 0){
			g.setColor(Color.BLACK);
			g.drawString("events: "+ String.valueOf(sumEvents),20,615);
			g.drawString("tracks/event: "+ String.valueOf(twoDecimal.format((double)sumTracks/(double)sumEvents)),200,615);
			g.drawString("Presented: "+ presentedEvents,20,645);
			g.drawString("Accepted: "+ acceptedEvents,200,645);
			g.drawString("HV [V]: "+ voltage,20,630);
			g.drawString("P [mbar]: "+ pressure,200,630);			
			g.drawString("flow [ccm]: "+ flow,380,630);
			g.drawString("unknown strings: "+ unknownStrings,380,645);
			for (int i =0 ; i<paintSectors.size(); i++){
				try{
					int sector = paintSectors.get(i).getNumber();
					g.setColor(tacColour[sector]);
					if (globals.drawTrack) paintSectors.get(i).paintTrack(g,globals.energyFactor,globals.logScale);
					if (globals.drawLine) paintSectors.get(i).paintLine(g,globals.energyFactor);
				}catch(IndexOutOfBoundsException e){
				}catch(NullPointerException e){}
			}
				try{
					histCircle.paint(g);
				}catch(NullPointerException e){}
			for(int i=0; i<histBar.length; i++){
				g.setColor(tacColour[i]);
				try{
					histBar[i].paint(g);
				}catch(NullPointerException e){}
			}
			drawLines(g);

		}
		if (globals.page == 1) {
			//g2D.setFont(new Font("sansserif", Font.PLAIN, 10));
			//g2D.drawString("Sectors", 300,600);
			for (int i = 0; i < 4; i++){
				sliceCircle[i].paint(g);
			}
		}
		if (globals.page == 2) {
			g2D.setFont(new Font("sansserif", Font.PLAIN, 10));
			g2D.drawString("Energy / MeV", 300,600);
			totalEnergyHist.paint(g, 500, 500, 30, 40,globals.totalEnergyLow,globals.totalEnergyHigh);
		}
		if (globals.page == 3) {
			g2D.setFont(new Font("sansserif", Font.PLAIN, 10));
			g2D.drawString("Energy / MeV", 300,600);
			switch(globals.display){
			case 0: energyHistAll.paint(g, 500, 500, 30, 40,globals.energyLow,globals.energyHigh); break;
			case 1: paintOneDFixedOverview(energyHist,g,globals.energyLow,globals.energyHigh); break;
			case 2: paintOneDFixedFirstHalf(energyHist,g,globals.energyLow,globals.energyHigh); break;
			case 3: paintOneDFixedSecondHalf(energyHist,g,globals.energyLow,globals.energyHigh); break;
			}
		}
			
		if (globals.page == 4) {
			g2D.setFont(new Font("sansserif", Font.PLAIN, 10));
			g2D.drawString("z Origin / pads", 300,600);
			switch(globals.display){
			case 0: zHistAll.paint(g, 500, 500, 30, 40,globals.zLow,globals.zHigh);break;
			case 1: paintOneDFixedOverview(zHist,g,globals.zLow,globals.zHigh); break;
			case 2: paintOneDFixedFirstHalf(zHist,g,globals.zLow,globals.zHigh); break;
			case 3: paintOneDFixedSecondHalf(zHist,g,globals.zLow,globals.zHigh); break;
			}
		}
		if (globals.page == 5) {
			g2D.setFont(new Font("sansserif", Font.PLAIN, 10));
			g2D.drawString("Energy / MeV", 300,600);
			g2D.rotate(-Math.PI/2);
			g2D.drawString("deltaE / MeV", -300,8);
			g2D.rotate(Math.PI/2);
			switch(globals.display){
			case 0: deltaEvsEAll.drawHistogram(g); break;
			case 1: for (int i = 0;i<8;i++) deltaEvsESmall[i].drawHistogram(g); break;
			case 2: for (int i = 0;i<4;i++) deltaEvsE[i].drawHistogram(g); break;
			case 3: for (int i = 4;i<8;i++) deltaEvsE[i].drawHistogram(g); break;
			}
		}
		if (globals.page == 6) {
			g2D.setFont(new Font("sansserif", Font.PLAIN, 10));
			g2D.drawString("Z endpoint / pads", 300,600);
			g2D.rotate(-Math.PI/2);
			g2D.drawString("Energy / MeV", -300,8);
			g2D.rotate(Math.PI/2);
			switch(globals.display){
			case 0: energyVsZAll.drawHistogram(g); break;
			case 1: for (int i = 0;i<8;i++) energyVsZSmall[i].drawHistogram(g); break;
			case 2: for (int i = 0;i<4;i++) energyVsZ[i].drawHistogram(g); break;
			case 3: for (int i = 4;i<8;i++) energyVsZ[i].drawHistogram(g); break;
			}
		}
		if (globals.page == 7) {
			g2D.setFont(new Font("sansserif", Font.PLAIN, 10));
			g2D.drawString("Z endpoint / pads", 300,600);
			g2D.rotate(-Math.PI/2);
			g2D.drawString("Radius endpoint / mm", -300,8);
			g2D.rotate(Math.PI/2);
			switch(globals.display){
			case 0: radiusVsZAll.drawHistogram(g); break;
			case 1: for (int i = 0;i<8;i++) radiusVsZSmall[i].drawHistogram(g); break;
			case 2: for (int i = 0;i<4;i++) radiusVsZ[i].drawHistogram(g); break;
			case 3: for (int i = 4;i<8;i++) radiusVsZ[i].drawHistogram(g); break;
			}
		}
		if (globals.page == 8) {
			g2D.setFont(new Font("sansserif", Font.PLAIN, 10));
			g2D.drawString("z Origin / pads", 300,600);
			g2D.rotate(-Math.PI/2);
			g2D.drawString("Energy / MeV", -300,8);
			g2D.rotate(Math.PI/2);
			switch(globals.display){
 			case 0: energyVsOriginAll.drawHistogram(g); break;
 			case 1: for (int i = 0;i<8;i++) energyVsOriginSmall[i].drawHistogram(g); break;
 			case 2: for (int i = 0;i<4;i++) energyVsOrigin[i].drawHistogram(g); break;
 			case 3: for (int i = 4;i<8;i++) energyVsOrigin[i].drawHistogram(g); break;
			}
		}
		if (globals.page == 9) {
			g2D.setFont(new Font("sansserif", Font.PLAIN, 10));
			g2D.drawString("Angle / degrees", 300,600);
			switch(globals.display){
			case 0: angleHistAll.paint(g, 500, 500, 30, 40,globals.angleLow,globals.angleHigh);break;
			case 1: paintOneDFixedOverview(angleHist,g,globals.angleLow,globals.angleHigh); break;
			case 2: paintOneDFixedFirstHalf(angleHist,g,globals.angleLow,globals.angleHigh); break;
			case 3: paintOneDFixedSecondHalf(angleHist,g,globals.angleLow,globals.angleHigh); break;
			}
		}
		if (globals.page == 10) {
			g2D.setFont(new Font("sansserif", Font.PLAIN, 10));
			g2D.drawString("Angle / degrees", 300,600);
			g2D.rotate(-Math.PI/2);
			g2D.drawString("Energy / MeV", -300,8);
			g2D.rotate(Math.PI/2);
			switch(globals.display){
 			case 0: energyVsAngleAll.drawHistogram(g); break;
  			case 1: for (int i = 0;i<8;i++) energyVsAngleSmall[i].drawHistogram(g); break;
  			case 2: for (int i = 0;i<4;i++) energyVsAngle[i].drawHistogram(g); break;
  			case 3: for (int i = 4;i<8;i++) energyVsAngle[i].drawHistogram(g); break;
			}
		}
		if (globals.page == 11) {
			g2D.setFont(new Font("sansserif", Font.PLAIN, 10));
			g2D.drawString("Angle / degrees", 300,600);
			g2D.rotate(-Math.PI/2);
			g2D.drawString("z Origin / pads", -300,8);
			g2D.rotate(Math.PI/2);
			switch(globals.display){
 			case 0: originVsAngleAll.drawHistogram(g); break;
 			case 1: for (int i = 0;i<8;i++) originVsAngleSmall[i].drawHistogram(g); break;
 			case 2: for (int i = 0;i<4;i++) originVsAngle[i].drawHistogram(g); break;
 			case 3: for (int i = 4;i<8;i++) originVsAngle[i].drawHistogram(g); break;
			}
		}
		if (globals.page == 12) {
			g2D.setFont(new Font("sansserif", Font.PLAIN, 10));
			g2D.drawString("Extrapolated track length along calculated slope / mm", 250,600);
			g2D.rotate(-Math.PI/2);
			g2D.drawString("Energy / MeV", -300,8);
			g2D.rotate(Math.PI/2);
			switch(globals.display){
 			case 0: energyVsTotalLengthAll.drawHistogram(g); break;
 			case 1: for (int i = 0;i<8;i++) energyVsTotalLengthSmall[i].drawHistogram(g); break;
 			case 2: for (int i = 0;i<4;i++) energyVsTotalLength[i].drawHistogram(g); break;
 			case 3: for (int i = 4;i<8;i++) energyVsTotalLength[i].drawHistogram(g); break;
			}
		}
		if (globals.page == 13) {
			g2D.setFont(new Font("sansserif", Font.PLAIN, 10));
			g2D.drawString("Radius endpoint / mm", 300,600);
			switch(globals.display){
			case 0: radiusHistAll.paint(g, 500, 500, 30, 40,globals.radiusLow,globals.radiusHigh);break;
			case 1: paintOneDFixedOverview(radiusHist,g,globals.radiusLow,globals.radiusHigh); break;
			case 2: paintOneDFixedFirstHalf(radiusHist,g,globals.radiusLow,globals.radiusHigh); break;
			case 3: paintOneDFixedSecondHalf(radiusHist,g,globals.radiusLow,globals.radiusHigh); break;
			}
		}
		if (globals.page == 14) {
			g2D.setFont(new Font("sansserif", Font.PLAIN, 10));
			g2D.drawString("Sectors", 300,600);
			switch(globals.display){
			case 0: paintOneDFixedOverview(coincidenceHist,g,0,7); break;
			case 1: paintOneDFixedOverview(coincidenceHist,g,0,7); break;
			case 2: paintOneDFixedFirstHalf(coincidenceHist,g,0,7); break;
			case 3: paintOneDFixedSecondHalf(coincidenceHist,g,0,7); break;
			}
		}


	}

	
	public void paintComponent(Graphics g) {
		super.paintComponent(g);       
		if (DEBUG) System.out.println("Paint");
		setBackground(Color.WHITE);
		drawPlots(g);
	}
	
	public void update(Graphics g) { //update using 'offscreen' image
		Dimension d = new Dimension(700,555);

		if ((offGraphics == null)
			|| (d.width != offDimension.width)
			|| (d.height != offDimension.height)) {
			offDimension = d;
			offImage = createImage(d.width, d.height);
			offGraphics = offImage.getGraphics();
		}

		setBackground(Color.WHITE);
		drawPlots(offGraphics);
		g.drawImage(offImage, 0, 0, null);
	}

	public void readSector(){
		if (DEBUG) System.out.println("reading sector num: " + Input);
		Input=Input.substring(1,2);
		CurrentSector = Integer.parseInt(Input);
		collectSectors.add(new Sector(CurrentSector));
		if (DEBUG) System.out.println(CurrentSector);
	}

	public void fillHistograms(int i, int sector){
		if (DEBUG) System.out.println("fill histos, input: " + Input);
		double currentOrigin =paintSectors.get(i).getOrigin();
		double currentAngle = paintSectors.get(i).getAngle();
		double currentRadius = paintSectors.get(i).getLastRadius();
		if (DEBUG) System.out.println("fill histos, energy: " + currentEnergy + " Origin: " + currentOrigin + " angle: " + currentAngle + " radius: " + currentRadius);
		boolean eCheck = ((currentEnergy >= energyLowCut) && (currentEnergy <= energyHighCut)) || (!energyCut);
		boolean tECheck = ((totalEnergy >= totalEnergyLowCut) && (totalEnergy <= totalEnergyHighCut)) || (!totalEnergyCut);
		boolean zCheck = ((currentOrigin >= zLowCut) && (currentOrigin <= zHighCut)) || (!originCut);
		boolean aCheck = ((currentAngle >= angleLowCut) && (currentAngle <= angleHighCut)) || (!angleCut);
		boolean dECheck = ((paintSectors.get(i).getDeltaE() >= deltaELowCut) && (paintSectors.get(i).getDeltaE() <= deltaEHighCut)) || (!deltaECut);
		if (eCheck && zCheck && aCheck && tECheck && dECheck){
			if (DEBUG) System.out.println("passed cuts");
			totalEnergyHist.updateHistogram(totalEnergy);
			energyHist[sector].updateHistogram(currentEnergy);
			zHist[sector].updateHistogram(currentOrigin);
			energyHistAll.updateHistogram(currentEnergy);
			angleHistAll.updateHistogram(currentAngle);
			angleHist[sector].updateHistogram(currentAngle);
			zHistAll.updateHistogram(currentOrigin);
			radiusHistAll.updateHistogram(currentRadius);
			radiusHist[sector].updateHistogram(currentRadius);
			energyVsAngleAll.updateHistogram(currentAngle,currentEnergy,globals.angleLow,globals.angleHigh, globals.energyLow,globals.energyHigh);
			energyVsAngle[sector].updateHistogram(currentAngle,currentEnergy,globals.angleLow,globals.angleHigh, globals.energyLow,globals.energyHigh);
			energyVsAngleSmall[sector].updateHistogram(currentAngle,currentEnergy,globals.angleLow,globals.angleHigh, globals.energyLow,globals.energyHigh);
			originVsAngleAll.updateHistogram(currentAngle,currentOrigin, globals.angleLow,globals.angleHigh,globals.zLow,globals.zHigh);
			originVsAngle[sector].updateHistogram(currentAngle,currentOrigin, globals.angleLow,globals.angleHigh,globals.zLow,globals.zHigh);
			originVsAngleSmall[sector].updateHistogram(currentAngle,currentOrigin, globals.angleLow,globals.angleHigh,globals.zLow,globals.zHigh);
			energyVsZAll.updateHistogram((paintSectors.get(i).getLastZ()),currentEnergy,globals.zEndLow,globals.zEndHigh,globals.energyLow,globals.energyHigh);
			energyVsZ[sector].updateHistogram((paintSectors.get(i).getLastZ()),currentEnergy,globals.zEndLow,globals.zEndHigh,globals.energyLow,globals.energyHigh);
			energyVsZSmall[sector].updateHistogram(paintSectors.get(i).getLastZ(),currentEnergy,globals.zEndLow,globals.zEndHigh,globals.energyLow,globals.energyHigh);
			radiusVsZAll.updateHistogram(paintSectors.get(i).getLastZ(),currentRadius,globals.zEndLow,globals.zEndHigh, globals.radiusLow,globals.radiusHigh);
			radiusVsZ[sector].updateHistogram(paintSectors.get(i).getLastZ(),currentRadius,globals.zEndLow,globals.zEndHigh, globals.radiusLow,globals.radiusHigh);
			radiusVsZSmall[sector].updateHistogram((paintSectors.get(i).getLastZ()),currentRadius,globals.zEndLow,globals.zEndHigh, globals.radiusLow,globals.radiusHigh);
			deltaEvsEAll.updateHistogram(currentEnergy,paintSectors.get(i).getDeltaE(),globals.energyLow,globals.energyHigh,globals.energyLow,globals.energyHigh);
			deltaEvsE[sector].updateHistogram(currentEnergy,paintSectors.get(i).getDeltaE(),globals.energyLow,globals.energyHigh,globals.energyLow,globals.energyHigh);
			deltaEvsESmall[sector].updateHistogram(currentEnergy,paintSectors.get(i).getDeltaE(),globals.energyLow,globals.energyHigh,globals.energyLow,globals.energyHigh);
			energyVsOriginAll.updateHistogram(currentOrigin,currentEnergy,globals.zLow,globals.zHigh, globals.energyLow,globals.energyHigh);
			energyVsOrigin[sector].updateHistogram(currentOrigin,currentEnergy,globals.zLow,globals.zHigh, globals.energyLow,globals.energyHigh);
			energyVsOriginSmall[sector].updateHistogram(currentOrigin,currentEnergy,globals.zLow,globals.zHigh, globals.energyLow,globals.energyHigh);
			energyVsTotalLengthAll.updateHistogram(paintSectors.get(i).getTotalLength(),currentEnergy, globals.lengthLow,globals.lengthHigh, globals.energyLow,globals.energyHigh);
			energyVsTotalLength[sector].updateHistogram(paintSectors.get(i).getTotalLength(),currentEnergy, globals.lengthLow,globals.lengthHigh, globals.energyLow,globals.energyHigh);
			energyVsTotalLengthSmall[sector].updateHistogram(paintSectors.get(i).getTotalLength(),currentEnergy, globals.lengthLow,globals.lengthHigh, globals.energyLow,globals.energyHigh);
		}
	}


	public synchronized void run()  { //read data
		if (DEBUG) System.out.println("start reading data");
		Input = null;
			
		while ((globals.gotContact) && (!globals.stop) ) {             //while incoming stream ok and no resets from main thread
			showTracks=globals.showTracks;                             // do we still want tracks?
			try {                                                      // read line
				Input = globals.incoming.readLine();
				if (DEBUG) System.out.println("read line: " + Input);
				if (Input == null) {
					if (DEBUG) System.err.println("Couldn't get I/O for reading.");
					globals.gotContact = false;
					break;
				}									
			} catch (IOException e) {
				if (DEBUG) System.err.println("Couldn't get I/O for reading.");
				globals.gotContact = false;
				break;
			}
			
			if (Input.indexOf(":")>=0){                                 //get voltage, pressure et al
				if (DEBUG) System.out.println("read line: " + Input);
				String[] temp=Input.split(":");
				if (DEBUG) System.out.println("name: " + temp[0] + "value: " + temp[1]);
				if (temp[0].matches("HV")) voltage=temp[1].trim();
				else if (temp[0].matches("P")) pressure=temp[1].trim();
				else if (temp[0].matches("R")) runNumber=temp[1].trim();
				else if (temp[0].matches("F")) flow=temp[1].trim();
				else if (temp[0].matches("PTE")) presentedEvents=temp[1].trim();
				else if (temp[0].matches("ATE")) acceptedEvents=temp[1].trim();
//				if (!DEBUG && !globals.freezeTrack) repaint(20,510,400,550);
			}

			else if ((Input.startsWith("#")) && (Input.substring(1).matches("[^a-zA-Z]"))){ //input starts with #
			    //			else if (Input.startsWith("#")){
				if (collectSectors.size()>0){            // general input with # and sector<8, check coincidences in sector
					if (globals.oneTrackPerSector && coincidenceList.contains(CurrentSector)){
						collectSectors.get(coincidenceList.indexOf(CurrentSector)).addData(collectSectors.get(collectSectors.size()-1).getData());
						collectSectors.remove(collectSectors.size()-1);
					}
					else {
						totalHits[CurrentSector]++;
						currentHits[CurrentSector]++;
						if (!coincidenceList.contains(CurrentSector)) coincidenceList.add(CurrentSector);
						int maxDisplay = (int)(200/globals.stepSize);
						if (globals.correctForShielding) maxDisplay = (int)(200/(1.4*globals.stepSize));
					
						if (currentHits[CurrentSector] > maxDisplay){   //histogram full, zero
							sectorIni();
							currentHits[CurrentSector]++;
//			if (!DEBUG && !globals.freezeTrack) repaint();
						}

					}
				}

				if ((Input.startsWith("#")) && (!Input.trim().endsWith("#"))){                                //input with # new sector
					readSector();
				}
				else if (Input.startsWith("##") && (Input.trim().endsWith("#")) &&  (CurrentSector != 8)){   //end of event, fill histograms
					if ((!globals.selectCoincidences) || ((globals.selectCoincidences) && (coincidenceList.size() > 1))){
						paintSectors.clear();
						paintSectors = new ArrayList<Sector>();

						sumEvents++;
						currentEvent = presentedEvents;
						eventInfo = false;
						for (int i =0 ; i<collectSectors.size(); i++){
							
							paintSectors.add(new Sector(collectSectors.get(i).getNumber(),collectSectors.get(i).getData()));
							totalEnergy += paintSectors.get(paintSectors.size()-1).getTotalEnergy();

							histBar[collectSectors.get(i).getNumber()].incHeight();
							histBar[collectSectors.get(i).getNumber()].setLabel(currentHits[collectSectors.get(i).getNumber()]);
							histBar[collectSectors.get(i).getNumber()].setTotalHits(totalHits[collectSectors.get(i).getNumber()]); 
							histCircle.incHeight(collectSectors.get(i).getNumber());
							
							sumTracks++;
						}
						collectSectors.clear();
						collectSectors = new ArrayList<Sector>();
						for (int i =0 ; i<paintSectors.size(); i++){
						
							currentEnergy =paintSectors.get(i).getTotalEnergy();
							if (currentEnergy>0){                
								fillHistograms(i,paintSectors.get(i).getNumber());
							}
							for (int j = 0; j < 4; j++){
								if ((paintSectors.get(i).getFirstZ() >= globals.sliceLimit[j]) && (paintSectors.get(i).getFirstZ() < globals.sliceLimit[j+4])) {
									sliceCircle[j].incHeight(paintSectors.get(i).getNumber());
									if (sliceCircle[j].getHeight(paintSectors.get(i).getNumber()) > 120) sliceCircle[i].reset(paintSectors.get(i).getNumber());
								}
							}
						}
						
						Integer[] coincidences = (Integer[])coincidenceList.toArray(new Integer[coincidenceList.size()]);
						for (int i =0 ; i<coincidences.length-1; i++){
							int sector = coincidences[i];
							for (int j =i+1 ; j<coincidences.length; j++){
								int sector2 = coincidences[j];
								coincidenceHist[sector].updateHistogram((double)sector2);
								coincidenceHist[sector2].updateHistogram((double)sector);
							}
						}


						if (!DEBUG && !globals.freezeTrack) repaint();

						totalEnergy=0;
						coincidenceList.clear();
					}

				}
			}
					
		else {
			if ((CurrentSector<8)) {                // get track only if wanted
				try {
					if (DEBUG) System.out.println("read sub-sub-line: " + Input);
					String[] splitData = Input.split("\t");
					double zPos = Double.parseDouble(splitData[0])-120;
					double radTemp = Double.parseDouble(splitData[1]);
					double enerTemp = Double.parseDouble(splitData[2]);
					collectSectors.get(collectSectors.size()-1).addPoint(zPos, radTemp, enerTemp);
					} catch (Exception e) {
						if (DEBUG) System.out.println("could not split input");
						unknownStrings++;
						unknownString=Input;
						if (!DEBUG && !globals.freezeTrack) repaint();
// 				} catch (ArrayIndexOutOfBoundsException e) {
// 					if (DEBUG) System.out.println("could not split input");
// 					globals.gotContact = false;
// 					collectSectors.clear();
// 					break;
				}
			}

		} 
	}
	
}
}
