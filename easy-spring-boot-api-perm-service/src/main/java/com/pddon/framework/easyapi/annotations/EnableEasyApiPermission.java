/**  
* Title EnableEasyApiPermission.java
* Description  
* @author Allen
* @date Mar 13, 2024
* @version 1.0.0
* site: pddon.cn
*/ 
package com.pddon.framework.easyapi.annotations;

import com.pddon.framework.easyapi.config.UserSecurityConfigurer;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Import({UserSecurityConfigurer.class})
public @interface EnableEasyApiPermission {
    
}
