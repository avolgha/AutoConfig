package dev.marius.autoconfig;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.*;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @see #load(Class)
 */
public final class AutoConfig {
    private static final Gson jsonParser = new GsonBuilder().setPrettyPrinting().create();

    private AutoConfig() {
        throw new RuntimeException("You cannot make an instance of AutoConfig");
    }

    /**
     * Creates a config from a given class
     *
     * @return Returns the new instance of the config class with the values of the config
     */
    public static <T> @NotNull T load(@NotNull Class<T> typeClass) throws AutoConfigException, IOException,
            NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        if (typeClass.isInterface() || typeClass.isEnum()) {
            throw new AutoConfigException("The given class cannot be an Interface or Enum");
        }

        T instance;
        if (typeClass.getConstructors().length == 0 || (typeClass.getConstructors().length == 1 &&
                typeClass.getConstructors()[0].getParameterCount() == 0)) {
            instance = typeClass.getConstructor().newInstance();
        } else {
            throw new AutoConfigException("The class cannot have constructors with parameters. Only allowed is a non argument constructor");
        }

        if (!typeClass.isAnnotationPresent(Config.class)) {
            throw new AutoConfigException("The given class has to be annotated with 'dev.marius.autoconfig.Config'");
        }
        Config configAnnotation = typeClass.getAnnotation(Config.class);

        JsonObject payload;

        String fN = configAnnotation.file();
        if (!fN.endsWith(".json")) fN += ".json";
        File file = new File(fN);
        if (!file.exists()) {
            Files.createDirectories(file.toPath().getParent());
            Files.createFile(file.toPath());
            payload = new JsonObject();
        } else {
            try (FileReader reader = new FileReader(file)) {
                StringBuilder builder = new StringBuilder();
                int current;
                while ((current = reader.read()) != -1) {
                    builder.append((char) current);
                }
                String rawJson = builder.toString();
                if (rawJson.isEmpty()) rawJson = "{}";
                payload = jsonParser.fromJson(rawJson, JsonElement.class).getAsJsonObject();
            }
        }

        List<Field> fields = List.of(typeClass.getDeclaredFields());
        if (fields.stream().anyMatch(f -> !f.isAnnotationPresent(Property.class))) {
            throw new AutoConfigException("The given class cannot contain non property fields");
        }

        HashMap<Field, Property> map = fields.stream().map(f -> new Map.Entry<Field, Property>() {
            @Override
            public Field getKey() {
                return f;
            }

            @Override
            public Property getValue() {
                return f.getAnnotation(Property.class);
            }

            @Override
            public Property setValue(Property value) {
                throw new IllegalStateException("Cannot update value");
            }
        }).collect(
                HashMap::new,
                (methodPropertyHashMap, entry) -> methodPropertyHashMap.put(entry.getKey(), entry.getValue()),
                HashMap::putAll
        );

        for (Map.Entry<Field, Property> entry : map.entrySet()) {
            PropertyType type = entry.getValue().type();
            if (type == PropertyType.NULL) {
                updateValue(instance, entry.getKey(), null);
            } else if (type == PropertyType.REFERENCE) {
                String[] reference = entry.getValue().name().split("#");

                if (reference.length != 2) {
                    throw new AutoConfigException("Illegal reference string");
                }

                Class<?> referenceClass;
                try {
                    referenceClass = Class.forName(reference[0]);
                } catch (ClassNotFoundException e) {
                    throw new AutoConfigException("Cannot find reference class '%s'".formatted(reference[0]));
                }

                Method referenceMethod;
                try {
                    referenceMethod = referenceClass.getMethod(reference[1]);
                } catch (NoSuchMethodException | NullPointerException | SecurityException e) {
                    throw new AutoConfigException("Cannot find reference method '%s'".formatted(reference[1]));
                }

                updateValue(instance, entry.getKey(), referenceMethod.invoke(instance));
            } else if (type.isSupported(entry.getKey().getType())) {
                String key = entry.getValue().name();
                switch (type) {
                    case STRING -> {
                        try {
                            String v = payload.get(key).getAsString();
                            updateValue(instance, entry.getKey(), v);
                        } catch (Exception e) {
                            updateValue(instance, entry.getKey(), "");
                        }
                    }
                    case INTEGER -> {
                        try {
                            updateValue(instance, entry.getKey(), payload.get(key).getAsInt());
                        } catch (Exception e) {
                            updateValue(instance, entry.getKey(), -1);
                        }
                    }
                    case DOUBLE -> {
                        try {
                            updateValue(instance, entry.getKey(), payload.get(key).getAsDouble());
                        } catch (Exception e) {
                            updateValue(instance, entry.getKey(), -1D);
                        }
                    }
                    case FLOAT -> {
                        try {
                            updateValue(instance, entry.getKey(), payload.get(key).getAsFloat());
                        } catch (Exception e) {
                            updateValue(instance, entry.getKey(), -1F);
                        }
                    }
                    case LONG -> {
                        try {
                            updateValue(instance, entry.getKey(), payload.get(key).getAsLong());
                        } catch (Exception e) {
                            updateValue(instance, entry.getKey(), -1L);
                        }
                    }
                    case SHORT -> {
                        try {
                            updateValue(instance, entry.getKey(), payload.get(key).getAsShort());
                        } catch (Exception e) {
                            updateValue(instance, entry.getKey(), -1);
                        }
                    }
                    case BYTE -> {
                        try {
                            updateValue(instance, entry.getKey(), payload.get(key).getAsByte());
                        } catch (Exception e) {
                            updateValue(instance, entry.getKey(), (byte)0);
                        }
                    }
                    case BOOLEAN -> {
                        try {
                            updateValue(instance, entry.getKey(), payload.get(key).getAsBoolean());
                        } catch (Exception e) {
                            updateValue(instance, entry.getKey(), false);
                        }
                    }
                }
            } else {
                throw new AutoConfigException("Method has illegal return type '%s'".formatted(entry.getKey().getType().getSimpleName()));
            }
        }

