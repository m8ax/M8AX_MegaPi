package com.m8ax_megapi

import android.animation.ArgbEvaluator
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
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
import java.util.Random

class MainActivity : AppCompatActivity() {
    private var ramTotalGB: Double = 0.0
    private var calculando = false
    private var currentTextSize = 9f
    private var contadorespe = 0
    private var ultimosMillonesPulsados: String = ""
    private var colorAnim: ObjectAnimator? = null
    private lateinit var tvNotaRam: TextView
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
        R.id.btn2000M,
        R.id.btn3000M,
        R.id.btn4000M,
        R.id.btn5000M,
        R.id.btn10000M,
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
        setContentView(R.layout.activity_main)
        val mainView = findViewById<View>(R.id.main_container)
        mainView?.let { vista ->
            ViewCompat.setOnApplyWindowInsetsListener(vista) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                insets
            }
        }
        val tvConsola = findViewById<TextView>(R.id.tvConsolaM8AX)
        val scaleDetector = android.view.ScaleGestureDetector(
            this, object : android.view.ScaleGestureDetector.SimpleOnScaleGestureListener() {
                override fun onScale(detector: android.view.ScaleGestureDetector): Boolean {
                    currentTextSize *= detector.scaleFactor
                    currentTextSize = currentTextSize.coerceIn(9f, 60f)
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
            "M8AX - RAM Libre: ${String.format("%.2f", ramTotalGB)}GB"
        val fichero = File(getExternalFilesDir(null), "M8AX_Pi.txt")
        if (fichero.exists()) fichero.delete()
        val tvNota = findViewById<TextView>(R.id.tvNotaRam)
        tvNota.movementMethod = android.text.method.LinkMovementMethod.getInstance()
        tvNotaRam = tvNota
        gestionarBotonera(true)
    }

    private fun startParpadeo(textView: TextView) {
        colorAnim = ObjectAnimator.ofInt(
            textView, "textColor", Color.parseColor("#FF9800"), Color.parseColor("#FFFFFF")
        )
        colorAnim?.duration = 500
        colorAnim?.setEvaluator(ArgbEvaluator())
        colorAnim?.repeatCount = ValueAnimator.INFINITE
        colorAnim?.repeatMode = ValueAnimator.REVERSE
        colorAnim?.start()
    }

    private fun stopParpadeo(textView: TextView) {
        colorAnim?.cancel()
        textView.setTextColor(Color.parseColor("#FF9800"))
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
                R.id.btn2000M to 2000L,
                R.id.btn3000M to 3000L,
                R.id.btn4000M to 4000L,
                R.id.btn5000M to 5000L,
                R.id.btn10000M to 10000L
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
                            millones >= 10000 -> {
                                ramNecesaria = 64.0; if (ramTotalGB < 64.0) bloqueadoPorRam = true
                            }

                            millones >= 5000 -> {
                                ramNecesaria = 32.0; if (ramTotalGB < 32.0) bloqueadoPorRam = true
                            }

                            millones >= 4000 -> {
                                ramNecesaria = 28.0; if (ramTotalGB < 28.0) bloqueadoPorRam = true
                            }

                            millones >= 3000 -> {
                                ramNecesaria = 24.0; if (ramTotalGB < 24.0) bloqueadoPorRam = true
                            }

                            millones >= 2000 -> {
                                ramNecesaria = 16.0; if (ramTotalGB < 16.0) bloqueadoPorRam = true
                            }

                            millones >= 1000 -> {
                                ramNecesaria = 10.0; if (ramTotalGB < 10.0) bloqueadoPorRam = true
                            }

                            millones >= 900 -> {
                                ramNecesaria = 9.5; if (ramTotalGB < 9.5) bloqueadoPorRam = true
                            }

                            millones >= 800 -> {
                                ramNecesaria = 9.0; if (ramTotalGB < 9.0) bloqueadoPorRam = true
                            }

                            millones >= 700 -> {
                                ramNecesaria = 8.0; if (ramTotalGB < 8.0) bloqueadoPorRam = true
                            }

                            millones >= 600 -> {
                                ramNecesaria = 7.0; if (ramTotalGB < 7.0) bloqueadoPorRam = true
                            }

                            millones >= 500 -> {
                                ramNecesaria = 5.5; if (ramTotalGB < 5.5) bloqueadoPorRam = true
                            }

                            millones >= 400 -> {
                                ramNecesaria = 4.5; if (ramTotalGB < 4.5) bloqueadoPorRam = true
                            }

                            millones >= 300 -> {
                                ramNecesaria = 3.5; if (ramTotalGB < 3.5) bloqueadoPorRam = true
                            }

                            millones >= 200 -> {
                                ramNecesaria = 2.5; if (ramTotalGB < 2.5) bloqueadoPorRam = true
                            }

                            millones >= 100 -> {
                                ramNecesaria = 1.5; if (ramTotalGB < 1.5) bloqueadoPorRam = true
                            }

                            millones >= 80 -> {
                                ramNecesaria = 1.2; if (ramTotalGB < 1.2) bloqueadoPorRam = true
                            }

                            millones >= 60 -> {
                                ramNecesaria = 0.9; if (ramTotalGB < 0.9) bloqueadoPorRam = true
                            }

                            millones >= 40 -> {
                                ramNecesaria = 0.6; if (ramTotalGB < 0.6) bloqueadoPorRam = true
                            }

                            millones >= 20 -> {
                                ramNecesaria = 0.4; if (ramTotalGB < 0.4) bloqueadoPorRam = true
                            }

                            millones >= 10 -> {
                                ramNecesaria = 0.25; if (ramTotalGB < 0.25) bloqueadoPorRam = true
                            }

                            millones >= 6 -> {
                                ramNecesaria = 0.18; if (ramTotalGB < 0.18) bloqueadoPorRam = true
                            }

                            millones >= 1 -> {
                                ramNecesaria = 0.08; if (ramTotalGB < 0.08) bloqueadoPorRam = true
                            }
                        }
                        if (bloqueadoPorRam) {
                            btn.backgroundTintList = colorOscuro
                            btn.setTextColor(Color.DKGRAY)
                            btn.setOnClickListener {
                                Toast.makeText(
                                    this,
                                    "M8AX - RAM Insuficiente (Necesitas > ${ramNecesaria}GB)",
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
        val fechaCompilacion = LocalDateTime.parse("17/02/2026 17:25", formatoCompilacion)
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
            "... Fecha De Compilación - 17/02/2026 17:25 ...\n\n... Tmp. Desde Compilación - ${años}a${dias}d${horas}h${minutos}m${segundos}s ..."
        val prefs = getSharedPreferences("M8AX-Dejar_De_Fumar", Context.MODE_PRIVATE)
        val textoIzquierda = SpannableString(
            "App Creada Por MarcoS OchoA DieZ - ( M8AX )\n\n" + "Mail - mviiiax.m8ax@gmail.com\n\n" + "Youtube - https://youtube.com/m8ax\n\n" + "El Futuro No Está Establecido, No Hay Destino, Solo Existe El Que Nosotros Hacemos...\n\n\n" + "... Creado En 17h De Programación ...\n\n" + "... Con +/- 3500 Líneas De Código ...\n\n" + "... +/- 200 KB En Texto Plano | TXT | ...\n\n" + "... +/- Novela Cándido De Voltaire En Código ...\n\n" + tiempoTranscurrido + "\n\n"
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
        startParpadeo(tvNotaRam)
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
                            val activityManager =
                                getSystemService(ACTIVITY_SERVICE) as android.app.ActivityManager
                            val memoryInfo = android.app.ActivityManager.MemoryInfo()
                            activityManager.getMemoryInfo(memoryInfo)
                            ramTotalGB = memoryInfo.totalMem.toDouble() / (1024.0 * 1024.0 * 1024.0)
                            txtStatus.text = "M8AX - Cálculo Finalizado · RAM Libre: ${
                                String.format(
                                    "%.2f", ramTotalGB
                                )
                            }GB"
                            calculando = false
                            stopParpadeo(tvNotaRam)
                            gestionarBotonera(true)
                            window.clearFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                            android.app.AlertDialog.Builder(this@MainActivity)
                                .setTitle("M8AX MEGA PI BENCHMARK").setMessage(
                                    "M8AX - Cálculo: [ $ultimosMillonesPulsados ]\n\n" + (if (lineaIndiceFinal.isNotEmpty()) lineaIndiceFinal else "Cálculo Completado")
                                ).setPositiveButton("ACEPTAR", null).show()
                        }
                    } catch (e: Exception) {
                        calculando = false
                    }
                }.start()
                while (calculando) {
                    val linea = reader.readLine() ?: break
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
                reader.close()
                procesoLog.destroy()
                Runtime.getRuntime().exec(arrayOf("sh", "-c", "pkill -9 logcat"))
            } catch (e: Exception) {
                runOnUiThread { gestionarBotonera(true) }
            }
        }.start()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
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
                "M8AX MegaPi: Suite De Diagnóstico Extremo.\n\n" + "• Algoritmo Principal: Implementación Chudnovsky (C Nativo).\n" + "• Multiplicación: Basada En FFT (Transformada Rápida De Fourier).\n" + "• Inversión/División: Método De Newton.\n" + "• Optimización: División Binaria (Binary Splitting).\n\n" + "• Cálculo: Decimales De Pi Con Precisión Arbitraria.\n\n" + "• Propósito 1: Test De Estrés Térmico Para CPU Y RAM.\n" + "• Propósito 2: Test De Estabilidad De CPU Y RAM.\n" + "• Propósito 3: Test De Velocidad De Cálculo De Tu Móvil.\n\n" + "• Nota: Puedes Seleccionar Cualquier Test 2M 10M 60M, Etc...\n\n" + "• 10M Es El Más Normalizado Para Comparar Rendimiento Entre Dispositivos.\n\n" + "• Librerías: Gestión Mediante GMP ( GNU Multi-Precision Library ).\n\n" + "Este Benchmark Lleva El Hardware Al Límite Operativo. En La Consola, Tienes Todo Tipo De Datos Estadísticos."
            setTextColor(Color.WHITE)
            textSize = 16f
            gravity = Gravity.START
        }
        val btnClose = Button(this).apply {
            text = "ENTENDIDO"
            setBackgroundColor(Color.parseColor("#2196F3"))
            setTextColor(Color.WHITE)
            setOnClickListener { dialog.dismiss() }
        }
        layout.addView(tvTitle)
        layout.addView(tvContent)
        layout.addView(btnClose)
        scroll.addView(layout)
        dialog.setContentView(scroll)
        dialog.window?.setLayout(
            android.view.ViewGroup.LayoutParams.MATCH_PARENT,
            android.view.ViewGroup.LayoutParams.MATCH_PARENT
        )
        dialog.show()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_borrar) {
            val fichero = File(getExternalFilesDir(null), "M8AX_Pi.txt")
            if (fichero.exists()) {
                fichero.delete()
                Toast.makeText(this, "M8AX - Archivo M8AX_Pi.txt Eliminado", Toast.LENGTH_SHORT)
                    .show()
            } else {
                Toast.makeText(this, "M8AX - No Hay Archivo Para Borrar", Toast.LENGTH_SHORT).show()
            }
            return true
        }
        if (item.itemId == R.id.action_ayuda) {
            mostrarDialogoAyuda()
            return true
        }
        if (item.itemId == R.id.action_acerca_de) {
            showAboutDialog()
            return true
        }
        if (item.itemId == R.id.action_chess) {
            val intent = Intent(this, ChessActivity::class.java)
            startActivity(intent)
            return true
        }
        if (item.itemId == R.id.action_pong) {
            val intent = Intent(this, ThePong::class.java)
            startActivity(intent)
            return true
        }
        if (item.itemId == R.id.action_salir) {
            calculando = false
            try {
                Runtime.getRuntime().exec(arrayOf("sh", "-c", "pkill -9 logcat"))
                window.clearFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            } catch (e: Exception) {
            }
            finishAffinity()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun compartirArchivo() {
        if (calculando) {
            Toast.makeText(
                this, "M8AX - Espera A Que Termine El Cálculo Actual", Toast.LENGTH_SHORT
            ).show()
            return
        }
        val fichero = File(getExternalFilesDir(null), "M8AX_Pi.txt")
        if (!fichero.exists()) {
            Toast.makeText(
                this, "M8AX - ¡ Calcula PI Primero !", Toast.LENGTH_LONG
            ).show()
            return
        }
        try {
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

    override fun onDestroy() {
        super.onDestroy()
        colorAnim?.cancel()
        colorAnim = null
        android.os.Process.killProcess(android.os.Process.myPid())
    }
}