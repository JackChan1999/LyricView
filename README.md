## 歌词展示

### 封装歌词信息

歌词的内容如下，一行歌词由两部分组成，[]里面的是开始时间，后面的是歌词内容

```
[00:03.25]最炫民族风 - 凤凰传奇
[00:08.67]献给苦逼的黑马程序员
[00:22.67]苍茫的天涯是我的爱
[00:26.42]绵绵的青山脚下花正开
[00:30.18]什么样的节奏是最呀最摇摆
[00:33.90]什么样的歌声才是最开怀
[00:37.71]弯弯的河水从天上来
[00:41.51]流向那万紫千红一片海
[00:45.27]火辣辣的歌谣是我们的期待
[00:49.05]一路边走边唱才是最自在
[00:52.86]我们要唱就要唱得最痛快
[00:56.61]你是我天边 最美的云彩
...
```
对应的实体类为
```java
public class Lyric implements Comparable<Lyric>{
    private int startPoint; // 开始时间
    private String content; // 一行歌词的内容

    public Lyric(int startPoint, String content) {
        this.startPoint = startPoint;
        this.content = content;
    }

    public int getStartPoint() {
        return startPoint;
    }

    public void setStartPoint(int startPoint) {
        this.startPoint = startPoint;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public int compareTo(Lyric another) {
        return startPoint-another.getStartPoint();
    }
}
```

### 绘制单行居中文本

自定义一个显示歌词的LyricView，歌词本身就是一个文本，所以在这里我们继承TextView。它还有一个好处继承TextView 之后不需要再去重写onMeasure 方法。在onDraw 方法中去绘制一个文本。

```java
public class LyricView extends TextView {
    private float hightlightSize; // 高亮歌词字体大小
    private float normalSize;
    private int   hightLightColor; // 高亮歌词字体颜色
    private int   normalColor;
    private Paint paint;

    public LyricView(Context context) {
        super(context);
        initView();
    }

    public LyricView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public LyricView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

  	// 初始化字体大小和颜色
    private void initView() {
        hightlightSize = getResources().getDimension(R.dimen.lyric_hightlight_size);
        normalSize = getResources().getDimension(R.dimen.lyric_normal_size);
        hightLightColor = Color.GREEN;
        normalColor = Color.WHITE;
        paint = new Paint();
        paint.setAntiAlias(true);//抗锯齿
        paint.setTextSize(hightlightSize);
        paint.setColor(hightLightColor);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        String text = "正在加载歌词...";
        canvas.drawText(text, 0, 0, paint);
    }
}
```

在项目中的歌词布局中引用View，重新build 之后的展示效果

