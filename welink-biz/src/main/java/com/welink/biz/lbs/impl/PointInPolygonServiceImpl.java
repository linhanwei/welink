package com.welink.biz.lbs.impl;

import com.google.common.collect.Lists;
import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.geom.impl.CoordinateArraySequence;
import com.welink.biz.lbs.PointInPolygonService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

/**
 * Created by saarixx on 5/1/15.
 */
@Service("pointInPolygonService")
class PointInPolygonServiceImpl implements PointInPolygonService {


    /**
     * 这里面都是经纬度，但是国外都是纬经度
     *
     * @param point
     * @param polygon points of linear ring do not form a closed linestring
     * @return
     */
    @Override
    public boolean isIn(String point, String polygon) {
        checkArgument(isNotBlank(polygon));
        checkArgument(isNotBlank(point));

        final GeometryFactory gf = new GeometryFactory();

        final List<Coordinate> points = Lists.newArrayList();

        String[] split = StringUtils.split(polygon, ',');

        checkArgument(split.length >= 6);
        checkArgument(split.length % 2 == 0);

        for (int i = 0; i < split.length / 2; i++) {
            double e = Double.parseDouble(split[2 * i]);
            double n = Double.parseDouble(split[2 * i + 1]);
            points.add(new Coordinate(n, e));
        }

        // points of linear ring do not form a closed linestring
        points.add(points.get(0));

        final Polygon gfPolygon = gf.createPolygon(new LinearRing(new CoordinateArraySequence(points.toArray(new Coordinate[points.size()])), gf), null);

        String[] lbs = StringUtils.split(point, ',');
        checkArgument(lbs.length == 2);
        double e = Double.parseDouble(lbs[0]);
        double n = Double.parseDouble(lbs[1]);
        final Coordinate coord = new Coordinate(n, e);
        final Point p = gf.createPoint(coord);

        return p.within(gfPolygon);
    }
}
