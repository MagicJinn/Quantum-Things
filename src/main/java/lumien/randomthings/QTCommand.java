package lumien.randomthings;

import net.minecraft.command.ICommandSender;

// Alias for RTCommand, referring to Quantum Things
public class QTCommand extends RTCommand {
    private static final String COMMAND_ROOT = "qt";
    private static final String COMMAND_PREFIX = "/" + COMMAND_ROOT;

    @Override
    public String getName() {
        return COMMAND_ROOT;
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return COMMAND_PREFIX;
    }
}
