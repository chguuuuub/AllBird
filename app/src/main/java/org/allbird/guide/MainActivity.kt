package org.allbird.guide

import android.app.Activity
import android.os.Bundle
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.graphics.BitmapFactory
import android.os.Handler
import android.os.Looper
import android.widget.FrameLayout
import android.widget.ImageView
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.concurrent.Executors
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.View
import android.widget.*

private data class Bird(
    val english: String,
    val chinese: String,
    val scientific: String,
    val family: String,
    val description: String,
    val habitat: String,
    val range: String,
    val emoji: String,
    val color: Int
)

class MainActivity : Activity() {
    private var chinese = true
    private var page = "home"
    private var selectedCategory = "all"
    private var pendingQuery = ""
    private val favorites = mutableSetOf<String>()
    private val photoExecutor = Executors.newFixedThreadPool(3)
    private val photoCache = mutableMapOf<String, android.graphics.Bitmap>()
    private val photoAttributionCache = mutableMapOf<String, String>()
    private val mainHandler = Handler(Looper.getMainLooper())
    private lateinit var body: LinearLayout
    private lateinit var scroll: ScrollView
    private lateinit var nav: LinearLayout
    private lateinit var search: EditText

    private val ink = Color.rgb(31, 53, 43)
    private val muted = Color.rgb(105, 123, 111)
    private val paper = Color.rgb(246, 248, 244)
    private val green = Color.rgb(37, 91, 65)
    private val birds = listOf(
        Bird("Common Kingfisher", "普通翠鸟", "Alcedo atthis", "翠鸟科 / Alcedinidae",
            "Blue-and-orange fish hunter / 蓝橙相间、以鱼类为食", "Rivers, streams and lakes / 河流、溪流与湖泊", "Europe, Asia and North Africa / 欧洲、亚洲及北非", "🐦", 0xFFE2F0E9.toInt()),
        Bird("Mute Swan", "疣鼻天鹅", "Cygnus olor", "鸭科 / Anatidae",
            "Large white waterbird with an orange bill / 大型白色水鸟，橙色喙基部有黑色瘤突", "Lakes, ponds and coasts / 湖泊、池塘与海岸", "Europe and parts of Asia / 欧洲及亚洲部分地区", "🦢", 0xFFE5EEF4.toInt()),
        Bird("Barn Owl", "仓鸮", "Tyto alba", "草鸮科 / Tytonidae",
            "Nocturnal hunter with a pale heart-shaped facial disc / 夜行性猛禽，面盘呈浅色心形", "Farmland, grassland and open woodland / 农田、草地与疏林", "Widespread across several continents / 广布多个大陆", "🦉", 0xFFF3EBDD.toInt()),
        Bird("Eurasian Blue Tit", "蓝山雀", "Cyanistes caeruleus", "山雀科 / Paridae",
            "Small agile bird with a blue crown and yellow underside / 小型活跃鸟类，蓝色头顶、黄色腹部", "Woodland, gardens and parks / 林地、花园与公园", "Europe and western Asia / 欧洲及西亚", "🐤", 0xFFE9F0D9.toInt()),
        Bird("Peregrine Falcon", "游隼", "Falco peregrinus", "隼科 / Falconidae",
            "Powerful falcon known for its high-speed hunting stoop / 以高速俯冲捕猎闻名的强健隼类", "Cliffs, coasts and cities / 悬崖、海岸与城市", "Nearly worldwide / 几乎遍布全球", "🦅", 0xFFE9E9F2.toInt()),
        Bird("Eurasian Magpie", "喜鹊", "Pica pica", "鸦科 / Corvidae",
            "Intelligent black-and-white bird with a long tail / 黑白相间、长尾，行为聪明灵活", "Open woodland, farmland and towns / 疏林、农田与城镇", "Europe, North Africa and Asia / 欧洲、北非及亚洲", "🐦‍⬛", 0xFFEDE9E3.toInt()),
        Bird("Mallard", "绿头鸭", "Anas platyrhynchos", "鸭科 / Anatidae",
            "Common dabbling duck; males have a glossy green head / 常见涉水鸭，雄鸟头部呈金属绿色", "Wetlands, ponds and parks / 湿地、池塘与公园", "Northern Hemisphere / 北半球", "🦆", 0xFFE1EEE8.toInt()),
        Bird("White-tailed Eagle", "白尾海雕", "Haliaeetus albicilla", "鹰科 / Accipitridae",
            "Large sea eagle with broad wings and a pale tail / 大型海雕，翼宽，成鸟尾部浅色", "Coasts, lakes and large rivers / 海岸、湖泊与大河", "Northern Europe and Asia / 北欧及亚洲", "🦅", 0xFFE8ECE3.toInt()),
        Bird("Atlantic Puffin", "冠海雀", "Fratercula arctica", "海雀科 / Alcidae",
            "Stocky seabird with a colourful breeding-season bill / 体形敦实，繁殖季喙部色彩鲜艳", "North Atlantic islands and sea cliffs / 北大西洋岛屿与海崖", "North Atlantic / 北大西洋", "🐧", 0xFFE8EAF0.toInt()),
        Bird("Common Raven", "渡鸦", "Corvus corax", "鸦科 / Corvidae",
            "Large all-black corvid with a heavy bill / 大型通体黑色鸦类，喙粗壮", "Mountains, forests and open country / 山地、森林与开阔地", "Northern Hemisphere / 北半球", "🐦‍⬛", 0xFFE8E7E5.toInt()),
        Bird("Great Spotted Woodpecker", "大斑啄木鸟", "Dendrocopos major", "啄木鸟科 / Picidae",
            "Black-and-white woodpecker with red undertail coverts / 黑白羽色，尾下覆羽红色", "Woodlands, parks and mature gardens / 林地、公园与成熟庭园", "Europe and northern Asia / 欧洲及亚洲北部", "🐦", 0xFFECE9E2.toInt()),
        Bird("European Robin", "欧亚鸲", "Erithacus rubecula", "鹟科 / Muscicapidae",
            "Small songbird with a distinctive orange-red breast / 小型鸣禽，胸前橙红色斑块醒目", "Woodland, hedges and gardens / 林地、树篱与花园", "Europe and western Asia / 欧洲及西亚", "🐤", 0xFFF3E5DA.toInt()),
        Bird("Common Blackbird", "乌鸫", "Turdus merula", "鸫科 / Turdidae",
            "Male is glossy black with a bright yellow bill / 雄鸟通体乌黑，喙呈鲜黄色", "Gardens, woodland and parks / 花园、林地与公园", "Europe, North Africa and Asia / 欧洲、北非及亚洲", "🐦‍⬛", 0xFFE8E7E5.toInt()),
        Bird("Great Cormorant", "普通鸬鹚", "Phalacrocorax carbo", "鸬鹚科 / Phalacrocoracidae",
            "Dark fish-eating waterbird that often spreads its wings to dry / 深色食鱼水鸟，常张开双翼晾晒", "Coasts, estuaries, lakes and rivers / 海岸、河口、湖泊与河流", "Widespread across the Old World / 广布旧大陆", "🐦", 0xFFE1E9E8.toInt()),
        Bird("Grey Heron", "苍鹭", "Ardea cinerea", "鹭科 / Ardeidae",
            "Tall wading bird with a long neck and dagger-like bill / 高大的涉禽，长颈、长而尖的喙", "Wetlands, rivers and shorelines / 湿地、河流与岸边", "Europe, Asia and Africa / 欧洲、亚洲及非洲", "🪶", 0xFFE5EBED.toInt()),
        Bird("Common Crane", "灰鹤", "Grus grus", "鹤科 / Gruidae",
            "Large elegant crane with a red crown patch / 大型优雅鹤类，头顶有红色裸露皮肤", "Marshes, wetlands and open fields / 沼泽、湿地与开阔田野", "Europe and northern Asia / 欧洲及亚洲北部", "🪽", 0xFFE7EAE5.toInt()),
        Bird("Osprey", "鹗", "Pandion haliaetus", "鹗科 / Pandionidae",
            "Fish-eating raptor with reversible outer toes / 以鱼为食的猛禽，外趾可前后转换", "Coasts, lakes and large rivers / 海岸、湖泊与大河", "Nearly worldwide / 近全球分布", "🦅", 0xFFE6ECE8.toInt()),
        Bird("Golden Eagle", "金雕", "Aquila chrysaetos", "鹰科 / Accipitridae",
            "Large brown eagle with golden feathers on the nape / 大型褐色雕，后颈羽毛呈金黄色", "Mountains, moorland and open country / 山地、荒原与开阔地", "Northern Hemisphere / 北半球", "🦅", 0xFFECE6D8.toInt()),
        Bird("Common Swift", "普通雨燕", "Apus apus", "雨燕科 / Apodidae",
            "Aerial specialist that spends most of its life in flight / 高度适应空中生活，绝大多数时间在飞行", "Towns, cliffs and open skies / 城镇、悬崖与开阔天空", "Europe, Asia and Africa / 欧洲、亚洲及非洲", "🐦", 0xFFE6E9E9.toInt()),
        Bird("White Stork", "白鹳", "Ciconia ciconia", "鹳科 / Ciconiidae",
            "Large white stork with black flight feathers and red legs / 大型白色鹳类，飞羽黑色、腿红色", "Wet meadows, farmland and wetlands / 湿草地、农田与湿地", "Europe, western Asia and Africa / 欧洲、西亚及非洲", "🪽", 0xFFECEDE8.toInt()),
        Bird("Northern Cardinal", "北美红雀", "Cardinalis cardinalis", "红雀科 / Cardinalidae",
            "Male has vivid red plumage and a prominent crest / 雄鸟通体鲜红，头顶有明显冠羽", "Woodland edges, gardens and scrub / 林缘、花园与灌丛", "Eastern and central North America / 北美东部及中部", "🐦", 0xFFF2E1DD.toInt()),
        Bird("Bald Eagle", "白头海雕", "Haliaeetus leucocephalus", "鹰科 / Accipitridae",
            "Adult has a white head and tail with a dark brown body / 成鸟头尾白色，身体深褐色", "Coasts, lakes and large rivers / 海岸、湖泊与大河", "North America / 北美洲", "🦅", 0xFFE9E9E2.toInt()),
        Bird("Emperor Penguin", "帝企鹅", "Aptenodytes forsteri", "企鹅科 / Spheniscidae",
            "The tallest and heaviest living penguin species / 现存体形最高、最重的企鹅", "Antarctic sea ice and surrounding ocean / 南极海冰及周边海域", "Antarctica / 南极洲", "🐧", 0xFFE2E9EF.toInt()),
        Bird("Shoebill", "鲸头鹳", "Balaeniceps rex", "鲸头鹳科 / Balaenicipitidae",
            "Large African wetland bird with a massive shoe-shaped bill / 非洲大型湿地鸟类，喙巨大呈鞋状", "Tropical swamps and papyrus wetlands / 热带沼泽与纸莎草湿地", "Central and East Africa / 中非及东非", "🪽", 0xFFE9E5D9.toInt()),
        Bird("Hummingbird", "蜂鸟（示例类群）", "Trochilidae", "蜂鸟科 / Trochilidae",
            "Tiny hovering nectar-feeding birds; this entry represents the family / 能悬停取食花蜜的小型鸟类；本条目代表整个科", "Forests, gardens and mountain habitats in the Americas / 美洲森林、花园及山地", "The Americas / 美洲", "🐦", 0xFFE7EEE0.toInt())
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = paper
        window.navigationBarColor = Color.WHITE
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        buildShell()
        render()
    }

    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()

