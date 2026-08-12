package hhsc.kangnasi.xyz.ustscampusservices.config;

import org.mybatis.spring.mapper.MapperFactoryBean;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class NativeAotCompatibilityConfig {

    @Bean
    static BeanFactoryPostProcessor nativeAotCompatibilityPostProcessor() {
        return new NativeAotCompatibilityPostProcessor();
    }

    private static final class NativeAotCompatibilityPostProcessor implements BeanFactoryPostProcessor {

        @Override
        public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
            ClassLoader classLoader = beanFactory.getBeanClassLoader();
            for (String beanName : beanFactory.getBeanDefinitionNames()) {
                BeanDefinition beanDefinition = beanFactory.getBeanDefinition(beanName);
                if (!(beanDefinition instanceof AbstractBeanDefinition abstractBeanDefinition)
                        || !isMapperFactoryBean(abstractBeanDefinition)) {
                    continue;
                }
                fixMapperFactoryBeanConstructorArgument(abstractBeanDefinition, classLoader);
            }
        }

        private void fixMapperFactoryBeanConstructorArgument(
                AbstractBeanDefinition beanDefinition,
                ClassLoader classLoader
        ) {
            Object mapperInterface = beanDefinition.getPropertyValues().get("mapperInterface");
            Class<?> mapperInterfaceClass = resolveMapperInterface(mapperInterface, classLoader);
            if (mapperInterfaceClass != null) {
                beanDefinition.getConstructorArgumentValues().clear();
                beanDefinition.getConstructorArgumentValues().addIndexedArgumentValue(0, mapperInterfaceClass);
            }
        }

        private boolean isMapperFactoryBean(AbstractBeanDefinition beanDefinition) {
            if (beanDefinition.hasBeanClass()) {
                return MapperFactoryBean.class.equals(beanDefinition.getBeanClass());
            }
            return MapperFactoryBean.class.getName().equals(beanDefinition.getBeanClassName());
        }

        private Class<?> resolveMapperInterface(Object mapperInterface, ClassLoader classLoader) {
            if (mapperInterface instanceof Class<?> mapperInterfaceClass) {
                return mapperInterfaceClass;
            }
            if (mapperInterface instanceof String mapperInterfaceName) {
                try {
                    return Class.forName(mapperInterfaceName, false, classLoader);
                } catch (ClassNotFoundException ignored) {
                    return null;
                }
            }
            return null;
        }
    }
}
