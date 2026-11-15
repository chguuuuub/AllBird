package org.allbird.guide

import android.app.Activity
import android.os.Bundle
import android.graphics.Typeface
import android.view.Gravity
import android.widget.*
import android.text.Editable
import android.text.TextWatcher

private data class Bird(
    val english: String,
    val chinese: String,
    val scientific: String,
    val family: String,
    val description: String
)

class MainActivity : Activity() {
    private var chinese = true
    private lateinit var content: LinearLayout
    private lateinit var search: EditText

    private val birds = listOf(
        Bird("Common Kingfisher", "普通翠鸟", "Alcedo atthis", "翠鸟科 / Alcedinidae", "Often perches above clear water and dives for small fish. 常停栖于水面上方，俯冲捕食小鱼。"),
        Bird("Mute Swan", "疣鼻天鹅", "Cygnus olor", "鸭科 / Anatidae", "A large waterbird with an orange bill and black knob. 大型水鸟，橙色喙基部有黑色瘤突。"),
        Bird("Barn Owl", "仓鸮", "Tyto alba", "草鸮科 / Tytonidae", "Heart-shaped facial disc; hunts mainly at night. 心形面盘，主要在夜间捕猎。"),
        Bird("Eurasian Blue Tit", "蓝山雀", "Cyanistes caeruleus", "山雀科 / Paridae", "Small, active woodland bird with a blue crown. 小型活跃林鸟，头顶呈蓝色。"),
        Bird("Peregrine Falcon", "游隼", "Falco peregrinus", "隼科 / Falconidae", "A powerful falcon known for high-speed hunting dives. 以高速俯冲捕猎著称。"),
        Bird("Eurasian Magpie", "喜鹊", "Pica pica", "鸦科 / Corvidae", "Black-and-white corvid with a long tail. 黑白相间、长尾的鸦科鸟类。"),
        Bird("Mallard", "绿头鸭", "Anas platyrhynchos", "鸭科 / Anatidae", "A common dabbling duck; males have a glossy green head. 常见涉水觅食鸭，雄鸟头部具绿色光泽。"),
        Bird("White-tailed Eagle", "白尾海雕", "Haliaeetus albicilla", "鹰科 / Accipitridae", "A large eagle of coasts and freshwater habitats. 常见于海岸及淡水区域的大型猛禽。")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        buildScreen()
    }

    private fun dp(value: Int): Int =
        (value * resources.displayMetrics.density).toInt()

    private fun label(value: String, size: Float = 16f, bold: Boolean = false) =
        TextView(this).apply {
            text = value
            textSize = size
            setTextColor(0xFF20332B.toInt())
            if (bold) setTypeface(null, Typeface.BOLD)
            setPadding(dp(4), dp(7), dp(4), dp(7))
        }

    private fun buildScreen() {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(14), dp(16), dp(8))
            setBackgroundColor(0xFFF4F7F3.toInt())
        }

        val header = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        header.addView(label("🐦 AllBird", 26f, true), LinearLayout.LayoutParams(0, -2, 1f))
        header.addView(Button(this).apply {
            text = "中文 / EN"
            setOnClickListener {
                chinese = !chinese
                refreshContent()
            }
        })
        root.addView(header)
        root.addView(label(if (chinese) "全球鸟类指南 · 本地资料库" else "Global Bird Guide · Local field notes", 14f))

        search = EditText(this).apply {
            hint = if (chinese) "搜索鸟名或学名" else "Search common or scientific name"
            singleLine = true
        }
        root.addView(search)

        val actions = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        actions.addView(Button(this).apply {
            text = if (chinese) "鸟种指南" else "Bird guide"
            setOnClickListener { showBirds(search.text.toString()) }
        }, LinearLayout.LayoutParams(0, -2, 1f))
        actions.addView(Button(this).apply {
            text = if (chinese) "智能识别" else "AI identify"
            setOnClickListener { showRecognition() }
        }, LinearLayout.LayoutParams(0, -2, 1f))
        root.addView(actions)

        val scroll = ScrollView(this)
        content = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        scroll.addView(content)
        root.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))
        setContentView(root)

        search.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                showBirds(s?.toString().orEmpty())
            }
            override fun afterTextChanged(s: Editable?) = Unit
        })
        showBirds("")
    }

    private fun refreshContent() {
        search.hint = if (chinese) "搜索鸟名或学名" else "Search common or scientific name"
        showBirds(search.text.toString())
    }

    private fun showBirds(query: String) {
        content.removeAllViews()
        content.addView(label(if (chinese) "鸟种条目（示例资料）" else "Species entries (sample dataset)", 18f, true))
        val matches = birds.filter { bird ->
            query.isBlank() || listOf(bird.english, bird.chinese, bird.scientific)
                .any { it.contains(query.trim(), ignoreCase = true) }
        }
        matches.forEach { bird ->
            val card = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(dp(12), dp(8), dp(12), dp(8))
                setBackgroundColor(0xFFFFFFFF.toInt())
                addView(label(if (chinese) "${bird.chinese} · ${bird.english}" else "${bird.english} · ${bird.chinese}", 17f, true))
                addView(label(bird.scientific, 14f))
                addView(label(bird.family, 13f))
                addView(label(bird.description, 14f))
            }
            val params = LinearLayout.LayoutParams(-1, -2)
            params.setMargins(0, dp(5), 0, dp(5))
            content.addView(card, params)
        }
        if (matches.isEmpty()) {
            content.addView(label(if (chinese) "没有找到匹配条目。" else "No matching species found."))
        }
    }

    private fun showRecognition() {
        content.removeAllViews()
        content.addView(label(if (chinese) "智能识别模块" else "Identification modules", 20f, true))
        content.addView(label(if (chinese)
            "当前版本已建立识别入口，但尚未内置可用的图像或鸟鸣识别模型。离线识别需要集成并验证 TFLite 模型；在线识别需要配置识别服务/API。"
            else "The identification entry point is ready, but this release does not include trained image or bird-call models. Offline recognition requires a validated TFLite model; online recognition requires a configured service/API."))
        content.addView(label(if (chinese) "图像识别：模型待接入" else "Image recognition: model pending", 15f, true))
        content.addView(label(if (chinese) "鸟鸣识别：音频模型待接入" else "Bird-call recognition: audio model pending", 15f, true))
        content.addView(label(if (chinese) "当前鸟种资料：本地示例数据" else "Bird data: local sample dataset", 15f, true))
    }
}
