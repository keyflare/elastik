package com.keyflare.elastik.core

internal object Errors {

    fun destinationAlreadyExists(destinationId: String) =
        "Destination with the ID \"$destinationId\" already exists. Each destination must have a unique for whole router tree ID."

    fun entryNotFound(entryId: Int) =
        "Entry with ID \"$entryId\" not found."

    fun routerNotFound(destinationId: String) =
        "Router for the destination \"$destinationId\" not found. It seems there is no such a stack entry in the stack."

    fun componentNotFound(destinationId: String) =
        "Component for the destination \"$destinationId\" not found. It seems there is no such a stack entry in the stack."

    fun destinationBindingNotFound(destinationId: String, isSingle: Boolean) =
        "Binding for ${if (isSingle) "single" else "stack"} destination with ID $destinationId not found"

    fun renderNotFound(entryId: Int) =
        "Render for stack entry with id $entryId not found"

    fun getNewRouterDataError() =
        "No data provided for a new router"

    fun noStackAssociated(entryId: Int) =
        "There is no stack associated with this router (\"$entryId\")"

    fun entryUnexpectedType(entryId: Int, stackExpected: Boolean) =
        "Entry with ID \"$entryId\" has unexpected type: ${if (stackExpected) "Stack" else "Single"} expected"

    fun parentRouterContextMismatch(destinationId: String) =
        "Parent router context mismatch for destination \"$destinationId\""

}
