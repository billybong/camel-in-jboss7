import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.jms.JmsComponent;
import org.apache.camel.impl.DefaultCamelContext;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.jms.ConnectionFactory;

/**
 * TODO: Using an EJB to bootstrap is perhaps little unnecessary and heavy-weight. Refactor?
 */

@Singleton
@Startup
public class Bootstrap {

    @Resource(name = "java:/JmsXA")
    private ConnectionFactory connectionFactory;

    @PostConstruct
    private void bootstrapCamel() throws Exception {
        CamelContext context = new DefaultCamelContext();

        JmsComponent jmsComponent = new JmsComponent();
        jmsComponent.setConnectionFactory(connectionFactory);

        context.addComponent("jms", jmsComponent);

        context.addRoutes(new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from("file:/tmp/in").log("Received inbound message ${body}").to("jms:testQueue");

                from("jms:testQueue").log("Received outbound message ${body}").to("file:/tmp/out");
            }
        });
        context.start();
    }
}
