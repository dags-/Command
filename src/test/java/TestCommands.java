import me.dags.command.annotation.*;
import me.dags.command.command.Flags;

import java.util.Collection;

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

    @Permission
    @Command("user promote <user>")
    public void twoA(String user) {
        System.out.printf("PromoteA user: %s", user);
    }

    @Flag("t")
    @Flag(value = "p", type = TestEnum.class)
    @Permission
    @Command("user promote <user>")
    public void twoB(String user, Flags flags) {
        System.out.printf("PromoteB user: %s, bool flag: %s, int flag: %s", user, flags.getOr("t", false), flags.getOr("p", TestEnum.NO));
    }

    @Permission(role = @Role("admin"))
    @Command("user demote <user>")
    @Description("Demote a user")
    public void three(String user, Collection<TestEnum> examples) {
        System.out.printf("User: %s, Enum: %s\n", user, examples);
    }
}
