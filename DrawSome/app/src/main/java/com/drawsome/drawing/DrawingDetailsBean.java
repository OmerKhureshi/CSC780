package com.drawsome.drawing;

import android.graphics.Paint;
import android.graphics.Path;
import android.os.Parcel;
import android.os.Parcelable;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by pooja on 10/10/2015.
 */
public class DrawingDetailsBean implements Parcelable {

    private int paint;
    private float strokewidth;
    private List<Point> pointList;

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    private int width;
    private int height;
    public boolean isEraserFlag() {
        return eraserFlag;
    }

    public void setEraserFlag(boolean eraserFlag) {
        this.eraserFlag = eraserFlag;
    }

    private boolean eraserFlag;
    public int getPaint() {
        return paint;
    }

    public void setPaint(int paint) {
        this.paint = paint;
    }

    public float getStrokewidth() {
        return strokewidth;
    }

    public void setStrokewidth(float strokewidth) {
        this.strokewidth = strokewidth;
    }

    public List<Point> getPointList() {
        return pointList;
    }

    public void setPointList(List<Point> pointList) {
        this.pointList = pointList;
    }


   /* public byte[] serialize() throws IOException {
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        ObjectOutputStream o = new ObjectOutputStream(b);
        o.writeObject(this);
        return b.toByteArray();
    }

    public static DrawingDetailsBean deserialize(byte[] bytes) throws IOException, ClassNotFoundException {
        ByteArrayInputStream b = new ByteArrayInputStream(bytes);
        ObjectInputStream o = new ObjectInputStream(b);
        return (DrawingDetailsBean) o.readObject();

    }*/


    public DrawingDetailsBean() {

    }


    protected DrawingDetailsBean(Parcel in) {
        paint = in.readInt();
        strokewidth = in.readFloat();
        if (in.readByte() == 0x01) {
            pointList = new ArrayList<Point>();
            in.readList(pointList, Point.class.getClassLoader());
        } else {
            pointList = null;
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(paint);
        dest.writeFloat(strokewidth);
        if (pointList == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeList(pointList);
        }
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<DrawingDetailsBean> CREATOR = new Parcelable.Creator<DrawingDetailsBean>() {
        @Override
        public DrawingDetailsBean createFromParcel(Parcel in) {
            return new DrawingDetailsBean(in);
        }

        @Override
        public DrawingDetailsBean[] newArray(int size) {
            return new DrawingDetailsBean[size];
        }
    };
}