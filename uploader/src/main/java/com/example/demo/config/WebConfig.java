    package com.example.demo.config;

    import com.example.demo.filters.FileFilter;
    import org.springframework.boot.web.servlet.FilterRegistrationBean;
    import org.springframework.context.annotation.Bean;
    import org.springframework.context.annotation.Configuration;
    import org.springframework.web.multipart.support.MultipartFilter;

    @Configuration
    public class WebConfig {


        @Bean
        public FilterRegistrationBean<MultipartFilter> multipartFilter() {
            FilterRegistrationBean<MultipartFilter> registration = new FilterRegistrationBean<>();
            registration.setFilter(new MultipartFilter());
            registration.addUrlPatterns("/blob/upload"); // only your upload path
            registration.setOrder(0);
            return registration;
        }

        @Bean
        public FilterRegistrationBean<FileFilter> fileFilterRegistrationBean() {
            FilterRegistrationBean<FileFilter> registrationBean = new FilterRegistrationBean<>();
            registrationBean.setFilter(new FileFilter());
            registrationBean.addUrlPatterns("/blob/upload");
            registrationBean.setOrder(1);
            return registrationBean;
        }
    }

