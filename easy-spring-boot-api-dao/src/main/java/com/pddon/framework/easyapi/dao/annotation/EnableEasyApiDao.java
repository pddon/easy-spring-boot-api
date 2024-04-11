/**  
* Title EnableEasyApiDao.java
* Description  
* @author Allen
* @date Mar 13, 2024
* @version 1.0.0
* site: pddon.cn
*/ 
package com.pddon.framework.easyapi.dao.annotation;

import com.pddon.framework.easyapi.dao.config.DruidConfig;
import com.pddon.framework.easyapi.dao.config.MybatisPlusConfig;
import com.pddon.framework.easyapi.dao.config.RedisConfig;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
//添加mybatis插件支持，包含乐观锁、逻辑删除、分页等插件，引入redis配置、druid连接池配置
//@Import({MybatisPlusConfig.class, RedisConfig.class, DruidConfig.class})
@Import({MybatisPlusConfig.class, RedisConfig.class}) //支持多数据源后移除单druid数据源配置
public @interface EnableEasyApiDao {
    
}
