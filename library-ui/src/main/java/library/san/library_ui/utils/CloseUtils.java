package library.san.library_ui.utils;

import java.io.Closeable;
import java.io.IOException;

/**
 * author : jichang
 * time   : 2017/06/30
 * desc   :
 * version: 1.0
 */
public final class CloseUtils {

  private CloseUtils() {
    throw new UnsupportedOperationException("u can't instantiate me...");
  }

  /**
   * 关闭IO
   *
   * @param closeables closeables
   */
  public static void closeIO(final Closeable... closeables) {
    if (closeables == null) {
      return;
    }
    for (Closeable closeable : closeables) {
      if (closeable != null) {
        try {
          closeable.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
  }
}