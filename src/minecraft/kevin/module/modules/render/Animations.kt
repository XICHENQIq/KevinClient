package kevin.module.modules.render

import kevin.module.*


object Animations : Module(name = "Animations", description = "防砍动画", category = ModuleCategory.RENDER) {

    @JvmField
    var xValue: FloatValue = FloatValue("Blocking-X", 0.0f, -2.0f, 2.0f)
    @JvmField
    val yValue: FloatValue = FloatValue("Blocking-Y", 0.0f, -2.0f, 2.0f)
    @JvmField
    val zValue: FloatValue = FloatValue("Blocking-Z", 0.0f, -2.0f, 2.0f)
    @JvmField
    val scaleValue: FloatValue = FloatValue("Blocking-scale", 0.8f, 0.1f, 1.0f)
    @JvmField
    val xhValue: FloatValue = FloatValue("Held-X", 0.0f, -2.0f, 2.0f)
    @JvmField
    val yhValue: FloatValue = FloatValue("Held-Y", 0.0f, -2.0f, 2.0f)
    @JvmField
    val zhValue: FloatValue = FloatValue("Held-Z", 0.0f, -2.0f, 2.0f)
    val scalehValue: FloatValue = FloatValue("Held-scale", 0.8f, 0.1f, 1.0f)

    @JvmField
    val Sword = ListValue("Mode", arrayOf("WindMill","Push","Avatar","ETB","Min"), "WindMill")


    override val tag: String
        get() = Sword.get()
}