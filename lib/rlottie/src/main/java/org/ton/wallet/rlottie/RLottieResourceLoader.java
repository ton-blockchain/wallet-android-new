package org.ton.wallet.rlottie;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;

import java.io.InputStream;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class RLottieResourceLoader {

    private static final Executor readResourceExecutor = Executors.newCachedThreadPool();
    private static final Handler handler = new Handler(Looper.getMainLooper());

    public static void readRawResourceAsync(Context context, int resourceId, RLottieResourceCallback callback) {
        readResourceExecutor.execute(() -> {
            final String result = readRawResource(context, resourceId);
            int width = readIntValue(result, "\"w\"");
            int height = readIntValue(result, "\"h\"");
            handler.post(() -> callback.invoke(result, width, height));
        });
    }

    private static int readIntValue(final String string, final String key) {
        if (TextUtils.isEmpty(string)) {
            return 0;
        }
        int value = 0;
        int indexOfKey = string.indexOf(key);
        if (indexOfKey != -1) {
            int indexOfWidthEnd = string.indexOf(',', indexOfKey);
            if (indexOfWidthEnd != -1) {
                String valueString = string.substring(indexOfKey + key.length() + 1, indexOfWidthEnd);
                value = Integer.parseInt(valueString);
            }
        }
        return value;
    }

    private static String readRawResource(Context context, int resourceId) {
        try (InputStream inputStream = context.getResources().openRawResource(resourceId)) {
            byte[] buffer = new byte[64 * 1024];
            StringBuilder builder = new StringBuilder();
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer, 0, buffer.length)) > 0) {
                builder.append(new String(buffer, 0, bytesRead));
            }
            return builder.toString();
        } catch (Exception e) {
            Log.e("RLottieResourceLoader", null, e);
        }
        return null;
    }

    public interface RLottieResourceCallback {

        void invoke(String json, int resWidth, int resHeight);
    }
}
