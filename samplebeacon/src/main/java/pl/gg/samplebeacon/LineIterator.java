package pl.gg.samplebeacon;

import android.graphics.Point;

import java.util.Iterator;

/**
 * Created by xxx on 10.12.2015.
 */
public class LineIterator implements Iterator<Point> {
    final static double DEFAULT_PRECISION = 0.1;
    final Line line;
    final double precision;

    final double sx, sy;
    final double dx, dy;

    double x, y, error;

    public LineIterator(Line line, double precision) {
        this.line = line;
        this.precision = precision;

        sx = line.p1.x < line.p2.x ? precision : -1 * precision;
        sy = line.p1.y < line.p2.y ? precision : -1 * precision;

        dx = Math.abs(line.p2.x - line.p1.x);
        dy = Math.abs(line.p2.y - line.p1.y);

        error = dx - dy;

        y = line.p1.y;
        x = line.p1.x;
    }

    public LineIterator(Line line) {
        this(line, DEFAULT_PRECISION);
    }

    @Override
    public boolean hasNext() {
        return Math.abs(x - line.p2.x) > 0.9 || (Math.abs(y - line.p2.y) > 0.9);
    }

    Point ret = new Point();

    @Override
    public Point next() {
        ret.set((int) x, (int) y);
        double e2 = 2 * error;
        if (e2 > -dy) {
            error -= dy;
            x += sx;
        }
        if (e2 < dx) {
            error += dx;
            y += sy;
        }

        return ret;
    }

    @Override
    public void remove() {
        throw new AssertionError();
    }
}