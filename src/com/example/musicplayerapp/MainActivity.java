package com.example.musicplayerapp;

import android.app.Activity;
import android.os.Bundle;
import android.os.IBinder;
import android.view.Menu;
import android.view.MenuItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import com.example.musicplayerapp.MusicService.MusicBinder;

import android.net.Uri;
import android.content.ContentResolver;
import android.database.Cursor;
import android.widget.ListView;
import android.os.Binder;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.view.MenuItem;
import android.view.View;
import android.widget.MediaController.MediaPlayerControl;


public class MainActivity extends Activity implements MediaPlayerControl{
	
	//store the list of songs
	private ArrayList<Song> songList;
	
	//store the listView
	private ListView songView;

	
	private MusicService musicSRV;
	private Intent playIntent;
	private boolean musicBound = false;
	
	private MusicController controller;
	private boolean paused=false, playbackPaused=false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		//instantiate the songView and songList
		songView = (ListView)findViewById(R.id.song_list);
		songList = new ArrayList<Song>();
		
		getSongList();
		
		//sort the songs based on title
		//can also sort based on artist
		Collections.sort(songList, new Comparator<Song>( ){
			public int compare(Song a, Song b){
				return a.getSongTitle().compareTo(b.getSongTitle());
			}
		});
		
		SongAdaptor songADPT = new SongAdaptor(this, songList);		
		songView.setAdapter(songADPT);
		
		setController( );
	}

	//connect to the service
	private ServiceConnection musicConnection = new ServiceConnection( ){

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			// TODO Auto-generated method stub
			
			MusicBinder binder = (MusicBinder)service;
			
			//get the musicplayer service
			musicSRV = binder.getService();
			
			//pass on the list
			musicSRV.setSongList(songList);
			musicBound = true;
		}

		@Override
		public void onServiceDisconnected(ComponentName arg0) {
			// TODO Auto-generated method stub
			musicBound = false;
			
		}
		
	};
	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		
		switch(item.getItemId()){
		
		case R.id.action_end:
			//close the app
			//stopService(play)
			stopService(playIntent);
			musicSRV = null;
			System.exit(0);
			break;
		
		case R.id.action_shuffle:
			musicSRV.setShuffle();
			break;
		}
		return super.onOptionsItemSelected(item);
	}
	
	//find the song information
	public void getSongList( ){
		ContentResolver musicResolver = getContentResolver( );
		Uri musicUri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
		Cursor musicCursor = musicResolver.query(musicUri, null, null, null, null);
		
		if(musicCursor!= null && musicCursor.moveToFirst()){
			//get column numbers
			int titleColumn = musicCursor.getColumnIndex
					(android.provider.MediaStore.Audio.Media.TITLE);
			int idColumn = musicCursor.getColumnIndex
					(android.provider.MediaStore.Audio.Media._ID);
			int artistColumn = musicCursor.getColumnIndex
					(android.provider.MediaStore.Audio.Media.ARTIST);
			
			//adding the songs to the list
			do{
				long songId = musicCursor.getLong(idColumn);
				String songTitle = musicCursor.getString(titleColumn);
				String songArtist = musicCursor.getString(artistColumn);
				songList.add(new Song(songId, songTitle, songArtist));
			}
			while(musicCursor.moveToNext());
		}
	}
	
	@Override
	protected void onStart( ){
		super.onStart();
		if(playIntent == null ){
			playIntent = new Intent(this, MusicService.class);
			bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
			startService(playIntent);
		}
	}
	
	public void songPicked(View view){
		musicSRV.setSong(Integer.parseInt(view.getTag( ).toString()));
		musicSRV.playSong();
		
		if(playbackPaused){
			setController( );
			playbackPaused = false;
		}
		
		controller.show(0);
	}
	
	@Override
	protected void onDestroy( ){
		stopService(playIntent);
		musicSRV = null;
		super.onDestroy();
	}

	@Override
	public boolean canPause() {
		return true;
	}

	@Override
	public boolean canSeekBackward() {
		return true;
	}

	@Override
	public boolean canSeekForward() {
		return true;
	}

	@Override
	public int getAudioSessionId() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getBufferPercentage() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getCurrentPosition() {
	
		if(musicSRV!=null && musicBound && musicSRV.isPng()){
			
		    return musicSRV.getPosn();
		}
		else return 0;
	}

	@Override
	public int getDuration() {
		if(musicSRV != null && musicBound && musicSRV.isPng()){
			return musicSRV.getDur();
		}
		
		else return 0;
		
	}

	@Override
	public boolean isPlaying() {
		if(musicSRV!= null && musicBound){
			return musicSRV.isPng();
		}
		
		return false;
	}

	@Override
	public void pause() {
		
		playbackPaused = true;
		musicSRV.pausePlayer();
		
	}

	@Override
	public void seekTo(int pos) {
		musicSRV.seek(pos);
	}

	@Override
	public void start() {
		musicSRV.go();
	}
	
	public void setController( ){
		controller = new MusicController(this);
		
		controller.setPrevNextListeners(new View.OnClickListener() {
			  @Override
			  public void onClick(View v) {
			    playNext();
			  }
			}, new View.OnClickListener() {
			  @Override
			  public void onClick(View v) {
			    playPrev();
			  }
			});
		
		controller.setMediaPlayer(this);
		controller.setAnchorView(findViewById(R.id.song_list));
		controller.setEnabled(true);
		
	}
	
	public void playNext( ){
		musicSRV.playNext();
		
		if(playbackPaused){
			setController();
			playbackPaused = false;
		}
		controller.show(0);
	}
	
	public void playPrev( ){
		musicSRV.playPrev();
		
		if(playbackPaused){
			setController( );
			playbackPaused=false;
		}
		controller.show(0);
	}
	
	@Override
	protected void onPause( ){
		super.onPause();
		paused = true;
	}
	
	@Override
	protected void onResume( ){
		super.onResume();
		if(paused){
			setController();
			paused = false;
		}
	}
	
	@Override
	protected void onStop( ){
		controller.hide();
		super.onStop();
	}
	
}
