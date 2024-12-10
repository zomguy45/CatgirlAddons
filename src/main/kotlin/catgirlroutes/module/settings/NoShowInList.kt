package catgirlroutes.module.settings

/**
 * [Module] classes with this annotation will not have "Show in List" [BooleanSetting] (array list)
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class NoShowInList