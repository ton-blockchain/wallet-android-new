package org.ton.wallet.lib.lists;

import androidx.annotation.NonNull;

public interface ListItemClickListener<T> {

    void onItemClicked(@NonNull T item, int position);

    default boolean onItemLongClick(@NonNull T item, int position) {
        return false;
    }
}
