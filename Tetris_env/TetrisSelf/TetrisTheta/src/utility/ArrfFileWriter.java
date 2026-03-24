package utility;

import tetris.TetrisDTFeature;

public class ArrfFileWriter {


	public String fileHead(TetrisDTFeature tf)
	{
		String s="@relation Tetris\n";
		String[] name = tf.getFeatureNames();
		for(int i=0;i<name.length;i++)
		{
			s+="@attribute \""+name[i]+"\" numeric\n";
		}
		//s+="@attribute \"reward\" numeric\n";
		s+="@attribute \"optimal\" {1,0}\n";
		s+="@data\n";
		
		return s;
	}
}
