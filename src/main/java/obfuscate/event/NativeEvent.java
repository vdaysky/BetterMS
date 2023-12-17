package obfuscate.event;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Does absolutely nothing, but a nice way to mark core game handlers */
@Target(value= ElementType.METHOD)
@Retention(value= RetentionPolicy.RUNTIME)

public @interface NativeEvent {

}
