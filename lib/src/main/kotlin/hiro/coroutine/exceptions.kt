package hiro.coroutine

import kotlinx.coroutines.CoroutineDispatcher

class UnsupportedDispatcherError(dispatcher: CoroutineDispatcher?) : Exception(
  "Unsupported dispatcher: ${
    if (dispatcher == null) "null" else dispatcher::class.qualifiedName
  }"
)
