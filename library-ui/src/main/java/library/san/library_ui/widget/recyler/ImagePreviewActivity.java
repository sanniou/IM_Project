package library.san.library_ui.widget.recyler;//package com.lib_im.pro.ui.widget.recyler;
//
//import android.content.Intent;
//import android.support.v7.widget.LinearLayoutManager;
//import android.support.v7.widget.PagerSnapHelper;
//import android.support.v7.widget.RecyclerView;
//import android.widget.ImageView;
//import com.alibaba.android.arouter.facade.annotation.Route;
//import com.alibaba.android.arouter.launcher.ARouter;
//import com.google.gson.reflect.TypeToken;
//import com.lib_im.pro.R;
//import com.lib_im.pro.ui.base.PermissionActivity;
//import com.lib_im.pro.ui.widget.swipe.LSwipeRefreshLayout;
//import library.san.library_ui.utils.ImageLoader;
//import library.san.library_ui.utils.JsonUtils;
//import java.util.Arrays;
//import java.util.List;
//
//@Route(path = ImagePreviewActivity.ROUTE_PATH)
//public class ImagePreviewActivity extends PermissionActivity {
//
//    public static final String KEY_IMAGE_INFO = "keyImageInfo";
//    public static final String KEY_IMAGE_POSITION = "keyImagePosition";
//    public static final String ROUTE_PATH = "/image/preview";
//    private BaseRecyclerAdapter<String> mAdapter;
//    private List<String> images;
//    private int mPosition;
//    private RecyclerView mRecyclerView;
//
//    @Override
//    protected void initTitle(TitleView view) {
//        view.setTitle("图片预览");
//    }
//
//    @Override
//    protected void initRecycler(RecyclerView view) {
//        mRecyclerView = view;
//        PagerSnapHelper snapHelper = new PagerSnapHelper();
//        snapHelper.attachToRecyclerView(view);
//
//        ((LinearLayoutManager) view.getLayoutManager())
//                .setOrientation(LinearLayoutManager.HORIZONTAL);
//        mAdapter = new BaseRecyclerAdapter<String>(this, R.layout.item_iamge) {
//
//            @Override
//            public void onBindHolder(LViewHolder holder, int position) {
//                String imageInfo = getItem(position);
//                ImageLoader.load(ImagePreviewActivity.this,
//                        (ImageView) holder.getView(R.id.item_image), imageInfo);
//            }
//        };
//        view.setAdapter(mAdapter);
//
//    }
//
//    @Override
//    protected void initSwipe(LSwipeRefreshLayout swipe) {
//        super.initSwipe(swipe);
//        swipe.setEnabled(false);
//    }
//
//    @Override
//    protected void initData() {
//        String jsonString = getIntent().getStringExtra(KEY_IMAGE_INFO);
//        mPosition = getIntent().getIntExtra(KEY_IMAGE_POSITION, 0);
//        images = JsonUtils.fromJson(jsonString, new TypeToken<List<String>>() {
//        }.getType());
//        mAdapter.setData(images);
//        mAdapter.notifyDataSetChanged();
//        mRecyclerView.scrollToPosition(mPosition);
//    }
//
//    public static void imagePreview(List<String> images, int index) {
//        ARouter.getInstance()
//               .build(ImagePreviewActivity.ROUTE_PATH)
//               .withString(ImagePreviewActivity.KEY_IMAGE_INFO, JsonUtils.toJson(images))
//               .withInt(ImagePreviewActivity.KEY_IMAGE_POSITION, index)
//               .withFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION | Intent.FLAG_ACTIVITY_NEW_TASK)
//               .navigation();
//    }
//
//    public static void imagePreview(List<String> images) {
//        imagePreview(images, 0);
//    }
//
//    public static void imagePreview(String... images) {
//        imagePreview(0, images);
//    }
//
//    public static void imagePreview(int position, String... images) {
//        imagePreview(Arrays.asList(images), position);
//    }
//
//    @Override
//    public void finishAfterTransition() {
//        finish();
//    }
//
//    @Override
//    public void finish() {
//        super.finish();
//        overridePendingTransition(0, 0);
//    }
//
//    @Override
//    public void onBackPressed() {
//        super.onBackPressed();
//    }
//}
