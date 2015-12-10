package pl.gg.samplebeacon;

import android.graphics.Point;

/**
 * Created by xxx on 10.12.2015.
 */
public class Line {

    Point p1;
    Point p2;

    public Line() {
    }

    public Line(Point p1, Point p2) {
        this.p1 = p1;
        this.p2 = p2;
    }

    public void set(Point p1, Point p2) {
        this.p1 = p1;
        this.p2 = p2;
    }
}
