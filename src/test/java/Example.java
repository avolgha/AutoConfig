import dev.marius.autoconfig.Config;
import dev.marius.autoconfig.Property;
import dev.marius.autoconfig.PropertyType;

@Config(file = "data\\example")
public class Example {
    @Property(name = "test", type = PropertyType.STRING)
    public String test;

    @Property(name = "id", type = PropertyType.INTEGER)
    public Integer id = 5;

    @Override
    public String toString() {
        return "Example{" +
                "test='" + test + '\'' +
                ", id=" + id +
                '}';
    }
}
