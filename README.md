# Nice Spinner 
- 依赖
    ```groovy
    implementation 'com.yey.nice.spinner:library:0.0.9' 
    ```
- 属性

|名称|类型|作用|  
|---|----|---|
| arrowTint | color | 箭头颜色|
| hideArrow | boolean| 箭头是否隐藏|
| arrowDrawable| reference或color| 箭头图片资源|
|backgroundSelector|integer|下拉框条目的背景色,和NiceSpinner的背景色|
|textTint|color|下拉框中内容颜色|
|popupTextAlignment|enum|下拉框中,内容水平对齐的方式|
|entries|reference|通过xml资源引用的方式为控件传入数据源|
|popup_bg_color|reference|下拉框背景颜色|
|popup_width|dimension,reference|下拉框宽度|
|popup_height|dimension,reference|下拉框高度|
|popup_horizontal_offset|dimension,reference|下拉框相对锚点水平偏移量|
|popup_vertical_offset|dimension,reference|下拉框相对锚点垂直偏移量|
|popup_anim|dimension,reference|入场动画资源|

- 使用
    ```xml
    <org.angmarch.views.NiceSpinner
        android:id="@+id/ns"
        android:layout_width="0dp"
        android:layout_height="match_parent"
        app:entries="@array/courses"
        app:textTint="@color/colorPrimary"
        app:arrowTint="@color/colorPrimary"
        app:popup_height="100dp"    
        app:popup_bg_color="@color/colorPrimaryDark"  
        app:popup_horizontal_offset="10dp"
        app:popup_vertical_offset="10dp" />
    ```
    ```java
    //设置资源时也可以使用entries这个属性来为控件设置数据源
    List<String> dataset = new LinkedList<>(Arrays.asList("One", "Two", "Three", "Four", "Five"));
    //设置数据源  
    spinner.attachDataSource(dataset);
    //当下拉框中某个条目被选中会触发这个回调方法  
    spinner.setOnSpinnerItemSelectedListener(new OnSpinnerItemSelectedListener() {
        @Override
        public void onItemSelected(NiceSpinner parent, View view, int position, long id) {
            String item = parent.getItemAtPosition(position).toString();
            Toast.makeText(MainActivity.this, "Selected: " + item, Toast.LENGTH_SHORT).show();
        }
    });
    ```
- 实例

    ![alt tag](nice-spinner.gif)  
