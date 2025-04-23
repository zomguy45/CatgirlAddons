package catgirlroutes.module.settings.impl

import catgirlroutes.module.settings.Setting
import catgirlroutes.module.settings.Visibility

class OrderSetting(
    name: String,
    override val default: Map<String, String>,
    description: String? = null,
    visibility: Visibility = Visibility.VISIBLE,
) : Setting<Map<String, String>>(name, description, visibility) {

    override var value: Map<String, String> = default

    var options: List<String>
        get() = value.values.toList()
        set(values) {
            val keys = value.keys.toList()
            val newValues = values.take(keys.size).plus(List(keys.size - values.size) { "" })
            value = keys.zip(newValues).toMap()
        }

    var placeholders: List<String>
        get() = value.keys.toList()
        set(keys) {
            val values = value.values.toList()
            val newKeys = keys.take(values.size).plus(List(values.size - keys.size) { "key_${it}" })
            value = newKeys.zip(values).toMap()
        }
}