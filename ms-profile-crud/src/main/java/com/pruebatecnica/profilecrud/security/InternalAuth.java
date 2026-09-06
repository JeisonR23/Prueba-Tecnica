package com.pruebatecnica.profilecrud.security;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.annotation.ElementType;
import jakarta.ws.rs.NameBinding;
import java.lang.annotation.RetentionPolicy;


@NameBinding
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})

public @interface InternalAuth {
    
}
