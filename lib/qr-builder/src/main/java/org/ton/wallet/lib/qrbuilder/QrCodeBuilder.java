package org.ton.wallet.lib.qrbuilder;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.google.zxing.qrcode.encoder.ByteMatrix;
import com.google.zxing.qrcode.encoder.QRCode;

import java.util.Arrays;
import java.util.HashMap;

public class QrCodeBuilder {

    private final String content;
    private final int width;
    private final int height;

    private int fillColor = Color.BLACK;
    private int backgroundColor = Color.WHITE;
    private int quietZonePx = 0;
    private boolean withCutout = false;
    private Drawable cutoutDrawable;

    private int cornerSquareSize = 0;
    private int cutoutFirstBlock = 0;
    private int cutoutBlockCount = 0;

    public QrCodeBuilder(String content, int width, int height) {
        this.content = content;
        this.width = width;
        this.height = height;
    }

    public QrCodeBuilder setBackgroundColor(int color) {
        this.backgroundColor = color;
        return this;
    }

    public QrCodeBuilder setFillColor(int color) {
        this.fillColor = color;
        return this;
    }

    public QrCodeBuilder setQuietZone(int px) {
        this.quietZonePx = px;
        return this;
    }

    public QrCodeBuilder setWithCutout(boolean withCutout) {
        this.withCutout = withCutout;
        return this;
    }

    public QrCodeBuilder setCutoutDrawable(Drawable drawable) {
        this.cutoutDrawable = drawable;
        return this;
    }

    public Bitmap build() {
        final QRCodeWriter writer = new QRCodeWriter();
        final HashMap<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);
        hints.put(EncodeHintType.MARGIN, 0);
        ByteMatrix matrix;
        try {
            QRCode qrCode = writer.getQrCode(content, BarcodeFormat.QR_CODE, hints);
            matrix = qrCode.getMatrix();
        } catch (Exception e) {
            matrix = null;
        }
        if (matrix == null) {
            return null;
        }

        final int qrWidth = matrix.getWidth() + (quietZonePx * 2);
        final int qrHeight = matrix.getHeight() + (quietZonePx * 2);
        final int outputWidth = Math.max(width, qrWidth);
        final int outputHeight = Math.max(height, qrHeight);
        final int multiple = Math.min(outputWidth / qrWidth, outputHeight / qrHeight);
        final int horizontalPadding = (outputWidth - (matrix.getWidth() * multiple)) / 2;
        final int verticalPadding = (outputHeight - (matrix.getHeight() * multiple)) / 2;
        final int size = multiple * matrix.getWidth() + horizontalPadding * 2;

        if (withCutout) {
            cutoutBlockCount = Math.round((size - 32) * 0.25f / multiple);
            if (cutoutBlockCount % 2 != matrix.getWidth() % 2) {
                ++cutoutBlockCount;
            }
            cutoutFirstBlock = (matrix.getWidth() - cutoutBlockCount) / 2;
        }

        for (int x = 0; x < matrix.getWidth(); ++x) {
            if (hasPoint(matrix, x, 0)) {
                cornerSquareSize++;
            } else {
                break;
            }
        }

        final Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        final Canvas canvas = new Canvas(bitmap);

        final Paint fillPaint = new Paint();
        fillPaint.setColor(fillColor);

        final float radius = multiple / 2f;
        final float[] radii = new float[8];
        final GradientDrawable rect = new GradientDrawable();
        rect.setCornerRadii(radii);
        rect.setShape(GradientDrawable.RECTANGLE);

        // draw corner squares
        for (int i = 0; i < 3; ++i) {
            int x;
            int y;
            if (i == 0) {
                x = horizontalPadding;
                y = verticalPadding;
            } else if (i == 1) {
                x = bitmap.getWidth() - cornerSquareSize * multiple - horizontalPadding;
                y = verticalPadding;
            } else {
                x = horizontalPadding;
                y = bitmap.getHeight() - cornerSquareSize * multiple - verticalPadding;
            }

            float cornerRadius = (cornerSquareSize * multiple) * 0.25f;
            Arrays.fill(radii, cornerRadius);
            rect.setBounds(x, y, x + cornerSquareSize * multiple, y + cornerSquareSize * multiple);
            rect.setColor(fillColor);
            rect.draw(canvas);

            cornerRadius = (cornerSquareSize * multiple) * 0.12f;
            Arrays.fill(radii, cornerRadius);
            rect.setBounds(x + multiple, y + multiple, x + (cornerSquareSize - 1) * multiple, y + (cornerSquareSize - 1) * multiple);
            rect.setColor(backgroundColor);
            rect.draw(canvas);

            cornerRadius = (cornerSquareSize * multiple) * 0.10f;
            Arrays.fill(radii, cornerRadius);
            rect.setBounds(x + multiple * 2, y + multiple * 2, x + (cornerSquareSize - 2) * multiple, y + (cornerSquareSize - 2) * multiple);
            rect.setColor(fillColor);
            rect.draw(canvas);
        }

