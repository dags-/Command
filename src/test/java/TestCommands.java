import block.BlockState;
import block.BlockType;
import me.dags.command.annotation.Command;

/**
 * @author dags <dags@dags.me>
 */
public class TestCommands {

    @Command("get block <block>")
    public void block(BlockType type) {
        System.out.printf("Block: %s\n", type.getId());
    }

    @Command("get state <state>")
    public void state(BlockState state) {
        System.out.printf("State: %s\n", state.getId());
    }
}
