package org.transcriptSearch;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

public class FileIndexer {
	private ArrayList<File> queue = new ArrayList<File>();
	 private IndexWriter writer;
    public static void main(String[] args) throws Exception {
    	
        FileIndexer indexer=new FileIndexer();
        indexer.initialise();
    }
    public void initialise() throws Exception
    {	
    	File indexDir = new File(getClass().getClassLoader().getResource("indexes_split_transcripts").getFile());
    	File dataDir = new File(getClass().getClassLoader().getResource("Transcripts_split").getFile());
         
        int numIndex = index(indexDir, dataDir);
        
        System.out.println("Total files indexed " + numIndex);
    }
    private int index(File indexDir, File dataDir) throws Exception {
        
    	FSDirectory dir=FSDirectory.open(indexDir);
    	Analyzer analyzer  = new StandardAnalyzer();
    	IndexWriterConfig config=new IndexWriterConfig(Version.LUCENE_4_10_1, analyzer);
        writer=new IndexWriter(dir, config);
        indexFileOrDirectory(dataDir);
        int numIndexed = writer.maxDoc();
        writer.close();
        return numIndexed;
        
    }
    
    public void indexFileOrDirectory(File dataDir) throws IOException {
        addFiles(dataDir);
        
        int originalNumDocs = writer.numDocs();
        for (File f : queue) {
          FileReader fr = null;
          try {
            Document doc = new Document();

            fr = new FileReader(f);
            doc.add(new TextField("contents", fr));
            doc.add(new StringField("path", f.getPath(), Field.Store.YES));
            doc.add(new StringField("filename", f.getName(), Field.Store.YES));

            writer.addDocument(doc);
            System.out.println("Added: " + f);
          } catch (Exception e) {
            System.out.println("Could not add: " + f);
          } finally {
            fr.close();
          }
        }
        
        int newNumDocs = writer.numDocs();
        System.out.println("");
        System.out.println("************");
        System.out.println((newNumDocs - originalNumDocs) + " documents added.");
        System.out.println("************");

        queue.clear();
      }
    private void addFiles(File file) {

        if (!file.exists()) {
          System.out.println(file + " does not exist.");
        }
        if (file.isDirectory()) {
          for (File f : file.listFiles()) {
            addFiles(f);
          }
        } else {
          String filename = file.getName().toLowerCase();
         if (filename.endsWith(".txt")) {
        	
            queue.add(file);
          } else {
            System.out.println("Skipped " + filename);
          }
        }
      }
}