package org.transcriptSearch;

public class Data {
	String videoId;
	String title;
	
	public Data(String id,String title)
	{
		videoId=id;
		this.title=title;
	}
	public String getVideoId()
	{
		return videoId;
	}
	public String getTitle()
	{
		return title;
	}

	
}
