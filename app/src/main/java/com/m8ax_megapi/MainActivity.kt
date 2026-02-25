package com.m8ax_megapi

import android.animation.ArgbEvaluator
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.ToneGenerator
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Calendar
import java.util.Locale
import java.util.Random

class MainActivity : AppCompatActivity() {
    private var ramTotalGB: Double = 0.0
    private var calculando = false
    private var currentTextSize = 8f
    private var contadorespe = 0
    private var ultimosMillonesPulsados: String = ""
    private var modoLoopActivo = false
    private var colorAnim: ObjectAnimator? = null
    private lateinit var tvNotaRam: TextView
    private var tts: TextToSpeech? = null
    private var ttsEnabled: Boolean = false
    private var grabarEnNotasEnabled: Boolean = false
    private var contadorLoop = 1
    private val botonesPiIds = listOf(
        R.id.btn1M,
        R.id.btn2M,
        R.id.btn3M,
        R.id.btn4M,
        R.id.btn5M,
        R.id.btn6M,
        R.id.btn7M,
        R.id.btn8M,
        R.id.btn9M,
        R.id.btn10M,
        R.id.btn20M,
        R.id.btn40M,
        R.id.btn60M,
        R.id.btn80M,
        R.id.btn100M,
        R.id.btn200M,
        R.id.btn300M,
        R.id.btn400M,
        R.id.btn500M,
        R.id.btn600M,
        R.id.btn700M,
        R.id.btn800M,
        R.id.btn900M,
        R.id.btn1000M,
        R.id.btn1200M,
        R.id.btn1400M,
        R.id.btn1600M,
        R.id.btn1800M,
        R.id.btn2000M,
        R.id.btnCompartir
    )

    companion object {
        init {
            try {
                System.loadLibrary("gmp")
                System.loadLibrary("m8ax_megapi")
            } catch (e: UnsatisfiedLinkError) {
                android.util.Log.e("M8AX", "Error Cargando Librerías")
            }
        }
    }

