package com.fatgyft.smartvelov.path;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.ItemizedIconOverlay;

/**
 * Created by Felipe on 04-May-15.
 */
public class InstructionPoint {

    private Integer step;
    private Integer sign;
    private GeoPoint point;
    private ItemizedIconOverlay item;
    private Boolean isOnLOcationPoint;

    public InstructionPoint(Integer step, Integer sign, GeoPoint point) {
        this.step=step;
        this.sign = sign;
        this.point = point;
        this.isOnLOcationPoint=false;
    }

    public GeoPoint getPoint() {
        return point;
    }

    public Integer getSign() {
        return sign;
    }

    public ItemizedIconOverlay getItem() {
        return item;
    }

    public void setItem(ItemizedIconOverlay item) {
        this.item = item;
    }

    public Boolean isOnLOcationPoint() {
        return isOnLOcationPoint;
    }

    public void setIsOnLOcationPoint(Boolean isOnLOcationPoint) {
        this.isOnLOcationPoint = isOnLOcationPoint;
    }
}
