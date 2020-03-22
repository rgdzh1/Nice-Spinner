package org.angmarch.views;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.PopupWindow;


import android.widget.ListPopupWindow;
import android.widget.ListView;


import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.interpolator.view.animation.LinearOutSlowInInterpolator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/*
 * Copyright (C) 2015 Angelo Marchesin.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
public class NiceSpinner extends AppCompatTextView {

    private static final int MAX_LEVEL = 10000;
    private static final int VERTICAL_OFFSET = 1;
    private static final String INSTANCE_STATE = "instance_state";
    private static final String SELECTED_INDEX = "selected_index";
    private static final String IS_POPUP_SHOWING = "is_popup_showing";
    private static final String IS_ARROW_HIDDEN = "is_arrow_hidden";
    private static final String ARROW_DRAWABLE_RES_ID = "arrow_drawable_res_id";

    private int selectedIndex;
    private Drawable arrowDrawable;
    private ListPopupWindow popupWindow;
    private NiceSpinnerBaseAdapter adapter;
    private OnSpinnerItemSelectedListener onSpinnerItemSelectedListener;
    private boolean isArrowHidden;
    private int textColor;//下拉框条目中字体颜色,和NiceSpinner中字体颜色
    private int mPopupBgColor;//下拉框条目中字体颜色,和NiceSpinner中字体颜色
    private int backgroundSelector;//下拉框条目的背景色,和NiceSpinner的背景色
    private int arrowDrawableTint;
    private int displayHeight;
    private int parentVerticalOffset;
    private @DrawableRes
    int arrowDrawableResId;
    private SpinnerTextFormatter spinnerTextFormatter = new SimpleSpinnerTextFormatter();
    private SpinnerTextFormatter selectedTextFormatter = new SimpleSpinnerTextFormatter();
    private PopUpTextAlignment horizontalAlignment;

    @Nullable
    private ObjectAnimator arrowAnimator = null;
    private int mPupopWidth;
    private int mPupopHeight;
    private int mPupopVerticalOffset;
    private int mPupopHorizontalOffset;
    private int mPopupAnim;

    public NiceSpinner(Context context) {
        super(context);
        init(context, null);
    }

    public NiceSpinner(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public NiceSpinner(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);

    }

    @Override
    public Parcelable onSaveInstanceState() {
        Bundle bundle = new Bundle();
        bundle.putParcelable(INSTANCE_STATE, super.onSaveInstanceState());
        bundle.putInt(SELECTED_INDEX, selectedIndex);
        bundle.putBoolean(IS_ARROW_HIDDEN, isArrowHidden);
        bundle.putInt(ARROW_DRAWABLE_RES_ID, arrowDrawableResId);
        if (popupWindow != null) {
            bundle.putBoolean(IS_POPUP_SHOWING, popupWindow.isShowing());
        }
        return bundle;
    }

    @Override
    public void onRestoreInstanceState(Parcelable savedState) {
        if (savedState instanceof Bundle) {
            Bundle bundle = (Bundle) savedState;
            selectedIndex = bundle.getInt(SELECTED_INDEX);
            if (adapter != null) {
                //将被选中索引对应的数据填充到NiceSpinner中
                setTextInternal(selectedTextFormatter.format(adapter.getItemInDataset(selectedIndex)).toString());
                //adapter中更新选中的索引
                adapter.setSelectedIndex(selectedIndex);
            }

            if (bundle.getBoolean(IS_POPUP_SHOWING)) {
                if (popupWindow != null) {
                    //post() 参数为传入一个方法, 该方法将在主线程中运行.
                    // Post the show request into the looper to avoid bad token exception
                    post(this::showDropDown);
                }
            }
            isArrowHidden = bundle.getBoolean(IS_ARROW_HIDDEN, false);
            arrowDrawableResId = bundle.getInt(ARROW_DRAWABLE_RES_ID);
            savedState = bundle.getParcelable(INSTANCE_STATE);
        }
        super.onRestoreInstanceState(savedState);
    }

    private void init(Context context, AttributeSet attrs) {
        Resources resources = getResources();
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.NiceSpinner);
        int defaultPadding = resources.getDimensionPixelSize(R.dimen.one_and_a_half_grid_unit);
        //setGravity(Gravity.CENTER_VERTICAL | Gravity.START);
        setPadding(resources.getDimensionPixelSize(R.dimen.three_grid_unit), defaultPadding, defaultPadding, defaultPadding);
        setClickable(true);
        backgroundSelector = typedArray.getResourceId(R.styleable.NiceSpinner_backgroundSelector, R.drawable.selector);
        //为NiceSpinner控件设置背景色
        setBackgroundResource(backgroundSelector);
        textColor = typedArray.getColor(R.styleable.NiceSpinner_textTint, getDefaultTextColor(context));
        //PopupWindow背景颜色
        mPopupBgColor = typedArray.getResourceId(R.styleable.NiceSpinner_popup_bg_color, android.R.color.white);
        //PopupWindowList宽度
        mPupopWidth = typedArray.getDimensionPixelSize(R.styleable.NiceSpinner_popup_width, 0);
        //PopupWindowList高度
        mPupopHeight = typedArray.getDimensionPixelSize(R.styleable.NiceSpinner_popup_height, 0);
        //PopupWindowList 锚点控件垂直偏移量
        mPupopVerticalOffset = typedArray.getDimensionPixelSize(R.styleable.NiceSpinner_popup_vertical_offset, 0);
        //PopupWindowList 锚点控件水平偏移量
        mPupopHorizontalOffset = typedArray.getDimensionPixelSize(R.styleable.NiceSpinner_popup_horizontal_offset, 0);
        //PopupWindowList 入场动画
        mPopupAnim = typedArray.getResourceId(R.styleable.NiceSpinner_popup_anim, R.style.pop_animation);
        //PopupWindowList 出场动画
        //设置NiceSpinner中字体颜色
        setTextColor(textColor);
        //初始化PopupWindow
        initPopup(context);
        // 是否展示箭头
        isArrowHidden = typedArray.getBoolean(R.styleable.NiceSpinner_hideArrow, false);
        // 箭头颜色
        arrowDrawableTint = typedArray.getColor(R.styleable.NiceSpinner_arrowTint, getResources().getColor(android.R.color.black));
        // 箭头图片资源
        arrowDrawableResId = typedArray.getResourceId(R.styleable.NiceSpinner_arrowDrawable, R.drawable.arrow);
        // 这个是条目中数据的对齐方式
        horizontalAlignment = PopUpTextAlignment.fromId(typedArray.getInt(R.styleable.NiceSpinner_popupTextAlignment, PopUpTextAlignment.CENTER.ordinal()));
        // 从xml资源中获取数组 <string-array name="courses"><item>English</item></string-array>
        CharSequence[] entries = typedArray.getTextArray(R.styleable.NiceSpinner_entries);
        if (entries != null) {
            //开始设置数据了
            attachDataSource(Arrays.asList(entries));
        }
        typedArray.recycle();
        measureDisplayHeight();
    }

    /**
     * 初始化PopupWindow设置
     *
     * @param context
     */
    private void initPopup(Context context) {
        //创建ListPopupWindow
        popupWindow = new ListPopupWindow(context);
        //为PopupWindow设置背景色
        popupWindow.setBackgroundDrawable(getResources().getDrawable(mPopupBgColor));
        //设置动画为PopupWindow
        popupWindow.setAnimationStyle(mPopupAnim);
        //点击回调事件
        popupWindow.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // The selected item is not displayed within the list, so when the selected position is equal to
                // the one of the currently selected item it gets shifted to the next item.

                // selectedIndex:该值代表Adapter数据源中,上一次被选中的数据对应的list索引值.
                // ListPopupWindow中被选中的条目索引值position,大于或者selectedIndex时,
                // 将position处理之后,转为被选中条目所对应的数据在数据源list中对应的索引值.
                // 这个地方有点绕,可以举例子来理解,比如数据源中数据为{A,B,C,D}
                // 展示时NiceSpinner控件出现"A",此时ListPopupWindow中三个条目分别为{B,C,D}
                // 当点击ListPopupWindow中第0个条目时,该条目中数据"B"对应的list数据源中索引其实为1,也就是selectedIndex=1.
                // 而当再次展示ListPopupWindow时, 因为selectedIndex=1,ListPopupWindow条目为0依旧展示数据源中0索引对应的数据"A",NiceSpinner控件展示数据"B"
                // 但条目1展示的数据,看NiceSpinnerAdapter中getItem()方法可知道,此时应该展示list数据源中索引为2对应的值"C".
                if (position >= selectedIndex && position < adapter.getCount()) {
                    position++;
                }
                // selectedIndex 为当前选中数据在数据源中对应的list的索引值.
                selectedIndex = position;
                // 选中之后的回调方法, 将被选中数据的list索引传出去.
                if (onSpinnerItemSelectedListener != null) {
                    onSpinnerItemSelectedListener.onItemSelected(NiceSpinner.this, view, position, id);
                }
                // 将当前选中数据在数据源中对应的list的索引值,设置到NiceSpinnerBaseAdapter中
                adapter.setSelectedIndex(position);
                // 通过索引取得list中的数据,将数据展示到NiceSpinner上
                setTextInternal(adapter.getItemInDataset(position));
                // 选中数据之后让ListPopupWindow消失
                dismissDropDown();
            }
        });
        // 是否触摸别的地方PopupWindow消失
        popupWindow.setModal(true);
        // ListPopupWindow消失的回调
        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                // 如果箭头没有隐藏
                if (!isArrowHidden) {
                    // 此时箭头应该执行向下动画
                    animateArrow(false);
                }
            }
        });
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        //ListPopupWindow 设置宽高
        if (mPupopHeight == 0) {
            mPupopHeight = this.getHeight() * 4;
        }
        if (mPupopWidth == 0) {
            mPupopWidth = this.getWidth();
        }
        popupWindow.setWidth(mPupopWidth);
        popupWindow.setHeight(mPupopHeight);
    }

    // 当窗体销毁,结束动画
    @Override
    protected void onDetachedFromWindow() {
        if (arrowAnimator != null) {
            arrowAnimator.cancel();
        }
        super.onDetachedFromWindow();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
            // 创建时候初始化
            onVisibilityChanged(this, getVisibility());
        }
    }

    /**
     * 主要是初始化箭头,以及设置箭头在NiceSpinner右侧
     *
     * @param changedView
     * @param visibility
     */
    @Override
    protected void onVisibilityChanged(View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        arrowDrawable = initArrowDrawable(arrowDrawableTint);
        setArrowDrawableOrHide(arrowDrawable);
    }

    /**
     * 初始化箭头
     *
     * @param drawableTint
     * @return
     */
    private Drawable initArrowDrawable(int drawableTint) {
        if (arrowDrawableResId == 0) return null;
        Drawable drawable = ContextCompat.getDrawable(getContext(), arrowDrawableResId);
        if (drawable != null) {
            // Gets a copy of this drawable as this is going to be mutated by the animator
            drawable = DrawableCompat.wrap(drawable).mutate();
            if (drawableTint != Integer.MAX_VALUE && drawableTint != 0) {
                DrawableCompat.setTint(drawable, drawableTint);
            }
        }
        return drawable;
    }

    /**
     * 设置箭头是否展示 isArrowHidden fals 不展示 true 展示
     *
     * @param drawable
     */
    private void setArrowDrawableOrHide(Drawable drawable) {
        if (!isArrowHidden && drawable != null) {
            setCompoundDrawablesWithIntrinsicBounds(null, null, drawable, null);
        } else {
            setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
        }
    }

    /**
     * 如何获取控件默认颜色
     *
     * @param context
     * @return
     */
    private int getDefaultTextColor(Context context) {
        TypedValue typedValue = new TypedValue();
        context.getTheme()
                .resolveAttribute(android.R.attr.textColorPrimary, typedValue, true);
        TypedArray typedArray = context.obtainStyledAttributes(typedValue.data,
                new int[]{android.R.attr.textColorPrimary});
        int defaultTextColor = typedArray.getColor(0, Color.BLACK);
        typedArray.recycle();
        return defaultTextColor;
    }

    public Object getItemAtPosition(int position) {
        return adapter.getItemInDataset(position);
    }

    public Object getSelectedItem() {
        return adapter.getItemInDataset(selectedIndex);
    }

    public int getSelectedIndex() {
        return selectedIndex;
    }

    public void setArrowDrawable(@DrawableRes @ColorRes int drawableId) {
        arrowDrawableResId = drawableId;
        arrowDrawable = initArrowDrawable(R.drawable.arrow);
        setArrowDrawableOrHide(arrowDrawable);
    }

    public void setArrowDrawable(Drawable drawable) {
        arrowDrawable = drawable;
        setArrowDrawableOrHide(arrowDrawable);
    }

    private void setTextInternal(Object item) {
        if (selectedTextFormatter != null) {
            setText(selectedTextFormatter.format(item));
        } else {
            setText(item.toString());
        }
    }

    /**
     * Set the default spinner item using its index
     * 这个方法可以手动设置数据源中哪条数据可以自己展示到 NiceSpinner 上.
     *
     * @param position the item's position
     */
    public void setSelectedIndex(int position) {
        if (adapter != null) {
            if (position >= 0 && position <= adapter.getCount()) {
                // 更新Adapter中selectedIndex的值
                adapter.setSelectedIndex(position);
                //上一次选中的item条目对应的数据, 对应的list数据源中对应的索引值
                selectedIndex = position;
                //通过传入的索引值, 取出list中对应的数据
                setTextInternal(selectedTextFormatter.format(adapter.getItemInDataset(position)).toString());
            } else {
                throw new IllegalArgumentException("Position must be lower than adapter count!");
            }
        }
    }

    /**
     * 为 NiceSpinner 设置数据
     *
     * @param list
     * @param <T>
     */
    public <T> void attachDataSource(@NonNull List<T> list) {
        adapter = new NiceSpinnerAdapter<>(getContext(), list, textColor, backgroundSelector, spinnerTextFormatter, horizontalAlignment);
        setAdapterInternal(adapter);
    }

    public void setAdapter(ListAdapter adapter) {
        this.adapter = new NiceSpinnerAdapterWrapper(getContext(), adapter, textColor, backgroundSelector,
                spinnerTextFormatter, horizontalAlignment);
        setAdapterInternal(this.adapter);
    }

    public PopUpTextAlignment getPopUpTextAlignment() {
        return horizontalAlignment;
    }

    /**
     * 为 ListPopupWindow 设置Adapter
     *
     * @param adapter
     * @param <T>
     */
    private <T> void setAdapterInternal(NiceSpinnerBaseAdapter<T> adapter) {
        if (adapter.getCount() >= 0) {
            // If the adapter needs to be set again, ensure to reset the selected index as well
            //当重新设置Adapter的时候需要将所选数据对应的所以置为0
            selectedIndex = 0;
            popupWindow.setAdapter(adapter);
            setTextInternal(adapter.getItemInDataset(selectedIndex));
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // 如果该视图可用
        // 事件为Up事件
        if (isEnabled() && event.getAction() == MotionEvent.ACTION_UP) {
            // ListPopupWindow没有展示且adapter中有数据
            if (!popupWindow.isShowing() && adapter != null && adapter.getCount() > 0) {
                //展示ListPopupWindow
                showDropDown();
            } else {
                //隐藏
                dismissDropDown();
            }
        }
        return super.onTouchEvent(event);
    }

    /**
     * 箭头的动画
     *
     * @param shouldRotateUp
     */
    private void animateArrow(boolean shouldRotateUp) {
        int start = shouldRotateUp ? 0 : MAX_LEVEL;
        int end = shouldRotateUp ? MAX_LEVEL : 0;
        arrowAnimator = ObjectAnimator.ofInt(arrowDrawable, "level", start, end);
        arrowAnimator.setInterpolator(new LinearOutSlowInInterpolator());
        arrowAnimator.start();
    }

    /**
     * 隐藏ListPopupWindow
     * 箭头执行隐藏动画
     */
    public void dismissDropDown() {
        if (!isArrowHidden) {
            animateArrow(false);
        }
        popupWindow.dismiss();
    }

    /**
     * 展示ListPopupWindow
     * 箭头执行展示动画
     */
    public void showDropDown() {
        if (!isArrowHidden) {
            animateArrow(true);
        }
        //定位
        popupWindow.setAnchorView(this);
        //设置偏移量
        popupWindow.setHorizontalOffset(mPupopHorizontalOffset);
        popupWindow.setVerticalOffset(mPupopVerticalOffset);
        //展示
        popupWindow.show();
        final ListView listView = popupWindow.getListView();
        if (listView != null) {
            // 去除滑动条
            listView.setVerticalScrollBarEnabled(false);
            listView.setHorizontalScrollBarEnabled(false);
            listView.setVerticalFadingEdgeEnabled(false);
            listView.setHorizontalFadingEdgeEnabled(false);
            // 去除ListView阴影
            listView.setOverScrollMode(View.OVER_SCROLL_NEVER);
        }
    }


    /**
     * 获取屏幕高度
     */
    private void measureDisplayHeight() {
        displayHeight = getContext().getResources().getDisplayMetrics().heightPixels;
    }

    /**
     * 获取NiceSpinner控件在屏幕左上角的垂直高度
     * 该方法已经废弃
     *
     * @return
     */
    private int getParentVerticalOffset() {
        if (parentVerticalOffset > 0) {
            return parentVerticalOffset;
        }
        int[] locationOnScreen = new int[2];
        //获取到的是View左上顶点在屏幕中的绝对位置.(屏幕范围包括状态栏).
        getLocationOnScreen(locationOnScreen);
        return parentVerticalOffset = locationOnScreen[VERTICAL_OFFSET];
    }

    /**
     * 屏幕左上角坐标
     *
     * @return
     */
    private int verticalSpaceAbove() {
        return getParentVerticalOffset();
    }

    /**
     * 获取ListPopupWindow高度
     * 屏幕像素- 控件距离屏幕左上角距离-控件高度
     * 该方法废弃,不能用
     *
     * @return
     */
    @Deprecated
    private int verticalSpaceBelow() {
        return displayHeight - getParentVerticalOffset() - getMeasuredHeight();
    }

    private int getPopUpHeight() {
        return Math.max(verticalSpaceBelow(), verticalSpaceAbove());
    }

    public void setTintColor(@ColorRes int resId) {
        if (arrowDrawable != null && !isArrowHidden) {
            DrawableCompat.setTint(arrowDrawable, ContextCompat.getColor(getContext(), resId));
        }
    }

    public void setArrowTintColor(int resolvedColor) {
        if (arrowDrawable != null && !isArrowHidden) {
            DrawableCompat.setTint(arrowDrawable, resolvedColor);
        }
    }

    public void hideArrow() {
        isArrowHidden = true;
        setArrowDrawableOrHide(arrowDrawable);
    }

    public void showArrow() {
        isArrowHidden = false;
        setArrowDrawableOrHide(arrowDrawable);
    }

    public boolean isArrowHidden() {
        return isArrowHidden;
    }

    public void setSpinnerTextFormatter(SpinnerTextFormatter spinnerTextFormatter) {
        this.spinnerTextFormatter = spinnerTextFormatter;
    }

    public void setSelectedTextFormatter(SpinnerTextFormatter textFormatter) {
        this.selectedTextFormatter = textFormatter;
    }


    public void performItemClick(int position, boolean showDropdown) {
        if (showDropdown) showDropDown();
        setSelectedIndex(position);
    }

    /**
     * only applicable when popup is shown .
     *
     * @param view
     * @param position
     * @param id
     */
    public void performItemClick(View view, int position, int id) {
        showDropDown();
        final ListView listView = popupWindow.getListView();
        if (listView != null) {
            listView.performItemClick(view, position, id);
        }
    }

    public OnSpinnerItemSelectedListener getOnSpinnerItemSelectedListener() {
        return onSpinnerItemSelectedListener;
    }

    public void setOnSpinnerItemSelectedListener(OnSpinnerItemSelectedListener onSpinnerItemSelectedListener) {
        this.onSpinnerItemSelectedListener = onSpinnerItemSelectedListener;
    }
}