    private fun rounded(color: Int, radius: Int = 18, stroke: Int = 0): GradientDrawable =
        GradientDrawable().apply {
            setColor(color)
            cornerRadius = dp(radius).toFloat()
            if (stroke != 0) setStroke(dp(1), stroke)
        }

    private fun text(value: String, size: Float = 14f, color: Int = ink, bold: Boolean = false): TextView =
        TextView(this).apply {
            text = value
            textSize = size
            setTextColor(color)
            if (bold) setTypeface(null, Typeface.BOLD)
            setPadding(dp(2), dp(3), dp(2), dp(3))
        }

    private fun space(height: Int): View = View(this).apply {
        layoutParams = LinearLayout.LayoutParams(1, dp(height))
    }

    private fun column(): LinearLayout = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
    }

    private fun row(): LinearLayout = LinearLayout(this).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
    }

    private fun pill(label: String, active: Boolean, action: () -> Unit): TextView =
        TextView(this).apply {
            text = label
            textSize = 13f
            setTextColor(if (active) Color.WHITE else green)
            gravity = Gravity.CENTER
            setPadding(dp(15), dp(9), dp(15), dp(9))
            background = rounded(if (active) green else Color.WHITE, 24, if (active) 0 else 0xFFDCE6DE.toInt())
            setOnClickListener { action() }
        }

    private fun buildShell() {
        val root = column().apply { setBackgroundColor(paper) }
        val header = row().apply {
            setPadding(dp(20), dp(12), dp(18), dp(8))
            setBackgroundColor(paper)
        }
        val brand = column()
        brand.addView(text("AllBird", 24f, green, true))
        brand.addView(text(if (chinese) "探索自然 · 认识鸟类" else "Explore nature · Discover birds", 11f, muted))
        header.addView(brand, LinearLayout.LayoutParams(0, -2, 1f))
        header.addView(TextView(this).apply {
            text = if (chinese) "中 / EN" else "EN / 中"
            textSize = 13f
            setTextColor(green)
            gravity = Gravity.CENTER
            background = rounded(Color.WHITE, 20, 0xFFDCE6DE.toInt())
            setPadding(dp(13), dp(9), dp(13), dp(9))
            setOnClickListener {
                chinese = !chinese
                render()
            }
        })
        root.addView(header)

        scroll = ScrollView(this).apply { clipToPadding = false }
        body = column().apply { setPadding(dp(18), dp(8), dp(18), dp(20)) }
        scroll.addView(body)
        root.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))

        nav = row().apply {
            setBackgroundColor(Color.WHITE)
            setPadding(dp(8), dp(7), dp(8), dp(7))
            elevation = dp(8).toFloat()
        }
        root.addView(nav)
        setContentView(root)
    }

    private fun addSectionTitle(title: String, subtitle: String = "") {
        body.addView(text(title, 20f, ink, true))
        if (subtitle.isNotBlank()) body.addView(text(subtitle, 12f, muted))
        body.addView(space(12))
    }

    private fun addCard(view: View, marginBottom: Int = 10) {
        val p = LinearLayout.LayoutParams(-1, -2)
        p.bottomMargin = dp(marginBottom)
        body.addView(view, p)
    }

    private fun birdCard(bird: Bird, compact: Boolean = false) {
        val card = row().apply {
            setPadding(dp(13), dp(12), dp(13), dp(12))
            background = rounded(Color.WHITE, 18, 0xFFE8EDE8.toInt())
            setOnClickListener { showDetail(bird) }
        }
        val art = TextView(this).apply {
            text = bird.emoji
            textSize = if (compact) 28f else 34f
            gravity = Gravity.CENTER
            background = rounded(bird.color, 15)
        }
        card.addView(art, LinearLayout.LayoutParams(dp(if (compact) 58 else 68), dp(if (compact) 58 else 68)))
        val info = column().apply { setPadding(dp(12), 0, dp(4), 0) }
        info.addView(text(if (chinese) bird.chinese else bird.english, 16f, ink, true))
        info.addView(text(if (chinese) bird.english else bird.chinese, 12f, muted))
        info.addView(text(bird.scientific, 11f, muted))
        card.addView(info, LinearLayout.LayoutParams(0, -2, 1f))
        card.addView(TextView(this).apply {
            text = if (favorites.contains(bird.scientific)) "♥" else "♡"
            textSize = 23f
            setTextColor(if (favorites.contains(bird.scientific)) 0xFFC45B58.toInt() else muted)
            setPadding(dp(8), dp(8), dp(3), dp(8))
            setOnClickListener {
                if (!favorites.add(bird.scientific)) favorites.remove(bird.scientific)
                render()
            }
        })
        addCard(card)
    }

    private fun render() {
        body.removeAllViews()
        when (page) {
            "home" -> showHome()
            "guide" -> showGuide()
            "identify" -> showIdentify()
            "saved" -> showSaved()
            else -> showHome()
        }
        renderNav()
    }

    private fun showHome() {
        val hero = column().apply {
            setPadding(dp(19), dp(20), dp(19), dp(19))
            background = rounded(0xFFE1EDE4.toInt(), 22)
        }
        hero.addView(text(if (chinese) "让每一次遇见，\n都成为自然发现。" else "Every encounter\nis a discovery.", 25f, green, true))
        hero.addView(space(8))
        hero.addView(text(if (chinese) "从身边常见鸟类，到世界各地的野生鸟类。" else "From familiar backyard birds to wildlife around the world.", 13f, muted))
        hero.addView(space(15))
        hero.addView(TextView(this).apply {
            text = if (chinese) "开始探索  →" else "Start exploring  →"
            textSize = 14f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            setPadding(dp(15), dp(11), dp(15), dp(11))
            background = rounded(green, 22)
            setOnClickListener { page = "guide"; render() }
        })
        addCard(hero, 18)

        val searchBox = EditText(this).apply {
            hint = if (chinese) "点击搜索鸟名、英文名或学名" else "Tap to search species"
            textSize = 14f
            setSingleLine(true)
            isFocusable = false
            setPadding(dp(15), dp(12), dp(15), dp(12))
            background = rounded(Color.WHITE, 15, 0xFFE2E9E2.toInt())
            setOnClickListener { page = "guide"; render() }
        }
        addCard(searchBox, 18)

        addSectionTitle(if (chinese) "快速探索" else "Explore", if (chinese) "选择一个入口开始" else "Choose where to begin")
        val shortcuts = row()
        listOf(
            Triple("📚", if (chinese) "鸟类图鉴" else "Bird guide", "guide"),
            Triple("📷", if (chinese) "拍照识别" else "Photo ID", "identify"),
            Triple("♥", if (chinese) "我的收藏" else "Saved", "saved")
        ).forEach { item ->
            val tile = column().apply {
                gravity = Gravity.CENTER
                setPadding(dp(8), dp(13), dp(8), dp(13))
                background = rounded(Color.WHITE, 16, 0xFFE8EDE8.toInt())
                addView(text(item.first, 24f, green, true))
                addView(text(item.second, 12f, ink, true))
                setOnClickListener { page = item.third; render() }
            }
            val lp = LinearLayout.LayoutParams(0, -2, 1f)
            lp.marginEnd = dp(7)
            shortcuts.addView(tile, lp)
        }
        body.addView(shortcuts)
        body.addView(space(20))
        addSectionTitle(if (chinese) "鸟类精选" else "Featured birds", if (chinese) "从这几种鸟开始认识" else "A few species to get you started")
        birds.take(3).forEach { birdCard(it, true) }
        body.addView(text(if (chinese) "当前收录 25 个示例条目（含一个科级示例）；全球完整名录仍待接入。" else "25 sample entries (including one family-level example). A complete global checklist is still to be integrated.", 11f, muted))
    }

    private fun showGuide() {
        addSectionTitle(if (chinese) "鸟类图鉴" else "Bird guide", if (chinese) "按名称搜索，打开条目查看详情" else "Search by name and open a species profile")
        val field = EditText(this).apply {
            hint = if (chinese) "搜索中英文名称或学名" else "Search names or scientific name"
            textSize = 14f
            setSingleLine(true)
            setText(pendingQuery)
            setPadding(dp(14), dp(11), dp(14), dp(11))
            background = rounded(Color.WHITE, 14, 0xFFE2E9E2.toInt())
        }
        addCard(field, 12)
        val categories = row()
        listOf("all", "water", "raptor", "garden").forEach { key ->
            val label = when (key) {
                "all" -> if (chinese) "全部" else "All"
                "water" -> if (chinese) "水鸟" else "Water"
                "raptor" -> if (chinese) "猛禽" else "Raptors"
                else -> if (chinese) "林园鸟" else "Garden"
            }
            val chip = pill(label, selectedCategory == key) { selectedCategory = key; render() }
            val lp = LinearLayout.LayoutParams(-2, -2)
            lp.marginEnd = dp(6)
            categories.addView(chip, lp)
        }
        body.addView(categories)
        body.addView(space(13))
        val list = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        fun populate(q: String) {
            list.removeAllViews()
            val filtered = birds.filter { b ->
                val textMatch = q.isBlank() || listOf(b.english, b.chinese, b.scientific).any { it.contains(q, true) }
                val categoryMatch = when (selectedCategory) {
                    "water" -> b.scientific in listOf("Cygnus olor", "Anas platyrhynchos", "Alcedo atthis", "Fratercula arctica", "Phalacrocorax carbo", "Ardea cinerea", "Grus grus", "Ciconia ciconia", "Aptenodytes forsteri", "Balaeniceps rex")
                    "raptor" -> b.scientific in listOf("Falco peregrinus", "Haliaeetus albicilla", "Tyto alba", "Pandion haliaetus", "Aquila chrysaetos", "Haliaeetus leucocephalus")
                    "garden" -> b.scientific in listOf("Cyanistes caeruleus", "Pica pica", "Erithacus rubecula", "Turdus merula", "Cardinalis cardinalis", "Dendrocopos major")
                    else -> true
                }
                textMatch && categoryMatch
            }
            if (filtered.isEmpty()) list.addView(text(if (chinese) "没有找到匹配的鸟种。" else "No matching species.", 13f, muted))
            filtered.forEach { b ->
                val card = makeBirdCard(b)
                list.addView(card)
                val lp = card.layoutParams as? LinearLayout.LayoutParams
                lp?.bottomMargin = dp(9)
            }
        }
        field.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { pendingQuery = s?.toString().orEmpty(); populate(pendingQuery) }
            override fun afterTextChanged(s: Editable?) = Unit
        })
        body.addView(list)
        populate(pendingQuery)
    }

    private fun makeBirdCard(bird: Bird): View {
        val card = row().apply {
            setPadding(dp(12), dp(11), dp(12), dp(11))
            background = rounded(Color.WHITE, 17, 0xFFE7ECE7.toInt())
            setOnClickListener { showDetail(bird) }
        }
        val photo = ImageView(this).apply {
            scaleType = ImageView.ScaleType.CENTER_CROP
            background = rounded(bird.color, 14)
            visibility = View.INVISIBLE
        }
        val photoFrame = FrameLayout(this).apply {
            background = rounded(bird.color, 14)
            addView(TextView(this@MainActivity).apply {
                text = bird.emoji
                textSize = 28f
                gravity = Gravity.CENTER
            }, FrameLayout.LayoutParams(-1, -1))
            addView(photo, FrameLayout.LayoutParams(-1, -1))
        }
        card.addView(photoFrame, LinearLayout.LayoutParams(dp(58), dp(58)))
        loadBirdPhoto(bird, photo)
        val info = column().apply { setPadding(dp(11), 0, 0, 0) }
        info.addView(text(if (chinese) bird.chinese else bird.english, 15f, ink, true))
        info.addView(text(bird.english + " · " + bird.scientific, 11f, muted))
        card.addView(info, LinearLayout.LayoutParams(0, -2, 1f))
        card.addView(text("›", 25f, muted))
        return card
    }

    private fun showDetail(bird: Bird) {
        body.removeAllViews()
        body.addView(TextView(this).apply {
            text = if (chinese) "‹ 返回图鉴" else "‹ Back to guide"
            textSize = 14f
            setTextColor(green)
            setPadding(0, dp(5), 0, dp(15))
            setOnClickListener { page = "guide"; render() }
        })
        val banner = column().apply {
            gravity = Gravity.CENTER
            setPadding(dp(20), dp(28), dp(20), dp(28))
            background = rounded(bird.color, 22)
            val heroPhoto = ImageView(this@MainActivity).apply {
                scaleType = ImageView.ScaleType.CENTER_CROP
                background = rounded(bird.color, 16)
                visibility = View.INVISIBLE
            }
            addView(FrameLayout(this@MainActivity).apply {
                addView(TextView(this@MainActivity).apply {
                    text = bird.emoji
                    textSize = 58f
                    gravity = Gravity.CENTER
                }, FrameLayout.LayoutParams(-1, -1))
                addView(heroPhoto, FrameLayout.LayoutParams(-1, -1))
            }, LinearLayout.LayoutParams(-1, dp(190)))
            loadBirdPhoto(bird, heroPhoto, photoCredit)
            addView(text(if (chinese) bird.chinese else bird.english, 23f, ink, true))
            addView(text(if (chinese) bird.english else bird.chinese, 14f, muted))
        }
        addCard(banner, 5)
        val photoCredit = text(if (chinese) "照片来源与许可信息加载中…" else "Loading photo source and license…", 10f, muted)
        body.addView(photoCredit)
        body.addView(text(bird.scientific, 16f, green, true))
        body.addView(text(bird.family, 13f, muted))
        body.addView(space(18))
        body.addView(text(if (chinese) "识别特征" else "Description", 17f, ink, true))
        body.addView(text(bird.description, 14f, muted))
        body.addView(space(15))
        body.addView(text(if (chinese) "栖息环境" else "Habitat", 17f, ink, true))
        body.addView(text(bird.habitat, 14f, muted))
        body.addView(space(15))
        body.addView(text(if (chinese) "分布范围" else "Range", 17f, ink, true))
        body.addView(text(bird.range, 14f, muted))
        body.addView(space(20))
        body.addView(TextView(this).apply {
            text = if (favorites.contains(bird.scientific)) (if (chinese) "♥ 已收藏" else "♥ Saved") else (if (chinese) "♡ 加入收藏" else "♡ Save species")
            textSize = 14f
            gravity = Gravity.CENTER
            setTextColor(Color.WHITE)
            setPadding(dp(16), dp(13), dp(16), dp(13))
            background = rounded(green, 22)
            setOnClickListener {
                if (!favorites.add(bird.scientific)) favorites.remove(bird.scientific)
                showDetail(bird)
            }
        })
    }

    private fun loadBirdPhoto(bird: Bird, target: ImageView, creditView: TextView? = null) {
        target.tag = bird.scientific
        val cached = synchronized(photoCache) { photoCache[bird.scientific] }
        val cachedCredit = synchronized(photoAttributionCache) { photoAttributionCache[bird.scientific] }
        if (cached != null) {
            target.setImageBitmap(cached)
            target.visibility = View.VISIBLE
            if (creditView != null && cachedCredit != null) creditView.text = cachedCredit
            return
        }
        photoExecutor.execute {
            try {
                val query = URLEncoder.encode("\"${bird.scientific}\"", "UTF-8")
                val api = URL("https://commons.wikimedia.org/w/api.php?action=query&generator=search&gsrsearch=$query&gsrnamespace=6&gsrlimit=8&prop=imageinfo&iiprop=url|extmetadata&iiurlwidth=640&format=json")
                val connection = (api.openConnection() as HttpURLConnection).apply {
                    connectTimeout = 9000
                    readTimeout = 9000
                    setRequestProperty("User-Agent", "AllBirdAndroid/1.0 (bird guide app)")
                }
                val json = connection.inputStream.bufferedReader().use { it.readText() }
                connection.disconnect()
                val pages = JSONObject(json).optJSONObject("query")?.optJSONObject("pages")
                val candidates = mutableListOf<Triple<Int, String, JSONObject>>()
                val keys = pages?.keys()
                while (keys != null && keys.hasNext()) {
                    val page = pages.optJSONObject(keys.next()) ?: continue
                    val title = page.optString("title").substringAfter("File:").replace('_', ' ')
                    val info = page.optJSONArray("imageinfo")?.optJSONObject(0) ?: continue
                    val metadata = info.optJSONObject("extmetadata") ?: JSONObject()
                    val description = metadata.optJSONObject("ImageDescription")?.optString("value").orEmpty()
                    val searchable = (title + " " + description).replace(Regex("<[^>]+>"), "").lowercase()
                    val scientificTokens = bird.scientific.lowercase().split(" ")
                    val commonTokens = bird.english.lowercase().split(" ").filter { it.length > 3 }
                    var score = 0
                    if (searchable.contains(bird.scientific.lowercase())) score += 100
                    if (searchable.contains(bird.english.lowercase())) score += 90
                    score += scientificTokens.count { searchable.contains(it) } * 22
                    score += commonTokens.count { searchable.contains(it) } * 8
                    candidates.add(Triple(score, title, info))
                }
                val best = candidates.maxByOrNull { it.first }
                if (best != null && best.first >= 22) {
                    val info = best.third
                    val imageUrl = info.optString("thumburl").ifBlank { info.optString("url") }
                    if (imageUrl.isNotBlank()) {
                        val imageConnection = (URL(imageUrl).openConnection() as HttpURLConnection).apply {
                            connectTimeout = 9000
                            readTimeout = 12000
                        }
                        val bitmap = imageConnection.inputStream.use { BitmapFactory.decodeStream(it) }
                        imageConnection.disconnect()
                        val metadata = info.optJSONObject("extmetadata") ?: JSONObject()
                        fun cleanMeta(key: String): String = metadata.optJSONObject(key)?.optString("value")
                            .orEmpty().replace(Regex("<[^>]+>"), "").replace("&nbsp;", " ").trim()
                        val artist = cleanMeta("Artist").ifBlank { "Unknown author" }
                        val license = cleanMeta("LicenseShortName").ifBlank { "License details unavailable" }
                        val credit = "Wikimedia Commons · ${best.second} · © $artist · $license"
                        if (bitmap != null) {
                            synchronized(photoCache) { photoCache[bird.scientific] = bitmap }
                            synchronized(photoAttributionCache) { photoAttributionCache[bird.scientific] = credit }
                            mainHandler.post {
                                if (target.tag == bird.scientific) {
                                    target.setImageBitmap(bitmap)
                                    target.visibility = View.VISIBLE
                                }
                                if (creditView != null && creditView.isAttachedToWindow) creditView.text = credit
                            }
                        }
                    }
                } else {
                    mainHandler.post {
                        if (creditView != null && creditView.isAttachedToWindow) {
                            creditView.text = if (chinese) "未找到足够匹配的照片，已保留示意图。" else "No sufficiently matched photo; illustration retained."
                        }
                    }
                }
            } catch (_: Exception) {
                mainHandler.post {
                    if (creditView != null && creditView.isAttachedToWindow) {
                        creditView.text = if (chinese) "照片暂不可用；请检查网络连接。" else "Photo unavailable; check the network connection."
                    }
                }
            }
        }
    }

    private fun showIdentify() {
        addSectionTitle(if (chinese) "鸟类识别" else "Bird identification", if (chinese) "拍摄照片或录制鸟鸣，开启观察记录" else "Use a photo or a bird call to start an observation")
        val photo = column().apply {
            gravity = Gravity.CENTER
            setPadding(dp(18), dp(25), dp(18), dp(25))
            background = rounded(0xFFE3EEE6.toInt(), 20)
            addView(text("📷", 42f))
            addView(text(if (chinese) "照片识别" else "Photo identification", 18f, green, true))
            addView(text(if (chinese) "选择照片或调用相机" else "Choose an image or open the camera", 12f, muted))
            addView(space(12))
            addView(TextView(this@MainActivity).apply {
                text = if (chinese) "模型待接入" else "Model pending"
                textSize = 12f
                setTextColor(green)
                setPadding(dp(13), dp(8), dp(13), dp(8))
                background = rounded(Color.WHITE, 18)
            })
        }
        addCard(photo, 12)
        val audio = column().apply {
            gravity = Gravity.CENTER
            setPadding(dp(18), dp(24), dp(18), dp(24))
            background = rounded(Color.WHITE, 20, 0xFFE5EAE5.toInt())
            addView(text("🎙️", 40f))
            addView(text(if (chinese) "鸟鸣识别" else "Bird-call identification", 18f, green, true))
            addView(text(if (chinese) "录制或导入鸟鸣音频" else "Record or import a bird-call audio clip", 12f, muted))
            addView(space(12))
            addView(text(if (chinese) "音频模型待接入" else "Audio model pending", 12f, muted))
        }
        addCard(audio, 15)
        body.addView(text(if (chinese) "当前版本说明" else "Current release", 16f, ink, true))
        body.addView(text(if (chinese)
            "识别页面目前是功能入口，尚未调用相机、录音或推理模型。下一阶段将先接入图像识别，再加入鸟鸣识别，并保留离线与在线两种路径。"
            else "This page is currently a feature preview. Camera, recording and inference are not connected yet. Image recognition will be integrated first, followed by audio recognition, with both offline and online paths planned.", 13f, muted))
    }

    private fun showSaved() {
        addSectionTitle(if (chinese) "我的收藏" else "Saved species", if (chinese) "保存你想继续认识的鸟类" else "Keep species you want to revisit")
        val saved = birds.filter { favorites.contains(it.scientific) }
        if (saved.isEmpty()) {
            body.addView(text(if (chinese) "这里还没有收藏。打开鸟种条目，点击“加入收藏”即可保存。" else "No saved species yet. Open a species profile and tap Save species.", 14f, muted))
        } else saved.forEach { birdCard(it, true) }
    }

    private fun renderNav() {
        nav.removeAllViews()
        listOf(
            Triple("⌂", if (chinese) "首页" else "Home", "home"),
            Triple("▤", if (chinese) "图鉴" else "Guide", "guide"),
            Triple("◎", if (chinese) "识别" else "Identify", "identify"),
            Triple("♥", if (chinese) "收藏" else "Saved", "saved")
        ).forEach { item ->
            val active = page == item.third
            val cell = column().apply {
                gravity = Gravity.CENTER
                setPadding(dp(5), dp(3), dp(5), dp(3))
                addView(text(item.first, 20f, if (active) green else muted, true))
                addView(text(item.second, 10f, if (active) green else muted, active))
                setOnClickListener { page = item.third; render() }
            }
            nav.addView(cell, LinearLayout.LayoutParams(0, -2, 1f))
        }
    }
}
