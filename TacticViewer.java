// part of Taclet
// author: Ulrike Hager

import java.applet.Applet;
import java.io.*;
import java.net.*;
import java.awt.*;
import java.awt.geom.Arc2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Ellipse2D;
import java.awt.event.*;
import javax.swing.event.*;
import javax.swing.*;
import java.lang.*;

public class TacticViewer extends JApplet implements Runnable, ActionListener, ListSelectionListener, ChangeListener { //class TacticUI
	private static final boolean DEBUG = false;
	boolean allStop = false;
	Thread daqThread;
	Thread connectThread, mainThread;
	static Socket tacticSocket;
	GlobalVars globals;
	OutputPanel outputPanel;
	JLabel stepLabel, tmLabel, energyLabel;
	JSpinner stepSpinner, energySpinner, energyLowLimit, energyHighLimit, zLowLimit, zHighLimit, angleLowLimit, angleHighLimit, radiusLowLimit, radiusHighLimit, zEndLowLimit, zEndHighLimit, lengthLowLimit, lengthHighLimit, totalEnergyLowLimit, totalEnergyHighLimit;
	JSpinner[] sliceLimit;
	JPanel buttonPanel;
	JButton setButton, energyResetButton, scaleButton, resetTotalHitsButton;
	JCheckBox  setEnergyLimit, setZLimit, setAngleLimit, setRadiusLimit, setZEndLimit, setLengthLimit, zCut, angleCut, energyCut, totalEnergyCut, setTotalEnergyLimit,deltaECut;
	JList pageChoice, displayChoice;
	String hostName;
	JToggleButton freezeButton;
	JMenuBar menuBar;
	JMenu settingsMenu, viewMenu, mainMenu;
	JCheckBoxMenuItem  correctForShielding, selectCoincidences, drawLine, drawTrack, loopFile, singlePopup, oneTrackPerSector, logScale;
	
	class provideSocket implements Runnable {
		public void waitForSocket(){ //if server didn't answer, wait and try again
			synchronized(this) {
				try {
					wait(500);
				} catch (InterruptedException e) { }
			}
		}

		public void run() { //connect Socket
			while (!globals.gotContact){
				if (DEBUG) System.out.println("running connection thread");
				if (allStop) return;
				try {
					tacticSocket = new Socket(hostName, 4711);
					globals.incoming = new BufferedReader(new InputStreamReader(tacticSocket.getInputStream()));
					globals.gotContact = true;
					if (DEBUG) System.out.println("connected");
				} catch (UnknownHostException e) {
					System.err.println("Unknown host.");
				} catch (IOException e) {
					if (DEBUG) System.err.println("Couldn't get I/O for the connection.");
					waitForSocket();
				}
			}
		}
	}

