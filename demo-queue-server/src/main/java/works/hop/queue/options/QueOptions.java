package works.hop.queue.options;

import org.apache.commons.cli.*;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.Properties;
import java.util.UUID;

public class QueOptions {

    public static Properties applyDefaults(String[] args) {
        //gather properties info
        Properties props = handleCli(args);
        //protocol
        props.put("port", Optional.ofNullable(props.getProperty("port")).orElse("8080"));
        props.put("host", Optional.ofNullable(props.getProperty("host")).orElse("localhost"));
        props.put("name", Optional.ofNullable(props.getProperty("name")).orElse(UUID.randomUUID().toString()));
        return props;
    }

    public static Properties handleCli(String[] args) {
        //define acceptable options
        Options options = new Options();
        options.addOption("p", "port", true, "The listening port for the server")
                .addOption("h", "host", true, "The host name for the server")
                .addOption("n", "name", true, "The name identifier for the running instance")
                .addOption("c", "config", true, "The application's config properties file");

        Properties props = new Properties();
        try {
            //get command line props, which will be preferred over default value
            CommandLineParser parser = new DefaultParser();
            CommandLine cmd = parser.parse(options, args);

            if (cmd.hasOption("config")) {
                props = loadProperties(cmd.getOptionValue("config"));
            }
            if (cmd.hasOption("port")) {
                props.setProperty("port", cmd.getOptionValue("port"));
            }
            if (cmd.hasOption("host")) {
                props.setProperty("host", cmd.getOptionValue("host"));
            }
            if (cmd.hasOption("name")) {
                props.setProperty("name", cmd.getOptionValue("name"));
            }
        } catch (Exception e) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("Server CLI options", options);
        }
        return props;
    }

    public static Properties loadProperties(String dataFile) throws IOException {
        Properties config = new Properties();
        config.load(loadDataFile(dataFile));
        return config;
    }

    public static InputStreamReader loadDataFile(String dataFile) {
        try {
            return new InputStreamReader(new FileInputStream(Paths.get(dataFile).toFile()));
        } catch (Exception e1) {
            try {
                return new InputStreamReader(QueOptions.class.getResourceAsStream(dataFile));
            } catch (Exception e2) {
                try {
                    return new InputStreamReader(QueOptions.class.getClassLoader().getResourceAsStream(dataFile));
                } catch (Exception e3) {
                    throw new RuntimeException("Could not locate file to load", e3);
                }
            }
        }
    }
}
