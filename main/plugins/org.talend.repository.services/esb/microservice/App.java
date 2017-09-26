package ${package};

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.cxf.Bus;
import org.apache.cxf.jaxrs.JAXRSServerFactoryBean;
import org.apache.cxf.transport.servlet.CXFServlet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.core.env.Environment;

@SpringBootApplication
@Configuration
@EnableAutoConfiguration
@ImportResource({ "classpath:META-INF/cxf/cxf.xml" })
public class App extends ${parent} implements ApplicationRunner {

    @Autowired
    Environment env;

    public App() {
        setRunInTalendEsbRuntimeContainer(true);
        setRunInDaemonMode(false);
        runJobInTOS(mainArgs);
    }

    @Autowired
    private ApplicationContext applicationContext;

    public static void main( String[] args )
    {
        String[] resetArgs = resetArgs(args);
        SpringApplication.run(App.class, resetArgs);
    }

    @Bean
    public org.apache.cxf.endpoint.Server restServer() {
        
        Thread4RestServiceProviderEndpoint thread4RestServiceProviderEndpoint = new Thread4RestServiceProviderEndpoint(this,
                getCXFRSEndpointAddress(restEndpoint));
        JAXRSServerFactoryBean sf = thread4RestServiceProviderEndpoint.getJAXRSServerFactoryBean();
        sf.setBus((Bus) applicationContext.getBean(Bus.DEFAULT_BUS_ID));

        List providers = sf.getProviders();
        // JAASAuthenticationFilter jaasAuthenticationFilter = new JAASAuthenticationFilter();
        // jaasAuthenticationFilter.setContextName("karaf");
        // providers.add(jaasAuthenticationFilter);
        // sf.setProviders(providers);
        
        ${enableSAML}
        
        thread4RestServiceProviderEndpoint.run();
        return thread4RestServiceProviderEndpoint.getServer();
    }

    @Bean
    public ServletRegistrationBean servletRegistrationBean(ApplicationContext context) {
        return new ServletRegistrationBean(new CXFServlet(), "/services/*");
    }

    private String[] mainArgs = new String[] {};

    public void run(ApplicationArguments args) throws Exception {
        String[] ma = args.getSourceArgs();
        mainArgs = ma;
    }
    
    public InputStream getConfigLocation(String fileName) {
        InputStream stream = null;

        String configFile = "config/" + fileName;

        String configPath = this.env.getProperty("spring.config.location");

        String file = "";
        if (configPath != null) {
            file = configPath + File.separator + fileName;
        } else {
            file = System.getProperty("user.dir") + File.separator + configFile;
        }
        File usersfile = new File(file);
        if (usersfile.exists()) {
            try {
                stream = new FileInputStream(file);
            } catch (Exception e) {
                stream = getClass().getClassLoader().getResourceAsStream(configFile);
            }
        } else {
            stream = getClass().getClassLoader().getResourceAsStream(configFile);
        }
        return stream;
    }

    private static void loadConfig(Map<String, String> argsMap, String configName, String configValue) {
        String configFileValue = "classpath:config/" + configValue;
        if (argsMap.get(configName) == null) {
            if (argsMap.get("--spring.config.location") == null) {
                argsMap.put(configName, configFileValue);
            } else {
                String value = (String) argsMap.get("--spring.config.location") + File.separator + configValue;
                if (new File(value).exists()) {
                    argsMap.put(configName, "file:" + value);
                } else if (((String) argsMap.get("--spring.config.location")).contains(":")) {
                    try {
                        if (new File(new URI(value)).exists()) {
                            argsMap.put(configName, value);
                        } else {
                            argsMap.put(configName, configFileValue);
                        }
                    } catch (Exception e) {
                        argsMap.put(configName, configFileValue);
                    }
                } else {
                    argsMap.put(configName, configFileValue);
                }
            }
        }
    }
    
    private static String[] resetArgs(String... args) {
        Map<String, String> argsMap = new HashMap<String, String>();

        for (int i = 0; i < args.length; i++) {
            String[] kv = args[i].split("=");
            argsMap.put(kv[0], kv[1]);
        }

        if (argsMap.get("--spring.config.location") != null) {
            System.setProperty("spring.config.location", argsMap.get("--spring.config.location"));
        }
        loadConfig(argsMap, "--banner.location", "banner.txt");
        loadConfig(argsMap, "--logging.config", "log4j2.xml");

        if (argsMap.get("--camel.springboot.typeConversion") == null) {
            argsMap.put("--camel.springboot.typeConversion", "false");
        }

        String[] resetArgs = new String[argsMap.size()];

        java.util.Set<String> keySet = argsMap.keySet();

        int idx = 0;

        for (String key : keySet) {
            resetArgs[idx] = key + "=" + argsMap.get(key);
            idx++;
        }

        return resetArgs;
    }
    
    public String getCXFRSEndpointAddress(String endpointUrl) {
        if (endpointUrl != null && !endpointUrl.trim().isEmpty() && !endpointUrl.contains("://")) {
            if (endpointUrl.startsWith("/services")) {
                endpointUrl = endpointUrl.substring("/services".length());
            }
            if (!endpointUrl.startsWith("/")) {
                endpointUrl = '/' + endpointUrl;
            }
        }
        return endpointUrl;
    }

}
