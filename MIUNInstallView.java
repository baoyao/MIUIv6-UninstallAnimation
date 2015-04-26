package com.example.kot32.animationdemo;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.CycleInterpolator;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;

import com.kimo.lib.alexei.Alexei;
import com.kimo.lib.alexei.Answer;
import com.litesuits.android.async.SimpleTask;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by kot32 on 15/4/25.
 */
public class UninstallView extends View implements View.OnClickListener {
    private Paint paint;
    private Bitmap bitmap;
    private Context context;
    private java.util.List<Integer> colors;
    private boolean isShakeed = false;
    private List<Circle> circles = new ArrayList<>();
    private ScaleAnimation scaleAnimation;

    public UninstallView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public UninstallView(Context context) {
        super(context);
    }

    //测量
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(bitmap.getWidth() + 20, bitmap.getHeight() + 10);
    }

    public UninstallView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.MIUNInstallViewAttr);
        int rID = ta.getResourceId(R.styleable.MIUNInstallViewAttr_src, R.drawable.tab_xinxidongtai);
        ta.recycle();
        initView(rID);
    }

    void initView(int id) {
        bitmap = BitmapFactory.decodeResource(getResources(), id);
        paint = new Paint();
        paint.setAntiAlias(true);
        setOnClickListener(this);
        //开始分析颜色
        new AnalyseColor().execute();
    }


    // effective
    private class Circle {
        int x;
        int y;
        int size;
        int color;
        Point controlPoint;
        Point endPoint;

        Circle(int x, int y, int size, int color) {
            this.x = x;
            this.y = y;
            this.size = size;
            this.color = color;
            int bx = bitmap.getWidth();
            int by = bitmap.getHeight();
            controlPoint = new Point();
            endPoint = new Point();

            //随机下掉

            if (x < bitmap.getWidth() / 2) {
                controlPoint.set(new Random().nextInt(bx / 2), new Random().nextInt(by));
                endPoint.set(new Random().nextInt(bx / 2), by);
            } else {
                controlPoint.set(new Random().nextInt(bx / 2) + bx / 2, new Random().nextInt(by));
                endPoint.set(new Random().nextInt(bx / 2) + bx / 2, by);

            }
        }
    }


    @Override
    public void onClick(View v) {

        shake(v);
    }

    void shake(View v) {
        TranslateAnimation translateAnimation = new TranslateAnimation(0, 6, 0, 0);
        translateAnimation.setDuration(500);
        translateAnimation.setInterpolator(new CycleInterpolator(8));
        translateAnimation.setFillAfter(true);
        scaleAnimation = new ScaleAnimation(1,0,1,0,Animation.RELATIVE_TO_SELF,0.5f,Animation.RELATIVE_TO_SELF,0.5f);
        scaleAnimation.setDuration(100);
        scaleAnimation.setFillAfter(true);
        scaleAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                isShakeed = true;
                invalidate();
                new calWays().execute();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        translateAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                UninstallView.this.startAnimation(scaleAnimation);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        v.startAnimation(translateAnimation);
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
                paint.setAlpha(255);
                if (circle.y >= (bitmap.getHeight() / 5 * 4)) {
                    paint.setAlpha(0);
                }
                canvas.drawCircle(circle.x, circle.y, circle.size, paint);
            }

        }
    }

    class calWays extends SimpleTask<Void> {
        @Override
        protected Void doInBackground() {
            float t = 0.1f;
            while (true) {
                t += 0.015;
                //计算每个球的运动轨迹
                for (Circle circle : circles) {
                    final float d = 1 - t;
                    final float d2 = d * d;

                    if (circle.x <= 0 || circle.x >= bitmap.getWidth() || circle.y <= 0 || circle.y >= bitmap.getHeight())
                        continue;

                    circle.x = (int) (d2 * circle.x + 2 * d * t * circle.controlPoint.x + t * t * circle.endPoint.x);
                    circle.y = (int) (d2 * circle.y + 2 * d * t * circle.controlPoint.y + t * t * circle.endPoint.y);

                    postInvalidate();

                }

                try {
                    Thread.sleep(30);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        }
    }

    class AnalyseColor extends SimpleTask<Void> {


        @Override
        protected Void doInBackground() {
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
                                //生成的小球大多数在正中间
                                int randomColor = colors.get(new Random().nextInt(colors.size()));
                                int randomX = bitmap.getWidth() / 2 + (new Random().nextInt(bitmap.getWidth() / 4)) - bitmap.getWidth() / 5;
                                int randomY = bitmap.getHeight() / 2 + (new Random().nextInt(bitmap.getHeight() / 5)) - bitmap.getHeight() / 5;
                                int randomSize = new Random().nextInt(20) + 3;
                                Circle circle = new Circle(randomX, randomY, randomSize, randomColor);
                                circles.add(circle);
                            }
                        }

                        @Override
                        public void ifFails(Exception e) {
                            System.out.println(e);
                        }
                    });
            return null;
        }
    }

}
