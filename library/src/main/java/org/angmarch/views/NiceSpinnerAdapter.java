package org.angmarch.views;

import android.content.Context;

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
public class NiceSpinnerAdapter<T> extends NiceSpinnerBaseAdapter {

    private final List<T> items;

    NiceSpinnerAdapter(
            Context context,
            List<T> items,
            int textColor,
            int backgroundSelector,
            SpinnerTextFormatter spinnerTextFormatter,
            PopUpTextAlignment horizontalAlignment
    ) {
        super(context, textColor, backgroundSelector, spinnerTextFormatter, horizontalAlignment);
        this.items = items;
    }

    @Override
    public int getCount() {
        //展示的条目个数永远都比源数据长度少一个,因为其中一个数据被展示到了NiceSpinner控件上.
        return items.size() - 1;
    }

    @Override
    public T getItem(int position) {
        // selectedIndex:该值代表Adapter数据源中,上一次被选中的数据对应的list索引值.
        // 假如,当ListPopupWindow展示第1条目时,上一次所选中selectedIndex =1,
        // 那么ListPopupWindow第1条目应该展示数据源list中索引为2的数据.
        if (position >= selectedIndex) {
            return items.get(position + 1);
        } else {
            return items.get(position);
        }
    }
    //通过传入索引取出数据源中的数据并返回
    @Override
    public T getItemInDataset(int position) {
        return items.get(position);
    }
}