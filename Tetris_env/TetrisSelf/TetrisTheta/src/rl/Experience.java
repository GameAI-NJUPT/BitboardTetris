package rl;

import tetris.TetrisEnvironment;

public class Experience {

	public int[] s1;
	public int a1;
	public double reward;
	public int[] s2; 
	public int a2;
	public double rho;
	public double c;
	public double hybridX;
	public double x; 
	public int done;
	
	public Experience()
	{
		s1 = TetrisEnvironment.defaultInitialState();
		s2 = TetrisEnvironment.defaultInitialState();
		a1=a2=-1;
		reward=rho=c=hybridX=x=0;
	}
	public Experience(int[] s1, int a1, double reward, int[] s2, int a2, double rho, double c, double hybridX,double x, int done) {
		this.s1 = TetrisEnvironment.copy(s1);
		this.a1 = a1;
		this.reward = reward;
		this.s2 = TetrisEnvironment.copy(s2);
		this.a2 = a2;
		this.rho = rho;
		this.c = c;
		this.hybridX = hybridX;
		this.x = x;
		this.done = done;
	}
	public void setDone(int done)
	{
		this.done = done;
	}
	public Experience copy()
	{
		Experience e = new Experience();
		e.s1 = TetrisEnvironment.copy(s1);
		e.a1 = this.a1;
		e.reward = this.reward;
		e.s2 = TetrisEnvironment.copy(s2);
		e.a2 = this.a2;
		e.rho = this.rho;
		e.c = this.c;
		e.hybridX = this.hybridX;
		e.x = this.x;
		e.done = this.done;
		return e;
	}
}
