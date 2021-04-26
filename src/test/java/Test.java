import com.github.myibu.regex.Matcher;
import com.github.myibu.regex.Pattern;

public class Test {
    public static void main(String[] args) {
        Matcher matcher = Pattern.compile("(a|b)*c");
        boolean d = matcher.matches("ac");
        System.out.println(d);
    }
}
