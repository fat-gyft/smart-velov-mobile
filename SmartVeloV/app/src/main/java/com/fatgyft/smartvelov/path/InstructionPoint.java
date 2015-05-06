package com.fatgyft.smartvelov.path;

import android.graphics.drawable.Drawable;

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
    private Boolean isOnLocationPoint;
    private Drawable image;
    private String signText;

    public InstructionPoint(Integer step, Integer sign, GeoPoint point) {
        this.step=step;
        this.sign = sign;
        this.point = point;
        this.isOnLocationPoint =false;
    }

    public Integer getStep() {
        return step;
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

    public Boolean isOnLocationPoint() {
        return isOnLocationPoint;
    }

    public void setIsOnLocationPoint(Boolean isOnLocationPoint) {
        this.isOnLocationPoint = isOnLocationPoint;
    }

    public Drawable getImage() {
        return image;
    }

    public void setImage(Drawable image) {
        this.image = image;
    }

    public String getSignText() {
        return signText;
    }

    public void setSignText(String signText) {
        this.signText = signText;
    }
}