	public void layoutButtonPanel(){
		buttonPanel.setLayout(new GridBagLayout());
		buttonPanel.setBackground(Color.WHITE);
		GridBagConstraints c = new GridBagConstraints();

		c.anchor = GridBagConstraints.FIRST_LINE_START;
		c.insets = new Insets(5,0,10,0);
		c.gridheight = 14;
		c.weightx = 0.5;
		c.weighty = 14;
		c.gridx = 0;
		c.gridy = 0;
		buttonPanel.add(pageChoice,c);

		c.anchor = GridBagConstraints.FIRST_LINE_END;
//		c.fill = GridBagConstraints.HORIZONTAL;
//		c.gridwidth = 1;
		c.insets = new Insets(0,0,0,0);
		c.gridheight = 1;
		c.weightx = 0.5;
		c.weighty = 1;
		c.gridx = 1;
		c.gridy = 0;
		buttonPanel.add(menuBar,c);
		
		c.anchor = GridBagConstraints.LAST_LINE_END;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridwidth = 1;
		c.insets = new Insets(0,0,0,0);
		c.gridheight = 1;
		c.weightx = 0.5;
		c.weighty = 1;
		c.gridx = 1;
		c.gridy = 18;
		buttonPanel.add(freezeButton,c);
		
		c.anchor = GridBagConstraints.LAST_LINE_END;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = new Insets(5,0,0,0);
		c.gridwidth = 1;
		c.gridx = 1;
		c.gridy = 19;
		buttonPanel.add(energyResetButton,c);

		c.anchor = GridBagConstraints.LAST_LINE_END;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridwidth = 1;
		c.weightx = 0.5;
		c.weighty = 1;
		c.gridx = 1;
		c.gridy = 22;
		buttonPanel.add(resetTotalHitsButton,c);

		c.anchor = GridBagConstraints.LAST_LINE_END;
		c.gridwidth = 1;
		c.weightx = 0.6;
		c.weighty = 0.6;
		c.gridx = 1;
		c.gridy = 23;
		buttonPanel.add(tmLabel,c);

		switch(globals.page){
		case 0:
			c.anchor = GridBagConstraints.LAST_LINE_END;
			c.fill = GridBagConstraints.HORIZONTAL;
			c.insets = new Insets(0,0,0,0);
			c.weightx = 0.5;
			c.weighty = 1;
			c.weighty = 0;
			c.gridx = 0;
			c.gridy = 15;
			buttonPanel.add(stepSpinner,c);
		
			c.anchor = GridBagConstraints.LAST_LINE_END;
			c.fill = GridBagConstraints.HORIZONTAL;
			c.gridwidth = 1;
			c.weightx = 0.5;
			c.weighty = 1;
			c.gridx = 1;
			c.gridy = 15;
			buttonPanel.add(setButton,c);

			c.anchor = GridBagConstraints.LAST_LINE_END;
			c.fill = GridBagConstraints.HORIZONTAL;
			c.gridwidth = 1;
			c.insets = new Insets(0,0,5,0);
			c.weightx = 0.5;
			c.weighty = 1;
			c.gridx = 0;
			c.gridy = 16;
			buttonPanel.add(energySpinner,c);

			c.anchor = GridBagConstraints.LAST_LINE_END;
			c.fill = GridBagConstraints.HORIZONTAL;
			c.gridwidth = 1;
			c.weightx = 0.5;
			c.weighty = 1;
			c.gridx = 1;
			c.gridy = 16;
			buttonPanel.add(scaleButton,c);
			break;
			
		case 1:
			for (int i = 0; i<4; i++){
			c.anchor = GridBagConstraints.LAST_LINE_END;
			c.fill = GridBagConstraints.HORIZONTAL;
			c.insets = new Insets(0,0,0,0);
			c.gridwidth = 1;
			c.gridheight = 1;
			c.weightx = 0.5;
			c.weighty = 1;
			c.gridx = 0;
			c.gridy = 14+i;
			buttonPanel.add(sliceLimit[i],c);

			c.gridwidth = 1;
			c.gridheight = 1;
			c.weightx = 0.5;
			c.weighty = 1;
			c.gridx = 1;
			c.gridy = 14+i;
			buttonPanel.add(sliceLimit[i+4],c);
			}
			break;			

		case 2:
			c.anchor = GridBagConstraints.LAST_LINE_END;
			c.fill = GridBagConstraints.HORIZONTAL;
			c.insets = new Insets(0,0,0,0);
			c.gridwidth = 1;
			c.gridheight = 1;
			c.weightx = 0.5;
			c.weighty = 1;
			c.gridx = 0;
			c.gridy = 14;
			buttonPanel.add(setTotalEnergyLimit,c);

			c.anchor = GridBagConstraints.LAST_LINE_END;
			c.fill = GridBagConstraints.HORIZONTAL;
			c.insets = new Insets(0,0,0,0);
			c.gridwidth = 1;
			c.gridheight = 1;
			c.weightx = 0.5;
			c.weighty = 1;
			c.gridx = 1;
			c.gridy = 14;
			buttonPanel.add(totalEnergyCut,c);

			c.anchor = GridBagConstraints.LAST_LINE_END;
			c.fill = GridBagConstraints.HORIZONTAL;
			c.insets = new Insets(0,0,0,0);
			c.gridwidth = 1;
			c.gridheight = 1;
			c.weightx = 0.5;
			c.weighty = 1;
			c.gridx = 0;
			c.gridy = 15;
			buttonPanel.add(totalEnergyLowLimit,c);

			c.anchor = GridBagConstraints.LAST_LINE_END;
			c.fill = GridBagConstraints.HORIZONTAL;
			c.insets = new Insets(0,0,0,0);
			c.gridwidth = 1;
			c.gridheight = 1;
			c.weightx = 0.5;
			c.weighty = 1;
			c.gridx = 1;
			c.gridy = 15;
			buttonPanel.add(totalEnergyHighLimit,c);

			break;

		case 3:
			c.anchor = GridBagConstraints.LAST_LINE_END;
			c.fill = GridBagConstraints.HORIZONTAL;
			c.insets = new Insets(0,0,0,0);
			c.gridwidth = 1;
			c.gridheight = 1;
			c.weightx = 0.5;
			c.weighty = 1;
			c.gridx = 0;
			c.gridy = 14;
			buttonPanel.add(setEnergyLimit,c);

			c.anchor = GridBagConstraints.LAST_LINE_END;
			c.fill = GridBagConstraints.HORIZONTAL;
			c.insets = new Insets(0,0,0,0);
			c.gridwidth = 1;
			c.gridheight = 1;
			c.weightx = 0.5;
			c.weighty = 1;
			c.gridx = 1;
			c.gridy = 14;
			buttonPanel.add(energyCut,c);

			c.anchor = GridBagConstraints.LAST_LINE_END;
			c.fill = GridBagConstraints.HORIZONTAL;
			c.insets = new Insets(0,0,0,0);
			c.gridwidth = 1;
			c.gridheight = 1;
			c.weightx = 0.5;
			c.weighty = 1;
			c.gridx = 0;
			c.gridy = 15;
			buttonPanel.add(energyLowLimit,c);

			c.anchor = GridBagConstraints.LAST_LINE_END;
			c.fill = GridBagConstraints.HORIZONTAL;
			c.insets = new Insets(0,0,0,0);
			c.gridwidth = 1;
			c.gridheight = 1;
			c.weightx = 0.5;
			c.weighty = 1;
			c.gridx = 1;
			c.gridy = 15;
			buttonPanel.add(energyHighLimit,c);

			c.anchor = GridBagConstraints.FIRST_LINE_START;
			c.insets = new Insets(30,0,0,0);
			c.gridheight = 4;
			c.weightx = 0.5;
			c.gridx = 1;
			c.gridy = 0;
			buttonPanel.add(displayChoice,c);

			break;
		
		case 4:
			c.anchor = GridBagConstraints.LAST_LINE_END;
			c.fill = GridBagConstraints.HORIZONTAL;
			c.insets = new Insets(0,0,0,0);
			c.gridwidth = 1;
			c.gridheight = 1;
			c.weightx = 0.5;
			c.weighty = 1;
			c.gridx = 0;
			c.gridy = 14;
			buttonPanel.add(setZLimit,c);

			c.anchor = GridBagConstraints.LAST_LINE_END;
			c.fill = GridBagConstraints.HORIZONTAL;
			c.insets = new Insets(0,0,0,0);
			c.gridwidth = 1;
			c.gridheight = 1;
			c.weightx = 0.5;
			c.weighty = 1;
			c.gridx = 1;
			c.gridy = 14;
			buttonPanel.add(zCut,c);

			c.anchor = GridBagConstraints.LAST_LINE_END;
			c.fill = GridBagConstraints.HORIZONTAL;
			c.insets = new Insets(0,0,0,0);
			c.gridwidth = 1;
			c.gridheight = 1;
			c.weightx = 0.5;
			c.weighty = 1;
			c.gridx = 0;
			c.gridy = 15;
			buttonPanel.add(zLowLimit,c);

			c.anchor = GridBagConstraints.LAST_LINE_END;
			c.fill = GridBagConstraints.HORIZONTAL;
			c.insets = new Insets(0,0,0,0);
			c.gridwidth = 1;
			c.gridheight = 1;
			c.weightx = 0.5;
			c.weighty = 1;
			c.gridx = 1;
			c.gridy = 15;
			buttonPanel.add(zHighLimit,c);

			c.anchor = GridBagConstraints.FIRST_LINE_START;
			c.insets = new Insets(30,0,0,0);
			c.gridheight = 4;
			c.weightx = 0.5;
			c.gridx = 1;
			c.gridy = 0;
			buttonPanel.add(displayChoice,c);
			break;

		case 5:
			c.anchor = GridBagConstraints.LAST_LINE_END;
			c.fill = GridBagConstraints.HORIZONTAL;
			c.insets = new Insets(0,0,0,0);
			c.gridwidth = 1;
			c.gridheight = 1;
			c.weightx = 0.5;
			c.weighty = 1;
			c.gridx = 0;
			c.gridy = 16;
			buttonPanel.add(setEnergyLimit,c);

			c.anchor = GridBagConstraints.LAST_LINE_END;
			c.fill = GridBagConstraints.HORIZONTAL;
			c.insets = new Insets(0,0,0,0);
			c.gridwidth = 1;
			c.gridheight = 1;
			c.weightx = 0.5;
			c.weighty = 1;
			c.gridx = 1;
			c.gridy = 16;
			buttonPanel.add(energyCut,c);

			c.anchor = GridBagConstraints.LAST_LINE_END;
			c.fill = GridBagConstraints.HORIZONTAL;
			c.insets = new Insets(0,0,0,0);
			c.gridwidth = 1;
			c.gridheight = 1;
			c.weightx = 0.5;
			c.weighty = 1;
			c.gridx = 0;
			c.gridy = 17;
			buttonPanel.add(energyLowLimit,c);

			c.anchor = GridBagConstraints.LAST_LINE_END;
			c.fill = GridBagConstraints.HORIZONTAL;
			c.insets = new Insets(0,0,0,0);
			c.gridwidth = 1;
			c.gridheight = 1;
			c.weightx = 0.5;
			c.weighty = 1;
			c.gridx = 1;
			c.gridy = 17;
			buttonPanel.add(energyHighLimit,c);

			c.anchor = GridBagConstraints.FIRST_LINE_START;
			c.insets = new Insets(30,0,0,0);
			c.gridheight = 4;
			c.weightx = 0.5;
			c.gridx = 1;
			c.gridy = 0;
			buttonPanel.add(displayChoice,c);
			break;

		case 6:
			c.anchor = GridBagConstraints.LAST_LINE_END;
			c.fill = GridBagConstraints.HORIZONTAL;
			c.insets = new Insets(0,0,0,0);
			c.gridwidth = 1;
			c.gridheight = 1;
			c.weightx = 0.5;
			c.weighty = 1;
			c.gridx = 0;
			c.gridy = 14;
			buttonPanel.add(setZEndLimit,c);

			c.anchor = GridBagConstraints.LAST_LINE_END;
			c.fill = GridBagConstraints.HORIZONTAL;
			c.insets = new Insets(0,0,0,0);
			c.gridwidth = 1;
			c.gridheight = 1;
			c.weightx = 0.5;
			c.weighty = 1;
			c.gridx = 0;
			c.gridy = 15;
			buttonPanel.add(zEndLowLimit,c);

			c.anchor = GridBagConstraints.LAST_LINE_END;
			c.fill = GridBagConstraints.HORIZONTAL;
			c.insets = new Insets(0,0,0,0);
			c.gridwidth = 1;
			c.gridheight = 1;
			c.weightx = 0.5;
			c.weighty = 1;
			c.gridx = 1;
			c.gridy = 15;
			buttonPanel.add(zEndHighLimit,c);

			c.anchor = GridBagConstraints.LAST_LINE_END;
			c.fill = GridBagConstraints.HORIZONTAL;
			c.insets = new Insets(0,0,0,0);
			c.gridwidth = 1;
			c.gridheight = 1;
			c.weightx = 0.5;
			c.weighty = 1;
			c.gridx = 0;
			c.gridy = 16;
			buttonPanel.add(setEnergyLimit,c);

			c.anchor = GridBagConstraints.LAST_LINE_END;
			c.fill = GridBagConstraints.HORIZONTAL;
			c.insets = new Insets(0,0,0,0);
			c.gridwidth = 1;
			c.gridheight = 1;
			c.weightx = 0.5;
			c.weighty = 1;
			c.gridx = 1;
			c.gridy = 16;
			buttonPanel.add(energyCut,c);

			c.anchor = GridBagConstraints.LAST_LINE_END;
			c.fill = GridBagConstraints.HORIZONTAL;
			c.insets = new Insets(0,0,0,0);
			c.gridwidth = 1;
			c.gridheight = 1;
			c.weightx = 0.5;
			c.weighty = 1;
			c.gridx = 0;
			c.gridy = 17;
			buttonPanel.add(energyLowLimit,c);

			c.anchor = GridBagConstraints.LAST_LINE_END;
			c.fill = GridBagConstraints.HORIZONTAL;
			c.insets = new Insets(0,0,0,0);
			c.gridwidth = 1;
			c.gridheight = 1;
			c.weightx = 0.5;
			c.weighty = 1;
			c.gridx = 1;
			c.gridy = 17;
			buttonPanel.add(energyHighLimit,c);

			c.anchor = GridBagConstraints.FIRST_LINE_START;
			c.insets = new Insets(30,0,0,0);
			c.gridheight = 4;
			c.weightx = 0.5;
			c.gridx = 1;
			c.gridy = 0;
			buttonPanel.add(displayChoice,c);
			break;

		case 7:
			c.anchor = GridBagConstraints.LAST_LINE_END;
			c.fill = GridBagConstraints.HORIZONTAL;
			c.insets = new Insets(0,0,0,0);
			c.gridwidth = 1;
			c.gridheight = 1;
			c.weightx = 0.5;
			c.weighty = 1;
			c.gridx = 0;
			c.gridy = 14;
			buttonPanel.add(setZEndLimit,c);

			c.anchor = GridBagConstraints.LAST_LINE_END;
			c.fill = GridBagConstraints.HORIZONTAL;
			c.insets = new Insets(0,0,0,0);
			c.gridwidth = 1;
			c.gridheight = 1;
			c.weightx = 0.5;
			c.weighty = 1;
			c.gridx = 0;
			c.gridy = 15;
			buttonPanel.add(zEndLowLimit,c);

			c.anchor = GridBagConstraints.LAST_LINE_END;
			c.fill = GridBagConstraints.HORIZONTAL;
			c.insets = new Insets(0,0,0,0);
			c.gridwidth = 1;
			c.gridheight = 1;
			c.weightx = 0.5;
			c.weighty = 1;
			c.gridx = 1;
			c.gridy = 15;
			buttonPanel.add(zEndHighLimit,c);

			c.anchor = GridBagConstraints.LAST_LINE_END;
			c.fill = GridBagConstraints.HORIZONTAL;
			c.insets = new Insets(0,0,0,0);
			c.gridwidth = 1;
			c.gridheight = 1;
			c.weightx = 0.5;
			c.weighty = 1;
			c.gridx = 0;
			c.gridy = 16;
			buttonPanel.add(setRadiusLimit,c);

			c.anchor = GridBagConstraints.LAST_LINE_END;
			c.fill = GridBagConstraints.HORIZONTAL;
			c.insets = new Insets(0,0,0,0);
			c.gridwidth = 1;
			c.gridheight = 1;
			c.weightx = 0.5;
			c.weighty = 1;
			c.gridx = 0;
			c.gridy = 17;
			buttonPanel.add(radiusLowLimit,c);

			c.anchor = GridBagConstraints.LAST_LINE_END;
			c.fill = GridBagConstraints.HORIZONTAL;
			c.insets = new Insets(0,0,0,0);
			c.gridwidth = 1;
			c.gridheight = 1;
			c.weightx = 0.5;
			c.weighty = 1;
			c.gridx = 1;
			c.gridy = 17;
			buttonPanel.add(radiusHighLimit,c);

			c.anchor = GridBagConstraints.FIRST_LINE_START;
			c.insets = new Insets(30,0,0,0);
			c.gridheight = 4;
			c.weightx = 0.5;
			c.gridx = 1;
			c.gridy = 0;
			buttonPanel.add(displayChoice,c);
			break;
		

		case 8:
			c.anchor = GridBagConstraints.LAST_LINE_END;
			c.fill = GridBagConstraints.HORIZONTAL;
			c.insets = new Insets(0,0,0,0);
			c.gridwidth = 1;
			c.gridheight = 1;
			c.weightx = 0.5;
			c.weighty = 1;
			c.gridx = 0;
			c.gridy = 14;
			buttonPanel.add(setEnergyLimit,c);

			c.anchor = GridBagConstraints.LAST_LINE_END;
			c.fill = GridBagConstraints.HORIZONTAL;
			c.insets = new Insets(0,0,0,0);
			c.gridwidth = 1;
			c.gridheight = 1;
			c.weightx = 0.5;
			c.weighty = 1;
			c.gridx = 1;
			c.gridy = 14;
			buttonPanel.add(energyCut,c);

			c.anchor = GridBagConstraints.LAST_LINE_END;
			c.fill = GridBagConstraints.HORIZONTAL;
			c.insets = new Insets(0,0,0,0);
			c.gridwidth = 1;
			c.gridheight = 1;
			c.weightx = 0.5;
			c.weighty = 1;
			c.gridx = 0;
			c.gridy = 15;
			buttonPanel.add(energyLowLimit,c);

			c.anchor = GridBagConstraints.LAST_LINE_END;
			c.fill = GridBagConstraints.HORIZONTAL;
			c.insets = new Insets(0,0,0,0);
			c.gridwidth = 1;
			c.gridheight = 1;
			c.weightx = 0.5;
			c.weighty = 1;
			c.gridx = 1;
			c.gridy = 15;
			buttonPanel.add(energyHighLimit,c);

			c.anchor = GridBagConstraints.LAST_LINE_END;
			c.fill = GridBagConstraints.HORIZONTAL;
			c.insets = new Insets(0,0,0,0);
			c.gridwidth = 1;
			c.gridheight = 1;
			c.weightx = 0.5;
			c.weighty = 1;
			c.gridx = 0;
			c.gridy = 16;
			buttonPanel.add(setZLimit,c);

			c.anchor = GridBagConstraints.LAST_LINE_END;
			c.fill = GridBagConstraints.HORIZONTAL;
			c.insets = new Insets(0,0,0,0);
			c.gridwidth = 1;
			c.gridheight = 1;
			c.weightx = 0.5;
			c.weighty = 1;
			c.gridx = 1;
			c.gridy = 16;
			buttonPanel.add(zCut,c);

			c.anchor = GridBagConstraints.LAST_LINE_END;
			c.fill = GridBagConstraints.HORIZONTAL;
			c.insets = new Insets(0,0,0,0);
			c.gridwidth = 1;
			c.gridheight = 1;
			c.weightx = 0.5;
			c.weighty = 1;
			c.gridx = 0;
			c.gridy = 17;
			buttonPanel.add(zLowLimit,c);

			c.anchor = GridBagConstraints.LAST_LINE_END;
			c.fill = GridBagConstraints.HORIZONTAL;
			c.insets = new Insets(0,0,0,0);
			c.gridwidth = 1;
			c.gridheight = 1;
			c.weightx = 0.5;
			c.weighty = 1;
			c.gridx = 1;
			c.gridy = 17;
			buttonPanel.add(zHighLimit,c);

			c.anchor = GridBagConstraints.FIRST_LINE_START;
			c.insets = new Insets(30,0,0,0);
			c.gridheight = 4;
			c.weightx = 0.5;
			c.gridx = 1;
			c.gridy = 0;
			buttonPanel.add(displayChoice,c);
			break;

		case 9:
			c.anchor = GridBagConstraints.LAST_LINE_END;
			c.fill = GridBagConstraints.HORIZONTAL;
			c.insets = new Insets(0,0,0,0);
			c.gridwidth = 1;
			c.gridheight = 1;
			c.weightx = 0.5;
			c.weighty = 1;
			c.gridx = 0;
			c.gridy = 14;
			buttonPanel.add(setAngleLimit,c);

			c.anchor = GridBagConstraints.LAST_LINE_END;
			c.fill = GridBagConstraints.HORIZONTAL;
			c.insets = new Insets(0,0,0,0);
			c.gridwidth = 1;
			c.gridheight = 1;
			c.weightx = 0.5;
			c.weighty = 1;
			c.gridx = 1;
			c.gridy = 14;
			buttonPanel.add(angleCut,c);

			c.anchor = GridBagConstraints.LAST_LINE_END;
			c.fill = GridBagConstraints.HORIZONTAL;
			c.insets = new Insets(0,0,0,0);
			c.gridwidth = 1;
			c.gridheight = 1;
			c.weightx = 0.5;
			c.weighty = 1;
			c.gridx = 0;
			c.gridy = 15;
			buttonPanel.add(angleLowLimit,c);

			c.anchor = GridBagConstraints.LAST_LINE_END;
			c.fill = GridBagConstraints.HORIZONTAL;
			c.insets = new Insets(0,0,0,0);
			c.gridwidth = 1;
			c.gridheight = 1;
			c.weightx = 0.5;
			c.weighty = 1;
			c.gridx = 1;
			c.gridy = 15;
			buttonPanel.add(angleHighLimit,c);		

			c.anchor = GridBagConstraints.FIRST_LINE_START;
			c.insets = new Insets(30,0,0,0);
			c.gridheight = 4;
			c.weightx = 0.5;
			c.gridx = 1;
			c.gridy = 0;
			buttonPanel.add(displayChoice,c);
			break;

		case 10:
			c.anchor = GridBagConstraints.LAST_LINE_END;
			c.fill = GridBagConstraints.HORIZONTAL;
			c.insets = new Insets(0,0,0,0);
			c.gridwidth = 1;
			c.gridheight = 1;
			c.weightx = 0.5;
			c.weighty = 1;
			c.gridx = 0;
			c.gridy = 14;
			buttonPanel.add(setEnergyLimit,c);

			c.anchor = GridBagConstraints.LAST_LINE_END;
			c.fill = GridBagConstraints.HORIZONTAL;
			c.insets = new Insets(0,0,0,0);
			c.gridwidth = 1;
			c.gridheight = 1;
			c.weightx = 0.5;
			c.weighty = 1;
			c.gridx = 1;
			c.gridy = 14;
			buttonPanel.add(energyCut,c);

			c.anchor = GridBagConstraints.LAST_LINE_END;
			c.fill = GridBagConstraints.HORIZONTAL;
			c.insets = new Insets(0,0,0,0);
			c.gridwidth = 1;
			c.gridheight = 1;
			c.weightx = 0.5;
			c.weighty = 1;
			c.gridx = 0;
			c.gridy = 15;
			buttonPanel.add(energyLowLimit,c);

			c.anchor = GridBagConstraints.LAST_LINE_END;
			c.fill = GridBagConstraints.HORIZONTAL;
			c.insets = new Insets(0,0,0,0);
			c.gridwidth = 1;
			c.gridheight = 1;
			c.weightx = 0.5;
			c.weighty = 1;
			c.gridx = 1;
			c.gridy = 15;
			buttonPanel.add(energyHighLimit,c);

			c.anchor = GridBagConstraints.LAST_LINE_END;
			c.fill = GridBagConstraints.HORIZONTAL;
			c.insets = new Insets(0,0,0,0);
			c.gridwidth = 1;
			c.gridheight = 1;
			c.weightx = 0.5;
			c.weighty = 1;
			c.gridx = 0;
			c.gridy = 16;
			buttonPanel.add(setAngleLimit,c);

			c.anchor = GridBagConstraints.LAST_LINE_END;
			c.fill = GridBagConstraints.HORIZONTAL;
			c.insets = new Insets(0,0,0,0);
			c.gridwidth = 1;
			c.gridheight = 1;
			c.weightx = 0.5;
			c.weighty = 1;
			c.gridx = 1;
			c.gridy = 16;
			buttonPanel.add(angleCut,c);

			c.anchor = GridBagConstraints.LAST_LINE_END;
			c.fill = GridBagConstraints.HORIZONTAL;
			c.insets = new Insets(0,0,0,0);
			c.gridwidth = 1;
			c.gridheight = 1;
			c.weightx = 0.5;
			c.weighty = 1;
			c.gridx = 0;
			c.gridy = 17;
			buttonPanel.add(angleLowLimit,c);

			c.anchor = GridBagConstraints.LAST_LINE_END;
			c.fill = GridBagConstraints.HORIZONTAL;
			c.insets = new Insets(0,0,0,0);
			c.gridwidth = 1;
			c.gridheight = 1;
			c.weightx = 0.5;
			c.weighty = 1;
			c.gridx = 1;
			c.gridy = 17;
			buttonPanel.add(angleHighLimit,c);

			c.anchor = GridBagConstraints.FIRST_LINE_START;
			c.insets = new Insets(30,0,0,0);
			c.gridheight = 4;
			c.weightx = 0.5;
			c.gridx = 1;
			c.gridy = 0;
			buttonPanel.add(displayChoice,c);
			break;

		case 11:
			c.anchor = GridBagConstraints.LAST_LINE_END;
			c.fill = GridBagConstraints.HORIZONTAL;
			c.insets = new Insets(0,0,0,0);
			c.gridwidth = 1;
			c.gridheight = 1;
			c.weightx = 0.5;
			c.weighty = 1;
			c.gridx = 0;
			c.gridy = 14;
			buttonPanel.add(setZLimit,c);

			c.anchor = GridBagConstraints.LAST_LINE_END;
			c.fill = GridBagConstraints.HORIZONTAL;
			c.insets = new Insets(0,0,0,0);
			c.gridwidth = 1;
			c.gridheight = 1;
			c.weightx = 0.5;
			c.weighty = 1;
			c.gridx = 1;
			c.gridy = 14;
			buttonPanel.add(zCut,c);

			c.anchor = GridBagConstraints.LAST_LINE_END;
			c.fill = GridBagConstraints.HORIZONTAL;
			c.insets = new Insets(0,0,0,0);
			c.gridwidth = 1;
			c.gridheight = 1;
			c.weightx = 0.5;
			c.weighty = 1;
			c.gridx = 0;
			c.gridy = 15;
			buttonPanel.add(zLowLimit,c);

			c.anchor = GridBagConstraints.LAST_LINE_END;
			c.fill = GridBagConstraints.HORIZONTAL;
			c.insets = new Insets(0,0,0,0);
			c.gridwidth = 1;
			c.gridheight = 1;
			c.weightx = 0.5;
			c.weighty = 1;
			c.gridx = 1;
			c.gridy = 15;
			buttonPanel.add(zHighLimit,c);

			c.anchor = GridBagConstraints.LAST_LINE_END;
			c.fill = GridBagConstraints.HORIZONTAL;
			c.insets = new Insets(0,0,0,0);
			c.gridwidth = 1;
			c.gridheight = 1;
			c.weightx = 0.5;
			c.weighty = 1;
			c.gridx = 0;
			c.gridy = 16;
			buttonPanel.add(setAngleLimit,c);

			c.anchor = GridBagConstraints.LAST_LINE_END;
			c.fill = GridBagConstraints.HORIZONTAL;
			c.insets = new Insets(0,0,0,0);
			c.gridwidth = 1;
			c.gridheight = 1;
			c.weightx = 0.5;
			c.weighty = 1;
			c.gridx = 1;
			c.gridy = 16;
			buttonPanel.add(angleCut,c);

			c.anchor = GridBagConstraints.LAST_LINE_END;
			c.fill = GridBagConstraints.HORIZONTAL;
			c.insets = new Insets(0,0,0,0);
			c.gridwidth = 1;
			c.gridheight = 1;
			c.weightx = 0.5;
			c.weighty = 1;
			c.gridx = 0;
			c.gridy = 17;
			buttonPanel.add(angleLowLimit,c);

			c.anchor = GridBagConstraints.LAST_LINE_END;
			c.fill = GridBagConstraints.HORIZONTAL;
			c.insets = new Insets(0,0,0,0);
			c.gridwidth = 1;
			c.gridheight = 1;
			c.weightx = 0.5;
			c.weighty = 1;
			c.gridx = 1;
			c.gridy = 17;
			buttonPanel.add(angleHighLimit,c);

			c.anchor = GridBagConstraints.FIRST_LINE_START;
			c.insets = new Insets(30,0,0,0);
			c.gridheight = 4;
			c.weightx = 0.5;
			c.gridx = 1;
			c.gridy = 0;
			buttonPanel.add(displayChoice,c);
			break;

		case 12:
			c.anchor = GridBagConstraints.LAST_LINE_END;
			c.fill = GridBagConstraints.HORIZONTAL;
			c.insets = new Insets(0,0,0,0);
			c.gridwidth = 1;
			c.gridheight = 1;
			c.weightx = 0.5;
			c.weighty = 1;
			c.gridx = 0;
			c.gridy = 14;
			buttonPanel.add(setLengthLimit,c);

			c.anchor = GridBagConstraints.LAST_LINE_END;
			c.fill = GridBagConstraints.HORIZONTAL;
			c.insets = new Insets(0,0,0,0);
			c.gridwidth = 1;
			c.gridheight = 1;
			c.weightx = 0.5;
			c.weighty = 1;
			c.gridx = 0;
			c.gridy = 15;
			buttonPanel.add(lengthLowLimit,c);

			c.anchor = GridBagConstraints.LAST_LINE_END;
			c.fill = GridBagConstraints.HORIZONTAL;
			c.insets = new Insets(0,0,0,0);
			c.gridwidth = 1;
			c.gridheight = 1;
			c.weightx = 0.5;
			c.weighty = 1;
			c.gridx = 1;
			c.gridy = 15;
			buttonPanel.add(lengthHighLimit,c);

			c.anchor = GridBagConstraints.LAST_LINE_END;
			c.fill = GridBagConstraints.HORIZONTAL;
			c.insets = new Insets(0,0,0,0);
			c.gridwidth = 1;
			c.gridheight = 1;
			c.weightx = 0.5;
			c.weighty = 1;
			c.gridx = 0;
			c.gridy = 16;
			buttonPanel.add(setEnergyLimit,c);

			c.anchor = GridBagConstraints.LAST_LINE_END;
			c.fill = GridBagConstraints.HORIZONTAL;
			c.insets = new Insets(0,0,0,0);
			c.gridwidth = 1;
			c.gridheight = 1;
			c.weightx = 0.5;
			c.weighty = 1;
			c.gridx = 1;
			c.gridy = 16;
			buttonPanel.add(energyCut,c);

			c.anchor = GridBagConstraints.LAST_LINE_END;
			c.fill = GridBagConstraints.HORIZONTAL;
			c.insets = new Insets(0,0,0,0);
			c.gridwidth = 1;
			c.gridheight = 1;
			c.weightx = 0.5;
			c.weighty = 1;
			c.gridx = 0;
			c.gridy = 17;
			buttonPanel.add(energyLowLimit,c);

			c.anchor = GridBagConstraints.LAST_LINE_END;
			c.fill = GridBagConstraints.HORIZONTAL;
			c.insets = new Insets(0,0,0,0);
			c.gridwidth = 1;
			c.gridheight = 1;
			c.weightx = 0.5;
			c.weighty = 1;
			c.gridx = 1;
			c.gridy = 17;
			buttonPanel.add(energyHighLimit,c);

			c.anchor = GridBagConstraints.FIRST_LINE_START;
			c.insets = new Insets(30,0,0,0);
			c.gridheight = 4;
			c.weightx = 0.5;
			c.gridx = 1;
			c.gridy = 0;
			buttonPanel.add(displayChoice,c);
			break;

			
		case 13:
			c.anchor = GridBagConstraints.LAST_LINE_END;
			c.fill = GridBagConstraints.HORIZONTAL;
			c.insets = new Insets(0,0,0,0);
			c.gridwidth = 1;
			c.gridheight = 1;
			c.weightx = 0.5;
			c.weighty = 1;
			c.gridx = 0;
			c.gridy = 14;
			buttonPanel.add(setRadiusLimit,c);

			c.anchor = GridBagConstraints.LAST_LINE_END;
			c.fill = GridBagConstraints.HORIZONTAL;
			c.insets = new Insets(0,0,0,0);
			c.gridwidth = 1;
			c.gridheight = 1;
			c.weightx = 0.5;
			c.weighty = 1;
			c.gridx = 0;
			c.gridy = 15;
			buttonPanel.add(radiusLowLimit,c);

			c.anchor = GridBagConstraints.LAST_LINE_END;
			c.fill = GridBagConstraints.HORIZONTAL;
			c.insets = new Insets(0,0,0,0);
			c.gridwidth = 1;
			c.gridheight = 1;
			c.weightx = 0.5;
			c.weighty = 1;
			c.gridx = 1;
			c.gridy = 15;
			buttonPanel.add(radiusHighLimit,c);

			c.anchor = GridBagConstraints.FIRST_LINE_START;
			c.insets = new Insets(30,0,0,0);
			c.gridheight = 4;
			c.weightx = 0.5;
			c.gridx = 1;
			c.gridy = 0;
			buttonPanel.add(displayChoice,c);
			break;
		
		case 14:
			c.anchor = GridBagConstraints.FIRST_LINE_START;
			c.insets = new Insets(30,0,0,0);
			c.gridheight = 4;
			c.weightx = 0.5;
			c.gridx = 1;
			c.gridy = 0;
			buttonPanel.add(displayChoice,c);
			break;
			
		}
		
	}
	
