package com.martin.pictionary2.messages;

/**
 * Created by desireelenart on 3/5/18.
 */

import android.os.Parcel;
import android.view.MotionEvent;

/**
 * Message that the artist has drawn a line and the receiver should mirror it in their own
 * PaintView.
 */


public class DrawingMessage extends Message {
    private int color;
    private byte[] motionEventData;

    public DrawingMessage() {
    }

    public DrawingMessage(byte[] motionEventData, int color) {
        this.motionEventData = motionEventData;
        this.color = color;
    }

    public byte[] getMotionEventData() {
        return motionEventData;
    }

    public void setMotionEventData(byte[] motionEventData) {
        this.motionEventData = motionEventData;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }
}
