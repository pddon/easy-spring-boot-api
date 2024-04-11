/**  
 * Title MybatisPlusConfig.java  
 * Description  
 * @author allen
 * @date Mar 14, 2019
 * @version 1.0.0
 * site: www.pddon.cn
 */
package com.pddon.framework.easyapi.dao.config;

import com.baomidou.mybatisplus.core.injector.ISqlInjector;
import com.baomidou.mybatisplus.extension.injector.LogicSqlInjector;
import com.baomidou.mybatisplus.extension.plugins.IllegalSQLInterceptor;
import com.baomidou.mybatisplus.extension.plugins.OptimisticLockerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.PaginationInterceptor;
import com.pddon.framework.easyapi.dao.interceptor.InsertCommentInterceptor;
import com.pddon.framework.easyapi.dao.interceptor.RenameCacheKeyInterceptor;
import com.pddon.framework.easyapi.dao.interceptor.UpdateCommentInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class MybatisPlusConfig {
	/**
	 * 逻辑删除插件
	 * @return
	 * @author allen
	 */
	@Bean
	public ISqlInjector sqlInjector() {
		log.info("Init LogicDelete Plugin ...");
		return new LogicSqlInjector();
	}
	/**
     * 分页插件
     * @return
	 * @author allen
     */
    @Bean
    public PaginationInterceptor paginationInterceptor() {
    	log.info("Init Pagination Plugin ...");
        return new PaginationInterceptor().setDialectType("mysql");
    }
    /**
     * 乐观锁插件
     * @return
     * @author allen
     */
    @Bean
    public OptimisticLockerInterceptor optimisticLockerInterceptor() {
    	log.info("Init OptimisticLocker Plugin ...");
        return new OptimisticLockerInterceptor();
    }
	/**
	 * 非法SQL拦截
	 * @return
	 * @author allen
	 */
	//@Bean
	public IllegalSQLInterceptor illegalSQLInterceptor() {
		log.info("Init IllegalSQLInterceptor Plugin ...");
		return new IllegalSQLInterceptor();
	}

	@Bean
	public UpdateCommentInterceptor updateCommentInterceptor() {
		log.info("Init updateCommentInterceptor Interceptor ...");
		return new UpdateCommentInterceptor();
	}

	@Bean
	public InsertCommentInterceptor insertCommentInterceptor() {
		log.info("Init insertCommentInterceptor Interceptor ...");
		return new InsertCommentInterceptor();
	}

	//@Bean
	public RenameCacheKeyInterceptor renameCacheKeyInterceptor() {
		log.info("Init renameCacheKeyInterceptor Interceptor ...");
		return new RenameCacheKeyInterceptor();
	}
}
