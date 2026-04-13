package com.example.tetris

class TetrisGame {
    companion object {
        const val GRID_WIDTH = 10
        const val GRID_HEIGHT = 20
    }

    private var grid = Array(GRID_HEIGHT) { IntArray(GRID_WIDTH) }
    private var currentPiece: Tetromino? = null
    private var score = 0
    private var gameOver = false

    init {
        spawnPiece()
    }

    fun spawnPiece() {
        val piece = Tetromino.random()
        piece.x = GRID_WIDTH / 2 - 2
        piece.y = 0
        
        if (canPlace(piece, piece.x, piece.y)) {
            currentPiece = piece
        } else {
            currentPiece = piece
            gameOver = true
        }
    }

    fun moveLeft() = move(-1, 0)
    fun moveRight() = move(1, 0)
    fun moveDown(): Boolean {
        if (gameOver) return false
        if (move(0, 1)) {
            return true
        } else {
            lockPiece()
            clearLines()
            spawnPiece()
            return false
        }
    }

    fun dropInstantly() {
        if (gameOver) return
        while (move(0, 1)) {
            // Keep moving down until it hits something
        }
        lockPiece()
        clearLines()
        spawnPiece()
    }

    private fun move(dx: Int, dy: Int): Boolean {
        val piece = currentPiece ?: return false
        if (canPlace(piece, piece.x + dx, piece.y + dy)) {
            piece.x += dx
            piece.y += dy
            return true
        }
        return false
    }

    fun rotatePiece() {
        val piece = currentPiece ?: return
        val originalX = piece.x
        val originalY = piece.y
        
        piece.rotate()
        
        // Wall Kick Logic: Bloğu döndürdüğümüzde duvara veya başka bir bloğa çarpıyorsa,
        // onu hafifçe sağa, sola veya yukarı kaydırarak sığdırmaya çalışır.
        val offsets = listOf(
            Pair(0, 0),   // Mevcut konum
            Pair(1, 0),   // Sağa kaydır
            Pair(-1, 0),  // Sola kaydır
            Pair(0, -1),  // Yukarı kaydır (tabana çarptıysa)
            Pair(2, 0),   // İki birim sağa (I bloğu için)
            Pair(-2, 0)   // İki birim sola (I bloğu için)
        )
        
        var success = false
        for (offset in offsets) {
            if (canPlace(piece, originalX + offset.first, originalY + offset.second)) {
                piece.x = originalX + offset.first
                piece.y = originalY + offset.second
                success = true
                break
            }
        }
        
        if (!success) {
            piece.rotateBack()
            piece.x = originalX
            piece.y = originalY
        }
    }

    private fun canPlace(piece: Tetromino, x: Int, y: Int): Boolean {
        for (block in piece.getBlocks()) {
            val nx = x + block.first
            val ny = y + block.second
            if (nx !in 0 until GRID_WIDTH || ny !in 0 until GRID_HEIGHT) return false
            if (ny >= 0 && grid[ny][nx] != 0) return false
        }
        return true
    }

    private fun lockPiece() {
        val piece = currentPiece ?: return
        for (block in piece.getBlocks()) {
            val nx = piece.x + block.first
            val ny = piece.y + block.second
            if (ny in 0 until GRID_HEIGHT && nx in 0 until GRID_WIDTH) {
                grid[ny][nx] = piece.type
            }
        }
    }

    private fun clearLines() {
        var lines = 0
        for (y in GRID_HEIGHT - 1 downTo 0) {
            if (grid[y].all { it != 0 }) {
                for (ty in y downTo 1) {
                    grid[ty] = grid[ty - 1].copyOf()
                }
                grid[0] = IntArray(GRID_WIDTH)
                lines++
                // Re-check the same Y because everything shifted down
                clearLines() 
                break
            }
        }
        if (lines > 0) score += lines * 100
    }

    fun getGrid() = grid
    fun getCurrentPiece() = currentPiece
    fun getScore() = score
    fun isGameOver() = gameOver
}

class Tetromino(val type: Int) {
    var x = 0
    var y = 0
    var rotation = 0

    fun getBlocks(): List<Pair<Int, Int>> {
        val base = when (type) {
            1 -> listOf(Pair(0, 1), Pair(1, 1), Pair(2, 1), Pair(3, 1)) // I
            2 -> listOf(Pair(1, 0), Pair(2, 0), Pair(1, 1), Pair(2, 1)) // O
            3 -> listOf(Pair(1, 0), Pair(0, 1), Pair(1, 1), Pair(2, 1)) // T
            4 -> listOf(Pair(1, 0), Pair(2, 0), Pair(0, 1), Pair(1, 1)) // S
            5 -> listOf(Pair(0, 0), Pair(1, 0), Pair(1, 1), Pair(2, 1)) // Z
            6 -> listOf(Pair(0, 0), Pair(0, 1), Pair(1, 1), Pair(2, 1)) // J
            7 -> listOf(Pair(2, 0), Pair(0, 1), Pair(1, 1), Pair(2, 1)) // L
            else -> listOf()
        }
        
        var result = base
        repeat(rotation % 4) {
            result = result.map { Pair(2 - it.second, it.first) }
        }
        return result
    }

    fun rotate() { rotation = (rotation + 1) % 4 }
    fun rotateBack() { rotation = (rotation + 3) % 4 }

    companion object {
        fun random() = Tetromino((1..7).random())
    }
}
