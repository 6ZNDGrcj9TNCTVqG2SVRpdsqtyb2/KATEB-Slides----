package com.example.engine

/**
 * Memento storing an immutable point-in-time snapshot of the Presentation editor state.
 */
data class PresentationMemento(
    val presentation: Presentation,
    val selectedSlideIndex: Int,
    val timestampMs: Long = System.currentTimeMillis()
)

typealias EditorSnapshot = PresentationMemento

/**
 * Caretaker managing bounded undo and redo stacks of PresentationMemento states.
 */
class PresentationCaretaker(private val maxHistory: Int = 50) {
    private val undoStack = ArrayDeque<PresentationMemento>()
    private val redoStack = ArrayDeque<PresentationMemento>()

    val canUndo: Boolean get() = undoStack.isNotEmpty()
    val canRedo: Boolean get() = redoStack.isNotEmpty()

    fun saveMemento(memento: PresentationMemento) {
        if (undoStack.size >= maxHistory) {
            undoStack.removeFirst()
        }
        undoStack.addLast(memento)
        redoStack.clear()
    }

    fun pushUndo(snapshot: EditorSnapshot) = saveMemento(snapshot)

    fun undo(current: EditorSnapshot): EditorSnapshot? {
        if (undoStack.isEmpty()) return null
        val previous = undoStack.removeLast()
        redoStack.addLast(current)
        return previous
    }

    fun redo(current: EditorSnapshot): EditorSnapshot? {
        if (redoStack.isEmpty()) return null
        val next = redoStack.removeLast()
        undoStack.addLast(current)
        return next
    }

    fun clear() {
        undoStack.clear()
        redoStack.clear()
    }
}

typealias UndoRedoManager = PresentationCaretaker
