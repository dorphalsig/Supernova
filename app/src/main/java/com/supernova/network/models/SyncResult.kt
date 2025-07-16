sealed class SyncResult {
    object Success : SyncResult()
    data class Error(val message: String) : SyncResult()
    data class Progress(val step: String, val current: Int, val total: Int) : SyncResult()
}
