// part of Taclet
// author: Ulrike Hager

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class EnergyHistogram extends JPanel {
	private int[] count= new int[200];
	private double[] lowBinLimit = new double[200];
	private double[] energyArray ;
	private double eMin, eMax, binSize;
	private int numberOfBins;	

	
  /** Set the count and display histogram */
  public void showHistogram(double[] energyArray) {
    this.energyArray = energyArray;
	sortBins();
    repaint();
  }

	private void sortBins(){
		eMin=energyArray[0];
		eMax=eMin;
		if (energyArray.length>1){
		for (int i =1 ; i<energyArray.length; i++){
			double energy=energyArray[i];
			if (energy < eMin) eMin = energy;
			if (energy > eMax) eMax = energy;
		}
		}
		binSize = ((eMax-eMin +5)/numberOfBins);
		lowBinLimit[0]=eMin-2.5;
		for (int i =1; i<200; i++){
			lowBinLimit[i]=lowBinLimit[i-1]+binSize;
		}
		for (int i =1 ; i<energyArray.length; i++){
			double energy=energyArray[i];
			int j = 0;
			while (energy>lowBinLimit[j]){
				j++;
			}
			count[j-1]++; 
		}		
	}
	
	
  /** Paint the histogram */
  protected void paintComponent(Graphics g) {
    if (count == null) return; // No display if count is null

    super.paintComponent(g);

    // Find the panel size and bar width and interval dynamically
    int width = getWidth();
    int height = getHeight();
    int interval = (width - 40) / count.length;
    int individualWidth = (int)(((width - 40) / 24) * 0.60);

    // Find the maximum count. The maximum count has the highest bar
    int maxCount = 0;
    for (int i = 0; i < count.length; i++) {
      if (maxCount < count[i])
        maxCount = count[i];
    }

    // x is the start position for the first bar in the histogram
    int x = 30;

    // Draw a horizontal base line
    g.drawLine(10, height - 45, width - 10, height - 45);
    for (int i = 0; i < count.length; i++) {
      // Find the bar height
      int barHeight =
        (int)(((double)count[i] / (double)maxCount) * (height - 55));

      // Display a bar (i.e. rectangle)
      g.drawRect(x, height - 45 - barHeight, individualWidth,
        barHeight);

      // Display a letter under the base line
      g.drawString((char)(lowBinLimit[x]) + "", x, height - 30);

      // Move x for displaying the next character
      x += interval;
    }
  }

  /** Override getPreferredSize */
  public Dimension getPreferredSize() {
    return new Dimension(300, 300);
  }
}
