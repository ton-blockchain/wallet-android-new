package org.ton.wallet.rlottie;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.ImageView;

import org.ton.wallet.lib.rlottie.R;

public class RLottieImageView extends ImageView {

    private RLottieDrawable drawable;
    private boolean autoRepeat;
    private boolean isAttachedToWindow;
    private boolean isPlaying;
    private int width;
    private int height;
    private float progress;

    public RLottieImageView(Context context) {
        super(context);
    }

    public RLottieImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public RLottieImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        setScaleType(ScaleType.CENTER);
        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(getContext(), attrs);
        TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.RLottieImageView);
        try {
            int resourceId = typedArray.getResourceId(R.styleable.RLottieImageView_rlottie_rawRes, 0);
            width = typedArray.getDimensionPixelSize(R.styleable.RLottieImageView_rlottie_width, layoutParams.width);
            height = typedArray.getDimensionPixelSize(R.styleable.RLottieImageView_rlottie_height, layoutParams.height);
            if (resourceId != 0) {
                setLottieResource(resourceId, width, height);
            }
            boolean autoRepeat = typedArray.getBoolean(R.styleable.RLottieImageView_rlottie_autoRepeat, false);
            setAutoRepeat(autoRepeat);
            isPlaying = typedArray.getBoolean(R.styleable.RLottieImageView_rlottie_autoPlay, false);
        } finally {
            typedArray.recycle();
        }
    }

    public void setLottieResource(int resourceId) {
        setLottieResource(resourceId, width, height);
    }

    public void setLottieResource(int resourceId, int width, int height) {
        RLottieResourceLoader.readRawResourceAsync(getContext(), resourceId, (json, resWidth, resHeight) -> {
            final String name = "" + resourceId;
            final int drawableWidth = width <= 0 ? resWidth : width;
            final int drawableHeight = height <= 0 ? resHeight : height;
            drawable = new RLottieDrawable(json, name, drawableWidth, drawableHeight, !isPlaying);
            if (autoRepeat) {
                drawable.setAutoRepeat(1);
            }
            if (isAttachedToWindow) {
                if (progress != 0f) {
                    drawable.setProgress(progress);
                }
                if (isPlaying) {
                    drawable.start();
                }
            }
            setImageDrawable(drawable);
        });
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        isAttachedToWindow = true;
        if (drawable != null) {
            if (progress != 0f) {
                drawable.setProgress(progress);
            }
            if (isPlaying) {
                drawable.start();
            }
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        isAttachedToWindow = false;
        if (drawable != null) {
            drawable.stop();
        }
    }

    public void setAutoRepeat(boolean repeat) {
        autoRepeat = repeat;
        if (drawable != null) {
            drawable.setAutoRepeat(1);
        }
    }

    public void playAnimation() {
        if (!isPlaying) {
            isPlaying = true;
            if (drawable != null && isAttachedToWindow) {
                drawable.start();
            }
        }
    }

    public void stopAnimation() {
        if (isPlaying) {
            isPlaying = false;
            if (drawable != null) {
                drawable.stop();
            }
        }
    }

    public void setFinalFrame() {
        progress = 0.99f;
        if (drawable != null) {
            drawable.setProgress(progress);
        }
    }
}
