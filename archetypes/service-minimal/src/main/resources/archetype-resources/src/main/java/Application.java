package ${package};

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import com.manydesigns.portofino.microservices.boot.support.PortofinoSupport;
import com.manydesigns.portofino.modules.DatabaseModule;

@SpringBootApplication
@Import({ PortofinoSupport.class, DatabaseModule.class })
public class Application {

    public static void main(String[] args) throws Exception {
        SpringApplication.run(Application.class, args);
    }

    //Here you can declare the Spring beans of your application.

}