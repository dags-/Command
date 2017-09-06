import me.dags.command.annotation.*;

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
    @Command("print")
    public void twoA(@Src Object source) {
        System.out.printf("This command is working? @%s\n", source);
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

    @Command("all thing <nums> <num>")
    public void match(Collection<TestEnum> nums, TestEnum num) {
        System.out.println("-------------");
        nums.forEach(System.out::println);
        System.out.println("## " + num);
    }
}
