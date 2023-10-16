package org.ton.wallet.lib.lists.decoration;

import android.graphics.Rect;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class SpacesItemDecoration extends RecyclerView.ItemDecoration {

    protected final int start;
    protected final int top;
    protected final int end;
    protected final int bottom;

    public SpacesItemDecoration(int space) {
        this(space, space, space, space);
    }

    public SpacesItemDecoration(int horizontal, int vertical) {
        this(horizontal, vertical, horizontal, vertical);
    }

    public SpacesItemDecoration(int start, int top, int end, int bottom) {
        this.start = start;
        this.top = top;
        this.end = end;
        this.bottom = bottom;
    }

    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);
        boolean isRtl = view.getContext().getResources().getConfiguration().getLayoutDirection() == View.LAYOUT_DIRECTION_RTL;
        outRect.top = top;
        outRect.bottom = bottom;
        outRect.left = isRtl ? end : start;
        outRect.right = isRtl ? start : end;
    }
}
