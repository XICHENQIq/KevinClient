package kevin.utils


object IPUtils {

    fun getJson(): String {
        return WebUtils.get("https://ipinfo.io/json")
    }
    fun readJsonIp(s: String): String {
        val json = JsonParser.parseString(s).asJsonObject
        return json.get("ip").asString
    }
    fun readJsonRegion(s: String): String {
        val json = JsonParser.parseString(s).asJsonObject
        return json.get("region").asString
    }
    fun readJsonCountry(s: String): String {
        val json = JsonParser.parseString(s).asJsonObject
        return json.get("country").asString
    }
    fun convertToChinese(province: String): String {
        return when (province) {
            "Beijing" -> "北京"
            "Tianjin" -> "天津"
            "Hebei" -> "河北"
            "Shanxi" -> "山西"
            "Inner Mongolia" -> "内蒙古"
            "Liaoning" -> "辽宁"
            "Jilin" -> "吉林"
            "Heilongjiang" -> "黑龙江"
            "Shanghai" -> "上海"
            "Jiangsu" -> "江苏"
            "Zhejiang" -> "浙江"
            "Anhui" -> "安徽"
            "Fujian" -> "福建"
            "Jiangxi" -> "江西"
            "Shandong" -> "山东"
            "Henan" -> "河南"
            "Hubei" -> "湖北"
            "Hunan" -> "湖南"
            "Guangdong" -> "广东"
            "Guangxi" -> "广西"
            "Hainan" -> "海南"
            "Chongqing" -> "重庆"
            "Sichuan" -> "四川"
            "Guizhou" -> "贵州"
            "Yunnan" -> "云南"
            "Tibet" -> "西藏"
            "Shaanxi" -> "陕西"
            "Gansu" -> "甘肃"
            "Qinghai" -> "青海"
            "Ningxia" -> "宁夏"
            "Xinjiang" -> "新疆"
            else -> province  // return unchanged if not found
        }
    }

}