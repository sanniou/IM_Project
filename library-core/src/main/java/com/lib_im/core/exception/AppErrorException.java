package com.lib_im.core.exception;

/**
 * 用户操作失误的异常
 */
public class AppErrorException extends RuntimeException{
  public AppErrorException(String message) {
    super(message);
  }
}
