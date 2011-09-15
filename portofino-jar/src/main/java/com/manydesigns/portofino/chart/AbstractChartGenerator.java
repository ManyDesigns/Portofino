package com.manydesigns.portofino.chart;

public abstract class AbstractChartGenerator implements ChartGenerator {

    //**************************************************************************
    // Public properties
    //**************************************************************************
    public int width = 400;
    public int height = 300;
    public boolean antiAlias = true;
    public boolean borderVisible = true;

    public boolean isAntiAlias() {
        return antiAlias;
    }

    public void setAntiAlias(boolean antiAlias) {
        this.antiAlias = antiAlias;
    }

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

    public boolean isBorderVisible() {
        return borderVisible;
    }

    public void setBorderVisible(boolean borderVisible) {
        this.borderVisible = borderVisible;
    }

}