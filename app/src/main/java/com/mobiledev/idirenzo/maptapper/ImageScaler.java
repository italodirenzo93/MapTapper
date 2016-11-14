package com.mobiledev.idirenzo.maptapper;

import android.graphics.Matrix;
import android.graphics.PointF;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

/**
 * Created by Italo on 2016-11-14.
 */

public class ImageScaler implements View.OnTouchListener {

    private Matrix matrix = new Matrix();
    private Matrix savedMatrix = new Matrix();

    // Keep track of the action
    private enum ImageState { NONE, DRAG, ZOOM }
    private ImageState state = ImageState.NONE;

    // Scaling stuff
    private PointF start = new PointF();
    private PointF mid = new PointF();
    private float oldDist = 1f;

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        ImageView image = (ImageView)view;
        switch (motionEvent.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                savedMatrix.set(matrix);
                start.set(motionEvent.getX(), motionEvent.getY());
                state = ImageState.DRAG;
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                oldDist = spacing(motionEvent);
                if (oldDist > 10f) {
                    savedMatrix.set(matrix);
                    midPoint(mid, motionEvent);
                    state = ImageState.ZOOM;
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
                state = ImageState.NONE;
                break;
            case MotionEvent.ACTION_MOVE:
                if (state == ImageState.DRAG) {
                    matrix.set(savedMatrix);
                    matrix.postTranslate(motionEvent.getX() - start.x, motionEvent.getY() - start.y);
                }
                else if (state == ImageState.ZOOM) {
                    float newDist = spacing(motionEvent);
                    if (newDist > 10f) {
                        matrix.set(savedMatrix);
                        float scale = newDist / oldDist;
                        matrix.postScale(scale, scale, mid.x, mid.y);
                    }
                }
                break;
        }
        image.setImageMatrix(matrix);
        return true;
    }

    /** Determine the space between the first two fingers */
    private static float spacing(MotionEvent event) {
        // ...
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float)Math.sqrt(x * x + y * y);
    }

    /** Calculate the mid point of the first two fingers */
    private static void midPoint(PointF point, MotionEvent event) {
        // 2 finger midpoint
        float x = event.getX(0) + event.getX(1);
        float y = event.getY(0) + event.getY(1);
        point.set(x / 2, y / 2);
    }
}
