package me.centauri07.chatreport.util.callback

import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent
import net.dv8tion.jda.api.hooks.SubscribeEvent
import net.dv8tion.jda.api.interactions.modals.Modal

open class Callback<C, E> {

    private val queue: MutableMap<C, (E) -> Unit> = mutableMapOf()

    fun queue(component: C, eventAction: (E) -> Unit) {
        queue[component] = eventAction
    }

    fun dequeue(component: C, event: E) {
        queue[component]?.let {
            it(event)
            queue.remove(component)
        }
    }

}

object ModalCallback: Callback<String, ModalInteractionEvent>() {

    @SubscribeEvent
    fun on(e: ModalInteractionEvent) {
        dequeue(e.modalId, e)
    }

}

fun Modal.callback(eventAction: (ModalInteractionEvent) -> Unit): Modal {
    ModalCallback.queue(id, eventAction)

    return this
}