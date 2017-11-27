package com.lib_im.core.exception;

/**
 * 接口请求 code 为 0，返回 [] 或者 [null] 时 的异常
 */
public class EmptyDateException extends NetRequestException {

  public EmptyDateException(String message) {
    super(message);
  }
}