![自定义歌词展示控件](https://alleniverson.gitbooks.io/customwidget/content/assets/1496388538759.png)

从上图中可以看到文本显示的坐标是view 的左上角。那么我们需要将文本显示的位置设置在view 的中间。计算的方法如图

![自定义歌词展示控件](https://alleniverson.gitbooks.io/customwidget/content/assets/1496388572189.png)

在onSizeChang 中计算出View 宽和高的一半，通过paint.getTextBounds 方法计算出文本的宽高的一半。

```java
@Override
protected void onSizeChanged(int w, int h, int oldw, int oldh) {
    super.onSizeChanged(w, h, oldw, oldh);
    //计算View 的宽和高
    halfViewW = w / 2;
    halfViewH = h / 2;
}

@Override
protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);
    String text = "正在加载歌词...";
    Rect bounds = new Rect();
  	// paint.getTextBounds(text,0,text.length,bounds); //  测量歌词内容文本矩形的大小

    //计算text 的宽和高
    int halfTextW = bounds.width() / 2;
    int halfTextH = bounds.height() / 2;
    //计算text 位置
    int drawX = halfViewW - halfTextW;
    int drawY = halfTextH + halfViewH;
    canvas.drawText(text, drawX, drawY, paint);
}
```

重新build 之后的效果如下：

![自定义歌词展示控件](https://alleniverson.gitbooks.io/customwidget/content/assets/1496388683307.png)

但是在Android Studio中使用bounds.width 方法获取的文本宽度设置之后不在View 的中间。所以我们使用了paint.getTextMeasure(text)来重新获取

```java
int halfTextW= (int) paint.measureText(text)/2;
// paint.getTextBounds(text,0,text.length,bounds); //  测量歌词内容文本矩形的大小
```

### 绘制多行歌词

首先用List 模拟歌词的数据并且记录高亮行的行数。

```java
private void initView() {
    hightlightSize = getResources().getDimension(R.dimen.lyric_hightlight_size);
    normalSize = getResources().getDimension(R.dimen.lyric_normal_size);
    hightLightColor = Color.GREEN;
    normalColor = Color.WHITE;
    paint = new Paint();
    paint.setAntiAlias(true);//抗锯齿
    paint.setTextSize(hightlightSize);
    paint.setColor(hightLightColor);
    //高亮的行数
    currentLine = 5;
    //模拟初始化数据
    lyrics = new ArrayList<>();
    for (int i = 0; i < 30; i++) {
        lyrics.add(new Lryic(i * 2000, "当前正在播放行数为：" + i));
    }
}
```

获取高亮行的位置。

```java
/**
 * 绘制多行文本
 */
private void drawMutiLineText(Canvas canvas) {
    Lryic lyric = lyrics.get(currentLine);

    //获取高亮行Y 的位置
    Rect bounds = new Rect();
    //计算text的宽和高
    paint.getTextBounds(lyric.getContent(), 0, lyric.getContent().length(), bounds);
    // int halfTextW=bounds.width()/2;
    int halfTextH = bounds.height() / 2;
    int centerY = halfTextH + halfViewH;
}
```

按行绘制文本。

```java
//按行绘制文本
for (int i = 0; i < lyrics.size(); i++) {
    if (currentLine == i) {
        paint.setColor(hightLightColor);
        paint.setTextSize(hightlightSize);
    } else {
        paint.setColor(normalColor);
        paint.setTextSize(normalSize);
    }
}
```

y=居中行y 的位置+(绘制行的位置-高亮行的行数)*行高。

```java
lineHeight=getResources().getDimensionPixelSize(R.dimen.lyric_line_height);
//y=居中行Y 的位置+(绘制行的行数-高亮行的行数)*行号
int downY=centerY+(i-currentLine)*lineHeight;
```

x=水平居中的x。

```java
//x=水平居中使用的x
drawHorizontalText(canvas,lyrics.get(i).getContent(),downY);
```

效果图如下

![自定义歌词展示控件](https://alleniverson.gitbooks.io/customwidget/content/assets/1496389150305.png)

### 按行滚动歌词

在LyricView 中提供一个滚动歌词的方法。说白了其实只要设置歌词高亮的位置就可以了。设置歌词高亮的位置的算法如图

![自定义歌词展示控件](https://alleniverson.gitbooks.io/customwidget/content/assets/1496389191321.png)

```java
/** 根据当前播放时间，改变高亮行的位置*/
public void roll(int position,int duration){
    for (int i = 0; i < lyrics.size(); i++) {
        Lyric lyric=lyrics.get(i);
        if (i==lyrics.size()-1){
            //最后一行
            endPoint = duration;
        }else{
            Lyric nextLyric=lyrics.get(i+1);
            endPoint=nextLyric.getStartPoint();
        }
        if (lyric.getStartPoint()<=position&&endPoint>position){
            currentLine=i;
            break;
        }
    }
    invalidate();
}
```

在音乐播放界面中发消息让歌词滚动。在接收到准备完成的广播之后就让歌词开始滚动。

```java
private static final int     UPDATE_LRYIC_ROLL = 1;
private  Handler handler   = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UPDATE_POSITION:
                    updateCurrentPosition();
                    break;
                case UPDATE_LRYIC_ROLL:
                    startRoll();
                    break;
            }
        }
    };

    private class AudioBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            //准备完成
            //更新界面的按钮
            updatePlayBtn();
            //初始化歌曲名和歌手
            AudioItem audioItem = (AudioItem) intent.getSerializableExtra("audioItem");
            tv_name.setText(StringUtil.formatDisplayName(audioItem.getName()));
            tv_artist.setText(audioItem.getArtist());
            sk_position.setMax(binder.getDuration());
            //更新播放进度
            updateCurrentPosition();
            //初始化播放模式
            updatePlayModeBtn();
            //开启歌词滚动更新
            startRoll();
        }
    }

    /**
     * 开启歌词滚动更新
     */
    private void startRoll() {
        lyricView.roll(binder.getCurrentPosition(), binder.getDuration());
        handler.sendEmptyMessage(UPDATE_LRYIC_ROLL);
    }
```

![自定义歌词展示控件](https://alleniverson.gitbooks.io/customwidget/content/assets/1496389354853.png)   ![自定义歌词展示控件](https://alleniverson.gitbooks.io/customwidget/content/assets/1496389341701.png)

### 平滑滚动歌词

平滑滚动歌词的算法如图

![自定义歌词展示控件](https://alleniverson.gitbooks.io/customwidget/content/assets/1496389415736.png)

计算时使用的已播放时间和播放总时间需要从roll 方法中获取

```java
/**
 * 绘制多行文本
 */
private void drawMutiLineText(Canvas canvas) {
    Lyric lyric = lyrics.get(currentLine);
    int endStartPoint;
    //变化位置=居中行位置+偏移位置
    //偏移位置=移动百分比*行高
    //移动时间百分比=移动时间/可用时间
    //可用时间=下一段的时间-本段的时间
    //移动时间=已播放时间-起始时间
    if (currentLine == lyrics.size() - 1) {
        //最后一行
        endStartPoint = mDuration;
    } else {
        Lyric nextLyric = lyrics.get(currentLine + 1);
        endStartPoint = nextLyric.getStartPoint();
    }
    int moveTime = mPosition - lyric.getStartPoint();
    int useTime = endStartPoint - lyric.getStartPoint();
    float movePercent = moveTime / (float) useTime;
    int offset = (int) (movePercent * lineHeight);
    //获取高亮行Y 的位置
    Rect bounds = new Rect();
    //计算text 的宽和高
    paint.getTextBounds(lyric.getContent(), 0, lyric.getContent().length(), bounds);
    // int halfTextW=bounds.width()/2;
    int halfTextH = bounds.height() / 2;
    // canvas.translate(0,-offset);
    int centerY = halfTextH + halfViewH - offset;
    //按行绘制文本
    for (int i = 0; i < lyrics.size(); i++) {
        if (currentLine == i) {
            paint.setColor(hightLightColor);
            paint.setTextSize(hightlightSize);
        } else {
            paint.setColor(normalColor);
            paint.setTextSize(normalSize);
        }
        //y=居中行Y 的位置+(绘制行的行数-高亮行的行数)*行号
        int downY = centerY + (i - currentLine) * lineHeight;
        //x=水平居中使用的x
        drawHorizontalText(canvas, lyrics.get(i).getContent(), downY);
    }
}
```

运行结果

![自定义歌词展示控件](https://alleniverson.gitbooks.io/customwidget/content/assets/1496389556569.png)![自定义歌词展示控件](https://alleniverson.gitbooks.io/customwidget/content/assets/1496389545464.png)

### 从文件中解析歌词

从文件中解析歌词。将歌词一行一行的读出来，并且根据歌词的格式解析成List 集合，并将歌词排序。

```java
import com.jackchan.medioplayer.bean.Lyric;

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
     * 从歌词文件中解析歌词列表
     */
    public static List<Lyric> parseLyricFromFile(File lyricFile) {
        List<Lyric> lyrics = new ArrayList<>();
        //数据可用性检查
        if (lyricFile == null || !lyricFile.exists()) {
            lyrics.add(new Lyric(0, "没有找到歌词文件"));
            return lyrics;
        }
        try {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new
                    FileInputStream(lyricFile), "GBK"));
            String line = bufferedReader.readLine();
            while (line != null) {
                List<Lyric> lineLyrics = parserLine(line);
                lyrics.addAll(lineLyrics);
                line = bufferedReader.readLine();
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //歌词排序
        Collections.sort(lyrics);
        return lyrics;
    }

    /**
     * 解析一行歌词[ 01:22.51][ 01:22.51]滴答滴答
     */
    private static List<Lyric> parserLine(String line) {
        List<Lyric> lineLyric = new ArrayList<>();
        // [ 01:22.51 [ 01:22.51 滴答滴答
        String[] arr = line.split("]");
        String content = arr[arr.length - 1];
        for (int i = 0; i < arr.length - 1; i++) {
            int startPoint = parserPoint(arr[i]);
            lineLyric.add(new Lyric(startPoint, content));
        }
        return lineLyric;
    }

    /**
     * 解析一行歌词的时间[ 01:22.51
     */
    private static int parserPoint(String s) {
        int time = 0;
        String timeStr = s.substring(1);
        // 01:22.51
        String[] arr = timeStr.split(":");
        // 01 22.51
        String minStr = arr[0];
        arr = arr[1].split("\\.");
        String senStr = arr[0];
        String mSenStr = arr[1];
        time = Integer.parseInt(minStr) * 60 * 1000 + Integer.parseInt(senStr) * 1000 + Integer.parseInt(mSenStr) * 100;
        return time;
    }
}
```

需要实现Comparable 接口，实现compareTo 方法

```java
 @Override
 public int compareTo(Lyric lyric) {
    return startPoint-lyric.getStartPoint();
 }
```

在LyricView 中提供从文件中获取歌词集合和设置当前高亮行的方法。

```java
 public void setLyricFile(File lyricFile){
    lyrics=LyricParser.parseLyricFromFile(lyricFile);
    currentLine=0;
 }
```

在onDraw 方法中绘制的时候，需要去判断集合是否有数据，没有数据的话就显示歌词正在加载中，如果有数据的话就显示歌词。

```java
@Override
protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);
    if (lyrics==null||lyrics.size()==0){
        //绘制单行居中
        drawSingleLineText(canvas);
    }else{
        drawMutiLineText(canvas);
    }
}
```

在接收准备的广播中的滚动歌词之前将歌词加载出来。

```java
private class AudioBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        //准备完成
        //更新界面的按钮
        updatePlayBtn();
        //初始化歌曲名和歌手
        AudioItem audioItem= (AudioItem) intent.getSerializableExtra("audioItem");
        tv_name.setText(StringUtil.formatDisplayName(audioItem.getName()));
        tv_artist.setText(audioItem.getArtist());
        sk_position.setMax(binder.getDuration());
        //更新播放进度
        updateCurrentPosition();
        //初始化播放模式
        updatePlayModeBtn();
        File file=new File(Environment.getExternalStorageDirectory(),"test/audio/"+
                StringUtil.formatDisplayName(audioItem.getName())+".lrc");
        lyricView.setLyricFile(file);
        //开启歌词滚动更新
        startRoll();
    }
}
```

运行结果

![自定义歌词展示控件](https://alleniverson.gitbooks.io/customwidget/content/assets/1496389891824.png)![自定义歌词展示控件](https://alleniverson.gitbooks.io/customwidget/content/assets/1496389881182.png)

### 歌词加载模块

我们发现北京北京的歌词没有加载出来。是因为上面我们传的文件时lrc 后缀的文件，但如图北京北京的歌词的后缀是txt，所以在这里我们需要写一个歌词加载器。当文件中没有lrc 后缀的歌词的时候，就看看有没有txt 后缀的歌词，如果都没有的话需要从服务器下载。

```java
package com.jackchan.medioplayer.db;

import android.os.Environment;

import java.io.File;

public class LyricLoader {
    private static final File root = new
            File(Environment.getExternalStorageDirectory(), "/test/audio");

    //加载歌词文件
    public static File loadLyricFile(String title) {
        //查找lrc 文件
        File lyricFile = new File(root, title + ".lrc");
        if (lyricFile.exists()) {
            return lyricFile;
        }
        //查找txt 文件
        lyricFile = new File(root, title + ".txt");
        if (lyricFile.exists()) {
            return lyricFile;
        }
        // TODO 服务器下载
        return null;
    }
}
```

在播放界面收到广播之后调用方法初始化歌词文件。

```java
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.io.File;

import static com.jackchan.vmplayer.R.id.tv_artist;

private class AudioBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        //准备完成
        //更新界面的按钮
        updatePlayBtn();
        //初始化歌曲名和歌手
        AudioItem audioItem = (AudioItem) intent.getSerializableExtra("audioItem");
        tv_name.setText(StringUtil.formatDisplayName(audioItem.getName()));
        tv_artist.setText(audioItem.getArtist());
        sk_position.setMax(binder.getDuration());
        //更新播放进度
        updateCurrentPosition();
        //初始化播放模式
        updatePlayModeBtn();
        // File file=new File(Environment.getExternalStorageDirectory(),"test/audio/"+
        StringUtil.formatDisplayName(audioItem.getName()) + ".lrc");
        File file = LyricLoader.loadLyricFile(StringUtil.formatDisplayName
                (audioItem.getName()));
        lyricView.setLyricFile(file);
        //开启歌词滚动更新
        startRoll();
    }
}
```

运行结果

![自定义歌词展示控件](https://alleniverson.gitbooks.io/customwidget/content/assets/1496390085095.png)

## 小结

本篇博客完成了音乐播放界面的歌词展示，自定义了展示歌词的控件，先在控件中间显示一行文字，然后又显示了集合中的所有文字。接着通过改变当前高亮显示的行数来使歌词移动起来。我们通过设置偏移量让歌词的移动看起来更平滑。最后从文件中将歌词解析出来。但是我们为了能够适应txt 和lrc 文件格式的歌词文件，自定义了一个歌词加载器。当文件中没有lrc 后缀的歌词的时候，就看看有没有txt 后缀的歌词，如果都没有的话需要从服务器下载

![](https://alleniverson.gitbooks.io/customwidget/content/assets/%E6%AD%8C%E8%AF%8D%E6%8E%A7%E4%BB%B6.png)