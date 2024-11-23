package catgirlroutes.commands

import catgirlroutes.utils.ChatUtils.modMessage
import com.github.stivais.commodore.Node
import com.github.stivais.commodore.utils.*
import net.minecraft.command.CommandBase
import net.minecraft.command.ICommandSender
import net.minecraft.util.BlockPos
import net.minecraftforge.client.ClientCommandHandler

val commodoreHandler = LegacyCommodore { root, cause ->
    val builder = StringBuilder("§cInvalid command.\n§7  Did you mean to run:§r\n").apply {
        println(cause)
        buildTree(cause)
        findCorrespondingNode(root, "help")?.let {
            append("\n  §7Run /${getArgumentsRequired(it).joinToString(" ")} for more help.")
        }
    }
    modMessage(builder.toString())
}

private fun StringBuilder.buildTree(node: Node) {
    node.children?.let {
        for (child in it) {
            buildTree(child)
        }
    }
    node.executables?.let {
        for (child in it) {
            append("  /${getArgumentsRequired(node).joinToString(" ")}")
            for (parser in child.parsers) {
                append(" <${parser.id}${if(parser.isOptional) "?" else ""}>")
            }
            append("\n")
        }
    }
    if (node.executables == null && node.children == null) {
        append("  /${getArgumentsRequired(node).joinToString(" ")}\n")
    }
}

fun commodore(vararg string: String, block: Node.() -> Unit): CommandBase {
    return object : CommandBase() {

        private val root = Node(string[0])

        init {
            root.block()
            if (findCorrespondingNode(root, "help") == null) { // creates a (barebones) help node for the command
                root.literal("help").runs {
                    val builder = StringBuilder("List of commands for /${root.name}:\n").also {
                        it.buildTree(root)
                        it.setLength(it.length - 1)
                    }
                    modMessage(builder.toString())
                }
            }
            root.build()
            commodoreHandler.register(root)
        }

        override fun getCommandName(): String = string[0]

        override fun getCommandAliases(): List<String> = string.drop(1)

        override fun getCommandUsage(sender: ICommandSender?): String = "/${commandName}"

        override fun getRequiredPermissionLevel(): Int = 0

        override fun processCommand(sender: ICommandSender, args: Array<out String>) {
            commodoreHandler.execute(args.fix(), root)
        }

        override fun addTabCompletionOptions(
            sender: ICommandSender,
            args: Array<out String>,
            pos: BlockPos
        ): List<String> {
            return commodoreHandler.completions(args.fix())
        }

        inline fun Array<out String>.fix(): String {
            return if (this.isEmpty()) commandName else "$commandName ${this.joinToString(separator = " ")}"
        }
    }
}

fun registerCommands(vararg command: CommandBase) {
    for (i in command) {
        ClientCommandHandler.instance.registerCommand(i)
    }
}