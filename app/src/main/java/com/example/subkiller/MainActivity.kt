package com.example.subkiller

import android.content.Context
import android.graphics.Color
import android.os.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

/**
 * This a simple game
 */

class MainActivity : AppCompatActivity() {

    /**
     * Dimensions of canvas and some other const used for configuration
     */
    var W = 0F // this var is set by setUp()
    var H = 0F
    val PADD_TOP = 100F
    val PADD_BOTTOM = 40F
    val BASE_WIDTH = 140F
    val BASE_HEIHT = 80F

    val SUB_WIDTH = 135F
    val SUB_HEIHT = 70F

    val BOMBA_WIDTH = 34F
    val SUB_VELOCITY = 6 // configure the velocity for move the submarine
    val BOMBA_VELOCITY_DOWN = 14
    val BASE_VELOCITY = 32

    val BOR = 6F // BOrder draw inside canvas, this border is of cyan or gray color, depending game's state
    private val MAX_FRAME_EXP = 30 // max frame for make explosion animation

    /*
        Entities
     */
    lateinit var base: Base
    lateinit var bomba: Bomba
    lateinit var submarine: Submarine

    var hits = 0
    var misses: Int = 0
    private var focused: Boolean = true // if the window have focus or not

    lateinit var canvas: CanvasView
    lateinit var bottomNav: BottomNavigationView

    private lateinit var mainHandler: Handler

