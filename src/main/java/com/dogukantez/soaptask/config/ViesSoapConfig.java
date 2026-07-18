package com.dogukantez.soaptask.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.ws.client.core.WebServiceTemplate;
import org.springframework.ws.client.support.interceptor.ClientInterceptor;
import org.springframework.ws.client.support.interceptor.PayloadValidatingInterceptor;
import org.springframework.ws.transport.http.ClientHttpRequestMessageSender;

@Configuration
public class ViesSoapConfig {

    @Bean
    public Jaxb2Marshaller viesMarshaller() {
        Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
        marshaller.setContextPath("com.dogukantez.soaptask.generated");
        return marshaller;
    }

    @Bean
    public PayloadValidatingInterceptor viesValidatingInterceptor() {
        PayloadValidatingInterceptor interceptor = new PayloadValidatingInterceptor();
        interceptor.setSchema(new ClassPathResource("xsd/checkVat.xsd"));
        interceptor.setValidateRequest(true);
        interceptor.setValidateResponse(true);
        return interceptor;
    }

    @Bean
    public WebServiceTemplate viesWebServiceTemplate(Jaxb2Marshaller viesMarshaller,
                                                     PayloadValidatingInterceptor viesValidatingInterceptor,
                                                     ViesProperties properties) {

        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(properties.getTimeout().getConnect());
        requestFactory.setReadTimeout(properties.getTimeout().getRead());

        WebServiceTemplate template = new WebServiceTemplate(viesMarshaller);
        template.setDefaultUri(properties.getEndpoint());
        template.setMessageSender(new ClientHttpRequestMessageSender(requestFactory));
        template.setInterceptors(new ClientInterceptor[]{ viesValidatingInterceptor });
        return template;
    }
}
