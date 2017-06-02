package com.jackchan.lyricview.bean;

public class LyricBean implements Comparable<LyricBean> {

    /**
     * 一行歌词开始时间
     *
     * @author JackChan
     * create at 2017/6/2
     */
    public int    startPoint;

    /**
     * 一行歌词的内容
     *
     * @author JackChan
     * create at 2017/6/2
     */
    public String content;

    public LyricBean(int startPoint, String content) {
        this.startPoint = startPoint;
        this.content = content;
    }

    @Override
    public int compareTo(LyricBean another) {
        return startPoint - another.startPoint;
    }
}