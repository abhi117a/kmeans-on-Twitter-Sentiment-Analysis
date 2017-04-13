//package kMeansPart1;

import java.io.File;
import java.util.Collections;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Scanner;

public class kmeans
{
	/***************************************************Global and Final Variable Declarations****************************************************/
    private static int NUMBER_Of_CLUSTERS = 0;    // Total clusters.
    private static final int MAX_INDICIES = 100;      // Total data points.
    
    private static ArrayList<InputData> dataInputt = new ArrayList<InputData>(); //Array List of Input
    private static ArrayList<CenterPoint> centerPoints = new ArrayList<CenterPoint>(); //ArrayList of Centroids

   /***************************************************Customized User defined Center Point Class****************************************************/
    private static class CenterPoint
    
    	{	
    	/*Initializing Variables*/	
        private double X_val = 0.0;
        private double Y_val = 0.0;
        private int id=0;
        
        			/*Constructor*/
        public CenterPoint(int Id_temp,double X_temp, double Y_temp)
        {
            this.id = Id_temp;
        	this.X_val = X_temp;
            this.Y_val = Y_temp;
            return;
        }
        
        		/*Setter*/
        public void X(double X_temp)
        {
            this.X_val = X_temp;
            return;
        }
        		/*Setter*/
        public void Y(double Y_temp)
        {
            this.Y_val = Y_temp;
            return;
        }
        /*Getter*/
        public double X()
        {return this.X_val;}
        	/*Getter*/
        public double Y()
        {return this.Y_val;}
    }
 /***************************************************Customized User defined Data Class****************************************************/
	private static class InputData
    {	/*******************Initialize Variable**************/
        private double X_val = 0;
        private double Y_val = 0;
        private int id;
        private int clust = 0;
        /*Constructor*/
        public InputData(int id, double x, double y)
        {
            this.ID(id);
        	this.X(x);
            this.Y(y);
            return;
        }
        /*Setter*/
        public void X(double x)
        {
            this.X_val = x;
            return;
        }
        /*Setter*/
        public void Y(double y)
        {
            this.Y_val = y;
            return;
        }
        /*Setter*/
        public void clusterInt(int clusterNumber)
        {
            this.clust = clusterNumber;
            return;
        }
        /*Setter*/
        public void ID(int id) {this.id= id;}
        /*Getter*/
        public int cluster()
        {return this.clust;}
        /*Getter*/
        public double X()
        {return this.X_val;}
        /*Getter*/
        public double Y()
        {return this.Y_val;}
    }
	
	/***************************************************Input Values and Helper Method for Euclidiean****************************************************/	
	
