package com.example.data

import android.util.Log
import com.example.BuildConfig
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.util.concurrent.TimeUnit

object GeminiService {
    private const val TAG = "GeminiService"
    private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    // Request structures
    data class GeminiRequest(val contents: List<Content>, val systemInstruction: Content? = null)
    data class Content(val parts: List<Part>)
    data class Part(val text: String)

    // Response structures
    data class GeminiResponse(val candidates: List<Candidate>?)
    data class Candidate(val content: ResponseContent?)
    data class ResponseContent(val parts: List<ResponsePart>?)
    data class ResponsePart(val text: String?)

    suspend fun getSaudiHRAdvice(prompt: String): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            Log.w(TAG, "API Key is missing or placeholder. Utilizing rich local Saudi compliance knowledge engine.")
            return@withContext getLocalSaudiLaborAdvice(prompt)
        }

        val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"

        val systemInstruction = "أنت المستشار العمالي والذكي لنظام شوامخ لإدارة الموارد البشرية وشؤون الموظفين المتوافق مع نظام العمل السعودي واللائحة التنفيذية وقانون حماية البيانات الشخصية السعودي (PDPL). أجب باحترافية عمالية مستشهداً بمواد نظام العمل السعودي (مثل المادة 74 لإنهاء العقود، المادة 80 للمخالفات، المادة 109 للإجازات السنوية، المادة 117 للإجازات المرضية، وحساب مكافأة نهاية الخدمة بدقة، واشتراكات التأمينات الاجتماعية المؤسسة العامة للتأمينات الاجتماعية GOSI). كن ودوداً وبليغاً باللغة العربية مع دعم مصطلحات إنجليزية تقنية مناسبة."

        val requestObj = GeminiRequest(
            contents = listOf(
                Content(parts = listOf(Part(text = prompt)))
            ),
            systemInstruction = Content(parts = listOf(Part(text = systemInstruction)))
        )

        val adapter = moshi.adapter(GeminiRequest::class.java)
        val requestJson = adapter.toJson(requestObj)

        val request = Request.Builder()
            .url(url)
            .post(requestJson.toRequestBody(JSON_MEDIA_TYPE))
            .build()

        try {
            okHttpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    Log.e(TAG, "Error from Gemini REST: code ${response.code}. Falling back to internal engine.")
                    return@withContext getLocalSaudiLaborAdvice(prompt)
                }

                val responseBody = response.body?.string() ?: ""
                val responseAdapter = moshi.adapter(GeminiResponse::class.java)
                val geminiResponse = responseAdapter.fromJson(responseBody)

                val reply = geminiResponse?.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                if (!reply.isNullOrBlank()) {
                    return@withContext reply
                } else {
                    return@withContext getLocalSaudiLaborAdvice(prompt)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception contacting Gemini API: ${e.message}. Falling back to local engine.", e)
            return@withContext getLocalSaudiLaborAdvice(prompt)
        }
    }

    // A comprehensive offline rule engine that mimics Gemini responding to common Saudi HR or Labor queries
    private fun getLocalSaudiLaborAdvice(prompt: String): String {
        val lowercasePrompt = prompt.lowercase()
        return when {
            lowercasePrompt.contains("إجاز") || lowercasePrompt.contains("leave") || lowercasePrompt.contains("vacation") -> """
                **إليك التوجيه القانوني طبقاً للمادة (109) من نظام العمل السعودي:**
                
                1. **الإجازة السنوية الاعتيادية:** يستحق الموظف إجازة سنوية مدفوعة الأجر لا تقل عن **21 يوماً** عن كل عام، وتزاد الإجازة إلى مدة لا تقل عن **30 يوماً** إذا بلغت خدمة الموظف في المؤسسة خمس سنوات متصلة.
                2. **أجر الإجازة:** يُدفع أجر الإجازة للموظف مقدماً قبل بدء الإجازة.
                3. **التأجيل والتنازل:** لا يجوز النزول عن الإجازة أو تأجيلها لأكثر من سنة تالية لمسير العمل بالتراضي (طبقاً لموافقة خطية من الموظف وصاحب العمل).
                4. **المادة (117) - الإجازة المرضية:** يستحق العامل الذي يثبت مرضه بشهادة طبية معتمدة إجازة مرضية خلال السنة الواحدة:
                   * أول 30 يوماً: بأجر كامل.
                   * الـ 60 يوماً التالية: بثلاثة أرباع الأجر.
                   * الـ 30 يوماً التالية لطلب التمديد: دون أجر.
            """.trimIndent()

            lowercasePrompt.contains("نهاية الخدمة") || lowercasePrompt.contains("service") || lowercasePrompt.contains("eos") || lowercasePrompt.contains("مكافأ") -> """
                **إليك حاسبة الاستحقاق القانوني لمكافأة نهاية الخدمة طبقاً للمادة (84) من نظام العمل:**
                
                * يُحسب الأجر الأخير كأجر أساسي لتقدير المكافأة.
                * **السنوات الخمس الأولى:** يستحق العامل نصف أجر شهر عن كل سنة من السنوات الخمس الأولى.
                * **السنوات التالية:** يستحق العامل أجر شهر كامل عن كل سنة تالية للسنوات الخمس الأولى.
                * **الاستقالة طبقاً للمادة (85):** إذا انتهت علاقة العمل برغبة العامل (الاستقالة):
                   * الخدمة أقل من سنتين: لا يستحق مكافأة نهاية خدمة.
                   * الخدمة من 2 إلى 5 سنوات: يستحق **ثلث** المكافأة المقررة.
                   * الخدمة من 5 إلى 10 سنوات: يستحق **ثلثي** المكافأة المقررة.
                   * الخدمة 10 سنوات فما فوق: يستحق المكافأة كاملة.
            """.trimIndent()

            lowercasePrompt.contains("تأمينات") || lowercasePrompt.contains("gosi") || lowercasePrompt.contains("راتب") || lowercasePrompt.contains("خصم") -> """
                **دليل الأجور واشتراكات التأمينات الاجتماعية (GOSI) في المملكة العربية السعودية:**
                
                1. **المواطنون السعوديون:** نسبة الاشتراك الإجمالية لفرع الأخطار والتعطل والمعاشات تبلغ **21.5%** من الأجر الخاضع للاشتراك (الأساسي + بدل السكن):
                   * يتحمل **العامل السعودي 9.75%** (يتم استقطاعها مباشرة بمسير رواتب شوامخ كحسم تقاعد).
                   * يتحمل **صاحب العمل 11.75%** (تعتبر عبئاً وظيفياً تتحمله الشركة).
                2. **الأجانب والمقيمون:** يتحمل صاحب العمل **2%** فقط فرع الأخطار المهنية، ولا يُستقطع أي قسط تقاعدي من العامل المقيم.
                3. **نظام مدد لحماية الأجور:** يلزم نظام العمل السعودي الشركات برفع ملفات مسيرات الأجور والرواتب عبر منصة مدد شهرياً لمتابعة الالتزام وتجنب عقوبات حسم الأجور غير المبرر.
            """.trimIndent()

            lowercasePrompt.contains("عقد") || lowercasePrompt.contains("contract") || lowercasePrompt.contains("فصل") || lowercasePrompt.contains("مادة 74") || lowercasePrompt.contains("مادة 80") -> """
                **تنظيم العقود والإنهاء في ضوء نظام العمل السعودي:**
                
                1. **العقد محدد المدة:** ينتهي بانتهاء مدته المتفق عليها. إذا رغب الطرفان في الاستمرار وتجاوزت فترات العمل المتعددة 3 سنوات متتالية أو تجدد العقد 3 مرات، يتحول العقد تلقائياً إلى عقد غير محدد المدة للمواطن السعودي.
                2. **المادة (75) - الإنهاء غير المحدد المدة:** يجب أن يوجه طلب الإنهاء بناءً على سبب مشروع بموجب إخطار مكتوب قبل الإنهاء بمدة لا تقل عن **60 يوماً** إذا كان أجر العامل يتقاضى شهرياً، أو **30 يوماً** لغير ذلك.
                3. **المادة (80) - الفصل بدون مكافأة أو إشعار:** لا يجوز فسخ العقد دون مكافأة أو تعويض إلا في حالات محددة وحصرية (مثل الاعتداء على صاحب العمل، عدم القيام بمهام العمل الأساسية متعمداً بعد إنذاره خطياً، إفشاء الأسرار الصناعية أو التجارية، التغيب دون عذر مشروع أكثر من 30 يوماً متقطعة خلال السنة ذاتها أو أكثر من 15 يوماً متتالية).
            """.trimIndent()

            else -> """
                أهلاً بك في مستشار **شوامخ** الذكي للموارد البشرية والأنظمة العمالية السعودية. 
                
                أنظمة ونقاط الاستشارة السريعة المدعومة:
                1. **احتساب الإجازات وضوابط المادة (109) و (117)**.
                2. **تصفية وحساب مكافأة نهاية الخدمة بدقة طبقاً للمادة (84) و (85)**.
                3. **نسب اشتراكات التأمينات الاجتماعية (GOSI) ومطابقة رواتب نظام مدد**.
                4. **بند العقود، وسلامة الإنهاء بموجب المادتين (74) و (75) ونطاق فصل المادة (80)**.
                5. **مؤشرات نطاقات (Saudization - Saudization Levels) وضوابط حماية البيانات الشخصية ومراقبة الـ Audit Logs**.
                
                فضلاً اطرح استفسارك، وسأقوم بتحليله وتقديم صياغة متكاملة ومتوافقة مع وزارة الموارد البشرية والتنمية الاجتماعية السعودية.
            """.trimIndent()
        }
    }
}
