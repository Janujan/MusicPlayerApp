package com.example.musicplayerapp;

public class Song {
	private long songId;
	private String title;
	private String artist;
	
	//constructor for the song class
	public Song(long songID, String songTitle, String artistName){
		
		songId = songID;
		title = songTitle;
		artist = artistName;
		
	}
	
	//get methods to return information about the song
	public long getSongId(){
		return songId;
	}
	
	public String getSongTitle( ){
		return title;
	}
	
	public String getSongArtist( ){
		return artist;
	}
}
