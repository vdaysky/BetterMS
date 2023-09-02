package obfuscate.event;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Target(value= ElementType.METHOD)
@Retention(value= RetentionPolicy.RUNTIME)
public @interface LocalEvent
{
    LocalPriority priority() default LocalPriority.NATIVE;
    boolean cascade() default false;
    boolean _native() default false;
}
