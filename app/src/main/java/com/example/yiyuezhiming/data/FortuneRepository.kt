package com.example.yiyuezhiming.data

import com.example.yiyuezhiming.data.deepseek.DeepSeekMessage
import com.example.yiyuezhiming.data.deepseek.DeepSeekRepository
import com.example.yiyuezhiming.data.deepseek.DeepSeekResult
import com.example.yiyuezhiming.data.local.FortuneDao
import com.example.yiyuezhiming.model.FortuneRecord
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.random.Random

data class DailySign(
    val number: Int,
    val title: String,
    val level: String,
    val poem: String,
    val keywords: List<String>
)

data class TarotCard(
    val name: String,
    val suit: String,
    val upright: String,
    val reversed: String
)

@Singleton
class FortuneRepository @Inject constructor(
    private val dao: FortuneDao,
    private val deepSeekRepository: DeepSeekRepository
) {
    fun observeHistory(type: String): Flow<List<FortuneRecord>> =
        dao.observeHistory(type).map { rows -> rows.map { it.toModel() } }

    suspend fun getToday(type: String, date: LocalDate = LocalDate.now()): FortuneRecord? =
        dao.getRecord(type, date)?.toModel()

    suspend fun drawDailySign(date: LocalDate = LocalDate.now()): DeepSeekResult<FortuneRecord> {
        getToday(TYPE_SIGN, date)?.let { return DeepSeekResult.Success(it) }
        val sign = dailySigns.random(Random(date.toEpochDay()))
        val prompt = """
            请以传统签运文化解签师的口吻，为下面这支每日签做温和、积极、娱乐性质的解读。
            不要做绝对化预言，不要给医疗、法律、投资等高风险建议。

            签号：第${sign.number}签
            签名：${sign.title}
            等级：${sign.level}
            签文：${sign.poem}
            关键词：${sign.keywords.joinToString("、")}

            请使用 Markdown 输出，严格按下面结构组织，语言温柔但专业，不要输出表格：
            ## 整体运势
            ## 感情提示
            ## 行动建议
            ## 今日提醒
        """.trimIndent()
        return when (val result = deepSeekRepository.complete("daily_sign", listOf(DeepSeekMessage("user", prompt)))) {
            is DeepSeekResult.Success -> {
                val record = FortuneRecord(
                    type = TYPE_SIGN,
                    date = date,
                    drawTitle = "第${sign.number}签 · ${sign.title}",
                    drawSubtitle = sign.level,
                    drawContent = sign.poem,
                    keywords = sign.keywords.joinToString("、"),
                    interpretation = result.value
                )
                dao.upsert(record.toEntity())
                DeepSeekResult.Success(record)
            }
            is DeepSeekResult.Failure -> result
        }
    }

    suspend fun drawTarot(date: LocalDate = LocalDate.now()): DeepSeekResult<FortuneRecord> {
        getToday(TYPE_TAROT, date)?.let { return DeepSeekResult.Success(it) }
        val random = Random(date.toEpochDay() * 31)
        val card = tarotDeck.random(random)
        val reversed = random.nextBoolean()
        val orientation = if (reversed) "逆位" else "正位"
        val meaning = if (reversed) card.reversed else card.upright
        val prompt = """
            请以专业塔罗牌解读师的口吻，为每日单张牌做温和、清晰、娱乐性质的解读。
            不要做绝对化预言，不要给医疗、法律、投资等高风险建议。

            抽到的牌：${card.name}
            牌组：${card.suit}
            方向：$orientation
            基础牌义：$meaning

            请使用 Markdown 输出，严格按下面结构组织，语言要适合情侣陪伴类 App，不要输出表格：
            ## 整体运势
            ## 感情提示
            ## 行动建议
            ## 今日提醒
        """.trimIndent()
        return when (val result = deepSeekRepository.complete("tarot", listOf(DeepSeekMessage("user", prompt)))) {
            is DeepSeekResult.Success -> {
                val record = FortuneRecord(
                    type = TYPE_TAROT,
                    date = date,
                    drawTitle = "${card.name} · $orientation",
                    drawSubtitle = card.suit,
                    drawContent = meaning,
                    keywords = orientation,
                    interpretation = result.value
                )
                dao.upsert(record.toEntity())
                DeepSeekResult.Success(record)
            }
            is DeepSeekResult.Failure -> result
        }
    }

    companion object {
        const val TYPE_SIGN = "sign"
        const val TYPE_TAROT = "tarot"
    }
}

