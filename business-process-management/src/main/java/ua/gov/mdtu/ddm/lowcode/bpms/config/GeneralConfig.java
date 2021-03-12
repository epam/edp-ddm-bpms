package ua.gov.mdtu.ddm.lowcode.bpms.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.stream.Stream;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.boot.autoconfigure.orm.jpa.JpaBaseConfiguration;
import org.springframework.boot.jdbc.DatabaseDriver;
import org.springframework.boot.json.JacksonJsonParser;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Import;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.jdbc.support.DatabaseStartupValidator;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import ua.gov.mdtu.ddm.general.integration.ceph.config.CephConfig;

@Configuration
@EnableAspectJAutoProxy
@Import(CephConfig.class)
public class GeneralConfig {

    @Bean
    public FilterRegistrationBean<CorsFilter> processCorsFilter() {
        var source = new UrlBasedCorsConfigurationSource();
        var config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.addAllowedOrigin("*");
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");
        source.registerCorsConfiguration("/**", config);

        var bean = new FilterRegistrationBean<>(new CorsFilter(source));
        bean.setOrder(0);
        return bean;
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public JacksonJsonParser jacksonJsonParser(ObjectMapper objectMapper) {
        return new JacksonJsonParser(objectMapper);
    }

    @Bean
    public ResourceBundleMessageSource messageSource() {
        ResourceBundleMessageSource rs = new ResourceBundleMessageSource();
        rs.setUseCodeAsDefaultMessage(true);
        rs.setDefaultEncoding("UTF-8");
        rs.setBasename("lang/messages");
        return rs;
    }

    @Bean
    public DatabaseStartupValidator databaseStartupValidator(DataSource dataSource,
        @Value("${database-startup-validator.interval:10}") int interval,
        @Value("${database-startup-validator.timeout:100}") int timeout) {
        var dsv = new DatabaseStartupValidator();
        dsv.setInterval(interval);
        dsv.setTimeout(timeout);
        dsv.setDataSource(dataSource);
        dsv.setValidationQuery(DatabaseDriver.POSTGRESQL.getValidationQuery());
        return dsv;
    }

    @Bean
    public static BeanFactoryPostProcessor dependsOnPostProcessor() {
        return bf -> {
            String[] jpa = bf.getBeanNamesForType(JpaBaseConfiguration.class);
            Stream.of(jpa)
                .map(bf::getBeanDefinition)
                .forEach(it -> it.setDependsOn("databaseStartupValidator"));
        };
    }
}
