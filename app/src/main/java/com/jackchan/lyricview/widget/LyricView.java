package com.jackchan.lyricview.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;

import com.jackchan.lyricview.R;
import com.jackchan.lyricview.bean.LyricBean;
import com.jackchan.lyricview.utils.LyricParser;

import java.io.File;
import java.util.List;

public class LyricView extends AppCompatTextView {
    private int             nomalColor;
    private int             hightColor;
    private float           nomalSize;
    private float           hightSize;
    private Paint           paint;
    private int             halfViewWidth;
    private int             halfViewHight;
    private List<LyricBean> lyricBeans;
    private int             currentLine;
    private float           lyricHeight;

    public LyricView(Context context) {
        super(context);
        init();
    }

    public LyricView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public LyricView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        nomalColor = Color.WHITE;
        hightColor = getResources().getColor(R.color.hightLightColor);

        nomalSize = getResources().getDimension(R.dimen.nomal_size);
        hightSize = getResources().getDimension(R.dimen.hight_size);

        lyricHeight = getResources().getDimensionPixelOffset(R.dimen.lyric_hieght);//行高
        paint = new Paint();
        paint.setAntiAlias(true);//抗锯齿

        paint.setColor(hightColor);
        paint.setTextSize(hightSize);
    }

    /**
     * 当歌词控件宽高发生变化是回调
     *
     * @author JackChan
     * create at 2017/6/2
     */
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        halfViewWidth = w / 2;
        halfViewHight = h / 2;

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (lyricBeans == null) {
            drawSingleLine(canvas);
        } else {
            drawMulitLine(canvas);
        }
    }

    /**
     * 绘制多行居中的文本
     *
     * @param canvas
     */
    private void drawMulitLine(Canvas canvas) {
        // 绘制的Y位置=centerY+(当前行数-高亮行)*行高
        LyricBean lyricBean = lyricBeans.get(currentLine);
        Rect bounds = new Rect();
        paint.getTextBounds(lyricBean.content, 0, lyricBean.content.length(), bounds);

        // 偏移量=经过的时间百分比*行高
        // 经过的时间百分比=经过的时间 / 行所用时间
        // 经过的时间=播放器的时间-当前行的开始时间
        // 行所用时间=下一行开始时间-当前行开始时间
        int endTime = 0;
        if (currentLine == lyricBeans.size() - 1) {
            endTime = mDuration;
        } else {
            LyricBean lyricBeanNext = lyricBeans.get(currentLine + 1);
            endTime = lyricBeanNext.startPoint;
        }
        //行所用时间
        int userTime = endTime - lyricBean.startPoint;
        //经过的时间
        int pastTime = mCurrentDuration - lyricBean.startPoint;
        float percentTime = pastTime / (float) userTime;

        int offsetY = (int) (percentTime * lyricHeight);

        int halfTextHeight = bounds.height() / 2;
        int centerY = halfViewHight + halfTextHeight - offsetY;

        for (int i = 0; i < lyricBeans.size(); i++) {
            if (i == currentLine) {
                paint.setColor(hightColor);
                paint.setTextSize(hightSize);
            } else {
                paint.setColor(nomalColor);
                paint.setTextSize(nomalSize);
            }

            // 绘制的Y位置=centerY+(当前行数-高亮行)*行高
            int drawY = (int) (centerY + (i - currentLine) * lyricHeight);
            drawHorizontal(canvas, lyricBeans.get(i).content, drawY);
        }


    }

    private void drawSingleLine(Canvas canvas) {
        String text = "正在加载歌词...";
        Rect bounds = new Rect();
        paint.getTextBounds(text, 0, text.length(), bounds);

        int halfTextHeight = bounds.height() / 2;
        int drawY = halfViewHight + halfTextHeight;

        drawHorizontal(canvas, text, drawY);
    }

    private void drawHorizontal(Canvas canvas, String text, int drawY) {
        Rect bounds = new Rect();
        paint.getTextBounds(text, 0, text.length(), bounds);

        int halfTextWidth = bounds.width() / 2;
        int drawX = halfViewWidth - halfTextWidth;
        canvas.drawText(text, drawX, drawY, paint);
    }

    private int mCurrentDuration;
    private int mDuration;

    /**
     * 滚动歌词
     *
     * @param currenDuration 当前播放时间
     * @param duration       歌曲总时长
     */
    public void roll(int currenDuration, int duration) {
        // 获取高亮行：当前歌词的开始时间 < 播放器的时间
        // 下一行歌词的开始时间>= 播放器的时间
        this.mCurrentDuration = currenDuration;
        this.mDuration = duration;

        for (int i = 0; i < lyricBeans.size(); i++) {

            LyricBean lyricBean = lyricBeans.get(i);
            int endTime = 0;
            if (i == lyricBeans.size() - 1) {
                endTime = duration;
            } else {
                LyricBean lyricBeanNext = lyricBeans.get(i + 1);
                endTime = lyricBeanNext.startPoint;
            }
            if (currenDuration > lyricBean.startPoint && currenDuration <= endTime) {
                currentLine = i;
                break;
            }
        }
        invalidate();
    }

   /**
    * 解析歌词文件
    *
    * @author JackChan
    * create at 2017/6/2
    */
    public void parserLyric(File file) {
        lyricBeans = LyricParser.parserLyricFromFile(file);
        currentLine = 0;
    }

}