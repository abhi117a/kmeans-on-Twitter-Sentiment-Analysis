//package kmeansPart2;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.simple.parser.ParseException;

public class tweetKmeans {
	/*Variable Declaration*/
	static double SSE_ERR;
/***********************************User Defined Twitter Class**********************************/
	public static class Twitter {
		/*Variable Declaration*/
		private String inputUse;
		private String jsonCreateT;
		private long id;
		private int clustID;
		private String str;
		/*Get Methods*/
		public int getclustID() {return clustID;}
		public String getstr() {return str;}
		public String getFrom_user() {return inputUse;}
		public long getId() {return id;}
		public String getjsonCreateT() {return jsonCreateT;}
		/*Set Methods*/
		public void setclustID(int clustID) {this.clustID = clustID;}
		public void setstr(String str) {this.str = str;}
		public void setId(long id) {this.id = id;}
		public void setFrom_user(String from_user) {this.inputUse = from_user;}
		public void setjsonCreateT(String created_at) {this.jsonCreateT = created_at;}

	}	
	
	/******************************************User Defined Jaccard Dist Class************************************************/
	public static class JaccardDist {
		/***Variable Declaration***/
	    private int k;
	    private List<Twitter> tweetList;
	    private List<Twitter> centerList;
	    private Map<Long, Twitter> tweetMap;
	    /*Get Methods*/
	    public List<Twitter> getcenterList() {return centerList;}
	    public int getK() {return k;}
	    public List<Twitter> getTweetList() {return tweetList;}
	    public Map<Long, Twitter> getTweetMap() {return tweetMap;}
	    /*Set Methods*/
	    public void setCentroidList(List<Twitter> centerList) {this.centerList = centerList;}
	    public void setK(int k) {this.k = k;}
	    public void setTweetList(List<Twitter> tweetList) {this.tweetList = tweetList;}
	    public void setTweetMap(Map<Long, Twitter> tweetMap) {this.tweetMap = tweetMap;}
	    /*Constructor*/
	    public JaccardDist(int k) {
	        this.k = k;
	        tweetList = new ArrayList<>();
	        tweetMap = new HashMap<>();
	        centerList = new ArrayList<>();
	    }
	    /*******************************Converting json objects**************************************************/
	    public void jsonObjects(String inputFile) throws IOException, ParseException {
	        File file = new File(inputFile);
	        BufferedReader br = new BufferedReader(new FileReader(file));
	        String str;
	        try{
	        while ((str = br.readLine()) != null) {
	            JSONObject jsonObject = new JSONObject(str);
	            Twitter twitter = new Twitter();
	            twitter.setjsonCreateT(jsonObject.getString("created_at"));
	            twitter.setFrom_user(jsonObject.getString("from_user"));
	            twitter.setstr(jsonObject.getString("text"));
	            long id = jsonObject.getLong("id");
	            twitter.setId(id);
	            tweetMap.put(id, twitter);
	            tweetList.add(twitter);
	        }
	        br.close();
	        }
			catch(Exception ex){System.out.println(ex);}
	        
	    }
/*****************************ReadingInitial Centers from the file*****************************/
	    public List<Twitter> findInitCenter(String initailCenter) throws IOException {
	        File file = new File(initailCenter);
	        BufferedReader br = new BufferedReader(new FileReader(file));
	        String str;
	        while ((str = br.readLine()) != null) {
	            if (str.endsWith(",")) {
	                str = str.substring(0, str.length() - 1);
	                }
	            Twitter tweetDim = tweetMap.get(Long.valueOf(str));
	            centerList.add(tweetDim);
	        }
	        br.close();
	        return centerList;
	    }
/*************************************Jaccard Distnce helper method*******************************************/
	    public double JaccardDistanceF(final Twitter tweet1, final Twitter tweet2) {
	        double jaccardDistance = 0.0;
	        String[] strArr1 = tweet1.getstr().split("\\s");
	        String[] strArr2 = tweet2.getstr().split("\\s");

	        Set<String> tweetHashSet1 = new HashSet<>();
	        Set<String> tweetHashSet2 = new HashSet<>();

	        for (String wordInTweet1 : strArr1) {tweetHashSet1.add(wordInTweet1);}
	        for (String wordInTweet2 : strArr2) {tweetHashSet2.add(wordInTweet2);}

	        List<String> list1 = new ArrayList<>();
	        list1.addAll(tweetHashSet1);
	        List<String> list2 = new ArrayList<>();
	        list2.addAll(tweetHashSet2);

	        Collections.sort(list1);
	        int common = 0;
	        for (String s : list2) {
	            if (Collections.binarySearch(list1, s) >= 0) {
	            	common++;
	            }
	        }
	        int finSize = list1.size() + list2.size();
	        int union = finSize - common;
	        jaccardDistance = 1 - ((double) common / union);
	        return jaccardDistance;
	    }
	    
	    
	    /***********************************ClusterHelper Method**********************************/
	    
