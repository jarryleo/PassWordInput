package cn.leo.passwordinput;

import android.content.Context;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputConnectionWrapper;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * create by : Jarry Leo
 * date : 2018/8/13 17:19
 */
public class PasswordInputLayout extends LinearLayout implements TextWatcher, View.OnFocusChangeListener {
    private static final int mDefaultLength = 6;
    private int mFocusIndex;
    private PasswordInputCompleteListener mInputCompleteListener;
    private StringBuilder mStringBuilder = new StringBuilder();

    public PasswordInputLayout(Context context) {
        this(context, null);
    }

    public PasswordInputLayout(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PasswordInputLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setOrientation(HORIZONTAL);
        ViewGroup.LayoutParams params = getLayoutParams();
        if (params == null) {
            params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.MATCH_PARENT);
        }
        LinearLayout.LayoutParams layoutParams = (LayoutParams) params;
        layoutParams.gravity = Gravity.CENTER;
        layoutParams.weight = 1;
        for (int i = 0; i < mDefaultLength; i++) {
            EditText editText = new EditText(getContext()) {
                @Override
                public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
                    TInputConnection tInputConnection = new TInputConnection(null, true);
                    tInputConnection.setTarget(super.onCreateInputConnection(outAttrs));
                    return tInputConnection;
                }
            };
            editText.addTextChangedListener(this);
            editText.setOnFocusChangeListener(this);
            editText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(1)});
            editText.setGravity(Gravity.CENTER);
            editText.setInputType(InputType.TYPE_CLASS_NUMBER);
            addView(editText, layoutParams);
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        int length = s.length();
        if (length >= 1) {
            moveFocusNext();
        }
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (hasFocus) {
            mFocusIndex = findChildIndex(v);
            if (v instanceof TextView) {
                ((TextView) v).setText("");
            }
        }
    }

    //焦点后移
    private void moveFocusNext() {
        if (mFocusIndex < mDefaultLength - 1) {
            getChildAt(++mFocusIndex).requestFocus();
            return;
        }
        if (mInputCompleteListener != null &&
                mFocusIndex == mDefaultLength - 1) {
            mStringBuilder.delete(0, mStringBuilder.length());
            for (int i = 0; i < mDefaultLength; i++) {
                View child = getChildAt(i);
                if (child instanceof TextView) {
                    String s = ((TextView) child).getText().toString();
                    if (s.length() == 1) {
                        mStringBuilder.append(s);
                    } else {
                        mFocusIndex = i;
                        child.requestFocus();
                        return;
                    }
                }
            }
            mInputCompleteListener.onInputComplete(mStringBuilder.toString());
            hideSoftInput();
        }
    }

    //焦点前移
    private void moveFocusPre() {
        if (mFocusIndex > 0) {
            View child = getChildAt(--mFocusIndex);
            if (child instanceof TextView) {
                ((TextView) child).setText("");
            }
            child.requestFocus();
        }
    }

    //查找输入框序号
    private int findChildIndex(View v) {
        int count = getChildCount();
        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            if (child == v) return i;
        }
        return -1;
    }

    //隐藏输入法
    public void hideSoftInput() {
        InputMethodManager inputMethodManager = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        assert inputMethodManager != null;
        if (inputMethodManager.isActive()) {
            inputMethodManager.hideSoftInputFromWindow(getWindowToken(), 0);
        }
    }

    public void setInputCompleteListener(PasswordInputCompleteListener inputCompleteListener) {
        mInputCompleteListener = inputCompleteListener;
    }

    public interface PasswordInputCompleteListener {
        void onInputComplete(String password);
    }

    public class TInputConnection extends InputConnectionWrapper {
        /**
         * Initializes a wrapper.
         * <p>
         * <p><b>Caveat:</b> Although the system can accept {@code (InputConnection) null} in some
         * places, you cannot emulate such a behavior by non-null {@link InputConnectionWrapper} that
         * has {@code null} in {@code target}.</p>
         *
         * @param target  the {@link InputConnection} to be proxied.
         * @param mutable set {@code true} to protect this object from being reconfigured to target
         *                another {@link InputConnection}.  Note that this is ignored while the target is {@code null}.
         */
        public TInputConnection(InputConnection target, boolean mutable) {
            super(target, mutable);
        }

        /**
         * 当软键盘删除文本之前，会调用这个方法通知输入框，我们可以重写这个方法并判断是否要拦截这个删除事件。
         * 在谷歌输入法上，点击退格键的时候不会调用{@link #sendKeyEvent(KeyEvent event)}，
         * 而是直接回调这个方法，所以也要在这个方法上做拦截；
         */
        @Override
        public boolean deleteSurroundingText(int beforeLength, int afterLength) {
            moveFocusPre();
            return super.deleteSurroundingText(beforeLength, afterLength);
        }


        /**
         * 当在软件盘上点击某些按钮（比如退格键，数字键，回车键等），该方法可能会被触发（取决于输入法的开发者），
         * 所以也可以重写该方法并拦截这些事件，这些事件就不会被分发到输入框了
         */
        @Override
        public boolean sendKeyEvent(KeyEvent event) {
            if (event.getKeyCode() == KeyEvent.KEYCODE_DEL && event.getAction() == KeyEvent.ACTION_DOWN) {
                moveFocusPre();
            }
            return super.sendKeyEvent(event);
        }
    }
}
