/**  
 * Title MybatisPlusConfig.java  
 * Description  
 * @author allen
 * @date Mar 14, 2019
 * @version 1.0.0
 * site: www.pddon.cn
 */
package com.pddon.framework.easyapi.dao.config;

import com.baomidou.mybatisplus.autoconfigure.MybatisPlusProperties;
import com.baomidou.mybatisplus.core.injector.ISqlInjector;
import com.baomidou.mybatisplus.core.parser.ISqlParser;
import com.baomidou.mybatisplus.extension.injector.LogicSqlInjector;
import com.baomidou.mybatisplus.extension.plugins.IllegalSQLInterceptor;
import com.baomidou.mybatisplus.extension.plugins.OptimisticLockerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.PaginationInterceptor;
import com.baomidou.mybatisplus.extension.plugins.tenant.TenantHandler;
import com.baomidou.mybatisplus.extension.plugins.tenant.TenantSqlParser;
import com.pddon.framework.easyapi.context.RequestContext;
import com.pddon.framework.easyapi.dao.interceptor.DataPermissionInterceptor;
import com.pddon.framework.easyapi.dao.interceptor.InsertCommentInterceptor;
import com.pddon.framework.easyapi.dao.interceptor.RenameCacheKeyInterceptor;
import com.pddon.framework.easyapi.dao.interceptor.UpdateCommentInterceptor;
import com.pddon.framework.easyapi.dao.tenant.EasyApiTenantHandler;
import com.pddon.framework.easyapi.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.StringValue;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
@MapperScan({"com.**.dao.mapper", "org.**.dao.mapper"})
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
     * 分页和多租户插件
     * @return
	 * @author allen
     */
    @Bean
    public PaginationInterceptor paginationInterceptor() {
    	log.info("Init Pagination Plugin ...");
		PaginationInterceptor paginationInterceptor = new PaginationInterceptor().setDialectType("mysql");
		// 创建SQL解析器，会对sql进行拦截处理。
		TenantSqlParser tenantSqlParser = new TenantSqlParser();
		tenantSqlParser.setTenantHandler(new EasyApiTenantHandler());

		// 创建SQL解析器集合
		List<ISqlParser> sqlParserList = new ArrayList<>();
		sqlParserList.add(tenantSqlParser);

		// 设置SQL解析器集合
		paginationInterceptor.setSqlParserList(sqlParserList);
        return paginationInterceptor;
    }

	@Bean
	public DataPermissionInterceptor dataPermissionInterceptor() {
		log.info("Init DataPermissionInterceptor Plugin ...");
		return new DataPermissionInterceptor();
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
