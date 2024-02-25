package net.juligame.classes.logic.annotations;

import javax.security.auth.callback.Callback;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface ShowVar {
    boolean editable() default true;
    // function that will be called when the value is changed
    String callback() default "";
}
