package org.ton.wallet.lib.lists.diff;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public interface DiffUtilItem {

    boolean areItemsTheSame(@NonNull DiffUtilItem newItem);

    boolean areContentsTheSame(@NonNull DiffUtilItem newItem);

    @Nullable
    default Object getChangePayload(@NonNull DiffUtilItem newItem) {
        return null;
    }
}
