package org.ton.wallet.lib.lists;

import android.util.SparseIntArray;

import androidx.recyclerview.widget.RecyclerView;

public class NoLimitRecycledViewPool extends RecyclerView.RecycledViewPool {

    private final SparseIntArray scrapCount = new SparseIntArray();
    private final SparseIntArray maxScrap = new SparseIntArray();

    @Override
    public void setMaxRecycledViews(int viewType, int max) {
        maxScrap.put(viewType, max);
        super.setMaxRecycledViews(viewType, max);
    }

    @Override
    public RecyclerView.ViewHolder getRecycledView(int viewType) {
        final RecyclerView.ViewHolder viewHolder = super.getRecycledView(viewType);
        if (viewHolder != null) {
            final int count = scrapCount.get(viewType, -1);
            if (count <= 0) {
                throw new IllegalStateException("Not expected here. The #put call must be before");
            } else {
                scrapCount.put(viewType, count - 1);
            }
        }
        return viewHolder;
    }
    @Override
    public void putRecycledView(RecyclerView.ViewHolder scrap) {
        final int viewType = scrap.getItemViewType();
        final int count = scrapCount.get(viewType, 0);
        scrapCount.put(viewType, count + 1);
        int max = maxScrap.get(viewType, -1);
        if (max == -1) {
            max = 5;
            setMaxRecycledViews(viewType, max);
        }
        if (count + 1 > max) {
            setMaxRecycledViews(viewType, count + 1);
        }
        super.putRecycledView(scrap);
    }

    @Override
    public void clear() {
        scrapCount.clear();
        maxScrap.clear();
        super.clear();
    }
}
