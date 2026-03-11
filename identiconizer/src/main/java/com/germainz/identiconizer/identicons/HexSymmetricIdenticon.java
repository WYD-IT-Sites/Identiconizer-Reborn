package com.germainz.identiconizer.identicons;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Tri-color hex mosaic with 60-degree rotational symmetry.
 * Supports three variants via Identicon.HEX_TYPE:
 * 0 = tri-color symmetry (classic), 1 = filled hex grid, 2 = variable hex radius.
 */
public class HexSymmetricIdenticon extends Identicon {

    private static final float SQRT3 = 1.7320508f;

    @Override
    public Bitmap generateIdenticonBitmap(byte[] hash) {
        if (hash == null || hash.length < 16) return null;

        final int size = SIZE;
        final Bitmap bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        final Canvas canvas = new Canvas(bmp);
        canvas.drawColor(BG_COLOR);

        final int[] palette = buildPalette(hash);
        final Paint fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        fillPaint.setStyle(Paint.Style.FILL);
        final Paint strokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        strokePaint.setStyle(Paint.Style.STROKE);
        strokePaint.setStrokeWidth(Math.max(1f, size / 240f));
        strokePaint.setColor(adjustContrast(Color.argb(96, 22, 36, 58), BG_COLOR));

        final int gridRadius = 2;
        final List<Cell> cells = buildHexCells(gridRadius);
        final Map<String, Integer> canonicalIndexMap = buildCanonicalIndexMap(cells);

        final float baseHexRadius = size / 7.2f;
        final float cx = size / 2f;
        final float cy = size / 2f;

        for (Cell cell : cells) {
            String canonicalKey = canonicalKey(cell.q, cell.r);
            int idx = canonicalIndexMap.get(canonicalKey);
            int nibble = getNibble(hash, idx);

            int colorChoice;
            boolean draw = true;
            float radius;

            if (HEX_TYPE == IdenticonFactory.HEX_STYLE_VARIABLE_RADIUS) {
                // Boldly different: sparse + dramatic size changes.
                if (cell.q == 0 && cell.r == 0) {
                    colorChoice = 1;
                } else {
                    draw = (nibble % 5) != 0; // some intentional holes
                    colorChoice = (nibble % 3) + 1;
                }
                float scale = 0.38f + (nibble / 15f) * 0.70f;
                radius = baseHexRadius * scale;
            } else if (HEX_TYPE == IdenticonFactory.HEX_STYLE_FILLED_GRID) {
                // Dense quilt look: no holes, full-ish cells.
                colorChoice = ((nibble + Math.abs(cell.q) + Math.abs(cell.r)) % 3) + 1;
                radius = baseHexRadius * 1.03f;
            } else {
                // Classic tri-color symmetry.
                colorChoice = nibble % 4; // 0 = empty, 1..3 = palette
                if (cell.q == 0 && cell.r == 0 && colorChoice == 0) colorChoice = 1;
                draw = colorChoice != 0;
                if (colorChoice == 0) colorChoice = 1;
                radius = baseHexRadius * 0.93f;
            }

            if (!draw) continue;

            float px = cx + baseHexRadius * SQRT3 * (cell.q + cell.r / 2f);
            float py = cy + baseHexRadius * 1.5f * cell.r;

            Path hexPath = makeHexPath(px, py, radius);
            fillPaint.setColor(palette[colorChoice - 1]);
            canvas.drawPath(hexPath, fillPaint);
            canvas.drawPath(hexPath, strokePaint);
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

    private int[] buildPalette(byte[] hash) {
        int c1 = Color.rgb(hash[15] & 0xFF, hash[14] & 0xFF, hash[13] & 0xFF);
        int c2 = Color.rgb(hash[12] & 0xFF, hash[11] & 0xFF, hash[10] & 0xFF);
        int c3 = Color.rgb(hash[9] & 0xFF, hash[8] & 0xFF, hash[7] & 0xFF);

        c1 = adjustContrast(c1, BG_COLOR);
        c2 = adjustContrast(c2, BG_COLOR);
        c3 = adjustContrast(c3, BG_COLOR);

        return new int[]{c1, c2, c3};
    }

    private int adjustContrast(int color, int against) {
        if (getColorDistance(color, against) > 64f) return color;
        return getComplementaryColor(color);
    }

    private List<Cell> buildHexCells(int radius) {
        List<Cell> cells = new ArrayList<>();
        for (int q = -radius; q <= radius; q++) {
            int r1 = Math.max(-radius, -q - radius);
            int r2 = Math.min(radius, -q + radius);
            for (int r = r1; r <= r2; r++) {
                cells.add(new Cell(q, r));
            }
        }
        return cells;
    }

    private Map<String, Integer> buildCanonicalIndexMap(List<Cell> cells) {
        List<String> keys = new ArrayList<>();
        for (Cell c : cells) keys.add(canonicalKey(c.q, c.r));
        Collections.sort(keys);

        List<String> unique = new ArrayList<>();
        String last = null;
        for (String key : keys) {
            if (!key.equals(last)) {
                unique.add(key);
                last = key;
            }
        }

        Map<String, Integer> map = new HashMap<>();
        for (int i = 0; i < unique.size(); i++) map.put(unique.get(i), i);
        return map;
    }

    private String canonicalKey(int q, int r) {
        int[][] variants = new int[6][2];
        int cq = q;
        int cr = r;
        for (int i = 0; i < 6; i++) {
            variants[i][0] = cq;
            variants[i][1] = cr;
            int nq = -cr;
            int nr = cq + cr;
            cq = nq;
            cr = nr;
        }

        int bestQ = variants[0][0];
        int bestR = variants[0][1];
        for (int i = 1; i < 6; i++) {
            int vq = variants[i][0];
            int vr = variants[i][1];
            if (vq < bestQ || (vq == bestQ && vr < bestR)) {
                bestQ = vq;
                bestR = vr;
            }
        }
        return String.format(Locale.ENGLISH, "%d,%d", bestQ, bestR);
    }

    private int getNibble(byte[] hash, int nibbleIndex) {
        int i = nibbleIndex % (hash.length * 2);
        int b = hash[i / 2];
        return ((i & 1) == 0) ? (b & 0x0F) : ((b >> 4) & 0x0F);
    }

    private Path makeHexPath(float cx, float cy, float radius) {
        Path p = new Path();
        for (int i = 0; i < 6; i++) {
            double angle = Math.toRadians(60 * i - 30);
            float x = (float) (cx + radius * Math.cos(angle));
            float y = (float) (cy + radius * Math.sin(angle));
            if (i == 0) p.moveTo(x, y);
            else p.lineTo(x, y);
        }
        p.close();
        return p;
    }

    private static class Cell {
        final int q;
        final int r;

        Cell(int q, int r) {
            this.q = q;
            this.r = r;
        }
    }
}
