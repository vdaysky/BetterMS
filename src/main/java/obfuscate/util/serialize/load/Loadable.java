package obfuscate.util.serialize.load;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Marks serializable fields that are meant to be loaded from JSON */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Loadable {
    String field();

    String key() default "";
    boolean explicit() default false;

    boolean propagateUpdate() default true;
    boolean merge() default false;
}
