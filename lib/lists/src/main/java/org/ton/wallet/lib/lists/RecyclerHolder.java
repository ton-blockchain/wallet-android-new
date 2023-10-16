package org.ton.wallet.lib.lists;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.CallSuper;
import androidx.annotation.IdRes;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public abstract class RecyclerHolder<T> extends RecyclerView.ViewHolder {

    private T item;

    public RecyclerHolder(@NonNull View view) {
        super(view);
    }

    public RecyclerHolder(@LayoutRes int layoutRes, @NonNull ViewGroup parent) {
        this(LayoutInflater.from(parent.getContext()).inflate(layoutRes, parent, false));
    }

    public final void bind(@NonNull T item, @NonNull List<Object> payloads) {
        if (payloads.isEmpty()) {
            bind(item);
        } else {
            this.item = item;
            for (int i = 0; i < payloads.size(); ++i) {
                bindPayload(payloads.get(i));
            }
        }
    }

    @CallSuper
    protected void bind(@NonNull T item) {
        this.item = item;
    }

    protected void bindPayload(@NonNull Object payload) {}

    protected final T getItem() {
        return item;
    }

    @NonNull
    protected final <V extends View> V findViewById(@IdRes int id) {
        return itemView.findViewById(id);
    }

    @NonNull
    protected final Context getContext() {
        return itemView.getContext();
    }
}
