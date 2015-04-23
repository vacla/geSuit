package net.cubespace.geSuit.commands;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Command {
    public String name();
    public boolean async() default false;
    public String[] aliases() default {};
    public String permission() default "";
    public String usage() default "/<command>";
    public String description() default "";
}
