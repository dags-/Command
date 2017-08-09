import me.dags.command.annotation.Command;
import me.dags.command.annotation.Description;
import me.dags.command.annotation.Flag;
import me.dags.command.annotation.Permission;
import me.dags.command.command.Flags;

/**
 * @author dags <dags@dags.me>
 */
public class TestCommands {

    @Permission("user.perm.set")
    @Command("user|u <name> perm set <permission> <value>")
    @Description("Set the permission value for a given user")
    public void one(String user, String perm, boolean value) {
        System.out.printf("Set user: %s, permission: %s, to: %s\n", user, perm, value);
    }

    @Flag("t")
    @Flag(value = "p", type = TestEnum.class)
    @Command("user promote <user>")
    public void two(String user, Flags flags) {
        System.out.printf("Promote user: %s, bool flag: %s, int flag: %s", user, flags.getOr("t", false), flags.getOr("p", TestEnum.NO));
    }

    @Command("user demote <user>")
    @Description("Demote a user")
    public void three(String user, TestEnum... examples) {
        for (TestEnum num : examples) {
            System.out.printf("User: %s, Enum: %s\n", user, num);
        }
    }
}
