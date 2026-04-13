package com.example.tetris

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

class GameView(context: Context, attrs: AttributeSet? = null) : View(context, attrs) {
    private var game: TetrisGame? = null
    private val paint = Paint()
    private var blockSize = 0f
    private var offsetX = 0f
    private var offsetY = 0f

    private val colors = mapOf(
        0 to Color.BLACK,
        1 to Color.CYAN,    // I
        2 to Color.YELLOW,  // O
        3 to Color.MAGENTA, // T
        4 to Color.GREEN,   // S
        5 to Color.RED,     // Z
        6 to Color.BLUE,    // J
        7 to Color.rgb(255, 165, 0) // Orange (L)
    )

    fun setGame(game: TetrisGame) {
        this.game = game
        invalidate()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        val availableWidth = w.toFloat()
        val availableHeight = h.toFloat()
        
        blockSize = Math.min(availableWidth / TetrisGame.GRID_WIDTH, availableHeight / TetrisGame.GRID_HEIGHT)
        
        offsetX = (availableWidth - (TetrisGame.GRID_WIDTH * blockSize)) / 2
        offsetY = (availableHeight - (TetrisGame.GRID_HEIGHT * blockSize)) / 2
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val currentGame = game ?: return

        canvas.drawColor(Color.BLACK)

        // Grid çiz
        val grid = currentGame.getGrid()
        for (y in 0 until TetrisGame.GRID_HEIGHT) {
            for (x in 0 until TetrisGame.GRID_WIDTH) {
                if (grid[y][x] != 0) {
                    drawBlock(canvas, x, y, grid[y][x])
                } else {
                    drawEmptyBlock(canvas, x, y)
                }
            }
        }

        // Aktif parçayı çiz
        currentGame.getCurrentPiece()?.let { piece ->
            val blocks = piece.getBlocks()
            for ((bx, by) in blocks) {
                val x = piece.x + bx
                val y = piece.y + by
                if (y >= 0 && y < TetrisGame.GRID_HEIGHT && x >= 0 && x < TetrisGame.GRID_WIDTH) {
                    drawBlock(canvas, x, y, piece.type)
                }
            }
        }

        // Skor yazısı - Biraz daha aşağı kaydırıldı (80f -> 140f)
        paint.color = Color.WHITE
        paint.textSize = 60f
        paint.style = Paint.Style.FILL
        paint.textAlign = Paint.Align.LEFT
        canvas.drawText("Score: ${currentGame.getScore()}", 40f, 140f, paint)

        if (currentGame.isGameOver()) {
            paint.color = Color.RED
            paint.textSize = 100f
            paint.textAlign = Paint.Align.CENTER
            canvas.drawText("GAME OVER", width / 2f, height / 2f, paint)
        }
    }

    private fun drawBlock(canvas: Canvas, x: Int, y: Int, type: Int) {
        val left = offsetX + x * blockSize
        val top = offsetY + y * blockSize
        val right = left + blockSize
        val bottom = top + blockSize

        // Fill
        paint.style = Paint.Style.FILL
        paint.color = colors[type] ?: Color.GRAY
        canvas.drawRect(left + 2, top + 2, right - 2, bottom - 2, paint)

        // Border
        paint.style = Paint.Style.STROKE
        paint.color = Color.WHITE
        paint.strokeWidth = 2f
        canvas.drawRect(left, top, right, bottom, paint)
    }

    private fun drawEmptyBlock(canvas: Canvas, x: Int, y: Int) {
        val left = offsetX + x * blockSize
        val top = offsetY + y * blockSize
        val right = left + blockSize
        val bottom = top + blockSize

        paint.style = Paint.Style.STROKE
        paint.color = Color.DKGRAY
        paint.strokeWidth = 1f
        canvas.drawRect(left, top, right, bottom, paint)
    }
}
