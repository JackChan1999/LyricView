package com.jackchan.lyricview.utils;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import com.jackchan.lyricview.bean.Music;
import com.jackchan.lyricview.constant.Constants;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class MediaUtils {
	public static List<Music> songList    = new ArrayList<Music>();
	public static int         CURSTATE    = Constants.STATE_STOP;
	public static int         CURPOSITION = 0;
	public static int         CURMODEL    = Constants.MODEL_NORMAL;

	//加载手机里面的本地音乐-->sqlite-->contentProvider
	/**
	 * 加载本地的音乐
	 * @param context
	 */
	public static void initSongList(Context context) {
		songList.clear();
		Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
		/**
		 * public String name;//歌曲名
		 * public String author;//歌手
		 * public String path;//路径
		 */
		String[] projection = { MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media.ARTIST,
				MediaStore.Audio.Media.DATA };
		Cursor c = context.getContentResolver().query(uri, projection, null, null, null);
		while (c.moveToNext()) {
			String title = c.getString(c.getColumnIndex(MediaStore.Audio.Media.TITLE));
			String artist = c.getString(c.getColumnIndex(MediaStore.Audio.Media.ARTIST));
			String path = c.getString(c.getColumnIndex(MediaStore.Audio.Media.DATA));
			Music music = new Music(title, artist, path);
			songList.add(music);
		}
	}

	public static String duration2Str(int duration) {
		//		"00:11" "11:11"
		String result = "";
		int i = duration / 1000;
		int min = i / 60;//1 2  3 
		int sec = i % 60;// 0-59  
		if (min > 9) {
			if (sec > 9) {
				result = min + ":" + sec;
			} else {
				result = min + ":0" + sec;
			}
		} else {
			if (sec > 9) {
				result = "0" + min + ":" + sec;
			} else {
				result = "0" + min + ":0" + sec;
			}
		}
		return result;
	}

	/**
	 * 根据歌曲的路径,得到对应的lrc
	 * @param path
	 * @return
	 */
	public static File getLrcFile(String path) {
		File file;
		String lrcName = path.replace(".mp3", ".lrc");//找歌曲名称相同的.lrc文件
		file = new File(lrcName);
		if (!file.exists()) {
			lrcName = path.replace(".mp3", ".txt");//歌词可能是.txt结尾
			file = new File(lrcName);
			if (!file.exists()) {
				return null;
			}
		}
		return file;
	}
}
