package com.nowcoder.configuration;

import com.nowcoder.interceptor.PassportInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

/**
 * 现在我们要增加一个拦截器
 * 这个配置可以通过实现，或扩展某个接口来实现
 */
@Component
public class WendaWebConfiguration extends WebMvcConfigurerAdapter {

    @Autowired
    PassportInterceptor passportInterceptor;//引用这个拦截器

    //注册点
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(passportInterceptor);   //在系统初始化的时候，加一个这样的拦截器.此时才是把这个拦截器注册到整个链路上。这样所有的请求都会先经过拦截器，这样就可以把用户给过滤出来了

        super.addInterceptors(registry);    //只要我定义了这样一个Component，Spring在初始化的时候，就会把这个拦截器回调到这个地方。我们可以在这个函数，注册一个我们自己的拦截器。
    }
}
