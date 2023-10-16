package org.ton.wallet.lib.lists;

import android.annotation.SuppressLint;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Collections;
import java.util.List;

public abstract class RecyclerAdapter<T, VH extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<VH> {

    private List<T> items = Collections.emptyList();

    @Override
    public final void onBindViewHolder(@NonNull VH holder, int position) {}

    @SuppressWarnings("unchecked")
    @Override
    public void onBindViewHolder(@NonNull VH holder, int position, @NonNull List<Object> payloads) {
        if (holder instanceof RecyclerHolder<?>) {
            ((RecyclerHolder<T>) holder).bind(getItemAt(position), payloads);
        }
        super.onBindViewHolder(holder, position, payloads);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @NonNull
    public T getItemAt(int position) {
        return items.get(position);
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setItems(@NonNull List<T> items) {
        setupItems(items, true);
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setupItems(@NonNull List<T> items, boolean withNotify) {
        this.items = items;
        if (withNotify) {
            notifyDataSetChanged();
        }
    }
}
