package utility;

public class DominanceFilter {

	public boolean duplicate(int[] featurei,int[]featurej)
	{
		for(int i=0;i<featurei.length;i++)
		{
			if(featurei[i]==featurej[i])
				return false;
		}
		return true;
	}
	public void removeDuplication(int[][]feature,boolean[] available)
	{
		for(int i=0;i<feature.length-1;i++)
		{
			if(available[i])
			for(int j=i+1;j<feature.length;j++)
			{
				if(available[j])
				{
					if(this.duplicate(feature[i], feature[j]))
					{
						available[j] = false;
					}
				}
			}
		}
		
	}
	//to test if feature vector i dominates feature vector j
	//return simple and cumulative dominance
	public int[] dominance(int[]featurei,int[]featurej)
	{
		boolean i_dominates_j = true;
		boolean	j_dominates_i = true;
		boolean	strictly_larger = false;
		boolean	strictly_smaller = false;
		boolean	i_cumu_dominates_j = true;
		boolean	j_cumu_dominates_i = true;
		boolean	cumu_strictly_larger = false;
		boolean	cumu_strictly_smaller = false;
		
		double diff_cumu = 0;
		for(int i=0;i<featurei.length;i++)
		{
			double diff = featurei[i]-featurej[i];
			diff_cumu += diff;
			if(diff>0)
			{
				j_dominates_i = false;
			    strictly_larger = true;
			}
			if(diff<0)
			{
				i_dominates_j = false;
			    strictly_smaller = true;
			}
			if(diff_cumu>0)
			{
				j_cumu_dominates_i = false;
			    cumu_strictly_larger = true;
			}
			if(diff_cumu<0) 
			{
				i_cumu_dominates_j = false;
			    cumu_strictly_smaller = true;
			}
			if((j_cumu_dominates_i==false)&&(i_cumu_dominates_j==false))
			{
				return new int[] {0,0};
			}
		}
		int out_simple = -10;
		int out_cumu = -10;
		if(i_cumu_dominates_j&&cumu_strictly_larger)
		{
			out_cumu = 1;
		}
		else if(j_cumu_dominates_i&&cumu_strictly_smaller)
		{
			out_cumu = -1;
		}
		else {
			out_cumu = 0;
		}
		if(i_dominates_j&&strictly_larger)
		{
			out_simple = 1;
		}
		else if(j_dominates_i&&strictly_smaller)
		{
			out_simple = -1;
		}
		else
		{
			out_simple = 0;
		}
		
		return new int[]{out_simple,out_cumu};
	}
	//filter non-dominated features
	//return non-simple and non-cumulative dominated features
	public boolean[][] dominate(int[][]feature)
	{
		boolean[] not_simply_dominated= new boolean[feature.length];
		boolean[] not_cumu_dominated= new boolean[feature.length];
		for(int i=0;i<feature.length;i++)
		{
			not_simply_dominated[i] = true;
			not_cumu_dominated[i] = true;
		}
		for(int i=0;i<feature.length-1;i++)
		{
			if(not_simply_dominated[i])
			for(int j=i+1;j<feature.length;j++)
			{
				if(not_simply_dominated[j])
				{
					int[] dom = this.dominance(feature[i], feature[j]);
					if(dom[0]==1)
					{
						not_simply_dominated[j] = false;
					}
					else if(dom[0]==-1)
					{
						not_simply_dominated[i] = false;
					}
					if(dom[1]==1)
					{
						not_cumu_dominated[j] = false;
					}
					else if(dom[1]==-1)
					{
						not_cumu_dominated[i] =false;
					}
				}
			}
		}
		this.removeDuplication(feature, not_cumu_dominated);
		this.removeDuplication(feature, not_simply_dominated);
		return new boolean[][] {not_simply_dominated,not_cumu_dominated};
	}
	public static void main(String[] args) {
		int [][] feature = 
			{
				{2, 2, 2, 2, 2},  // 0 dom
                {0, 1, 2, 3, 4},  // 1 cum dom
                {0, 1, 2, 3, 4},  // 1 cum dom
                {4, 3, 2, 1, 0},  // 2 dom
                {3, 2, 1, 0, -1}, // 3 dom
                {5, 4, 3, 2, 1},  // 4
                {5, 4, 3, 2, 1}, 
                {3, 3, 3, 3, 3},  // 6 cum dom
                {3, 3, 3, 3, 0},  // 7 dom
                {3, 2, 4, 3, 3},  // 8 cum dom
                {0, 4, 3, 3, 3}   // 9 cum dom
			};
		DominanceFilter df = new DominanceFilter();
		boolean [][] dom = df.dominate(feature);
		for(int i=0;i<dom.length;i++)
		{	
			for(int j=0;j<dom[i].length;j++)
			{
				System.out.print(dom[i][j]+", ");
			}
			System.out.println();
		}
	}

}
