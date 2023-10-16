package org.ton.wallet.lib.lists;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class AdapterItemChangedNotifier<T> {

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final List<Command> commands = new ArrayList<>();

    @Nullable
    private RecyclerAdapter<T, ?> adapter;

    public void setAdapter(@Nullable RecyclerAdapter<T, ?> adapter) {
        this.adapter = adapter;
        for (Command command : commands) {
            executeCommand(command);
        }
    }

    public void itemChanged(int position, @Nullable Object payload) {
        executeCommand(new ItemChangedCommand(position, payload));
    }

    public void setItems(@NonNull List<T> items) {
        executeCommand(new SetItemsCommand<>(items));
    }

    private void executeCommand(@NonNull Command command) {
        if (Looper.getMainLooper().isCurrentThread()) {
            if (adapter == null) {
                commands.add(command);
            } else {
                if (command instanceof ItemChangedCommand) {
                    ItemChangedCommand cmd = (ItemChangedCommand) command;
                    adapter.notifyItemChanged(cmd.position, cmd.payload);
                } else if (command instanceof SetItemsCommand<?>) {
                    adapter.setItems(((SetItemsCommand<T>) command).items);
                }
            }
        } else {
            handler.post(() -> executeCommand(command));
        }
    }


    private interface Command {}

    private static class ItemChangedCommand implements Command {

        public final int position;

        @Nullable
        public final Object payload;

        public ItemChangedCommand(int position, @Nullable Object payload) {
            this.position = position;
            this.payload = payload;
        }
    }

    private static class SetItemsCommand<T> implements Command {

        public final List<T> items;

        public SetItemsCommand(List<T> items) {
            this.items = items;
        }
    }
}
