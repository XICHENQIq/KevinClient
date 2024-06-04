package kevin.utils

import net.minecraft.item.ItemStack

object ArmorMaterial {
    /**
     * by XiChenQi-7.30
     * 相当的蠢，但是能用:)
     */

    fun getArmorMaterial(itemStack : ItemStack) : Int{
        val item = itemStack.item
        val name = item.unlocalizedName
        if (name == "item.helmetLeather" || name == "item.chestplateLeather" || name == "item.leggingsLeather" || name == "item.bootsLeather") {
            return 1
        } else if (name == "item.helmetChain" || name == "item.chestplateChain" || name == "item.leggingsChain" || name == "item.bootsChain") {
            return 2
        } else if (name == "item.helmetGold" || name == "item.chestplateGold" || name == "item.leggingsGold" || name == "item.bootsGold") {
            return 3
        } else if (name == "item.helmetIron" || name == "item.chestplateIron" || name == "item.leggingsIron" || name == "item.bootsIron") {
            return 4
        } else if (name == "item.helmetDiamond" || name == "item.chestplateDiamond" || name == "item.leggingsDiamond" || name == "item.bootsDiamond") {
            return 5
        } else {
            return 0
        }
    }
}