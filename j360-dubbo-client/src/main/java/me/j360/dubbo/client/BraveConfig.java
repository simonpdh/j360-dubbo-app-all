package me.j360.dubbo.client;

import com.github.kristofa.brave.Brave;
import com.github.kristofa.brave.InheritableServerClientAndLocalSpanState;
import com.github.kristofa.brave.Sampler;
import com.github.kristofa.brave.spring.ServletHandlerInterceptor;
import me.j360.dubbo.trace.brave.Slf4jLogReporter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import zipkin.Endpoint;

@Configuration
@ImportResource("classpath:/client.xml")
@EnableWebMvc
public class BraveConfig extends WebMvcConfigurerAdapter {

    @Bean
    public Brave brave() {
        //default reporter LoggingSpanCollector
        Brave.Builder builder = braveBuilder(Sampler.ALWAYS_SAMPLE);
        return builder.build();
    }

    @Bean
    public ServletHandlerInterceptor interceptor(){
        return ServletHandlerInterceptor.create(brave());
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(interceptor());
    }

    Endpoint local = Endpoint.builder().serviceName("local").ipv4(127 << 24 | 1).port(100).build();
    Brave.Builder braveBuilder(Sampler sampler) {
        com.twitter.zipkin.gen.Endpoint localEndpoint = com.twitter.zipkin.gen.Endpoint.builder()
                .ipv4(local.ipv4)
                .ipv6(local.ipv6)
                .port(local.port)
                .serviceName(local.serviceName)
                .build();
        return new Brave.Builder(new InheritableServerClientAndLocalSpanState(localEndpoint))
                .reporter(new Slf4jLogReporter("zipkin"))
                .traceSampler(sampler);
    }
}