	private static void Euclidiean(ArrayList<Float> x, ArrayList<Float> y,int k, ArrayList<Integer> id) 
	{
		/****************************Declaring Variables***********************************/
		int sample = 0;
        int clusterVal = 0;
        boolean flagIfChange = true;
        InputData dat = null;
		final double MAX_VALUE = Math.pow(10, 10);    // some large number that's sure to be larger than our data range.
        double min = MAX_VALUE;                   		// The minimum value to beat. 
        double distance = 0.0;                        // The current minimum value.
        /***************Initialize Centroid*********************/
        ArrayList<Integer> randomVals = new ArrayList<Integer>();
        for (int i=0; i<100; i++) {
            randomVals.add(new Integer(i));
        }
        Collections.shuffle(randomVals);
        for (int i=0; i<k; i++) {
            int d= randomVals.get(i);
            centerPoints.add(new CenterPoint(id.get(d), x.get(d), y.get(d))); 
        }
        
        /*********Adding new values, computing centroids with each new one********/ 
        while(dataInputt.size() < MAX_INDICIES)
        {
            dat = new InputData(id.get(sample), x.get(sample), y.get(sample));
            dataInputt.add(dat);
            min = MAX_VALUE;
            for(int i = 0; i < k; i++)
            {
                distance = EucledianDistance(centerPoints.get(i),dat);
                if(distance < min){
                    min = distance;
                    clusterVal = i;
                }
            }
            dat.clusterInt(clusterVal);
            
            /*****************calculate new centroids.************/
            for(int i = 0; i < k; i++)
            {
                double finX = 0;
                double finY = 0;
                int totClust = 0;
                for(int j = 0; j < dataInputt.size(); j++)
                {
                    if(dataInputt.get(j).cluster() == i){
                        finX += dataInputt.get(j).X();
                        finY += dataInputt.get(j).Y();
                        totClust++;
                    }
                }
                if(totClust > 0){
                	centerPoints.get(i).X(finX / totClust);
                	centerPoints.get(i).Y(finY / totClust);
                }
            }
            sample++;
        }
        
        /***************Shifting centroids until points don't change****************/
        while(flagIfChange)
        {
            /*New Centroids*/
            for(int i = 0; i < k; i++)
            {
                double finX = 0;
                double finY = 0;
                int totCluster = 0;
                for(int p = 0; p < dataInputt.size(); p++)
                {
                    if(dataInputt.get(p).cluster() == i)
                    {
                        finX += dataInputt.get(p).X();
                        finY += dataInputt.get(p).Y();
                        totCluster++;
                    }
                }
                if(totCluster > 0){
                	centerPoints.get(i).X(finX / totCluster);
                	centerPoints.get(i).Y(finY / totCluster);
                }
            }
            
            /*********************Allocating all data to the new centroids********************/
            flagIfChange = false;
            
            for(int i = 0; i < dataInputt.size(); i++)
            {
            	InputData tempData = dataInputt.get(i);
                min = MAX_VALUE;
                for(int p = 0; p < k; p++)
                {
                    distance = EucledianDistance(centerPoints.get(p),tempData);
                    if(distance < min){
                        min = distance;
                        clusterVal = p;
                    }
                }
                tempData.clusterInt(clusterVal);
                if(tempData.cluster() != clusterVal){
                    tempData.clusterInt(clusterVal);
                    flagIfChange = true;
                }
            }
        }
        return;
		
	}
/***************************************************Euclidean Distance Calculation*****************************************/
    private static double EucledianDistance(CenterPoint cp, InputData dd)
    {return Math.sqrt(Math.pow((cp.Y() - dd.Y()), 2) + Math.pow((cp.X() - dd.X()), 2));}
/***************************************************SSE Calculation*******************************************************/	
    public static double SSE(ArrayList<InputData> dataSet2, ArrayList<CenterPoint> centroids2, int k) {
		double sse_err = 0;
        for(int i = 0; i < dataInputt.size(); i++)
        {
        	InputData tempData = dataInputt.get(i);
            for(int j = 0; j < k; j++)
            {
            	sse_err += (Math.pow(EucledianDistance(centerPoints.get(j),tempData), 2)/1000);
               
            }
        }
		return sse_err;
	} 	
/***************************************************MAIN Method****************************************************/	
    public static void main(String[] args) 
	{
		
    	String chck="";
    	ArrayList<Float> y_y= new ArrayList<>();
    	ArrayList<Float> x_x= new ArrayList<>();
    	ArrayList<Integer> token= new ArrayList<>();
		NUMBER_Of_CLUSTERS=Integer.parseInt(args[0]);
		
		
		try 
		{
		File file = new File(args[2]); //Your file
		FileOutputStream fOut = new FileOutputStream(file);
		PrintStream ps = new PrintStream(fOut);
		System.setOut(ps);
		}
		catch (Exception e) {e.printStackTrace();}
		
		try 
		{
            Scanner sc = new Scanner(new File(args[1]));         
            sc.nextLine();
            while (sc.hasNextLine()) 
            {
            	String[] data = sc.nextLine().split("\\s+");
            	token.add(Integer.parseInt(data[0]));
            	x_x.add(Float.parseFloat(data[1]));
            	y_y.add(Float.parseFloat(data[2]));          	
            }
            sc.close();
            } catch (Exception e) {e.printStackTrace();}

		Euclidiean(x_x,y_y,NUMBER_Of_CLUSTERS,token);
		
			double SSE_val= SSE(dataInputt,centerPoints,NUMBER_Of_CLUSTERS);;	
			
			for(int i = 0; i < NUMBER_Of_CLUSTERS; i++)
				{
    	  boolean flag=true;
    	  int clustID=i+1;
    	  System.out.print(clustID+"  ");
            for(int j = 0; j < MAX_INDICIES; j++)
            {
            	
                if(dataInputt.get(j).cluster()==i)
                {
                	if(flag)
                	{
                		chck=String.valueOf(dataInputt.get(j).id);
                		flag=false;
                	}
                	else{chck += "," + String.valueOf(dataInputt.get(j).id);}
                }
                
            }
            //System.out.println();
            System.out.print(chck);
  
            System.out.println();
        }
			
			System.out.println("Value of SSE: "+SSE_val);		
	
	return;
	
    }	
}


	



