package org.transcriptSearch;

import org.vertx.java.core.Handler;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.http.HttpServer;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.Verticle;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.transcriptSearch.Data;
public class Server extends Verticle {

  public void start() {
	  HttpServer server = vertx.createHttpServer();
	
	  server.requestHandler(new Handler<HttpServerRequest>() {
	      public void handle(final HttpServerRequest request) {
	          request.bodyHandler(new Handler<Buffer>() {
	              public void handle(Buffer body) {
	                // The entire body has now been received
	            
	                container.logger().info("The total body received was " + body.length() + " bytes");
	          	  String query=body.toString();
					          	try
					      	  {
					          		final JsonArray arr=search(query);
					          		container.logger().info(arr);
					          		 request.response().setChunked(true);
					          		 request.response().write(arr.encode()).end();
					      	  }
					      	  catch(Exception e)
					      	  {
					      		 request.response().write("Error while searching").end();
					      	  }
	                
					          	
	       
	              }
	          });
	      }
	  }).listen(8080, "localhost");
	  
  }
  public JsonArray search(String query)throws Exception
  {

	File file = new File(getClass().getClassLoader().getResource("mapping.txt").getFile());
	  
		FileReader fileReader = new FileReader(file);
		BufferedReader bufferedReader = new BufferedReader(fileReader);
		
		String line;
		HashMap<String, Data> map=new HashMap<String,Data>();
		while ((line = bufferedReader.readLine()) != null) {
			String temp[]=line.split(" ");
			String title=temp[2];
			for(int i=3; i<temp.length;i++)
			{
					title+=" "+temp[i];
				
			}
			map.put(temp[0],new Data(temp[1],title));
			
		}
		fileReader.close();
		
      File indexDir = new File(getClass().getClassLoader().getResource("indexes_split_transcripts").getFile());
  	
      JsonArray data=new JsonArray(); 	
      	 searchIndex(indexDir, query, 100,map,data);
      	 
      return data;
  }
  
  private void searchIndex(File indexDir, String queryStr, int maxHits,HashMap<String, Data> map,JsonArray rankedData) 
          throws Exception {
      Directory directory = FSDirectory.open(indexDir);
      IndexReader reader = DirectoryReader.open(directory);
      IndexSearcher searcher = new IndexSearcher(reader);
      QueryParser parser = new QueryParser("contents", new StandardAnalyzer());
      Query query = parser.parse(queryStr);
      
      TopDocs topDocs = searcher.search(query, maxHits);
      ScoreDoc[] hits = topDocs.scoreDocs;
      for (int i = 0; i < hits.length && i<10 ; i++) {
          int docId = hits[i].doc;
          Document d = searcher.doc(docId);
          System.out.println(d.get("filename")+"  hits:"+hits[i].score);
          String filename=d.get("filename");
          JsonObject obj=new JsonObject();
          String pattern = "(lec)(\\d+)";
          Pattern r = Pattern.compile(pattern);
          Matcher m = r.matcher(filename);
          String lec_filename=null;
			if(m.find())
          	lec_filename=m.group(0);
          BufferedReader br = new BufferedReader(new FileReader("D:/VideoSearch/Transcripts_split/"+lec_filename+"/"+filename));
          pattern="(\\(Refer Slide Time: )(((\\d++)(:))?(\\d++)(:)(\\d++))(\\))";
          String line=br.readLine();
          br.close();
          r = Pattern.compile(pattern);
          Matcher m2 = r.matcher(line);
          String start_time=null;
          if(m2.find())
          	start_time=m2.group(2);
          obj.putString("title",map.get(lec_filename+".txt").getTitle());
         obj.putString("videoId",map.get(lec_filename+".txt").getVideoId());
          obj.putString("start_time", start_time);
          rankedData.add(obj);
      }   
      System.out.println("Found " + hits.length);        
  }
  
}

