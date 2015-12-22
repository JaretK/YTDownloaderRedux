import java.util.Arrays;

public class test {

	public static void main(String[] args) {
		String s = "\u00F8";
		String s2 = "ø";
		System.out.println(s);
		System.out.println(s2);
		System.out.println(Arrays.toString(s.getBytes()));
	}
}
