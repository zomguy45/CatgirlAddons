package catgirlroutes.module.settings


/**
 * [Module] classes with this annotation will be displayed in Settings category.
 * doesn't remove from its original category
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class SettingsCategory