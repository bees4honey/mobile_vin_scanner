package com.bees4honey.vinscanner;


class ImageBuffer {
    public byte[] data;
    public int width;
    public int height;
    public int orientation;

    public static int ORIENTATION_UNKNOWN = 0;
    public static int ORIENTATION_LANDSCAPE = 1;
    public static int ORIENTATION_PORTRAIT = 2;

    public ImageBuffer(byte[] data, int width, int height, int orientation) {
        this.data = data;
        this.width = width;
        this.height = height;
        this.orientation = orientation;
    }

    public static int calcImgOrientation(int camOrientation) {
        switch (camOrientation) {
            case 90:
            case 270:
                return ORIENTATION_PORTRAIT;
            case 0:
            case 180:
                return ORIENTATION_LANDSCAPE;
            default:
                break;
        }

        return ORIENTATION_UNKNOWN;
    }
}
