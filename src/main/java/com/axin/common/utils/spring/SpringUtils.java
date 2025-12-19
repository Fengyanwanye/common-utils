package com.axin.common.utils.spring;

import com.axin.common.utils.StringUtils;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * Spring工具类
 * 
 * <p>提供便捷的方法来获取Spring容器中的Bean、环境配置等信息。</p>
 * 
 * <p>主要功能：</p>
 * <ul>
 *   <li>根据名称或类型获取Bean</li>
 *   <li>获取AOP代理对象</li>
 *   <li>获取当前激活的环境配置</li>
 *   <li>检查Bean是否存在</li>
 * </ul>
 * 
 * @author fuchuanxin
 * @version 1.0
 * @date 2025/9/23 12:34
 */
@Component
public class SpringUtils implements BeanFactoryPostProcessor, ApplicationContextAware {

    /**
     * Spring Bean工厂
     */
    private static ConfigurableListableBeanFactory beanFactory;
    
    /**
     * Spring应用上下文
     */
    private static ApplicationContext applicationContext;

    public SpringUtils() {
    }

    /**
     * 实现BeanFactoryPostProcessor接口，在Bean工厂初始化后设置beanFactory
     * 
     * @param beanFactory Spring Bean工厂
     * @throws BeansException Bean异常
     */
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        SpringUtils.beanFactory = beanFactory;
    }

    /**
     * 实现ApplicationContextAware接口，设置应用上下文
     * 
     * @param applicationContext Spring应用上下文
     * @throws BeansException Bean异常
     */
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        SpringUtils.applicationContext = applicationContext;
    }

    /**
     * 根据Bean名称获取Bean实例
     * 
     * @param name Bean名称
     * @return Bean实例
     * @throws BeansException 如果Bean不存在
     */
    public static <T> T getBean(String name) throws BeansException {
        return (T) beanFactory.getBean(name);
    }

    /**
     * 根据Bean类型获取Bean实例
     * 
     * @param clz Bean类型
     * @return Bean实例
     * @throws BeansException 如果Bean不存在或存在多个相同类型的Bean
     */
    public static <T> T getBean(Class<T> clz) throws BeansException {
        return (T) beanFactory.getBean(clz);
    }

    /**
     * 检查容器中是否包含指定名称的Bean
     * 
     * @param name Bean名称
     * @return 如果存在返回true，否则返回false
     */
    public static boolean containsBean(String name) {
        return beanFactory.containsBean(name);
    }

    /**
     * 判断指定名称的Bean是否为单例模式
     * 
     * @param name Bean名称
     * @return 如果是单例返回true，否则返回false
     * @throws NoSuchBeanDefinitionException 如果Bean不存在
     */
    public static boolean isSingleton(String name) throws NoSuchBeanDefinitionException {
        return beanFactory.isSingleton(name);
    }

    /**
     * 获取指定名称Bean的类型
     * 
     * @param name Bean名称
     * @return Bean的Class类型
     * @throws NoSuchBeanDefinitionException 如果Bean不存在
     */
    public static Class<?> getType(String name) throws NoSuchBeanDefinitionException {
        return beanFactory.getType(name);
    }

    /**
     * 获取指定名称Bean的所有别名
     * 
     * @param name Bean名称
     * @return Bean的别名数组
     * @throws NoSuchBeanDefinitionException 如果Bean不存在
     */
    public static String[] getAliases(String name) throws NoSuchBeanDefinitionException {
        return beanFactory.getAliases(name);
    }

    /**
     * 获取AOP代理对象
     * 
     * <p>在AOP环境下，获取当前正在执行的代理对象。</p>
     * <p>需要在@EnableAspectJAutoProxy注解中设置exposeProxy=true才能使用。</p>
     * 
     * @param invoker 被代理的对象
     * @return AOP代理对象
     */
    public static <T> T getAopProxy(T invoker) {
        return (T) AopContext.currentProxy();
    }

    /**
     * 获取所有激活的环境配置
     * 
     * @return 激活的环境配置数组，如["dev", "local"]
     */
    public static String[] getActiveProfiles() {
        return applicationContext.getEnvironment().getActiveProfiles();
    }

    /**
     * 获取第一个激活的环境配置
     * 
     * @return 激活的环境配置，如"dev"，如果没有则返回null
     */
    public static String getActiveProfile() {
        String[] activeProfiles = getActiveProfiles();
        return StringUtils.isNotEmpty(activeProfiles) ? activeProfiles[0] : null;
    }
}
