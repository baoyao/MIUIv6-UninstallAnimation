package com.example.kot32.animationdemo;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.CycleInterpolator;
import android.view.animation.TranslateAnimation;

import com.kimo.lib.alexei.Alexei;
import com.kimo.lib.alexei.Answer;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by kot32 on 15/3/21.
 */
public class MIUNInstallView extends View implements View.OnClickListener {
    private Paint paint;
    private Bitmap bitmap;
    private Context context;
    private java.util.List<Integer> colors;
    private boolean isShakeed = false;
    private List<Circle> circles = new ArrayList<>();

    public MIUNInstallView(Context context) {
        super(context);
    }

    public MIUNInstallView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.MIUNInstallViewAttr);
        int rID = ta.getResourceId(R.styleable.MIUNInstallViewAttr_src,R.drawable.tab_xinxidongtai);
        ta.recycle();
        initView(rID);
    }

    public MIUNInstallView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    //初始化
    void initView(int id) {
        bitmap = BitmapFactory.decodeResource(getResources(), id);
        paint = new Paint();
        paint.setAntiAlias(true);
        setOnClickListener(this);
        new Thread(new AnalyseColor()).start();

    }

    //测量
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(bitmap.getWidth() + 20, bitmap.getHeight() + 10);
    }

    //绘制
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //绘制图片
        if (!isShakeed) {
            canvas.drawBitmap(bitmap, 5, 5, paint);
        } else {
            for (Circle circle : circles) {
                paint.setColor(circle.color);
                paint.setAlpha(new Random().nextInt(105)+150);
                canvas.drawCircle(circle.x, circle.y, circle.size, paint);
            }

        }
    }


    @Override
    public void onClick(View v) {

        shake(v);
    }

    void shake(View v) {

        TranslateAnimation translateAnimation = new TranslateAnimation(0, 8, 0, 0);
        translateAnimation.setDuration(800);
        translateAnimation.setInterpolator(new CycleInterpolator(8));
        translateAnimation.setFillAfter(true);
        translateAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                isShakeed = true;
                invalidate();
                new Thread(new calWayThread()).start();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        v.startAnimation(translateAnimation);
    }

    class AnalyseColor implements Runnable {
        @Override
        public void run() {
            Alexei.with(context)
                    .analize(bitmap)
                    .perform(new com.kimo.lib.alexei.calculus.ColorPaletteCalculus(bitmap, 10))
                    .showMe(new Answer() {
                        @Override
                        public void beforeExecution() {

                        }

                        @Override
                        public void afterExecution(Object o, long l) {
                            colors = (java.util.List<Integer>) o;
                            for (int i = 0; i < 100; i++) {
                                int randomColor = colors.get(new Random().nextInt(colors.size()));
                                int randomX = new Random().nextInt(bitmap.getWidth()/2)+bitmap.getWidth()/4;
                                int randomY = new Random().nextInt(bitmap.getHeight()/2)+bitmap.getHeight()/4;
                                int randomSize = new Random().nextInt(20) + 10;
                                Circle circle = new Circle(randomX, randomY, randomSize, randomColor);
                                circles.add(circle);
                            }
                        }

                        @Override
                        public void ifFails(Exception e) {
                            System.out.println(e);
                        }
                    });
        }
    }

    // effective
    private static class Circle {
        int x;
        int y;
        int size;
        int color;

        Circle(int x, int y, int size, int color) {
            this.x = x;
            this.y = y;
            this.size = size;
            this.color = color;
        }
    }


    //计算轨迹的线程
    class calWayThread implements Runnable {
        int t = 0;

        @Override
        public void run() {
            isShakeed = true;
            while (true) {
                t += 1;
                //计算每个球的运动轨迹
                for (Circle circle : circles) {
                    int y = circle.y;
                    //if the ball is on the middle of the View,it should be have faster velocity and smaller sitaValue
                    int v = new Random().nextInt(40)+2;
                    int sita =45;
                    float g = 4f;

                    if (circle.x > bitmap.getWidth() / 2) {
                        //右边的
                        circle.x += v * Math.cos(sita) * t;
                        circle.y = -(int) (4 * Math.sin(sita) * t - g * t * t / 2) + y;
                    } else {
                        //左边的
                        circle.x += -v * Math.cos(sita) * t;
                        circle.y = -(int) (4 * Math.sin(sita) * t - g * t * t / 2) + y;
                    }


                }
                postInvalidate();
                try {
                    Thread.sleep(40);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        }
    }

}
