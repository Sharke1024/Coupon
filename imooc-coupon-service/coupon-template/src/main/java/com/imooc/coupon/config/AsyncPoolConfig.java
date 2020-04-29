package com.imooc.coupon.config;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.lang.reflect.Method;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 *  <h1>自定义异步任务线程池</h1>
 * @Author DL_Wu
 * @Date 2020/4/29 15:59
 * @Version 1.0
 */
@Configuration
@EnableAsync
@Slf4j
public class AsyncPoolConfig implements AsyncConfigurer {

    /**
     * <h2>异步任务线程执行</h2>
     * @return
     */
    @Bean
    @Override
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        executor.setCorePoolSize(10);  //核心线程池数量
        executor.setMaxPoolSize(20);    //最大线程池数量
        executor.setQueueCapacity(20);  //队列容量
        executor.setKeepAliveSeconds(60);   //最长生存时间
        executor.setThreadNamePrefix("ImoocAsync_");    //线程名称前缀

        executor.setWaitForTasksToCompleteOnShutdown(true); //任务关闭线程池是否退出
        executor.setAwaitTerminationSeconds(60);    //任务关闭最长等待时间

        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy()); //拒绝策略

        return executor;
    }

    /**
     * <h2>定义异步任务处理类</h2>
     * @return
     */
    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return new AsyncExceptionHandler();
    }

    @SuppressWarnings("all")
    class AsyncExceptionHandler implements AsyncUncaughtExceptionHandler{

        @Override
        public void handleUncaughtException(Throwable throwable, Method method, Object... objects) {
            throwable.printStackTrace();
            log.error("AsyncError: {}, Method: {}, Param: {}",
                    throwable.getMessage(), method.getName(),
                    JSON.toJSONString(objects));

            //TODO 发送邮件或短信, 做进一步的处理
        }
    }

}
