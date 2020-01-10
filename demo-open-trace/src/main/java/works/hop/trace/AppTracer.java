package works.hop.trace;

import io.jaegertracing.Configuration;
import io.opentracing.Tracer;

public class AppTracer {

    public static Tracer tracer() {
        Configuration.SamplerConfiguration samplerConfig = new Configuration.SamplerConfiguration().fromEnv().withType("const").withParam(1);
        Configuration.ReporterConfiguration reporterConfig = new Configuration.ReporterConfiguration().fromEnv().withLogSpans(true);
        Configuration config = new Configuration("demo-open-trace").withSampler(samplerConfig).withReporter(reporterConfig);
        return config.getTracer();
    }
}
