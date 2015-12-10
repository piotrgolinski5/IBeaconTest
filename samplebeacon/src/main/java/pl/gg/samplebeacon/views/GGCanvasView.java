package pl.gg.samplebeacon.views;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by xxx on 24.11.2015.
 */
public class GGCanvasView extends View {
    public interface GGCanvasViewListener {
        void onDraw(Canvas canvas);
    }

    private Activity mActivity;
    private GGCanvasViewListener mGGCanvasViewListener;
    private Thread mDrawingThread;
    private Paint mPaint;
    private Canvas mCanvas;
    private int mBackgroundColor = -1;
    private boolean mIsDrawing = false;
boolean canInvalidate = true;
    public GGCanvasView(Context c, AttributeSet attrs) {
        super(c, attrs);
        mActivity = (Activity) c;
        mPaint = new Paint();
        mPaint.setColor(Color.BLACK);
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeWidth(1f);
        mPaint.setDither(true);

        mDrawingThread = new Thread(new Runnable() {
            @Override
            public void run() {
                //TODO ograniczyc klatki
                while (mIsDrawing) {
                    try {
                        mActivity.runOnUiThread(mInvalidate);
                        Thread.sleep(1000 / 30);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }


            }
        });
    }

    public void onStart() {
        mIsDrawing = true;
        mDrawingThread.start();
    }

    public void onStop() {
        mIsDrawing = false;
        try {
            mDrawingThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canInvalidate = false;
        mCanvas = canvas;
        if(mBackgroundColor != -1){
            canvas.drawColor(mBackgroundColor);
        }
        mPaint.setColor(Color.BLACK);

        if (mGGCanvasViewListener != null) {
             mGGCanvasViewListener.onDraw(canvas);
        }
        canInvalidate = true;
    }

    public void setGGCanvasViewListener(GGCanvasViewListener listener) {
        mGGCanvasViewListener = listener;
    }

    public void drawCircle(int x, int y, int radius) {
        mCanvas.drawCircle(x, y, radius, mPaint);
    }

    public void drawRect(Rect rect) {
        mCanvas.drawRect(rect.left,rect.top,rect.right,rect.bottom, mPaint);
    }

    public void drawLine(int x1, int y1,int x2, int y2) {
        mCanvas.drawLine(x1, y1, x2, y2, mPaint);
    }

    public void drawText( String text, int x, int y) {
        mCanvas.drawText(text, x, y, mPaint);
    }

    private Runnable mInvalidate = new Runnable() {
        @Override
        public void run() {
            if(canInvalidate) {
                invalidate();
            }
        }
    };

    public void setBackgroundColor(int color){
        mBackgroundColor = color;
    }

    public Paint getPaint() {
        return mPaint;
    }
    public void setPaintColor(int color) {
        mPaint.setColor(color);
    }

    public void setPaintTextSize(int size) {
        mPaint.setTextSize(size);
    }
}
