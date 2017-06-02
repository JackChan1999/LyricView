package com.jackchan.lyricview.utils;

import com.jackchan.lyricview.bean.LyricBean;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LyricParser {
    /**
     * 解析歌词
     * @param file 歌词文件
     * @return
     */
    public static List<LyricBean> parserLyricFromFile(File file){
        List<LyricBean> lyricBeanList=new ArrayList<>();
        if (file==null || !file.exists()){
            lyricBeanList.add(new LyricBean(0,"未加载到歌词"));
        }

        try {
            BufferedReader bufferedReader=new BufferedReader(new InputStreamReader(
                    new FileInputStream(file),"GBK"));
            String readLine = bufferedReader.readLine();
            while (readLine!=null){
               List<LyricBean> lyricBeans= parserReadLine(readLine);
                lyricBeanList.addAll(lyricBeans);
                readLine=bufferedReader.readLine();
            }

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Collections.sort(lyricBeanList);
        return lyricBeanList;
    }

    /**
     * 解析一行歌词
     * @param readLine 歌词
     * @return
     */
    private static List<LyricBean> parserReadLine(String readLine) {
        // [00:22.67]苍茫的天涯是我的爱
        List<LyricBean> lyricBeens=new ArrayList<>();
        String[] strings = readLine.split("]");
        for (int i=0;i<strings.length-1;i++){
            String time = strings[i];
            int startpoint=parserTime(time);
            lyricBeens.add(new LyricBean(startpoint,strings[strings.length-1]));
        }
        return lyricBeens;
    }

    /**
     * 解析时间
     * @param time 时间
     * @return
     */
    private static int parserTime(String time) {
        // [00:22.67
        String[] times = time.split(":");
        String min=times[0].substring(1); // 分

        times = times[1].split("\\.");
        String sec=times[0]; // 秒
        String mSec=times[1]; // 毫秒

        return   Integer.parseInt(min) * 60 * 1000+Integer.parseInt(sec) * 1000+Integer.parseInt(mSec) * 10;
    }
}