	public void init(){
		globals = new GlobalVars();
		hostName = getParameter("hostName");
		if (hostName == null) hostName = "itactic.triumf.ca";
		outputPanel = new OutputPanel(globals);
		buttonPanel = new JPanel();
		menuBar = new JMenuBar();
//		menuBar.setLayout(new BoxLayout(menuBar, BoxLayout.PAGE_AXIS));
		settingsMenu = new JMenu("Settings");
		settingsMenu.setMnemonic(KeyEvent.VK_A);
		settingsMenu.getAccessibleContext().setAccessibleDescription("Various settings");
		viewMenu = new JMenu("View");
		mainMenu = new JMenu("Menu");
		mainMenu.add(settingsMenu);
		mainMenu.add(viewMenu);
		menuBar.add(mainMenu);

		String[] masterPages = {"Hits&Tracks", "Beachball/z","Total energy/event","Energy","Z origin","dE vs E","E vs Z end","R end vs Z end", "E vs Z origin", "Angle", "E vs Angle", "Z origin vs Angle", "E vs length", "R end", "Coincidences"};
		pageChoice = new JList(masterPages);
		pageChoice.setSelectedIndex(0);
		pageChoice.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		pageChoice.addListSelectionListener(this);
        pageChoice.setVisibleRowCount(15);

		String[] subPages ={"Sum", "Overview", "0-3","4-7"};

		displayChoice = new JList(subPages);
		displayChoice.setSelectedIndex(globals.display);
		displayChoice.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		displayChoice.addListSelectionListener(this);
        displayChoice.setVisibleRowCount(4);

		setButton = new JButton("Scale hits");
		setButton.addActionListener(this);
		setButton.setToolTipText("Resets the hits and applies the step size, does not affect the histograms on the other pages.");
		
		SpinnerModel stepModel = new SpinnerNumberModel(1,0.1,20,0.5);
		stepSpinner = new JSpinner(stepModel);
		stepSpinner.setEditor(new JSpinner.NumberEditor(stepSpinner, "#.#"));
		stepSpinner.setValue(new Double(1.0));
		stepSpinner.setToolTipText("Sets the increment in the hits plots, choose a larger value for lower count rates.");
		
		resetTotalHitsButton = new JButton("Reset all hits");
		resetTotalHitsButton.addActionListener(this);
		resetTotalHitsButton.setToolTipText("Resets all hits and applies the step size, does not affect the histograms on the other pages.");

		freezeButton = new JToggleButton("freeze",globals.freezeTrack);
		freezeButton.addActionListener(this);
		freezeButton.setToolTipText("Freezes display of one track, does not affect the histograms.");
		
		scaleButton = new JButton("Scale energy");
		scaleButton.addActionListener(this);
		scaleButton.setToolTipText("Scale the energy to adjust marker size, does not affect the histograms.");

		SpinnerModel energyModel = new SpinnerNumberModel(50,1,10000,5);
		energySpinner = new JSpinner(energyModel);
		energySpinner.setEditor(new JSpinner.NumberEditor(energySpinner, "#"));
		energySpinner.setValue(new Integer(50));
		energySpinner.setToolTipText("Sets the scaling of the energy [MeV] for drawing tracks, does not affect histograms.");
		
		energyResetButton = new JButton("Reset E & z");
		energyResetButton.addActionListener(this);
		energyResetButton.setToolTipText("Resets the energy and z histograms, does not affect the hits.");

		tmLabel = new JLabel("Taclet created by Ulrike");
		tmLabel.setForeground(new Color(30,30,150));
		Font usedFont = tmLabel.getFont();
		Font smallFont = usedFont.deriveFont(8.5f);
		tmLabel.setFont(smallFont);
		tmLabel.setVerticalTextPosition(JLabel.BOTTOM);
		tmLabel.setHorizontalTextPosition(JLabel.RIGHT);

		SpinnerModel energyLowLimitModel = new SpinnerNumberModel(0,-1,100,0.1);
		energyLowLimit = new JSpinner(energyLowLimitModel);
		energyLowLimit.setEditor(new JSpinner.NumberEditor(energyLowLimit, "#.#"));
		energyLowLimit.setValue(new Double(0.0));
		energyLowLimit.setToolTipText("Sets the lower limit in energy histograms.");

		SpinnerModel energyHighLimitModel = new SpinnerNumberModel(0,0,100,0.1);
		energyHighLimit = new JSpinner(energyHighLimitModel);
		energyHighLimit.setEditor(new JSpinner.NumberEditor(energyHighLimit, "#.#"));
		energyHighLimit.setValue(new Double(5.0));
		energyHighLimit.setToolTipText("Sets the upper limit in energy histograms.");

		setEnergyLimit = new JCheckBox("set energy limits");
		setEnergyLimit.setSelected(false);
		setEnergyLimit.addActionListener(this);
		setEnergyLimit.setToolTipText("Use the chosen limits for the energy histograms. If unchecked, the limits are calculated from the minimum and maximum values. Uncheck and recheck to update limits.");

		SpinnerModel totalEnergyLowLimitModel = new SpinnerNumberModel(0,-1,100,0.1);
		totalEnergyLowLimit = new JSpinner(totalEnergyLowLimitModel);
		totalEnergyLowLimit.setEditor(new JSpinner.NumberEditor(totalEnergyLowLimit, "#.#"));
		totalEnergyLowLimit.setValue(new Double(0.0));
		totalEnergyLowLimit.setToolTipText("Sets the lower limit in the total energy histogram.");

		SpinnerModel totalEnergyHighLimitModel = new SpinnerNumberModel(0,0,100,0.1);
		totalEnergyHighLimit = new JSpinner(totalEnergyHighLimitModel);
		totalEnergyHighLimit.setEditor(new JSpinner.NumberEditor(totalEnergyHighLimit, "#.#"));
		totalEnergyHighLimit.setValue(new Double(5.0));
		totalEnergyHighLimit.setToolTipText("Sets the upper limit in the total energy histogram.");

		setTotalEnergyLimit = new JCheckBox("total energy limits");
		setTotalEnergyLimit.setSelected(false);
		setTotalEnergyLimit.addActionListener(this);
		setTotalEnergyLimit.setToolTipText("Use the chosen limits for the total energy histogram. Uncheck and recheck to update limits.");

		SpinnerModel[] sliceModel = new SpinnerNumberModel[8];
		for (int i = 0; i<8; i++){
			sliceModel[i] = new SpinnerNumberModel(-120,-120,120,1.0);
		}
		sliceLimit = new JSpinner[8];
		for (int i = 0; i<8; i++){
			sliceLimit[i] = new JSpinner(sliceModel[i]);
			sliceLimit[i].setEditor(new JSpinner.NumberEditor(sliceLimit[i],"#"));
			sliceLimit[i].setValue(globals.sliceLimit[i]);
			sliceLimit[i].addChangeListener(this);
		}
		
		SpinnerModel zLowLimitModel = new SpinnerNumberModel(-120,-121,120,1.0);
		zLowLimit = new JSpinner(zLowLimitModel);
		zLowLimit.setEditor(new JSpinner.NumberEditor(zLowLimit, "#"));
		zLowLimit.setValue(new Double(-120.0));
		zLowLimit.setToolTipText("Sets the lower limit in z histograms.");

		SpinnerModel zHighLimitModel = new SpinnerNumberModel(120,-120,122.0,1.0);
		zHighLimit = new JSpinner(zHighLimitModel);
		zHighLimit.setEditor(new JSpinner.NumberEditor(zHighLimit, "#"));
		zHighLimit.setValue(new Double(120.0));
		zHighLimit.setToolTipText("Sets the upper limit in z histograms.");

		setZLimit = new JCheckBox("set z origin limits");
		setZLimit.setSelected(false);
		setZLimit.addActionListener(this);
		setZLimit.setToolTipText("Use the chosen limits for the z origin histograms. If unchecked, the limits are calculated from the minimum and maximum values. Uncheck and recheck to update limits.");

		SpinnerModel angleLowLimitModel = new SpinnerNumberModel(0,-180.0,180.0,1.0);
		angleLowLimit = new JSpinner(angleLowLimitModel);
		angleLowLimit.setEditor(new JSpinner.NumberEditor(angleLowLimit, "#"));
		angleLowLimit.setValue(new Double(0.0));
		angleLowLimit.setToolTipText("Sets the lower limit in angle histograms.");

		SpinnerModel angleHighLimitModel = new SpinnerNumberModel(0,-180.0,180.0,1.0);
		angleHighLimit = new JSpinner(angleHighLimitModel);
		angleHighLimit.setEditor(new JSpinner.NumberEditor(angleHighLimit, "#"));
		angleHighLimit.setValue(new Double(180.0));
		angleHighLimit.setToolTipText("Sets the upper limit in angle histograms.");

		setAngleLimit = new JCheckBox("set angle limits");
		setAngleLimit.setSelected(false);
		setAngleLimit.addActionListener(this);
		setAngleLimit.setToolTipText("Use the chosen limits for the angle histograms. If unchecked, the limits are calculated from the minimum and maximum values. Uncheck and recheck to update limits.");

		SpinnerModel radiusLowLimitModel = new SpinnerNumberModel(0,0,50.0,1.0);
		radiusLowLimit = new JSpinner(radiusLowLimitModel);
		radiusLowLimit.setEditor(new JSpinner.NumberEditor(radiusLowLimit, "#"));
		radiusLowLimit.setValue(new Double(12.0));
		radiusLowLimit.setToolTipText("Sets the lower limit in radius histograms.");

		SpinnerModel radiusHighLimitModel = new SpinnerNumberModel(0,0,70.0,1.0);
		radiusHighLimit = new JSpinner(radiusHighLimitModel);
		radiusHighLimit.setEditor(new JSpinner.NumberEditor(radiusHighLimit, "#"));
		radiusHighLimit.setValue(new Double(50.0));
		radiusHighLimit.setToolTipText("Sets the upper limit in radius histograms.");

		setRadiusLimit = new JCheckBox("set radius limits");
		setRadiusLimit.setSelected(false);
		setRadiusLimit.addActionListener(this);
		setRadiusLimit.setToolTipText("Use the chosen limits for the radius histograms. If unchecked, the limits are calculated from the minimum and maximum values. Uncheck and recheck to update limits.");

		SpinnerModel lengthLowLimitModel = new SpinnerNumberModel(0,0,200.0,1.0);
		lengthLowLimit = new JSpinner(lengthLowLimitModel);
		lengthLowLimit.setEditor(new JSpinner.NumberEditor(lengthLowLimit, "#"));
		lengthLowLimit.setValue(new Double(0.0));
		lengthLowLimit.setToolTipText("Sets the lower limit in length histograms.");

		SpinnerModel lengthHighLimitModel = new SpinnerNumberModel(0,0,200.0,1.0);
		lengthHighLimit = new JSpinner(lengthHighLimitModel);
		lengthHighLimit.setEditor(new JSpinner.NumberEditor(lengthHighLimit, "#"));
		lengthHighLimit.setValue(new Double(120.0));
		lengthHighLimit.setToolTipText("Sets the upper limit in length histograms.");

		setLengthLimit = new JCheckBox("set length limits");
		setLengthLimit.setSelected(false);
		setLengthLimit.addActionListener(this);
		setLengthLimit.setToolTipText("Use the chosen limits for the length histograms. If unchecked, the limits are calculated from the minimum and maximum values. Uncheck and recheck to update limits.");

		SpinnerModel zEndLowLimitModel = new SpinnerNumberModel(-121,-121,120.0,1.0);
		zEndLowLimit = new JSpinner(zEndLowLimitModel);
		zEndLowLimit.setEditor(new JSpinner.NumberEditor(zEndLowLimit, "#"));
		zEndLowLimit.setValue(new Double(-120.0));
		zEndLowLimit.setToolTipText("Sets the lower limit in z endpoint histograms.");

		SpinnerModel zEndHighLimitModel = new SpinnerNumberModel(120,-121,122.0,1.0);
		zEndHighLimit = new JSpinner(zEndHighLimitModel);
		zEndHighLimit.setEditor(new JSpinner.NumberEditor(zEndHighLimit, "#"));
		zEndHighLimit.setValue(new Double(120.0));
		zEndHighLimit.setToolTipText("Sets the upper limit in z endpoint histograms.");

		setZEndLimit = new JCheckBox("set z end limits");
		setZEndLimit.setSelected(false);
		setZEndLimit.addActionListener(this);
		setZEndLimit.setToolTipText("Use the chosen limits for the z endpoint histograms. If unchecked, the limits are calculated from the minimum and maximum values. Uncheck and recheck to update limits.");

		correctForShielding = new JCheckBoxMenuItem("Correct for shielding");
		correctForShielding.setSelected(globals.correctForShielding);
		correctForShielding.addActionListener(this);
		correctForShielding.setToolTipText("Scale the hits in the histograms to correct for shielding by the flutes. 1.2 for sectors 1 & 2, 1.5 for sectors 4 & 7");
		settingsMenu.add(correctForShielding);

		selectCoincidences = new JCheckBoxMenuItem("Coincidences only");
		selectCoincidences.setSelected(globals.selectCoincidences);
		selectCoincidences.addActionListener(this);
		selectCoincidences.setToolTipText("Show only coincidences in the histograms and tracks");
		settingsMenu.add(selectCoincidences);

		oneTrackPerSector = new JCheckBoxMenuItem("Single track per sector");
		oneTrackPerSector.setSelected(globals.oneTrackPerSector);
		oneTrackPerSector.addActionListener(this);
		oneTrackPerSector.setToolTipText("If the simulation gives several tracks in one sector for one event, the traacks will be added, using the largest energy if one pad has several hits");
		settingsMenu.add(oneTrackPerSector);

		drawTrack = new JCheckBoxMenuItem("Draw tracks");
		drawTrack.setSelected(globals.drawTrack);
		drawTrack.addActionListener(this);
		drawTrack.setToolTipText("draw colourful balls to represent the track");
		viewMenu.add(drawTrack);
		
		drawLine = new JCheckBoxMenuItem("Draw lines");
		drawLine.setSelected(globals.drawLine);
		drawLine.addActionListener(this);
		drawLine.setToolTipText("draw a line representing the calculated path");
		viewMenu.add(drawLine);

		logScale = new JCheckBoxMenuItem("Log scale");
		logScale.setSelected(globals.logScale);
		logScale.addActionListener(this);
		logScale.setToolTipText("use a logarithmic scale for drawing tracks");
		viewMenu.add(logScale);

//		settingsMenu.addSeparator();
		
		singlePopup = new JCheckBoxMenuItem("Reuse event window");
		singlePopup.setSelected(globals.singlePopup);
		singlePopup.addActionListener(this);
		singlePopup.setToolTipText("Event information is displayed in the same popup rather than opening a new window for each event");
		viewMenu.add(singlePopup);

		zCut = new JCheckBox("origin cut");
		zCut.setSelected(false);
		zCut.addActionListener(this);
		zCut.setToolTipText("only fill histograms if the event origin lies within the specified limits");

		angleCut = new JCheckBox("angle cut");
		angleCut.setSelected(false);
		angleCut.addActionListener(this);
		angleCut.setToolTipText("only fill histograms if the angle lies within the specified limits");

		energyCut = new JCheckBox("energy cut");
		energyCut.setSelected(false);
		energyCut.addActionListener(this);
		energyCut.setToolTipText("only fill histograms if the energy lies within the specified limits");

		totalEnergyCut = new JCheckBox("total energy cut");
		totalEnergyCut.setSelected(false);
		totalEnergyCut.addActionListener(this);
		totalEnergyCut.setToolTipText("only fill histograms if the total energy lies within the specified limits, use reset button to clear histograms");
		deltaECut = new JCheckBox("dE cut");
		deltaECut.setSelected(false);
		deltaECut.addActionListener(this);
		deltaECut.setToolTipText("only fill histograms if the track dE lies within the specified limits");


//		setJMenuBar(menuBar);
		layoutButtonPanel();
//		getContentPane().setSize(800,700);
		getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.X_AXIS));
		getContentPane().add(outputPanel);
		getContentPane().add(buttonPanel);
