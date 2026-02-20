package com.m8ax_megapi

import android.content.Intent
import android.graphics.SurfaceTexture
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Surface
import android.view.TextureView
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.BounceInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.util.Calendar

class SplashActivity : AppCompatActivity() {
    private var mediaPlayer: MediaPlayer? = null
    private var videoPlayer: MediaPlayer? = null
    private val splashHandler = Handler(Looper.getMainLooper())
    private var runnableFinal: Runnable? = null
    private lateinit var logoVideo: TextureView
    private var isFinishingSplash = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        val rootLayout = findViewById<RelativeLayout>(R.id.rootLayout)
        val backgroundImage = findViewById<ImageView>(R.id.backgroundImage)
        val logoImage = findViewById<ImageView>(R.id.logoImage)
        val txtMensajeSplash = findViewById<TextView>(R.id.txtMensajeSplash)
        logoVideo = findViewById(R.id.logoVideo)
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val isSpecialDates =
            (month == Calendar.DECEMBER && day >= 20) || (month == Calendar.JANUARY && day <= 6)
        if (isSpecialDates) {
            backgroundImage.setImageResource(
                arrayOf(
                    R.drawable.m8axnavidad1,
                    R.drawable.m8axnavidad2,
                    R.drawable.m8axnavidad3,
                    R.drawable.m8axnavidad4
                ).random()
            )
            val mensajesNavideños = arrayOf(
                "M8AX: Calculando El Pi Navideño",
                "Analizando Índice De Velocidad En Fiestas",
                "¿ Podrá Tu Cpu Con El Algoritmo De Navidad ?",
                "Buscando El Decimal Dorado De Los Reyes Magos",
                "Midiendo Gigaflops Bajo El Árbol De Navidad",
                "¡ Cuidado ! Procesador Caliente Como Un Pavo Asado",
                "Verificando Núcleos Para El Cálculo De Año Nuevo",
                "M8AX: Extrayendo Precisión En Plena Nochebuena",
                "Test De Estrés Navideño: Máximo Rendimiento",
                "¿ Cuántos Decimales Te Traerán Los Reyes ?",
                "Sincronizando Algoritmos Con Las Campanadas",
                "M8AX: El Regalo Es La Potencia Bruta",
                "Optimizando Punto Flotante Para El Roscón",
                "Tu Móvil Analiza Pi Más Rápido Que Papá Noel",
                "Corriendo Chudnovsky Entre Polvorones",
                "M8AX: El Algoritmo Más Rápido De Estas Fiestas",
                "Estabilizando El Voltaje Del Árbol De Navidad",
                "Analizando Benchmark: ¿ Eres Un Reno O Un Cohete ?",
                "Forjando Decimales Con El Frío Del Invierno",
                "¡ Feliz Navidad ! Que Tu Soc No Se Derrita"
            )
            txtMensajeSplash.text = "\n${mensajesNavideños.random()}"
        } else {
            if (hour in 7..19) {
                backgroundImage.setImageResource(
                    arrayOf(
                        R.drawable.m8axdia, R.drawable.m8axdia2, R.drawable.m8axdia3
                    ).random()
                )
            } else {
                backgroundImage.setImageResource(
                    arrayOf(
                        R.drawable.m8axnoche, R.drawable.m8axnoche2, R.drawable.m8axnoche3
                    ).random()
                )
            }
            val mensajesMotivadores = arrayOf(
                "M8AX: Motor De Precisión Arbitraria",
                "Ejecutando Algoritmo Chudnovsky...",
                "Estabilizando Núcleos De CPU",
                "Verificando Integridad De RAM",
                "M8AX - The Algorithm Man - M8AX",
                "Llevando El Hardware Al Límite",
                "https://youtube.com/m8ax",
                "Midiendo Potencia Bruta Del Soc...",
                "Exprimiendo Hilos De Procesamiento",
                "M8AX: ¿ Tu Chip Está Preparado ?",
                "Test De Estrés: Máxima Frecuencia",
                "Optimizando Cálculo En Punto Flotante",
                "Detectando Límites Térmicos Del Terminal",
                "¡ Cuidado ! Hardware En Máximo Rendimiento",
                "Analizando Capacidad De Cálculo Paralelo",
                "M8AX: Extracción De Decimales Al 100%",
                "¿ Podrá Tu Procesador Con El Mega-Pi ?",
                "Aquellos Tiempos De 2M 2 Minutos ¿ Recuerdas ?",
                "Pon Tu CPU A Trabajar Duro",
                "Transistores... Preparados Para Trabajar...",
            )
            txtMensajeSplash.text = "\n${mensajesMotivadores.random()}"
        }
        backgroundImage.alpha = 0f
        backgroundImage.animate().alpha(1f).setDuration(1000).start()
        val imageLogos = arrayOf(
            R.drawable.logom8ax,
            R.drawable.logom8ax2,
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
        val videoLogos = arrayOf(
            R.raw.m8axvideo1,
            R.raw.m8axvideo2,
            R.raw.m8axvideo3,
            R.raw.m8axvideo4,
            R.raw.m8axvideo5,
            R.raw.m8axvideo6,
            R.raw.m8axvideo7,
            R.raw.m8axvideo8,
            R.raw.m8axvideo9
        )
        if (Math.random() < 0.5) {
            logoImage.visibility = View.VISIBLE
            logoImage.setImageResource(imageLogos.random())
            animarEntradaRandom(logoImage)
        } else {
            logoVideo.visibility = View.VISIBLE
            logoVideo.alpha = 0f
            logoVideo.surfaceTextureListener = object : TextureView.SurfaceTextureListener {
                override fun onSurfaceTextureAvailable(st: SurfaceTexture, w: Int, h: Int) {
                    val surface = Surface(st)
                    try {
                        videoPlayer = MediaPlayer()
                        videoPlayer?.setDataSource(
                            this@SplashActivity,
                            Uri.parse("android.resource://$packageName/${videoLogos.random()}")
                        )
                        videoPlayer?.setSurface(surface)
                        videoPlayer?.isLooping = true
                        videoPlayer?.prepareAsync()
                        videoPlayer?.setOnPreparedListener { mp ->
                            mp.start()
                            animarEntradaRandom(logoVideo)
                        }
                    } catch (e: Exception) {
                    }
                }

                override fun onSurfaceTextureSizeChanged(st: SurfaceTexture, w: Int, h: Int) {}
                override fun onSurfaceTextureDestroyed(st: SurfaceTexture): Boolean = true
                override fun onSurfaceTextureUpdated(st: SurfaceTexture) {}
            }
        }
        val sounds = arrayOf(
            R.raw.m8axinicio1,
            R.raw.m8axinicio2,
            R.raw.m8axinicio3,
            R.raw.m8axinicio4,
            R.raw.m8axinicio5,
            R.raw.m8axinicio6,
            R.raw.m8axinicio7,
            R.raw.m8axinicio8,
            R.raw.m8axinicio9,
            R.raw.m8axinicio10
        )
        mediaPlayer = MediaPlayer.create(this, sounds.random())
        mediaPlayer?.start()
        runnableFinal = Runnable {
            if (!isFinishingSplash) {
                val viewParaMover =
                    if (logoImage.visibility == View.VISIBLE) logoImage else logoVideo
                vibrarVisualmente(viewParaMover)
                viewParaMover.postDelayed({
                    val metrics = resources.displayMetrics
                    val screenWidth = metrics.widthPixels.toFloat() * 1.5f
                    val screenHeight = metrics.heightPixels.toFloat() * 1.5f
                    val direcciones = arrayOf("ARRIBA", "ABAJO", "IZQUIERDA", "DERECHA")
                    val direccionFinal = direcciones.random()
                    val randomRotation = (720..1440).random().toFloat()
                    val randomDuration = (700..1100).random().toLong()
                    var moveX = 0f
                    var moveY = 0f
                    when (direccionFinal) {
                        "ARRIBA" -> moveY = -screenHeight
                        "ABAJO" -> moveY = screenHeight
                        "IZQUIERDA" -> moveX = -screenWidth
                        "DERECHA" -> moveX = screenWidth
                    }
                    viewParaMover.animate().translationX(moveX).translationY(moveY)
                        .rotation(randomRotation).alpha(0f).setDuration(randomDuration)
                        .withEndAction {
                            rootLayout.animate().alpha(0f).setDuration(400).withEndAction {
                                if (!isFinishingSplash) {
                                    liberarRecursos()
                                    startActivity(Intent(this, MainActivity::class.java))
                                    overridePendingTransition(
                                        android.R.anim.fade_in, android.R.anim.fade_out
                                    )
                                    finish()
                                }
                            }.start()
                        }.start()
                }, 300)
            }
        }
        runnableFinal?.let { splashHandler.postDelayed(it, 5000) }
        onBackPressedDispatcher.addCallback(
            this, object : androidx.activity.OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    isFinishingSplash = true
                    splashHandler.removeCallbacksAndMessages(null)
                    liberarRecursos()
                    finishAndRemoveTask()
                }
            })
    }

    private fun animarEntradaRandom(view: View) {
        view.alpha = 1f
        view.scaleX = 1f
        view.scaleY = 1f
        view.rotation = 0f
        view.translationY = 0f
        val opcion = (1..4).random()
        when (opcion) {
            1 -> {
                view.translationY = -1500f
                view.animate().translationY(0f).setDuration(1000)
                    .setInterpolator(AccelerateInterpolator()).withEndAction {
                        vibrarImpacto()
                        view.animate().translationY(-200f).setDuration(250)
                            .setInterpolator(DecelerateInterpolator()).withEndAction {
                                view.animate().translationY(0f).setDuration(250)
                                    .setInterpolator(AccelerateInterpolator()).withEndAction {
                                        vibrarImpacto()
                                        view.animate().translationY(-80f).setDuration(150)
                                            .setInterpolator(DecelerateInterpolator())
                                            .withEndAction {
                                                view.animate().translationY(0f).setDuration(150)
                                                    .setInterpolator(AccelerateInterpolator())
                                                    .withEndAction {
                                                        vibrarImpacto()
                                                    }.start()
                                            }.start()
                                    }.start()
                            }.start()
                    }.start()
            }

            2 -> {
                view.alpha = 0f
                view.animate().alpha(1f).setDuration(1000).start()
            }

            3 -> {
                view.rotation = -720f
                view.scaleX = 0f
                view.scaleY = 0f
                view.animate().rotation(0f).scaleX(1f).scaleY(1f).setDuration(1200)
                    .setInterpolator(DecelerateInterpolator()).start()
            }

            4 -> {
                view.translationY = -1500f
                view.animate().translationY(0f).setDuration(600)
                    .setInterpolator(AccelerateInterpolator()).withEndAction {
                        vibrarImpacto()
                        view.animate().scaleY(0.7f).scaleX(1.3f).setDuration(100).withEndAction {
                            view.animate().scaleY(1f).scaleX(1f).setDuration(300)
                                .setInterpolator(BounceInterpolator()).start()
                        }.start()
                    }.start()
            }
        }
    }

    private fun vibrarVisualmente(view: View) {
        vibrarFuerte()
        val d = 25L
        val a = 25f
        view.animate().translationXBy(a).setDuration(d).withEndAction {
            view.animate().translationXBy(-a * 2).setDuration(d).withEndAction {
                view.animate().translationXBy(a * 2).setDuration(d).withEndAction {
                    view.animate().translationXBy(-a * 2).setDuration(d).withEndAction {
                        view.animate().translationXBy(a * 2).setDuration(d).withEndAction {
                            view.animate().translationXBy(-a * 2).setDuration(d).withEndAction {
                                view.animate().translationXBy(a).setDuration(d).start()
                            }.start()
                        }.start()
                    }.start()
                }.start()
            }.start()
        }.start()
    }

    private fun vibrarImpacto() {
        val vibrator = getSystemService(VIBRATOR_SERVICE) as android.os.Vibrator
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            vibrator.vibrate(
                android.os.VibrationEffect.createOneShot(
                    50, android.os.VibrationEffect.DEFAULT_AMPLITUDE
                )
            )
        } else {
            @Suppress("DEPRECATION") vibrator.vibrate(50)
        }
    }

    private fun vibrarFuerte() {
        val vibrator = getSystemService(VIBRATOR_SERVICE) as android.os.Vibrator
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val pattern = longArrayOf(0, 40, 20, 40, 20, 40)
            vibrator.vibrate(android.os.VibrationEffect.createWaveform(pattern, -1))
        } else {
            @Suppress("DEPRECATION") vibrator.vibrate(200)
        }
    }

    private fun liberarRecursos() {
        try {
            mediaPlayer?.let { if (it.isPlaying) it.stop(); it.release() }
            mediaPlayer = null
            videoPlayer?.let { if (it.isPlaying) it.stop(); it.release() }
            videoPlayer = null
        } catch (e: Exception) {
        }
    }

    override fun onPause() {
        super.onPause()
        mediaPlayer?.pause()
        videoPlayer?.pause()
    }

    override fun onResume() {
        super.onResume()
        if (!isFinishingSplash) {
            mediaPlayer?.start()
            videoPlayer?.start()
        }
    }

    override fun onDestroy() {
        isFinishingSplash = true
        splashHandler.removeCallbacksAndMessages(null)
        liberarRecursos()
        super.onDestroy()
    }
}