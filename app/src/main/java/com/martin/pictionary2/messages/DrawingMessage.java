package com.martin.pictionary2.messages;

/**
 * Created by desireelenart on 3/5/18.
 */

import com.martin.pictionary2.DrawingCoordinate;

/**
 * Message that the artist has drawn a point and the recipient
 * should mirror it on their own DrawView instance.
 * DrawingCoordinate coordinate - the location of the drawn point.
 * int color - the index of the drawn color in the array of colors.
 */


public class DrawingMessage extends Message {
    private DrawingCoordinate coordinate;
    private int color;

    public DrawingMessage() {
    }

    public DrawingMessage(DrawingCoordinate coordinate, int color) {
        this.coordinate = coordinate;
        this.color = color;
    }

    public DrawingCoordinate getPoint() {
        return coordinate;
    }

    public void setPoint(DrawingCoordinate point) {
        this.coordinate = point;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }
}