//		getContentPane().add(menuBar);
		
	}

	public void start(){
		if (DEBUG) System.out.println("starting...");
		mainThread = new Thread(this);
		mainThread.start();
	}

	public void stop(){
		if (DEBUG) System.out.println("stopping...");
//			globals.stop = true;
//			while (daqThread.isAlive()){if (DEBUG) System.out.println("daq still threading");}
			globals.stop = true;
		allStop = true;
		while (connectThread.isAlive()){if (DEBUG) System.out.println("connect still threading");}
		while (daqThread.isAlive()){if (DEBUG) System.out.println("daq still threading");}
		if (globals.gotContact) stopConnection();
		mainThread = null;
//			stopConnection();
	}

	public void destroy(){
		if (DEBUG) System.out.println("preparing to unload...");
//		stopConnection();
	}


	public void actionPerformed(ActionEvent event){
		Object source = event.getSource();
		if(source == setButton){
			if (DEBUG) System.out.println("threads when clicked: " + Thread.currentThread().activeCount());
			globals.stop = true;
			if (DEBUG) System.out.println("threads just after stop: " + Thread.currentThread().activeCount());
			while (daqThread.isAlive()){if (DEBUG) System.out.println("daq still threading");}
			globals.stepSize=(Double)(stepSpinner.getValue());
			outputPanel.sectorIni();
			globals.stop = false;
		}
		if(source == resetTotalHitsButton){
			if (DEBUG) System.out.println("threads when clicked: " + Thread.currentThread().activeCount());
			globals.stop = true;
			if (DEBUG) System.out.println("threads just after stop: " + Thread.currentThread().activeCount());
			while (daqThread.isAlive()){if (DEBUG) System.out.println("daq still threading");}
			globals.stepSize=(Double)(stepSpinner.getValue());
			outputPanel.sectorIni();
			outputPanel.totalHits = new int[8];
			outputPanel.sumTracks = outputPanel.sumEvents = 0;
			globals.stop = false;
		}
		if(source == scaleButton){
			globals.energyFactor=(Integer)(energySpinner.getValue());
			outputPanel.repaint();
		}
		if (source == setEnergyLimit){
			if (!setEnergyLimit.isSelected())
			{
				globals.energyLow = Double.NaN;
				globals.energyHigh = Double.NaN;
			}
			else if (setEnergyLimit.isSelected())
			{
				globals.energyLow = (Double)(energyLowLimit.getValue());
				globals.energyHigh = (Double)(energyHighLimit.getValue());
			}
			if (globals.energyLow>=globals.energyHigh) globals.energyHigh = globals.energyLow + 1;
		}
		if (source == setTotalEnergyLimit){
			if (!setTotalEnergyLimit.isSelected())
			{
				globals.totalEnergyLow = Double.NaN;
				globals.totalEnergyHigh = Double.NaN;
			}
			else if (setTotalEnergyLimit.isSelected())
			{
				globals.totalEnergyLow = (Double)(totalEnergyLowLimit.getValue());
				globals.totalEnergyHigh = (Double)(totalEnergyHighLimit.getValue());
			}
			if (globals.totalEnergyLow>=globals.totalEnergyHigh) globals.totalEnergyHigh = globals.totalEnergyLow + 1;
		}

		if (source == setZLimit){
			if (!setZLimit.isSelected())
			{
				globals.zLow = Double.NaN;
				globals.zHigh = Double.NaN;
			}
			else if (setZLimit.isSelected())
			{
				globals.zLow = (Double)(zLowLimit.getValue());
				globals.zHigh = (Double)(zHighLimit.getValue());
			}
			if (globals.zLow>=globals.zHigh) globals.zHigh = globals.zLow + 1;
		}
		if (source == setAngleLimit){
			if (!setAngleLimit.isSelected())
			{
				globals.angleLow = Double.NaN;
				globals.angleHigh = Double.NaN;
			}
			else if (setAngleLimit.isSelected())
			{
				globals.angleLow = (Double)(angleLowLimit.getValue());
				globals.angleHigh = (Double)(angleHighLimit.getValue());
			}
			if (globals.angleLow>=globals.angleHigh) globals.angleHigh = globals.angleLow + 1;
		}
		if (source == setRadiusLimit){
			if (!setRadiusLimit.isSelected())
			{
				globals.radiusLow = Double.NaN;
				globals.radiusHigh = Double.NaN;
			}
			else if (setRadiusLimit.isSelected())
			{
				globals.radiusLow = (Double)(radiusLowLimit.getValue());
				globals.radiusHigh = (Double)(radiusHighLimit.getValue());
			}
			if (globals.radiusLow>=globals.radiusHigh) globals.radiusHigh = globals.radiusLow + 1;

		}
		if (source == setLengthLimit){
			if (!setLengthLimit.isSelected())
			{
				globals.lengthLow = Double.NaN;
				globals.lengthHigh = Double.NaN;
			}
			else if (setLengthLimit.isSelected())
			{
				globals.lengthLow = (Double)(lengthLowLimit.getValue());
				globals.lengthHigh = (Double)(lengthHighLimit.getValue());
			}
			if (globals.lengthLow>=globals.lengthHigh) globals.lengthHigh = globals.lengthLow + 1;
		}
		if (source == setZEndLimit){
			if (!setZEndLimit.isSelected())
			{
				globals.zEndLow = Double.NaN;
				globals.zEndHigh = Double.NaN;
			}
			else if (setZEndLimit.isSelected())
			{
				globals.zEndLow = (Double)(zEndLowLimit.getValue());
				globals.zEndHigh = (Double)(zEndHighLimit.getValue());
			}
			if (globals.zEndLow>=globals.zEndHigh) globals.zEndHigh = globals.zEndLow + 1;
		}
		if (source == correctForShielding){
				globals.correctForShielding = correctForShielding.isSelected();
		}

		if (source == oneTrackPerSector){
				globals.oneTrackPerSector = oneTrackPerSector.isSelected();
		}

		if (source == singlePopup){
				globals.singlePopup = singlePopup.isSelected();
		}

		if (source == selectCoincidences){
			if (DEBUG) System.out.println("threads when clicked: " + Thread.currentThread().activeCount());
				globals.selectCoincidences = selectCoincidences.isSelected();

		}

		if (source == drawTrack){
				globals.drawTrack = drawTrack.isSelected();
		}

		if (source == logScale){
				globals.logScale = logScale.isSelected();
		}

		if (source == drawLine){
				globals.drawLine = drawLine.isSelected();
		}

		if (source == zCut){
			if (DEBUG) System.out.println("threads when clicked: " + Thread.currentThread().activeCount());
			globals.stop = true;
			if (DEBUG) System.out.println("threads just after stop: " + Thread.currentThread().activeCount());
			while (daqThread.isAlive()){if (DEBUG) System.out.println("daq still threading");}
			outputPanel.initHistos();
			if (!zCut.isSelected())
			{
				outputPanel.zLowCut = outputPanel.zLow;
				outputPanel.zHighCut = outputPanel.zHigh;
				
			}
			else if (zCut.isSelected())
			{
				outputPanel.zLowCut = (Double)(zLowLimit.getValue());
				outputPanel.zHighCut = (Double)(zHighLimit.getValue());
			}
			globals.stop = false;

		}

// 		if (source == deltaECut){
// 			if (DEBUG) System.out.println("threads when clicked: " + Thread.currentThread().activeCount());
// 			globals.stop = true;
// 			if (DEBUG) System.out.println("threads just after stop: " + Thread.currentThread().activeCount());
// 			while (daqThread.isAlive()){if (DEBUG) System.out.println("daq still threading");}
// 			outputPanel.initHistos();
// 			if (!deltaECut.isSelected())
// 			{
// 				outputPanel.deltaELowCut = outputPanel.energyLow;
// 				outputPanel.deltaEHighCut = outputPanel.energyHigh;
				
// 			}
// 			else if (deltaECut.isSelected())
// 			{
// 				outputPanel.deltaELowCut = (Double)(deltaELowLimit.getValue());
// 				outputPanel.deltaEHighCut = (Double)(deltaEHighLimit.getValue());
// 			}
// 			globals.stop = false;

// 		}

		if (source == angleCut){
			if (DEBUG) System.out.println("threads when clicked: " + Thread.currentThread().activeCount());
			globals.stop = true;
			if (DEBUG) System.out.println("threads just after stop: " + Thread.currentThread().activeCount());
			while (daqThread.isAlive()){if (DEBUG) System.out.println("daq still threading");}
			outputPanel.initHistos();
			if (!angleCut.isSelected())
			{
				outputPanel.angleLowCut = outputPanel.angleLow;
				outputPanel.angleHighCut = outputPanel.angleHigh;
				
			}
			else if (angleCut.isSelected())
			{
				outputPanel.angleLowCut = (Double)(angleLowLimit.getValue());
				outputPanel.angleHighCut = (Double)(angleHighLimit.getValue());
			}
			globals.stop = false;

		}
		if (source == energyCut){
			if (DEBUG) System.out.println("threads when clicked: " + Thread.currentThread().activeCount());
			globals.stop = true;
			if (DEBUG) System.out.println("threads just after stop: " + Thread.currentThread().activeCount());
			while (daqThread.isAlive()){if (DEBUG) System.out.println("daq still threading");}
			outputPanel.initHistos();
			if (!energyCut.isSelected())
			{
				outputPanel.energyLowCut = outputPanel.energyLow;
				outputPanel.energyHighCut = outputPanel.energyHigh;
				
			}
			else if (energyCut.isSelected())
			{
				outputPanel.energyLowCut = (Double)(energyLowLimit.getValue());
				outputPanel.energyHighCut = (Double)(energyHighLimit.getValue());
			}
			globals.stop = false;
		}

		if (source == totalEnergyCut){
			if (DEBUG) System.out.println("threads when clicked: " + Thread.currentThread().activeCount());
			if (!totalEnergyCut.isSelected())
			{
				outputPanel.totalEnergyLowCut = outputPanel.totalEnergyLow;
				outputPanel.totalEnergyHighCut = outputPanel.totalEnergyHigh;
				outputPanel.totalEnergyCut = false;

			}
			else if (totalEnergyCut.isSelected())
			{
				outputPanel.totalEnergyLowCut = (Double)(totalEnergyLowLimit.getValue());
				outputPanel.totalEnergyHighCut = (Double)(totalEnergyHighLimit.getValue());
				outputPanel.totalEnergyCut = true;
			}
		}

		if(source == energyResetButton){
//			System.out.println("energyResetButton clicked");
			if (DEBUG) System.out.println("threads when clicked: " + Thread.currentThread().activeCount());
			globals.stop = true;
			if (DEBUG) System.out.println("threads just after stop: " + Thread.currentThread().activeCount());
			while (daqThread.isAlive()){if (DEBUG) System.out.println("daq still threading");}
			outputPanel.initHistos();
			globals.stop = false;
//			System.out.println("continue after energy reset");
		}
		if (source == freezeButton){
			globals.freezeTrack = freezeButton.isSelected();
		}

	}

	
	public void valueChanged(ListSelectionEvent event) {
		Object source = event.getSource();
		if (source == pageChoice){
			globals.page = pageChoice.getSelectedIndex();
			if (!DEBUG) {
				buttonPanel.removeAll();
				layoutButtonPanel();
//				buttonPanel.repaint();
				buttonPanel.revalidate();
//				getContentPane().add(buttonPanel);
				outputPanel.repaint();
//				repaint();
				
			}
 		}
		if (source == displayChoice){
			globals.display = displayChoice.getSelectedIndex();
			outputPanel.repaint();
		}
		
	}

	public void stateChanged(ChangeEvent event) {
		Object source = event.getSource();
		for (int i = 0; i<8; i++){
			if (source == sliceLimit[i]) {
				globals.sliceLimit[i] =  (Double)(sliceLimit[i].getValue());
				outputPanel.changeSliceStep(i);
				return;
			}
        }
    }

	
	public void stopConnection() { //stop
		if (DEBUG) System.out.println("stopping connection");
			try {
				globals.incoming.close();
				tacticSocket.close();
			} catch (IOException e) {
				System.out.println(e);
			}
	}

	public void run()  { //read data
		boolean loopOn = true;
		while(loopOn){
			connectThread = new Thread(new provideSocket());
			daqThread = new Thread(outputPanel);
			if (DEBUG) System.out.println("threads at begin of run: " + Thread.currentThread().activeCount());
			if (!globals.gotContact){
				connectThread.start();
				try {
					connectThread.join();
				} catch (InterruptedException e) {
					System.err.println("still connecting??");
				}
			}
			if (DEBUG) System.out.println("threads after connect thread: " + Thread.currentThread().activeCount());
			daqThread.start();
			if (DEBUG) System.out.println("threads when daq started: " + Thread.currentThread().activeCount());
			try {
				daqThread.join();
			} catch (InterruptedException e) {
				System.err.println("still reading??");
			}
//			stopConnection();
			if (allStop) loopOn = false;
		}
	}
}

	