    external fun startCalculation(cantidad: Long)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ttsEnabled = CargarEstadoTts()
        grabarEnNotasEnabled =
            getSharedPreferences("M8AX-ConfigNotas", Context.MODE_PRIVATE).getBoolean(
                "GrabarNotas", false
            )
        setContentView(R.layout.activity_main)
        val mainView = findViewById<View>(R.id.main_container)
        mainView?.let { vista ->
            ViewCompat.setOnApplyWindowInsetsListener(vista) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                insets
            }
        }
        tts = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.setLanguage(tts?.defaultLanguage ?: Locale.getDefault())
                tts?.setSpeechRate(0.9f)
                if (ttsEnabled) {
                    val saludos = arrayOf(
                        "Motor En Marcha.",
                        "M 8 A X; Listo.",
                        "Sistema Iniciado.",
                        "Hola Que Tal.",
                        "Motor Preparado.",
                        "Transformada De Fourier En Espera.",
                        "Núcleo Positrónico Activado.",
                        "C P U, Procesando En Red Neural.",
                        "Núcleos De Rendimiento Preparados.",
                        "Branch Predictor Preparado.",
                        "No Hay Cuchara, Solo Datos.",
                        "Buscando El Fin De Pi.",
                        "Algoritmo Chudnovsky A Full.",
                        "Bienvenido Al Desierto De Lo Real.",
                        "Transistores Al Límite.",
                        "Tu C P U Es El Arquitecto.",
                        "F F T Calculando El Caos.",
                        "Dígitos De Pi Sincronizados.",
                        "Código Nativo Sin Filtros.",
                        "Precisión Arbitraria Alcanzada.",
                        "Voltaje En Punto Crítico.",
                        "Tu Silicio Está Despertando.",
                        "M 8 A X: El Oráculo Del Hardware.",
                        "Decimales Infinitos Detectados.",
                        "Newton Iterando La Verdad.",
                        "Sé Kung Fu Digital.",
                        "HAL 9000 Conectado.",
                        "Protocolo Skynet Iniciado.",
                        "Exigiendo El Máximo Al Silicio.",
                        "Carga Extrema Verificada.",
                        "Sueñan Los Androides.",
                        "He Visto Cosas Increíbles.",
                        "Inestabilidad No Detectada.",
                        "Hackeando El Límite Térmico.",
                        "Sin Piedad Con Los Núcleos."
                    )
                    tts?.speak(saludos.random(), TextToSpeech.QUEUE_FLUSH, null, "ttsTetrisId")
                }
            }
        }
        val tvConsola = findViewById<TextView>(R.id.tvConsolaM8AX)
        tvConsola.setOnClickListener {
            if (!calculando) {
                val textoACopiar = tvConsola.text.toString()
                if (textoACopiar.isNotEmpty()) {
                    val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("M8AX_DATA", textoACopiar)
                    clipboard.setPrimaryClip(clip)
                    Toast.makeText(
                        this, "M8AX - Contenido Copiado Al Portapapeles", Toast.LENGTH_SHORT
                    ).show()
                    if (ttsEnabled) {
                        tts?.speak(
                            "Contenido Copiado Al Portapapeles.",
                            TextToSpeech.QUEUE_FLUSH,
                            null,
                            "ttsId"
                        )
                    }
                }
            } else {
                if (ttsEnabled) {
                    tts?.speak(
                        "Espera Que Termine El Cálculo; Para Copiar Al Portapapeles.",
                        TextToSpeech.QUEUE_FLUSH,
                        null,
                        "ttsId"
                    )
                }
                Toast.makeText(
                    this, "M8AX - Espera A Que Termine El Cálculo Para Copiar", Toast.LENGTH_SHORT
                ).show()
            }
        }
        val scaleDetector = android.view.ScaleGestureDetector(
            this, object : android.view.ScaleGestureDetector.SimpleOnScaleGestureListener() {
                override fun onScale(detector: android.view.ScaleGestureDetector): Boolean {
                    currentTextSize *= detector.scaleFactor
                    currentTextSize = currentTextSize.coerceIn(8f, 60f)
                    tvConsola.textSize = currentTextSize
                    return true
                }
            })
        findViewById<ScrollView>(R.id.scrollConsola).setOnTouchListener { _, event ->
            scaleDetector.onTouchEvent(event)
            false
        }
        val activityManager = getSystemService(ACTIVITY_SERVICE) as android.app.ActivityManager
        val memoryInfo = android.app.ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)
        ramTotalGB = memoryInfo.totalMem.toDouble() / (1024.0 * 1024.0 * 1024.0)
        findViewById<TextView>(R.id.txtStatus).text =
            "M8AX - RAM Libre - ${String.format("%.2f", ramTotalGB)}GB"
        val fichero = File(getExternalFilesDir(null), "M8AX_Pi.txt")
        if (fichero.exists()) fichero.delete()
        val tvNota = findViewById<TextView>(R.id.tvNotaRam)
        tvNota.movementMethod = android.text.method.LinkMovementMethod.getInstance()
        tvNotaRam = tvNota
        gestionarBotonera(true)
    }

    private fun GuardarEstadoTts(estado: Boolean) {
        val prefs = getSharedPreferences("M8AX-ConfigTTS", Context.MODE_PRIVATE)
        val editor = prefs.edit()
        editor.putBoolean("TtsActivado", estado)
        editor.apply()
    }

    private fun CargarEstadoTts(): Boolean {
        val prefs = getSharedPreferences("M8AX-ConfigTTS", Context.MODE_PRIVATE)
        return prefs.getBoolean("TtsActivado", true)
    }

    private fun startParpadeo(textView: TextView, boton: Button?) {
        colorAnim = ObjectAnimator.ofInt(
            textView, "textColor", Color.parseColor("#FF9800"), Color.parseColor("#FFFFFF")
        ).apply {
            duration = 500
            setEvaluator(ArgbEvaluator())
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.REVERSE
            start()
        }
        boton?.let {
            val animBoton = ObjectAnimator.ofInt(
                it, "textColor", Color.parseColor("#FF9800"), Color.parseColor("#FFFFFF")
            ).apply {
                duration = 500
                setEvaluator(ArgbEvaluator())
                repeatCount = ValueAnimator.INFINITE
                repeatMode = ValueAnimator.REVERSE
                start()
            }
            it.tag = animBoton
        }
    }

    private fun stopParpadeo(textView: TextView) {
        colorAnim?.cancel()
        textView.setTextColor(Color.parseColor("#FF9800"))
        botonesPiIds.forEach { id ->
            val btn = findViewById<Button>(id)
            (btn?.tag as? ObjectAnimator)?.cancel()
            btn?.tag = null
            btn?.setTextColor(if (id == R.id.btn10M) Color.BLACK else Color.WHITE)
        }
    }

    private fun detenerCalculoM8AX() {
        if (!calculando) {
            if (ttsEnabled) tts?.speak(
                "No Hay Nada Que Detener. No Hay Cálculo De Pi En Curso.",
                TextToSpeech.QUEUE_FLUSH,
                null,
                "ttsStopId"
            )
            Toast.makeText(this, "M8AX - No Hay Ningún Cálculo Activo", Toast.LENGTH_SHORT).show()
            return
        }
        val builder = android.app.AlertDialog.Builder(this)
        builder.setTitle("M8AX - DETENER MOTOR - M8AX")
        builder.setMessage("Para Detener Los Cálculos Es Necesario Reiniciar El Motor. ¿ Estás Seguro ?")
        builder.setPositiveButton("SÍ, DETENER") { _, _ ->
            if (ttsEnabled) {
                tts?.speak("Okey.", TextToSpeech.QUEUE_FLUSH, null, "ttsStopId")
                Thread.sleep(600)
            }
            val intent = Intent(this, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(intent)
            android.os.Process.killProcess(android.os.Process.myPid())
            System.exit(0)
        }
        builder.setNegativeButton("NO, CONTINUAR") { _, _ ->
            if (ttsEnabled) {
                tts?.speak("Vale.", TextToSpeech.QUEUE_FLUSH, null, "ttsStopId")
            }
        }
        builder.create().show()
    }

    private fun gestionarBotonera(activar: Boolean) {
        runOnUiThread {
            val valoresPi = mapOf(
                R.id.btn1M to 1L,
                R.id.btn2M to 2L,
                R.id.btn3M to 3L,
                R.id.btn4M to 4L,
                R.id.btn5M to 5L,
                R.id.btn6M to 6L,
                R.id.btn7M to 7L,
                R.id.btn8M to 8L,
                R.id.btn9M to 9L,
                R.id.btn10M to 10L,
                R.id.btn20M to 20L,
                R.id.btn40M to 40L,
                R.id.btn60M to 60L,
                R.id.btn80M to 80L,
                R.id.btn100M to 100L,
                R.id.btn200M to 200L,
                R.id.btn300M to 300L,
                R.id.btn400M to 400L,
                R.id.btn500M to 500L,
                R.id.btn600M to 600L,
                R.id.btn700M to 700L,
                R.id.btn800M to 800L,
                R.id.btn900M to 900L,
                R.id.btn1000M to 1000L,
                R.id.btn1200M to 1200L,
                R.id.btn1400M to 1400L,
                R.id.btn1600M to 1600L,
                R.id.btn1800M to 1800L,
                R.id.btn2000M to 2000L,
            )
            val colorAzul = android.content.res.ColorStateList.valueOf(Color.parseColor("#2196F3"))
            val colorOscuro =
                android.content.res.ColorStateList.valueOf(Color.parseColor("#121212"))
            val colorGrisCalculando =
                android.content.res.ColorStateList.valueOf(Color.parseColor("#333333"))
            botonesPiIds.forEach { id ->
                val btn = findViewById<Button>(id) ?: return@forEach
                btn.isEnabled = true
                btn.alpha = 1.0f
                if (!activar) {
                    if (id != R.id.btnCompartir) {
                        btn.backgroundTintList = colorGrisCalculando
                        btn.setTextColor(Color.GRAY)
                        btn.setOnClickListener {
                            if (ttsEnabled) {
                                tts?.speak(
                                    "Espera A Que Termine El Cálculo Actual.",
                                    TextToSpeech.QUEUE_FLUSH,
                                    null,
                                    "ttsStopId"
                                )
                                Thread.sleep(600)
                            }
                            Toast.makeText(
                                this,
                                "M8AX - Espera A Que Termine El Cálculo Actual",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else {
                        btn.backgroundTintList =
                            android.content.res.ColorStateList.valueOf(Color.parseColor("#4CAF50"))
                        btn.setOnClickListener { compartirArchivo() }
                    }
                } else {
                    if (id == R.id.btnCompartir) {
                        btn.backgroundTintList =
                            android.content.res.ColorStateList.valueOf(Color.parseColor("#4CAF50"))
                        btn.setTextColor(Color.WHITE)
                        btn.setOnClickListener { compartirArchivo() }
                    } else {
                        val millones = valoresPi[id] ?: 0L
                        var bloqueadoPorRam = false
                        var ramNecesaria = 0.0
                        when {
                            millones >= 2000 -> {
                                ramNecesaria = 28.0; if (ramTotalGB < 28.0) bloqueadoPorRam = true
                            }

                            millones >= 1800 -> {
                                ramNecesaria = 25.5; if (ramTotalGB < 25.5) bloqueadoPorRam = true
                            }

                            millones >= 1600 -> {
                                ramNecesaria = 23.0; if (ramTotalGB < 23.0) bloqueadoPorRam = true
                            }

                            millones >= 1400 -> {
                                ramNecesaria = 20.5; if (ramTotalGB < 20.5) bloqueadoPorRam = true
                            }

                            millones >= 1200 -> {
                                ramNecesaria = 18.0; if (ramTotalGB < 18.0) bloqueadoPorRam = true
                            }

                            millones >= 1000 -> {
                                ramNecesaria = 15.5; if (ramTotalGB < 15.5) bloqueadoPorRam = true
                            }

                            millones >= 900 -> {
                                ramNecesaria = 14.0; if (ramTotalGB < 14.0) bloqueadoPorRam = true
                            }

                            millones >= 800 -> {
                                ramNecesaria = 12.5; if (ramTotalGB < 12.5) bloqueadoPorRam = true
                            }

                            millones >= 700 -> {
                                ramNecesaria = 10.8; if (ramTotalGB < 10.8) bloqueadoPorRam = true
                            }

                            millones >= 600 -> {
                                ramNecesaria = 8.5; if (ramTotalGB < 8.5) bloqueadoPorRam = true
                            }

                            millones >= 500 -> {
                                ramNecesaria = 7.0; if (ramTotalGB < 7.0) bloqueadoPorRam = true
                            }

                            millones >= 400 -> {
                                ramNecesaria = 5.4; if (ramTotalGB < 5.4) bloqueadoPorRam = true
                            }

                            millones >= 300 -> {
                                ramNecesaria = 4.2; if (ramTotalGB < 4.2) bloqueadoPorRam = true
                            }

                            millones >= 200 -> {
                                ramNecesaria = 2.8; if (ramTotalGB < 2.8) bloqueadoPorRam = true
                            }

                            millones >= 100 -> {
                                ramNecesaria = 1.4; if (ramTotalGB < 1.4) bloqueadoPorRam = true
                            }

                            millones >= 80 -> {
                                ramNecesaria = 1.1; if (ramTotalGB < 1.1) bloqueadoPorRam = true
                            }

                            millones >= 60 -> {
                                ramNecesaria = 0.8; if (ramTotalGB < 0.8) bloqueadoPorRam = true
                            }

                            millones >= 40 -> {
                                ramNecesaria = 0.6; if (ramTotalGB < 0.6) bloqueadoPorRam = true
                            }

                            millones >= 20 -> {
                                ramNecesaria = 0.3; if (ramTotalGB < 0.3) bloqueadoPorRam = true
                            }

                            millones >= 10 -> {
                                ramNecesaria = 0.2; if (ramTotalGB < 0.2) bloqueadoPorRam = true
                            }

                            millones >= 6 -> {
                                ramNecesaria = 0.15; if (ramTotalGB < 0.15) bloqueadoPorRam = true
                            }

                            millones >= 1 -> {
                                ramNecesaria = 0.05; if (ramTotalGB < 0.05) bloqueadoPorRam = true
                            }
                        }
                        if (bloqueadoPorRam) {
                            btn.backgroundTintList = colorOscuro
                            btn.setTextColor(Color.DKGRAY)
                            btn.setOnClickListener {
                                if (ttsEnabled) {
                                    tts?.speak(
                                        "RAM Insuficiente, Necesitas ${ramNecesaria}GB Libres, De Memoria RAM.",
                                        TextToSpeech.QUEUE_FLUSH,
                                        null,
                                        "ttsStopId"
                                    )
                                }
                                Toast.makeText(
                                    this,
                                    "M8AX - RAM Insuficiente ( Necesitas ${ramNecesaria}GB ) Libres",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        } else {
                            if (id == R.id.btn10M) {
                                btn.backgroundTintList =
                                    android.content.res.ColorStateList.valueOf(Color.parseColor("#FF9800"))
                                btn.setTextColor(Color.BLACK)
                            } else {
                                btn.backgroundTintList = colorAzul
                                btn.setTextColor(Color.WHITE)
                            }
                            btn.setOnClickListener {
                                ultimosMillonesPulsados = "${millones}M"
                                lanzarMotorM8AX(millones * 1000000L)
                            }
                        }
                    }
                }
            }
        }
    }

    fun intToRoman(num: Int): String {
        if (num < 0) return ""
        if (num == 0) return "N"
        val valores = intArrayOf(
            1_000_000,
            900_000,
            500_000,
            400_000,
            100_000,
            90_000,
            50_000,
            40_000,
            10_000,
            9_000,
            5_000,
            4_000,
            1000,
            900,
            500,
            400,
            100,
            90,
            50,
            40,
            10,
            9,
            5,
            4,
            1
        )
        val cadenas = arrayOf(
            "M",
            "CM",
            "D",
            "CD",
            "C",
            "XC",
            "L",
            "XL",
            "X",
            "IX",
            "V",
            "IV",
            "M",
            "CM",
            "D",
            "CD",
            "C",
            "XC",
            "L",
            "XL",
            "X",
            "IX",
            "V",
            "IV",
            "I"
        )
        var resultado = StringBuilder()
        var decimal = num
        while (decimal > 0) {
            for (i in valores.indices) {
                if (decimal >= valores[i]) {
                    if (valores[i] > 1000) cadenas[i].forEach { c ->
                        resultado.append(c).append('\u0305')
                    } else resultado.append(cadenas[i])
                    decimal -= valores[i]
                    break
                }
            }
        }
        return resultado.toString()
    }

    private fun showAboutDialog() {
        val calendar = Calendar.getInstance()
        val mes = calendar.get(Calendar.MONTH)
        val dia = calendar.get(Calendar.DAY_OF_MONTH)
        val isNavidad =
            (mes == Calendar.DECEMBER && dia >= 20) || (mes == Calendar.JANUARY && dia <= 6)
        val soundRes = if (isNavidad) {
            listOf(R.raw.m8axdialogo3, R.raw.m8axdialogo4).random()
        } else {
            listOf(R.raw.m8axdialogo1, R.raw.m8axdialogo2, R.raw.m8axdialogo6).random()
        }
        val aboutPlayer: MediaPlayer = MediaPlayer.create(this, soundRes)
        aboutPlayer.start()
        aboutPlayer.setOnCompletionListener(object : MediaPlayer.OnCompletionListener {
            override fun onCompletion(mp: MediaPlayer) {
                mp.release()
            }
        })
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        val formatoCompilacion = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
        val fechaCompilacion = LocalDateTime.parse("25/02/2026 16:45", formatoCompilacion)
        val ahora = LocalDateTime.now()
        val (años, dias, horas, minutos, segundos) = if (ahora.isBefore(fechaCompilacion)) {
            listOf(0L, 0L, 0L, 0L, 0L)
        } else {
            val a = ChronoUnit.YEARS.between(fechaCompilacion, ahora)
            val fechaMasAnios = fechaCompilacion.plusYears(a)
            val d = ChronoUnit.DAYS.between(fechaMasAnios, ahora)
            val h = ChronoUnit.HOURS.between(fechaMasAnios.plusDays(d), ahora)
            val m = ChronoUnit.MINUTES.between(fechaMasAnios.plusDays(d).plusHours(h), ahora)
            val s = ChronoUnit.SECONDS.between(
                fechaMasAnios.plusDays(d).plusHours(h).plusMinutes(m), ahora
            )
            listOf(a, d, h, m, s)
        }
        val tiempoTranscurrido =
            "... Fecha De Compilación - 25/02/2026 16:45 ...\n\n... Tmp. Desde Compilación - ${años}a${dias}d${horas}h${minutos}m${segundos}s ..."
        val textoIzquierda = SpannableString(
            "App Creada Por MarcoS OchoA DieZ - ( M8AX )\n\n" + "Mail - mviiiax.m8ax@gmail.com\n\n" + "Youtube - https://youtube.com/m8ax\n\n" + "El Futuro No Está Establecido, No Hay Destino, Solo Existe El Que Nosotros Hacemos...\n\n\n" + "... Creado En 27h De Programación ...\n\n" + "... Con +/- 4500 Líneas De Código ...\n\n" + "... +/- 250 KB En Texto Plano | TXT | ...\n\n" + "... +/- Novela Cándido De Voltaire En Código ...\n\n" + tiempoTranscurrido + "\n\n"
        )
        val textoCentro = SpannableString(
            "| AND | OR | NOT | Ax = b | 0 - 1 |\n\n" + "M8AX CORP. $currentYear - ${
                intToRoman(
                    currentYear
                )
            }\n\n"
        )
        textoIzquierda.setSpan(
            android.text.style.StyleSpan(android.graphics.Typeface.BOLD),
            0,
            textoIzquierda.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        textoCentro.setSpan(
            android.text.style.StyleSpan(android.graphics.Typeface.BOLD),
            0,
            textoCentro.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        val emailStart = textoIzquierda.indexOf("mviiiax.m8ax@gmail.com")
        val emailEnd = emailStart + "mviiiax.m8ax@gmail.com".length
        textoIzquierda.setSpan(object : ClickableSpan() {
            override fun onClick(widget: View) {
                val intent = Intent(Intent.ACTION_SENDTO).apply {
                    data = Uri.parse("mailto:mviiiax.m8ax@gmail.com")
                }
                startActivity(intent)
            }

            override fun updateDrawState(ds: android.text.TextPaint) {
                ds.color = android.graphics.Color.parseColor("#FF0000")
                ds.isUnderlineText = true
            }
        }, emailStart, emailEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        val ytStart = textoIzquierda.indexOf("https://youtube.com/m8ax")
        val ytEnd = ytStart + "https://youtube.com/m8ax".length
        textoIzquierda.setSpan(object : ClickableSpan() {
            override fun onClick(widget: View) {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://youtube.com/m8ax"))
                startActivity(intent)
            }

            override fun updateDrawState(ds: android.text.TextPaint) {
                ds.color = android.graphics.Color.parseColor("#FF0000")
                ds.isUnderlineText = true
            }
        }, ytStart, ytEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        val mainLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(50, 40, 50, 40)
            gravity = Gravity.START
        }
        val tvLeft = TextView(this).apply {
            text = textoIzquierda
            movementMethod = LinkMovementMethod.getInstance()
            textAlignment = View.TEXT_ALIGNMENT_TEXT_START
        }
        val tvCenter = TextView(this).apply {
            text = textoCentro
            textAlignment = View.TEXT_ALIGNMENT_CENTER
        }
        val frame = FrameLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 400
            )
        }
        val logosM = arrayOf(
            R.drawable.logom8ax,
            R.drawable.logom8ax3,
            R.drawable.logom8ax4,
            R.drawable.logom8ax5,
            R.drawable.logom8ax6,
            R.drawable.logom8ax7,
            R.drawable.logom8ax8,
            R.drawable.logom8ax9,
            R.drawable.logom8ax10,
            R.drawable.logom8ax11,
            R.drawable.logom8ax12,
            R.drawable.logom8ax13,
            R.drawable.m8axlogoapp,
        )
        val logo = ImageView(this).apply {
            setImageResource(logosM.random())
            adjustViewBounds = true
            scaleType = ImageView.ScaleType.FIT_CENTER
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT
            )
        }
        var touchCount = 0
        val mensajes = listOf(
            "Modo Dios Activado. No Rompas Nada.",
            "No Deberías Estar Haciendo Esto... Pero Bienvenido Al Club.",
            "Has Desbloqueado El Snack Secreto De M 8 A X",
            "01001000 01101111 01101100 01100001... Eso Significa Hola, Por Si No Lo Pillas.",
            "El Futuro, No Está Establecido, No Hay Destino; Solo Existe El Que Nosotros Hacemos...",
            "Que La Fuerza Te Acompañe.",
            "M 8 A X Es El Mejor Programador Del Mundo.",
            "Nivel Oculto Activado. Prepárate Para Compilar Sin Piedad.",
            "Error 404: Huevo De Pascua No Encontrado... Oh Espera, Aquí Está...",
            "Jijijiji! Por Fin Alguien, Descubre Mi Tesoro Oculto...",
            "Cuidado Con Los Loops Infinitos, Pero Hoy Es Tu Día De Suerte.",
            "Has Encontrado La Ruta Secreta De Compilación.",
            "¡Atención! Se Ha Activado El Protocolo Oculto.",
            "Cada Click Cuenta, Pero Hoy Has Ganado.",
            "Los Bits No Mienten, Pero A Veces Se Divierten.",
            "Nivel Épico Alcanzado. Tu Código Brilla.",
            "Error 1337: Programador Legendario Detectado.",
            "Si Puedes Leer Esto, Eres Parte Del Club Secreto.",
            "Compila Sin Miedo, Que El Universo Te Respeta.",
            "Has Desatado La Magia Del Debug."
        )
        logo.setOnClickListener {
            touchCount++
            if (touchCount >= 10) {
                touchCount = 0
                contadorespe = 1
                val player = MediaPlayer.create(this@MainActivity, R.raw.m8axdialogo5)
                player.start()
                player.setOnCompletionListener { mp -> mp.release() }
                val mensaje = mensajes.random()
                tts?.speak(
                    "Huevito De Pascua. " + mensaje, TextToSpeech.QUEUE_FLUSH, null, "ttsStopId"
                )
                AlertDialog.Builder(this@MainActivity).setTitle("¡ Huevito De Pascua !")
                    .setMessage("$mensaje\n\nhttps://youtube.com/m8ax")
                    .setPositiveButton("OK", null).show()
            }
        }
        frame.addView(logo)
        mainLayout.addView(tvLeft)
        mainLayout.addView(tvCenter)
        mainLayout.addView(frame)
        val tvVer = TextView(this).apply {
            textAlignment = View.TEXT_ALIGNMENT_CENTER
            gravity = Gravity.CENTER
            textSize = 15f
            setTypeface(null, Typeface.BOLD)
        }
        val githubUrl = "https://github.com/m8ax/M8AX_MegaPi"
        val spannableText = SpannableString("\nÚltima Release - v10.03.1977")
        spannableText.setSpan(object : android.text.style.ClickableSpan() {
            override fun onClick(widget: View) {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(githubUrl))
                widget.context.startActivity(intent)
            }

            override fun updateDrawState(ds: android.text.TextPaint) {
                val red = (100..255).random()
                val green = (100..255).random()
                val blue = (100..255).random()
                ds.color = android.graphics.Color.rgb(red, green, blue)
                ds.isUnderlineText = true
            }
        }, 0, spannableText.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        tvVer.text = spannableText
        tvVer.movementMethod = LinkMovementMethod.getInstance()
        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            gravity = Gravity.CENTER
            topMargin = -55
        }
        tvVer.layoutParams = params
        mainLayout.addView(tvVer)
        val handler = Handler(mainLooper)
        val random = Random()
        fun addStar() {
            val star = ImageView(this).apply {
                setImageResource(android.R.drawable.star_big_on)
                alpha = 0.6f + random.nextFloat() * 0.4f
                val size = 10 + random.nextInt(15)
                layoutParams = FrameLayout.LayoutParams(size, size)
                x = random.nextFloat() * frame.width
                y = -size.toFloat()
            }
            frame.addView(star)
            val distance = frame.height + 20
            val duration = 3000L + random.nextInt(2000)
            star.animate().translationYBy(distance.toFloat()).alpha(0f).setDuration(duration)
                .withEndAction { frame.removeView(star) }.start()
        }

        val snowRunnable = object : Runnable {
            override fun run() {
                addStar()
                handler.postDelayed(this, 200)
            }
        }
        handler.post(snowRunnable)
        val handlerr = Handler(Looper.getMainLooper())
        val dialog = AlertDialog.Builder(this).setTitle("Acerca De M8AX - Mega Pi v10.03.77")
            .setView(mainLayout).setPositiveButton("Aceptar") { _, _ ->
                handlerr.removeCallbacks(snowRunnable)
            }.create()
        dialog.setOnDismissListener {
            contadorespe = 0
            handlerr.removeCallbacks(snowRunnable)
        }
        dialog.show()
        vibrarImpacto()
    }

    private fun lanzarMotorM8AX(decimales: Long) {
        val tvConsola = findViewById<TextView>(R.id.tvConsolaM8AX)
        val scroll = findViewById<ScrollView>(R.id.scrollConsola)
        val txtStatus = findViewById<TextView>(R.id.txtStatus)
        tvConsola.text = ""
        val logCompleto = StringBuilder()
        logCompleto.setLength(0)
        var lineaIndiceFinal = ""
        txtStatus.text = "M8AX - Motor En Marcha..."
        val botonActual = botonesPiIds.mapNotNull { findViewById<Button>(it) }
            .find { it.text.toString() == ultimosMillonesPulsados }
        startParpadeo(tvNotaRam, botonActual)
        vibrarImpacto()
        if (ttsEnabled && !modoLoopActivo) {
            tts?.speak(
                "Calculando ${ultimosMillonesPulsados}illones De Decimales De Pi.",
                TextToSpeech.QUEUE_FLUSH,
                null,
                "ttsStopId"
            )
        }
        gestionarBotonera(false)
        calculando = true
        window.addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        Thread {
            try {
                Runtime.getRuntime().exec(arrayOf("sh", "-c", "pkill -9 logcat")).waitFor()
                Thread.sleep(300)
                Runtime.getRuntime().exec(arrayOf("sh", "-c", "logcat -c")).waitFor()
                Thread.sleep(200)
                val procesoLog = Runtime.getRuntime().exec("logcat -v raw M8AX_MOTOR:I *:S")
                val reader = procesoLog.inputStream.bufferedReader()
                Thread {
                    try {
                        Thread.sleep(400)
                        startCalculation(decimales)
                        Thread.sleep(2000)
                        runOnUiThread {
                            if (!calculando && !modoLoopActivo) return@runOnUiThread
                            val activityManager =
                                getSystemService(ACTIVITY_SERVICE) as android.app.ActivityManager
                            val memoryInfo = android.app.ActivityManager.MemoryInfo()
                            activityManager.getMemoryInfo(memoryInfo)
                            ramTotalGB = memoryInfo.totalMem.toDouble() / (1024.0 * 1024.0 * 1024.0)
                            txtStatus.text = "M8AX - Cálculo Finalizado · RAM Libre - ${
                                String.format(
                                    "%.2f", ramTotalGB
                                )
                            }GB"
                            calculando = false
                            stopParpadeo(tvNotaRam)
                            val textoConsola = tvConsola.text.toString()
                            val lineaChi = textoConsola.lines()
                                .findLast { it.contains("Test De Aleatoriedad Chi-Square:") }
                            val numeroChi =
                                lineaChi?.substringAfter("Chi-Square: ")?.substringBefore(" ]")
                                    ?.trim() ?: "0.0000"
                            val chiDouble = numeroChi.toDoubleOrNull() ?: 0.0
                            val saludEstado = if (chiDouble < 16.92) "Buena." else "Baja."
                            val soloNumero = lineaIndiceFinal.replace("M8AX", "")
                                .filter { it.isDigit() || it == '.' }.trim().replace(".", ",")
                            if (grabarEnNotasEnabled) {
                                try {
                                    val fechaNota = java.text.SimpleDateFormat(
                                        "dd/MM/yyyy - HH:mm:ss", java.util.Locale.getDefault()
                                    ).format(java.util.Date())
                                    val prefsNotas = getSharedPreferences(
                                        "M8AX-Notas_Importantes", Context.MODE_PRIVATE
                                    )
                                    val notasActuales =
                                        prefsNotas.getString("Lista_Notas_JsoN", "[]")
                                    val jsonArray = org.json.JSONArray(notasActuales)
                                    val nuevaNotaTexto =
                                        "[ $fechaNota ]\nPi - $ultimosMillonesPulsados\nChi-Square - ${
                                            numeroChi.replace(
                                                '.', ','
                                            )
                                        }\nSalud CPU / RAM- ${saludEstado}\nÍndice De Velocidad - ${
                                            soloNumero.replace(
                                                '.', ','
                                            )
                                        }"
                                    val nuevaNotaObj = org.json.JSONObject()
                                    nuevaNotaObj.put("Nombre", nuevaNotaTexto)
                                    nuevaNotaObj.put("Comprado", false)
                                    jsonArray.put(nuevaNotaObj)
                                    prefsNotas.edit()
                                        .putString("Lista_Notas_JsoN", jsonArray.toString()).apply()
                                    if (!modoLoopActivo) {
                                        Toast.makeText(
                                            this@MainActivity,
                                            "Resultado Guardado En Notas",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                            if (modoLoopActivo) {
                                contadorLoop++
                                val mensajeLoop = "Iniciando Test, Número $contadorLoop."
                                if (ttsEnabled) {
                                    tts?.speak(
                                        mensajeLoop, TextToSpeech.QUEUE_FLUSH, null, "ttsStopId"
                                    )
                                }
                                Toast.makeText(
                                    this@MainActivity, "M8AX - $mensajeLoop", Toast.LENGTH_SHORT
                                ).show()
                                Handler(Looper.getMainLooper()).postDelayed({
                                    if (modoLoopActivo) lanzarMotorM8AX(decimales)
                                }, 500)
                            } else {
                                gestionarBotonera(true)
                                window.clearFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                                contadorLoop = 1
                                ToneGenerator(AudioManager.STREAM_ALARM, 100).startTone(
                                    ToneGenerator.TONE_PROP_ACK, 150
                                )
                                vibrarImpacto()
                                val textoConsola = tvConsola.text.toString()
                                val lineaChi = textoConsola.lines()
                                    .findLast { it.contains("Test De Aleatoriedad Chi-Square:") }
                                val numeroChi =
                                    lineaChi?.substringAfter("Chi-Square: ")?.substringBefore(" ]")
                                        ?.trim() ?: "0.0000"
                                val chiDouble = numeroChi.toDoubleOrNull() ?: 0.0
                                val saludEstado = if (chiDouble < 16.92) "Buena." else "Baja."
                                val fraseSinMotor = lineaIndiceFinal.replace("M8AX", "")
                                val soloNumero =
                                    fraseSinMotor.filter { it.isDigit() || it == '.' }.trim()
                                        .replace(".", ",")
                                val mensajeVoz =
                                    "Cálculo Terminado. Índice De Velocidad Para ${ultimosMillonesPulsados}illones De Decimales De Pi En Tu Móvil: $soloNumero. Prueba De Bondad De Ajuste De Chi Cuadrado De Pearson; $numeroChi. Salud De C P U Y Ram, $saludEstado Grácias Por Usar M 8 A X; Mega Pi."
                                val mensajePantalla =
                                    "M8AX - Cálculo: [ $ultimosMillonesPulsados ]\n\nChi-Square: $numeroChi\n\nSalud De ( CPU / RAM ) - $saludEstado\n\n$lineaIndiceFinal\n\n--- Referencias [ 10M ] De Algunos SOC ---\n\nQualcomm Snapdragon 400 - 1.285 Puntos.\n\nQualcomm Snapdragon 710 - 5.506 Puntos."
                                if (ttsEnabled) {
                                    tts?.stop()
                                    Thread.sleep(200)
                                    tts?.speak(
                                        mensajeVoz, TextToSpeech.QUEUE_FLUSH, null, "ttsResultId"
                                    )
                                }
                                val alerta = android.app.AlertDialog.Builder(this@MainActivity)
                                    .setTitle("--- M8AX MEGA PI BENCHMARK ---")
                                    .setMessage(mensajePantalla).setPositiveButton("ACEPTAR", null)
                                    .show()
                                val textView = alerta.findViewById<TextView>(android.R.id.message)
                                textView?.textSize = 15f
                            }
                        }
                    } catch (e: Exception) {
                        calculando = false
                    }
                }.start()
                var ultimaRam = 0L
                while (calculando) {
                    val linea = try {
                        reader.readLine()
                    } catch (e: Exception) {
                        null
                    } ?: break
                    val ahora = System.currentTimeMillis()
                    if (ahora - ultimaRam > 1000) {
                        ultimaRam = ahora
                        val activityManager =
                            getSystemService(ACTIVITY_SERVICE) as android.app.ActivityManager
                        val memoryInfo = android.app.ActivityManager.MemoryInfo()
                        activityManager.getMemoryInfo(memoryInfo)
                        val ramDisponible =
                            memoryInfo.availMem.toDouble() / (1024.0 * 1024.0 * 1024.0)
                        runOnUiThread {
                            txtStatus.text = "M8AX - Motor En Marcha... RAM Libre - ${
                                String.format(
                                    "%.2f", ramDisponible
                                )
                            }GB"
                        }
                    }
                    if (!linea.contains("beginning of") && !linea.startsWith("---------")) {
                        if (linea.contains("Índice De Velocidad De Tu Móvil")) {
                            lineaIndiceFinal = linea
                        }
                        logCompleto.append(linea).append("\n")
                        runOnUiThread {
                            tvConsola.text = logCompleto.toString()
                            scroll.post { scroll.fullScroll(android.view.View.FOCUS_DOWN) }
                        }
                    }
                }
                try {
                    reader.close()
                } catch (e: Exception) {
                }
                try {
                    procesoLog.destroy()
                } catch (e: Exception) {
                }
                try {
                    Runtime.getRuntime().exec(arrayOf("sh", "-c", "pkill -9 logcat"))
                } catch (e: Exception) {
                }
            } catch (e: Exception) {
                runOnUiThread { gestionarBotonera(true) }
            }
        }.start()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        val itemTts = menu?.findItem(R.id.M8AX_Menu_TTS)
        itemTts?.isChecked = ttsEnabled
        itemTts?.title = if (ttsEnabled) "● Audio TTS Activado" else "● Audio TTS Desactivado"
        val itemNotas = menu?.findItem(R.id.action_guardar_notas_toggle)
        itemNotas?.isChecked = grabarEnNotasEnabled
        itemNotas?.title =
            if (grabarEnNotasEnabled) "● Grabar Auto-Notas - ON" else "● Grabar Auto-Notas - OFF"
        return true
    }

    private fun mostrarDialogoAyuda() {
        val dialog = android.app.Dialog(this)
        dialog.requestWindowFeature(android.view.Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.window?.setBackgroundDrawableResource(android.R.color.black)
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 40, 40, 40)
            setBackgroundColor(Color.BLACK)
            gravity = Gravity.CENTER
        }
        val scroll = ScrollView(this)
        val tvTitle = TextView(this).apply {
            text = "M8AX - MANUAL TÉCNICO - M8AX"
            setTextColor(Color.parseColor("#FF9800"))
            textSize = 16f
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, 20)
        }
        val tvContent = TextView(this).apply {
            text =
                "M8AX MegaPi: Suite De Diagnóstico Extremo.\n\n" + "• Algoritmo Principal: Implementación Chudnovsky (C Nativo).\n" + "• Multiplicación: Basada En FFT (Transformada Rápida De Fourier).\n" + "• Inversión/División: Método De Newton.\n" + "• Optimización: División Binaria (Binary Splitting).\n\n" + "• Cálculo: Decimales De Pi Con Precisión Arbitraria.\n\n" + "• Propósito 1: Test De Estrés Térmico Para CPU Y RAM.\n" + "• Propósito 2: Test De Estabilidad De CPU Y RAM.\n" + "• Propósito 3: Test De Velocidad De Cálculo De Tu Móvil.\n\n" + "• Nota 1: Puedes Seleccionar Cualquier Test 2M 10M 60M, Etc...\n\n" + "• Nota 2: Activa El Modo Loop Para Repetir Continuamente El Test Que Selecciones, Ideal Para Detectar Inestabilidades De Voltaje, Medir El Impacto En La Salud De La Batería Bajo Carga Extrema Y Analizar El 'Thermal Throttling' Del Procesador En Sesiones De Estrés Prolongadas.\n\n" + "• 10M Es El Más Normalizado Para Comparar Rendimiento Entre Dispositivos.\n\n" + "• Librerías: Gestión Mediante GMP ( GNU Multi-Precision Library ).\n\n" + "Este Benchmark Lleva El Hardware Al Límite Operativo. En La Consola, Tienes Todo Tipo De Datos Estadísticos."
            setTextColor(Color.WHITE)
            textSize = 16f
            gravity = Gravity.START
        }
        val buttonLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setPadding(0, 20, 0, 0)
        }
        val btnSpeak = Button(this).apply {
            text = "LEER POR VOZ"
            setBackgroundColor(Color.parseColor("#4CAF50"))
            setTextColor(Color.WHITE)
            setOnClickListener {
                if (ttsEnabled) {
                    tts?.stop()
                    tts?.speak(tvContent.text.toString(), TextToSpeech.QUEUE_FLUSH, null, null)
                }
            }
        }
        val btnClose = Button(this).apply {
            text = "ENTENDIDO"
            setBackgroundColor(Color.parseColor("#2196F3"))
            setTextColor(Color.WHITE)
            setOnClickListener {
                if (ttsEnabled) {
                    tts?.stop()
                    tts?.speak("Okey.", TextToSpeech.QUEUE_FLUSH, null, null)
                }
                dialog.dismiss()
            }
        }
        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            setMargins(10, 0, 10, 0)
        }
        layout.addView(tvTitle)
        layout.addView(tvContent)
        buttonLayout.addView(btnSpeak, params)
        buttonLayout.addView(btnClose, params)
        layout.addView(buttonLayout)
        scroll.addView(layout)
        dialog.setContentView(scroll)
        dialog.window?.setLayout(
            android.view.ViewGroup.LayoutParams.MATCH_PARENT,
            android.view.ViewGroup.LayoutParams.MATCH_PARENT
        )
        dialog.setOnDismissListener {
            if (ttsEnabled) {
                tts?.stop()
            }
        }
        dialog.show()
    }

    private fun vibrarImpacto() {
        val vibrator = getSystemService(VIBRATOR_SERVICE) as android.os.Vibrator
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            vibrator.vibrate(
                android.os.VibrationEffect.createOneShot(
                    25, android.os.VibrationEffect.DEFAULT_AMPLITUDE
                )
            )
        } else {
            @Suppress("DEPRECATION") vibrator.vibrate(25)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_borrar) {
            val fichero = File(getExternalFilesDir(null), "M8AX_Pi.txt")
            if (fichero.exists()) {
                fichero.delete()
                if (ttsEnabled) {
                    tts?.speak("Archivo Eliminado.", TextToSpeech.QUEUE_FLUSH, null, "ttsTetrisId")
                }
                Toast.makeText(this, "M8AX - Archivo M8AX_Pi.txt Eliminado", Toast.LENGTH_SHORT)
                    .show()
            } else {
                if (ttsEnabled) {
                    tts?.speak(
                        "No Hay Archivo Para Borrar.", TextToSpeech.QUEUE_FLUSH, null, "ttsTetrisId"
                    )
                }
                Toast.makeText(this, "M8AX - No Hay Archivo Para Borrar", Toast.LENGTH_SHORT).show()
            }
            return true
        }
        if (item.itemId == R.id.action_loop_toggle) {
            modoLoopActivo = !modoLoopActivo
            item.isChecked = modoLoopActivo
            if (!modoLoopActivo && !calculando) {
                gestionarBotonera(true)
            }
            contadorLoop = 1
            val estado = if (modoLoopActivo) "Activado" else "Desactivado"
            Toast.makeText(
                this, "M8AX - Loop: ${if (modoLoopActivo) "ON" else "OFF"}", Toast.LENGTH_SHORT
            ).show()
            if (ttsEnabled) {
                tts?.speak("Loop $estado.", TextToSpeech.QUEUE_FLUSH, null, "ttsTetrisId")
            }
            return true
        }
        if (item.itemId == R.id.action_ayuda) {
            if (ttsEnabled) {
                tts?.speak("Ayuda Técnica.", TextToSpeech.QUEUE_FLUSH, null, "ttsTetrisId")
            }
            mostrarDialogoAyuda()
            return true
        }
        if (item.itemId == R.id.action_acerca_de) {
            if (ttsEnabled) {
                tts?.speak(
                    "Acerca De M 8 A X Mega Pi.", TextToSpeech.QUEUE_FLUSH, null, "ttsTetrisId"
                )
            }
            showAboutDialog()
            return true
        }
        if (item.itemId == R.id.action_chess) {
            val intent = Intent(this, ChessActivity::class.java)
            if (ttsEnabled) {
                tts?.speak("Juguemos Al Ajedrez.", TextToSpeech.QUEUE_FLUSH, null, "ttsTetrisId")
            }
            startActivity(intent)
            return true
        }
        if (item.itemId == R.id.action_pong) {
            val intent = Intent(this, ThePong::class.java)
            if (ttsEnabled) {
                tts?.speak("Juguemos Al Pong.", TextToSpeech.QUEUE_FLUSH, null, "ttsTetrisId")
            }
            startActivity(intent)
            return true
        }
        if (item.itemId == R.id.action_astros) {
            val intent = Intent(this, AstronomiaActivity::class.java)
            if (ttsEnabled) {
                tts?.speak(
                    "Observatorio Astronómico.", TextToSpeech.QUEUE_FLUSH, null, "ttsTetrisId"
                )
            }
            startActivity(intent)
            return true
        }
        if (item.itemId == R.id.action_notas) {
            val intent = Intent(this, NotasActivity::class.java)
            if (ttsEnabled) {
                tts?.speak(
                    "Añade Tus Notas.", TextToSpeech.QUEUE_FLUSH, null, "ttsTetrisId"
                )
            }
            startActivity(intent)
            return true
        }
        if (item.itemId == R.id.action_tetris) {
            val intent = Intent(this, TetrisActivity::class.java)
            intent.putExtra("ttsEnabled", ttsEnabled)
            if (ttsEnabled) {
                tts?.speak("Juguemos Al Tetris.", TextToSpeech.QUEUE_FLUSH, null, "ttsTetrisId")
            }
            startActivity(intent)
            return true
        }
        if (item.itemId == R.id.action_stop) {
            if (ttsEnabled) {
                tts?.speak(
                    "Detenemos EL Cálculo; Si O No...",
                    TextToSpeech.QUEUE_FLUSH,
                    null,
                    "ttsTetrisId"
                )
            }
            detenerCalculoM8AX()
            return true
        }
        if (item.itemId == R.id.M8AX_Menu_TTS) {
            ttsEnabled = !ttsEnabled
            GuardarEstadoTts(ttsEnabled)
            item.isChecked = ttsEnabled
            if (ttsEnabled) {
                item.title = "● Audio TTS Activado"
                Toast.makeText(this, "M8AX - Audio Activado", Toast.LENGTH_SHORT).show()
                tts?.stop()
                tts?.speak("Activado", TextToSpeech.QUEUE_FLUSH, null, "ttsId")
            } else {
                item.title = "● Audio TTS Desactivado"
                tts?.stop()
                tts?.speak("Desactivado", TextToSpeech.QUEUE_FLUSH, null, "ttsId")
                Handler(Looper.getMainLooper()).postDelayed({ tts?.stop() }, 2000)
                Toast.makeText(this, "M8AX - Audio Desactivado", Toast.LENGTH_SHORT).show()
            }
            return true
        }
        if (item.itemId == R.id.action_salir) {
            if (ttsEnabled) {
                tts?.speak(
                    "Cerrando Sistema; Adiós.", TextToSpeech.QUEUE_FLUSH, null, "ttsTetrisId"
                )
                Thread.sleep(2200)
            }
            calculando = false
            try {
                Runtime.getRuntime().exec(arrayOf("sh", "-c", "pkill -9 logcat"))
                window.clearFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            } catch (e: Exception) {
            }
            finishAffinity()
            return true
        }
        if (item.itemId == R.id.action_guardar_notas_toggle) {
            grabarEnNotasEnabled = !grabarEnNotasEnabled
            getSharedPreferences("M8AX-ConfigNotas", Context.MODE_PRIVATE).edit()
                .putBoolean("GrabarNotas", grabarEnNotasEnabled).apply()
            item.isChecked = grabarEnNotasEnabled
            item.title =
                if (grabarEnNotasEnabled) "● Grabar Auto-Notas - ON" else "● Grabar Auto-Notas - OFF"
            val aviso =
                if (grabarEnNotasEnabled) "Grabación De Auto-Notas; Activado" else "Grabación De Auto-Notas; Desactivado"
            Toast.makeText(this, "M8AX - $aviso", Toast.LENGTH_SHORT).show()
            if (ttsEnabled) tts?.speak(aviso, TextToSpeech.QUEUE_FLUSH, null, null)
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun compartirArchivo() {
        if (calculando) {
            if (ttsEnabled) {
                tts?.speak(
                    "Espera Que El Cálculo Termine.", TextToSpeech.QUEUE_FLUSH, null, "ttsTetrisId"
                )
            }
            Toast.makeText(
                this, "M8AX - Espera A Que Termine El Cálculo Actual", Toast.LENGTH_SHORT
            ).show()
            return
        }
        val fichero = File(getExternalFilesDir(null), "M8AX_Pi.txt")
        if (!fichero.exists()) {
            if (ttsEnabled) {
                tts?.speak("Calcula Pi Primero.", TextToSpeech.QUEUE_FLUSH, null, "ttsTetrisId")
            }
            Toast.makeText(
                this, "M8AX - ¡ Calcula PI Primero !", Toast.LENGTH_LONG
            ).show()
            return
        }
        try {
            if (ttsEnabled) {
                tts?.speak("Compartir...", TextToSpeech.QUEUE_FLUSH, null, "ttsTetrisId")
            }
            val uri = FileProvider.getUriForFile(this, "com.m8ax_megapi.fileprovider", fichero)
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            startActivity(Intent.createChooser(intent, "M8AX - Compartir PI"))
        } catch (e: Exception) {
        }
    }

    override fun onResume() {
        super.onResume()
        if (ttsEnabled) {
            tts?.stop()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        colorAnim?.cancel()
        colorAnim = null
        tts?.stop()
        tts?.shutdown()
        android.os.Process.killProcess(android.os.Process.myPid())
    }
}