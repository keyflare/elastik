package com.keyflare.elastik.core

internal object Errors {

    fun destinationAlreadyExists(destinationId: String) =
        "Destination with the ID \"$destinationId\" already exists. Each destination must have a unique for whole router tree ID."

    fun routerNotFound(destinationId: String) =
        "Router for the destination \"$destinationId\" not found. It seems there is no such a backstack entry in the backstack."

    fun componentNotFound(destinationId: String) =
        "Component for the destination \"$destinationId\" not found. It seems there is no such a backstack entry in the backstack."

    fun getNewRouterDataError() =
        "No data provided for a new router"

    fun noBackstackAssociated(backstackEntryId: Int) =
        "There is no backstack associated with this router (\"$backstackEntryId\")"

    fun backstackEntryUnexpectedType(backstackEntryId: Int, backstackExpected: Boolean) =
        "Backstack entry with ID \"$backstackEntryId\" has unexpected type: ${if (backstackExpected) "backstack" else "single"} expected"

    fun parentRouterContextMismatch(destinationId: String) =
        "Parent router context mismatch for destination \"$destinationId\""

    fun destinationBindingNotFound(destinationId: String, isSingle: Boolean) =
        "Binding for ${if (isSingle) "single" else "backstack"} destination with ID $destinationId not found"

    fun renderNotFound(backstackEntryId: Int) =
        "Render for backstack entry with id $backstackEntryId not found"
}
