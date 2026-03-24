package rl;
import javax.swing.JPanel;

import java.awt.Color;
import java.awt.Graphics;

/**
 * @author chenxg
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class MyJPanel extends JPanel 
{
 	/**
	 * Comment for <code>serialVersionUID</code>
	 */
	private static final long serialVersionUID = 1L;
		String s="";
      	Color current;
      	Color imagebaizhi;
      	Color colorRed;
      	Color colorBlue;
      	Color colorYellow;
      	Color colorGreen;
      	Color corlorWhite;
        int positionx=0;
        int positiony=0;
        public MyJPanel()
        {
        	
        }
        public void paintComponent(Graphics g)
        {
        	super.paintComponent(g);
        	g.setColor(current);
            g.drawRect(0,0,getWidth(),getHeight());
            g.fillRect(0,0,getWidth(),getHeight());
            char []aa;
            aa=s.toCharArray();
            String m="";
            for(int i=0;i<s.length();i++)
              {
                if(aa[i]!='\n'&&m.length()<30)
                {
                  m=m+aa[i];
                }
                else
                {
                  g.drawString(m,positionx,positiony);
                  positiony=positiony+15;
                  positionx=5;
                  m="";
                  System.out.println(" x="+positionx+" y="+positiony);
                }
              }
            g.drawString(m,positionx,positiony);
            positionx=0;
            positiony=0;
        }
        
        public void setbackground(int i,String s,int x,int y)
        {        
        	if(i==0)
        	{
        		corlorWhite=new Color(255,255,255);
        		current=corlorWhite;
        	}
        	if(i==1)
        	{
        		colorRed=new Color(255,0,0);
        		current=colorRed;
        	}
        	if(i==2)
        	{
        		colorBlue=new Color(0,0,255);
        		current=colorBlue;
        	}
        	if(i==3)
        	{
        		colorYellow=new Color(255,255,0);
        		current=colorYellow;
        	}
        	if(i==4)
        	{
        		colorGreen=new Color(0,255,0);
        		current=colorGreen;
        	}
        	if(i==5)
            {
        		imagebaizhi=new Color(255,255,255);
        		current=imagebaizhi;
            }
            this.s=s;
            this.positionx=x;
            this.positiony=y;
            repaint();
        }
  
}

