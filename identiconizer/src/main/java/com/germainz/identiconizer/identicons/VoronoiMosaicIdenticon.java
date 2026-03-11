package com.germainz.identiconizer.identicons;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.text.TextUtils;

/**
 * Crystalline Voronoi with restrained palette:
 * - mix of filled mosaic regions
 * - some outlined (empty) regions
 * - hash-driven layout modes to avoid samey output
 */
public class VoronoiMosaicIdenticon extends Identicon {

    private static final int SEED_COUNT = 10;

    @Override
    public Bitmap generateIdenticonBitmap(byte[] hash) {
        if (hash == null || hash.length < 16) return null;

        final int size = SIZE;
        final Bitmap bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        final Canvas canvas = new Canvas(bmp);
        canvas.drawColor(BG_COLOR);

        float[] seedX = new float[SEED_COUNT];
        float[] seedY = new float[SEED_COUNT];
        int layoutMode = (hash[0] & 0xFF) % 4;
        buildSeeds(hash, size, seedX, seedY, layoutMode);

        int c1 = Color.rgb(hash[15] & 0xFF, hash[14] & 0xFF, hash[13] & 0xFF);
        int c2 = Color.rgb(hash[12] & 0xFF, hash[11] & 0xFF, hash[10] & 0xFF);
        int c3 = Color.rgb(hash[9] & 0xFF, hash[8] & 0xFF, hash[7] & 0xFF);
        c1 = ensureContrast(c1);
        c2 = ensureContrast(c2);
        c3 = ensureContrast(c3);

        int[] palette = new int[]{c1, c2, c3};
        int lineColor = ensureContrast(Color.rgb(hash[6] & 0xFF, hash[5] & 0xFF, hash[4] & 0xFF));

        Paint fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        fillPaint.setStyle(Paint.Style.FILL);

        Paint strokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        strokePaint.setStyle(Paint.Style.STROKE);
        strokePaint.setStrokeWidth(Math.max(1f, size / 220f));
        strokePaint.setColor(lineColor);

        final int step = Math.max(1, size / 220);
        final int h = (size + step - 1) / step;
        final int w = (size + step - 1) / step;
        final int[][] nearestMap = new int[h][w];

        int yi = 0;
        for (int y = 0; y < size; y += step, yi++) {
            int xi = 0;
            for (int x = 0; x < size; x += step, xi++) {
                nearestMap[yi][xi] = nearestSeed(x, y, seedX, seedY);
            }
        }

        int[] seedMode = new int[SEED_COUNT];
        for (int i = 0; i < SEED_COUNT; i++) {
            int nibble = getNibble(hash, i + layoutMode * 3);
            seedMode[i] = nibble % 4; // 0 = empty/outlined only; 1..3 filled
        }

        yi = 0;
        for (int y = 0; y < size; y += step, yi++) {
            int xi = 0;
            for (int x = 0; x < size; x += step, xi++) {
                int seed = nearestMap[yi][xi];
                int mode = seedMode[seed];

                if (mode != 0) {
                    fillPaint.setColor(palette[(mode - 1) % palette.length]);
                    canvas.drawRect(x, y, Math.min(size, x + step), Math.min(size, y + step), fillPaint);
                }

                boolean edge = false;
                int current = seed;
                if (xi > 0 && nearestMap[yi][xi - 1] != current) edge = true;
                else if (yi > 0 && nearestMap[yi - 1][xi] != current) edge = true;
                else if (xi + 1 < w && nearestMap[yi][xi + 1] != current) edge = true;
                else if (yi + 1 < h && nearestMap[yi + 1][xi] != current) edge = true;

                if (edge) {
                    canvas.drawRect(x, y, Math.min(size, x + step), Math.min(size, y + step), strokePaint);
                }
            }
        }

        return bmp;
    }

    @Override
    public byte[] generateIdenticonByteArray(byte[] hash) {
        return bitmapToByteArray(generateIdenticonBitmap(hash));
    }

    @Override
    public Bitmap generateIdenticonBitmap(String key) {
        if (TextUtils.isEmpty(key)) return null;
        return generateIdenticonBitmap(generateHash(saltedKey(key)));
    }

    @Override
    public byte[] generateIdenticonByteArray(String key) {
        if (TextUtils.isEmpty(key)) return null;
        return generateIdenticonByteArray(generateHash(saltedKey(key)));
    }

