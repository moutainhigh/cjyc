package com.cjyc.customer.api.config;

import com.cjyc.customer.api.interceptor.HeaderInterceptor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import springfox.documentation.spring.web.SpringfoxWebMvcConfiguration;

/**
 * 资源处理规则配置
 * @Author JPG
 * @Date 2019/7/12 9:06
 * @Param
 */
@Configuration
@ConditionalOnClass(SpringfoxWebMvcConfiguration.class)
@ComponentScan("com.cjyc.customer.api.controller")
public class ResourcesHandlerConfiguration implements WebMvcConfigurer {

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(new HeaderInterceptor())
				.addPathPatterns("/**")
				.excludePathPatterns("/swagger-resources/**", "/webjars/**", "/v2/**", "/swagger-ui.html/**","/doc.html");

	}

	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		registry.addResourceHandler("doc.html").addResourceLocations("classpath:/META-INF/resources/");
		registry.addResourceHandler("/webjars/**").addResourceLocations("classpath:/META-INF/resources/webjars/");
	}
}