    /**
     * Esta al tanto de los movimientos de la bomba hacia abajo, y de los
     * movimientos del submarino a la izq o der. Tambien pintar el siguiente frame
     * de la animacion
     */
    private val makeTask = object : Runnable {
        override fun run() {
            // ignore when dimensions are not available
            if (W != 0F && H != 0F) {
                submarine.move()
                if (bomba.isLaunch) bomba.move(Mov.DOWN)
                paintFrame()
            }
            mainHandler.postDelayed(this, 16)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        bottomNav = findViewById(R.id.bottomNavigation)
        canvas = findViewById(R.id.canvas)
        mainHandler = Handler(Looper.getMainLooper())

    }

    /**
     * When user prees back bottom, show confirm alert
     */
    override fun onBackPressed() {
        onPause() //pause the game
        val alertBuilder = AlertDialog.Builder(this@MainActivity)
        alertBuilder.setTitle("Confirm?")
        alertBuilder.setMessage("Are yu sure do you want to exit?")
        alertBuilder.setCancelable(false)
        alertBuilder.setPositiveButton("YES") { _, _ ->
            /*end the game, the next time this window is open a new
             game state is created */
            super.onBackPressed()

        }
        alertBuilder.setNegativeButton("NO") { _, _ ->
            onResume() // continue the game
        }
        alertBuilder.create().show()
    }

    /**
     * This happen when the app is minimized. Stop the timer
     */
    override fun onPause() {
        super.onPause()
        focused = false
        mainHandler.removeCallbacks(makeTask)
        paintFrame()
    }

    /**
     * This happen when the app gain again focus. Restart the timer again
     */
    override fun onResume() {
        super.onResume()
        focused = true
        mainHandler.post(makeTask)
    }

    /**
     * Setting up when dim are available, this method is called from canvasView Object
     */
    fun setUp(w: Float, h: Float) {
        this.W = w
        this.H = h
        base = Base(
            (W - BASE_WIDTH) / 2,
            PADD_TOP,
            BASE_WIDTH,
            BASE_HEIHT,
            Color.BLUE,
            BASE_VELOCITY,
            0
        )
        bomba = Bomba(
            (W - BOMBA_WIDTH) / 2,
            base.y + base.h - BOMBA_WIDTH / 2,
            BOMBA_WIDTH,
            BOMBA_WIDTH,
            Color.RED,
            BASE_VELOCITY,
            BOMBA_VELOCITY_DOWN
        )
        submarine = Submarine(
            (Math.random() * (W - SUB_WIDTH)).toFloat(),
            H - (PADD_BOTTOM + SUB_HEIHT),
            SUB_WIDTH,
            SUB_HEIHT,
            Color.BLACK,
            SUB_VELOCITY,
            0
        )
        /**
         * Hanlde user's actions
         */
        bottomNav.setOnNavigationItemSelectedListener(BottomNavigationView.OnNavigationItemSelectedListener {
            lateinit var mov: Mov
            when (it.itemId) {
                R.id.left -> mov = Mov.LEFT
                R.id.right -> mov = Mov.RIGHT
                R.id.drop -> mov = Mov.DOWN
            }
            keyPressed(mov)
            return@OnNavigationItemSelectedListener true
        })
    }

    /**
     * Handle event when the user move arrow key, in this version that is
     * an android app a boton identify each arrow hey
     */
    private fun keyPressed(direction: Mov) {
        when (direction) {
            Mov.LEFT -> {
                base.move(direction) // move to left
                if (!bomba.isLaunch) bomba.move(direction)
            }
            Mov.RIGHT -> {
                base.move(direction)
                if (!bomba.isLaunch) bomba.move(direction)
            }
            else -> {
                //down
                bomba.isLaunch = true
            }
        }

    }

    /**
     * Draw the correspondent frame
     */
    private fun paintFrame() {
        canvas.paintFrame(hits, misses, focused, BOR)
        base.paint()
        bomba.paint()
        submarine.paint()
        canvas.invalidate()
    }

    /**
     * vibrate the phone during 500ms
     */
    private fun doVibration() {
        val v = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (Build.VERSION.SDK_INT >= 26)
            v.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE))
        else
            v.vibrate(500)
    }

    /**
     * Usefull clases
     */
    abstract inner class Shape protected constructor(
        var x: Float, // top left corner coordinates of the shape
        var y: Float,
        var w: Float,
        var h: Float,
        color: Int,
        movHor: Int,
        movVert: Int
    ) {
        protected var color: Int = color
        protected var movHor // cuantos px mover horizontalmente
                : Int = movHor
        protected var movVert // cuantos px mover vertical
                : Int = movVert
        /*
        * this method is override by Bomba subclass
        * */
        open fun move(dir: Mov) {
            if (dir == Mov.RIGHT) // move to right
                x += movHor
            else if (dir == Mov.LEFT) x -= movHor

            if (x < -w / 2) x = -w / 2
            if (x > W - w / 2) x = W - w / 2
        }

        // every shape know how to draw it
        abstract fun paint()

    }

    inner class Base(
        x: Float,
        y: Float,
        w: Float,
        h: Float,
        color: Int,
        movHor: Int,
        movVert: Int
    ) :
        Shape(x, y, w, h, color, movHor, movVert) {

        override fun paint() {
            canvas.drawBase(x, y, w, h, color)
        }
    }

    inner class Bomba(
        x: Float,
        y: Float,
        w: Float,
        h: Float,
        color: Int,
        movHor: Int,
        movVert: Int
    ) :
        Shape(x, y, w, h, color, movHor, movVert) {
        var isLaunch = false

        override fun move(dir: Mov) {
            if (dir == Mov.DOWN) {
                y += movVert
                // check for explosion
                if (x + w > submarine.x && x < submarine.x + submarine.w && y + h > submarine.y && y < submarine.y + submarine.h) {
                    // the submarine is exploding
                    submarine.isExplode = true
                    doVibration()
                    isLaunch = false
                    hits++
                } else if (y >= H) {
                    isLaunch = false
                    misses++
                }
                if (!isLaunch) {
                    // poner la bomba a la posicion inicial
                    y = base.y + base.h - w / 2
                    x = base.x + base.w / 2 - w / 2
                }
            } else super.move(dir)
        }

        override fun paint() {
            canvas.drawBomb(x, y, w, color)
        }
    }

    /**
     *
     * Objeto a ser explotado
     *
     */
    inner class Submarine(
        x: Float,
        y: Float,
        w: Float,
        h: Float,
        color: Int,
        movHor: Int,
        movVert: Int
    ) :
        Shape(x, y, w, h, color, movHor, movVert) {
        private var dir: Mov // left mean false, RIGHT is when is true

        var isExplode // if Exploding
                = false
        private var numExplode = 0
        fun move() {
            super.move(dir)
            if (Math.random() < 0.02) {
                // change sentido de direccion
                //como promedio 1 cada 100 veces
                if (dir == Mov.LEFT) dir = Mov.RIGHT else if (dir == Mov.RIGHT) dir = Mov.LEFT
            }
            //if reach a edge reverse the motion
            if (x < BOR) {
                dir = Mov.RIGHT
                x = BOR
            } else if (x > W - w - BOR) {
                dir = Mov.LEFT
                x = W - w - BOR
            }
        }

        override fun paint() {
            if (isExplode) {
                val nx = x + w / 2 // center of the submarine for make explosion
                val ny = y + h / 2
                numExplode++
                canvas.doExplosion(nx, ny, numExplode)

                if (numExplode == MAX_FRAME_EXP) {
                    numExplode = 0
                    isExplode = false
                    // reubicar el submarino en una posicion aleatoria en eje x
                    x = (Math.random() * (W - w)).toFloat()
                    dir = if (Math.random() > 0.5) Mov.LEFT else Mov.RIGHT
                }
            } else {
                // paint the submarine in the new position
                canvas.drawSubmarine(x, y, w, h, color)

            }
        }

        init {
            dir = if (Math.random() > 0.5) Mov.LEFT else Mov.RIGHT
        }
    }

    /**
     * Enum for arrow keys
     *
     * @author anniel
     */
    enum class Mov {
        LEFT, RIGHT, DOWN
    }
}