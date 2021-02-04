package local.gongule.tools;

import freemarker.template.Configuration;
import freemarker.template.Template;
import local.gongule.tools.resources.Resources;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public interface TemplateFillable {
    /**
     * Заполняет html-шаблон данными
     */
    default String fillTemplate(String fileName, Map<String, Object> data) {
        Writer stream = new StringWriter();
        try {
            Configuration conf = new Configuration(Configuration.DEFAULT_INCOMPATIBLE_IMPROVEMENTS);
            String codePage = "UTF-8";
            conf.setDefaultEncoding(codePage);
            InputStream inputStream = Resources.getAsStream(fileName);
            Charset charset = StandardCharsets.UTF_8;
            Template template = new Template(fileName, new InputStreamReader(inputStream, charset), conf, codePage);
            template.process(data, stream);
        } catch (Exception exception) {
            Log.printError("Unpossible fill template", exception);
        }
        return stream.toString();
    }

}