private val dailySigns = listOf(
    DailySign(1, "春风入户", "上吉", "春风轻叩小窗开，旧愿随云入梦来。若把真心藏袖里，明朝花信自然回。", listOf("新机", "回应", "温柔")),
    DailySign(2, "月照归舟", "中吉", "一叶归舟过晚汀，月华如水照人行。莫嫌此刻风声慢，稳处方知路自明。", listOf("等待", "稳定", "归心")),
    DailySign(3, "花影逢晴", "小吉", "花影摇摇雨后新，晴光一点落衣襟。心中若有相逢意，浅笑先从眼底寻。", listOf("和解", "靠近", "喜讯")),
    DailySign(4, "云开见鹿", "中吉", "云深不见旧时桥，偶有灵光过树梢。行到水穷回首处，一番天地正相邀。", listOf("转机", "探索", "勇气")),
    DailySign(5, "灯火可亲", "上吉", "小楼灯火夜微温，一句平安抵万金。今日宜将心事说，清茶半盏也知音。", listOf("沟通", "陪伴", "安心")),
    DailySign(6, "桃枝待雨", "平", "桃枝含露未成红，莫向东风问始终。先把根心安土里，花期自会到帘栊。", listOf("蓄力", "耐心", "成长")),
    DailySign(7, "星河有信", "大吉", "星河迢递也传书，万里清辉落玉壶。若问今朝何所获，真情一点胜珍珠。", listOf("好消息", "真诚", "幸运")),
    DailySign(8, "柳岸听莺", "小吉", "柳岸莺声细细来，莫因小事锁眉台。退开一步春波阔，笑把闲愁作絮裁。", listOf("放松", "包容", "轻盈")),
    DailySign(9, "青石留香", "中吉", "青石阶前香未歇，旧时心愿正堪携。慢行不误花间路，所爱终将有处栖。", listOf("坚持", "回忆", "承诺")),
    DailySign(10, "朝露映心", "平", "朝露晶莹照寸心，清明一念抵千金。今日不宜多猜测，坦白温言胜远音。", listOf("坦诚", "清醒", "少猜"))
)

private val majorArcana = listOf(
    "愚者" to "新的开始、自由、冒险",
    "魔术师" to "行动力、创造、资源整合",
    "女祭司" to "直觉、静心、隐藏信息",
    "皇后" to "滋养、丰盛、温柔表达",
    "皇帝" to "秩序、责任、边界",
    "教皇" to "传统、承诺、学习",
    "恋人" to "选择、亲密、关系课题",
    "战车" to "推进、掌控、胜利",
    "力量" to "耐心、勇气、柔软的坚持",
    "隐士" to "独处、反思、内在答案",
    "命运之轮" to "变化、周期、机会",
    "正义" to "平衡、公正、因果",
    "倒吊人" to "换位、等待、放下执念",
    "死神" to "结束、转化、新阶段",
    "节制" to "调和、疗愈、慢慢来",
    "恶魔" to "执念、诱惑、看见束缚",
    "高塔" to "打破旧结构、突然觉醒",
    "星星" to "希望、祝福、长远信念",
    "月亮" to "不安、梦境、模糊感",
    "太阳" to "快乐、坦荡、被照亮",
    "审判" to "复苏、回应、重新决定",
    "世界" to "完成、圆满、阶段成果"
)

private val minorSuits = listOf("权杖", "圣杯", "宝剑", "星币")
private val minorRanks = listOf("王牌", "二", "三", "四", "五", "六", "七", "八", "九", "十", "侍从", "骑士", "王后", "国王")

private val tarotDeck: List<TarotCard> =
    majorArcana.map { (name, meaning) ->
        TarotCard(name, "大阿尔卡那", meaning, "能量受阻，提醒你慢一点看清真正的需要")
    } + minorSuits.flatMap { suit ->
        minorRanks.mapIndexed { index, rank ->
            val base = when (suit) {
                "权杖" -> "热情、行动、创造力"
                "圣杯" -> "情感、关系、共鸣"
                "宝剑" -> "思考、沟通、选择"
                else -> "现实、稳定、长期经营"
            }
            TarotCard("$suit$rank", "小阿尔卡那", "$base，第${index + 1}阶段的推进", "$base 需要整理，先处理卡住的部分")
        }
    }
