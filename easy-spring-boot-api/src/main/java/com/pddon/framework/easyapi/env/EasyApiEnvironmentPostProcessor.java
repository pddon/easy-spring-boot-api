/**  
 * Title EasyApiEnvironmentPostProcessor.java
 * Description  
 * @author danyuan
 * @date Nov 29, 2020
 * @version 1.0.0
 * site: pddon.cn
 */
package com.pddon.framework.easyapi.env;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.pddon.framework.easyapi.consts.DefaultConfigPropertiesValue;
import com.pddon.framework.easyapi.consts.DefaultConfigValueMode;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.AbstractEnvironment;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.util.StringUtils;

public class EasyApiEnvironmentPostProcessor implements
		EnvironmentPostProcessor {

	private static final String OPEN_LOG_KEY="easyapi.printAllProperties";
	
	private static final String DEFAULT_ENV_CONFIG = "defaultEnvConfig";
	@Override
	public void postProcessEnvironment(ConfigurableEnvironment environment,
			SpringApplication application) {
		
		Map<String,Object> defaultConfigs = DefaultConfigPropertiesValue.getAllDefaultConfigs();
		
        boolean printLog = false;
        String openLogValue = environment.getProperty(OPEN_LOG_KEY);
        if(!StringUtils.isEmpty(openLogValue)){
        	printLog = Boolean.valueOf(openLogValue);
        }
        if(printLog){
        	System.out.println("#############################配置信息##############################");
        }		
		for (Iterator<?> it = ((AbstractEnvironment) environment)
				.getPropertySources().iterator(); it.hasNext();) {
			PropertySource<?> propertySource = (PropertySource<?>) it.next();
			//拼接默认配置到用户自定义配置上
			Map<String,Object> modifyMap = new HashMap<>();
			for(DefaultConfigPropertiesValue config : DefaultConfigPropertiesValue.values()){
				if(propertySource.containsProperty(config.getKey()) && !DefaultConfigValueMode.OPTION.equals(config.getMode())){
					modifyMap.put(config.getKey(), config.getComposeValue(propertySource.getProperty(config.getKey())));
				}
			}
			if(modifyMap.size() > 0){
				//批量修改属性值
				updateStringPropertys(environment, propertySource, modifyMap);
			}			
				
			// 遍历每个配置来源中的配置项
			if (propertySource instanceof EnumerablePropertySource) {
				for (String name : ((EnumerablePropertySource<?>) propertySource)
						.getPropertyNames()) {
					if(defaultConfigs.containsKey(name)){
						//如果存在配置就移除默认配置
						defaultConfigs.remove(name);
					}
					if(printLog){
						/*
						 * 由于每个配置来源可能配置了同一个配置项，存在相互覆盖的情况，为了保证获取到的值与通过@Value获取到的值一致，
						 * 需要通过env.getProperty(name)获取配置项的值。
						 */
						System.out.println(name + " = "
								+ environment.getProperty(name));
			        }				
				}
			}
		}
		//最后填充默认配置信息
		if(!environment.getPropertySources().contains(DEFAULT_ENV_CONFIG) && defaultConfigs.size() > 0){
			MapPropertySource defaultEnvConfig = new MapPropertySource(DEFAULT_ENV_CONFIG, defaultConfigs);
			environment.getPropertySources().addLast(defaultEnvConfig);
			if(printLog){//打印配置信息
				defaultConfigs.forEach((key,value)->{
					System.out.println(key + " = "
							+ value.toString());
				});
			}
		}		
		if(printLog){
			System.out.println("###############################################################");
        }		
	}

	/**
	 * 批量修改配置信息
	 * @author danyuan
	 */
	private void updateStringPropertys(ConfigurableEnvironment environment, PropertySource<?> source, Map<String, Object> params){
        Map<String,Object> map = new HashMap<>();
        if(source instanceof EnumerablePropertySource){
        	for (String name : ((EnumerablePropertySource<?>) source).getPropertyNames()) {
    			map.put(name, environment.getProperty(name));			
    		}
            //动态修改配置
            params.forEach((k, v) -> {
                map.replace(k , v);
            });
            environment.getPropertySources().replace(source.getName(), new MapPropertySource(source.getName(), map));	
        }        
	}

}