        // draw points
        for (int y = 0, yOutput = verticalPadding;  y < matrix.getHeight();  ++y, yOutput += multiple) {
            for (int x = 0, xOutput = horizontalPadding;  x < matrix.getWidth();  ++x, xOutput += multiple) {
                if (hasPoint(matrix, x, y)) {
                    Arrays.fill(radii, radius);
                    if (hasPoint(matrix, x - 1, y)) {
                        radii[0] = radii[1] = radii[6] = radii[7] = 0f;
                    }
                    if (hasPoint(matrix, x, y - 1)) {
                        radii[0] = radii[1] = radii[2] = radii[3] = 0f;
                    }
                    if (hasPoint(matrix, x + 1, y)) {
                        radii[2] = radii[3] = radii[4] = radii[5] = 0f;
                    }
                    if (hasPoint(matrix, x, y + 1)) {
                        radii[4] = radii[5] = radii[6] = radii[7] = 0f;
                    }
                    rect.setColor(fillColor);
                    rect.setBounds(xOutput, yOutput, xOutput + multiple, yOutput + multiple);
                    rect.draw(canvas);
                } else {
                    Arrays.fill(radii, 0f);
                    boolean hasCornerNeighbor = false;
                    if (hasPoint(matrix, x - 1, y) && hasPoint(matrix, x - 1, y - 1) && hasPoint(matrix, x, y - 1)) {
                        radii[0] = radii[1] = radius;
                        hasCornerNeighbor = true;
                    }
                    if (hasPoint(matrix, x, y - 1) && hasPoint(matrix, x + 1, y - 1) && hasPoint(matrix, x + 1, y)) {
                        radii[2] = radii[3] = radius;
                        hasCornerNeighbor = true;
                    }
                    if (hasPoint(matrix, x + 1, y) && hasPoint(matrix, x + 1, y + 1) && hasPoint(matrix, x, y + 1)) {
                        radii[4] = radii[5] = radius;
                        hasCornerNeighbor = true;
                    }
                    if (hasPoint(matrix, x, y + 1) && hasPoint(matrix, x - 1, y + 1) && hasPoint(matrix, x - 1, y)) {
                        radii[6] = radii[7] = radius;
                        hasCornerNeighbor = true;
                    }
                    if (hasCornerNeighbor) {
                        canvas.drawRect(xOutput, yOutput, xOutput + multiple, yOutput + multiple, fillPaint);
                        rect.setBounds(xOutput, yOutput, xOutput + multiple, yOutput + multiple);
                        rect.setColor(backgroundColor);
                        rect.draw(canvas);
                    }
                }
            }
        }

        if (withCutout && cutoutDrawable != null) {
            final int cutoutSize = cutoutBlockCount * multiple;
            final int cutoutOffset = (size - cutoutSize) / 2;
            cutoutDrawable.setBounds(cutoutOffset, cutoutOffset, cutoutOffset + cutoutSize, cutoutOffset + cutoutSize);
            cutoutDrawable.draw(canvas);
        }

        canvas.setBitmap(null);
        return bitmap;
    }

    private boolean hasPoint(ByteMatrix matrix, int x, int y) {
        if (cutoutFirstBlock <= x && x < cutoutFirstBlock + cutoutBlockCount && cutoutFirstBlock <= y && y < cutoutFirstBlock + cutoutBlockCount) {
            return false;
        }
        if (x < cornerSquareSize && y < cornerSquareSize) {
            return false;
        }
        if (matrix.getWidth() - cornerSquareSize <= x && y < cornerSquareSize) {
            return false;
        }
        if (x < cornerSquareSize && matrix.getHeight() - cornerSquareSize <= y) {
            return false;
        }
        return 0 <= x && x < matrix.getWidth() && 0 <= y && y < matrix.getHeight() && matrix.get(x, y) == 1;
    }
}
