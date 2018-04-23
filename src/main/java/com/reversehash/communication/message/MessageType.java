package com.reversehash.communication.message;

//import java.lang.annotation.ElementType;
//import java.lang.annotation.Target;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface MessageType {
    int type();
    int version();
}
