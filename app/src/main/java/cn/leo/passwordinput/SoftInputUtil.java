package cn.leo.passwordinput;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

/**
 * 输入方弹出隐藏类
 * create by : Jarry Leo
 * date : 2018/8/14 14:23
 */
public class SoftInputUtil {
    public static void ShowSoftInput(@NonNull View v) {
        Context context = v.getContext();
        InputMethodManager inputMethodManager =
                (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        Activity activity = null;
        if (context instanceof Activity) {
            activity = (Activity) context;
        }
        if (inputMethodManager != null) {
            inputMethodManager.showSoftInput(v, 0);
            if (activity != null && !activity.isFinishing()) {
                activity.getWindow().setSoftInputMode(
                        WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
            }
        }
    }

    public static void hideSoftInput(@NonNull View v) {
        Context context = v.getContext();
        InputMethodManager inputMethodManager =
                (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputMethodManager != null && inputMethodManager.isActive()) {
            inputMethodManager.hideSoftInputFromWindow(v.getWindowToken(), 0);
        }
    }
}
