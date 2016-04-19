package player;

import android.database.Cursor;
import android.provider.MediaStore.Video.Media;

import java.io.Serializable;

public class VideoItem implements Serializable{
	private String title;
	private long duration;
	private long size;
	private String path;
	
	public static VideoItem fromCursor(Cursor cursor){
		VideoItem videoItem = new VideoItem();
		videoItem.setDuration(cursor.getLong(cursor.getColumnIndex(Media.DURATION)));
		videoItem.setPath(cursor.getString(cursor.getColumnIndex(Media.DATA)));
		videoItem.setSize(cursor.getLong(cursor.getColumnIndex(Media.SIZE)));
		videoItem.setTitle(cursor.getString(cursor.getColumnIndex(Media.TITLE)));
		return videoItem;
	}
	
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public long getDuration() {
		return duration;
	}
	public void setDuration(long duration) {
		this.duration = duration;
	}
	public long getSize() {
		return size;
	}
	public void setSize(long size) {
		this.size = size;
	}
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
	
	
}
