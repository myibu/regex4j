package com.github.myibu.regex;

import com.github.myibu.regex.nfa.NFA;

import java.util.*;

/**
 * 参考https://deniskyashif.com/2019/02/17/implementing-a-regular-expression-engine/
 * 1.插入连接运算符，比如： (a|b)*c -> (a|b)*.c
 * 2.转成成后缀表达式，比如：(a|b)*.c -> ab∣*c.
 * 3.构造一个NFA
 * @author myibu
 * @since 1.0
 */
public class Pattern {
    private NFA nfa;

    public NFA nfa() {
        return nfa;
    }

    private Pattern(String regex) {
        String postfixExp = toPostfix(insertExplicitConcatOperator(regex));
        this.nfa = toNFA(postfixExp);
    }

    public static Matcher compile(String regex) {
        Pattern pattern = new Pattern(regex);
        return new Matcher(pattern);
    }

    private static NFA toNFA(String postfixExp) {
        if("".equals(postfixExp)) {
            return NFA.fromEpsilon();
        }

        Stack<NFA> stack = new Stack<>();
        for(char token : postfixExp.toCharArray()) {
            if(token == '*') {
                stack.push(NFA.closure(stack.pop()));
            } else if (token == '|') {
                NFA right = stack.pop();
                NFA left = stack.pop();
                stack.push(NFA.union(left, right));
            } else if (token == '.') {
                NFA right = stack.pop();
                NFA left = stack.pop();
                stack.push(NFA.concat(left, right));
            } else {
                stack.push(NFA.fromSymbol(token));
            }
        }

        return stack.pop();
    }

    /**
     * (a|b)*c => (a|b)*.c
     */
    private String insertExplicitConcatOperator(String exp) {
        StringBuilder output = new StringBuilder();
        for (int i = 0; i < exp.length(); i++) {
            char token = exp.charAt(i);
            output.append(token);
            if (token == '(' || token == '|') {
                continue;
            }
            if (i < exp.length() -1) {
                char lookahead = exp.charAt(i+1);
                if (lookahead == '*' || lookahead == '?' || lookahead == '+' || lookahead == '|' || lookahead == ')') {
                    continue;
                }
                output.append('.');
            }
        }
        return output.toString();
    }

    private static final Map<Character, Integer> operatorPrecedence = new HashMap<>();
    static  {
        operatorPrecedence.put('|', 0);
        operatorPrecedence.put('.', 1);
        operatorPrecedence.put('?', 2);
        operatorPrecedence.put('*', 2);
        operatorPrecedence.put('+', 2);
    }

    /**
     * (a|b)*.c => ab∣*c.
     */
    private String toPostfix(String exp) {
        StringBuilder output = new StringBuilder();
        Stack<Character> operatorStack = new Stack<>();
        for(char token : exp.toCharArray()) {
            if (token == '.' || token == '|' || token == '*' || token == '?' || token == '+') {
                while (!operatorStack.isEmpty() && operatorStack.peek() != '('
                        && operatorPrecedence.get(operatorStack.peek()) >= operatorPrecedence.get(token)) {
                    output.append(operatorStack.pop());
                }

                operatorStack.push(token);
            } else if (token == '(' || token == ')') {
                if (token == '(') {
                    operatorStack.push(token);
                } else {
                    while (operatorStack.peek() != '(') {
                        output.append(operatorStack.pop());
                    }
                    operatorStack.pop();
                }
            } else {
                output.append(token);
            }
        }
        while (!operatorStack.isEmpty()) {
            output.append(operatorStack.pop());
        }

        return output.toString();
    }
}