    private int ensureContrast(int color) {
        if (getColorDistance(color, BG_COLOR) <= 60f) {
            return getComplementaryColor(color);
        }
        return color;
    }

    private void buildSeeds(byte[] hash, int size, float[] seedX, float[] seedY, int mode) {
        switch (mode) {
            case 0:
                buildAsymmetricSeeds(hash, size, seedX, seedY);
                return;
            case 1:
                buildMirroredXSeeds(hash, size, seedX, seedY);
                return;
            case 2:
                buildRadialJitterSeeds(hash, size, seedX, seedY);
                return;
            default:
                buildGridJitterSeeds(hash, size, seedX, seedY);
        }
    }

    private void buildAsymmetricSeeds(byte[] hash, int size, float[] seedX, float[] seedY) {
        for (int i = 0; i < SEED_COUNT; i++) {
            int b1 = hash[(i * 2) % 16] & 0xFF;
            int b2 = hash[(i * 2 + 1) % 16] & 0xFF;
            seedX[i] = (b1 / 255f) * (size - 1);
            seedY[i] = (b2 / 255f) * (size - 1);
        }
    }

    private void buildMirroredXSeeds(byte[] hash, int size, float[] seedX, float[] seedY) {
        int halfPairs = SEED_COUNT / 2;
        for (int i = 0; i < halfPairs; i++) {
            int b1 = hash[(i * 2) % 16] & 0xFF;
            int b2 = hash[(i * 2 + 1) % 16] & 0xFF;
            seedX[i] = (b1 / 255f) * (size - 1);
            seedY[i] = (b2 / 255f) * (size - 1);
            seedX[i + halfPairs] = (size - 1) - seedX[i];
            seedY[i + halfPairs] = seedY[i];
        }
    }

    private void buildRadialJitterSeeds(byte[] hash, int size, float[] seedX, float[] seedY) {
        float cx = size / 2f;
        float cy = size / 2f;
        for (int i = 0; i < SEED_COUNT; i++) {
            int b1 = hash[(i * 2) % 16] & 0xFF;
            int b2 = hash[(i * 2 + 1) % 16] & 0xFF;
            float angle = (float) ((i / (float) SEED_COUNT) * Math.PI * 2.0 + (b1 / 255f) * 0.45);
            float radius = (0.18f + (b2 / 255f) * 0.34f) * size;
            seedX[i] = clamp((float) (cx + radius * Math.cos(angle)), 0, size - 1);
            seedY[i] = clamp((float) (cy + radius * Math.sin(angle)), 0, size - 1);
        }
    }

    private void buildGridJitterSeeds(byte[] hash, int size, float[] seedX, float[] seedY) {
        int idx = 0;
        for (int gy = 0; gy < 2; gy++) {
            for (int gx = 0; gx < 5; gx++) {
                if (idx >= SEED_COUNT) break;
                int b1 = hash[(idx * 2) % 16] & 0xFF;
                int b2 = hash[(idx * 2 + 1) % 16] & 0xFF;
                float cellW = size / 5f;
                float cellH = size / 2f;
                float x = gx * cellW + cellW * 0.2f + (b1 / 255f) * cellW * 0.6f;
                float y = gy * cellH + cellH * 0.2f + (b2 / 255f) * cellH * 0.6f;
                seedX[idx] = clamp(x, 0, size - 1);
                seedY[idx] = clamp(y, 0, size - 1);
                idx++;
            }
        }
    }

    private int nearestSeed(float x, float y, float[] seedX, float[] seedY) {
        int nearest = 0;
        float best = Float.MAX_VALUE;
        for (int i = 0; i < seedX.length; i++) {
            float dx = x - seedX[i];
            float dy = y - seedY[i];
            float d = dx * dx + dy * dy;
            if (d < best) {
                best = d;
                nearest = i;
            }
        }
        return nearest;
    }

    private int getNibble(byte[] hash, int nibbleIndex) {
        int i = nibbleIndex % (hash.length * 2);
        int b = hash[i / 2];
        return ((i & 1) == 0) ? (b & 0x0F) : ((b >> 4) & 0x0F);
    }

    private float clamp(float v, float min, float max) {
        return Math.max(min, Math.min(max, v));
    }
}
