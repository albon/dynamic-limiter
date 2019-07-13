package bj.albon.arith.dynamic.limiter.example;

public class Solution {
    public int myAtoi(String str) {
        if (str == null || str.trim().length() == 0) {
            return 0;
        }
        str = str.trim();

        for (int i=0; i<str.length(); ++i) {
            char c = str.charAt(i);
            if (i == 0) {
                if (c == '-' || c == '+') {
                    continue;
                }
            }

            if (c >= '0' && c <= '9') {
                continue;
            } else {
                return 0;
            }

        }

        int num = 0;
        int base = 1;
        for (int i = str.length() - 1; i >= 0; --i) {
            char c = str.charAt(i);

            if (c >= '0' && c <= '9') {
                num += base * (c - '0');
                base *=10;
            } else if (c == '-') {
                num *= -1;
            }
        }
        return num;
    }

    public static void main(String[] args) {
        System.out.println("123");
    }
}
