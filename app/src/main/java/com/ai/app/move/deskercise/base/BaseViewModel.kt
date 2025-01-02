package com.ai.app.move.deskercise.base

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import com.ai.app.move.deskercise.utils.SingleLiveEvent

open class BaseViewModel : ViewModel() {
    sealed class MessageEvent {
        class StringMessage(val message: String) : MessageEvent()
        class ResourceMessage(@StringRes val message: Int) : MessageEvent()
    }

    val messageLiveData = SingleLiveEvent<MessageEvent>()

    protected fun publishMessage(message: String) {
        messageLiveData.postValue(MessageEvent.StringMessage(message))
    }

    protected fun publishMessage(@StringRes message: Int) {
        messageLiveData.postValue(MessageEvent.ResourceMessage(message))
    }
}
