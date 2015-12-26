// part of Taclet
// author: Ulrike Hager

import java.awt.*;
import java.awt.geom.Arc2D;

public class HistCircle {//class HistCircle

	private double xPos;
	private double yPos;
	private double[] height;
	private double[] stepSize;
	private Color[] tacColour;

	HistCircle(double xPos, double yPos, double aStepSize, Color[] tacColour){
		this.xPos = xPos;
		this.yPos = yPos;
		height = new double[8];
		for (int i= 0; i<8; i++){
			height[i] = 0;
		}
		stepSize = new double[8];
		for (int i= 0; i<8; i++){
			stepSize[i] =  aStepSize/2;
		}
		this.tacColour = tacColour;
	}
	public void setHeight(int i, double aHeight){ 
		height[i] = aHeight;
	}
	public void reset(int j){
		height = new double[8];
		for (int i= 0; i<8; i++){
			height[i] = 0;
		}
		height[j]+=stepSize[j];
	}

	public void incHeight(int i){ 
		height[i]+=stepSize[i];
	}
	public double getHeight(int i){
		return height[i];
	}
	public double getX(){
		return xPos;
	}
	public double getY(){
		return yPos;
	}
	public void setStepSize(int i, double step){
		stepSize[i] = step/2;
	}
    public void paint(Graphics g){
		Graphics2D g2D = (Graphics2D) g;
		for (int i = 0; i < 8; i++){
		Arc2D arc = new Arc2D.Double(Arc2D.PIE);
		double angle = (double)((i *45) +90);
		if (angle > 359) angle-=360;
		g2D.setColor(tacColour[i]);
		arc.setArcByCenter(xPos,yPos,height[i],angle,45,Arc2D.PIE);
		g2D.fill(arc);
		}
	}
}
