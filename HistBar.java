// part of Taclet
// author: Ulrike Hager

import java.awt.*;
import java.awt.geom.Rectangle2D;

public class HistBar {//class HistBar

	private double xPos = 0;
	private double yPos = 50;
	private double width = 20;
	private double height = 0;
	private String histLabel = "0";
	private String totalHits = "0";
	private double stepSize =0;

	HistBar(int xPos, int totalHits, double stepSize){
		this.xPos = (double)(xPos+20+(xPos * 25));
		this.totalHits = String.valueOf(totalHits);
		this.stepSize = stepSize;
	}
	public void setHistHeight(double height){ 
		this.height = height;
	}
	public void setLabel(int ILabel){ 
		this.histLabel = String.valueOf(ILabel);
	}
	public void setTotalHits(int ILabel){ 
		this.totalHits = String.valueOf(ILabel);
	}

	public void incHeight(){ 
		height = height+stepSize;
	}
	public String getLabel(){
		return histLabel;
	}
	public void setStepSize(double step){
		stepSize = step;
	}
    public void paint(Graphics g){
		Graphics2D g2D = (Graphics2D) g;
		g.setFont(new Font("sansserif", Font.PLAIN, 8));
		g2D.fill(new Rectangle2D.Double(xPos,yPos,width,height));
		g2D.setColor(Color.BLACK);
		g2D.drawString(totalHits,(int)xPos,15);
		g2D.drawString(histLabel,(int)xPos,30);
	}
}