	    public void ClusterForTweet( List<Double> list,Long clustId) {
	        double MAX = Integer.MAX_VALUE;
	        int index = 0;
	        for (int i = 0; i < list.size(); i++) {
	            if (MAX > list.get(i)) {
	                index = i;
	                MAX = list.get(i);
	            }
	        }
	        tweetMap.get(clustId).setclustID(index + 1);
	    }
	}
	/****************************************HashMap of Cluster Mapping******************************/
	private static Map<Integer, List<Twitter>> ClustMapp(List<Twitter> tweetVals) {
		Map<Integer, List<Twitter>> clust_Map = new LinkedHashMap<Integer, List<Twitter>>();
		for (Twitter tv : tweetVals) {
			if (clust_Map.containsKey(tv.getclustID())) {
				List<Twitter> clustCount = clust_Map.get(tv.getclustID());
				clustCount.add(tv);
				clust_Map.put(tv.getclustID(), clustCount);
			} else {
				List<Twitter> nClustPoint = new ArrayList<>();
				nClustPoint.add(tv);
				clust_Map.put(tv.getclustID(), nClustPoint);
			}
		}
		return clust_Map;
	}
/****************************************SSE Calculation Method**********************************/
  private static double SSE_calculator(List<Twitter> tweets, JaccardDist jd,List<Twitter> centrePoints) 
	{
		double SSE = 0.0;
        Map<Integer, List<Twitter>> map_clust = ClustMapp(tweets);
        for (Integer id_Clust : map_clust.keySet()) {
        	Twitter centre= centrePoints.get(id_Clust - 1);
            List<Twitter> tweetClust = map_clust.get(id_Clust);
            for (Twitter tweet : tweetClust) {
                double jaccardDistance = jd.JaccardDistanceF(centre, tweet);
                SSE += jaccardDistance * jaccardDistance;
            }
        }
        return SSE;
    }
  /********************************************Computing Cost*************************************************/
	private static double costComputer(List<Twitter> centerPoints, JaccardDist jd,List<Twitter> tweets) {
		double DIST = 0;
		
		Map<Integer, List<Twitter>> MapCluster = ClustMapp(tweets);
		for (Integer clusterId : MapCluster.keySet()) {
			Twitter center = centerPoints.get(clusterId - 1);
			List<Twitter> twetForCluster = MapCluster.get(clusterId);

			for (Twitter tweet : twetForCluster) {
				DIST += jd.JaccardDistanceF(center, tweet);
			}
		}
		return DIST;
	}
  
	/******************************************Distance Creation****************************************/
	private static void createDist(Map<Long, List<Double>> disttMap,List<Twitter> centerPoint, 
			JaccardDist jd,List<Twitter> tweet )
	{

		for (Twitter t1 : centerPoint) {
			for (Twitter t2 : tweet) {
				double jaccardDistance = jd.JaccardDistanceF(t1, t2);
				if (disttMap.containsKey(t2.getId())) {
					List<Double> distThruCenter = disttMap.get(t2.getId());
					distThruCenter.add(jaccardDistance);
				} else {
					List<Double> JDlist = new ArrayList<>();
					JDlist.add(jaccardDistance);
					disttMap.put(t2.getId(), JDlist);
				}
			}
		}
	}

/***********************************Sending Result to the external file*****************************/
	@SuppressWarnings("unchecked")
	private static void writeResultOnFile(String OPFile,List<Twitter> tweets) throws IOException {
		try {
			File file = new File(OPFile);
			FileOutputStream fOut = new FileOutputStream(file);
			PrintStream ps = new PrintStream(fOut);
			System.setOut(ps);
		    } 
		catch (Exception e) {e.printStackTrace();}

		@SuppressWarnings("rawtypes")
		Map<Integer,ArrayList> OPMap = new HashMap<Integer,ArrayList>();
		for(Twitter user : tweets) {        
			if(OPMap.containsKey(user.getclustID())) {
				OPMap.get(user.getclustID()).add(user.getId());
			} else {
				ArrayList<Long> ID = new ArrayList<Long>();
				ID.add(user.getId());
				OPMap.put(user.getclustID(), ID);
			}
			}
		System.out.print("Value for SSE :"+SSE_ERR);
		System.out.println();
		//System.out.println("Value for SSE: "+ SSE_calculator(temp_tweet, jd,temp_center));
		for (@SuppressWarnings("rawtypes") Entry<Integer, ArrayList> value : OPMap.entrySet()) {
			System.out.print(value.getKey()+"  ");
			for(Object obj : value.getValue()){
				System.out.print(obj + ",");
			}
			System.out.println();
		}
	}
	
	/******************************************** Main Function *******************************************************/
	public static void main(String[] args) throws IOException, ParseException, JSONException {
		/*******Arguments Passing*****/
		int k = Integer.parseInt(args[0]);
		String TweetsJson = args[1];
		String seedInput = args[2];
		String result = args[3];
		JaccardDist jd = new JaccardDist(k);
		jd.jsonObjects(TweetsJson);
		List<Twitter> centers = jd.findInitCenter(seedInput);
		Map<Long, List<Double>> distMap = new LinkedHashMap<>();
		List<Twitter> tweetL = jd.getTweetList();

		createDist(distMap,centers,  jd,tweetL);

		for (Long i : distMap.keySet()) {
			jd.ClusterForTweet(distMap.get(i),i);
		}

		List<Twitter> temp_tweet = tweetL;
		List<Twitter> temp_center = centers;

		double cost = costComputer(centers, jd,tweetL);
		SSE_ERR = SSE_calculator(temp_tweet, jd,temp_center);
		Map<Integer, List<Twitter>> Map_Cluster = ClustMapp(tweetL);

		for (Integer i : Map_Cluster.keySet()) {
			List<Twitter> mc = Map_Cluster.get(i);
			for (Twitter tweet : mc) {
				centers.remove(i - 1);
				centers.add(i - 1, tweet);
				distMap.clear();
				
				createDist(distMap,centers, jd,tweetL );

				for (Long id : distMap.keySet()) {
					jd.ClusterForTweet(distMap.get(id),id );
				}

				double nCost = costComputer(centers, jd,tweetL);

				if (nCost < cost) {
					cost = nCost;
					temp_tweet = jd.getTweetList();
					temp_center = jd.getcenterList();
				}

			}
		}
		/*Output*/
		System.out.println("For "+ args[0] +" Cluster please chek the "+ args[3]+" file for the output");
		writeResultOnFile(result,temp_tweet);
	}
}





