package com.company.alex.privategallery.utils;

import java.io.Serializable;

/**
 * Created by Alex on 08/05/2017.
 */

public class Parameters implements Serializable {
    private int pickerLimit;
    private int captureLimit;
    private int toolbarColor;
    private int lightColor;
    private int darkColor;
    private int actionButtonColor;
    private int buttonTextColor;
    private int columnCount;
    private int thumbnailWidthInDp;

    public Parameters() {
    }

    public int getPickerLimit() {
        return this.pickerLimit;
    }

    public void setPickerLimit(int pickerLimit) {
        this.pickerLimit = pickerLimit;
    }

    public int getCaptureLimit() {
        return this.captureLimit;
    }

    public void setCaptureLimit(int captureLimit) {
        this.captureLimit = captureLimit;
    }

    public int getToolbarColor() {
        return this.toolbarColor;
    }

    public void setToolbarColor(int toolbarColor) {
        this.toolbarColor = toolbarColor;
    }

    public int getActionButtonColor() {
        return this.actionButtonColor;
    }

    public void setActionButtonColor(int actionButtonColor) {
        this.actionButtonColor = actionButtonColor;
    }

    public int getButtonTextColor() {
        return this.buttonTextColor;
    }

    public void setButtonTextColor(int buttonTextColor) {
        this.buttonTextColor = buttonTextColor;
    }

    public int getColumnCount() {
        return this.columnCount;
    }

    public void setColumnCount(int columnCount) {
        this.columnCount = columnCount;
    }

    public int getThumbnailWidthInDp() {
        return this.thumbnailWidthInDp;
    }

    public void setThumbnailWidthInDp(int thumbnailWidthInDp) {
        this.thumbnailWidthInDp = thumbnailWidthInDp;
    }

    public int getLightColor() {
        if(this.lightColor != 0) {
            return this.lightColor;
        } else {
            if(this.toolbarColor != 0) {
                this.lightColor = Utils.getLightColor(this.toolbarColor);
            }

            return this.lightColor;
        }
    }
}
