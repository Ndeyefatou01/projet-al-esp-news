package esp.sn.projet_al_backend.endpoint;

import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.ws.config.annotation.EnableWs;
import org.springframework.ws.transport.http.MessageDispatcherServlet;
import org.springframework.ws.wsdl.wsdl11.DefaultWsdl11Definition;
import org.springframework.xml.xsd.SimpleXsdSchema;
import org.springframework.xml.xsd.XsdSchema;

@EnableWs
@Configuration
public class WebServiceConfig {

    @Bean
    public ServletRegistrationBean<MessageDispatcherServlet> messageDispatcherServlet(ApplicationContext applicationContext) {
        MessageDispatcherServlet servlet = new MessageDispatcherServlet();
        servlet.setApplicationContext(applicationContext);
        servlet.setTransformWsdlLocations(true);
        return new ServletRegistrationBean<>(servlet, "/ws/soap/*");
    }

    @Bean(name = "utilisateurs")
    public DefaultWsdl11Definition defaultWsdl11Definition(XsdSchema utilisateursSchema) {
        DefaultWsdl11Definition wsdl11Definition = new DefaultWsdl11Definition();
        wsdl11Definition.setPortTypeName("UtilisateursPort");
        wsdl11Definition.setLocationUri("/ws/soap/");
        wsdl11Definition.setTargetNamespace("http://projet-al.esp.sn/soap/utilisateurs");
        wsdl11Definition.setSchema(utilisateursSchema);
        return wsdl11Definition;
    }

    @Bean
    public XsdSchema utilisateursSchema() {
        return new SimpleXsdSchema(new ClassPathResource("xsd/utilisateurs.xsd"));
    }
}