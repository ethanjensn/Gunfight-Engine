package com.gunfight.data;

public class StaticMapComponent {
    public static final int COLS = 14;
    public static final int ROWS = 10;
    public static final float TILE_W = 57.142857f;  // 800/14
    public static final float TILE_H = 60f;         // 600/10
    
    public final boolean[][] solid = new boolean[COLS][ROWS];
    public float p1SpawnX, p1SpawnY;
    public float p2SpawnX, p2SpawnY;
    
    public StaticMapComponent(String[] asciiMap) {
        if (asciiMap.length != ROWS) {
            throw new IllegalArgumentException("Map must have " + ROWS + " rows");
        }
        for (int row = 0; row < ROWS; row++) {
            String line = asciiMap[row];
            if (line.length() != COLS) {
                throw new IllegalArgumentException("Row " + row + " must have " + COLS + " columns");
            }
            for (int col = 0; col < COLS; col++) {
                char c = line.charAt(col);
                solid[col][row] = (c == '#');
                if (c == '1') {
                    p1SpawnX = col * TILE_W + TILE_W / 2f;
                    p1SpawnY = row * TILE_H + TILE_H / 2f;
                } else if (c == '2') {
                    p2SpawnX = col * TILE_W + TILE_W / 2f;
                    p2SpawnY = row * TILE_H + TILE_H / 2f;
                }
            }
        }
    }
    
    public boolean isWall(int tx, int ty) {
        if (tx < 0 || tx >= COLS || ty < 0 || ty >= ROWS) return false;
        return solid[tx][ty];
    }
    
    public void getTileBounds(int tx, int ty, float[] out) {
        out[0] = tx * TILE_W;      // x
        out[1] = ty * TILE_H;      // y
        out[2] = TILE_W;           // width
        out[3] = TILE_H;           // height
    }
    
    public int getSolidTileCount() {
        int count = 0;
        for (int x = 0; x < COLS; x++) {
            for (int y = 0; y < ROWS; y++) {
                if (solid[x][y]) count++;
            }
        }
        return count;
    }
}
