# AutoConfig

> Generate JSON-Configuration-Files automatically without writing any JSON by yourself

## How to use

It's very easy! Just create a normal Java-Class-File and annotate it with the ``dev.marius.autoconfig.Config``
annotation. 

Now you can add local fields. The fields can have a default value, because there will be no value if you
create the configuration the first time. The fields have to be annotated with the ``dev.marius.autoconfig.Property``
annotation

<br>

><details>
><summary>Example Configuration</summary>
>
>````java
>@Config(file = "data\\example")
>public class Example {
>    @Property(name = "test", type = PropertyType.STRING)
>    public String test;
>
>    @Property(name = "id", type = PropertyType.INTEGER)
>    public Integer id = 5;
>}
>````
></details>

<br>
After that you have to have to implement the following methods into your code:

````java
public static void main(String[] args) {
    try {
        Example exampleConfig = AutoConfig.load(Example.class);

        AutoConfig.save(exampleConfig);
    } catch (AutoConfigException | IOException | NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
        e.printStackTrace();
    }
}
````

This code uses the ``Example``-Class from above and loads it from the ``#load`` function of the ``AutoConfig``-Class. Then it uses its instance to save it with the ``#save`` function of the ``AutoConfig``-Class.

## Mini Javadoc
<details>
<summary>Open</summary>

#### ``dev.marius.autoconfig.AutoConfig``
````java
public class AutoConfig {
    public static <T> T load(Class<T> clazz) throws AutoConfigException, IOException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {}

    public static void save(Object object) throws AutoConfigException, IOException {}
}
````

The ``load``-Function loads the configuration from the given class. 

The ``save``-Function saves the ``object`` that has to be a configuration

#### ``dev.marius.autoconfig.Config``
````java
public @interface Config {
    String file();
}
````

The main annotation of the hole process. It is used to detect if the class
is a configuration class.

The ``file``-Parameter is used to define the file where the configuration
is saved

#### ``dev.marius.autoconfig.Property``
````java
public @interface Property {
    String name();
    PropertyType type();
}
````

The annotation is used to detect if a field is a property.

The ``name``-Parameter is used to define the key of the property in the 
json configuration.

The ``type``-Parameter is used to define the type of the property.

#### ``dev.marius.autoconfig.PropertyType``
````java
public enum PropertyType {
    STRING,
    INTEGER,
    DOUBLE,
    FLOAT,
    LONG,
    SHORT,
    BYTE,
    BOOLEAN,
    REFERENCE,
    NULL;
}
````

The ``PropertyType``-Enum is used to define the type of the property.


``STRING``, ``INTEGER``, ``DOUBLE``, ``FLOAT``, ``LONG``, ``SHORT``,
``BYTE`` and ``BOOLEAN`` should be self defined. They refere to the
Java-Types.

``NULL`` is used to define a value as ``null``.

``REFERENCE`` is used to define a reference to a method that will be called at defining this property.
</details>

## Contact

You can contact me on discord: ``Marius#0686``