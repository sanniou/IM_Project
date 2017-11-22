package library.san.library_ui.retrofit.exception;

/**
 * 接口请求 code 失败的异常
 */
public class ApiErrorException extends NetRequestException {
  public ApiErrorException(String message) {
    super(message);
  }
}
