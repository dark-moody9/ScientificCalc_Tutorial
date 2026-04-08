/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.mycompany.scientificcalculator;

/**
 *
 * @author Darkmoody
 */
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Stack;

public class ScientificCalculator extends JFrame implements ActionListener {

    private JTextField display;
    private JTextArea history;
    private StringBuilder input = new StringBuilder();

    public ScientificCalculator() {
        setTitle("Scientific Calculator");
        setSize(450, 650);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        display = new JTextField();
        display.setFont(new Font("Arial", Font.BOLD, 28));
        display.setEditable(false);
        display.setHorizontalAlignment(SwingConstants.RIGHT);
        add(display, BorderLayout.NORTH);

        history = new JTextArea(5, 20);
        history.setEditable(false);
        add(new JScrollPane(history), BorderLayout.SOUTH);

        JPanel panel = new JPanel(new GridLayout(7,4,5,5));

        String[] buttons = {
            "(",")","C","/",
            "7","8","9","*",
            "4","5","6","-",
            "1","2","3","+",
            "0",".","=","^",
            "sin","cos","tan","√",
            "log","ln","",""
        };

        for(String b : buttons){
            JButton btn = new JButton(b);
            btn.setFont(new Font("Arial", Font.BOLD, 16));
            btn.addActionListener(this);
            panel.add(btn);
        }

        add(panel, BorderLayout.CENTER);
        setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String cmd = e.getActionCommand();

        switch(cmd){
            case "=":
                try {
                    double result = evaluate(input.toString());
                    display.setText(String.valueOf(result));
                    history.append(input + " = " + result + "\n");
                    input.setLength(0);
                } catch (ArithmeticException ex){
                    display.setText("Undefined!");
                    input.setLength(0);
                } catch (Exception ex){
                    display.setText("Error");
                    input.setLength(0);
                }
                break;

            case "C":
                input.setLength(0);
                display.setText("");
                break;

            case "√":
                input.append("sqrt("); // ALWAYS correct format
                display.setText(input.toString());
                break;

            case "sin": case "cos": case "tan":
            case "log": case "ln":
                input.append(cmd).append("(");
                display.setText(input.toString());
                break;

            default:
                input.append(cmd);
                display.setText(input.toString());
        }
    }

    public static void main(String[] args) {
        new ScientificCalculator();
    }

    // ================= EVALUATOR =================

    private double evaluate(String expr){
        return parse(expr.replaceAll("\\s+",""));
    }

    private double parse(String expr){
        Stack<Double> nums = new Stack<>();
        Stack<Character> ops = new Stack<>();

        for(int i=0;i<expr.length();){
            char c = expr.charAt(i);

            // NUMBER (supports negative)
            if(Character.isDigit(c) || c=='.' ||
              (c=='-' && (i==0 || expr.charAt(i-1)=='(' || isOp(expr.charAt(i-1))))){

                StringBuilder sb = new StringBuilder();

                if(c=='-'){ sb.append('-'); i++; }

                while(i<expr.length() &&
                      (Character.isDigit(expr.charAt(i)) || expr.charAt(i)=='.')){
                    sb.append(expr.charAt(i++));
                }

                nums.push(Double.parseDouble(sb.toString()));
            }

            // (
            else if(c=='('){
                ops.push(c);
                i++;
            }

            // )
            else if(c==')'){
                while(ops.peek()!='(') apply(nums, ops.pop());
                ops.pop();
                i++;
            }

            // operator
            else if(isOp(c)){
                while(!ops.isEmpty() && priority(ops.peek())>=priority(c)){
                    apply(nums, ops.pop());
                }
                ops.push(c);
                i++;
            }

            // FUNCTION (sin, sqrt, etc.)
            else{
                StringBuilder func = new StringBuilder();
                while(i<expr.length() && Character.isLetter(expr.charAt(i))){
                    func.append(expr.charAt(i++));
                }

                double val;

                if(i < expr.length() && expr.charAt(i)=='('){
                    i++;
                    int count=1;
                    StringBuilder inside = new StringBuilder();

                    while(count!=0){
                        char ch = expr.charAt(i++);
                        if(ch=='(') count++;
                        if(ch==')') count--;
                        if(count!=0) inside.append(ch);
                    }

                    val = parse(inside.toString());
                } else {
                    // support sqrt9, sin30
                    StringBuilder num = new StringBuilder();
                    while(i<expr.length() &&
                          (Character.isDigit(expr.charAt(i)) || expr.charAt(i)=='.')){
                        num.append(expr.charAt(i++));
                    }
                    val = Double.parseDouble(num.toString());
                }

                nums.push(applyFunc(func.toString(), val));
            }
        }

        while(!ops.isEmpty()) apply(nums, ops.pop());

        return nums.pop();
    }

    private boolean isOp(char c){
        return "+-*/^".indexOf(c)!=-1;
    }

    private int priority(char op){
        switch(op){
            case '+': case '-': return 1;
            case '*': case '/': return 2;
            case '^': return 3;
        }
        return 0;
    }

    private void apply(Stack<Double> nums, char op){
        double b = nums.pop();
        double a = nums.pop();

        switch(op){
            case '+': nums.push(a+b); break;
            case '-': nums.push(a-b); break;
            case '*': nums.push(a*b); break;
            case '/':
                if(b==0) throw new ArithmeticException();
                nums.push(a/b);
                break;
            case '^': nums.push(Math.pow(a,b)); break;
        }
    }

    private double applyFunc(String f, double v){
        switch(f){
            case "sin": return Math.sin(Math.toRadians(v));
            case "cos": return Math.cos(Math.toRadians(v));
            case "tan": return Math.tan(Math.toRadians(v));
            case "log": return Math.log10(v);
            case "ln": return Math.log(v);
            case "sqrt": return Math.sqrt(v);
        }
        return v;
    }
}