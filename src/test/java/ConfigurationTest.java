import dev.marius.autoconfig.AutoConfig;
import dev.marius.autoconfig.AutoConfigException;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

public class ConfigurationTest {
    public static void main(String[] args) {
        try {
            Example exampleConfig = AutoConfig.load(Example.class);

            System.out.println(exampleConfig);

            AutoConfig.save(exampleConfig);
        } catch (AutoConfigException | IOException | NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}
