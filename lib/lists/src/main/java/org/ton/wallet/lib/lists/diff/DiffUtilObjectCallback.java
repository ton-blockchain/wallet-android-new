package org.ton.wallet.lib.lists.diff;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DiffUtil;

public class DiffUtilObjectCallback<T> extends DiffUtil.ItemCallback<T> {

    @Override
    public boolean areItemsTheSame(@NonNull T oldItem, @NonNull T newItem) {
        if (oldItem instanceof DiffUtilItem && newItem instanceof DiffUtilItem) {
            return ((DiffUtilItem) oldItem).areItemsTheSame(((DiffUtilItem) newItem));
        } else {
            return false;
        }
    }

    @Override
    public boolean areContentsTheSame(@NonNull T oldItem, @NonNull T newItem) {
        if (oldItem instanceof DiffUtilItem && newItem instanceof DiffUtilItem) {
            return ((DiffUtilItem) oldItem).areContentsTheSame(((DiffUtilItem) newItem));
        } else {
            return false;
        }
    }

    @Nullable
    @Override
    public Object getChangePayload(@NonNull T oldItem, @NonNull T newItem) {
        if (oldItem instanceof DiffUtilItem && newItem instanceof DiffUtilItem) {
            return ((DiffUtilItem) oldItem).getChangePayload(((DiffUtilItem) newItem));
        } else {
            return false;
        }
    }
}
