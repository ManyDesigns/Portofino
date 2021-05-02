package com.manydesigns.portofino.jersey;

import com.manydesigns.mail.rest.SendMailAction;
import io.swagger.v3.jaxrs2.integration.resources.OpenApiResource;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.util.ClassUtils;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.Path;
import javax.ws.rs.ext.Provider;
import java.util.stream.Collectors;

@ApplicationPath("/")
public class PortofinoApplication extends ResourceConfig {

    public PortofinoApplication() {
        ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AnnotationTypeFilter(Provider.class));
        scanner.addIncludeFilter(new AnnotationTypeFilter(Path.class));
        registerClasses(scanner.findCandidateComponents("com.manydesigns.portofino.rest").stream()
                .map(beanDefinition -> ClassUtils.resolveClassName(beanDefinition.getBeanClassName(), getClassLoader()))
                .collect(Collectors.toSet()));
        register(OpenApiResource.class);
        register(JacksonFeature.class);
        try {
            register(new MailInit().getSendMailAction());
        } catch (NoClassDefFoundError e) {
            //Mail not available
        }
    }

    private static class MailInit {
        private Class<?> getSendMailAction() {
            return SendMailAction.class;
        }
    }

}
