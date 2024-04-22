package com.pddon.framework.easyapi.validate;

import com.pddon.framework.easyapi.LanguageTranslateManager;
import com.pddon.framework.easyapi.context.RequestContext;
import com.pddon.framework.easyapi.utils.IOUtils;
import com.pddon.framework.easyapi.utils.SpringBeanUtil;
import com.pddon.framework.easyapi.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.ReflectionUtils;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @ClassName: AtLeastOneConstraintValidator
 * @Description:
 * @Author: Allen
 * @Date: 2024-04-22 22:04
 * @Addr: https://pddon.cn
 */
@Slf4j
public class AtLeastOneConstraintValidator implements ConstraintValidator<AtLeastOne, Object> {

    @Override
    public void initialize(AtLeastOne constraintAnnotation) {
        //
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        try {
            // 获取注解所在的字段的类
            Class<?> beanClass = value.getClass();
            Map<String, List<List<Object>>> map = new HashMap<>();
            for (Field field : beanClass.getDeclaredFields()) {
                AtLeastOneField atLeastOneField = field.getAnnotation(AtLeastOneField.class);
                if(atLeastOneField == null){
                    continue;
                }
                String group = atLeastOneField.group();
                if(!map.containsKey(group)){
                    map.put(group, new ArrayList<>());
                }
                ReflectionUtils.makeAccessible(field);
                map.get(group).add(Arrays.asList((Object)field.getName(), ReflectionUtils.getField(field, value)));
            }
            for(List<List<Object>> list : map.values()){
                List<String> keys = list.stream()
                        .filter(data -> StringUtils.isBlank(data.get(1)))
                        .map(data -> (String)data.get(0)).collect(Collectors.toList());
                if(list.size() == keys.size()){
                    //不能全为空
                    LanguageTranslateManager languageTranslateManager = SpringBeanUtil.getBean(LanguageTranslateManager.class);
                    String locale = RequestContext.getContext().getLocale();
                    context.disableDefaultConstraintViolation();
                    context.buildConstraintViolationWithTemplate(languageTranslateManager.get("多选一必填分组参数不能全为空!", locale))
                            .addPropertyNode(String.join(",", keys))
                            .addConstraintViolation();
                    return false;
                }
            }
        } catch (Exception e) {
            // 字段访问异常，校验失败
            log.warn(IOUtils.getThrowableInfo(e));
            return false;
        }
        // 校验成功
        return true;
    }
}
