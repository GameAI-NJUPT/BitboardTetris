package utility;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class FilesReadingAndWriting {

	public void writeParameter(double[] theta, String s)
	{
		FileOutputStream fws=null;
		DataOutputStream out=null;
		try
		{
			fws=new FileOutputStream(s);
			out=new DataOutputStream(fws);
			out.writeInt(theta.length);
			for(int i=0;i<theta.length;i++)
			{
				out.writeDouble(theta[i]);
			}
		}
		catch(FileNotFoundException ex)
		{
			System.out.println("file not found :"+s);
		}
		catch(IOException ex)
		{
			System.out.println(ex.getMessage());
		}
		finally
		{
			try
			{
				if(fws!=null)fws.close();
			}
			catch(IOException ex)
			{
				System.out.println(ex);
			}
		}
	}
	public double[] readParameter(String s)
	{
		FileInputStream frs=null;
		DataInputStream in=null;
		
		try{
			
				frs=new FileInputStream(s);
				in=new DataInputStream(frs);
				int length = in.readInt();
				double [] theta = new double[length];
				for(int i=0;i<theta.length;i++)
			    {
					theta[i]=0;
			    }
				for(int i=0;i<theta.length;i++)
			    {
					theta[i]=in.readDouble();
			    }
				return theta;
			}
			catch(FileNotFoundException ex)
			{
				System.out.println("file not found :"+s);
			}
			catch(IOException ex)
			{
				System.out.println(ex.getMessage());
			}
			finally
			{
				try
				{
					if(frs!=null)frs.close();
				}
				catch(IOException ex)
				{
					System.out.println(ex);
				}
			}
		return null;
	}
	public void writeResultToUTF(double[] record, String s)
	{
		FileOutputStream fws=null;
		DataOutputStream out=null;
		try
		{
			fws=new FileOutputStream(s);
			out=new DataOutputStream(fws);
			
			
			 String resultString = "";
		        int lineNum = 100;
		        for(int i=0;i<record.length/lineNum+1;i++)
		        {
		        	resultString = "";
		        	for(int j=0;j<lineNum;j++)
		        	{
		        		if(i*lineNum+j>=record.length)
		        		{
		        			break;
		        		}
		        		resultString += record[i*lineNum+j]+" "; 
		        	}
		        	resultString+="\n";
		        	out.write(resultString.getBytes("utf8"));
		        	out.flush();
		        }
			System.out.println("save to file: "+s);
			
		}
		catch(FileNotFoundException ex)
		{
			System.out.println("file not found :"+s);
		}
		catch(IOException ex)
		{
			System.out.println(ex.getMessage());
		}
		finally
		{
			try
			{
				if(fws!=null)fws.close();
			}
			catch(IOException ex)
			{
				System.out.println(ex);
			}
		}
	
	}
	public void writeStringToUTF(String content,String fileName)
	{
		FileOutputStream fws=null;
		DataOutputStream out=null;
		try
		{
			fws=new FileOutputStream(fileName);
			out=new DataOutputStream(fws);
			
			
		    out.write(content.getBytes("utf8"));
		    out.flush();
		}
		catch(FileNotFoundException ex)
		{
			System.out.println("file not found :"+fileName);
		}
		catch(IOException ex)
		{
			System.out.println(ex.getMessage());
		}
		finally
		{
			try
			{
				if(fws!=null)fws.close();
			}
			catch(IOException ex)
			{
				System.out.println(ex);
			}
		}
	
	}
}
