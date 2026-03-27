import edu.princeton.cs.algs4.Picture;

import java.awt.Color;

public class SeamCarver {

    private int width;
    private int height;
    private Picture picture;
    private double[][] energy;

    private void assignEnergy() {
        energy = new double[height][width];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (y == 0 || x == 0 || y == height - 1 || x == width - 1) {
                    energy[y][x] = 1000.0;
                    continue;
                }
                Color left = picture.get(x - 1, y);
                Color right = picture.get(x + 1, y);
                Color above = picture.get(x, y - 1);
                Color below = picture.get(x, y + 1);

                double red_dx = Math.pow(left.getRed() - right.getRed(), 2);
                double blue_dx = Math.pow(left.getBlue() - right.getBlue(), 2);
                double green_dx = Math.pow(left.getGreen() - right.getGreen(), 2);

                double red_dy = Math.pow(above.getRed() - below.getRed(), 2);
                double blue_dy = Math.pow(above.getBlue() - below.getBlue(), 2);
                double green_dy = Math.pow(above.getGreen() - below.getGreen(), 2);

                double dx = red_dx + blue_dx + green_dx;
                double dy = red_dy + blue_dy + green_dy;
                energy[y][x] = Math.sqrt(dx + dy);
            }
        }
    }

    private boolean valid(int x, int y) {
        return x >= 0 && x < width && y >= 0 && y < height;
    }

    private void validateSeam(int[] seam, boolean isVertical) {
        if (seam == null) {
            throw new IllegalArgumentException();
        }

        if (isVertical) {
            if (seam.length != height) {
                throw new IllegalArgumentException();
            }
            if (width <= 1) {
                throw new IllegalArgumentException();
            }
        } else {
            if (seam.length != width) {
                throw new IllegalArgumentException();
            }
            if (height <= 1) {
                throw new IllegalArgumentException();
            }
        }

        for (int i = 0; i < seam.length; i++) {
            int val = seam[i];

            // check range
            if (isVertical) {
                if (val < 0 || val >= width) {
                    throw new IllegalArgumentException();
                }
            } else {
                if (val < 0 || val >= height) {
                    throw new IllegalArgumentException();
                }
            }

            // check adjacency
            if (i > 0 && Math.abs(seam[i] - seam[i - 1]) > 1) {
                throw new IllegalArgumentException();
            }
        }
    }

    public SeamCarver(Picture picture) {
        if (picture == null) {
            throw new IllegalArgumentException();
        }
        width = picture.width();
        height = picture.height();
        this.picture = new Picture(picture);
        assignEnergy();
    }

    public Picture picture() {
        return new Picture(picture);
    }

    public int width() {
        return width;
    }

    public int height() {
        return height;
    }

    public double energy(int x, int y) {
        if (!valid(x, y)) {
            throw new IllegalArgumentException();
        }
        return energy[y][x];
    }

    public int[] findHorizontalSeam() {
        int[][] prev = new int[height][width];
        double[][] dist = new double[height][width];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (x == 0) {
                    prev[y][x] = -1;
                    dist[y][x] = energy[y][x];
                } else {
                    double score = Double.MAX_VALUE;
                    int p = -1;
                    for (int py = y - 1; py <= y + 1; py++) {
                        if (valid(x, py) && score > energy[y][x] + dist[py][x - 1]) {
                            score = energy[y][x] + dist[py][x - 1];
                            p = py;
                        }
                    }
                    prev[y][x] = p;
                    dist[y][x] = score;
                }
            }
        }
        int[] seam = new int[width];
        int startY = -1;
        double final_score = Double.MAX_VALUE;
        for (int y = 0; y < height; y++) {
            if (final_score > dist[y][width - 1]) {
                final_score = dist[y][width - 1];
                startY = y;
            }
        }
        int y = startY;
        for (int x = width - 1; x >= 0; x--) {
            if (y == -1) {
                break;
            }
            seam[x] = y;
            y = prev[y][x];
        }
        return seam;
    }

    public int[] findVerticalSeam() {
        int[][] prev = new int[height][width];
        double[][] dist = new double[height][width];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (y == 0) {
                    prev[y][x] = -1;
                    dist[y][x] = energy[y][x];
                } else {
                    double score = Double.MAX_VALUE;
                    int p = -1;
                    for (int px = x - 1; px <= x + 1; px++) {
                        if (valid(px, y) && score > energy[y][x] + dist[y - 1][px]) {
                            score = energy[y][x] + dist[y - 1][px];
                            p = px;
                        }
                    }
                    prev[y][x] = p;
                    dist[y][x] = score;
                }
            }
        }
        int[] seam = new int[height];
        int startX = -1;
        double final_score = Double.MAX_VALUE;
        for (int x = 0; x < width; x++) {
            if (final_score > dist[height - 1][x]) {
                final_score = dist[height - 1][x];
                startX = x;
            }
        }
        int x = startX;
        for (int y = height - 1; y >= 0; y--) {
            if (x == -1) {
                break;
            }
            seam[y] = x;
            x = prev[y][x];
        }
        return seam;
    }

    public void removeHorizontalSeam(int[] seam) {
        validateSeam(seam, false);
        Picture new_picture = new Picture(width, height - 1);
        for (int x = 0; x < width; x++) {
            boolean found = false;
            for (int y = 0; y < height; y++) {
                if (y == seam[x]) {
                    found = true;
                    continue;
                }
                new_picture.set(x, y - (found ? 1 : 0), picture.get(x, y));
            }
        }
        this.picture = new_picture;
        height--;
        assignEnergy();
    }

    public void removeVerticalSeam(int[] seam) {
        validateSeam(seam, true);
        Picture new_picture = new Picture(width - 1, height);
        for (int y = 0; y < height; y++) {
            boolean found = false;
            for (int x = 0; x < width; x++) {
                if (x == seam[y]) {
                    found = true;
                    continue;
                }
                new_picture.set(x - (found ? 1 : 0), y, picture.get(x, y));
            }
        }
        this.picture = new_picture;
        width--;
        assignEnergy();
    }

    public static void main(String[] args) {
        // optional unit testing
    }

}