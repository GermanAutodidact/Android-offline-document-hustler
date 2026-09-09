package com.example.model

sealed interface TextCommand {
    fun apply(current: String): String
    fun revert(current: String): String

    data class Insert(val index: Int, val text: String) : TextCommand {
        override fun apply(current: String): String {
            val safeIndex = index.coerceIn(0, current.length)
            return current.substring(0, safeIndex) + text + current.substring(safeIndex)
        }

        override fun revert(current: String): String {
            val safeIndex = index.coerceIn(0, current.length)
            val endIndex = (safeIndex + text.length).coerceAtMost(current.length)
            return current.removeRange(safeIndex, endIndex)
        }
    }

    data class Delete(val index: Int, val text: String) : TextCommand {
        override fun apply(current: String): String {
            val safeIndex = index.coerceIn(0, current.length)
            val endIndex = (safeIndex + text.length).coerceAtMost(current.length)
            return current.removeRange(safeIndex, endIndex)
        }

        override fun revert(current: String): String {
            val safeIndex = index.coerceIn(0, current.length)
            return current.substring(0, safeIndex) + text + current.substring(safeIndex)
        }
    }

    data class Replace(val index: Int, val oldText: String, val newText: String) : TextCommand {
        override fun apply(current: String): String {
            val safeIndex = index.coerceIn(0, current.length)
            val endIndex = (safeIndex + oldText.length).coerceAtMost(current.length)
            return current.substring(0, safeIndex) + newText + current.substring(endIndex)
        }

        override fun revert(current: String): String {
            val safeIndex = index.coerceIn(0, current.length)
            val endIndex = (safeIndex + newText.length).coerceAtMost(current.length)
            return current.substring(0, safeIndex) + oldText + current.substring(endIndex)
        }
    }
}

class UndoRedoManager(private val maxHistory: Int = 100) {
    private val undoStack = ArrayDeque<TextCommand>()
    private val redoStack = ArrayDeque<TextCommand>()

    val canUndo: Boolean get() = undoStack.isNotEmpty()
    val canRedo: Boolean get() = redoStack.isNotEmpty()

    fun pushCommand(command: TextCommand) {
        if (undoStack.size >= maxHistory) {
            undoStack.removeFirst()
        }
        undoStack.addLast(command)
        redoStack.clear()
    }

    fun undo(current: String): String {
        if (undoStack.isEmpty()) return current
        val cmd = undoStack.removeLast()
        val undone = cmd.revert(current)
        redoStack.addLast(cmd)
        return undone
    }

    fun redo(current: String): String {
        if (redoStack.isEmpty()) return current
        val cmd = redoStack.removeLast()
        val redone = cmd.apply(current)
        undoStack.addLast(cmd)
        return redone
    }

    fun clear() {
        undoStack.clear()
        redoStack.clear()
    }
}
