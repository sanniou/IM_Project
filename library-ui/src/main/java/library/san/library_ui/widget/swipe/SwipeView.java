package library.san.library_ui.widget.swipe;

public interface SwipeView {
  int STATE_REFRESH = 0; //刷新中
  int STATE_NORMAL = 1; //普通状态
  int STATE_PULLING = 2; //提示刷新
  int STATE_COMPLETE = 3; //刷新完成
  int STATE_FAILED = 4; //刷新失败

  void setRefreshing();

  void setNormal();

  void setPulling();

  void setComplete();

  void setFailed();

  /**
   * 得到下拉的距离计算状态
   *
   * @param totalHeight SwipeRefreshLayout统计的head高度
   * @param offset 需要处理的滑动距离
   */
  float scrollOffset(float totalHeight, int offset);

  void setOnRefreshListener(OnRequestListener listener);

  boolean isRefreshing();

  /**
   * 结束滚动时被调用
   */
  void stopScroll(float totalHeight);

  void setCanRequest(boolean canLoadMore);

  interface OnRequestListener {
    void onRequest();
  }
}
