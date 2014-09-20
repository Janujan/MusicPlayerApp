package com.example.musicplayerapp;

import android.annotation.TargetApi;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.media.MediaPlayer;
import java.util.ArrayList;
import android.content.ContentUris;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.PowerManager;
import android.util.Log;
import java.util.Random;
import android.app.Notification;
import android.app.PendingIntent;



public class MusicService extends Service implements MediaPlayer.OnPreparedListener, 
MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener{

	private MediaPlayer player;
	private ArrayList<Song> songs;
	private int songPosition;
	private final IBinder musicBind = new MusicBinder( );
	private String songTitle = "";
	private static final int NOTIFY_ID = 1;
	private boolean shuffle = false;
	private Random rand;
	
	

	public void onCreate( ){
		
		//create the service
		super.onCreate();
		
		//start position is at 0
		songPosition = 0;
		
		//create player
		player= new MediaPlayer();
		
		initiateMusicPlayer();
		
		rand = new Random( );
		
		
	}
	
	
	//initiate the music player 
	public void initiateMusicPlayer( ){
		player.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
		player.setAudioStreamType(AudioManager.STREAM_MUSIC);
		
		player.setOnCompletionListener(this);
		player.setOnErrorListener(this);
		player.setOnPreparedListener(this);
		
		
	}
	
	//pass song list to player
	public void setSongList(ArrayList<Song> songlist){
		songs = songlist;
	}
	
	//Binder method that is required
	public class MusicBinder extends Binder{
		MusicService getService( ) {
			return MusicService.this;
		}
	}
	@Override
	public IBinder onBind(Intent intent) {
		return musicBind;
	}

	@Override
	public void onCompletion(MediaPlayer mp) {
		if(player.getCurrentPosition() > 0){
			mp.reset();
			playNext( );
		}
		
	}

	@Override
	public boolean onError(MediaPlayer mp, int what, int extra) {
		mp.reset();
		return false;
	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	@Override
	public void onPrepared(MediaPlayer mp) {
		//start the playback
		mp.start();
		
		Intent notIntent = new Intent(this, MainActivity.class);
		notIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		PendingIntent pendInt = PendingIntent.getActivity(this, 0, notIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		
		Notification.Builder builder = new Notification.Builder(this);
		
		builder.setContentIntent(pendInt)
			.setSmallIcon(R.drawable.play)
			.setTicker(songTitle)
			.setOngoing(true)
			.setContentTitle("Playing")
			.setContentText(songTitle);
		Notification not = builder.build();
		
		startForeground(NOTIFY_ID, not);
		
	}
	
	@Override
	public boolean onUnbind(Intent intent){
		player.stop();
		player.release();
		return false;
	}
	
	public void playSong(){
		player.reset();
		
		Song playSong= songs.get(songPosition);
		songTitle = playSong.getSongTitle( );
		long currSong = playSong.getSongId();
		
		Uri trackUri = ContentUris.withAppendedId(android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, currSong);
		
		try{
			player.setDataSource(getApplicationContext(), trackUri);
		}
		
		catch(Exception e){
			Log.e("MUSIC SERVICE", "Error setting data source", e);
		}
		
		player.prepareAsync();
		
	}
	
	public void setSong(int songIndex){
		songPosition = songIndex;
	}
	
	public int getPosn(){
		  return player.getCurrentPosition();
	}
		 
	public int getDur(){
		  return player.getDuration();
	}
		 
	public boolean isPng(){
		  return player.isPlaying();
	}
		 
	public void pausePlayer(){
		  player.pause();
	}
		 
	public void seek(int posn){
		  player.seekTo(posn);
	}
		 
	public void go(){
		  player.start();
	}
	
	public void playPrev( ){
		songPosition--;
		if(songPosition < 0) songPosition = songs.size() - 1;
		playSong();
	}
	
	public void playNext( ){
		
		if(shuffle){
			int newSong = songPosition;
			while(newSong == songPosition){
				newSong = rand.nextInt(songs.size());
			}
			
			songPosition = newSong;
		}
		
		else{
			songPosition++;
			if(songPosition >= songs.size()) songPosition = 0; 
			playSong();
		}

	}
	
	@Override
	public void onDestroy( ){
		stopForeground(true);
	}
	
	public void setShuffle( ){
		if(shuffle) shuffle = false;
		else shuffle = true;
	}

}
