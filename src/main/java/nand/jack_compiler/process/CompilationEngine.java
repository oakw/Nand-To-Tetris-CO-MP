package nand.jack_compiler.process;


import nand.jack_analyzer.process.KeywordEnum;
import nand.vm_translator.process.command.ArithmeticType;
import nand.vm_translator.process.command.SegmentType;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Stack;

public class CompilationEngine {

    private ArrayList<String> inputFiles;
    private Document xml;
    private VMWriter vmWriter;
    private String className;

    public CompilationEngine(ArrayList<String> inputFiles) {
        this.inputFiles = inputFiles;
    }

    public void compileAll() {
        inputFiles.forEach(file -> compile(file));
    }

    public void compile(String fileName) {
        try {
            vmWriter = new VMWriter(fileName.replace(".jack", ".vm"));

            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();

            FileInputStream fr = new FileInputStream(fileName.replace(".jack", ".xml"));
            xml = db.parse(fr);
            xml.getDocumentElement().normalize();
            Node programClass = xml.getElementsByTagName("class").item(0);

            compileClass(programClass);
            fr.close();
            vmWriter.close();

        } catch (Exception e) {
            System.err.println("Error occurred while reading " + fileName);
            e.printStackTrace();
        }
    }

    public void compileClass(Node programClass) throws IOException {
        className = getElement(programClass, "identifier").getTextContent();

        NodeList subroutines = ((Element) (programClass)).getElementsByTagName("subroutineDec");
        for (int i = 0; i < subroutines.getLength(); i++) {
            compileSubRoutine(subroutines.item(i));
        }
    }

    public void compileSubRoutine(Node fnElement) throws IOException {
        vmWriter.writeFunction(className + "." + getElement(fnElement, "identifier").getTextContent(), 0);
        compileStatements(getElement(fnElement, "statements"));
    }

    public void compileStatements(Node stsElement) throws IOException {
        NodeList statements = stsElement.getChildNodes();

        for (int i = 1; i < statements.getLength(); i++) {
            if (statements.item(i).getNodeType() == Node.ELEMENT_NODE) {
                compileStatement(statements.item(i));
            }
        }
    }

    public void compileStatement(Node stElement) throws IOException {
        KeywordEnum statementType = KeywordEnum.valueOfLabel(getElement(stElement, "keyword").getTextContent());

        switch (statementType) {
            case K_DO -> compileDo(stElement);
            case K_RETURN -> compileReturn();
        }
    }

    public void compileDo(Node stElement) throws IOException {
        StringBuilder statementName = new StringBuilder();
        int i = 2;
        while(stElement.getChildNodes().item(i + 2 ).getNodeName() != "expressionList") {
            statementName.append(stElement.getChildNodes().item(i).getTextContent().trim().replaceAll("\\s+"," "));
            i++;
        }

        Node expressionList = getElement(stElement, "expressionList");
        compileExpressionList(expressionList);
        vmWriter.writeCall(statementName.toString(), 1);
    }

    public void compileReturn() throws IOException {
        vmWriter.writePush(SegmentType.S_CONSTANT, 0);
        vmWriter.writeReturn();
    }

    public void compileExpressionList(Node listElement) throws IOException {
        String expressionListTextContent = listElement.getTextContent().trim().replaceAll("\\s+"," ");
        ArrayList<String> postfixExpressionList = infixToPostfix(expressionListTextContent);

        for (int i = 0; i < postfixExpressionList.size(); i++) {
            String term = postfixExpressionList.get(i);

            if (term.matches("[0-9]+")) {
                vmWriter.writePush(SegmentType.S_CONSTANT, Integer.parseInt(term));
            } else if (term.equals("*")) {
                vmWriter.writeCall("Math.multiply", 2);
            } else if (term.equals("/")) {
                vmWriter.writeCall("Math.divide", 2);
            } else if (term.equals("+")) {
                vmWriter.writeArithmetic(ArithmeticType.A_ADD);
            } else if (term.equals("-")) {
                vmWriter.writeArithmetic(ArithmeticType.A_SUB);
            }
        }
    }


    private Node getElement(Node node, String tagName, Integer... index) {
        if (node.getNodeType() == Node.ELEMENT_NODE) {
            return ((Element) node).getElementsByTagName(tagName).item(index.length > 0 ? index[0] : 0);
        } else {
            return node;
        }
    }


    // Converts infix expression to postfix notation
    // Based on https://www.geeksforgeeks.org/convert-infix-expression-to-postfix-expression/
    static ArrayList<String> infixToPostfix(String exp)
    {
        ArrayList<String> result = new ArrayList<>();
        Stack<String> stack = new Stack<>();

        while (true) {
            String s = exp.substring(0, exp.contains(" ") ? exp.indexOf(" ") : exp.length());
            exp = exp.substring(exp.indexOf(" ") + 1);

            // If operand, add to output
            if (s.matches("[0-9]+")) {
                result.add(s);

            // If '(', add to stack
            } else if (s.equals("(")) {
                stack.push(s);

            // If ')', add everything from stack until '('
            } else if (s.equals(")")) {
                while (!stack.isEmpty() && !Objects.equals(stack.peek(), "(")) {
                    result.add(stack.peek());
                    stack.pop();
                }
                stack.pop();

            // Operator
            } else {
                while (!stack.isEmpty() && operatorPrecedence(s.charAt(0)) <= operatorPrecedence(stack.peek().charAt(0))) {
                    result.add(stack.peek());
                    stack.pop();
                }
                stack.push(s);
            }

            if (s.equals(exp)) break;
        }

        // Pop all the operators from the stack
        while (!stack.isEmpty()) {
            result.add(stack.peek());
            stack.pop();
        }

        return result;
    }


    // Returns the weight (importance) of an operator according to PEMDAS
    static int operatorPrecedence(char ch)
    {
        switch (ch) {
            case '+':
            case '-':
                return 1;
            case '*':
            case '/':
                return 2;
        }
        return -1;
    }

}