        return instance;
    }

    /**
     * Saves the given configuration object to its configuration
     */
    public static void save(@NotNull Object object) throws AutoConfigException, IOException, IllegalAccessException {
        Class<?> typeClass = object.getClass();
        if (typeClass.isInterface() || typeClass.isEnum()) {
            throw new AutoConfigException("The given object cannot be an Interface or Enum");
        }

        if (!typeClass.isAnnotationPresent(Config.class)) {
            throw new AutoConfigException("The given class has to be annotated with 'dev.marius.autoconfig.Config'");
        }
        Config configAnnotation = typeClass.getAnnotation(Config.class);

        JsonObject payload;

        String fN = configAnnotation.file();
        if (!fN.endsWith(".json")) fN += ".json";
        File file = new File(fN);
        if (!file.exists()) {
            Files.createDirectories(file.toPath().getParent());
            Files.createFile(file.toPath());
            payload = new JsonObject();
        } else {
            try (FileReader reader = new FileReader(file)) {
                StringBuilder builder = new StringBuilder();
                int current;
                while ((current = reader.read()) != -1) {
                    builder.append((char) current);
                }
                String rawJson = builder.toString();
                if (rawJson.isEmpty()) rawJson = "{}";
                payload = jsonParser.fromJson(rawJson, JsonElement.class).getAsJsonObject();
            }
        }

        List<Field> fields = List.of(typeClass.getDeclaredFields());
        if (fields.stream().anyMatch(f -> !f.isAnnotationPresent(Property.class))) {
            throw new AutoConfigException("The given class cannot contain non property fields");
        }

        HashMap<Field, Property> map = fields.stream().map(f -> new Map.Entry<Field, Property>() {
            @Override
            public Field getKey() {
                return f;
            }

            @Override
            public Property getValue() {
                return f.getAnnotation(Property.class);
            }

            @Override
            public Property setValue(Property value) {
                throw new IllegalStateException("Cannot update value");
            }
        }).collect(
                HashMap::new,
                (methodPropertyHashMap, entry) -> methodPropertyHashMap.put(entry.getKey(), entry.getValue()),
                HashMap::putAll
        );

        JsonObject next = payload;
        for (Map.Entry<Field, Property> entry : map.entrySet()) {
            String key = entry.getValue().name();
            PropertyType type = entry.getValue().type();

            switch (type) {
                case NULL -> next.addProperty(key, "null");
                case REFERENCE -> next.addProperty(key, entry.getValue().name());
                case STRING -> next.addProperty(key, entry.getKey().get(object).toString());
                case INTEGER -> next.addProperty(key, (Integer) entry.getKey().get(object));
                case DOUBLE -> next.addProperty(key, (Double) entry.getKey().get(object));
                case FLOAT -> next.addProperty(key, (Float) entry.getKey().get(object));
                case LONG -> next.addProperty(key, (Long) entry.getKey().get(object));
                case SHORT -> next.addProperty(key, (Short) entry.getKey().get(object));
                case BYTE -> next.addProperty(key, (Byte) entry.getKey().get(object));
                case BOOLEAN -> next.addProperty(key, (Boolean) entry.getKey().get(object));
            }
        }

        try(FileWriter writer = new FileWriter(file)) {
            writer.write(jsonParser.toJson(next));
        }
    }

    private static <T> void updateValue(Object object, @NotNull Field field, T value) throws IllegalAccessException {
        if (field.canAccess(object)) {
            field.set(object, value);
        } else {
            field.setAccessible(true);
            field.set(object, value);
            field.setAccessible(false);
        }
    }
}
