package com.trusthold.shared;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Guard {

  public static<T> T requireNotNull(T value, RuntimeException exception){

    log.debug("Checking if value is null");
    if(value == null){
      throw exception;
    }
    return value;
  }
}
