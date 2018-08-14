package cn.leo.passwordinput;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.support.annotation.DrawableRes;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
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
/*自定义属性:
<declare-styleable name="PasswordInputLayout">
        <attr name="pil_background" format="reference"/>
        <attr name="pil_textSize" format="dimension"/>
        <attr name="pil_textColor" format="color"/>
        <attr name="pil_padding" format="dimension"/>
        <attr name="pil_length" format="integer"/>
        <attr name="pil_inputType" format="enum">
            <enum name="pil_char" value="1"/>
            <enum name="pil_number" value="2"/>
            <enum name="pil_char_password" value="129"/>
            <enum name="pil_number_password" value="18"/>
        </attr>
</declare-styleable>
*/
public class PasswordInputLayout extends LinearLayout implements TextWatcher, View.OnFocusChangeListener {
    private int mDefaultLength = 6;
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
        init(attrs);
    }

    private void init(@Nullable AttributeSet attrs) {
        TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.PasswordInputLayout);
        int backgroundRes = typedArray.getResourceId(R.styleable.PasswordInputLayout_pil_background, 0);
        int inputType = typedArray.getInt(R.styleable.PasswordInputLayout_pil_inputType, InputType.TYPE_CLASS_NUMBER);
        float textSize = typedArray.getDimension(R.styleable.PasswordInputLayout_pil_textSize,
                TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 18, getResources().getDisplayMetrics()));
        int textColor = typedArray.getColor(R.styleable.PasswordInputLayout_pil_textColor, Color.BLACK);
        int padding = typedArray.getDimensionPixelSize(R.styleable.PasswordInputLayout_pil_padding, 5);
        mDefaultLength = typedArray.getInt(R.styleable.PasswordInputLayout_pil_length, 6);
        typedArray.recycle();
        setOrientation(HORIZONTAL);
        ViewGroup.LayoutParams params = getLayoutParams();
        if (params == null) {
            params = new LinearLayout.LayoutParams(0,
                    ViewGroup.LayoutParams.MATCH_PARENT);
        }
        LinearLayout.LayoutParams layoutParams = (LayoutParams) params;
        layoutParams.gravity = Gravity.CENTER;
        layoutParams.weight = 1;
        layoutParams.leftMargin = padding;
        layoutParams.rightMargin = padding;
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
            editText.setBackgroundResource(backgroundRes);
            editText.setInputType(inputType);
            editText.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
            editText.setTextColor(textColor);
            editText.setLongClickable(false);
            editText.setTextIsSelectable(false);
            addView(editText, layoutParams);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        if (widthMode == MeasureSpec.AT_MOST) {

            float width = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 300, displayMetrics) + 0.5f;
            widthMeasureSpec = MeasureSpec.makeMeasureSpec((int) width, MeasureSpec.EXACTLY);
        }
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        if (heightMode == MeasureSpec.AT_MOST) {
            float height = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 50, displayMetrics) + 0.5f;
            heightMeasureSpec = MeasureSpec.makeMeasureSpec((int) height, MeasureSpec.EXACTLY);
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
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
            int index = findChildIndex(v);
            if (mFocusIndex != index) {
                getChildAt(mFocusIndex).requestFocus();
            }
        }
    }

    //焦点后移
    private void moveFocusNext() {
        if (mFocusIndex < mDefaultLength - 1) {
            ((TextView) getChildAt(mFocusIndex)).setCursorVisible(false);
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
                TextView textView = (TextView) child;
                textView.setCursorVisible(true);
                textView.setText("");
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

    //设置每个输入框样式
    public void setEditBoard(@DrawableRes int drawableRes) {
        for (int i = 0; i < getChildCount(); i++) {
            getChildAt(i).setBackgroundResource(drawableRes);
        }
    }

    //设置字体大小
    public void setTextSize(float textSize) {
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            if (child instanceof TextView) {
                ((TextView) child).setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize);
            }
        }
    }

    //设置字体颜色
    public void setTextColor(int textColor) {
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            if (child instanceof TextView) {
                ((TextView) child).setTextColor(textColor);
            }
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

