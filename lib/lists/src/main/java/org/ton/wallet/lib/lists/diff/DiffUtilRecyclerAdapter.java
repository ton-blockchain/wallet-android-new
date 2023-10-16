package org.ton.wallet.lib.lists.diff;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.AsyncListDiffer;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import org.ton.wallet.lib.lists.RecyclerAdapter;

import java.util.List;

public abstract class DiffUtilRecyclerAdapter<T, VH extends RecyclerView.ViewHolder> extends RecyclerAdapter<T, VH> {

    private final AsyncListDiffer<T> differ;

    public DiffUtilRecyclerAdapter(DiffUtil.ItemCallback<T> diffCallback) {
        this.differ = new AsyncListDiffer<>(this, diffCallback);
    }

    @Override
    public void setItems(@NonNull List<T> items) {
        super.setupItems(items, false);
        differ.submitList(items);
    }
}
