import me.dags.command.annotation.Command;
import me.dags.command.annotation.Description;
import me.dags.command.annotation.Permission;
import me.dags.command.annotation.Role;

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

    @Permission
    @Command("test <num> set <name>")
    public void twoB(TestEnum num, Object user) {
        System.out.printf("PromoteB user: %s, Enum flag: %s\n", user, num);
    }

    @Permission(role = @Role("admin"))
    @Command("user num <num>")
    @Description("Demote a user")
    public void three(TestEnum num) {
        System.out.printf("Num: %s\n", num);
    }
}
