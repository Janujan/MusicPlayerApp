package com.example.musicplayerapp;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import java.util.ArrayList;
import android.content.Context;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;




public class SongAdaptor extends BaseAdapter {
	
	private ArrayList<Song> songList;
	private LayoutInflater songInf;
	
	//constructor for songAdaptor class
	public SongAdaptor(Context c, ArrayList<Song> songs){
		songList = songs;
		songInf = LayoutInflater.from(c);
	}

	@Override
	public int getCount() {
		return songList.size();
	}

	@Override
	public Object getItem(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getItemId(int arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		//map song.xml to songlayout
		LinearLayout songLayout = (LinearLayout)songInf.inflate(R.layout.song, parent, false);
		
		//get title and artist view
		TextView songView = (TextView)songLayout.findViewById(R.id.song_title);
		TextView songArtist = (TextView)songLayout.findViewById(R.id.song_artist);
		
		//find song using position
		Song currentSong = songList.get(position);
		
		//Title of song and artist
		songView.setText(currentSong.getSongTitle());
		songArtist.setText(currentSong.getSongArtist());
		
		//set position as tag
		songLayout.setTag(position);
		
		return songLayout;
	}

}
