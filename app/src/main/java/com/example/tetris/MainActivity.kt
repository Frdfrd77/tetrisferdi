package com.example.tetris

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import kotlin.math.abs

class MainActivity : AppCompatActivity() {
    private lateinit var gameView: GameView
    private lateinit var game: TetrisGame
    private lateinit var restartButton: Button
    private lateinit var dropButton: Button
    private lateinit var startButton: Button
    private lateinit var welcomeScreen: LinearLayout
    private lateinit var gestureDetector: GestureDetector
    private val handler = Handler(Looper.getMainLooper())
    
    private var lastX = 0f
    private var lastY = 0f
    private val swipeThreshold = 50f 
    private var isGameStarted = false

    private val gameRunnable = object : Runnable {
        override fun run() {
            if (isGameStarted && !game.isGameOver()) {
                game.moveDown()
                gameView.postInvalidate()
                handler.postDelayed(this, 700)
            } else if (game.isGameOver()) {
                restartButton.visibility = View.VISIBLE
                dropButton.visibility = View.GONE
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout)

        gameView = findViewById(R.id.gameView)
        restartButton = findViewById(R.id.restartButton)
        dropButton = findViewById(R.id.dropButton)
        startButton = findViewById(R.id.startButton)
        welcomeScreen = findViewById(R.id.welcomeScreen)
        
        setupGame()

        startButton.setOnClickListener {
            welcomeScreen.visibility = View.GONE
            dropButton.visibility = View.VISIBLE
            isGameStarted = true
            handler.post(gameRunnable)
        }

        restartButton.setOnClickListener {
            restartButton.visibility = View.GONE
            dropButton.visibility = View.VISIBLE
            setupGame()
            handler.removeCallbacks(gameRunnable)
            handler.post(gameRunnable)
        }

        dropButton.setOnClickListener {
            if (isGameStarted && !game.isGameOver()) {
                game.dropInstantly()
                gameView.invalidate()
            }
        }

        gestureDetector = GestureDetector(this, object : GestureDetector.SimpleOnGestureListener() {
            override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
                if (isGameStarted && !game.isGameOver()) {
                    game.rotatePiece()
                    gameView.invalidate()
                }
                return true
            }
        })

        gameView.setOnTouchListener { _, event ->
            if (!isGameStarted || game.isGameOver()) return@setOnTouchListener false
            
            gestureDetector.onTouchEvent(event)
            
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    lastX = event.x
                    lastY = event.y
                }
                MotionEvent.ACTION_MOVE -> {
                    val dx = event.x - lastX
                    val dy = event.y - lastY

                    if (abs(dx) > swipeThreshold) {
                        if (dx > 0) game.moveRight()
                        else game.moveLeft()
                        lastX = event.x
                        gameView.invalidate()
                    }
                    
                    if (dy > swipeThreshold * 1.5) {
                        game.moveDown()
                        lastY = event.y
                        gameView.invalidate()
                    }
                }
            }
            true
        }
    }

    private fun setupGame() {
        game = TetrisGame()
        gameView.setGame(game)
        gameView.invalidate()
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(gameRunnable)
    }
}
