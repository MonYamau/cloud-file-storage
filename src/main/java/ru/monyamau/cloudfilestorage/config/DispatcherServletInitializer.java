package ru.monyamau.cloudfilestorage.config;

import jakarta.servlet.MultipartConfigElement;
import jakarta.servlet.ServletRegistration;
import org.jspecify.annotations.Nullable;
import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer;

public class DispatcherServletInitializer extends AbstractAnnotationConfigDispatcherServletInitializer {
    private static final long UNIT_MB = 1024 * 1024;
    private static final long MAX_FILE_SIZE = UNIT_MB * 12;
    private static final long MAX_REQUEST_SIZE = (MAX_FILE_SIZE + 2) * 3;
    private static final long FILE_SIZE_THRESHOLD = UNIT_MB * 2;

    @Override
    protected Class<?> @Nullable [] getRootConfigClasses() {
        return new Class[]{ApplicationConfig.class};
    }

    @Override
    protected Class<?> @Nullable [] getServletConfigClasses() {
        return new Class[]{WebConfig.class};
    }

    @Override
    protected String[] getServletMappings() {
        return new String[]{"/api/*"};
    }

    @Override
    protected void customizeRegistration(ServletRegistration.Dynamic registration) {
        MultipartConfigElement configElement = new MultipartConfigElement(
                System.getProperty("java.io.tmpdir"), MAX_FILE_SIZE, MAX_REQUEST_SIZE, (int) FILE_SIZE_THRESHOLD);
        registration.setMultipartConfig(configElement);
    }
}