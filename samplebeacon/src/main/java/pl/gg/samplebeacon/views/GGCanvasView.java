package pl.gg.samplebeacon.views;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
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
    private boolean mIsDrawing = false;

    public GGCanvasView(Context c, AttributeSet attrs) {
        super(c, attrs);
        mActivity = (Activity) c;
        mPaint = new Paint();
        mPaint.setColor(Color.BLACK);
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.STROKE);
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
                        Thread.sleep(1000 / 2);
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
        mPaint.setColor(Color.BLACK);
        //canvas.drawColor(Color.GREEN);
        if (mGGCanvasViewListener != null) {
             mGGCanvasViewListener.onDraw(canvas);
        }
        mPaint.setColor(Color.YELLOW);
        canvas.drawCircle(0, 0, 100, mPaint);

    }

    public void setGGCanvasViewListener(GGCanvasViewListener listener) {
        mGGCanvasViewListener = listener;
    }

    public void drawCircle(int x, int y, int radius, Canvas canvas) {
        canvas.drawCircle(x, y, radius, mPaint);
    }

    private Runnable mInvalidate = new Runnable() {
        @Override
        public void run() {
            invalidate();
        }
    };

    public Paint getPaint() {
        return mPaint;
    }
}
