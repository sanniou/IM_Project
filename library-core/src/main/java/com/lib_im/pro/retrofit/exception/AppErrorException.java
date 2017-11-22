package com.lib_im.pro.retrofit.exception;

/**
 * 用户操作失误的异常
 */
public class AppErrorException extends NetRequestException{
  public AppErrorException(String message) {
    super(message);
  }
}
