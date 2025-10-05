package nl.connectplay.scoreplay.models

/**
 * This is an interface for `dynamic dispatch` on enum classes.
 *
 * With this interface you can give every enum value its own implementation,
 * and at runtime it will automatically call `handle()` for the right enum value
 *
 * This way you prevent to have a `when`-statement on each enum value,
 * keeping the code cleaner and more maintainable.
 *
 * **Dynamic dispatch example:**
 * ```
 * enum class UserStatus : EnumAction {
 *     ONLINE {
 *         override fun handle() { // own implementation
 *             println("User Online")
 *         }
 *     },
 *     OFFLINE {
 *         override fun handle() { println("User Offline") }
 *     }
 * }
 *
 * fun process(action: EnumAction){
 *     action.handle()
 * }
 *
 * process(UserStatus.ONLINE) // -> prints "User Online"
 * process(UserStatus.OFFLINE) // -> prints "User Offline"
 * ```
 */
interface EnumAction {
    fun handle()
}