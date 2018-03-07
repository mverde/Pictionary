package com.martin.pictionary2.messages;

/**
 * Created by desireelenart on 3/5/18.
 */

import android.graphics.Bitmap;
import android.view.MotionEvent;

import com.martin.pictionary2.drawing.FingerPath;
import com.martin.pictionary2.drawing.PaintView;
import com.martin.pictionary2.DrawingCoordinate;

import java.lang.reflect.Array;
import java.util.ArrayList;

/**
 * Message that the artist has drawn a point and the recipient
 * should mirror it on their own DrawView instance.
 * DrawingCoordinate coordinate - the location of the drawn point.
 * int color - the index of the drawn color in the array of colors.
 */


public class DrawingMessage extends Message {
    private MotionEvent motionEvent;

    public DrawingMessage() {
    }

    public DrawingMessage(MotionEvent motionEvent) {
        this.motionEvent = motionEvent;
    }

    public MotionEvent getMotionEvent() {
        return motionEvent;
    }

    public void setMotionEvent() {
        this.motionEvent = motionEvent;
    }
}
