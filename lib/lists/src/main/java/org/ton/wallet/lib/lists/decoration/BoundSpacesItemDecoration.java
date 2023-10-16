package org.ton.wallet.lib.lists.decoration;

import android.graphics.Rect;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class BoundSpacesItemDecoration extends SpacesItemDecoration {

    private final int head;
    private final int tail;

    public BoundSpacesItemDecoration(int headTail) {
        this(headTail, 0, 0);
    }

    public BoundSpacesItemDecoration(int headTail, int horizontal, int vertical) {
        this(headTail, headTail, horizontal, vertical, horizontal, vertical);
    }

    public BoundSpacesItemDecoration(int head, int tail, int start, int top, int end, int bottom) {
        super(start, top, end, bottom);
        this.head = head;
        this.tail = tail;
    }

    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);

        boolean isHorizontal = false;
        if (parent.getLayoutManager() instanceof LinearLayoutManager) {
            isHorizontal = ((LinearLayoutManager) parent.getLayoutManager()).getOrientation() == LinearLayoutManager.HORIZONTAL;
        }

        int itemCount = parent.getAdapter() != null ? parent.getAdapter().getItemCount() : 0;
        int position = parent.getChildAdapterPosition(view);
        if (position == 0) {
            if (isHorizontal) {
                outRect.left = head;
            } else {
                outRect.top = head;
            }
        } else if (position == itemCount - 1) {
            if (isHorizontal) {
                outRect.right = tail;
            } else {
                outRect.bottom = tail;
            }
        }
    }
}
